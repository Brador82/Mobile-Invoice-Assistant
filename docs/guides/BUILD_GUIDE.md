# Mobile Invoice OCR - Android Build Guide

## 📋 Prerequisites

### Required Software
- **Android Studio** (installed at: `C:\Program Files\Android\Android Studio`)
- **JDK 21** (bundled with Android Studio)
- **Gradle 8.5** (configured via Gradle Wrapper)

### Environment Setup
Before running any build commands, you need to set JAVA_HOME:

```cmd
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
```

**Note**: This needs to be set in every new terminal session, or you can add it permanently to your Windows environment variables.

---

## 🔧 Project Structure

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/mobileinvoice/ocr/
│   │   │   └── database/       # Room database entities and DAOs
│   │   ├── res/                # Android resources (layouts, values, etc.)
│   │   └── AndroidManifest.xml
│   └── build.gradle            # App-level build configuration
├── gradle/wrapper/
│   ├── gradle-wrapper.jar      # Gradle wrapper executable
│   └── gradle-wrapper.properties
├── gradlew                     # Gradle wrapper script (Linux/Mac)
├── gradlew.bat                 # Gradle wrapper script (Windows)
├── build.gradle                # Project-level build configuration
└── settings.gradle             # Project settings
```

---

## 🚀 Common Build Commands

### 1. View All Available Tasks
```cmd
cd android
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
gradlew.bat tasks
```
Shows all available Gradle tasks you can run.

### 2. Clean Build
```cmd
gradlew.bat clean
```
Removes all build artifacts and output files.

### 3. Build Debug APK
```cmd
gradlew.bat assembleDebug
```
Builds a debug version of the app. Output: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Build Release APK
```cmd
gradlew.bat assembleRelease
```
Builds a release (production) version. Requires signing configuration.

### 5. Install Debug Build on Device
```cmd
gradlew.bat installDebug
```
Builds and installs the debug APK on a connected Android device or emulator.

### 6. Run Unit Tests
```cmd
gradlew.bat test
```
Runs all unit tests in the project.

### 7. Run Lint Checks
```cmd
gradlew.bat lint
```
Analyzes code for potential bugs, performance issues, and style problems.

### 8. Check Dependencies
```cmd
gradlew.bat dependencies
```
Displays the dependency tree for your project.

### 9. View Android Dependencies
```cmd
gradlew.bat androidDependencies
```
Shows Android-specific dependencies.

---

## 📦 Build Types

### Debug Build
- **Command**: `gradlew.bat assembleDebug`
- **Use**: Development and testing
- **Features**: Debuggable, not optimized
- **Output**: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build
- **Command**: `gradlew.bat assembleRelease`
- **Use**: Production deployment
- **Features**: Optimized, minified (if enabled)
- **Output**: `app/build/outputs/apk/release/app-release.apk`
- **Note**: Requires signing configuration in `app/build.gradle`

---

## 🔍 Debugging & Troubleshooting

### Check Build Environment
```cmd
gradlew.bat buildEnvironment
```

### View Java Toolchains
```cmd
gradlew.bat javaToolchains
```

### Clean and Rebuild
```cmd
gradlew.bat clean build
```

### Run with Stack Trace (for errors)
```cmd
gradlew.bat build --stacktrace
```

### Run with Debug Info
```cmd
gradlew.bat build --debug
```

---

## 📱 Device Installation

### Install Debug Build
```cmd
gradlew.bat installDebug
```

### Uninstall App
```cmd
gradlew.bat uninstallDebug
```

### Uninstall All Variants
```cmd
gradlew.bat uninstallAll
```

---

## 🧪 Testing

### Run All Tests
```cmd
gradlew.bat test
```

### Run Debug Unit Tests
```cmd
gradlew.bat testDebugUnitTest
```

### Run Instrumented Tests (requires device)
```cmd
gradlew.bat connectedAndroidTest
```

---

## 🎯 Quick Start Workflow

1. **First Time Setup**
   ```cmd
   cd android
   set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
   ```

2. **Clean Previous Builds**
   ```cmd
   gradlew.bat clean
   ```

3. **Build Debug APK**
   ```cmd
   gradlew.bat assembleDebug
   ```

4. **Install on Device** (optional)
   ```cmd
   gradlew.bat installDebug
   ```

---

## ⚙️ Configuration Files

### gradle-wrapper.properties
- Located: `android/gradle/wrapper/gradle-wrapper.properties`
- Current Gradle version: 8.5
- Gradle distribution: `gradle-8.5-bin.zip`

### app/build.gradle
- **compileSdk**: 34
- **minSdk**: 24
- **targetSdk**: 34
- **Java Version**: 1.8 (source & target compatibility)

### Dependencies (from app/build.gradle)
- AndroidX AppCompat
- Material Design Components
- ConstraintLayout
- RecyclerView & CardView
- Room Database
- Retrofit (for API calls)
- Glide (for image loading)

---

## 🛠️ Gradle Properties

Current configuration in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
```

---

## 🚨 Common Issues & Solutions

### Issue: "JAVA_HOME is not set"
**Solution**: 
```cmd
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
```

### Issue: "Unsupported class file major version"
**Solution**: Make sure you're using Gradle 8.5+ which supports JDK 21

### Issue: "SDK location not found"
**Solution**: Create `local.properties` file in android folder:
```properties
sdk.dir=C\:\\Users\\[YourUsername]\\AppData\\Local\\Android\\Sdk
```

### Issue: Build is slow
**Solution**: Add to `gradle.properties`:
```properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

---

## 📊 Build Output Locations

- **APK Files**: `android/app/build/outputs/apk/`
- **AAB Files**: `android/app/build/outputs/bundle/`
- **Build Reports**: `android/app/build/reports/`
- **Test Results**: `android/app/build/test-results/`
- **Lint Reports**: `android/app/build/reports/lint-results.html`

---

## 🌐 Additional Resources

- [Android Developer Documentation](https://developer.android.com/docs)
- [Gradle Build Tool](https://gradle.org/guides/)
- [AndroidX Migration Guide](https://developer.android.com/jetpack/androidx/migrate)

---

## 📝 Notes

- Always run `gradlew.bat clean` before a fresh build if you encounter issues
- The Gradle wrapper ensures everyone uses the same Gradle version
- Use Android Studio for GUI-based building and debugging
- For CI/CD, use the command-line Gradle commands

---

**Last Updated**: January 8, 2026  
**Project**: Mobile Invoice OCR  
**Android SDK**: 34  
**Min SDK**: 24  
**Gradle Version**: 8.5  
**JDK Version**: 21
