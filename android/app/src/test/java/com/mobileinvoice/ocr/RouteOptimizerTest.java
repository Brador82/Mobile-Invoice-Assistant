package com.mobileinvoice.ocr;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Unit tests for RouteOptimizer
 * Tests the distance calculations, formatting, and algorithm correctness
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class RouteOptimizerTest {

    private static final double DELTA = 0.001; // Tolerance for floating point comparisons

    // ==================== calculateDistance (Haversine) Tests ====================

    @Test
    public void testCalculateDistance_sameLocation() {
        double distance = RouteOptimizer.calculateDistance(40.7128, -74.0060, 40.7128, -74.0060);
        assertEquals("Same location should have zero distance", 0.0, distance, DELTA);
    }

    @Test
    public void testCalculateDistance_knownDistance_NewYorkToLosAngeles() {
        // New York: 40.7128, -74.0060
        // Los Angeles: 34.0522, -118.2437
        // Approximate distance: ~3935 km (great circle)
        double distance = RouteOptimizer.calculateDistance(40.7128, -74.0060, 34.0522, -118.2437);
        assertTrue("NY to LA should be approximately 3935 km", distance > 3900 && distance < 4000);
    }

    @Test
    public void testCalculateDistance_knownDistance_LondonToParis() {
        // London: 51.5074, -0.1278
        // Paris: 48.8566, 2.3522
        // Approximate distance: ~344 km
        double distance = RouteOptimizer.calculateDistance(51.5074, -0.1278, 48.8566, 2.3522);
        assertTrue("London to Paris should be approximately 344 km", distance > 340 && distance < 350);
    }

    @Test
    public void testCalculateDistance_shortDistance() {
        // Two points about 1 km apart
        // 0.01 degrees latitude ≈ 1.11 km
        double distance = RouteOptimizer.calculateDistance(40.0, -74.0, 40.009, -74.0);
        assertTrue("Short distance should be approximately 1 km", distance > 0.9 && distance < 1.2);
    }

    @Test
    public void testCalculateDistance_symmetry() {
        double distanceAtoB = RouteOptimizer.calculateDistance(40.7128, -74.0060, 34.0522, -118.2437);
        double distanceBtoA = RouteOptimizer.calculateDistance(34.0522, -118.2437, 40.7128, -74.0060);
        assertEquals("Distance should be symmetric", distanceAtoB, distanceBtoA, DELTA);
    }

    @Test
    public void testCalculateDistance_crossEquator() {
        // Miami: 25.7617, -80.1918
        // São Paulo: -23.5505, -46.6333
        double distance = RouteOptimizer.calculateDistance(25.7617, -80.1918, -23.5505, -46.6333);
        assertTrue("Cross-equator distance should be positive", distance > 0);
        assertTrue("Miami to São Paulo should be approximately 6300 km", distance > 6200 && distance < 6500);
    }

    @Test
    public void testCalculateDistance_crossDateLine() {
        // Tokyo: 35.6762, 139.6503
        // Los Angeles: 34.0522, -118.2437
        double distance = RouteOptimizer.calculateDistance(35.6762, 139.6503, 34.0522, -118.2437);
        assertTrue("Cross-dateline distance should be positive", distance > 0);
        // Should be approximately 8800 km
        assertTrue("Tokyo to LA should be approximately 8800 km", distance > 8700 && distance < 9000);
    }

    @Test
    public void testCalculateDistance_poles() {
        // North Pole to South Pole
        double distance = RouteOptimizer.calculateDistance(90.0, 0.0, -90.0, 0.0);
        // Half the Earth's circumference ≈ 20,000 km
        assertTrue("Pole to pole should be approximately 20000 km", distance > 19900 && distance < 20100);
    }

    @Test
    public void testCalculateDistance_verySmallDistance() {
        // Two points 10 meters apart (approximately)
        double distance = RouteOptimizer.calculateDistance(40.7128, -74.0060, 40.71289, -74.0060);
        assertTrue("Very small distance should be < 0.1 km", distance < 0.1);
    }

    // ==================== formatDistance Tests ====================

    @Test
    public void testFormatDistance_meters() {
        assertEquals("500 m", RouteOptimizer.formatDistance(0.5));
        assertEquals("100 m", RouteOptimizer.formatDistance(0.1));
        assertEquals("750 m", RouteOptimizer.formatDistance(0.75));
    }

    @Test
    public void testFormatDistance_kilometers() {
        assertEquals("1.0 km", RouteOptimizer.formatDistance(1.0));
        assertEquals("5.5 km", RouteOptimizer.formatDistance(5.5));
        assertEquals("10.0 km", RouteOptimizer.formatDistance(10.0));
        assertEquals("100.0 km", RouteOptimizer.formatDistance(100.0));
    }

    @Test
    public void testFormatDistance_boundary() {
        // Just under 1 km
        String result = RouteOptimizer.formatDistance(0.999);
        assertEquals("999 m", result);

        // Exactly 1 km
        result = RouteOptimizer.formatDistance(1.0);
        assertEquals("1.0 km", result);
    }

    @Test
    public void testFormatDistance_zero() {
        assertEquals("0 m", RouteOptimizer.formatDistance(0.0));
    }

    @Test
    public void testFormatDistance_largeDistance() {
        assertEquals("1000.0 km", RouteOptimizer.formatDistance(1000.0));
    }

    @Test
    public void testFormatDistance_decimalPrecision() {
        String result = RouteOptimizer.formatDistance(5.567);
        assertEquals("5.6 km", result);
    }

    // ==================== estimateTravelTime Tests ====================

    @Test
    public void testEstimateTravelTime_shortTrip() {
        String result = RouteOptimizer.estimateTravelTime(10);
        assertEquals("15 min", result);
    }

    @Test
    public void testEstimateTravelTime_mediumTrip() {
        String result = RouteOptimizer.estimateTravelTime(20);
        assertEquals("30 min", result);
    }

    @Test
    public void testEstimateTravelTime_exactHour() {
        String result = RouteOptimizer.estimateTravelTime(40);
        assertEquals("1h 0m", result);
    }

    @Test
    public void testEstimateTravelTime_overOneHour() {
        String result = RouteOptimizer.estimateTravelTime(60);
        assertEquals("1h 30m", result);
    }

    @Test
    public void testEstimateTravelTime_multipleHours() {
        String result = RouteOptimizer.estimateTravelTime(120);
        assertEquals("3h 0m", result);
    }

    @Test
    public void testEstimateTravelTime_zeroDistance() {
        String result = RouteOptimizer.estimateTravelTime(0);
        assertEquals("0 min", result);
    }

    @Test
    public void testEstimateTravelTime_veryShort() {
        String result = RouteOptimizer.estimateTravelTime(1);
        assertEquals("1 min", result); // 1 km at 40 km/h = 1.5 min, rounded to 1
    }

    @Test
    public void testEstimateTravelTime_longTrip() {
        String result = RouteOptimizer.estimateTravelTime(200);
        assertEquals("5h 0m", result);
    }

    // ==================== RoutePoint Tests ====================

    @Test
    public void testRoutePoint_creation() {
        RouteOptimizer.RoutePoint point = new RouteOptimizer.RoutePoint(null, 40.7128, -74.0060, "123 Main St");

        assertEquals(40.7128, point.latitude, DELTA);
        assertEquals(-74.0060, point.longitude, DELTA);
        assertEquals("123 Main St", point.formattedAddress);
        assertNull(point.invoice);
    }

    @Test
    public void testRoutePoint_orderIndex() {
        RouteOptimizer.RoutePoint point = new RouteOptimizer.RoutePoint(null, 0, 0, "Test");
        point.orderIndex = 1;
        assertEquals(1, point.orderIndex);
    }

    // ==================== OptimizedRoute Tests ====================

    @Test
    public void testOptimizedRoute_initialization() {
        RouteOptimizer.OptimizedRoute route = new RouteOptimizer.OptimizedRoute();

        assertNotNull(route.orderedPoints);
        assertTrue(route.orderedPoints.isEmpty());
        assertEquals(0.0, route.totalDistance, DELTA);
        assertEquals(0, route.totalStops);
        assertNull(route.summary);
    }

    @Test
    public void testOptimizedRoute_addPoints() {
        RouteOptimizer.OptimizedRoute route = new RouteOptimizer.OptimizedRoute();

        route.orderedPoints.add(new RouteOptimizer.RoutePoint(null, 40.0, -74.0, "Stop 1"));
        route.orderedPoints.add(new RouteOptimizer.RoutePoint(null, 41.0, -75.0, "Stop 2"));

        assertEquals(2, route.orderedPoints.size());
    }

    @Test
    public void testOptimizedRoute_setSummary() {
        RouteOptimizer.OptimizedRoute route = new RouteOptimizer.OptimizedRoute();
        route.totalDistance = 50.5;
        route.totalStops = 5;
        route.summary = String.format("Total: %.1f km | %d stops", route.totalDistance, route.totalStops);

        assertEquals("Total: 50.5 km | 5 stops", route.summary);
    }

    // ==================== Algorithm Behavior Tests ====================

    @Test
    public void testNearestNeighbor_conceptualOrder() {
        // Test that the nearest neighbor concept works
        // Given start at origin, point at (1,0) should be selected before point at (2,0)

        double startLat = 0.0, startLng = 0.0;
        double point1Lat = 0.009, point1Lng = 0.0; // ~1 km away
        double point2Lat = 0.018, point2Lng = 0.0; // ~2 km away

        double dist1 = RouteOptimizer.calculateDistance(startLat, startLng, point1Lat, point1Lng);
        double dist2 = RouteOptimizer.calculateDistance(startLat, startLng, point2Lat, point2Lng);

        assertTrue("Point 1 should be closer than Point 2", dist1 < dist2);
    }

    @Test
    public void testTriangleInequality() {
        // The direct distance should always be <= sum of two segments
        double lat1 = 40.0, lng1 = -74.0;
        double lat2 = 41.0, lng2 = -75.0;
        double lat3 = 42.0, lng3 = -76.0;

        double directDist = RouteOptimizer.calculateDistance(lat1, lng1, lat3, lng3);
        double leg1 = RouteOptimizer.calculateDistance(lat1, lng1, lat2, lng2);
        double leg2 = RouteOptimizer.calculateDistance(lat2, lng2, lat3, lng3);

        assertTrue("Triangle inequality should hold", directDist <= leg1 + leg2 + DELTA);
    }

    // ==================== Edge Cases ====================

    @Test
    public void testCalculateDistance_negativeLatitude() {
        // Sydney, Australia: -33.8688, 151.2093
        // Melbourne, Australia: -37.8136, 144.9631
        double distance = RouteOptimizer.calculateDistance(-33.8688, 151.2093, -37.8136, 144.9631);
        assertTrue("Sydney to Melbourne should be approximately 714 km", distance > 700 && distance < 750);
    }

    @Test
    public void testCalculateDistance_negativeLongitude() {
        // New York to Chicago
        double distance = RouteOptimizer.calculateDistance(40.7128, -74.0060, 41.8781, -87.6298);
        assertTrue("NY to Chicago should be approximately 1149 km", distance > 1100 && distance < 1200);
    }

    @Test
    public void testFormatDistance_verySmall() {
        String result = RouteOptimizer.formatDistance(0.001);
        assertEquals("1 m", result);
    }

    @Test
    public void testEstimateTravelTime_fractionalMinutes() {
        // 2 km at 40 km/h = 3 minutes
        String result = RouteOptimizer.estimateTravelTime(2);
        assertEquals("3 min", result);
    }

    // ==================== Stress Tests ====================

    @Test
    public void testCalculateDistance_manyCalculations() {
        // Ensure calculation is consistent across many iterations
        double lat1 = 40.7128, lng1 = -74.0060;
        double lat2 = 34.0522, lng2 = -118.2437;

        double firstResult = RouteOptimizer.calculateDistance(lat1, lng1, lat2, lng2);

        for (int i = 0; i < 1000; i++) {
            double result = RouteOptimizer.calculateDistance(lat1, lng1, lat2, lng2);
            assertEquals("Distance calculation should be deterministic", firstResult, result, DELTA);
        }
    }

    @Test
    public void testEarthRadiusConstant() {
        // Verify the Haversine formula uses correct Earth radius
        // By testing a known distance

        // 1 degree of latitude = ~111.32 km
        double distance = RouteOptimizer.calculateDistance(0.0, 0.0, 1.0, 0.0);
        assertTrue("1 degree latitude should be ~111 km", distance > 110 && distance < 112);
    }

    // ==================== Practical Delivery Scenario Tests ====================

    @Test
    public void testLocalDeliveryDistance() {
        // Simulate a local delivery route (< 50 km total)
        double startLat = 36.0, startLng = -86.5; // Nashville area
        double stop1Lat = 36.05, stop1Lng = -86.55;
        double stop2Lat = 36.1, stop2Lng = -86.6;
        double stop3Lat = 36.15, stop3Lng = -86.65;

        double totalDist = 0;
        totalDist += RouteOptimizer.calculateDistance(startLat, startLng, stop1Lat, stop1Lng);
        totalDist += RouteOptimizer.calculateDistance(stop1Lat, stop1Lng, stop2Lat, stop2Lng);
        totalDist += RouteOptimizer.calculateDistance(stop2Lat, stop2Lng, stop3Lat, stop3Lng);

        assertTrue("Local delivery route should be < 50 km", totalDist < 50);
    }

    @Test
    public void testTravelTimeForTypicalRoute() {
        // 30 km delivery route
        String time = RouteOptimizer.estimateTravelTime(30);
        assertEquals("45 min", time);
    }
}
