package com.mobileinvoice.delivery.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mobileinvoice.delivery.data.entities.Delivery;
import com.mobileinvoice.delivery.models.DeliveryStatus;
import com.mobileinvoice.delivery.ui.adapters.DeliveryAdapter;
import com.mobileinvoice.delivery.viewmodel.DeliveryViewModel;
import com.mobileinvoice.ocr.R;

/**
 * Main Dashboard Activity for Delivery Management
 * 
 * Features:
 * - Material Design 3 with adaptive layouts
 * - Real-time delivery list with filters
 * - Quick status chips for filtering
 * - Search functionality
 * - Swipe actions (complete/delete)
 * - FAB for creating new deliveries
 * - Statistics header
 */
public class DeliveryDashboardActivity extends AppCompatActivity {
    
    private DeliveryViewModel viewModel;
    private DeliveryAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyView;
    private ExtendedFloatingActionButton fabAddDelivery;
    private ChipGroup chipGroupFilters;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_dashboard);
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Deliveries");
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DeliveryViewModel.class);
        
        // Initialize views
        initializeViews();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup filters
        setupFilters();
        
        // Observe data
        observeData();
        
        // Setup FAB
        setupFab();
    }
    
    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_deliveries);
        emptyView = findViewById(R.id.layout_empty);
        fabAddDelivery = findViewById(R.id.fab_add_delivery);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
    }
    
    private void setupRecyclerView() {
        adapter = new DeliveryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        
        // Click listener
        adapter.setOnDeliveryClickListener(delivery -> {
            // Open delivery detail
            // TODO: Implement DeliveryDetailActivity
            Toast.makeText(this, "Detail view coming soon", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, DeliveryDetailActivity.class);
            // intent.putExtra("DELIVERY_ID", delivery.getId());
            // startActivity(intent);
        });
        
        // Long click listener
        adapter.setOnDeliveryLongClickListener(delivery -> {
            showDeliveryOptionsDialog(delivery);
        });
        
        // Swipe actions
        setupSwipeActions();
    }
    
    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
            0, 
            ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, 
                                @NonNull RecyclerView.ViewHolder viewHolder, 
                                @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Delivery delivery = adapter.getCurrentList().get(position);
                
                if (direction == ItemTouchHelper.RIGHT) {
                    // Swipe right to complete
                    if (delivery.getStatus() == DeliveryStatus.PENDING || 
                        delivery.getStatus() == DeliveryStatus.IN_TRANSIT) {
                        viewModel.updateStatus(delivery.getId(), DeliveryStatus.DELIVERED);
                        Snackbar.make(recyclerView, "Delivery marked as completed", Snackbar.LENGTH_SHORT).show();
                    } else {
                        adapter.notifyItemChanged(position);
                    }
                } else {
                    // Swipe left to delete
                    viewModel.delete(delivery);
                    Snackbar.make(recyclerView, "Delivery deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> viewModel.insert(delivery))
                        .show();
                }
            }
        };
        
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }
    
    private void setupFilters() {
        // All filter (default)
        Chip chipAll = findViewById(R.id.chip_all);
        chipAll.setOnClickListener(v -> {
            viewModel.clearFilters();
            chipAll.setChecked(true);
        });
        
        // Status filters
        Chip chipPending = findViewById(R.id.chip_pending);
        chipPending.setOnClickListener(v -> {
            viewModel.setStatusFilter(DeliveryStatus.PENDING);
        });
        
        Chip chipInTransit = findViewById(R.id.chip_in_transit);
        chipInTransit.setOnClickListener(v -> {
            viewModel.setStatusFilter(DeliveryStatus.IN_TRANSIT);
        });
        
        Chip chipDelivered = findViewById(R.id.chip_delivered);
        chipDelivered.setOnClickListener(v -> {
            viewModel.setStatusFilter(DeliveryStatus.DELIVERED);
        });
    }
    
    private void observeData() {
        // Observe filtered deliveries
        viewModel.getFilteredDeliveries().observe(this, deliveries -> {
            if (deliveries != null && !deliveries.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                adapter.submitList(deliveries);
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
        
        // Observe statistics
        viewModel.getTotalCount().observe(this, count -> {
            // Update statistics in header
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(count + " total deliveries");
            }
        });
        
        // Observe loading state
        viewModel.isLoading().observe(this, isLoading -> {
            // Show/hide loading indicator
            // TODO: Implement loading indicator
        });
        
        // Observe errors
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }
    
    private void setupFab() {
        fabAddDelivery.setOnClickListener(v -> {
            // Open new delivery form
            // TODO: Implement NewDeliveryActivity
            Toast.makeText(this, "Create delivery coming soon", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, NewDeliveryActivity.class);
            // startActivity(intent);
        });
        
        // Hide FAB on scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fabAddDelivery.shrink();
                } else if (dy < 0) {
                    fabAddDelivery.extend();
                }
            }
        });
    }
    
    private void showDeliveryOptionsDialog(Delivery delivery) {
        // TODO: Show bottom sheet with delivery options
        // - View details
        // - Start navigation
        // - Call customer
        // - Mark as delivered/failed
        // - Delete
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO: Create menu_dashboard.xml with menu items
        // getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        
        // // Setup search
        // MenuItem searchItem = menu.findItem(R.id.action_search);
        // SearchView searchView = (SearchView) searchItem.getActionView();
        // searchView.setQueryHint("Search deliveries...");
        // searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        //     @Override
        //     public boolean onQueryTextSubmit(String query) {
        //         return false;
        //     }

        //     @Override
        //     public boolean onQueryTextChange(String newText) {
        //         viewModel.setSearchQuery(newText);
        //         return true;
        //     }
        // });
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // TODO: Implement menu actions
        // int id = item.getItemId();
        
        // if (id == R.id.action_route_map) {
        //     // Open route optimization map
        //     startActivity(new Intent(this, RouteMapActivity.class));
        //     return true;
        // } else if (id == R.id.action_statistics) {
        //     // Open statistics screen
        //     startActivity(new Intent(this, StatisticsActivity.class));
        //     return true;
        // } else if (id == R.id.action_export) {
        //     // Show export options
        //     showExportDialog();
        //     return true;
        // } else if (id == R.id.action_settings) {
        //     // Open settings
        //     startActivity(new Intent(this, SettingsActivity.class));
        //     return true;
        // }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showExportDialog() {
        // TODO: Implement export dialog
        Toast.makeText(this, "Export feature coming soon", Toast.LENGTH_SHORT).show();
    }
}
