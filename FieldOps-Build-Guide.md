# FieldOps Build Guide

This guide explains how to build and run FieldOps on Windows without Android Studio.

## 1. Requirements

FieldOps can be built using:

- Windows
- Java JDK
- Android SDK command-line tools
- Android SDK Platform Tools
- Gradle Wrapper
- VS Code or another text editor
- A physical Android device or Android emulator

Android Studio is not required.

## 2. Project Location

The project root is:

C:\Users\Admin\fieldops

Open a Windows Command Prompt in this directory.

    cd C:\Users\Admin\fieldops

## 3. Verify Java

Check the installed Java version:

    java -version

The project uses Java 17 for Android compilation through the Gradle toolchain.

## 4. Verify ADB

Check that Android Debug Bridge is available:

    adb version

Connect an Android device with USB debugging enabled and verify:

    adb devices

The device should appear in the list.

## 5. Firebase Configuration

The local Firebase configuration file is:

    app\google-services.json

This file is intentionally excluded from Git because it contains project-specific Firebase configuration.

Before building the application locally, make sure the correct `google-services.json` is present at:

    C:\Users\Admin\fieldops\app\google-services.json

The CI environment creates its own temporary Firebase configuration during GitHub Actions.

## 6. Build the Debug APK

From the project root, run:

    gradlew.bat :app:assembleDebug

If the build succeeds, the debug APK is generated at:

    app\build\outputs\apk\debug\app-debug.apk

## 7. Install the APK

With an Android device connected:

    adb install -r app\build\outputs\apk\debug\app-debug.apk

If an older version is already installed, `-r` updates the existing installation while preserving application data when possible.

## 8. Launch the Application

The application package is:

    com.zaidsiddique.fieldops

Launch it with:

    adb shell monkey -p com.zaidsiddique.fieldops 1

## 9. Run Unit Tests

Run the debug unit tests:

    gradlew.bat testDebugUnitTest

## 10. Run Lint

Run Android lint:

    gradlew.bat lint

A successful run ends with:

    BUILD SUCCESSFUL

## 11. Full Local Verification

Before pushing changes to GitHub, run:

    gradlew.bat testDebugUnitTest

Then:

    gradlew.bat lint

Then:

    gradlew.bat :app:assembleDebug

All three commands should complete successfully.

## 12. Git Workflow

Check the current state:

    git status

Review changes:

    git diff

Stage changes:

    git add .

Create a commit:

    git commit -m "Describe the change"

Push to GitHub:

    git push origin main

## 13. GitHub Actions

Every push to `main` and every pull request runs the CI workflow located at:

    .github\workflows\ci.yml

The CI workflow:

1. Checks out the repository.
2. Sets up Java 17.
3. Creates a temporary CI Firebase configuration.
4. Runs unit tests.
5. Runs Android lint.

A successful workflow should show a green check in GitHub Actions.

## 14. Project Architecture

FieldOps follows an offline-first architecture.

High-level flow:

    QR Scan
       ↓
    QR Confirmation
       ↓
    Photo Capture
       ↓
    GPS Location
       ↓
    Reverse Geocoding
       ↓
    Room Database
       ↓
    PENDING
       ↓
    WorkManager
       ↓
    Firebase Realtime Database
       ↓
    SYNCED

The local Room database is the source of the check-in data while the device is offline.

## 15. Offline Synchronization

When a check-in is created:

    Check-in
       ↓
    Save to Room
       ↓
    PENDING

If network connectivity is unavailable, the record remains locally stored.

When network connectivity becomes available:

    WorkManager
       ↓
    SyncWorker
       ↓
    Firebase Realtime Database
       ↓
    Mark record SYNCED

Failed synchronization attempts use WorkManager retry behavior with exponential backoff.

## 16. Permissions

FieldOps uses:

- Camera permission for QR scanning and photo capture.
- Fine/coarse location permissions for GPS coordinates.
- Internet permission for Firebase synchronization and reverse geocoding.

The application is designed to continue saving a check-in even when location permission is denied.

## 17. Main Technologies

The project currently uses:

- Kotlin
- Jetpack Compose
- MVVM
- Hilt
- Room
- WorkManager
- Firebase Realtime Database
- CameraX
- ML Kit
- Retrofit
- OpenStreetMap Nominatim

## 18. Important Project Files

    app/
    ├── src/
    │   ├── main/
    │   │   ├── java/com/zaidsiddique/fieldops/
    │   │   │   ├── data/
    │   │   │   ├── di/
    │   │   │   ├── location/
    │   │   │   ├── network/
    │   │   │   ├── sync/
    │   │   │   └── ui/
    │   │   └── AndroidManifest.xml
    │   └── test/
    │
    ├── build.gradle.kts
    └── google-services.json
    │
    ├── .github/
    │   └── workflows/
    │       └── ci.yml
    │
    ├── build.gradle.kts
    ├── gradle.properties
    ├── settings.gradle.kts
    ├── gradlew
    ├── gradlew.bat
    └── README.md

## 19. Current Application Flow

The primary check-in flow is:

    Check-in List
          ↓
    Scan QR
          ↓
    Confirm QR
          ↓
    Capture Photo
          ↓
    Save Check-in
          ↓
    Capture Location
          ↓
    Reverse Geocode
          ↓
    Save Locally
          ↓
    Schedule Sync

The saved check-in contains:

- QR code
- Timestamp
- Latitude
- Longitude
- Address
- Photo path
- Synchronization status
- Local check-in ID

## 20. Troubleshooting

### Gradle build fails

Run:

    gradlew.bat --stop

Then retry:

    gradlew.bat :app:assembleDebug

### ADB does not show the device

Run:

    adb devices

Make sure:

- USB debugging is enabled.
- The device is unlocked.
- The computer is authorized on the device.
- ADB is installed and available in PATH.

### Firebase configuration error

Verify that:

    app\google-services.json

exists locally and belongs to the Firebase project configured for the application.

### Unit tests fail

Run:

    gradlew.bat testDebugUnitTest

Read the first failing test rather than relying only on the final Gradle error.

### Lint fails

Run:

    gradlew.bat lint

Fix the reported error first. Warnings should be reviewed separately from errors.

## 21. CI Verification

The project currently has GitHub Actions CI configured.

The expected successful pipeline is:

    Checkout
       ↓
    Java 17
       ↓
    CI Firebase configuration
       ↓
    Unit Tests
       ↓
    Lint
       ↓
    GREEN

## 22. Development Principle

FieldOps is intentionally designed as an offline-first field-data collection application.

The important reliability property is:

> A field worker should be able to complete a check-in even when network connectivity is unavailable.

Synchronization happens separately in the background when connectivity becomes available.