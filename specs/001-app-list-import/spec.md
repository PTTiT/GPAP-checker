# Feature Specification: Custom App List Import

**Feature Branch**: `001-app-list-import`
**Created**: 2026-01-17
**Status**: Draft
**Input**: User description: "Add a feature for user to import the list of the apps to check. The import feature supports multiple ways:
- A string of package names separated by commas
- File: txt

User can click on the setting button in the top bar of main screen to navigate to the import screen.
After user imports correctly, it backs to the main screen. The main screen is refreshed with the updated app list.

The updated app list should be retained in the next app launch."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Import App List via Text Input (Priority: P1)

A user wants to quickly add a few custom package names to check without creating a file. They navigate to the import screen, paste comma-separated package names into a text field, and submit. The app validates the input, imports the packages, and updates the main screen to show the newly added apps.

**Why this priority**: This is the fastest and most direct way for users to import a small number of packages without external file preparation. It provides immediate value and is the simplest MVP slice.

**Independent Test**: Can be fully tested by entering "com.example.app1, com.example.app2" in the text input field and verifying that the main screen displays these two packages in the app list.

**Acceptance Scenarios**:

1. **Given** user is on the main screen, **When** user taps the settings button in the top bar, **Then** the app navigates to the import screen
2. **Given** user is on the import screen with an empty text field, **When** user enters "com.package.one, com.package.two" and submits, **Then** the app validates the input, saves the packages, returns to the main screen, and displays both packages in the app list
3. **Given** user has imported packages via text input, **When** user closes and reopens the app, **Then** the previously imported packages remain in the app list

---

### User Story 2 - Import App List from TXT File (Priority: P2)

A user has a prepared text file containing package names (one per line or comma-separated). They navigate to the import screen, select a .txt file from their device storage, and import it. The app parses the file, validates the package names, and updates the main screen with the imported list.

**Why this priority**: Text files are simple and widely accessible. This enables users to import larger lists prepared externally, expanding the feature's utility beyond manual text entry.

**Independent Test**: Can be fully tested by creating a .txt file with package names, selecting it via the file picker, and verifying that all packages from the file appear in the main screen's app list.

**Acceptance Scenarios**:

1. **Given** user is on the import screen, **When** user taps "Import from File" and selects a .txt file containing package names, **Then** the app reads the file, parses package names, and navigates back to the main screen with the imported packages displayed
2. **Given** the .txt file contains package names separated by commas, **When** user imports the file, **Then** the app correctly parses all package names
3. **Given** the .txt file contains package names on separate lines, **When** user imports the file, **Then** the app correctly parses all package names
4. **Given** user has imported packages from a .txt file, **When** user relaunches the app, **Then** the imported packages persist in the app list

---

### User Story 3 - Replace Existing App List with New Import (Priority: P2)

A user wants to replace the current app list entirely with a new import (either text input or file). They navigate to the import screen, select the "Replace existing list" option, and proceed with the import. The app clears the current list and replaces it with the newly imported packages.

**Why this priority**: This provides flexibility for users who want to completely change the app list rather than append to it. It's important for users managing different scenarios or testing different package sets.

**Independent Test**: Can be fully tested by importing an initial list, then importing a new list with the "Replace" option, and verifying that only the new packages appear in the main screen.

**Acceptance Scenarios**:

1. **Given** user has an existing app list with packages, **When** user imports new packages with "Replace existing list" option enabled, **Then** the old packages are removed and only the new packages appear in the app list
2. **Given** user imports new packages with "Replace existing list" option disabled, **When** the import completes, **Then** the new packages are appended to the existing app list

---

### Edge Cases

- **Empty input/file**: Import button remains disabled until valid input provided (FR-015, FR-016)
- **Invalid package formats**: Show inline validation with count, import only valid packages (FR-013, FR-014)
- **Duplicate package names**: Automatically removed during import (FR-009, Assumption 4)
- **File selection cancelled**: Return to import screen without changes
- **File read failure**: Display error message describing the failure (FR-018)
- **Whitespace in packages**: Automatically trimmed during parsing (FR-007)
- **Navigate away without importing**: Changes discarded, return to main screen with original list (Assumption 7)
- **Same package imported multiple times**: Deduplicated to single instance (Assumption 4)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a settings button in the top bar of the main screen that navigates to the import screen
- **FR-002**: System MUST display the original hardcoded 213-package list on first app launch before any import occurs
- **FR-003**: System MUST allow users to input package names as a comma-separated string in a text field
- **FR-004**: System MUST allow users to select and import package names from a UTF-8 encoded .txt file
- **FR-005**: System MUST parse comma-separated package names from text input
- **FR-006**: System MUST parse package names from .txt files that contain either comma-separated values or one package name per line
- **FR-007**: System MUST validate package names to ensure they follow valid Android package naming conventions using regex pattern `^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$` (lowercase letters, dots, numbers, underscores; must have at least two dot-separated segments)
- **FR-008**: System MUST trim leading and trailing whitespace from package names during import
- **FR-009**: System MUST remove duplicate package names within a single import operation
- **FR-010**: System MUST provide an option to replace the existing app list or append to it, with replace as the default behavior
- **FR-011**: System MUST persist the imported app list to storage so it is retained across app launches, completely replacing the hardcoded package list
- **FR-012**: System MUST navigate back to the main screen after successful import
- **FR-013**: System MUST refresh the main screen to display the updated app list after import
- **FR-014**: System MUST display inline validation showing the count of valid and invalid package names when processing imports
- **FR-015**: System MUST import only valid package names and skip invalid ones, displaying which packages were rejected
- **FR-016**: System MUST disable the import button when no valid package names are present in the input
- **FR-017**: System MUST enable the import button only when at least one valid package name is detected
- **FR-018**: System MUST display an error message if file selection fails or file cannot be read

### Key Entities

- **Package Name**: A string representing an Android application package identifier (e.g., "com.example.app"), which follows Android package naming conventions
- **App List**: A collection of package names that the system will check for installation and license activity presence
- **Import Source**: The origin of package names, which can be text input or .txt file

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can navigate from the main screen to the import screen with a single tap on the settings button
- **SC-002**: Users can successfully import up to 500 package names via text input in under 5 seconds
- **SC-003**: Users can successfully import up to 1000 package names from a file in under 10 seconds
- **SC-004**: The app correctly persists imported package names across app launches with 100% reliability
- **SC-005**: The main screen displays the updated app list immediately after import without requiring manual refresh
- **SC-006**: 95% of valid package name formats are correctly parsed and imported without errors
- **SC-007**: Users receive clear feedback within 2 seconds for invalid input or file errors

## Clarifications

### Session 2026-01-17

- Q: How should the system handle invalid package names during import? → A: Show inline validation with count of valid/invalid packages, import only valid ones
- Q: What should happen if user attempts to submit empty input or file? → A: Prevent submission until at least one valid package name is provided (disable import button)
- Q: How should imported packages interact with the existing hardcoded 213-package list? → A: Imported list replaces hardcoded list entirely (user takes full control)
- Q: What should the app display on first launch before any import? → A: Show the original 213 hardcoded packages on first launch (until user imports custom list)
- Q: What file encoding should be supported for .txt file imports? → A: UTF-8 only (Android standard, covers all valid package name characters)

## Assumptions

1. **File Format Parsing**: For .txt files, package names can be separated by commas, newlines, or both. The parser will handle all common text file formats. Files must be UTF-8 encoded.
2. **Default Import Mode**: The default behavior will be to replace the existing app list with the imported packages, unless the user explicitly selects "Append to existing list" option.
3. **Package Name Validation**: Only basic format validation will be performed (valid characters, dot-separated structure). The system will not verify if packages actually exist on the device during import.
4. **Duplicate Handling**: If the same package name appears multiple times in the import source, only one instance will be added. If a package name already exists in the current list and append mode is used, the duplicate will be ignored.
5. **File Size Limits**: Text files up to 1MB will be supported for import, which accommodates tens of thousands of package names.
6. **Storage Mechanism**: Imported package names will be stored persistently and will completely replace the hardcoded 213-package list in AppRepository. On first launch (before any import), the app uses the original hardcoded list. Once a user imports a custom list, the hardcoded list is no longer used.
7. **Navigation**: The back button on the import screen will return to the main screen without importing (cancel behavior).
