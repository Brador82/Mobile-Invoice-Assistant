package com.mobileinvoice.ocr;

import android.location.Address;
import android.location.Geocoder;
import android.content.Context;
import android.util.Log;
import com.mobileinvoice.ocr.database.Invoice;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Route Optimizer using Nearest Neighbor algorithm for TSP (Traveling Salesman Problem)
 * Calculates the most efficient delivery route based on geographic proximity
 */
public class RouteOptimizer {
    private static final String TAG = "RouteOptimizer";
    private Context context;
    private Geocoder geocoder;
    
    public static class RoutePoint {
        public Invoice invoice;
        public double latitude;
        public double longitude;
        public String formattedAddress;
        public int orderIndex;
        
        public RoutePoint(Invoice invoice, double lat, double lng, String address) {
            this.invoice = invoice;
            this.latitude = lat;
            this.longitude = lng;
            this.formattedAddress = address;
        }
    }
    
    public static class OptimizedRoute {
        public List<RoutePoint> orderedPoints;
        public double totalDistance; // in kilometers
        public int totalStops;
        public String summary;
        
        public OptimizedRoute() {
            orderedPoints = new ArrayList<>();
            totalDistance = 0;
            totalStops = 0;
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
     * @return OptimizedRoute with ordered delivery points
     */
    public OptimizedRoute optimizeRoute(List<Invoice> invoices, double startLatitude, double startLongitude) {
        Log.d(TAG, "Starting route optimization for " + invoices.size() + " invoices");
        
        OptimizedRoute route = new OptimizedRoute();
        
        // Step 1: Geocode all addresses
        List<RoutePoint> points = geocodeAddresses(invoices);
        
        if (points.isEmpty()) {
            Log.w(TAG, "No valid addresses found for route optimization");
            return route;
        }
        
        Log.d(TAG, "Successfully geocoded " + points.size() + " addresses");
        
        // Step 2: Apply Nearest Neighbor algorithm for TSP
        List<RoutePoint> optimizedPoints = nearestNeighborTSP(points, startLatitude, startLongitude);
        
        // Step 3: Calculate total distance
        double totalDist = calculateTotalDistance(optimizedPoints, startLatitude, startLongitude);
        
        // Step 4: Build result
        route.orderedPoints = optimizedPoints;
        route.totalDistance = totalDist;
        route.totalStops = optimizedPoints.size();
        route.summary = String.format("Total: %.1f km | %d stops", totalDist, optimizedPoints.size());
        
        Log.d(TAG, "Route optimization complete: " + route.summary);
        
        return route;
    }
    
    /**
     * Geocode all invoice addresses to lat/lng coordinates
     */
    private List<RoutePoint> geocodeAddresses(List<Invoice> invoices) {
        List<RoutePoint> points = new ArrayList<>();
        
        for (Invoice invoice : invoices) {
            String address = invoice.getAddress();
            
            if (address == null || address.trim().isEmpty() || 
                address.equalsIgnoreCase("No address found")) {
                Log.w(TAG, "Skipping invoice " + invoice.getInvoiceNumber() + " - no valid address");
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
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed for: " + address, e);
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
     * @return distance in kilometers
     */
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS = 6371; // km
        
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
    public static String formatDistance(double kilometers) {
        if (kilometers < 1) {
            return String.format("%.0f m", kilometers * 1000);
        } else {
            return String.format("%.1f km", kilometers);
        }
    }
    
    /**
     * Estimate travel time (assuming average speed)
     */
    public static String estimateTravelTime(double kilometers) {
        final double AVG_SPEED_KMH = 40; // Average delivery speed
        double hours = kilometers / AVG_SPEED_KMH;
        int minutes = (int) (hours * 60);
        
        if (minutes < 60) {
            return minutes + " min";
        } else {
            int hrs = minutes / 60;
            int mins = minutes % 60;
            return hrs + "h " + mins + "m";
        }
    }
}
