# VS Code Build Guide - Mobile Invoice OCR

## 🎯 Quick Start

This guide shows how to build and develop the Android app using **VS Code** instead of Android Studio.

## 📋 Prerequisites

### 1. Install Java Development Kit (JDK)
```bash
# Check if Java is installed
java -version

# Should show: Java version 8 or higher
```

**Download JDK:** [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)

### 2. Install Android SDK Command Line Tools

**Option A: Via Android Studio (Easiest)**
1. Install Android Studio
2. Open SDK Manager (Tools → SDK Manager)
3. Install SDK Platform API 34 and Build Tools
4. Note SDK location: `C:\Users\[YourName]\AppData\Local\Android\Sdk`

**Option B: Standalone Command Line Tools**
1. Download from: https://developer.android.com/studio#command-tools
2. Extract to: `C:\Android\sdk\cmdline-tools\latest\`
3. Run: `sdkmanager --sdk_root=C:\Android\sdk "platform-tools" "platforms;android-34" "build-tools;34.0.0"`

### 3. Set Environment Variables

Add these to your system PATH:
```
C:\Program Files\Java\jdk-21\bin
C:\Users\[YourName]\AppData\Local\Android\Sdk\platform-tools
C:\Users\[YourName]\AppData\Local\Android\Sdk\tools
```

Add environment variable:
```
ANDROID_HOME = C:\Users\[YourName]\AppData\Local\Android\Sdk
```

**Verify:**
```bash
adb version
# Android Debug Bridge version 1.0.41
```

### 4. Install VS Code Extensions

Open VS Code → Extensions (Ctrl+Shift+X):

**Essential:**
- ✅ **Extension Pack for Java** (Microsoft) - Java language support
- ✅ **Gradle for Java** (Microsoft) - Gradle build support

**Recommended:**
- ✅ **Android** (DiemasMichiels) - Android development support
- ✅ **XML** (Red Hat) - XML editing for layouts
- ✅ **Error Lens** - Inline error messages
- ✅ **GitLens** - Git integration

## 🏗️ Building the App

### Open Project in VS Code

1. Open VS Code
2. `File → Open Folder`
3. Navigate to: `Mobile_Invoice_OCR\android`
4. Click "Select Folder"

### Initial Setup

**Trust the workspace** when prompted.

**Open integrated terminal:** `` Ctrl+` `` or `View → Terminal`

### Build Commands

#### Build Debug APK
```bash
# Windows
.\gradlew assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

**Output:** `app\build\outputs\apk\debug\app-debug.apk`

#### Build Release APK (Unsigned)
```bash
.\gradlew assembleRelease
```

#### Clean Build
```bash
.\gradlew clean
.\gradlew assembleDebug
```

#### Refresh Dependencies
```bash
.\gradlew --refresh-dependencies assembleDebug
```

#### Build with Verbose Output
```bash
.\gradlew assembleDebug --info
```

## 📱 Installing on Device

### Via USB (Recommended)

**1. Enable Developer Mode on Android Device**
- Settings → About Phone
- Tap "Build Number" 7 times
- Go back → Developer Options
- Enable "USB Debugging"

**2. Connect Device**
```bash
# List connected devices
adb devices

# Should show: 
# List of devices attached
# [device-id]    device
```

**3. Install APK**
```bash
# Option 1: Gradle install command
.\gradlew installDebug

# Option 2: ADB install command
adb install app\build\outputs\apk\debug\app-debug.apk

# Option 3: Uninstall first, then install
adb uninstall com.mobileinvoice.ocr
adb install app\build\outputs\apk\debug\app-debug.apk
```

**4. Launch App**
```bash
adb shell am start -n com.mobileinvoice.ocr/.MainActivity
```

### Via Wireless ADB (No Cable Needed)

**Requirements:** Android 11+ and same WiFi network

**1. Initial Setup (with USB)**
```bash
# Connect via USB first
adb devices

# Enable wireless mode
adb tcpip 5555

# Find device IP address
adb shell ip addr show wlan0
```

**2. Connect Wirelessly**
```bash
# Disconnect USB cable
# Connect to device IP
adb connect [device-ip]:5555

# Verify connection
adb devices
# Should show: [device-ip]:5555    device

# Now install normally
adb install app\build\outputs\apk\debug\app-debug.apk
```

## 🐛 Debugging and Logs

### View Logcat in VS Code Terminal

```bash
# All logs
adb logcat

# Filter by app package
adb logcat | findstr "mobileinvoice"

# Filter by error level
adb logcat *:E

# Filter by tag
adb logcat -s "OCRProcessor"

# Clear logs first
adb logcat -c
adb logcat | findstr "mobileinvoice"
```

### Save Logs to File
```bash
adb logcat > logcat.txt
```

### Common Debug Commands
```bash
# List installed packages
adb shell pm list packages | findstr "mobileinvoice"

# Get app info
adb shell dumpsys package com.mobileinvoice.ocr

# Clear app data
adb shell pm clear com.mobileinvoice.ocr

# Pull database from device
adb pull /data/data/com.mobileinvoice.ocr/databases/invoice_database .

# Pull app files
adb pull /data/data/com.mobileinvoice.ocr/files/ ./app_files/
```

## 🎨 Editing Layout Files

**VS Code XML editing:**
1. Install "XML" extension (Red Hat)
2. Open layout files: `app\src\main\res\layout\*.xml`
3. Edit XML directly (no visual designer)
4. Use Android XML autocomplete

**Preview layouts:**
- Use Android Studio for visual preview
- Or build APK and test on device

## ⚙️ VS Code Configuration

### Create Build Tasks (Optional)

Create `.vscode\tasks.json`:
```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Build Debug APK",
      "type": "shell",
      "command": "${workspaceFolder}/gradlew",
      "args": ["assembleDebug"],
      "group": {
        "kind": "build",
        "isDefault": true
      },
      "problemMatcher": []
    },
    {
      "label": "Install Debug",
      "type": "shell",
      "command": "${workspaceFolder}/gradlew",
      "args": ["installDebug"],
      "problemMatcher": []
    },
    {
      "label": "Clean Build",
      "type": "shell",
      "command": "${workspaceFolder}/gradlew",
      "args": ["clean", "assembleDebug"],
      "problemMatcher": []
    }
  ]
}
```

**Run tasks:** `Ctrl+Shift+P` → "Tasks: Run Task"

### Create Launch Configuration

Create `.vscode\launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Android App",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    }
  ]
}
```

## 🚀 Quick Build Scripts

### Create `build-and-install.bat`

In `android\` folder:
```batch
@echo off
echo ====================================
echo Mobile Invoice OCR - Build & Install
echo ====================================
echo.

echo [1/3] Cleaning previous build...
call gradlew clean
if %errorlevel% neq 0 goto error

echo.
echo [2/3] Building debug APK...
call gradlew assembleDebug
if %errorlevel% neq 0 goto error

echo.
echo [3/3] Installing on device...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if %errorlevel% neq 0 goto error

echo.
echo ====================================
echo SUCCESS! App installed on device.
echo ====================================
echo.
echo Launching app...
adb shell am start -n com.mobileinvoice.ocr/.MainActivity
goto end

:error
echo.
echo ====================================
echo ERROR! Build or install failed.
echo ====================================
echo.

:end
pause
```

**Usage:** Double-click `build-and-install.bat` in Windows Explorer or run from VS Code terminal:
```bash
.\build-and-install.bat
```

### Create `logs.bat`

```batch
@echo off
echo Showing logs for Mobile Invoice OCR...
echo Press Ctrl+C to stop.
echo.
adb logcat -c
adb logcat | findstr "mobileinvoice"
```

## 🔧 Troubleshooting

### Issue: `gradlew: command not found`

**Solution:**
```bash
# Make sure you're in the android folder
cd android

# Windows: Use .\gradlew
.\gradlew assembleDebug

# Linux/Mac: Make executable first
chmod +x gradlew
./gradlew assembleDebug
```

### Issue: `ANDROID_HOME not set`

**Solution:**
```bash
# Windows (temporary)
set ANDROID_HOME=C:\Users\[YourName]\AppData\Local\Android\Sdk

# Windows (permanent) - System Properties → Environment Variables
# Add: ANDROID_HOME = C:\Users\[YourName]\AppData\Local\Android\Sdk
```

### Issue: `SDK location not found`

**Solution:**
Create `android\local.properties`:
```properties
sdk.dir=C\:\\Users\\[YourName]\\AppData\\Local\\Android\\Sdk
```

### Issue: `adb: device not found`

**Solution:**
```bash
# Check USB connection
adb devices

# If unauthorized, check device for permission prompt

# If offline, restart adb
adb kill-server
adb start-server
adb devices
```

### Issue: Build fails with "Out of memory"

**Solution:**
Create/edit `android\gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m
org.gradle.parallel=true
org.gradle.daemon=true
```

## 📊 Build Performance Tips

### 1. Enable Gradle Daemon
Already enabled in `gradle.properties`

### 2. Parallel Builds
```properties
org.gradle.parallel=true
```

### 3. Build Cache
```bash
.\gradlew assembleDebug --build-cache
```

### 4. Offline Mode (when dependencies are cached)
```bash
.\gradlew assembleDebug --offline
```

## 🎯 Typical Workflow

### Daily Development Workflow
```bash
# 1. Pull latest code
git pull

# 2. Open VS Code
code android

# 3. Make code changes

# 4. Build and install
.\gradlew installDebug

# 5. View logs
adb logcat | findstr "mobileinvoice"

# 6. Test features on device

# 7. Commit changes
git add .
git commit -m "Feature: Added X"
git push
```

### Release Workflow
```bash
# 1. Update version in build.gradle
# versionCode 2
# versionName "1.1.0"

# 2. Build release APK
.\gradlew assembleRelease

# 3. Sign APK (manual or via Gradle)

# 4. Test release APK
adb install app\build\outputs\apk\release\app-release.apk

# 5. Distribute
```

## 🆚 VS Code vs Android Studio

| Feature | VS Code | Android Studio |
|---------|---------|----------------|
| **Startup Time** | ⚡ Fast | 🐌 Slow |
| **Memory Usage** | 💚 Low | 🔴 High |
| **Layout Editor** | ❌ No | ✅ Yes |
| **Build System** | ✅ Gradle CLI | ✅ Gradle GUI |
| **Debugging** | ⚠️ Manual | ✅ Integrated |
| **APK Install** | 📱 ADB | 🔘 One-click |
| **Extensions** | 📦 Many | 🔌 Android-specific |
| **Git Integration** | ✅ Excellent | ✅ Good |

## ✅ Advantages of VS Code

- ✅ **Lightweight**: Uses 1/4 the RAM of Android Studio
- ✅ **Fast**: Opens instantly
- ✅ **Flexible**: Use for multiple languages/projects
- ✅ **Command-line**: Full control over build process
- ✅ **Remote**: Easy SSH development
- ✅ **Customizable**: Thousands of extensions

## 🎓 Learning Resources

### Gradle Commands
```bash
# List all tasks
.\gradlew tasks

# Help for specific task
.\gradlew help --task assembleDebug

# Build with stack trace
.\gradlew assembleDebug --stacktrace
```

### ADB Commands Reference
```bash
# Device info
adb shell getprop ro.build.version.release  # Android version
adb shell getprop ro.product.model          # Device model

# Screenshots
adb shell screencap -p /sdcard/screen.png
adb pull /sdcard/screen.png

# Screen recording
adb shell screenrecord /sdcard/demo.mp4
# Press Ctrl+C to stop
adb pull /sdcard/demo.mp4
```

## 🏁 Summary

**VS Code Setup Checklist:**
- [x] JDK installed and in PATH
- [x] Android SDK installed
- [x] ANDROID_HOME environment variable set
- [x] VS Code extensions installed
- [x] Project opened in VS Code
- [x] Gradle build successful
- [x] Device connected and recognized by ADB
- [x] APK installed on device
- [x] App running successfully

**You're now ready to develop Android apps in VS Code!** 🚀

---

**Pro Tip:** Use `.\gradlew installDebug && adb shell am start -n com.mobileinvoice.ocr/.MainActivity` to build, install, and launch in one command!
