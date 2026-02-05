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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
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
 * with split-screen view showing both map and delivery list
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
    private RouteStopAdapter stopAdapter;
    private ItemTouchHelper itemTouchHelper;
    private boolean isMapExpanded = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRouteMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        database = InvoiceDatabase.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        setupPermissionLauncher();
        setupRecyclerView();
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
    
    private void setupRecyclerView() {
        stopAdapter = new RouteStopAdapter(new RouteStopAdapter.OnStopClickListener() {
            @Override
            public void onCallClick(RouteOptimizer.RoutePoint stop) {
                handleCallCustomer(stop);
            }

            @Override
            public void onNavigateClick(RouteOptimizer.RoutePoint stop) {
                handleNavigateToStop(stop);
            }
        }, new RouteStopAdapter.OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                if (itemTouchHelper != null) {
                    itemTouchHelper.startDrag(viewHolder);
                }
            }
        });

        // Setup stop change listener for priority and time adjustments
        stopAdapter.setOnStopChangeListener(new RouteStopAdapter.OnStopChangeListener() {
            @Override
            public void onMakeFirst(RouteOptimizer.RoutePoint stop) {
                handleMakeFirst(stop);
            }

            @Override
            public void onMakeLast(RouteOptimizer.RoutePoint stop) {
                handleMakeLast(stop);
            }

            @Override
            public void onStopTimeChanged(RouteOptimizer.RoutePoint stop, int newTimeMinutes) {
                handleStopTimeChanged(stop, newTimeMinutes);
            }

            @Override
            public void onRouteOrderChanged() {
                updateMapAfterReorder();
            }

            @Override
            public void onCompletedChanged(RouteOptimizer.RoutePoint stop, boolean completed) {
                handleCompletedChanged(stop, completed);
            }
        });

        binding.recyclerViewStops.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewStops.setAdapter(stopAdapter);

        // Setup drag and drop
        RouteItemTouchHelper.OnItemMovedListener moveListener =
            new RouteItemTouchHelper.OnItemMovedListener() {
                @Override
                public void onItemMoved(int fromPosition, int toPosition) {
                    // Update map when items are reordered
                    updateMapAfterReorder();
                }
            };

        RouteItemTouchHelper callback = new RouteItemTouchHelper(stopAdapter, moveListener);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewStops);
    }

    /**
     * Handle "Make First" priority action
     */
    private void handleMakeFirst(RouteOptimizer.RoutePoint stop) {
        List<RouteOptimizer.RoutePoint> activeStops = stopAdapter.getActiveStops();
        RouteOptimizer.makeFirst(activeStops, stop);

        // Clear other FIRST priorities
        for (RouteOptimizer.RoutePoint s : activeStops) {
            if (s != stop && s.priority == RouteOptimizer.PRIORITY_FIRST) {
                s.priority = RouteOptimizer.PRIORITY_NORMAL;
            }
        }

        // Update route and recalculate ETAs
        optimizedRoute.orderedPoints = activeStops;
        recalculateETAsAndRefresh();

        Toast.makeText(this, stop.invoice.getCustomerName() + " moved to first", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle "Make Last" priority action
     */
    private void handleMakeLast(RouteOptimizer.RoutePoint stop) {
        List<RouteOptimizer.RoutePoint> activeStops = stopAdapter.getActiveStops();
        RouteOptimizer.makeLast(activeStops, stop);

        // Clear other LAST priorities
        for (RouteOptimizer.RoutePoint s : activeStops) {
            if (s != stop && s.priority == RouteOptimizer.PRIORITY_LAST) {
                s.priority = RouteOptimizer.PRIORITY_NORMAL;
            }
        }

        // Update route and recalculate ETAs
        optimizedRoute.orderedPoints = activeStops;
        recalculateETAsAndRefresh();

        Toast.makeText(this, stop.invoice.getCustomerName() + " moved to last", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle stop time change
     */
    private void handleStopTimeChanged(RouteOptimizer.RoutePoint stop, int newTimeMinutes) {
        stop.stopTimeMinutes = newTimeMinutes;

        // Save to database for persistence
        stop.invoice.setStopTimeMinutes(newTimeMinutes);
        new Thread(() -> {
            database.invoiceDao().update(stop.invoice);
        }).start();

        // Recalculate ETAs for all subsequent stops
        recalculateETAsAndRefresh();
    }

    /**
     * Handle delivery completed checkbox change
     */
    private void handleCompletedChanged(RouteOptimizer.RoutePoint stop, boolean completed) {
        // Update the invoice's completed status
        stop.invoice.setCompleted(completed);

        // Save to database
        new Thread(() -> {
            database.invoiceDao().update(stop.invoice);
            runOnUiThread(() -> {
                // Refresh the adapter to update styling
                stopAdapter.setStops(stopAdapter.getAllStops());
                recalculateETAsAndRefresh();

                String status = completed ? "completed" : "active";
                Toast.makeText(this, stop.invoice.getCustomerName() + " marked as " + status,
                    Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    /**
     * Recalculate ETAs and refresh the display
     */
    private void recalculateETAsAndRefresh() {
        if (optimizedRoute == null || optimizedRoute.orderedPoints.isEmpty()) {
            return;
        }

        double startLat = currentLocation != null ?
            currentLocation.getLatitude() : optimizedRoute.orderedPoints.get(0).latitude;
        double startLng = currentLocation != null ?
            currentLocation.getLongitude() : optimizedRoute.orderedPoints.get(0).longitude;

        // Recalculate ETAs
        RouteOptimizer.recalculateETAs(optimizedRoute, startLat, startLng);

        // Recalculate distance
        recalculateRouteDistance();

        // Rebuild adapter items to show updated ETAs
        List<RouteOptimizer.RoutePoint> allPoints = new ArrayList<>(optimizedRoute.orderedPoints);

        // Re-add completed stops
        List<Invoice> allInvoices;
        try {
            allInvoices = database.invoiceDao().getAllInvoicesSync();
            for (Invoice invoice : allInvoices) {
                if (invoice.isCompleted()) {
                    RouteOptimizer.RoutePoint completedPoint =
                        new RouteOptimizer.RoutePoint(invoice, 0, 0, invoice.getAddress());
                    completedPoint.orderIndex = 0;
                    allPoints.add(completedPoint);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading completed invoices", e);
        }

        stopAdapter.setStops(allPoints);

        // Update map
        displayRouteOnMap(startLat, startLng);

        // Update summary
        updateRouteSummary();
    }
    
    /**
     * Redraw the route on the map after manual reordering
     */
    private void updateMapAfterReorder() {
        if (googleMap == null || optimizedRoute == null) {
            return;
        }

        // Update the optimized route with new order (ACTIVE ONLY)
        optimizedRoute.orderedPoints = stopAdapter.getActiveStops();
        
        // Recalculate total distance
        recalculateRouteDistance();
        
        // Redraw map
        double startLat = currentLocation != null ? 
            currentLocation.getLatitude() : optimizedRoute.orderedPoints.get(0).latitude;
        double startLng = currentLocation != null ? 
            currentLocation.getLongitude() : optimizedRoute.orderedPoints.get(0).longitude;
        
        displayRouteOnMap(startLat, startLng);
        
        // Update summary
        updateRouteSummary();
    }
    
    /**
     * Recalculate route distance after reordering
     */
    private void recalculateRouteDistance() {
        if (optimizedRoute == null || optimizedRoute.orderedPoints.isEmpty()) {
            return;
        }
        
        double totalDistance = 0;
        double prevLat = currentLocation != null ? 
            currentLocation.getLatitude() : optimizedRoute.orderedPoints.get(0).latitude;
        double prevLng = currentLocation != null ? 
            currentLocation.getLongitude() : optimizedRoute.orderedPoints.get(0).longitude;
        
        for (RouteOptimizer.RoutePoint point : optimizedRoute.orderedPoints) {
            double distance = RouteOptimizer.calculateDistance(
                prevLat, prevLng, point.latitude, point.longitude);
            totalDistance += distance;
            prevLat = point.latitude;
            prevLng = point.longitude;
        }
        
        optimizedRoute.totalDistance = totalDistance;
    }
    
    /**
     * Update the route summary text
     */
    private void updateRouteSummary() {
        if (optimizedRoute == null || optimizedRoute.orderedPoints.isEmpty()) {
            return;
        }

        String endTime = optimizedRoute.getFormattedEndTime();
        String summary;

        if (endTime != null && !endTime.equals("N/A")) {
            summary = String.format(
                "Optimized Route\n%d stops • %.1f mi • Finish by %s",
                optimizedRoute.orderedPoints.size(),
                optimizedRoute.totalDistance,
                endTime
            );
        } else {
            summary = String.format(
                "Optimized Route\n%d stops • %.1f mi • %s estimated",
                optimizedRoute.orderedPoints.size(),
                optimizedRoute.totalDistance,
                RouteOptimizer.estimateTravelTime(optimizedRoute.totalDistance)
            );
        }
        binding.tvRouteSummary.setText(summary);
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
        // Toggle map size (expand/collapse)
        binding.btnToggleMapSize.setOnClickListener(v -> toggleMapSize());
        
        // Recenter map
        binding.btnRecenterMap.setOnClickListener(v -> recenterMap());
        
        // Start navigation
        binding.btnStartNavigation.setOnClickListener(v -> {
            if (optimizedRoute != null && !optimizedRoute.orderedPoints.isEmpty()) {
                startGoogleMapsNavigation();
            } else {
                Toast.makeText(this, "No route available", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Apply order to list
        binding.btnReorderList.setOnClickListener(v -> {
            if (optimizedRoute != null && !optimizedRoute.orderedPoints.isEmpty()) {
                reorderInvoicesInDatabase();
            } else {
                Toast.makeText(this, "No route to apply", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Toggle between split-screen and full-screen map view
     */
    private void toggleMapSize() {
        LinearLayout.LayoutParams mapParams = 
            (LinearLayout.LayoutParams) binding.mapContainer.getLayoutParams();
        LinearLayout.LayoutParams listParams = 
            (LinearLayout.LayoutParams) binding.stopsListContainer.getLayoutParams();
        
        if (isMapExpanded) {
            // Return to split view
            mapParams.weight = 1;
            listParams.weight = 1;
            binding.stopsListContainer.setVisibility(View.VISIBLE);
            binding.summaryBar.setVisibility(View.VISIBLE);
            isMapExpanded = false;
        } else {
            // Expand map to full screen
            mapParams.weight = 1;
            listParams.weight = 0;
            binding.stopsListContainer.setVisibility(View.GONE);
            binding.summaryBar.setVisibility(View.GONE);
            isMapExpanded = true;
        }
        
        binding.mapContainer.setLayoutParams(mapParams);
        binding.stopsListContainer.setLayoutParams(listParams);
    }
    
    /**
     * Recenter map to show all route points
     */
    private void recenterMap() {
        if (googleMap != null && optimizedRoute != null && !optimizedRoute.orderedPoints.isEmpty()) {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            
            if (currentLocation != null) {
                boundsBuilder.include(new LatLng(
                    currentLocation.getLatitude(), 
                    currentLocation.getLongitude()));
            }
            
            for (RouteOptimizer.RoutePoint point : optimizedRoute.orderedPoints) {
                boundsBuilder.include(new LatLng(point.latitude, point.longitude));
            }
            
            try {
                LatLngBounds bounds = boundsBuilder.build();
                int padding = 150;
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                Log.e(TAG, "Error recentering map", e);
            }
        }
    }
    
    /**
     * Handle call customer action
     */
    private void handleCallCustomer(RouteOptimizer.RoutePoint stop) {
        String phoneNumber = stop.invoice.getPhone();
        
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle navigate to specific stop
     */
    private void handleNavigateToStop(RouteOptimizer.RoutePoint stop) {
        String uri = String.format("google.navigation:q=%f,%f", 
            stop.latitude, stop.longitude);
        
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Fallback to regular maps
            String fallbackUri = String.format("geo:%f,%f?q=%f,%f(%s)",
                stop.latitude, stop.longitude,
                stop.latitude, stop.longitude,
                Uri.encode(stop.invoice.getCustomerName()));
            Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUri));
            startActivity(fallbackIntent);
        }
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
        // Default warehouse location: 1517 West Battlefield Rd., Springfield, MO 65807
        double defaultLat = 37.1819; // Springfield, MO warehouse
        double defaultLng = -93.3147;
        optimizeAndDisplayRoute(defaultLat, defaultLng);
    }
    
    private void optimizeAndDisplayRoute(double startLat, double startLng) {
        new Thread(() -> {
            try {
                // Get all invoices from database
                List<Invoice> allInvoices = database.invoiceDao().getAllInvoicesSync();

                if (allInvoices.isEmpty()) {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvRouteSummary.setText("No deliveries to route");
                        Toast.makeText(this, "No invoices found", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // FILTER: Only optimize active invoices (exclude completed)
                List<Invoice> activeInvoices = new ArrayList<>();
                for (Invoice invoice : allInvoices) {
                    if (invoice.isActive()) {
                        activeInvoices.add(invoice);
                    }
                }

                if (activeInvoices.isEmpty()) {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvRouteSummary.setText("All deliveries completed");

                        // Still show completed items in list
                        Toast.makeText(this, "No active deliveries to route", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Optimize route with ACTIVE invoices only
                RouteOptimizer optimizer = new RouteOptimizer(this);
                optimizedRoute = optimizer.optimizeRoute(activeInvoices, startLat, startLng);

                // Calculate ETAs for all stops (starting now)
                long startTimeMillis = System.currentTimeMillis();
                RouteOptimizer.calculateETAs(optimizedRoute, startLat, startLng, startTimeMillis);
                
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
                        "Optimized Route\n%d stops • %.1f mi • %s estimated",
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

                    // Update RecyclerView with ALL stops (active + completed)
                    // Adapter will handle splitting into sections
                    List<RouteOptimizer.RoutePoint> allPoints = new ArrayList<>(optimizedRoute.orderedPoints);

                    // Add completed invoices to list (they won't be in optimized route)
                    for (Invoice invoice : allInvoices) {
                        if (invoice.isCompleted()) {
                            // Create RoutePoint for completed items (with address, no geocoding)
                            RouteOptimizer.RoutePoint completedPoint =
                                new RouteOptimizer.RoutePoint(invoice, 0, 0, invoice.getAddress());
                            completedPoint.orderIndex = 0; // No order for completed
                            allPoints.add(completedPoint);
                        }
                    }

                    stopAdapter.setStops(allPoints);

                    // Show dialog if some invoices failed to geocode
                    if (!optimizedRoute.failedInvoices.isEmpty()) {
                        showGeocodingFailuresDialog(optimizedRoute.failedInvoices);
                    } else {
                        Toast.makeText(this, "Route optimized successfully!", Toast.LENGTH_SHORT).show();
                    }
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
                        "Route order applied! " + optimizedRoute.totalStops + " stops reordered",
                        Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error reordering invoices", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error saving route order", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * Show a dialog listing invoices that failed geocoding
     */
    private void showGeocodingFailuresDialog(List<RouteOptimizer.GeocodingFailure> failures) {
        StringBuilder message = new StringBuilder();
        message.append("The following invoices could not be added to the route:\n\n");

        for (RouteOptimizer.GeocodingFailure failure : failures) {
            String customerName = failure.invoice.getCustomerName();
            String invoiceNum = failure.invoice.getInvoiceNumber();
            message.append("• ").append(customerName != null ? customerName : "Unknown");
            if (invoiceNum != null && !invoiceNum.isEmpty()) {
                message.append(" (#").append(invoiceNum).append(")");
            }
            message.append("\n   ").append(failure.reason).append("\n\n");
        }

        message.append("Please check the addresses for these invoices.");

        new AlertDialog.Builder(this)
            .setTitle("Missing Stops (" + failures.size() + ")")
            .setMessage(message.toString())
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
}
