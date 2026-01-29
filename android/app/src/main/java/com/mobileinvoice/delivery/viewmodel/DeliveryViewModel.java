package com.mobileinvoice.delivery.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.mobileinvoice.delivery.data.entities.Delivery;
import com.mobileinvoice.delivery.data.repository.DeliveryRepository;
import com.mobileinvoice.delivery.models.DeliveryStatus;
import com.mobileinvoice.delivery.models.Priority;

import java.util.Date;
import java.util.List;

/**
 * ViewModel for Delivery Management
 * 
 * Architecture:
 * - Separates UI from business logic
 * - Survives configuration changes (rotation, etc.)
 * - Provides LiveData for reactive UI updates
 * - Handles all data operations through repository
 */
public class DeliveryViewModel extends AndroidViewModel {
    
    private final DeliveryRepository repository;
    
    // LiveData streams
    private final LiveData<List<Delivery>> allDeliveries;
    private final LiveData<List<Delivery>> activeDeliveries;
    private final LiveData<List<Delivery>> todaysDeliveries;
    private final LiveData<Integer> totalCount;
    
    // Filter state
    private final MutableLiveData<DeliveryStatus> statusFilter = new MutableLiveData<>();
    private final MutableLiveData<Priority> priorityFilter = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    
    // Filtered deliveries (based on current filters)
    private final MediatorLiveData<List<Delivery>> filteredDeliveries = new MediatorLiveData<>();
    
    // UI state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public DeliveryViewModel(@NonNull Application application) {
        super(application);
        repository = new DeliveryRepository(application);
        
        // Initialize LiveData streams
        allDeliveries = repository.getAllDeliveries();
        activeDeliveries = repository.getActiveDeliveries();
        todaysDeliveries = repository.getTodaysDeliveries();
        totalCount = repository.getTotalCount();
        
        // Setup filtered deliveries
        setupFilteredDeliveries();
    }
    
    /**
     * Setup filtered deliveries mediator
     * Automatically updates when filters or data changes
     */
    private void setupFilteredDeliveries() {
        filteredDeliveries.addSource(allDeliveries, deliveries -> {
            applyFilters();
        });
        
        filteredDeliveries.addSource(statusFilter, status -> {
            applyFilters();
        });
        
        filteredDeliveries.addSource(priorityFilter, priority -> {
            applyFilters();
        });
        
        filteredDeliveries.addSource(searchQuery, query -> {
            applyFilters();
        });
    }
    
    /**
     * Apply all active filters to delivery list
     */
    private void applyFilters() {
        List<Delivery> deliveries = allDeliveries.getValue();
        if (deliveries == null) {
            filteredDeliveries.setValue(null);
            return;
        }
        
        // Apply filters based on active criteria
        DeliveryStatus status = statusFilter.getValue();
        Priority priority = priorityFilter.getValue();
        String query = searchQuery.getValue();
        
        // If no filters, show all
        if (status == null && priority == null && (query == null || query.isEmpty())) {
            filteredDeliveries.setValue(deliveries);
            return;
        }
        
        // Apply filters
        List<Delivery> filtered = deliveries.stream()
            .filter(d -> status == null || d.getStatus() == status)
            .filter(d -> priority == null || d.getPriority() == priority)
            .filter(d -> query == null || query.isEmpty() || matchesSearchQuery(d, query))
            .collect(java.util.stream.Collectors.toList());
        
        filteredDeliveries.setValue(filtered);
    }
    
    /**
     * Check if delivery matches search query
     */
    private boolean matchesSearchQuery(Delivery delivery, String query) {
        String lowerQuery = query.toLowerCase();
        return (delivery.getCustomerName() != null && delivery.getCustomerName().toLowerCase().contains(lowerQuery))
            || (delivery.getTrackingNumber() != null && delivery.getTrackingNumber().toLowerCase().contains(lowerQuery))
            || (delivery.getStreetAddress() != null && delivery.getStreetAddress().toLowerCase().contains(lowerQuery))
            || (delivery.getCity() != null && delivery.getCity().toLowerCase().contains(lowerQuery));
    }
    
    // ========== GETTERS FOR LIVEDATA ==========
    
    public LiveData<List<Delivery>> getAllDeliveries() {
        return allDeliveries;
    }
    
    public LiveData<List<Delivery>> getActiveDeliveries() {
        return activeDeliveries;
    }
    
    public LiveData<List<Delivery>> getTodaysDeliveries() {
        return todaysDeliveries;
    }
    
    public LiveData<List<Delivery>> getFilteredDeliveries() {
        return filteredDeliveries;
    }
    
    public LiveData<Delivery> getDeliveryById(long id) {
        return repository.getDeliveryById(id);
    }
    
    public LiveData<List<Delivery>> getDeliveriesByStatus(DeliveryStatus status) {
        return repository.getDeliveriesByStatus(status);
    }
    
    public LiveData<List<Delivery>> getOverdueDeliveries() {
        return repository.getOverdueDeliveries();
    }
    
    public LiveData<Integer> getTotalCount() {
        return totalCount;
    }
    
    public LiveData<Integer> getCountByStatus(DeliveryStatus status) {
        return repository.getCountByStatus(status);
    }
    
    public LiveData<Integer> getTodaysCount() {
        return repository.getTodaysCount();
    }
    
    public LiveData<Integer> getOverdueCount() {
        return repository.getOverdueCount();
    }
    
    // ========== FILTER OPERATIONS ==========
    
    public void setStatusFilter(DeliveryStatus status) {
        statusFilter.setValue(status);
    }
    
    public void setPriorityFilter(Priority priority) {
        priorityFilter.setValue(priority);
    }
    
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }
    
    public void clearFilters() {
        statusFilter.setValue(null);
        priorityFilter.setValue(null);
        searchQuery.setValue(null);
    }
    
    // ========== DATA OPERATIONS ==========
    
    public void insert(Delivery delivery) {
        isLoading.setValue(true);
        repository.insert(delivery, id -> {
            isLoading.postValue(false);
            if (id > 0) {
                // Success
            } else {
                errorMessage.postValue("Failed to create delivery");
            }
        });
    }
    
    public void update(Delivery delivery) {
        isLoading.setValue(true);
        repository.update(delivery);
        isLoading.setValue(false);
    }
    
    public void delete(Delivery delivery) {
        repository.delete(delivery);
    }
    
    public void updateStatus(long id, DeliveryStatus status) {
        repository.updateStatus(id, status);
    }
    
    public void completeDelivery(long id, String recipientName, String signaturePath) {
        repository.completeDelivery(id, recipientName, signaturePath);
    }
    
    public void markDeliveryFailed(long id, String reason) {
        repository.markDeliveryFailed(id, reason);
    }
    
    public void updateRouteOrders(List<Delivery> deliveries) {
        repository.updateRouteOrders(deliveries);
    }
    
    public void deleteCompleted() {
        repository.deleteCompleted();
    }
    
    // ========== UI STATE ==========
    
    public LiveData<Boolean> isLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public void clearError() {
        errorMessage.setValue(null);
    }
}
