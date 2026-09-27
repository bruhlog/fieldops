# FieldOps

Offline-first Android field-data collection app.

FieldOps lets a field worker scan a QR code, capture a photo, attach the device's current GPS location, and save the check-in locally. The data is then synchronized with Firebase automatically when network connectivity returns.

## Features

- QR code scanning using CameraX + ML Kit
- Photo capture using CameraX
- GPS location capture
- Reverse geocoding to obtain a readable address
- Offline-first local persistence with Room
- Automatic background synchronization with WorkManager
- Firebase Realtime Database synchronization
- Network-aware sync using `NetworkType.CONNECTED`
- Exponential retry backoff
- Check-in sync status (`PENDING` / `SYNCED`)
- Check-ins survive app force-stop/process death
- Graceful camera and location permission handling
- Jetpack Compose UI
- Unit tests
- GitHub Actions CI

## Stack

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

## Why this project

The main engineering challenge is **offline-first synchronization**.

A field worker cannot always depend on a reliable network connection. Therefore, FieldOps does not make Firebase the immediate source of truth.

Instead:

1. The check-in is written to the local Room database immediately.
2. The UI displays the check-in as `PENDING`.
3. WorkManager waits for network connectivity.
4. `SyncWorker` uploads pending records to Firebase.
5. Successfully uploaded records are marked `SYNCED`.
6. Failed uploads are retried using exponential backoff.

This means a temporary loss of connectivity does not prevent the field worker from completing their work.

## Architecture

```text
                    ┌──────────────────────┐
                    │   Jetpack Compose    │
                    │         UI           │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │     ViewModel        │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │     Repository       │
                    └──────────┬───────────┘
                               │
                    ┌──────────┴───────────┐
                    │                      │
                    ▼                      ▼
          ┌─────────────────┐    ┌─────────────────┐
          │      Room       │    │    Firebase     │
          │ Local Database  │    │ Realtime DB     │
          └────────┬────────┘    └─────────────────┘
                   │
                   │ Pending records
                   ▼
          ┌─────────────────────┐
          │     WorkManager     │
          │     SyncWorker      │
          └─────────────────────┘
## Offline Sync Flow

Create Check-in
       |
       v
   Save to Room
       |
       v
    PENDING
       |
       v
Network becomes available
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

## Running the Project

The project can be built without Android Studio using the Android command-line tools, Gradle, Java, and VS Code.
See FieldOps-Build-Guide.md for the full terminal-only build process.

## Testing

Run unit tests:

./gradlew testDebugUnitTest

Run Android lint:

./gradlew lint

Build the debug APK:

./gradlew :app:assembleDebug

## CI

GitHub Actions runs unit tests and Android lint automatically on every push and pull request.
Workflow:

Git Push / Pull Request
          |
          v
    GitHub Actions
       /       \
      v         v
   Tests       Lint
      \         /
       \       /
         PASS

## Offline Behaviour

FieldOps continues working when there is no internet connection.

No Internet
     |
     v
Scan QR
     |
     v
Capture Photo
     |
     v
Save Check-in
     |
     v
Room Database
     |
     v
PENDING
     |
     v
Internet Returns
     |
     v
WorkManager
     |
     v
Firebase
     |
     v
SYNCED

A pending check-in also remains available after the application is force-stopped.

## Project Status

v1.0
Core offline-first check-in, local persistence, background synchronization, and Firebase synchronization have been implemented and tested.