# Google Maps API Setup - Quick Guide

## 🚀 5-Minute Setup

### Step 1: Get Your API Key

1. Visit [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Click "APIs & Services" → "Library"
4. Search and enable:
   - **Maps SDK for Android**
   - **Geocoding API**
5. Go to "Credentials" → "Create Credentials" → "API Key"
6. Copy your new API key

### Step 2: Add Key to AndroidManifest.xml

Open: `android/app/src/main/AndroidManifest.xml`

Find this line:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY_HERE" />
```

Replace `YOUR_GOOGLE_MAPS_API_KEY_HERE` with your actual API key:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyABCDEFGHIJKLMNOPQRSTUVWXYZ1234567" />
```

### Step 3: (Optional) Restrict API Key

For security, restrict your API key:

1. In Google Cloud Console → "Credentials"
2. Click on your API key
3. Under "Application restrictions":
   - Choose "Android apps"
   - Add package name: `com.mobileinvoice.ocr`
   - Add SHA-1 fingerprint (from your keystore)

To get SHA-1 fingerprint:
```bash
# Debug keystore (development)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Release keystore (production)
keytool -list -v -keystore /path/to/your-release-key.jks -alias your-alias
```

### Step 4: Build and Run

```bash
cd android
.\gradlew assembleDebug
adb install app\build\outputs\apk\debug\app-debug.apk
```

## 💡 Free Tier Limits

**Monthly Free Usage:**
- Geocoding API: 40,000 requests
- Maps SDK for Android: Unlimited (with valid API key)

**Cost After Free Tier:**
- Geocoding: $5 per 1,000 requests
- No charge for Maps SDK display

## ⚠️ Important Security Notes

### Never Commit API Keys to Git

❌ **Don't do this:**
```xml
<!-- In AndroidManifest.xml -->
<meta-data android:value="AIzaSyA..." />
```
Then commit to public repo!

✅ **Do this instead:**

**Option 1: Use local.properties (Recommended)**

1. Add to `android/local.properties`:
```properties
MAPS_API_KEY=AIzaSyABCDEFGHIJKLMNOPQRSTUVWXYZ1234567
```

2. Update `android/app/build.gradle`:
```gradle
android {
    defaultConfig {
        // ...
        Properties properties = new Properties()
        properties.load(project.rootProject.file("local.properties").newDataInputStream())
        manifestPlaceholders = [MAPS_API_KEY: properties.getProperty("MAPS_API_KEY", "")]
    }
}
```

3. Update AndroidManifest.xml:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

4. Add to `.gitignore`:
```
android/local.properties
```

**Option 2: Environment Variables**

```bash
# Set environment variable
export MAPS_API_KEY="AIzaSyA..."

# In build.gradle, read from environment
manifestPlaceholders = [MAPS_API_KEY: System.getenv("MAPS_API_KEY") ?: ""]
```

## 🧪 Test Your Setup

1. Launch the app
2. Add at least 2 invoices with valid addresses
3. Tap "Optimize Delivery Route"
4. Grant location permission
5. You should see:
   - Map loads successfully
   - Markers appear at delivery locations
   - Blue route line connects all points
   - Route summary shows distance and time

## 🐛 Troubleshooting

### Map Shows "For development purposes only"
**Problem:** API key not properly configured  
**Solution:** Verify key in AndroidManifest.xml, check API is enabled

### "Could not geocode any addresses"
**Problem:** Geocoding API not enabled or no internet  
**Solution:** Enable Geocoding API in Cloud Console, check network

### Blank/Gray map
**Problem:** Maps SDK for Android not enabled  
**Solution:** Enable in APIs & Services → Library

### "Authorization failure" error
**Problem:** API key restrictions too strict  
**Solution:** Remove restrictions or add correct package name + SHA-1

## 📚 Additional Resources

- [Google Maps Platform Documentation](https://developers.google.com/maps/documentation)
- [Maps SDK for Android Guide](https://developers.google.com/maps/documentation/android-sdk)
- [Geocoding API Reference](https://developers.google.com/maps/documentation/geocoding)
- [API Key Best Practices](https://developers.google.com/maps/api-security-best-practices)

## 💰 Cost Estimation

**Example Usage:**
- 100 deliveries/day = 100 geocoding requests/day
- 30 days = 3,000 requests/month
- **Cost:** $0 (well within free tier of 40,000/month)

**If you exceed free tier:**
- Next 100,000 requests: $0.50 per 1,000 = $50
- Consider caching coordinates in database to reduce API calls

---

**Need Help?** Check [ROUTE_OPTIMIZATION_GUIDE.md](ROUTE_OPTIMIZATION_GUIDE.md) for detailed documentation.
