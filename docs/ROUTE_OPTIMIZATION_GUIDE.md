# 🗺️ Route Optimization Guide

## Overview

The Route Optimization feature calculates the most efficient delivery route for all invoices using the **Traveling Salesman Problem (TSP)** algorithm. It integrates with Google Maps to display the optimized route and provides turn-by-turn navigation.

## Features

✅ **Automatic Route Optimization** - Uses Nearest Neighbor algorithm for TSP  
✅ **Google Maps Integration** - Visual route display with markers and polylines  
✅ **Distance Calculation** - Shows total route distance in km  
✅ **Travel Time Estimation** - Estimates delivery time based on average speed  
✅ **Turn-by-Turn Navigation** - Launches Google Maps with all waypoints  
✅ **Location-Based Starting Point** - Uses current location or default warehouse  
✅ **Reorder Functionality** - Apply optimized order to invoice list  

## Setup Instructions

### 1. Get Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing project
3. Enable **Maps SDK for Android** API
4. Enable **Geocoding API** (for address-to-coordinates conversion)
5. Create credentials → API Key
6. (Optional) Restrict API key to Android apps with your package name

### 2. Add API Key to AndroidManifest.xml

Open `android/app/src/main/AndroidManifest.xml` and replace the placeholder:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_ACTUAL_API_KEY_HERE" />
```

**⚠️ Important:** Never commit your API key to public repositories. Consider using:
- Environment variables
- `local.properties` file (gitignored)
- Build config fields

### 3. Grant Location Permissions

The app requests location permissions at runtime. User must grant:
- `ACCESS_FINE_LOCATION` - For precise current location
- `ACCESS_COARSE_LOCATION` - For approximate location

If denied, the app uses a default warehouse location.

## How It Works

### Algorithm: Nearest Neighbor TSP

The route optimizer uses a greedy **Nearest Neighbor** approach:

1. **Start Point**: Current device location or default warehouse coordinates
2. **Find Nearest**: Identify the closest unvisited delivery address
3. **Add to Route**: Move to that address and mark as visited
4. **Repeat**: Continue until all addresses visited
5. **Calculate Distance**: Sum all leg distances using Haversine formula

**Time Complexity**: O(n²) where n = number of deliveries  
**Space Complexity**: O(n)

### Distance Calculation

Uses the **Haversine formula** for great-circle distance between GPS coordinates:

```
a = sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlon/2)
c = 2 × atan2(√a, √(1−a))
distance = R × c  (where R = Earth's radius = 6371 km)
```

### Geocoding

Addresses from invoices are converted to GPS coordinates using Android's `Geocoder`:

```java
List<Address> addresses = geocoder.getFromLocationName(address, 1);
double lat = addresses.get(0).getLatitude();
double lng = addresses.get(0).getLongitude();
```

**Note:** Requires internet connection. Invalid addresses are skipped.

## Usage

### Basic Workflow

1. **Add Invoices** with valid delivery addresses
2. **Tap "Optimize Delivery Route"** button on main screen
3. **Grant Location Permission** when prompted (optional)
4. **View Optimized Route** on Google Maps
   - Green marker = Starting point
   - Red markers = Delivery stops (numbered)
   - Blue line = Route path
5. **Start Navigation** to launch Google Maps with waypoints
6. **Apply Order** (optional) to reorder invoice list

### Route Map Screen

**Components:**
- **Map View**: Shows all delivery markers and route polyline
- **Route Summary**: Displays total stops, distance, and estimated time
- **Start Navigation Button**: Opens Google Maps with turn-by-turn directions
- **Apply Order Button**: Saves optimized sequence to database

### Tips for Best Results

✅ **Use Complete Addresses**: Include street, city, state, ZIP  
✅ **Verify Addresses**: Check for typos before optimization  
✅ **Update Location Services**: Enable GPS for accurate starting point  
✅ **Check Internet**: Geocoding requires network connection  
✅ **Review Route**: Manually adjust if needed before navigation  

## Code Architecture

### Key Classes

**RouteOptimizer.java**
- Core TSP algorithm implementation
- Geocoding and distance calculations
- Route optimization logic

**RouteMapActivity.java**
- Google Maps integration
- User interface for route display
- Navigation launcher

**RoutePoint (inner class)**
- Represents single delivery stop
- Contains: Invoice, GPS coords, order index

**OptimizedRoute (inner class)**
- Complete route information
- Contains: Ordered points, total distance, summary

### Key Methods

```java
// Main optimization entry point
OptimizedRoute optimizeRoute(List<Invoice> invoices, 
                              double startLat, 
                              double startLng)

// Convert addresses to coordinates
List<RoutePoint> geocodeAddresses(List<Invoice> invoices)

// TSP algorithm implementation
List<RoutePoint> nearestNeighborTSP(List<RoutePoint> points, 
                                     double startLat, 
                                     double startLng)

// Calculate distance between two points
double calculateDistance(double lat1, double lng1, 
                         double lat2, double lng2)
```

## Configuration

### Default Starting Location

Edit `RouteMapActivity.java` to set your warehouse/office location:

```java
private void optimizeRouteFromDefaultLocation() {
    // Update with your actual starting point
    double defaultLat = 41.8781; // Your latitude
    double defaultLng = -87.6298; // Your longitude
    optimizeAndDisplayRoute(defaultLat, defaultLng);
}
```

### Average Delivery Speed

Adjust estimated travel time calculation in `RouteOptimizer.java`:

```java
public static String estimateTravelTime(double kilometers) {
    final double AVG_SPEED_KMH = 40; // Change this value
    // ... rest of method
}
```

Recommended values:
- Urban areas: 25-35 km/h
- Suburban: 40-50 km/h  
- Rural: 50-70 km/h

## Troubleshooting

### "Could not geocode any addresses"
**Cause**: No valid addresses found  
**Fix**: Verify invoices have complete addresses (not "No address found")

### "Location permission required"
**Cause**: User denied location access  
**Fix**: Grant permission in Settings → Apps → Mobile Invoice OCR → Permissions

### Map shows blank/doesn't load
**Cause**: Invalid or missing API key  
**Fix**: Verify API key in AndroidManifest.xml and check billing enabled

### Addresses not geocoding
**Cause**: No internet connection or invalid addresses  
**Fix**: Check network connection, verify address format

### "Google Maps not installed"
**Cause**: Google Maps app not available  
**Fix**: Navigation opens in browser as fallback

## Performance

### Scalability

- **10 stops**: < 1 second
- **50 stops**: 2-3 seconds
- **100 stops**: 5-8 seconds
- **500+ stops**: Consider more advanced algorithms

For large route sets (>100 stops), consider:
- 2-opt optimization for route improvement
- Genetic algorithms for better solutions
- Cloud-based route optimization APIs

### API Quota Limits

**Google Maps API Free Tier:**
- Geocoding: 40,000 requests/month
- Maps SDK: Unlimited with valid API key

**Optimization:**
- Cache geocoded coordinates in database
- Batch geocoding requests
- Add rate limiting if needed

## Future Enhancements

🔮 **Planned Features:**
- [ ] 2-opt route improvement after initial solution
- [ ] Time windows for delivery constraints
- [ ] Vehicle capacity constraints
- [ ] Multiple vehicle routing
- [ ] Real-time traffic integration
- [ ] Save favorite routes
- [ ] Export route to PDF/CSV
- [ ] Route replay/history

## API Reference

### RouteOptimizer

```java
// Constructor
public RouteOptimizer(Context context)

// Optimize route
public OptimizedRoute optimizeRoute(
    List<Invoice> invoices,
    double startLatitude,
    double startLongitude
)

// Utility methods
public static double calculateDistance(double lat1, double lng1, 
                                        double lat2, double lng2)
public static String formatDistance(double kilometers)
public static String estimateTravelTime(double kilometers)
```

### RoutePoint

```java
public class RoutePoint {
    public Invoice invoice;
    public double latitude;
    public double longitude;
    public String formattedAddress;
    public int orderIndex;
}
```

### OptimizedRoute

```java
public class OptimizedRoute {
    public List<RoutePoint> orderedPoints;
    public double totalDistance; // kilometers
    public int totalStops;
    public String summary;
}
```

## Support

For issues or questions:
- Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
- Open issue on GitHub
- Review Google Maps SDK documentation

---

**Last Updated:** January 15, 2026  
**Version:** 1.0  
**Status:** ✅ Production Ready
