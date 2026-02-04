package com.mobileinvoice.ocr;

import android.location.Address;
import android.location.Geocoder;
import android.content.Context;
import android.util.Log;
import com.mobileinvoice.ocr.database.Invoice;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Route Optimizer using Nearest Neighbor algorithm for TSP (Traveling Salesman Problem)
 * Calculates the most efficient delivery route based on geographic proximity
 */
public class RouteOptimizer {
    private static final String TAG = "RouteOptimizer";
    private Context context;
    private Geocoder geocoder;
    
    // Priority constants
    public static final int PRIORITY_NORMAL = 0;
    public static final int PRIORITY_FIRST = 1;
    public static final int PRIORITY_LAST = 2;

    public static class RoutePoint {
        public Invoice invoice;
        public double latitude;
        public double longitude;
        public String formattedAddress;
        public int orderIndex;

        // New fields for ETA and stop time
        public int stopTimeMinutes = 30;  // Default stop time (30 min for appliance delivery)
        public double distanceFromPrevious = 0;  // Distance from previous stop (miles)
        public long etaMillis = 0;  // Estimated arrival time in milliseconds
        public int priority = PRIORITY_NORMAL;  // Delivery priority
        public int travelTimeMinutes = 0;  // Travel time from previous stop

        public RoutePoint(Invoice invoice, double lat, double lng, String address) {
            this.invoice = invoice;
            this.latitude = lat;
            this.longitude = lng;
            this.formattedAddress = address;
            // Load stop time from invoice (persisted value)
            this.stopTimeMinutes = invoice.getStopTimeMinutes();
        }

        /**
         * Get formatted ETA string (e.g., "10:30 AM")
         */
        public String getFormattedETA() {
            if (etaMillis == 0) return "N/A";
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return sdf.format(new Date(etaMillis));
        }

        /**
         * Get stop time display string
         */
        public String getStopTimeDisplay() {
            return stopTimeMinutes + " min stop";
        }

        /**
         * Get priority display text
         */
        public String getPriorityText() {
            switch (priority) {
                case PRIORITY_FIRST: return "FIRST";
                case PRIORITY_LAST: return "LAST";
                default: return null;
            }
        }
    }
    
    public static class GeocodingFailure {
        public Invoice invoice;
        public String reason;

        public GeocodingFailure(Invoice invoice, String reason) {
            this.invoice = invoice;
            this.reason = reason;
        }
    }

    public static class OptimizedRoute {
        public List<RoutePoint> orderedPoints;
        public List<GeocodingFailure> failedInvoices;
        public double totalDistance; // in kilometers
        public int totalStops;
        public String summary;
        public long startTimeMillis; // When the route starts
        public long endTimeMillis;   // Estimated end time

        public OptimizedRoute() {
            orderedPoints = new ArrayList<>();
            failedInvoices = new ArrayList<>();
            totalDistance = 0;
            totalStops = 0;
            startTimeMillis = System.currentTimeMillis();
        }

        /**
         * Get total estimated route time in minutes
         */
        public int getTotalTimeMinutes() {
            if (endTimeMillis == 0 || startTimeMillis == 0) return 0;
            return (int) ((endTimeMillis - startTimeMillis) / 60000);
        }

        /**
         * Get formatted end time
         */
        public String getFormattedEndTime() {
            if (endTimeMillis == 0) return "N/A";
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return sdf.format(new Date(endTimeMillis));
        }
    }
    
    public RouteOptimizer(Context context) {
        this.context = context;
        this.geocoder = new Geocoder(context);
    }
    
    /**
     * Geocode all invoice addresses and optimize the route
     * @param invoices List of invoices to deliver
     * @param startLatitude Starting point latitude (e.g., warehouse)
     * @param startLongitude Starting point longitude
     * @return OptimizedRoute with ordered delivery points and any failed invoices
     */
    public OptimizedRoute optimizeRoute(List<Invoice> invoices, double startLatitude, double startLongitude) {
        Log.d(TAG, "Starting route optimization for " + invoices.size() + " invoices");

        OptimizedRoute route = new OptimizedRoute();

        // Step 1: Geocode all addresses (tracks failures)
        List<RoutePoint> points = geocodeAddresses(invoices, route.failedInvoices);

        if (points.isEmpty()) {
            Log.w(TAG, "No valid addresses found for route optimization");
            return route;
        }

        Log.d(TAG, "Successfully geocoded " + points.size() + " addresses, " +
              route.failedInvoices.size() + " failed");

        // Step 2: Apply Nearest Neighbor algorithm for TSP
        List<RoutePoint> optimizedPoints = nearestNeighborTSP(points, startLatitude, startLongitude);

        // Step 3: Calculate total distance
        double totalDist = calculateTotalDistance(optimizedPoints, startLatitude, startLongitude);

        // Step 4: Build result
        route.orderedPoints = optimizedPoints;
        route.totalDistance = totalDist;
        route.totalStops = optimizedPoints.size();
        route.summary = String.format("Total: %.1f mi | %d stops", totalDist, optimizedPoints.size());

        Log.d(TAG, "Route optimization complete: " + route.summary);

        return route;
    }
    
    /**
     * Geocode all invoice addresses to lat/lng coordinates
     * @param invoices List of invoices to geocode
     * @param failures List to populate with failed geocoding attempts
     */
    private List<RoutePoint> geocodeAddresses(List<Invoice> invoices, List<GeocodingFailure> failures) {
        List<RoutePoint> points = new ArrayList<>();

        for (Invoice invoice : invoices) {
            String address = invoice.getAddress();

            if (address == null || address.trim().isEmpty()) {
                Log.w(TAG, "Skipping invoice " + invoice.getInvoiceNumber() + " - no address");
                failures.add(new GeocodingFailure(invoice, "No address provided"));
                continue;
            }

            if (address.equalsIgnoreCase("No address found")) {
                Log.w(TAG, "Skipping invoice " + invoice.getInvoiceNumber() + " - address not found during OCR");
                failures.add(new GeocodingFailure(invoice, "Address not detected during scan"));
                continue;
            }

            try {
                List<Address> addresses = geocoder.getFromLocationName(address, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address location = addresses.get(0);
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    RoutePoint point = new RoutePoint(invoice, lat, lng, address);
                    points.add(point);

                    Log.d(TAG, "Geocoded: " + invoice.getCustomerName() + " -> (" + lat + ", " + lng + ")");
                } else {
                    Log.w(TAG, "No geocoding results for: " + address);
                    failures.add(new GeocodingFailure(invoice, "Address not recognized: " + address));
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed for: " + address, e);
                failures.add(new GeocodingFailure(invoice, "Network error geocoding address"));
            }
        }

        return points;
    }
    
    /**
     * Nearest Neighbor algorithm for TSP
     * Greedy approach: always visit the closest unvisited point
     */
    private List<RoutePoint> nearestNeighborTSP(List<RoutePoint> points, double startLat, double startLng) {
        List<RoutePoint> unvisited = new ArrayList<>(points);
        List<RoutePoint> route = new ArrayList<>();
        
        double currentLat = startLat;
        double currentLng = startLng;
        int order = 1;
        
        while (!unvisited.isEmpty()) {
            // Find nearest unvisited point
            RoutePoint nearest = null;
            double minDistance = Double.MAX_VALUE;
            
            for (RoutePoint point : unvisited) {
                double dist = calculateDistance(currentLat, currentLng, point.latitude, point.longitude);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = point;
                }
            }
            
            if (nearest != null) {
                nearest.orderIndex = order++;
                route.add(nearest);
                unvisited.remove(nearest);
                
                currentLat = nearest.latitude;
                currentLng = nearest.longitude;
            }
        }
        
        return route;
    }
    
    /**
     * Calculate total route distance including return to start
     */
    private double calculateTotalDistance(List<RoutePoint> route, double startLat, double startLng) {
        if (route.isEmpty()) {
            return 0;
        }
        
        double total = 0;
        
        // Distance from start to first point
        total += calculateDistance(startLat, startLng, route.get(0).latitude, route.get(0).longitude);
        
        // Distance between consecutive points
        for (int i = 0; i < route.size() - 1; i++) {
            RoutePoint from = route.get(i);
            RoutePoint to = route.get(i + 1);
            total += calculateDistance(from.latitude, from.longitude, to.latitude, to.longitude);
        }
        
        // Optional: Distance from last point back to start
        // Uncomment if you want round-trip distance
        // RoutePoint last = route.get(route.size() - 1);
        // total += calculateDistance(last.latitude, last.longitude, startLat, startLng);
        
        return total;
    }
    
    /**
     * Calculate distance between two lat/lng points using Haversine formula
     * @return distance in miles
     */
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS = 3959; // miles
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    /**
     * Format distance for display
     */
    public static String formatDistance(double miles) {
        if (miles < 0.1) {
            return String.format("%.0f ft", miles * 5280);
        } else {
            return String.format("%.1f mi", miles);
        }
    }
    
    /**
     * Estimate travel time (assuming average speed)
     */
    public static String estimateTravelTime(double miles) {
        final double AVG_SPEED_MPH = 25; // Average delivery speed in mph
        double hours = miles / AVG_SPEED_MPH;
        int minutes = (int) (hours * 60);

        if (minutes < 60) {
            return minutes + " min";
        } else {
            int hrs = minutes / 60;
            int mins = minutes % 60;
            return hrs + "h " + mins + "m";
        }
    }

    /**
     * Estimate travel time in minutes
     */
    public static int estimateTravelTimeMinutes(double miles) {
        final double AVG_SPEED_MPH = 25; // Average delivery speed in mph
        double hours = miles / AVG_SPEED_MPH;
        return Math.max(1, (int) (hours * 60)); // At least 1 minute
    }

    /**
     * Calculate ETAs for all stops based on start time
     * @param route The optimized route
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param startTimeMillis When the route begins
     */
    public static void calculateETAs(OptimizedRoute route, double startLat, double startLng, long startTimeMillis) {
        if (route.orderedPoints.isEmpty()) return;

        route.startTimeMillis = startTimeMillis;
        long currentTime = startTimeMillis;
        double prevLat = startLat;
        double prevLng = startLng;

        for (RoutePoint point : route.orderedPoints) {
            // Calculate distance and travel time from previous point
            point.distanceFromPrevious = calculateDistance(prevLat, prevLng, point.latitude, point.longitude);
            point.travelTimeMinutes = estimateTravelTimeMinutes(point.distanceFromPrevious);

            // Add travel time to get ETA
            currentTime += point.travelTimeMinutes * 60 * 1000L;
            point.etaMillis = currentTime;

            // Add stop time for the departure to next stop
            currentTime += point.stopTimeMinutes * 60 * 1000L;

            // Update previous location
            prevLat = point.latitude;
            prevLng = point.longitude;
        }

        // Set end time (after last stop)
        route.endTimeMillis = currentTime;

        Log.d(TAG, "ETAs calculated. Route starts at " +
              new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date(startTimeMillis)) +
              ", ends at " + route.getFormattedEndTime());
    }

    /**
     * Recalculate ETAs after stop time change
     * @param route The route with modified stop times
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     */
    public static void recalculateETAs(OptimizedRoute route, double startLat, double startLng) {
        calculateETAs(route, startLat, startLng, route.startTimeMillis);
    }

    /**
     * Move a stop to the first position
     * @param stops List of route points
     * @param point The point to move to first
     */
    public static void makeFirst(List<RoutePoint> stops, RoutePoint point) {
        if (stops.remove(point)) {
            stops.add(0, point);
            point.priority = PRIORITY_FIRST;
            // Re-number all stops
            for (int i = 0; i < stops.size(); i++) {
                stops.get(i).orderIndex = i + 1;
            }
        }
    }

    /**
     * Move a stop to the last position
     * @param stops List of route points
     * @param point The point to move to last
     */
    public static void makeLast(List<RoutePoint> stops, RoutePoint point) {
        if (stops.remove(point)) {
            stops.add(point);
            point.priority = PRIORITY_LAST;
            // Re-number all stops
            for (int i = 0; i < stops.size(); i++) {
                stops.get(i).orderIndex = i + 1;
            }
        }
    }

    /**
     * Clear priority from a stop
     */
    public static void clearPriority(RoutePoint point) {
        point.priority = PRIORITY_NORMAL;
    }

    /**
     * Format time in minutes to readable string
     */
    public static String formatMinutes(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        } else {
            int hrs = minutes / 60;
            int mins = minutes % 60;
            if (mins == 0) {
                return hrs + " hr";
            }
            return hrs + "h " + mins + "m";
        }
    }
}
