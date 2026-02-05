# Golden Path (Known-Good Build) — Mobile Invoice OCR

This document defines the **canonical, known-good** way to build and run the current app.

## What is “current”?

The canonical, current app is:

- Android project: `android/`
- OCR engine: ML Kit Text Recognition (`OCRProcessorMLKit`)
- Primary guides:
  - `docs/guides/VSCODE_BUILD_GUIDE.md`
  - `LINUX_QUICK_SETUP.md`
  - `README.md`
  - `WHAT_IS_WHAT.md`

## Required vs generated (do not archive as source)

**Required (source of truth):**
- `android/` (source + Gradle wrapper + config)
- `docs/` and root docs (`README.md`, etc.)

**Generated / machine-local (should not be committed; not needed for a clean archive):**
- `android/app/build/`
- `android/.gradle/`
- User Gradle cache: `C:\Users\<you>\.gradle\`
- `android/local.properties` (Android SDK path)
- Keystores / signing keys

## Windows (VS Code / CLI) — build + verify

Prereqs:
- JDK installed (JDK 17 is a safe default for modern Android builds)
- Android SDK installed (platform-tools + build-tools + platform API)
- `adb` available in PATH

Commands (from repo root):

1) Build debug APK
- `cd android`
- `gradlew.bat assembleDebug`

2) Confirm output exists
- `android/app/build/outputs/apk/debug/app-debug.apk`

3) (Optional) Install to a connected device
- `adb devices`
- `gradlew.bat installDebug`

## Linux (CLI) — build + verify

Follow `LINUX_QUICK_SETUP.md` (installs JDK + Android command-line tools + adb).

Commands (from repo root):

- `cd android`
- `chmod +x gradlew`
- `./gradlew assembleDebug`

## Packaging / archiving

Use `tools/package.ps1` to generate:
- a **golden-source zip** (only tracked files; excludes build/cache)
- optional snapshot zips for the other workspace folders

Example:
- `powershell -NoProfile -ExecutionPolicy Bypass -File tools/package.ps1 -ArchiveRoot "C:\Workspace\Archives\MobileInvoice" -IncludeSnapshots`
