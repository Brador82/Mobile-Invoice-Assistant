# Setup & Installation Guide

Complete instructions for setting up the Mobile Invoice OCR project from scratch.

## Prerequisites

### Required Software

1. **Android Studio** (Hedgehog 2023.1.1+)
   - Download: https://developer.android.com/studio
   - Install Android SDK Platform 34
   - Install Android Build Tools 34.0.0+

2. **Java Development Kit (JDK)**
   - Version 8 (1.8) or higher
   - Bundled with Android Studio or download from Oracle/OpenJDK

3. **Git** (for cloning repository)
   - Download: https://git-scm.com/downloads

### Optional Tools

- **ADB** (Android Debug Bridge) - included with Android Studio
- **Scrcpy** - for wireless debugging and APK installation
- **Gradle** - bundled with project (gradlew wrapper)

## Installation Steps

### 1. Clone the Repository

```bash
# Clone the repository
git clone https://github.com/Brador82/Mobile-Invoice-Assistant.git

# Navigate to Android project directory
cd Mobile-Invoice-Assistant/android
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Click **File → Open**
3. Navigate to `Mobile-Invoice-Assistant/android` folder
4. Click **OK**
5. Wait for Gradle sync to complete (downloads dependencies)

### 3. Configure Android SDK

Android Studio should auto-configure, but verify:

1. **File → Settings → Appearance & Behavior → System Settings → Android SDK**
2. Ensure these are installed:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Android SDK Platform-Tools
   - Android SDK Tools

### 4. Sync Gradle Dependencies

Gradle will automatically download:
- Google ML Kit Text Recognition (16.0.0)
- Room Database (2.6.0)
- CameraX libraries
- Apache POI (for Excel export)
- Material Design components

If sync fails, try:
```bash
# Force refresh dependencies
./gradlew --refresh-dependencies
```

### 5. Build the Project

#### Using Android Studio
- **Build → Make Project** (Ctrl+F9)
- **Build → Build Bundle(s) / APK(s) → Build APK(s)**

#### Using Command Line
```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing configuration)
./gradlew assembleRelease
```

### 6. Install on Device

#### Via USB
```bash
# Connect device via USB, enable USB debugging
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

#### Via Android Studio
1. Connect device or start emulator
2. Click **Run** button (green play icon)
3. Select target device

#### Via Wireless ADB
```bash
# Connect device to same WiFi as PC
adb tcpip 5555
adb connect <device-ip>:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

```
android/
├── app/
│   ├── build.gradle              # App-level dependencies
│   ├── src/
│   │   └── main/
│   │       ├── java/com/mobileinvoice/ocr/
│   │       │   ├── MainActivity.java
│   │       │   ├── OCRProcessorMLKit.java    # ML Kit OCR engine
│   │       │   ├── InvoiceDetailActivity.java
│   │       │   ├── CameraActivity.java
│   │       │   ├── InvoiceAdapter.java
│   │       │   └── database/
│   │       │       ├── Invoice.java           # Entity
│   │       │       ├── InvoiceDao.java
│   │       │       └── InvoiceDatabase.java
│   │       ├── res/                           # Layouts, strings, etc.
│   │       └── AndroidManifest.xml
│   └── proguard-rules.pro
├── build.gradle                  # Project-level Gradle config
├── gradle.properties
├── settings.gradle
└── gradlew                       # Gradle wrapper scripts
```

## Configuration

### 1. Package Name

If you want to change the package name from `com.mobileinvoice.ocr`:

1. Right-click package in Project view
2. **Refactor → Rename**
3. Update `applicationId` in `app/build.gradle`
4. Update `package` in `AndroidManifest.xml`

### 2. App Name

Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">Your App Name</string>
```

### 3. Minimum Android Version

Edit `app/build.gradle`:
```gradle
android {
    defaultConfig {
        minSdk 26  // Change to lower version if needed (min 21 for ML Kit)
    }
}
```

### 4. Release Signing

Create `keystore.properties` in project root:
```properties
storeFile=/path/to/keystore.jks
storePassword=your-store-password
keyAlias=your-key-alias
keyPassword=your-key-password
```

Update `app/build.gradle`:
```gradle
android {
    signingConfigs {
        release {
            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
        }
    }
}
```

## Troubleshooting

### Gradle Sync Fails

**Problem**: "Could not resolve com.google.mlkit:text-recognition:16.0.0"

**Solution**:
```bash
# Clear Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches/

# Force refresh
./gradlew --refresh-dependencies
```

### Build Fails with "Duplicate class" Error

**Problem**: Conflicting dependencies

**Solution**: Check `app/build.gradle` for duplicate libraries. Current `packagingOptions` should handle this:
```gradle
packagingOptions {
    pickFirst 'lib/armeabi-v7a/libc++_shared.so'
    pickFirst 'lib/arm64-v8a/libc++_shared.so'
}
```

### ML Kit Model Download Fails on Device

**Problem**: First run requires internet to download ML Kit model

**Solution**: 
- Ensure device has internet connection on first launch
- Model downloads once (~10MB), then works offline
- Or use bundled model (see ML Kit docs)

### Camera Permission Denied

**Problem**: App crashes when trying to use camera

**Solution**:
- Go to Settings → Apps → Mobile Invoice OCR → Permissions
- Enable Camera and Storage permissions

### ADB Device Not Found

**Problem**: `adb devices` shows no devices

**Solution**:
```bash
# Restart ADB server
adb kill-server
adb start-server

# Windows: Add platform-tools to PATH
set PATH=%PATH%;C:\Users\YourName\AppData\Local\Android\Sdk\platform-tools
```

## Building for Production

### 1. Optimize APK Size

Enable ProGuard in `app/build.gradle`:
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

### 2. Generate Signed Release APK

```bash
# With signing configured
./gradlew assembleRelease

# APK location:
# app/build/outputs/apk/release/app-release.apk
```

### 3. Generate App Bundle (for Play Store)

```bash
./gradlew bundleRelease

# AAB location:
# app/build/outputs/bundle/release/app-release.aab
```

## Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] Upload invoice image from gallery
- [ ] Capture invoice with camera
- [ ] OCR extraction completes successfully
- [ ] Customer data extracted correctly
- [ ] Data persists after app restart
- [ ] Delete invoice removes from database
- [ ] View invoice details
- [ ] Export to CSV/Excel

## Next Steps

Once setup is complete:
1. Read [USAGE.md](USAGE.md) for user instructions
2. Review [TECHNICAL.md](TECHNICAL.md) for architecture details
3. Check [API.md](API.md) for OCR customization

## Support

If you encounter issues not covered here:
- Check [GitHub Issues](https://github.com/Brador82/Mobile-Invoice-Assistant/issues)
- Review Android Studio Build Output for error details
- Enable verbose Gradle logging: `./gradlew assembleDebug --info`
