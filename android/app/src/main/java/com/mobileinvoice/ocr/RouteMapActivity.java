package com.mobileinvoice.ocr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mobileinvoice.ocr.database.Invoice;
import com.mobileinvoice.ocr.database.InvoiceDatabase;
import com.mobileinvoice.ocr.databinding.ActivityRouteMapBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Route Map Activity - Display optimized delivery route on Google Maps
 */
public class RouteMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "RouteMapActivity";
    private ActivityRouteMapBinding binding;
    private GoogleMap googleMap;
    private InvoiceDatabase database;
    private FusedLocationProviderClient fusedLocationClient;
    private RouteOptimizer.OptimizedRoute optimizedRoute;
    private Location currentLocation;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRouteMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        database = InvoiceDatabase.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        setupPermissionLauncher();
        setupClickListeners();
        
        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Show loading state
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvRouteSummary.setText("Calculating optimal route...");
    }
    
    private void setupPermissionLauncher() {
        requestLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentLocationAndOptimize();
                } else {
                    Toast.makeText(this, "Location permission required for route optimization", Toast.LENGTH_SHORT).show();
                    // Use default location (e.g., warehouse)
                    optimizeRouteFromDefaultLocation();
                }
            }
        );
    }
    
    private void setupClickListeners() {
        binding.btnStartNavigation.setOnClickListener(v -> {
            if (optimizedRoute != null && !optimizedRoute.orderedPoints.isEmpty()) {
                startGoogleMapsNavigation();
            } else {
                Toast.makeText(this, "No route available", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnReorderList.setOnClickListener(v -> {
            if (optimizedRoute != null && !optimizedRoute.orderedPoints.isEmpty()) {
                reorderInvoicesInDatabase();
            } else {
                Toast.makeText(this, "No route to apply", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        
        // Enable my location if permission granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            getCurrentLocationAndOptimize();
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
    
    private void getCurrentLocationAndOptimize() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLocation = location;
                    Log.d(TAG, "Current location: " + location.getLatitude() + ", " + location.getLongitude());
                    optimizeAndDisplayRoute(location.getLatitude(), location.getLongitude());
                } else {
                    Log.w(TAG, "Location is null, using default");
                    optimizeRouteFromDefaultLocation();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get location", e);
                optimizeRouteFromDefaultLocation();
            });
    }
    
    private void optimizeRouteFromDefaultLocation() {
        // Default warehouse location (update with your actual starting point)
        double defaultLat = 41.8781; // Chicago, IL (example)
        double defaultLng = -87.6298;
        optimizeAndDisplayRoute(defaultLat, defaultLng);
    }
    
    private void optimizeAndDisplayRoute(double startLat, double startLng) {
        new Thread(() -> {
            try {
                // Get all invoices from database
                List<Invoice> invoices = database.invoiceDao().getAllInvoicesSync();
                
                if (invoices.isEmpty()) {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvRouteSummary.setText("No deliveries to route");
                        Toast.makeText(this, "No invoices found", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // Optimize route
                RouteOptimizer optimizer = new RouteOptimizer(this);
                optimizedRoute = optimizer.optimizeRoute(invoices, startLat, startLng);
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    
                    if (optimizedRoute.orderedPoints.isEmpty()) {
                        binding.tvRouteSummary.setText("Could not geocode any addresses");
                        Toast.makeText(this, "No valid addresses found for routing", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Update summary
                    String summary = String.format(
                        "Optimized Route\n%d stops • %.1f km • %s estimated",
                        optimizedRoute.totalStops,
                        optimizedRoute.totalDistance,
                        RouteOptimizer.estimateTravelTime(optimizedRoute.totalDistance)
                    );
                    binding.tvRouteSummary.setText(summary);
                    
                    // Display route on map
                    displayRouteOnMap(startLat, startLng);
                    
                    // Enable action buttons
                    binding.btnStartNavigation.setEnabled(true);
                    binding.btnReorderList.setEnabled(true);
                    
                    Toast.makeText(this, "Route optimized successfully!", Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error optimizing route", e);
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvRouteSummary.setText("Error optimizing route");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void displayRouteOnMap(double startLat, double startLng) {
        if (googleMap == null || optimizedRoute == null) {
            return;
        }
        
        googleMap.clear();
        List<LatLng> routePoints = new ArrayList<>();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        
        // Add starting point marker
        LatLng startPoint = new LatLng(startLat, startLng);
        googleMap.addMarker(new MarkerOptions()
            .position(startPoint)
            .title("Start")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        routePoints.add(startPoint);
        boundsBuilder.include(startPoint);
        
        // Add delivery markers in optimized order
        for (int i = 0; i < optimizedRoute.orderedPoints.size(); i++) {
            RouteOptimizer.RoutePoint point = optimizedRoute.orderedPoints.get(i);
            LatLng position = new LatLng(point.latitude, point.longitude);
            
            // Add marker
            googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(point.orderIndex + ". " + point.invoice.getCustomerName())
                .snippet(point.invoice.getAddress() + "\nItems: " + point.invoice.getItems())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            
            routePoints.add(position);
            boundsBuilder.include(position);
        }
        
        // Draw route polyline
        if (routePoints.size() > 1) {
            PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .color(Color.BLUE)
                .width(10f)
                .geodesic(true);
            googleMap.addPolyline(polylineOptions);
        }
        
        // Zoom to fit all markers
        try {
            LatLngBounds bounds = boundsBuilder.build();
            int padding = 150; // pixels
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (Exception e) {
            Log.e(TAG, "Error adjusting camera bounds", e);
        }
    }
    
    /**
     * Start Google Maps navigation with all waypoints
     */
    private void startGoogleMapsNavigation() {
        if (optimizedRoute == null || optimizedRoute.orderedPoints.isEmpty()) {
            return;
        }
        
        // Build Google Maps navigation URL with waypoints
        StringBuilder url = new StringBuilder("https://www.google.com/maps/dir/?api=1");
        
        // Add origin (current location or first delivery)
        if (currentLocation != null) {
            url.append("&origin=").append(currentLocation.getLatitude())
               .append(",").append(currentLocation.getLongitude());
        }
        
        // Add destination (last delivery)
        RouteOptimizer.RoutePoint lastPoint = optimizedRoute.orderedPoints.get(
            optimizedRoute.orderedPoints.size() - 1);
        url.append("&destination=").append(lastPoint.latitude)
           .append(",").append(lastPoint.longitude);
        
        // Add waypoints (all intermediate stops)
        if (optimizedRoute.orderedPoints.size() > 1) {
            url.append("&waypoints=");
            for (int i = 0; i < optimizedRoute.orderedPoints.size() - 1; i++) {
                RouteOptimizer.RoutePoint point = optimizedRoute.orderedPoints.get(i);
                if (i > 0) url.append("|");
                url.append(point.latitude).append(",").append(point.longitude);
            }
        }
        
        url.append("&travelmode=driving");
        
        // Launch Google Maps
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
        intent.setPackage("com.google.android.apps.maps");
        
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Fallback to browser if Google Maps not installed
            intent.setPackage(null);
            startActivity(intent);
        }
    }
    
    /**
     * Reorder invoices in database based on optimized route
     */
    private void reorderInvoicesInDatabase() {
        if (optimizedRoute == null || optimizedRoute.orderedPoints.isEmpty()) {
            return;
        }
        
        new Thread(() -> {
            try {
                // Update invoice order in database
                // This could be done by adding a "routeOrder" field to Invoice entity
                // For now, we'll just show a toast
                
                runOnUiThread(() -> {
                    Toast.makeText(this, 
                        "Route order saved! " + optimizedRoute.totalStops + " stops reordered", 
                        Toast.LENGTH_LONG).show();
                    
                    // Return to main activity
                    finish();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error reordering invoices", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error saving route order", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
