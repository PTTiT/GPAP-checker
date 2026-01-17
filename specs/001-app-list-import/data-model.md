# Data Model: Custom App List Import

**Feature**: 001-app-list-import
**Date**: 2026-01-17
**Status**: Complete

## Overview

This document defines the data structures, entities, and state management for the custom app list import feature. All entities follow Kotlin data class conventions and integrate with the existing MVVM architecture.

---

## Core Entities

### ValidationResult

**Purpose**: Encapsulates the result of package name validation during import

**Location**: `app/src/main/java/com/geocomply/test/gpapchecker/data/ValidationResult.kt`

**Definition**:
```kotlin
package com.geocomply.test.gpapchecker.data

data class ValidationResult(
    val validCount: Int,
    val invalidCount: Int,
    val validPackages: List<String>,
    val invalidPackages: List<String>
) {
    val totalCount: Int get() = validCount + invalidCount
    val hasValidPackages: Boolean get() = validCount > 0
    val hasInvalidPackages: Boolean get() = invalidCount > 0
}
```

**Fields**:
- `validCount`: Number of package names that passed validation (Int, >= 0)
- `invalidCount`: Number of package names that failed validation (Int, >= 0)
- `validPackages`: List of valid package names (List<String>, unique, trimmed)
- `invalidPackages`: List of invalid package names that were rejected (List<String>, for user feedback)

**Computed Properties**:
- `totalCount`: Sum of valid and invalid packages
- `hasValidPackages`: Boolean flag for UI button enablement (FR-016, FR-017)
- `hasInvalidPackages`: Boolean flag for displaying validation feedback (FR-014, FR-015)

**Validation Rules**:
- All strings must be non-empty after trimming
- `validPackages` must match Android package name regex: `^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$`
- No duplicates in `validPackages` list

**Relationships**:
- Used by `ImportState.ValidationComplete` to pass validation results to UI
- Created by `PackageListParser.parse()` method

---

### ImportState (Sealed Class)

**Purpose**: Represents all possible states of the import screen UI (following existing `UiState` pattern)

**Location**: `app/src/main/java/com/geocomply/test/gpapchecker/data/ImportState.kt`

**Definition**:
```kotlin
package com.geocomply.test.gpapchecker.data

sealed class ImportState {
    object Initial : ImportState()
    object ParsingInput : ImportState()
    object ReadingFile : ImportState()
    data class ValidationComplete(val result: ValidationResult) : ImportState()
    data class ImportSuccess(val packageCount: Int, val mode: ImportMode) : ImportState()
    data class Error(val message: String) : ImportState()
}

enum class ImportMode {
    REPLACE,
    APPEND
}
```

**States**:

1. **Initial**: Default state when import screen loads
   - Displayed: Empty text field, disabled import button
   - Transitions to: `ParsingInput` (user enters text), `ReadingFile` (user selects file)

2. **ParsingInput**: Actively parsing text input or file content
   - Displayed: Progress indicator (brief, < 1 second for typical input)
   - Transitions to: `ValidationComplete` (parsing done), `Error` (parsing failed)

3. **ReadingFile**: Reading file content from SAF URI
   - Displayed: Progress indicator
   - Transitions to: `ParsingInput` (file read complete), `Error` (file read failed)

4. **ValidationComplete**: Validation finished with results
   - Data: `ValidationResult` with counts and package lists
   - Displayed: Validation summary ("X valid, Y invalid"), enabled/disabled import button
   - Transitions to: `ImportSuccess` (user taps import), `Initial` (user clears input)

5. **ImportSuccess**: Import completed successfully
   - Data: `packageCount` (Int), `mode` (ImportMode enum)
   - Displayed: Success message, navigation back to main screen
   - Transitions to: N/A (screen closes)

6. **Error**: Import failed with error message
   - Data: `message` (String) - human-readable error description
   - Displayed: Error message in UI
   - Transitions to: `Initial` (user dismisses error)

**State Transitions**:
```
Initial
  ├→ ParsingInput (text input changed)
  └→ ReadingFile (file selected)

ReadingFile
  ├→ ParsingInput (file read success)
  └→ Error (file read failed: permissions, corrupted, >1MB)

ParsingInput
  ├→ ValidationComplete (parsing success)
  └→ Error (parsing failed: encoding issue)

ValidationComplete
  ├→ ImportSuccess (user taps import with valid packages)
  └→ Initial (user clears input)

Error
  └→ Initial (user dismisses)

ImportSuccess
  └→ [Navigate to MainActivity]
```

**Relationships**:
- Exposed as `LiveData<ImportState>` by `ImportViewModel`
- Observed by `ImportActivity` to update UI
- Created by ViewModel based on repository/parser operations

---

### ImportMode (Enum)

**Purpose**: Defines whether import replaces or appends to existing package list

**Location**: Included in `ImportState.kt`

**Definition**:
```kotlin
enum class ImportMode {
    REPLACE,  // Default: Clear existing list and use imported packages
    APPEND    // Opt-in: Merge imported packages with existing list
}
```

**Usage**:
- Selected via RadioGroup in UI (REPLACE is default/pre-selected per clarifications)
- Passed to `PackageListRepository.savePackages(packages, mode)` method
- Affects how `PackageListRepository` merges with existing list

---

## Storage Schema

### SharedPreferences Keys

**Location**: `app/src/main/java/com/geocomply/test/gpapchecker/utils/PackageListStorage.kt`

**Schema**:
```kotlin
private const val PREFS_NAME = "package_list_prefs"

// Keys
private const val KEY_CUSTOM_PACKAGES = "custom_packages"     // Type: String (JSON array)
private const val KEY_IS_CUSTOM_LIST = "is_custom_list"       // Type: Boolean
private const val KEY_LAST_IMPORT_TIME = "last_import_time"   // Type: Long (timestamp)
```

**Stored Data**:

1. **custom_packages** (String):
   - Format: JSON array of package name strings
   - Example: `["com.example.app1", "com.example.app2", "com.test.app3"]`
   - Max size: ~1MB ≈ 50,000 packages (well above 1000-package requirement)
   - Encoding: UTF-8

2. **is_custom_list** (Boolean):
   - `false`: No custom import yet, use hardcoded 213-package list (default)
   - `true`: Custom list imported, use `custom_packages` value
   - Per clarification: First launch detection mechanism

3. **last_import_time** (Long):
   - Unix timestamp (milliseconds since epoch)
   - Used for debugging/analytics (not required for feature functionality)
   - Optional: Can be used for "last imported" display in UI

**Access Pattern**:
```kotlin
// Read
fun getPackageList(): List<String> {
    if (!prefs.getBoolean(KEY_IS_CUSTOM_LIST, false)) {
        return HardcodedPackages.DEFAULT_LIST  // First launch
    }
    val json = prefs.getString(KEY_CUSTOM_PACKAGES, "[]") ?: "[]"
    return Json.decodeFromString<List<String>>(json)
}

// Write
fun savePackageList(packages: List<String>) {
    val json = Json.encodeToString(packages)
    prefs.edit().apply {
        putString(KEY_CUSTOM_PACKAGES, json)
        putBoolean(KEY_IS_CUSTOM_LIST, true)
        putLong(KEY_LAST_IMPORT_TIME, System.currentTimeMillis())
        apply()  // Async write
    }
}
```

---

## Data Flow Diagram

```
[User Input: Text or File]
         |
         v
   ImportActivity
         |
         v
   ImportViewModel
         |
         v
   PackageListParser.parse(input: String)
         |
         v
   ValidationResult (valid/invalid counts + lists)
         |
         v
   ImportState.ValidationComplete
         |
         v
   [UI: Display validation summary, enable/disable button]
         |
         v (user taps import)
   ImportViewModel.performImport(mode: ImportMode)
         |
         v
   PackageListRepository.savePackages(packages, mode)
         |
         v
   PackageListStorage.savePackageList(merged_packages)
         |
         v
   SharedPreferences (persist JSON)
         |
         v
   ImportState.ImportSuccess
         |
         v
   [Navigate back to MainActivity]
         |
         v
   AppRepository.getPackagesToCheck()
         |
         v
   PackageListStorage.getPackageList()
         |
         v
   [Return custom list or hardcoded list based on is_custom_list flag]
```

---

## Validation Rules Summary

### Package Name Validation (FR-007)

**Pattern**: `^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$`

**Rules**:
1. Must start with lowercase letter
2. Each segment (between dots) must start with lowercase letter
3. Segments can contain: lowercase letters, digits, underscores
4. Must have at least one dot (minimum 2 segments)
5. No uppercase letters, no spaces, no special characters except underscore and dot

**Valid Examples**:
- `com.example.app`
- `com.geocomply.test.gpapchecker`
- `org.company.product_name`
- `a.b` (minimal valid)

**Invalid Examples**:
- `Com.Example.App` (uppercase letters)
- `com.example.app-name` (hyphen not allowed)
- `com.example app` (space not allowed)
- `example.app.` (trailing dot)
- `example` (no dot separator)

---

## Performance Characteristics

| Operation | Time Complexity | Space Complexity | Expected Time |
|-----------|----------------|------------------|---------------|
| `ValidationResult` creation | O(n) | O(n) | < 100ms for 1000 packages |
| Package name regex validation | O(m) | O(1) | < 1ms per package |
| SharedPreferences write (JSON) | O(n) | O(n) | < 200ms for 1000 packages |
| SharedPreferences read (JSON) | O(n) | O(n) | < 100ms for 1000 packages |
| File read (1MB .txt) | O(file size) | O(file size) | < 500ms |

*Note: n = number of packages, m = length of package name string*

---

## Testing Considerations

### Unit Test Coverage

1. **ValidationResult**:
   - Test computed properties (`totalCount`, `hasValidPackages`, etc.)
   - Test edge cases (empty lists, all valid, all invalid)

2. **ImportState**:
   - Test state transitions
   - Test data preservation in data classes

3. **PackageListStorage**:
   - Test first launch detection (`is_custom_list` = false)
   - Test JSON serialization/deserialization
   - Test concurrent access (thread safety)

4. **PackageListParser**:
   - Test comma-separated parsing
   - Test line-separated parsing
   - Test mixed format parsing
   - Test whitespace trimming
   - Test duplicate removal
   - Test validation regex (valid/invalid package names)

### Integration Tests

1. File import end-to-end (SAF → read → parse → validate → persist)
2. Text import end-to-end (input → parse → validate → persist)
3. Replace mode (verify old list cleared)
4. Append mode (verify merge behavior)
5. First launch vs subsequent launch (hardcoded vs custom list)

---

## Migration & Backwards Compatibility

**No migration needed** - this is a new feature with no existing persisted data.

**Fallback behavior**:
- If `is_custom_list` = false (default): Use hardcoded 213-package list
- If `custom_packages` key missing: Empty list → triggers error (should never happen if `is_custom_list` = true)
- If JSON parse fails: Log error, fallback to hardcoded list

**Uninstall/reinstall**:
- SharedPreferences data is cleared on app uninstall
- After reinstall: `is_custom_list` = false → hardcoded list used (expected behavior)
