# FieldOps
[![CI](https://github.com/bruhlog/fieldops/actions/workflows/ci.yml/badge.svg)](https://github.com/bruhlog/fieldops/actions/workflows/ci.yml)
Offline-first Android field-data collection application.
FieldOps is designed for field workers who need to collect reliable field data even when network connectivity is unavailable or unreliable.
A field worker can scan a QR code, capture a photo, attach the device's current GPS location, obtain a readable address through reverse geocoding,
and save the check-in locally. The check-in is then synchronized with Firebase automatically when network connectivity becomes available.
---
## Features
- QR code scanning using CameraX + ML Kit
- Photo capture using CameraX
- GPS location capture
- Reverse geocoding using OpenStreetMap Nominatim
- Offline-first local persistence with Room
- Automatic background synchronization with WorkManager
- Firebase Realtime Database synchronization
- Network-aware synchronization using `NetworkType.CONNECTED`
- Exponential retry backoff
- Check-in synchronization status using `PENDING` / `SYNCED`
- Check-ins survive application force-stop
- Graceful camera permission handling
- Graceful location permission handling
- Jetpack Compose UI
- MVVM architecture
- Hilt dependency injection
- Unit tests
- GitHub Actions CI
## Tech Stack
| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| Jetpack Compose | UI |
| MVVM | Application architecture |
| Hilt | Dependency injection |
| Room | Local persistence |
| WorkManager | Background synchronization |
| Firebase Realtime Database | Remote synchronization |
| CameraX | Camera and image capture |
| ML Kit | QR/barcode scanning |
| Retrofit | HTTP networking |
| OpenStreetMap Nominatim | Reverse geocoding |
| Gradle | Build system |
| GitHub Actions | Continuous integration |
## Why This Project
The main engineering challenge in FieldOps is **offline-first synchronization**.
A field worker cannot always depend on a reliable internet connection. Therefore, FieldOps does not require Firebase to be immediately available
before a check-in can be completed.
Instead, the application follows a local-first workflow:
1. The check-in is written to the local Room database.
2. The check-in is initially marked as `PENDING`.
3. WorkManager schedules synchronization work.
4. WorkManager waits for network connectivity.
5. `SyncWorker` uploads pending check-ins to Firebase.
6. Successfully uploaded check-ins are marked as `SYNCED`.
7. Failed synchronization attempts are retried using exponential backoff.
This allows the field worker to continue collecting data even when the device is temporarily offline.
## Application Flow
The primary check-in workflow is:
```text
Check-in List
 |
 v
 Scan QR
 |
 v
Confirm QR Code
 |
 v
Capture Photo
 |
 v
Capture Location
 |
 v
Reverse Geocode
 |
 v
 Save to Room
 |
 v
 PENDING
 |
 v
WorkManager
 |
 v
SyncWorker
 |
 v
Firebase Realtime DB
 |
 v
 SYNCED
```
## Architecture
FieldOps uses a layered architecture built around Jetpack Compose, ViewModels, repositories, local persistence, and background synchronization.
```text
 +----------------------+
 | Jetpack Compose |
 | UI |
 +----------+-----------+
 |
 v
 +----------------------+
 | ViewModel |
 +----------+-----------+
 |
 v
 +----------------------+
 | Repository |
 +----------+-----------+
 |
 +-------+-------+
 | |
 v v
 +-----------+ +-------------+
 | Room | | Firebase |
 | Local DB | | Realtime DB |
 +-----+-----+ +-------------+
 |
 | Pending records
 v
 +-------------+
 | WorkManager |
 | SyncWorker |
 +------+------+
 |
 v
 +-------------+
 | Firebase |
 +-------------+
```
## Offline Sync Flow
When a check-in is created:
```text
Create Check-in
 |
 v
Save to Room
 |
 v
 PENDING
 |
 v
Network Available
 |
 v
 WorkManager
 |
 v
 SyncWorker
 |
 v
Upload to Firebase
 |
 v
 SYNCED
```
If the device has no network connection, the check-in remains stored locally.
When connectivity becomes available, WorkManager can execute the synchronization work.
## Offline Behaviour
FieldOps is designed to continue working when there is no internet connection.
```text
NO INTERNET
 |
 v
Scan QR Code
 |
 v
Capture Photo
 |
 v
Save Check-in
 |
 v
 Room DB
 |
 v
 PENDING
 |
 | Internet Returns
 v
WorkManager
 |
 v
 SyncWorker
 |
 v
 Firebase
 |
 v
 SYNCED
```
A pending check-in remains stored locally even when the application is force-stopped.
## Check-in Data
Each check-in contains:
- Unique local check-in ID
- QR code
- Timestamp
- Latitude
- Longitude
- Reverse-geocoded address
- Local photo path
- Synchronization status
- Remote ID field
The synchronization status is represented by:
```text
PENDING
SYNCED
FAILED
```
The current synchronization workflow primarily uses `PENDING` and `SYNCED`.
## Background Synchronization
Synchronization is handled using Android WorkManager.
The synchronization process is:
```text
Room Database
 |
 v
Find PENDING records
 |
 v
Network constraint satisfied
 |
 v
SyncWorker starts
 |
 v
Upload record to Firebase
 |
 +----------------+
 | |
 Success Failure
 | |
 v v
Mark SYNCED Retry Work
 |
 v
 Exponential Backoff
```
The current WorkManager configuration requires a connected network before synchronization runs.
## Permissions
FieldOps uses the following Android permissions:
- Camera
- Fine location
- Coarse location
- Internet
- Network state
Camera permission is required for QR scanning and photo capture.
Location permission is requested when location data is needed. If location permission is denied, the check-in can still be saved.
## Project Structure
The main application source is organized into packages for data, dependency injection, location, networking, synchronization, and UI.
```text
FieldOps/
|
+-- app/
| |
| +-- src/
| | |
| | +-- main/
| | | |
| | | +-- java/com/zaidsiddique/fieldops/
| | | | +-- data/
| | | | +-- di/
| | | | +-- location/
| | | | +-- network/
| | | | +-- sync/
| | | | +-- ui/
| | |
| | +-- test/
| |
| +-- build.gradle.kts
| +-- google-services.json
|
+-- .github/
| +-- workflows/
| +-- ci.yml
|
+-- build.gradle.kts
+-- gradle.properties
+-- settings.gradle.kts
+-- gradlew
+-- gradlew.bat
+-- README.md
+-- FieldOps-Build-Guide.md
```
## Firebase
FieldOps uses Firebase Realtime Database for remote synchronization.
The application currently stores synchronized check-in records under:
```text
checkins/
+-- demoUser/
 +-- <check-in-id>
```
The Firebase configuration file is intentionally excluded from Git:
```text
app/google-services.json
```
The file is required for a local Firebase-enabled build but is not committed to the repository.
GitHub Actions creates a temporary CI Firebase configuration during CI execution so that the build and tests can run without exposing the local
Firebase configuration.
## Running the Project
The project can be built without Android Studio using:
- Java
- Android SDK command-line tools
- Android SDK Platform Tools
- Gradle Wrapper
- VS Code or another text editor
For the complete Windows terminal-only setup and build process, see:
[FieldOps Build Guide](FieldOps-Build-Guide.md)
## Build Commands
From the project root:
```cmd
gradlew.bat :app:assembleDebug
```
The generated debug APK is located at:
```text
app\build\outputs\apk\debug\app-debug.apk
```
## Install on Android Device
Verify that ADB detects the device:
```cmd
adb devices
```
Install the debug APK:
```cmd
adb install -r app\build\outputs\apk\debug\app-debug.apk
```
Launch FieldOps:
```cmd
adb shell monkey -p com.zaidsiddique.fieldops 1
```
Application ID:
```text
com.zaidsiddique.fieldops
```
## Testing
### Unit Tests
Run:
```cmd
gradlew.bat testDebugUnitTest
```
### Android Lint
Run:
```cmd
gradlew.bat lint
```
### Debug APK
Build:
```cmd
gradlew.bat :app:assembleDebug
```
A complete local verification consists of running all three successfully.
## Continuous Integration
FieldOps uses GitHub Actions for continuous integration.
The workflow is located at:
```text
.github/workflows/ci.yml
```
The CI pipeline performs:
```text
Git Push / Pull Request
 |
 v
 GitHub Actions
 / \
 v v
 Unit Tests Lint
 \ /
 \ /
 PASS
```
The repository currently has a passing CI pipeline.
## CI Status
The CI status is displayed at the top of this README.
CI workflow:
[GitHub Actions](https://github.com/bruhlog/fieldops/actions/workflows/ci.yml)
## Reliability Considerations
The project was tested against several important offline-first scenarios:
- Check-in saved while offline
- Pending check-in synchronized after network restoration
- Pending check-in survives application force-stop
- Pending check-in remains available after reopening the application
- Camera permission denial handled without crashing
- Location permission denial still allows the check-in to be saved
- Firebase synchronization verified
- Unit tests pass
- Android lint passes
- GitHub Actions CI passes
## Development Without Android Studio
Android Studio is not required to build the current project.
The development workflow can be performed using:
```text
Windows
 |
 +-- VS Code
 +-- Java
 +-- Android SDK
 +-- ADB
 +-- Gradle Wrapper
 |
 v
 FieldOps
```
This makes it possible to work on the project on a machine where Android Studio is not practical to run.
## Engineering Highlights
FieldOps demonstrates several production-oriented Android concepts.
### Offline-first data handling
Important field data is persisted locally before remote synchronization.
### Background work
WorkManager handles synchronization independently from the UI lifecycle.
### Network-aware processing
Synchronization waits for network connectivity rather than continuously attempting remote requests.
### Retry strategy
Failed synchronization attempts use exponential backoff.
### Dependency injection
Hilt provides application dependencies such as:
- Room database
- DAO
- Firebase Realtime Database
- Location helper
- Geocoding service
### Camera and QR processing
CameraX provides camera functionality while ML Kit performs QR/barcode detection.
### Local persistence
Room provides structured local storage for check-ins and their synchronization state.
## Current Project Status
**v1.0**
Core offline-first check-in functionality has been implemented and tested.
Current functionality includes:
- QR scanning
- Photo capture
- GPS location capture
- Reverse geocoding
- Local Room persistence
- Pending synchronization state
- WorkManager background synchronization
- Firebase Realtime Database synchronization
- Retry handling
- Permission handling
- Unit testing
- Android lint
- GitHub Actions CI
## Repository
GitHub repository:
[https://github.com/bruhlog/fieldops](https://github.com/bruhlog/fieldops)
## License
This project is currently presented as a portfolio/application project.
No separate open-source license has been specified yet.