# Feature Specification: Custom App List Import

**Feature Branch**: `001-app-list-import`
**Created**: 2026-01-17
**Status**: Draft
**Input**: User description: "Add a feature for user to import the list of the apps to check. The import feature supports multiple ways:
- A string of package names separated by commas
- File: txt, csv

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

### User Story 3 - Import App List from CSV File (Priority: P3)

A user has a CSV file (potentially exported from a spreadsheet or database) containing package names. They navigate to the import screen, select a .csv file, and import it. The app parses the CSV (using the first column or a specified column for package names), validates the data, and updates the main screen.

**Why this priority**: CSV files offer structured data import and are common in enterprise/data-driven workflows. While valuable, this is lower priority since text files and manual input cover most use cases.

**Independent Test**: Can be fully tested by creating a CSV file with package names in the first column, selecting it via the file picker, and verifying that all packages appear in the main screen's app list.

**Acceptance Scenarios**:

1. **Given** user is on the import screen, **When** user taps "Import from File" and selects a .csv file with package names in the first column, **Then** the app reads the file, parses package names from the first column, and navigates back to the main screen with the imported packages displayed
2. **Given** the CSV file contains multiple columns, **When** user imports the file, **Then** the app uses the first column for package names and ignores other columns
3. **Given** user has imported packages from a CSV file, **When** user relaunches the app, **Then** the imported packages persist in the app list

---

### User Story 4 - Replace Existing App List with New Import (Priority: P2)

A user wants to replace the current app list entirely with a new import (either text input or file). They navigate to the import screen, select the "Replace existing list" option, and proceed with the import. The app clears the current list and replaces it with the newly imported packages.

**Why this priority**: This provides flexibility for users who want to completely change the app list rather than append to it. It's important for users managing different scenarios or testing different package sets.

**Independent Test**: Can be fully tested by importing an initial list, then importing a new list with the "Replace" option, and verifying that only the new packages appear in the main screen.

**Acceptance Scenarios**:

1. **Given** user has an existing app list with packages, **When** user imports new packages with "Replace existing list" option enabled, **Then** the old packages are removed and only the new packages appear in the app list
2. **Given** user imports new packages with "Replace existing list" option disabled, **When** the import completes, **Then** the new packages are appended to the existing app list

---

### Edge Cases

- What happens when user inputs an empty string or file?
- What happens when file contains invalid package name formats (e.g., spaces, special characters)?
- What happens when file is empty?
- What happens when file contains duplicate package names?
- What happens when user cancels file selection?
- What happens when file read fails (permissions, corrupted file)?
- What happens when imported packages contain whitespace (leading/trailing)?
- What happens when CSV file has headers?
- What happens when user navigates away from import screen without importing?
- What happens when the same package name is imported multiple times?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a settings button in the top bar of the main screen that navigates to the import screen
- **FR-002**: System MUST allow users to input package names as a comma-separated string in a text field
- **FR-003**: System MUST allow users to select and import package names from a .txt file
- **FR-004**: System MUST allow users to select and import package names from a .csv file
- **FR-005**: System MUST parse comma-separated package names from text input
- **FR-006**: System MUST parse package names from .txt files that contain either comma-separated values or one package name per line
- **FR-007**: System MUST parse package names from .csv files by extracting values from the first column
- **FR-008**: System MUST validate package names to ensure they follow valid Android package naming conventions (lowercase letters, dots, numbers, underscores)
- **FR-009**: System MUST trim leading and trailing whitespace from package names during import
- **FR-010**: System MUST remove duplicate package names within a single import operation
- **FR-011**: System MUST provide an option to replace the existing app list or append to it, with replace as the default behavior
- **FR-012**: System MUST persist the imported app list to storage so it is retained across app launches
- **FR-013**: System MUST navigate back to the main screen after successful import
- **FR-014**: System MUST refresh the main screen to display the updated app list after import
- **FR-015**: System MUST display an error message if no valid package names are found in the import
- **FR-016**: System MUST display an error message if file selection fails or file cannot be read
- **FR-017**: System MUST validate the first row of CSV files and skip it if it doesn't match valid package name format (treating it as a header)

### Key Entities

- **Package Name**: A string representing an Android application package identifier (e.g., "com.example.app"), which follows Android package naming conventions
- **App List**: A collection of package names that the system will check for installation and license activity presence
- **Import Source**: The origin of package names, which can be text input, .txt file, or .csv file

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can navigate from the main screen to the import screen with a single tap on the settings button
- **SC-002**: Users can successfully import up to 500 package names via text input in under 5 seconds
- **SC-003**: Users can successfully import up to 1000 package names from a file in under 10 seconds
- **SC-004**: The app correctly persists imported package names across app launches with 100% reliability
- **SC-005**: The main screen displays the updated app list immediately after import without requiring manual refresh
- **SC-006**: 95% of valid package name formats are correctly parsed and imported without errors
- **SC-007**: Users receive clear feedback within 2 seconds for invalid input or file errors

## Assumptions

1. **File Format Parsing**: For .txt files, package names can be separated by commas, newlines, or both. The parser will handle all common text file formats.
2. **CSV Structure**: CSV files will use the first column for package names, with the first row optionally containing a header that will be automatically detected and skipped if it doesn't match package name format.
3. **Default Import Mode**: The default behavior will be to replace the existing app list with the imported packages, unless the user explicitly selects "Append to existing list" option.
4. **Package Name Validation**: Only basic format validation will be performed (valid characters, dot-separated structure). The system will not verify if packages actually exist on the device during import.
5. **Duplicate Handling**: If the same package name appears multiple times in the import source, only one instance will be added. If a package name already exists in the current list and append mode is used, the duplicate will be ignored.
6. **File Size Limits**: Files up to 1MB will be supported for import, which accommodates tens of thousands of package names.
7. **Storage Mechanism**: Imported package names will be stored using Android SharedPreferences or a local database, replacing the hardcoded list in AppRepository.
8. **Navigation**: The back button on the import screen will return to the main screen without importing (cancel behavior).
