# Quickstart Guide: Custom App List Import

**Feature**: 001-app-list-import
**Date**: 2026-01-17
**For**: Developers implementing this feature

## Overview

This guide provides step-by-step implementation instructions for the custom app list import feature. Follow the sections in order for a systematic build-out aligned with the MVVM architecture.

---

## Prerequisites

- Read [spec.md](./spec.md) - Feature specification
- Read [research.md](./research.md) - Technical decisions
- Read [data-model.md](./data-model.md) - Entity definitions
- Familiar with existing MVVM pattern in codebase (see `CLAUDE.md`)

---

## Implementation Phases

### Phase 1: Data Layer (Bottom-Up Approach)

Start with foundational utilities and data models (no UI dependencies).

#### Step 1.1: Create ValidationResult Data Class

**File**: `app/src/main/java/com/geocomply/test/gpapchecker/data/ValidationResult.kt`

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

**Test**: Create `ValidationResultTest.kt` in `app/src/test/java/.../data/`
- Test computed properties
- Test edge cases (empty lists, all valid, all invalid)

---

#### Step 1.2: Create ImportState Sealed Class

**File**: `app/src/main/java/com/geocomply/test/gpapchecker/data/ImportState.kt`

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

**Test**: Create `ImportStateTest.kt`
- Test state equality (data classes)
- Test enum values

---

#### Step 1.3: Implement PackageListParser

**File**: `app/src/main/java/com/geocomply/test/gpapchecker/utils/PackageListParser.kt`

```kotlin
package com.geocomply.test.gpapchecker.utils

import com.geocomply.test.gpapchecker.data.ValidationResult

class PackageListParser {
    companion object {
        private val PACKAGE_NAME_REGEX = Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+\$")
    }

    fun parse(input: String): ValidationResult {
        val candidates = input
            .split('\n')
            .flatMap { line -> line.split(',') }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        val (valid, invalid) = candidates.partition { isValidPackageName(it) }

        return ValidationResult(
            validCount = valid.size,
            invalidCount = invalid.size,
            validPackages = valid,
            invalidPackages = invalid
        )
    }

    private fun isValidPackageName(name: String): Boolean {
        return PACKAGE_NAME_REGEX.matches(name)
    }
}
```

**Test**: Create `PackageListParserTest.kt` with extensive coverage:
- Comma-separated input: `"com.a, com.b, com.c"`
- Line-separated input: `"com.a\ncom.b\ncom.c"`
- Mixed format: `"com.a, com.b\ncom.c"`
- Whitespace handling: `" com.a , com.b \n com.c "`
- Duplicates: `"com.a, com.a, com.b"` → Should deduplicate
- Valid package names: `com.example.app`, `org.test.app_123`
- Invalid package names: `Com.A` (uppercase), `com.a-b` (hyphen), `example` (no dot)
- Empty input: `""` → 0 valid, 0 invalid
- All invalid: `"ABC, XYZ"` → 0 valid, 2 invalid

---

#### Step 1.4: Implement PackageListStorage

**File**: `app/src/main/java/com/geocomply/test/gpapchecker/utils/PackageListStorage.kt`

```kotlin
package com.geocomply.test.gpapchecker.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class PackageListStorage(context: Context) {
    companion object {
        private const val PREFS_NAME = "package_list_prefs"
        private const val KEY_CUSTOM_PACKAGES = "custom_packages"
        private const val KEY_IS_CUSTOM_LIST = "is_custom_list"
        private const val KEY_LAST_IMPORT_TIME = "last_import_time"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasCustomList(): Boolean {
        return prefs.getBoolean(KEY_IS_CUSTOM_LIST, false)
    }

    fun getPackageList(): List<String> {
        if (!hasCustomList()) {
            return emptyList()  // Caller should use hardcoded list
        }
        val json = prefs.getString(KEY_CUSTOM_PACKAGES, "[]") ?: "[]"
        return try {
            Json.decodeFromString<List<String>>(json)
        } catch (e: Exception) {
            emptyList()  // Fallback on parse error
        }
    }

    fun savePackageList(packages: List<String>) {
        val json = Json.encodeToString(packages)
        prefs.edit().apply {
            putString(KEY_CUSTOM_PACKAGES, json)
            putBoolean(KEY_IS_CUSTOM_LIST, true)
            putLong(KEY_LAST_IMPORT_TIME, System.currentTimeMillis())
            apply()
        }
    }

    fun clearCustomList() {
        prefs.edit().apply {
            remove(KEY_CUSTOM_PACKAGES)
            putBoolean(KEY_IS_CUSTOM_LIST, false)
            apply()
        }
    }
}
```

**Dependency**: Add kotlinx-serialization to `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

plugins {
    kotlin("plugin.serialization") version "1.9.0"
}
```

**Test**: Create `PackageListStorageTest.kt` (use AndroidX Test for Context):
- Test first launch: `hasCustomList()` returns false
- Test save → read round-trip
- Test JSON serialization correctness
- Test clear functionality
- Test fallback on corrupted JSON

---

#### Step 1.5: Implement PackageListRepository

**File**: `app/src/main/java/com/geocomply/test/gpapchecker/repository/PackageListRepository.kt`

```kotlin
package com.geocomply.test.gpapchecker.repository

import com.geocomply.test.gpapchecker.data.ImportMode
import com.geocomply.test.gpapchecker.utils.PackageListStorage

class PackageListRepository(
    private val storage: PackageListStorage
) {
    suspend fun savePackages(packages: List<String>, mode: ImportMode) {
        val finalList = when (mode) {
            ImportMode.REPLACE -> packages
            ImportMode.APPEND -> {
                val existing = storage.getPackageList()
                (existing + packages).distinct()  // Merge & deduplicate
            }
        }
        storage.savePackageList(finalList)
    }

    fun getPackageList(): List<String> {
        return storage.getPackageList()
    }

    fun hasCustomList(): Boolean {
        return storage.hasCustomList()
    }
}
```

**Test**: Create `PackageListRepositoryTest.kt`:
- Test REPLACE mode: Verify old list cleared
- Test APPEND mode: Verify merge + deduplication
- Test delegation to storage layer

---

#### Step 1.6: Modify AppRepository to Use PackageListStorage

**File**: `app/src/main/java/com/geocomply/test/gpapchecker/repository/AppRepository.kt` (MODIFY)

Add constructor parameter and update `getPackagesToCheck()`:

```kotlin
class AppRepository(
    private val appChecker: AppChecker,
    private val csvExporter: CsvExporter,
    private val packageListStorage: PackageListStorage  // NEW
) {
    // Hardcoded list (lines 12-214) - keep as fallback
    private val packageNamesToCheck = listOf(
        "com.example.app1",
        // ... existing 213 packages
    )

    suspend fun getPackagesToCheck(): List<String> {
        return if (packageListStorage.hasCustomList()) {
            packageListStorage.getPackageList()
        } else {
            packageNamesToCheck  // First launch: use hardcoded list
        }
    }

    // ... rest of AppRepository unchanged
}
```

**Test**: Update existing `AppRepositoryTest.kt`:
- Mock `PackageListStorage`
- Test first launch: Returns hardcoded list
- Test after import: Returns custom list

---

### Phase 2: ViewModel Layer

#### Step 2.1: Implement ImportViewModel

**File**: `app/src/main/java/com/geocomply/test/gpapchecker/viewmodel/ImportViewModel.kt`

```kotlin
package com.geocomply.test.gpapchecker.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geocomply.test.gpapchecker.data.ImportMode
import com.geocomply.test.gpapchecker.data.ImportState
import com.geocomply.test.gpapchecker.repository.PackageListRepository
import com.geocomply.test.gpapchecker.utils.PackageListParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImportViewModel(
    private val repository: PackageListRepository,
    private val parser: PackageListParser,
    private val contentResolver: ContentResolver
) : ViewModel() {

    private val _importState = MutableLiveData<ImportState>(ImportState.Initial)
    val importState: LiveData<ImportState> = _importState

    fun parseTextInput(input: String) {
        viewModelScope.launch {
            _importState.value = ImportState.ParsingInput
            val result = withContext(Dispatchers.Default) {
                parser.parse(input)
            }
            _importState.value = ImportState.ValidationComplete(result)
        }
    }

    fun parseFileContent(uri: Uri) {
        viewModelScope.launch {
            _importState.value = ImportState.ReadingFile
            try {
                val content = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                        it.readText(Charsets.UTF_8)
                    } ?: throw IllegalStateException("Failed to read file")
                }

                _importState.value = ImportState.ParsingInput
                val result = withContext(Dispatchers.Default) {
                    parser.parse(content)
                }
                _importState.value = ImportState.ValidationComplete(result)
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Failed to read file: ${e.message}")
            }
        }
    }

    fun performImport(packages: List<String>, mode: ImportMode) {
        viewModelScope.launch {
            try {
                repository.savePackages(packages, mode)
                _importState.value = ImportState.ImportSuccess(
                    packageCount = packages.size,
                    mode = mode
                )
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Failed to save: ${e.message}")
            }
        }
    }

    fun resetState() {
        _importState.value = ImportState.Initial
    }
}
```

**Test**: Create `ImportViewModelTest.kt`:
- Mock repository, parser, contentResolver
- Test `parseTextInput()` state transitions
- Test `parseFileContent()` success & error cases
- Test `performImport()` for REPLACE and APPEND modes
- Use `InstantTaskExecutorRule` for LiveData testing

---

#### Step 2.2: Create ImportViewModelFactory

**File**: `app/src/main/java/com/geocomply/test/gpapchecker/viewmodel/ImportViewModelFactory.kt`

```kotlin
package com.geocomply.test.gpapchecker.viewmodel

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geocomply.test.gpapchecker.repository.PackageListRepository
import com.geocomply.test.gpapchecker.utils.PackageListParser

class ImportViewModelFactory(
    private val repository: PackageListRepository,
    private val parser: PackageListParser,
    private val contentResolver: ContentResolver
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImportViewModel(repository, parser, contentResolver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

---

### Phase 3: UI Layer

#### Step 3.1: Create Layout XML for Import Screen

**File**: `app/src/main/res/layout/activity_import.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <!-- Title -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/import_title"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp"/>

    <!-- Text Input Section -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/import_text_label"
        android:textSize="16sp"
        android:layout_marginBottom="8dp"/>

    <EditText
        android:id="@+id/textInputField"
        android:layout_width="match_parent"
        android:layout_height="120dp"
        android:hint="@string/import_text_hint"
        android:gravity="top|start"
        android:inputType="textMultiLine"
        android:scrollbars="vertical"
        android:layout_marginBottom="16dp"/>

    <!-- OR Divider -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/import_or"
        android:layout_gravity="center_horizontal"
        android:layout_marginBottom="16dp"/>

    <!-- File Import Button -->
    <Button
        android:id="@+id/importFileButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/import_file_button"
        android:layout_gravity="center_horizontal"
        android:layout_marginBottom="24dp"/>

    <!-- Import Mode Selection -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/import_mode_label"
        android:textSize="16sp"
        android:layout_marginBottom="8dp"/>

    <RadioGroup
        android:id="@+id/importModeGroup"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp">

        <RadioButton
            android:id="@+id/radioReplace"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/import_mode_replace"
            android:checked="true"/>

        <RadioButton
            android:id="@+id/radioAppend"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/import_mode_append"/>
    </RadioGroup>

    <!-- Validation Summary -->
    <TextView
        android:id="@+id/validationSummary"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="14sp"
        android:layout_marginBottom="16dp"
        android:visibility="gone"/>

    <!-- Progress Indicator -->
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center_horizontal"
        android:layout_marginBottom="16dp"
        android:visibility="gone"/>

    <!-- Error Message -->
    <TextView
        android:id="@+id/errorMessage"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@android:color/holo_red_dark"
        android:layout_marginBottom="16dp"
        android:visibility="gone"/>

    <!-- Spacer -->
    <View
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_weight="1"/>

    <!-- Import Button -->
    <Button
        android:id="@+id/importButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/import_button"
        android:enabled="false"/>
</LinearLayout>
```

**Strings**: Add to `app/src/main/res/values/strings.xml`:
```xml
<string name="import_title">Import Package List</string>
<string name="import_text_label">Enter package names (comma or line separated):</string>
<string name="import_text_hint">com.example.app1, com.example.app2...</string>
<string name="import_or">OR</string>
<string name="import_file_button">Import from .txt File</string>
<string name="import_mode_label">Import mode:</string>
<string name="import_mode_replace">Replace existing list (recommended)</string>
<string name="import_mode_append">Append to existing list</string>
<string name="import_button">Import</string>
<string name="validation_summary">%1$d valid, %2$d invalid packages</string>
```

---

#### Step 3.2: Implement ImportActivity

**File**: `app/src/main/java/com/geocomply/test/gpapchecker/ImportActivity.kt`

```kotlin
package com.geocomply.test.gpapchecker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.geocomply.test.gpapchecker.data.ImportMode
import com.geocomply.test.gpapchecker.data.ImportState
import com.geocomply.test.gpapchecker.repository.PackageListRepository
import com.geocomply.test.gpapchecker.utils.PackageListParser
import com.geocomply.test.gpapchecker.utils.PackageListStorage
import com.geocomply.test.gpapchecker.viewmodel.ImportViewModel
import com.geocomply.test.gpapchecker.viewmodel.ImportViewModelFactory

class ImportActivity : AppCompatActivity() {

    private val viewModel: ImportViewModel by viewModels {
        val storage = PackageListStorage(applicationContext)
        val repository = PackageListRepository(storage)
        val parser = PackageListParser()
        ImportViewModelFactory(repository, parser, contentResolver)
    }

    private lateinit var textInputField: EditText
    private lateinit var importFileButton: Button
    private lateinit var importModeGroup: RadioGroup
    private lateinit var validationSummary: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorMessage: TextView
    private lateinit var importButton: Button

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.parseFileContent(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)

        bindViews()
        setupListeners()
        observeViewModel()
    }

    private fun bindViews() {
        textInputField = findViewById(R.id.textInputField)
        importFileButton = findViewById(R.id.importFileButton)
        importModeGroup = findViewById(R.id.importModeGroup)
        validationSummary = findViewById(R.id.validationSummary)
        progressBar = findViewById(R.id.progressBar)
        errorMessage = findViewById(R.id.errorMessage)
        importButton = findViewById(R.id.importButton)
    }

    private fun setupListeners() {
        textInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.parseTextInput(s.toString())
            }
        })

        importFileButton.setOnClickListener {
            filePickerLauncher.launch(arrayOf("text/plain"))
        }

        importButton.setOnClickListener {
            val state = viewModel.importState.value
            if (state is ImportState.ValidationComplete) {
                val mode = if (findViewById<RadioButton>(R.id.radioReplace).isChecked) {
                    ImportMode.REPLACE
                } else {
                    ImportMode.APPEND
                }
                viewModel.performImport(state.result.validPackages, mode)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.importState.observe(this) { state ->
            when (state) {
                is ImportState.Initial -> showInitialState()
                is ImportState.ParsingInput -> showParsingState()
                is ImportState.ReadingFile -> showReadingFileState()
                is ImportState.ValidationComplete -> showValidationResults(state)
                is ImportState.ImportSuccess -> showImportSuccess(state)
                is ImportState.Error -> showError(state.message)
            }
        }
    }

    private fun showInitialState() {
        progressBar.visibility = View.GONE
        validationSummary.visibility = View.GONE
        errorMessage.visibility = View.GONE
        importButton.isEnabled = false
    }

    private fun showParsingState() {
        progressBar.visibility = View.VISIBLE
        validationSummary.visibility = View.GONE
        errorMessage.visibility = View.GONE
    }

    private fun showReadingFileState() {
        progressBar.visibility = View.VISIBLE
        validationSummary.visibility = View.GONE
        errorMessage.visibility = View.GONE
    }

    private fun showValidationResults(state: ImportState.ValidationComplete) {
        progressBar.visibility = View.GONE
        errorMessage.visibility = View.GONE

        val result = state.result
        validationSummary.text = getString(
            R.string.validation_summary,
            result.validCount,
            result.invalidCount
        )
        validationSummary.visibility = View.VISIBLE

        importButton.isEnabled = result.hasValidPackages
    }

    private fun showImportSuccess(state: ImportState.ImportSuccess) {
        Toast.makeText(
            this,
            "Imported ${state.packageCount} packages",
            Toast.LENGTH_SHORT
        ).show()

        // Navigate back to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        errorMessage.text = message
        errorMessage.visibility = View.VISIBLE
        importButton.isEnabled = false
    }
}
```

**AndroidManifest.xml**: Register ImportActivity:
```xml
<activity
    android:name=".ImportActivity"
    android:label="@string/import_title"
    android:exported="false" />
```

---

#### Step 3.3: Modify MainActivity to Add Settings Button

**File**: `app/src/main/res/layout/activity_main.xml` (MODIFY)

Add settings button to top bar (example using Toolbar):
```xml
<androidx.appcompat.widget.Toolbar
    android:id="@+id/toolbar"
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize"
    android:background="?attr/colorPrimary"
    app:title="@string/app_name"/>
```

**File**: `app/src/main/java/com/geocomply/test/gpapchecker/MainActivity.kt` (MODIFY)

Add navigation to ImportActivity:
```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Existing code...

        // Add settings button to toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, ImportActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
```

**File**: `app/src/main/res/menu/main_menu.xml` (NEW):
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <item
        android:id="@+id/action_settings"
        android:icon="@drawable/ic_settings"
        android:title="@string/action_settings"
        app:showAsAction="ifRoom" />
</menu>
```

**File**: `app/src/main/res/drawable/ic_settings.xml` (NEW):
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z"/>
</vector>
```

**Strings**:
```xml
<string name="action_settings">Import List</string>
```

---

### Phase 4: Testing

#### Step 4.1: Unit Tests

Run all unit tests:
```bash
./gradlew testDebugUnitTest
```

Verify coverage for:
- `ValidationResultTest`
- `ImportStateTest`
- `PackageListParserTest`
- `PackageListStorageTest`
- `PackageListRepositoryTest`
- `ImportViewModelTest`

Target: 80%+ line coverage for new classes.

---

#### Step 4.2: Instrumented Tests

**File**: `app/src/androidTest/java/com/geocomply/test/gpapchecker/ImportActivityTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class ImportActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(ImportActivity::class.java)

    @Test
    fun testTextInputFlow() {
        onView(withId(R.id.textInputField))
            .perform(typeText("com.example.app1, com.example.app2"))

        onView(withId(R.id.validationSummary))
            .check(matches(withText(containsString("2 valid"))))

        onView(withId(R.id.importButton))
            .check(matches(isEnabled()))
    }

    @Test
    fun testInvalidInputDisablesButton() {
        onView(withId(R.id.textInputField))
            .perform(typeText("INVALID"))

        onView(withId(R.id.importButton))
            .check(matches(not(isEnabled())))
    }
}
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

---

### Phase 5: Integration & Manual Testing

#### Step 5.1: Build & Run

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

#### Step 5.2: Manual Test Cases

1. **First Launch**:
   - Launch app → Verify 213 hardcoded packages shown in main screen
   - Tap settings button → Navigate to import screen

2. **Text Input Import**:
   - Enter: `com.test.app1, com.test.app2`
   - Verify: "2 valid, 0 invalid" summary shown
   - Tap Import → Navigate back to main screen
   - Verify: Only 2 packages shown (hardcoded list replaced)

3. **File Import**:
   - Create .txt file with 10 package names (one per line)
   - Tap "Import from File" → Select file
   - Verify: "10 valid, 0 invalid" summary
   - Tap Import → Navigate back
   - Verify: 10 packages shown

4. **Append Mode**:
   - Import com.app1 (replace mode)
   - Return to import screen
   - Select "Append to existing list" radio button
   - Import com.app2
   - Return to main screen → Verify both packages shown

5. **Validation**:
   - Enter invalid packages: `INVALID, com.valid.app`
   - Verify: "1 valid, 1 invalid" summary
   - Tap Import → Only com.valid.app imported

6. **Persistence**:
   - Import custom list
   - Close app (force-stop)
   - Relaunch app → Verify custom list persists

---

## Troubleshooting

### Issue: Import button always disabled
- Check: TextWatcher triggering `parseTextInput()`
- Check: `hasValidPackages` logic in ValidationResult
- Check: LiveData observation in Activity

### Issue: File picker not opening
- Check: AndroidManifest permissions
- Check: ActivityResultLauncher registration
- Check: MIME type filter: `text/plain`

### Issue: Packages not persisting
- Check: SharedPreferences write (use `apply()` not `commit()`)
- Check: `is_custom_list` flag set to true
- Check: JSON serialization (add kotlinx-serialization dependency)

### Issue: Hardcoded list still showing after import
- Check: `AppRepository.getPackagesToCheck()` reads from storage
- Check: `hasCustomList()` returns true after import
- Check: MainActivity refresh logic after navigation back

---

## Performance Validation

Run these benchmarks to verify performance requirements:

```kotlin
@Test
fun benchmark500PackagesTextImport() {
    val packages = (1..500).map { "com.example.app$it" }.joinToString(",")
    val start = System.currentTimeMillis()
    val result = parser.parse(packages)
    val duration = System.currentTimeMillis() - start
    assertTrue("Expected < 5000ms, got ${duration}ms", duration < 5000)
}
```

Expected results:
- 500 packages (text): < 5 seconds ✅
- 1000 packages (file): < 10 seconds ✅

---

## Completion Checklist

Before marking feature complete, verify:

- [ ] All unit tests pass (`./gradlew test`)
- [ ] All instrumented tests pass (`./gradlew connectedAndroidTest`)
- [ ] Code coverage ≥ 80% for new classes (`./gradlew jacocoTestReport`)
- [ ] Lint passes (`./gradlew lint`)
- [ ] Manual testing completed (all 6 scenarios above)
- [ ] Performance benchmarks pass (SC-002, SC-003)
- [ ] Settings button visible on MainActivity toolbar
- [ ] First launch shows hardcoded list
- [ ] Import replaces/appends correctly based on mode
- [ ] Validation feedback shows inline (<2 seconds)
- [ ] Custom list persists across app restarts

---

## Next Steps

After implementation:
1. Run `/speckit.tasks` to generate task breakdown for tracking
2. Review implementation against spec.md acceptance scenarios
3. Submit PR with tests + coverage report
4. Update CLAUDE.md with new components (ImportActivity, PackageListStorage, etc.)
