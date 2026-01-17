# Implementation Plan: Custom App List Import

**Branch**: `001-app-list-import` | **Date**: 2026-01-17 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-app-list-import/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Implement a custom app list import feature that allows users to replace the hardcoded 213-package list with their own package names via text input or .txt file import. The feature includes a new ImportActivity with UI for text input and file selection, a PackageListParser utility for parsing and validation, and SharedPreferences-based persistence. The imported list completely replaces the hardcoded list while maintaining the existing MVVM architecture patterns.

## Technical Context

**Language/Version**: Kotlin (matching existing codebase: Kotlin 1.9.0+)
**Primary Dependencies**: Android SDK 29-35, AndroidX (Activity, LiveData, ViewModel, Material Components)
**Storage**: SharedPreferences for package list persistence
**Testing**: JUnit 4, Mockito, AndroidX Test for instrumented tests
**Target Platform**: Android 10+ (SDK 29-35)
**Project Type**: Android mobile (single module)
**Performance Goals**: Import 500 packages (text) < 5 seconds, 1000 packages (file) < 10 seconds
**Constraints**: File size limit 1MB, UTF-8 encoding only, package name validation regex
**Scale/Scope**: Single feature adding 1 new Activity, 1 ViewModel, 2 utilities, XML layouts

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Note**: No constitution.md found - using default Android MVVM architectural principles:

✅ **MVVM Architecture**: Feature follows existing MVVM pattern (ImportViewModel, ImportActivity, Repository pattern)
✅ **Testing Strategy**: Unit tests for ViewModel/Repository, instrumented tests for UI/file I/O
✅ **Separation of Concerns**: Clear boundaries between UI (Activity), business logic (ViewModel/Repository), utilities (Parser)
✅ **LiveData for State**: Following existing pattern (UiState sealed class for import state management)
✅ **Code Style**: Kotlin official style, consistent with existing codebase conventions

**Gate Status**: ✅ PASS - No violations detected

## Project Structure

### Documentation (this feature)

```text
specs/001-app-list-import/
├── plan.md              # This file (/speckit.plan command output)
├── spec.md              # Feature specification
├── research.md          # Phase 0 output (technical decisions)
├── data-model.md        # Phase 1 output (entity definitions)
├── quickstart.md        # Phase 1 output (implementation guide)
└── contracts/           # Phase 1 output (not applicable for mobile)
```

### Source Code (repository root)

```text
app/src/main/java/com/geocomply/test/gpapchecker/
├── ImportActivity.kt                        # NEW: Import screen UI controller
├── viewmodel/
│   ├── ImportViewModel.kt                   # NEW: Import screen state & logic
│   └── ImportViewModelFactory.kt            # NEW: DI factory for ImportViewModel
├── repository/
│   ├── AppRepository.kt                     # MODIFIED: Load packages from storage
│   └── PackageListRepository.kt             # NEW: Manage package list persistence
├── utils/
│   ├── PackageListParser.kt                 # NEW: Parse & validate package names
│   └── PackageListStorage.kt                # NEW: SharedPreferences abstraction
├── data/
│   ├── ImportState.kt                       # NEW: Sealed class for import UI state
│   └── ValidationResult.kt                  # NEW: Validation result with counts
└── MainActivity.kt                          # MODIFIED: Add settings button & navigation

app/src/main/res/
├── layout/
│   ├── activity_import.xml                  # NEW: Import screen layout
│   └── activity_main.xml                    # MODIFIED: Add settings button
├── values/
│   └── strings.xml                          # MODIFIED: Add import screen strings
└── drawable/
    └── ic_settings.xml                      # NEW: Settings icon (24dp)

app/src/test/java/com/geocomply/test/gpapchecker/
├── viewmodel/
│   └── ImportViewModelTest.kt               # NEW: Unit tests for ImportViewModel
├── repository/
│   └── PackageListRepositoryTest.kt         # NEW: Unit tests for repository
└── utils/
    ├── PackageListParserTest.kt             # NEW: Unit tests for parser
    └── PackageListStorageTest.kt            # NEW: Unit tests for storage

app/src/androidTest/java/com/geocomply/test/gpapchecker/
└── ImportActivityTest.kt                    # NEW: Instrumented UI tests
```

**Structure Decision**: Following existing Android single-module MVVM pattern. All source code resides in `app/src/main/java/com/geocomply/test/gpapchecker/` organized by architectural layer (viewmodel/, repository/, utils/, data/). This matches the established codebase structure documented in CLAUDE.md.

## Complexity Tracking

No constitution violations detected. Feature aligns with existing MVVM architecture and testing patterns.
