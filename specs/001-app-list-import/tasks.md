# Tasks: Custom App List Import

**Feature Branch**: `001-app-list-import`
**Input**: Design documents from `/specs/001-app-list-import/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

**Tests**: This feature does NOT explicitly request tests in the specification, therefore test tasks are NOT included. Unit tests and instrumented tests can be added later if desired.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Android single-module**: `app/src/main/java/com/geocomply/test/gpapchecker/`
- **Resources**: `app/src/main/res/`
- **Tests**: `app/src/test/java/` and `app/src/androidTest/java/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and foundational utilities

- [X] T001 Add kotlinx-serialization dependency to app/build.gradle.kts (version 1.6.0)
- [X] T002 Add kotlinx-serialization plugin to app/build.gradle.kts plugins block

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core data models and utilities that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T003 [P] Create ValidationResult data class in app/src/main/java/com/geocomply/test/gpapchecker/data/ValidationResult.kt
- [X] T004 [P] Create ImportState sealed class in app/src/main/java/com/geocomply/test/gpapchecker/data/ImportState.kt
- [X] T005 [P] Create ImportMode enum in app/src/main/java/com/geocomply/test/gpapchecker/data/ImportState.kt
- [X] T006 [P] Implement PackageListParser utility with validation regex, whitespace trimming, and deduplication in app/src/main/java/com/geocomply/test/gpapchecker/utils/PackageListParser.kt
- [X] T007 [P] Implement PackageListStorage utility in app/src/main/java/com/geocomply/test/gpapchecker/utils/PackageListStorage.kt
- [X] T008 Implement PackageListRepository in app/src/main/java/com/geocomply/test/gpapchecker/repository/PackageListRepository.kt
- [X] T009 Modify AppRepository to integrate PackageListStorage in app/src/main/java/com/geocomply/test/gpapchecker/repository/AppRepository.kt

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Import App List via Text Input (Priority: P1) 🎯 MVP

**Goal**: Allow users to quickly import package names via comma-separated text input, with immediate validation feedback and persistence across app launches.

**Independent Test**: Enter "com.example.app1, com.example.app2" in the text input field, verify validation summary shows "2 valid, 0 invalid", tap import, verify GPAP Checker screen displays these two packages, relaunch app and verify packages persist.

### Implementation for User Story 1

- [X] T010 [P] [US1] Create activity_import.xml layout in app/src/main/res/layout/activity_import.xml
- [X] T011 [P] [US1] Add import screen strings to app/src/main/res/values/strings.xml
- [X] T012 [P] [US1] Create settings icon drawable in app/src/main/res/drawable/ic_settings.xml
- [X] T013 [P] [US1] Create ImportViewModel in app/src/main/java/com/geocomply/test/gpapchecker/viewmodel/ImportViewModel.kt
- [X] T014 [P] [US1] Create ImportViewModelFactory in app/src/main/java/com/geocomply/test/gpapchecker/viewmodel/ImportViewModelFactory.kt
- [X] T015 [US1] Implement ImportActivity with text input handling in app/src/main/java/com/geocomply/test/gpapchecker/ImportActivity.kt
- [X] T016 [US1] Add ImportActivity to AndroidManifest.xml
- [X] T017 [US1] Create main_menu.xml with settings action in app/src/main/res/menu/main_menu.xml
- [X] T018 [US1] Modify MainActivity (GPAP Checker screen) to add toolbar with settings button in app/src/main/java/com/geocomply/test/gpapchecker/MainActivity.kt
- [X] T019 [US1] Modify activity_main.xml (GPAP Checker screen) to include Toolbar in app/src/main/res/layout/activity_main.xml
- [X] T020 [US1] Add navigation from MainActivity (GPAP Checker screen) to ImportActivity via settings button in app/src/main/java/com/geocomply/test/gpapchecker/MainActivity.kt
- [X] T021 [US1] Implement TextWatcher for live validation in ImportActivity
- [X] T022 [US1] Wire up import button to perform import and navigate back to MainActivity (GPAP Checker screen)
- [X] T023 [US1] Implement validation summary display (valid/invalid counts) in ImportActivity

**Checkpoint**: User Story 1 complete - users can import packages via text input, validation works, packages persist

---

## Phase 4: User Story 2 - Import App List from TXT File (Priority: P2)

**Goal**: Enable users to import larger lists from .txt files (comma-separated or line-separated), expanding utility beyond manual text entry.

**Independent Test**: Create a .txt file with package names (e.g., "com.app1\ncom.app2\ncom.app3"), select it via file picker, verify all packages appear in validation summary, tap import, verify all packages appear in GPAP Checker screen.

### Implementation for User Story 2

- [ ] T024 [US2] Add file picker button click handler in ImportActivity
- [ ] T025 [US2] Register ActivityResultLauncher for file selection in ImportActivity
- [ ] T026 [US2] Implement parseFileContent() method in ImportViewModel using Storage Access Framework
- [ ] T027 [US2] Add file read logic with UTF-8 encoding in ImportViewModel
- [ ] T028 [US2] Add error handling for file read failures in ImportViewModel
- [ ] T029 [US2] Wire file selection result to ViewModel in ImportActivity
- [ ] T030 [US2] Update UI state handling for ReadingFile state in ImportActivity

**Checkpoint**: User Stories 1 AND 2 both work independently - text input and file import both functional

---

## Phase 5: User Story 3 - Replace Existing App List with New Import (Priority: P2)

**Goal**: Provide flexibility to replace or append to existing app list based on user preference.

**Independent Test**: Import an initial list (e.g., "com.app1, com.app2"), navigate back to import screen, import a new list (e.g., "com.app3, com.app4") with "Replace" option selected, verify GPAP Checker screen shows only com.app3 and com.app4. Then import "com.app5" with "Append" option and verify all 3 packages appear.

### Implementation for User Story 3

- [ ] T031 [US3] Add RadioGroup for import mode selection (Replace/Append) to activity_import.xml
- [ ] T032 [US3] Add import mode strings to strings.xml
- [ ] T033 [US3] Implement import mode selection reading in ImportActivity
- [ ] T034 [US3] Pass ImportMode to performImport() in ImportActivity
- [ ] T035 [US3] Implement REPLACE mode logic in PackageListRepository.savePackages()
- [ ] T036 [US3] Implement APPEND mode logic with deduplication in PackageListRepository.savePackages()
- [ ] T037 [US3] Set default radio button to "Replace" in activity_import.xml

**Checkpoint**: All user stories independently functional - text input, file import, and replace/append modes all work

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories and edge case handling

- [ ] T038 [P] Add empty input validation (disable import button when no valid packages) in ImportActivity
- [ ] T039 [P] Add error message display for file read failures in ImportActivity
- [ ] T040 [P] Add success toast message showing package count after import in ImportActivity
- [ ] T041 [P] Add progress indicators for ParsingInput and ReadingFile states in ImportActivity
- [ ] T042 Verify whitespace trimming works correctly in PackageListParser.parse()
- [ ] T043 Verify duplicate removal (distinct()) works correctly in PackageListParser.parse()
- [ ] T044 Verify package name validation regex rejects invalid formats in PackageListParser
- [ ] T045 Update MainActivity (GPAP Checker screen) to refresh app list when returning from ImportActivity
- [ ] T046 [P] Add file size validation (max 1MB) in ImportViewModel.parseFileContent()
- [ ] T047 [P] Add logging for import operations in ImportViewModel
- [ ] T048 Verify first launch detection (hardcoded list shown) in AppRepository
- [ ] T049 Run quickstart.md manual validation scenarios
- [ ] T050 Build and test on physical device or emulator

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-5)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P2)
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Builds on US1 UI but independently testable
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - Extends US1/US2 but independently testable

### Within Each User Story

- Layouts/strings before Activity implementation
- ViewModel before Activity (for dependency injection)
- ViewModelFactory alongside ViewModel
- Menu/navigation changes can be done in parallel with core feature
- UI state handling after ViewModel state definitions

### Parallel Opportunities

- All Setup tasks (T001-T002) can run in parallel
- All Foundational tasks marked [P] (T003-T007) can run in parallel within Phase 2
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- Within each user story, all tasks marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: Foundational Phase

```bash
# Launch all foundational data models together:
Task: "Create ValidationResult data class in app/src/main/java/com/geocomply/test/gpapchecker/data/ValidationResult.kt"
Task: "Create ImportState sealed class in app/src/main/java/com/geocomply/test/gpapchecker/data/ImportState.kt"
Task: "Create ImportMode enum in app/src/main/java/com/geocomply/test/gpapchecker/data/ImportState.kt"
Task: "Implement PackageListParser utility in app/src/main/java/com/geocomply/test/gpapchecker/utils/PackageListParser.kt"
Task: "Implement PackageListStorage utility in app/src/main/java/com/geocomply/test/gpapchecker/utils/PackageListStorage.kt"
```

## Parallel Example: User Story 1

```bash
# Launch all UI resources for User Story 1 together:
Task: "Create activity_import.xml layout in app/src/main/res/layout/activity_import.xml"
Task: "Add import screen strings to app/src/main/res/values/strings.xml"
Task: "Create settings icon drawable in app/src/main/res/drawable/ic_settings.xml"
Task: "Create ImportViewModel in app/src/main/java/com/geocomply/test/gpapchecker/viewmodel/ImportViewModel.kt"
Task: "Create ImportViewModelFactory in app/src/main/java/com/geocomply/test/gpapchecker/viewmodel/ImportViewModelFactory.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T002)
2. Complete Phase 2: Foundational (T003-T009) - CRITICAL, blocks all stories
3. Complete Phase 3: User Story 1 (T010-T023)
4. **STOP and VALIDATE**: Test text input import independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 (Text Input) → Test independently → MVP Ready!
3. Add User Story 2 (File Import) → Test independently → Enhanced Version
4. Add User Story 3 (Replace/Append) → Test independently → Full Feature
5. Complete Polish Phase → Production Ready
6. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup (Phase 1) together
2. Team completes Foundational (Phase 2) together - must be done before stories
3. Once Foundational is done:
   - Developer A: User Story 1 (Text Input)
   - Developer B: User Story 2 (File Import)
   - Developer C: User Story 3 (Replace/Append)
4. Stories complete and integrate independently
5. Team completes Polish (Phase 6) together

---

## Notes

- [P] tasks = different files, no dependencies, can run in parallel
- [Story] label maps task to specific user story for traceability (US1, US2, US3)
- Each user story should be independently completable and testable
- Tests are NOT included as they were not explicitly requested in spec
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Performance targets: 500 packages (text) < 5s, 1000 packages (file) < 10s
- File size limit: 1MB max
- UTF-8 encoding only
- Android package naming validation: `^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$`
