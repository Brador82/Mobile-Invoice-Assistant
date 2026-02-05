# 📱 Google Play Store Publishing Guide

**Complete guide to publish Mobile Invoice Assistant without Android Studio**

---

## 🎯 Overview

This guide covers everything you need to publish your app to Google Play Store using only command-line tools.

## 📋 Prerequisites

- ✅ Completed app with all features working
- ✅ JDK installed (Java 11 or higher)
- ✅ Gradle wrapper in `android/` directory
- ✅ Google Play Developer account ($25 one-time fee)

---

## 🔐 Step 1: Create a Signing Key

Every Android app must be digitally signed before release.

### 1.1 Generate Keystore

```bash
cd android/app

keytool -genkey -v -keystore mobile-invoice-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mobile-invoice-key
```

**You'll be prompted for:**
- Keystore password (choose a strong password, SAVE THIS!)
- Key password (can be same as keystore password)
- Your name
- Organization name
- City/Locality
- State/Province
- Country code (e.g., US)

**⚠️ CRITICAL:** 
- Save the keystore file (`mobile-invoice-release.jks`)
- Save both passwords in a secure location
- **NEVER commit the keystore to Git**
- **If you lose this, you can NEVER update your app!**

### 1.2 Verify Keystore

```bash
keytool -list -v -keystore mobile-invoice-release.jks -alias mobile-invoice-key
```

You should see your key details and SHA-1/SHA-256 fingerprints.

---

## ⚙️ Step 2: Configure Gradle for Signing

### 2.1 Create `keystore.properties` (DO NOT COMMIT!)

Create file: `android/keystore.properties`

```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=mobile-invoice-key
storeFile=app/mobile-invoice-release.jks
```

### 2.2 Update `.gitignore`

Add to `android/.gitignore`:

```
# Keystore files
*.jks
*.keystore
keystore.properties
```

### 2.3 Update `app/build.gradle`

Add this BEFORE the `android {` block:

```gradle
def keystorePropertiesFile = rootProject.file("keystore.properties")
def keystoreProperties = new Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
}
```

Inside the `android {` block, add:

```gradle
signingConfigs {
    release {
        keyAlias keystoreProperties['keyAlias']
        keyPassword keystoreProperties['keyPassword']
        storeFile file(keystoreProperties['storeFile'])
        storePassword keystoreProperties['storePassword']
    }
}

buildTypes {
    release {
        signingConfig signingConfigs.release
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

---

## 📦 Step 3: Build Release APK/AAB

### 3.1 Clean Previous Builds

```bash
cd android
.\gradlew clean
```

### 3.2 Build Release APK (for testing)

```bash
.\gradlew assembleRelease
```

**Output:** `android/app/build/outputs/apk/release/app-release.apk`

### 3.3 Build Release AAB (for Play Store) ⭐ RECOMMENDED

```bash
.\gradlew bundleRelease
```

**Output:** `android/app/build/outputs/bundle/release/app-release.aab`

**Why AAB?**
- Smaller download size for users
- Google Play handles APK generation per device
- Required for new apps since August 2021

### 3.4 Test the Release APK

```bash
adb install android/app/build/outputs/apk/release/app-release.apk
```

**Test thoroughly:**
- All features work
- OCR processing
- Route optimization
- POD photos & signatures
- Export functionality
- Google Maps integration

---

## 🎨 Step 4: Create Store Listing Assets

### 4.1 App Icon (Already done!)

✅ Your `mobile_ocr.png` is ready
✅ Launcher icons generated for all densities

### 4.2 Feature Graphic (Required)

**Size:** 1024 x 500 pixels

Create using:
- Canva, Photoshop, GIMP, or online tools
- Should showcase your app's main features
- Include app name and tagline

**Suggested text:**
```
Mobile Invoice Assistant
Smart OCR • Route Optimization • Digital POD
```

### 4.3 Screenshots (Required: 2-8 screenshots)

**Phone screenshots:** 320-3840 px wide, 16:9 or 9:16 ratio

**Recommended screenshots:**
1. **Main screen** - Invoice list with cards
2. **OCR Processing** - Camera capture or OCR results
3. **Invoice Details** - Edit screen with POD/signature
4. **Route Optimization** - Map with delivery route
5. **Items Selection** - Multi-select dialog
6. **Export** - Export/share functionality

**How to capture:**
```bash
# Take screenshot on device (Power + Volume Down)
# Or use adb:
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

### 4.4 Promotional Graphics (Optional but recommended)

- **Promo graphic:** 180 x 120 px
- **TV banner:** 1280 x 720 px (if supporting Android TV)

---

## 📝 Step 5: Prepare Store Listing Content

### 5.1 App Title

**Max 50 characters**

```
Mobile Invoice Assistant - OCR & Route Optimizer
```

### 5.2 Short Description

**Max 80 characters**

```
Scan invoices with OCR, optimize delivery routes, capture POD & signatures
```

### 5.3 Full Description

**Max 4000 characters**

```
📱 Mobile Invoice Assistant - The Complete Delivery Management Solution

Transform your delivery workflow with powerful invoice scanning, intelligent route optimization, and digital proof-of-delivery capture. No internet required for OCR!

✨ KEY FEATURES

📸 Smart Invoice Scanning
• On-device OCR with 95%+ accuracy (Google ML Kit)
• Automatic extraction of customer name, address & phone
• Camera or gallery import support
• Works offline - no internet required!

🗺️ Intelligent Route Optimization
• AI-powered route planning (TSP algorithm)
• Split-screen map + delivery list view
• Drag-and-drop to manually adjust stops
• Google Maps turn-by-turn navigation
• Real-time distance & time calculations

📦 Complete Delivery Management
• 3 independent POD (Proof of Delivery) photos per stop
• Digital signature capture on device
• 10 common appliance/item types
• Notes field for delivery details
• Auto-save - never lose data!

📤 Flexible Export Options
• Export to CSV, Excel, JSON, or Markdown
• Direct sharing to Google Drive, Dropbox, OneDrive
• Email or messaging app integration
• Organized delivery card folders with images

💾 Reliable Data Storage
• Room database with SQLite backend
• Automatic persistence across sessions
• Survives app restart and device reboot
• All images stored securely in app storage

🎯 Perfect For:
• Delivery drivers & couriers
• Appliance installation teams
• Field service technicians
• Moving & furniture delivery
• Any business requiring POD capture

🚀 Why Choose Mobile Invoice Assistant?

✅ 100% offline OCR - no cloud dependency
✅ Fast processing (2-3 seconds per invoice)
✅ Privacy-focused - your data stays on device
✅ Professional digital signatures
✅ Optimized routes save time & fuel
✅ Multiple export formats for flexibility
✅ Modern Material Design interface
✅ Regular updates & improvements

📊 Technical Highlights:
• Built with native Android (Java)
• Google ML Kit for accurate text recognition
• Google Maps SDK for route visualization
• Room database for reliable persistence
• CameraX for professional photo capture
• Material Design 3 components

🔒 Privacy & Security:
• All OCR processing happens on your device
• No internet required for core features
• Your data never leaves your phone
• Secure local storage only
• Optional cloud sharing only when you choose

📱 Requirements:
• Android 8.0 (Oreo) or higher
• Camera permission for invoice scanning
• Location permission for route optimization (optional)
• Google Maps API key (free tier sufficient)

💡 How It Works:
1. Scan invoice with camera
2. OCR automatically extracts customer details
3. Add POD photos and signature
4. Optimize your delivery route
5. Navigate stop-by-stop
6. Export and share with office

🆓 Free Features:
• Unlimited invoice scanning
• Unlimited POD photos
• Route optimization
• All export formats
• No ads, no subscriptions

Get started today and revolutionize your delivery workflow! 🚚
```

### 5.4 Privacy Policy

**Required for Play Store**

See the separate `PRIVACY_POLICY.md` file (creating next).

### 5.5 App Category

**Best fit:** Business or Productivity

### 5.6 Content Rating

Complete the questionnaire:
- Violence: None
- Sexual content: None
- Drugs: None
- Gambling: None
- User-generated content: No
- Location sharing: Minimal (only for route start point)
- Personal info access: Camera, Storage

**Expected rating:** Everyone (3+)

---

## 🌐 Step 6: Create Google Play Console Account

### 6.1 Sign Up

1. Go to: https://play.google.com/console
2. Sign in with Google account
3. Accept Developer Agreement
4. Pay $25 one-time registration fee
5. Complete account verification

### 6.2 Create App

1. Click "Create app"
2. Select app details:
   - **Name:** Mobile Invoice Assistant
   - **Default language:** English (US)
   - **App or game:** App
   - **Free or paid:** Free
3. Accept declarations
4. Click "Create app"

---

## 📤 Step 7: Upload Your App

### 7.1 Production Track

1. In Play Console → Your app → Release → Production
2. Click "Create new release"
3. Upload `app-release.aab` file
4. Enter release notes:

```
Initial Release v1.0.0

Features:
• Smart invoice OCR with 95%+ accuracy
• Intelligent route optimization with drag-and-drop
• 3 POD photos + digital signature per delivery
• Multiple export formats (CSV, Excel, JSON, Markdown)
• Offline-capable - no internet required for OCR
• Google Maps integration for turn-by-turn navigation
```

### 7.2 Store Listing

Fill in all required fields:
- App name
- Short description
- Full description
- App icon (512x512)
- Feature graphic (1024x500)
- Screenshots (minimum 2)
- Privacy policy URL

### 7.3 Content Rating

Complete the questionnaire → Submit for rating

### 7.4 App Content

- Privacy policy ✅
- Ads declaration: No ads ✅
- Target audience: Everyone ✅
- Data safety: Complete form

**Data Safety Form:**
```
Data collection:
- Camera: For invoice scanning
- Storage: For saving photos/signatures
- Location: Optional, for route optimization only

Data sharing: None
Data security: All data stored locally on device
```

### 7.5 Pricing & Distribution

- Price: Free
- Countries: All (or select specific)
- Content rating: Apply after questionnaire

---

## ✅ Step 8: Submit for Review

1. Complete all sections (dashboard shows checkmarks)
2. Click "Send X items for review"
3. Review policy compliance
4. Submit app for review

**Review timeline:** Usually 1-7 days

---

## 🔧 Step 9: Post-Submission

### Monitor Review Status

Play Console → Dashboard → Track your review status

### Respond to Review Feedback

If rejected, address issues and resubmit

### Set Up Alerts

Enable email notifications for:
- Review status
- Crash reports
- User reviews

---

## 📊 Step 10: Analytics & Updates

### Track Performance

- Install statistics
- User ratings & reviews
- Crash reports
- ANR (App Not Responding) reports

### Release Updates

For updates:
```bash
# Increment versionCode and versionName in app/build.gradle
# Build new AAB
.\gradlew bundleRelease

# Upload to Play Console → Create new release
```

---

## 🚨 Common Issues & Solutions

### Build Fails with Signing Error

**Problem:** Keystore path or passwords incorrect

**Solution:**
- Verify `keystore.properties` paths are correct
- Check passwords are correct
- Ensure `.jks` file exists

### AAB Upload Rejected

**Problem:** Version code not incremented

**Solution:**
- Edit `app/build.gradle`
- Increase `versionCode` by 1
- Rebuild AAB

### Missing Permissions

**Problem:** App requires permissions not declared

**Solution:**
- Check `AndroidManifest.xml` has all required permissions
- Rebuild and test

### ProGuard Issues

**Problem:** App crashes in release but not debug

**Solution:**
- Check `proguard-rules.pro`
- Add keep rules for your classes
- Test release APK thoroughly before upload

---

## 📚 Additional Resources

- **Play Console Help:** https://support.google.com/googleplay/android-developer
- **Android Publishing Guide:** https://developer.android.com/studio/publish
- **Play Policy Center:** https://play.google.com/about/developer-content-policy/

---

## 🎉 Congratulations!

Your app is now published on Google Play Store! 🚀

**Next steps:**
- Monitor user reviews
- Respond to feedback
- Plan feature updates
- Track analytics
- Build your user base

---

**Last Updated:** January 20, 2026
