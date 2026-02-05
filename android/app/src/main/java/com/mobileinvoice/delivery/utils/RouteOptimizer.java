package com.mobileinvoice.delivery.utils;

import com.mobileinvoice.delivery.data.entities.Delivery;
import java.util.ArrayList;
import java.util.List;

/**
 * Smart Route Optimization Engine
 * 
 * Algorithms:
 * - Nearest Neighbor (fast, good for small routes)
 * - Time Window Priority (respects delivery windows)
 * - Priority-First (urgent deliveries first)
 * - Geographic Clustering (groups nearby deliveries)
 */
public class RouteOptimizer {
    
    /**
     * Optimize route using Nearest Neighbor algorithm
     * Starts from current location and always goes to nearest unvisited delivery
     */
    public static List<Delivery> optimizeByNearestNeighbor(List<Delivery> deliveries, 
                                                            double startLat, 
                                                            double startLng) {
        if (deliveries == null || deliveries.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Delivery> optimized = new ArrayList<>();
        List<Delivery> remaining = new ArrayList<>(deliveries);
        
        double currentLat = startLat;
        double currentLng = startLng;
        
        while (!remaining.isEmpty()) {
            Delivery nearest = findNearest(remaining, currentLat, currentLng);
            optimized.add(nearest);
            remaining.remove(nearest);
            
            if (nearest.hasCoordinates()) {
                currentLat = nearest.getLatitude();
                currentLng = nearest.getLongitude();
            }
        }
        
        // Update route orders
        for (int i = 0; i < optimized.size(); i++) {
            optimized.get(i).setRouteOrder(i);
        }
        
        return optimized;
    }
    
    /**
     * Optimize route considering time windows
     * Prioritizes deliveries with narrow time windows
     */
    public static List<Delivery> optimizeByTimeWindow(List<Delivery> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Delivery> optimized = new ArrayList<>(deliveries);
        
        // Sort by time window urgency
        optimized.sort((d1, d2) -> {
            // Prioritize deliveries with time windows
            boolean d1HasWindow = d1.getTimeWindowStart() != null;
            boolean d2HasWindow = d2.getTimeWindowStart() != null;
            
            if (d1HasWindow && !d2HasWindow) return -1;
            if (!d1HasWindow && d2HasWindow) return 1;
            
            // Both have time windows - sort by start time
            if (d1HasWindow && d2HasWindow) {
                return d1.getTimeWindowStart().compareTo(d2.getTimeWindowStart());
            }
            
            // Neither has time window - sort by priority
            return Integer.compare(d2.getPriority().getValue(), d1.getPriority().getValue());
        });
        
        // Update route orders
        for (int i = 0; i < optimized.size(); i++) {
            optimized.get(i).setRouteOrder(i);
        }
        
        return optimized;
    }
    
    /**
     * Optimize route by priority
     * Urgent and high-priority deliveries first
     */
    public static List<Delivery> optimizeByPriority(List<Delivery> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Delivery> optimized = new ArrayList<>(deliveries);
        
        // Sort by priority (high to low) then by scheduled date
        optimized.sort((d1, d2) -> {
            int priorityCompare = Integer.compare(
                d2.getPriority().getValue(), 
                d1.getPriority().getValue()
            );
            
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            
            // Same priority - sort by scheduled date
            if (d1.getScheduledDate() != null && d2.getScheduledDate() != null) {
                return d1.getScheduledDate().compareTo(d2.getScheduledDate());
            }
            
            return 0;
        });
        
        // Update route orders
        for (int i = 0; i < optimized.size(); i++) {
            optimized.get(i).setRouteOrder(i);
        }
        
        return optimized;
    }
    
    /**
     * Smart hybrid optimization
     * Combines priority, time windows, and distance
     */
    public static List<Delivery> optimizeSmart(List<Delivery> deliveries, 
                                               double startLat, 
                                               double startLng) {
        if (deliveries == null || deliveries.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Step 1: Separate urgent/high priority deliveries
        List<Delivery> urgent = new ArrayList<>();
        List<Delivery> normal = new ArrayList<>();
        
        for (Delivery delivery : deliveries) {
            if (delivery.getPriority().getValue() >= 3) { // HIGH or URGENT
                urgent.add(delivery);
            } else {
                normal.add(delivery);
            }
        }
        
        // Step 2: Optimize urgent deliveries by time window
        List<Delivery> optimizedUrgent = optimizeByTimeWindow(urgent);
        
        // Step 3: Optimize normal deliveries by nearest neighbor
        List<Delivery> optimizedNormal = optimizeByNearestNeighbor(normal, startLat, startLng);
        
        // Step 4: Combine (urgent first, then normal)
        List<Delivery> result = new ArrayList<>();
        result.addAll(optimizedUrgent);
        result.addAll(optimizedNormal);
        
        // Update route orders
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRouteOrder(i);
        }
        
        return result;
    }
    
    /**
     * Calculate total route distance in kilometers
     */
    public static double calculateTotalDistance(List<Delivery> deliveries, 
                                                double startLat, 
                                                double startLng) {
        if (deliveries == null || deliveries.isEmpty()) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        double currentLat = startLat;
        double currentLng = startLng;
        
        for (Delivery delivery : deliveries) {
            if (delivery.hasCoordinates()) {
                double distance = calculateDistance(
                    currentLat, currentLng,
                    delivery.getLatitude(), delivery.getLongitude()
                );
                totalDistance += distance;
                currentLat = delivery.getLatitude();
                currentLng = delivery.getLongitude();
            }
        }
        
        return totalDistance;
    }
    
    /**
     * Find nearest delivery to given coordinates
     */
    private static Delivery findNearest(List<Delivery> deliveries, double lat, double lng) {
        Delivery nearest = deliveries.get(0);
        double minDistance = Double.MAX_VALUE;
        
        for (Delivery delivery : deliveries) {
            if (delivery.hasCoordinates()) {
                double distance = calculateDistance(
                    lat, lng,
                    delivery.getLatitude(), delivery.getLongitude()
                );
                
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = delivery;
                }
            }
        }
        
        return nearest;
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in km
    }
    
    /**
     * Estimate delivery time based on distance
     * Assumes average speed of 40 km/h in city
     */
    public static int estimateDeliveryTimeMinutes(double distanceKm) {
        final double AVERAGE_SPEED_KMH = 40.0;
        final int STOP_TIME_MINUTES = 5; // Time per stop
        
        double travelTimeMinutes = (distanceKm / AVERAGE_SPEED_KMH) * 60;
        return (int) Math.ceil(travelTimeMinutes) + STOP_TIME_MINUTES;
    }
}
