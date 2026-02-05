# What is what (Mobile Invoice)

You’ve got a few different snapshots of the project in this workspace. This doc labels the “current” buildable app vs older/reference copies.

## Canonical / current (use this)

- **Android app (ML Kit OCR)**: [android/](android/)
  - Entry point: `android/app/src/main/java/com/mobileinvoice/ocr/MainActivity.java`
  - OCR engine: `android/app/src/main/java/com/mobileinvoice/ocr/OCRProcessorMLKit.java`
  - Dependency: `com.google.mlkit:text-recognition:16.0.0` (see `android/app/build.gradle`)

- **Docs (VS Code + Linux build)**: [docs/](docs/)
  - VS Code build guide: [docs/guides/VSCODE_BUILD_GUIDE.md](docs/guides/VSCODE_BUILD_GUIDE.md)
  - Linux build guide: [LINUX_QUICK_SETUP.md](LINUX_QUICK_SETUP.md)

## Old / reference (do not build from here)

- **Archive of deprecated experiments**: `archive/`
  - Old web UI / scripts / Termux python server artifacts.
  - This folder is intentionally ignored by git.

- **Accidental nested duplicate**: [Mobile_Invoice_OCR/](Mobile_Invoice_OCR/)
  - This appears to be a partial copy that mostly contains build output.
  - Safe to delete locally once you’ve verified you don’t need anything inside.

- **Loose/duplicate build guide**: `# Mobile Invoice OCR - Android Build_Guide.txt`
  - Redundant with the Markdown guides under [docs/guides/](docs/guides/).
  - Moved out of repo root into the external archive (`C:\Workspace\Archives\MobileInvoice\loose_docs_*`) to keep the repo clean.

## Other folders in your VS Code workspace

These are *separate top-level folders* (not part of this repo’s build):

- `Mobile_Invoice_2.1/` — a snapshot with Java files at the root (useful for comparing older logic).
- `Mobile_Invoice_Update_2.0/` — primarily documentation/spec snapshot.

## Quick build commands (canonical)

From [android/](android/):

- Windows: `./gradlew.bat assembleDebug`
- Linux: `chmod +x gradlew && ./gradlew assembleDebug`

If you want, I can consolidate these extra snapshots into a single `snapshots/` folder (no deletions) and leave a clear README per snapshot.
