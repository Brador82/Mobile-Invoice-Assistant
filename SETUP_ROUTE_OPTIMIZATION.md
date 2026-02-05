# ✅ Route Optimization Setup Checklist

Complete these steps to activate the route optimization feature:

## Required Steps

### ☐ 1. Get Google Maps API Key (5 minutes)

1. [ ] Go to [Google Cloud Console](https://console.cloud.google.com/)
2. [ ] Create new project or select existing
3. [ ] Navigate to "APIs & Services" → "Library"
4. [ ] Search and **enable** "Maps SDK for Android"
5. [ ] Search and **enable** "Geocoding API"
6. [ ] Go to "Credentials" → "Create Credentials" → "API Key"
7. [ ] Copy your API key (starts with `AIza...`)

**Estimated Cost:** FREE (40,000 geocoding requests/month free tier)

---

### ☐ 2. Add API Key to AndroidManifest.xml (1 minute)

1. [ ] Open file: `android/app/src/main/AndroidManifest.xml`
2. [ ] Find this line (around line 60):
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_GOOGLE_MAPS_API_KEY_HERE" />
   ```
3. [ ] Replace `YOUR_GOOGLE_MAPS_API_KEY_HERE` with your actual API key
4. [ ] Save file

**Example:**
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyBXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" />
```

⚠️ **Security Note:** Never commit your API key to public repositories!

---

### ☐ 3. Configure Default Warehouse Location (2 minutes)

1. [ ] Open file: `android/app/src/main/java/com/mobileinvoice/ocr/RouteMapActivity.java`
2. [ ] Find method `optimizeRouteFromDefaultLocation()` (around line 150)
3. [ ] Update coordinates to your actual warehouse/office:
   ```java
   private void optimizeRouteFromDefaultLocation() {
       double defaultLat = 41.8781; // ← Your warehouse latitude
       double defaultLng = -87.6298; // ← Your warehouse longitude
       optimizeAndDisplayRoute(defaultLat, defaultLng);
   }
   ```

**How to find your coordinates:**
- Go to Google Maps
- Right-click your warehouse location
- Click on coordinates to copy them
- First number = Latitude, Second number = Longitude

---

### ☐ 4. Build and Install (2 minutes)

```bash
cd android
.\gradlew assembleDebug
adb install app\build\outputs\apk\debug\app-debug.apk
```

Or use Android Studio:
1. [ ] Open Android Studio
2. [ ] Click "Sync Gradle"
3. [ ] Click "Run" button

---

### ☐ 5. Test the Feature (5 minutes)

1. [ ] Launch the app on device
2. [ ] Add at least 2-3 invoices with **valid addresses**
   - Example: "123 Main St, Chicago, IL 60601"
3. [ ] Tap **"🚚 Optimize Delivery Route"** button (orange)
4. [ ] Grant **location permission** when prompted
5. [ ] Verify map displays correctly:
   - [ ] Green marker at start point
   - [ ] Red markers at delivery locations (numbered)
   - [ ] Blue route line connecting them
   - [ ] Route summary shows distance and time
6. [ ] Tap **"Start Navigation"**
   - [ ] Google Maps opens with all waypoints

**Expected Result:** Map shows optimized route with numbered stops

---

## Optional Steps (Recommended)

### ☐ 6. Secure Your API Key (10 minutes)

**For Production Use:**

1. [ ] Restrict API key in Google Cloud Console:
   - Go to Credentials → Your API Key
   - Under "Application restrictions":
     - Select "Android apps"
     - Add package name: `com.mobileinvoice.ocr`
     - Add SHA-1 fingerprint from your keystore

2. [ ] Get SHA-1 fingerprint:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```

3. [ ] Use environment variables (recommended):
   - Move API key to `android/local.properties`
   - Update build.gradle to read from properties
   - Never commit local.properties to Git

**See:** [docs/GOOGLE_MAPS_SETUP.md](docs/GOOGLE_MAPS_SETUP.md) for detailed security setup

---

### ☐ 7. Adjust Average Delivery Speed (1 minute)

**If needed**, customize travel time estimates:

1. [ ] Open: `android/app/src/main/java/com/mobileinvoice/ocr/RouteOptimizer.java`
2. [ ] Find method `estimateTravelTime()` (around line 240)
3. [ ] Adjust speed constant:
   ```java
   final double AVG_SPEED_KMH = 40; // Change this value
   ```

**Recommended Values:**
- Urban areas: 25-35 km/h
- Suburban: 40-50 km/h
- Rural: 50-70 km/h

---

## Troubleshooting

### Map shows "For development purposes only"
✗ **Problem:** API key not configured  
✓ **Solution:** Complete Step 2 above

### "Could not geocode any addresses"
✗ **Problem:** No internet or invalid addresses  
✓ **Solution:** Check network, verify invoice addresses are complete

### Map is blank/gray
✗ **Problem:** APIs not enabled in Cloud Console  
✓ **Solution:** Enable "Maps SDK for Android" and "Geocoding API"

### Location permission denied
✗ **Problem:** User denied location access  
✓ **Solution:** This is OK! App will use default warehouse location

---

## Quick Links

📖 **Documentation:**
- [Complete Route Optimization Guide](docs/ROUTE_OPTIMIZATION_GUIDE.md)
- [Google Maps Setup Guide](docs/GOOGLE_MAPS_SETUP.md)
- [Quick Reference Card](ROUTE_QUICKREF.md)
- [Implementation Summary](ROUTE_OPTIMIZATION_IMPLEMENTATION.md)

🔧 **Files to Edit:**
- `android/app/src/main/AndroidManifest.xml` - Add API key
- `android/app/src/main/java/com/mobileinvoice/ocr/RouteMapActivity.java` - Set warehouse location

---

## Status Tracking

- [ ] Step 1: Got Google Maps API key
- [ ] Step 2: Added API key to AndroidManifest.xml
- [ ] Step 3: Configured warehouse coordinates
- [ ] Step 4: Built and installed app
- [ ] Step 5: Tested route optimization successfully
- [ ] Step 6: (Optional) Secured API key
- [ ] Step 7: (Optional) Adjusted delivery speed

**When all required steps complete:** ✅ **Route optimization is ACTIVE!**

---

**Need Help?** 
- Check [Troubleshooting](docs/ROUTE_OPTIMIZATION_GUIDE.md#troubleshooting)
- Review [Google Maps Setup](docs/GOOGLE_MAPS_SETUP.md)
- Open GitHub issue

**Estimated Total Setup Time:** 15-20 minutes (first time)
