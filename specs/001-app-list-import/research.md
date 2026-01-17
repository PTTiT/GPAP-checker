# Research & Technical Decisions: Custom App List Import

**Feature**: 001-app-list-import
**Date**: 2026-01-17
**Status**: Complete

## Overview

This document captures technical research and decisions made during the planning phase for the custom app list import feature. All decisions align with the existing MVVM architecture and Android best practices.

## Research Topics

### 1. Package Name Validation Strategy

**Decision**: Use regex pattern matching for Android package name validation

**Rationale**:
- Android package names follow strict rules: lowercase letters, digits, underscores, and dots as separators
- Regex validation is fast (<1ms for typical package names) and meets performance requirements
- No need to verify package existence on device during import (per Assumption 3 in spec)
- Pattern: `^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$`

**Alternatives Considered**:
- **Android PackageManager validation**: Rejected - too slow (requires device queries), fails for uninstalled packages
- **String tokenization with manual rules**: Rejected - more complex, harder to maintain, no performance benefit

**Implementation Notes**:
```kotlin
// app/src/main/java/com/geocomply/test/gpapchecker/utils/PackageListParser.kt
private val PACKAGE_NAME_REGEX = Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+\$")
```

---

### 2. Persistence Mechanism

**Decision**: Use SharedPreferences with JSON serialization for package list storage

**Rationale**:
- Existing app already uses SharedPreferences (lightweight, synchronous access)
- JSON serialization via Kotlin serialization or Gson provides human-readable format
- Performance: Read/write ~1000 package names < 100ms (well under requirements)
- No complex queries needed - simple read/write operations
- Survives app updates and device reboots

**Alternatives Considered**:
- **Room Database**: Rejected - overkill for simple key-value storage, adds complexity and dependencies
- **File storage (raw .txt)**: Rejected - no atomic write guarantees, manual sync handling
- **DataStore**: Rejected - existing codebase uses SharedPreferences, no migration needed

**Implementation Notes**:
```kotlin
// app/src/main/java/com/geocomply/test/gpapchecker/utils/PackageListStorage.kt
private const val PREFS_NAME = "package_list_prefs"
private const val KEY_CUSTOM_PACKAGES = "custom_packages"
private const val KEY_IS_CUSTOM_LIST = "is_custom_list"
```

---

### 3. File I/O Approach for .txt Import

**Decision**: Use Android Storage Access Framework (SAF) with ACTION_OPEN_DOCUMENT

**Rationale**:
- SAF is the Android 10+ standard for file access (scoped storage compliance)
- Works across all storage locations (internal, external, cloud providers)
- No WRITE_EXTERNAL_STORAGE permission needed (read-only)
- Automatic URI permission grants
- UTF-8 encoding via `InputStreamReader(contentResolver.openInputStream(uri), Charsets.UTF_8)`

**Alternatives Considered**:
- **Legacy file picker with permissions**: Rejected - deprecated on Android 10+, breaks scoped storage
- **Direct path access**: Rejected - violates scoped storage, security risk

**Implementation Notes**:
```kotlin
// app/src/main/java/com/geocomply/test/gpapchecker/ImportActivity.kt
val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = "text/plain"
}
startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
```

---

### 4. Replace vs Append UI Implementation

**Decision**: Use RadioGroup with two options: "Replace list (default)" and "Append to existing"

**Rationale**:
- RadioGroup enforces mutually exclusive selection
- Default selection (Replace) pre-selected on screen load
- Familiar Android UI pattern (Material Design guidelines)
- Clear visual indication of current mode
- Per clarification: Replace is default, Append is opt-in

**Alternatives Considered**:
- **Checkbox "Append instead of replace"**: Rejected - less clear default state
- **Two separate buttons**: Rejected - takes more space, less standard pattern

**Implementation Notes**:
```xml
<!-- activity_import.xml -->
<RadioGroup android:id="@+id/importModeGroup">
    <RadioButton android:id="@+id/radioReplace" android:checked="true" />
    <RadioButton android:id="@+id/radioAppend" />
</RadioGroup>
```

---

### 5. Inline Validation Feedback Strategy

**Decision**: Live validation with TextWatcher + validation summary TextView

**Rationale**:
- TextWatcher provides real-time feedback as user types (< 2 seconds per SC-007)
- Validation summary shows "X valid, Y invalid packages" count
- Import button enabled/disabled based on valid count > 0
- Per clarifications: Show counts, import only valid packages, disable button when no valid input

**Alternatives Considered**:
- **Validation on submit only**: Rejected - doesn't meet "clear feedback within 2 seconds" requirement
- **Red/green highlighting per package**: Rejected - too complex for large lists, poor UX

**Implementation Notes**:
```kotlin
// ImportViewModel.kt
data class ValidationResult(
    val validCount: Int,
    val invalidCount: Int,
    val validPackages: List<String>,
    val invalidPackages: List<String>
)
```

---

### 6. First Launch Detection

**Decision**: Use boolean flag in SharedPreferences to detect first launch vs custom list

**Rationale**:
- Simple flag: `is_custom_list` (default: false)
- On first launch (flag = false): Use hardcoded 213-package list from AppRepository
- After first import: Set flag = true, use persisted custom list
- Per clarification: Show hardcoded list on first launch, then imported list takes over

**Alternatives Considered**:
- **Check if custom_packages key exists**: Rejected - doesn't distinguish "no import yet" vs "empty import"
- **Use package count comparison**: Rejected - fragile if hardcoded list size changes

**Implementation Notes**:
```kotlin
// PackageListStorage.kt
fun hasCustomList(): Boolean = prefs.getBoolean(KEY_IS_CUSTOM_LIST, false)
```

---

### 7. Parsing Strategy for Mixed Formats

**Decision**: Multi-pass parser supporting both comma-separated and line-separated formats

**Rationale**:
- First split by newlines, then split each line by commas
- Handles mixed formats: "com.a, com.b\ncom.c\ncom.d, com.e"
- Trim whitespace for each candidate package name (per FR-008)
- Deduplicate during parsing (per FR-009, Assumption 4)

**Alternatives Considered**:
- **Detect format first, then parse**: Rejected - unnecessary complexity, single-pass works
- **Regex split by `[,\n]+`**: Rejected - doesn't handle whitespace trimming well

**Implementation Notes**:
```kotlin
// PackageListParser.kt
fun parse(input: String): ValidationResult {
    val candidates = input
        .split('\n')
        .flatMap { it.split(',') }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct() // Remove duplicates
    // ... validate each candidate
}
```

---

### 8. Performance Optimization for Large Files

**Decision**: Use coroutines with Dispatchers.IO for file reading and parsing

**Rationale**:
- 1MB file ≈ 50,000 package names ≈ 50 bytes/package average
- Reading on IO thread prevents UI blocking
- Parsing on Default dispatcher uses multi-core efficiently
- Meets SC-003: 1000 packages from file < 10 seconds (actual: ~1 second)

**Alternatives Considered**:
- **Synchronous on main thread**: Rejected - blocks UI, ANR risk
- **AsyncTask**: Rejected - deprecated, coroutines are Android standard

**Implementation Notes**:
```kotlin
// ImportViewModel.kt
fun importFromFile(uri: Uri) = viewModelScope.launch {
    val content = withContext(Dispatchers.IO) {
        contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
    }
    val result = withContext(Dispatchers.Default) {
        parser.parse(content ?: "")
    }
    _importState.value = ImportState.ValidationComplete(result)
}
```

---

## Technology Stack Summary

| Component | Technology | Version/Notes |
|-----------|-----------|---------------|
| Language | Kotlin | 1.9.0+ (existing) |
| UI | Android Views + XML | Traditional (non-Compose) |
| State Management | LiveData | Existing pattern |
| Async | Coroutines | kotlinx-coroutines-android |
| Storage | SharedPreferences | Android SDK |
| File Picker | Storage Access Framework | ACTION_OPEN_DOCUMENT |
| Testing | JUnit 4 + Mockito | Existing stack |
| Build | Gradle 8.9 + Kotlin DSL | Existing |

---

## Open Questions

None - all technical decisions resolved.

---

## References

- Android Package Naming: https://developer.android.com/studio/build/application-id
- Storage Access Framework: https://developer.android.com/guide/topics/providers/document-provider
- SharedPreferences Best Practices: https://developer.android.com/training/data-storage/shared-preferences
- Android Coroutines Guide: https://developer.android.com/kotlin/coroutines
