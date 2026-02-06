# Linux Quick Setup Guide (Ubuntu/Debian/Zorin)

**Complete beginner-friendly guide to build Android apps on Linux WITHOUT Android Studio!**

---

## 📦 Step 1: Install Dependencies (5 minutes)

### What you're installing:
- **Java (JDK)** - Compiles the app code
- **wget** - Downloads files from the internet
- **unzip** - Extracts compressed files
- **adb** - Android Debug Bridge for phone communication

### How to do it:

**1.1 Open Terminal**
- Press `Ctrl + Alt + T` on your keyboard
- A black window appears - this is the terminal

**1.2 Update package list**
```bash
sudo apt update
```
- **What this does:** Updates the list of available software
- **sudo** means "run as administrator" - you'll be asked for your password
- Type your password (nothing appears while typing - this is normal!) and press Enter

**1.3 Install Java and tools**
```bash
sudo apt install openjdk-11-jdk unzip wget adb -y
```
- **What this does:** Installs Java and necessary tools
- The `-y` automatically answers "yes" to any prompts
- This takes 1-3 minutes depending on your internet speed

**1.4 Verify Java is installed**
```bash
java -version
```
- **What you should see:** Something like `openjdk version "11.0.x"`
- If you see a version number, Java is installed correctly! ✅

---

## 📲 Step 2: Install Android SDK Command Line Tools (5 minutes)

### What this is:
The Android SDK contains tools to build Android apps. We're installing the **command-line version** (no GUI needed).

### How to do it:

**2.1 Create a folder for Android SDK**
```bash
cd
```
- **mkdir** = "make directory" (create folder)
- **-p** = create parent folders if they don't exist
- **~/** means your home folder (e.g., `/home/yourname/`)
- This creates: `/home/yourname/android-sdk/cmdline-tools/`

**2.2 Navigate to that folder**
```bash
cd ~/android-sdk/cmdline-tools
```
- **cd** = "change directory" (go to folder)
- You're now "inside" the cmdline-tools folder

**2.3 Download Android command-line tools**
```bash
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
```
- **What this does:** Downloads a file from Google's servers
- **Size:** ~150 MB - takes 1-2 minutes on decent internet
- You'll see a progress bar like: `[====>    ] 45%`
- When done, you'll see: `saved [filesize]`

**2.4 Extract (unzip) the downloaded file**
```bash

```
- **What this does:** Extracts the contents of the zip file
- The `*` is a wildcard meaning "match any characters"
- You'll see many files being extracted

**2.5 Rename the extracted folder**
```bash
mv cmdline-tools latest
```
- **mv** = "move" (also used for renaming)
- This renames `cmdline-tools` to `latest`
- **Why?** The SDK manager expects this specific folder name

**2.6 Navigate to the bin folder**
```bash
cd latest/bin
```
- You're now in: `/home/yourname/android-sdk/cmdline-tools/latest/bin/`
- This folder contains the `sdkmanager` tool

**2.7 Install required Android components**
```bash
./sdkmanager --sdk_root=$HOME/android-sdk "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```
- **./sdkmanager** = run the SDK manager program
- **--sdk_root=$HOME/android-sdk** = tells it where to install things
- **What it installs:**
  - `platform-tools` - ADB and other tools
  - `platforms;android-34` - Android 14 API
  - `build-tools;34.0.0` - Build tools version 34
- **Size:** ~600 MB total
- **Time:** 3-5 minutes
- You may see a license prompt - type `y` and press Enter to accept

---

## ⚙️ Step 3: Set Environment Variables (2 minutes)

### What this does:
Tells Linux where to find Android tools so you can use them from any folder.

### How to do it:

**3.1 Add ANDROID_HOME to your shell config**
```bash
echo 'export ANDROID_HOME=$HOME/android-sdk' >> ~/.bashrc
```
- **What this does:** Adds a line to your `.bashrc` file
- **.bashrc** is a file that runs every time you open a terminal
- **export ANDROID_HOME** creates a variable pointing to your SDK folder
- **>>** means "append to file" (don't overwrite)

**3.2 Add Android tools to PATH**
```bash
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.bashrc
```
- **PATH** is a list of folders where Linux looks for programs
- This adds the `platform-tools` folder (which contains `adb`) to that list
- Now you can run `adb` from anywhere

**3.3 Apply the changes**
```bash
source ~/.bashrc
```
- **source** = run the commands in this file right now
- Without this, you'd have to close and reopen terminal
- Alternative: Close terminal and open a new one

**3.4 Verify ADB is working**
```bash
adb version
```
- **What you should see:** `Android Debug Bridge version 1.0.41` (or similar)
- If you see a version, ADB is installed correctly! ✅
- If you see "command not found", close terminal and open a new one, then try again

---

## 🔨 Step 4: Build the App (3 minutes)

### What you're doing:
Compiling the Java code into an APK file (Android app package) that can run on your phone.

### How to do it:

**4.1 Download or clone the project**

If you haven't already, get the project on your computer:

```bash
# Option A: If you have git installed
cd ~/Projects  # or wherever you want to put it
git clone https://github.com/Brador82/Mobile_Invoice_OCR.git
cd Mobile_Invoice_OCR/android

# Option B: Download zip from GitHub
# 1. Go to https://github.com/Brador82/Mobile_Invoice_OCR
# 2. Click green "Code" button → Download ZIP
# 3. Extract the zip file
# 4. Open terminal in that folder
```

**4.2 Navigate to the android folder**
```bash
cd /path/to/Mobile_Invoice_OCR/android
```
- Replace `/path/to/` with the actual location
- Example: `cd ~/Downloads/Mobile_Invoice_OCR/android`
- **Tip:** You can drag the folder into terminal to paste the path!

**4.3 Make gradlew executable (first time only)**
```bash
chmod +x gradlew
```
- **chmod** = "change mode" (change permissions)
- **+x** = make executable (allow it to run as a program)
- **gradlew** = Gradle wrapper - a script that builds the app
- You only need to do this once

**4.4 Build the debug APK**
```bash
./gradlew assembleDebug
```
- **./gradlew** = run the Gradle wrapper in current folder
- **assembleDebug** = build a debug version of the app
- **What happens:**
  1. Downloads Gradle (first time only) - ~100 MB, 1-2 minutes
  2. Downloads Android libraries and dependencies - 2-3 minutes
  3. Compiles Java code
  4. Packages everything into an APK
- **First build:** 5-10 minutes (downloads stuff)
- **Subsequent builds:** 30-60 seconds

**4.5 Find your APK**

When done, you'll see:
```
BUILD SUCCESSFUL in 2m 34s
```

Your APK is at:
```bash
app/build/outputs/apk/debug/app-debug.apk
```

**4.6 Verify the APK exists**
```bash
ls -lh app/build/outputs/apk/debug/app-debug.apk
```
- **ls** = list files
- **-lh** = long format, human-readable sizes
- You should see: `app-debug.apk` with size ~25-30 MB

✅ **You've successfully built an Android app on Linux!**

---

## 📱 Step 5: Install on Phone (5 minutes)

### What you're doing:
Transferring the APK to your phone and installing it.

### Prerequisites:

**5.1 Enable Developer Options on your Android phone**

1. Open **Settings** on your phone
2. Scroll down to **About Phone** (or **About Device**)
3. Find **Build Number**
4. **Tap "Build Number" 7 times** rapidly
5. You'll see a message: "You are now a developer!"
6. Go back to main Settings
7. You should now see **Developer Options** (usually under System or Advanced)

**5.2 Enable USB Debugging**

1. Open **Developer Options**
2. Find **USB Debugging**
3. Toggle it **ON**
4. A warning appears - tap **OK**

**5.3 Connect phone to computer**

1. Use a **USB cable** (the one you charge your phone with)
2. Plug USB into computer and phone
3. On your phone, you'll see a popup:
   - **"Allow USB debugging?"**
   - Check **"Always allow from this computer"**
   - Tap **OK**

### How to install:

**5.4 Check if phone is detected**
```bash
adb devices
```

**What you should see:**
```
List of devices attached
ABC123XYZ    device
```

**Possible issues:**
- If you see `unauthorized` - check phone for the USB debugging popup
- If you see `no devices` - try a different USB port or cable
- If you see `offline` - unplug and replug the USB cable

**5.5 Install the APK**
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

- **adb install** = install an APK
- **-r** = reinstall (replace existing app if already installed)
- **What happens:**
  1. Uploads APK to phone (takes 5-10 seconds)
  2. Installs the app
  3. Shows: `Success`

**What you should see:**
```
Performing Streamed Install
Success
```

✅ **App is now installed on your phone!**

**5.6 Launch the app**

Option A: **From terminal**
```bash
adb shell am start -n com.mobileinvoice.ocr/.MainActivity
```
- Opens the app automatically on your phone

Option B: **Manually on phone**
- Open app drawer
- Look for "Mobile Invoice OCR"
- Tap to open

---

## 🔁 Step 6: Making Changes and Rebuilding

### When you edit the code:

**6.1 Clean previous build (optional but recommended)**
```bash
./gradlew clean
```
- Deletes old build files
- Ensures fresh build

**6.2 Rebuild**
```bash
./gradlew assembleDebug
```
- Builds new APK with your changes
- Much faster than first build (30-60 seconds)

**6.3 Reinstall on phone**
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
- The `-r` replaces the old version
- No need to uninstall first

### One-line rebuild and install:
```bash
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```
- **&&** means "run second command if first succeeds"
- Builds and installs in one go!

---

## 📝 Step 7: View App Logs (Debugging)

### When testing your app:

**7.1 Clear old logs (optional)**
```bash
adb logcat -c
```
- **-c** = clear all logs

**7.2 View live logs**
```bash
adb logcat | grep "mobileinvoice"
```
- **adb logcat** = show all Android system logs
- **| grep "mobileinvoice"** = filter to only show lines containing "mobileinvoice"
- Press `Ctrl + C` to stop viewing logs

**7.3 View logs with color (easier to read)**
```bash
adb logcat | grep --color "mobileinvoice"
```

**7.4 Save logs to a file**
```bash
adb logcat | grep "mobileinvoice" > app_logs.txt
```
- Creates `app_logs.txt` with all matching logs
- Useful for troubleshooting

**7.5 View only errors**
```bash
adb logcat | grep "mobileinvoice" | grep -i "error"
```
- **-i** = case-insensitive (matches ERROR, Error, error)

---

## 🎓 Step 8: Understanding What You've Built

### Project structure:
```
Mobile_Invoice_OCR/
└── android/
    ├── app/
    │   ├── src/main/java/       ← Your Java code here
    │   │   └── com/mobileinvoice/ocr/
    │   │       ├── MainActivity.java
    │   │       ├── InvoiceDetailActivity.java
    │   │       └── ...
    │   ├── src/main/res/        ← UI layouts (XML) here
    │   │   └── layout/
    │   │       ├── activity_main.xml
    │   │       └── ...
    │   └── build.gradle         ← Dependencies and config
    └── gradlew                  ← Build script (what you run)
```

### Editing code:

**Option 1: VS Code (recommended)**
```bash
# Install VS Code
sudo snap install code --classic

# Open project
cd /path/to/Mobile_Invoice_OCR/android
code .
```

**Option 2: Any text editor**
- gedit (default Ubuntu text editor)
- nano (terminal text editor)
- vim (advanced terminal editor)

### Making changes:
1. Edit Java files in `app/src/main/java/`
2. Edit layouts in `app/src/main/res/layout/`
3. Save files
4. Rebuild: `./gradlew assembleDebug`
5. Reinstall: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
6. Test on phone

---


## 🐛 Troubleshooting

### Problem: "adb: device not found"

**Solution 1: Restart ADB**
```bash
adb kill-server
adb start-server
adb devices
```

**Solution 2: Check USB connection**
- Try a different USB cable (some cables are charge-only)
- Try a different USB port on your computer
- Make sure USB debugging is enabled on phone

**Solution 3: Fix USB permissions (Linux specific)**
```bash
# Add udev rules for Android devices
sudo wget -O /etc/udev/rules.d/51-android.rules \
  https://raw.githubusercontent.com/snowdream/51-android/master/51-android.rules

# Make it readable
sudo chmod a+r /etc/udev/rules.d/51-android.rules

# Restart udev service
sudo service udev restart

# Unplug and replug your phone
```

### Problem: "ANDROID_HOME not set"

**Check if it's set:**
```bash
echo $ANDROID_HOME
```

**If empty, set it temporarily:**
```bash
export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

**To fix permanently:**
```bash
# Add to .bashrc
echo 'export ANDROID_HOME=$HOME/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.bashrc

# Close terminal and open a new one
```

### Problem: "Permission denied" when running gradlew

**Solution:**
```bash
chmod +x gradlew
./gradlew assembleDebug
```

### Problem: "SDK location not found"

**Solution: Create local.properties file**
```bash
# Navigate to android folder
cd /path/to/Mobile_Invoice_OCR/android

# Create local.properties
echo "sdk.dir=$HOME/android-sdk" > local.properties

# Rebuild
./gradlew assembleDebug
```

### Problem: Build fails with "Out of memory"

**Solution: Increase Gradle memory**
```bash
# Edit gradle.properties
nano android/gradle.properties

# Add these lines:
org.gradle.jvmargs=-Xmx4096m
org.gradle.daemon=true
org.gradle.parallel=true

# Save (Ctrl+O, Enter) and exit (Ctrl+X)

# Rebuild
./gradlew clean assembleDebug
```

### Problem: Phone shows "unauthorized"

**Solution:**
1. On phone: Revoke USB debugging authorizations
   - Settings → Developer Options → Revoke USB debugging authorizations
2. Unplug USB cable
3. Replug USB cable
4. Phone asks: "Allow USB debugging?" → Tap OK
5. Try again: `adb devices`

### Problem: "Could not find or load main class org.gradle.wrapper.GradleWrapperMain"

**Solution:**
```bash
# Delete gradle wrapper and re-download
rm -rf ~/.gradle/wrapper
./gradlew --version
# This will re-download gradle wrapper
```

### Problem: First build takes forever (10+ minutes)

**This is normal!** First build downloads:
- Gradle (~100 MB)
- Android libraries (~500 MB)
- Dependencies (~200 MB)

**Solution:** Wait it out. Subsequent builds will be much faster (30-60 seconds).

### Problem: "adb: command not found"

**Check if ADB is installed:**
```bash
which adb
```

**If not found, install:**
```bash
sudo apt install adb
```

**Or add to PATH:**
```bash
export PATH=$PATH:$HOME/android-sdk/platform-tools
```

---

## ✅ Quick Reference Cheat Sheet

### Build Commands
```bash
# Clean build (removes old files)
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK (for production)
./gradlew assembleRelease

# Clean and build
./gradlew clean assembleDebug
```

### ADB Commands
```bash
# List connected devices
adb devices

# Install APK (replace if exists)
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Uninstall app
adb uninstall com.mobileinvoice.ocr

# Launch app
adb shell am start -n com.mobileinvoice.ocr/.MainActivity

# View logs
adb logcat | grep "mobileinvoice"

# Clear logs
adb logcat -c

# Screenshot
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Restart ADB
adb kill-server
adb start-server
```

### File Locations
```bash
# Project folder
/path/to/Mobile_Invoice_OCR/android/

# Built APK
app/build/outputs/apk/debug/app-debug.apk

# Source code
app/src/main/java/com/mobileinvoice/ocr/

# Layouts (UI)
app/src/main/res/layout/

# Android SDK
~/android-sdk/

# Gradle cache
~/.gradle/
```

---

## 🎉 Success! You're Ready to Develop

**What you've accomplished:**
- ✅ Installed Java and Android SDK on Linux
- ✅ Built an Android app from source code
- ✅ Installed it on your phone
- ✅ All without Android Studio!

**Your workflow:**
1. Edit code in VS Code (or any editor)
2. Run `./gradlew assembleDebug`
3. Run `adb install -r app/build/outputs/apk/debug/app-debug.apk`
4. Test on phone

**Next steps:**
- Edit Java files in `app/src/main/java/`
- Edit UI layouts in `app/src/main/res/layout/`
- Check logs with `adb logcat`
- Read project documentation in `docs/`

---

## 📚 Additional Resources

**Official Documentation:**
- VS Code Build Guide: `docs/guides/VSCODE_BUILD_GUIDE.md`
- Project Structure: `PROJECT_STRUCTURE.md`
- Quick Reference: `QUICKREF.md`

**Learn More:**
- Gradle: https://gradle.org/
- ADB: https://developer.android.com/studio/command-line/adb
- Android Development: https://developer.android.com/

---

**Total setup time:** ~15-20 minutes (first time)  
**Build time:** ~5-10 minutes (first build), ~30-60 seconds (subsequent)  
**No Android Studio required!** 🎉

---

**Questions? Check the troubleshooting section above or create an issue on GitHub!**
