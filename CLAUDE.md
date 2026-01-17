# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GPAP-checker is an Android application that checks for the presence of specific gambling/gaming apps on a device and detects whether they contain a specific license validation activity (`com.pairip.licensecheck.LicenseActivity`). The app can export results to CSV format.

**Tech Stack:**
- Language: Kotlin
- Build System: Gradle 8.9 with Kotlin DSL
- Architecture: MVVM (Model-View-ViewModel)
- UI: Traditional Android Views (XML layouts) + RecyclerView
- Min SDK: 29 (Android 10), Target SDK: 35
- Testing: JUnit 4, Mockito, JaCoCo for coverage
- Java Version: 11

## Development Commands

### Building
```bash
./gradlew build                    # Build the entire project
./gradlew assembleDebug           # Build debug APK
./gradlew assembleRelease         # Build release APK (unsigned)
```

### Testing
```bash
./gradlew test                     # Run all unit tests
./gradlew testDebugUnitTest       # Run debug unit tests specifically
./gradlew connectedAndroidTest    # Run instrumented tests (requires device/emulator)
./gradlew jacocoTestReport        # Generate code coverage report (HTML + XML)
```

**Coverage Report Location:** `app/build/reports/jacoco/jacocoTestReport/html/index.html`

### Running Individual Tests
```bash
./gradlew test --tests "com.geocomply.test.gpapchecker.viewmodel.BasicMainViewModelTest"
./gradlew test --tests "*.HashUtilsTest"
```

### Linting & Code Quality
```bash
./gradlew lint                     # Run Android lint
./gradlew lintDebug               # Lint debug variant only
```

**Lint Reports:** `app/build/reports/lint-results-debug.html`

### Cleaning
```bash
./gradlew clean                    # Clean build artifacts
```

## Architecture & Code Organization

### MVVM Pattern
The app follows MVVM architecture with clear separation of concerns:

- **View Layer** (`MainActivity`, `AppsActivity`): Activities handle UI rendering and user interaction
- **ViewModel Layer** (`MainViewModel`): Manages UI state via LiveData, coordinates with repository
- **Repository Layer** (`AppRepository`): Mediates between ViewModel and utilities, contains business logic for package checking
- **Data Layer** (`AppInfo`, `UiState`): Data models and sealed class for UI state management
- **Utils** (`AppChecker`, `CsvExporter`, `HashUtils`): Platform-specific utilities for app checking and data export

### Key Components

**AppRepository** (`app/src/main/java/com/geocomply/test/gpapchecker/repository/AppRepository.kt`)
- Contains hardcoded list of ~213 package names to check (gambling/gaming apps)
- Delegates actual checking to `AppChecker`, CSV export to `CsvExporter`

**AppChecker** (`app/src/main/java/com/geocomply/test/gpapchecker/utils/AppChecker.kt`)
- Uses Android `PackageManager` to query installed apps
- Checks for presence of `com.pairip.licensecheck.LicenseActivity` in each package
- Returns `AppInfo` objects with install status and license activity detection

**MainViewModel** (`app/src/main/java/com/geocomply/test/gpapchecker/viewmodel/MainViewModel.kt`)
- Exposes `uiState: LiveData<UiState<List<AppInfo>>>` for UI state (Initial, Loading, Success, Error)
- Exposes `canExport: LiveData<Boolean>` to enable/disable export button
- Uses coroutines (`viewModelScope`) for async package checking

**UiState** (`app/src/main/java/com/geocomply/test/gpapchecker/data/UiState.kt`)
- Sealed class with states: `Initial`, `Loading`, `Success<T>`, `Error`
- Type-safe state management pattern

### Testing Strategy

Tests are organized to mirror the main source structure:
- Unit tests in `app/src/test/java/` use JUnit 4 + Mockito
- Instrumented tests in `app/src/androidTest/java/` use AndroidJUnit
- `TestConfig.kt` provides common test utilities

**JaCoCo Configuration:**
- Configured in `app/build.gradle.kts` lines 43-81
- Excludes Android framework classes, R files, BuildConfig, test classes
- Auto-runs after `testDebugUnitTest` task
- Coverage threshold not enforced (no min coverage requirement)

### Dependency Injection

The app uses manual DI via ViewModelFactory pattern:
- `MainViewModelFactory` creates `MainViewModel` with required dependencies
- Dependencies (Context-based utilities) are instantiated in the factory

## Important Notes

### Package List Management
The list of packages to check is hardcoded in `AppRepository.packageNamesToCheck` (lines 12-214). When adding/removing packages, update this list directly.

### Android Permissions
The app uses `PackageManager` which requires `QUERY_ALL_PACKAGES` permission for Android 11+ to detect all installed apps. Check `AndroidManifest.xml` for permission declarations.

### CSV Export
`CsvExporter` uses Android's file system APIs and likely shares files via email intent. Changes to export functionality should maintain compatibility with external storage scoped access (Android 10+).

## Code Style
- Kotlin official code style (configured in `gradle.properties`)
- ViewModel uses LiveData (not StateFlow/SharedFlow) for state management
- Repository pattern with suspend functions for async operations
- Traditional Android Views (not Jetpack Compose, despite Compose dependencies)
