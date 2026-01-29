package com.mobileinvoice.delivery.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.mobileinvoice.delivery.data.dao.DeliveryDao;
import com.mobileinvoice.delivery.data.database.DeliveryDatabase;
import com.mobileinvoice.delivery.data.entities.Delivery;
import com.mobileinvoice.delivery.models.DeliveryStatus;
import com.mobileinvoice.delivery.models.Priority;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository Pattern Implementation
 * 
 * Single source of truth for delivery data
 * Abstracts data sources (Room, Network, etc.)
 * Handles background threading automatically
 */
public class DeliveryRepository {
    
    private final DeliveryDao deliveryDao;
    private final ExecutorService executorService;
    
    // Cached queries
    private final LiveData<List<Delivery>> allDeliveries;
    private final LiveData<List<Delivery>> activeDeliveries;
    private final LiveData<List<Delivery>> todaysDeliveries;
    private final LiveData<Integer> totalCount;
    
    public DeliveryRepository(Application application) {
        DeliveryDatabase database = DeliveryDatabase.getInstance(application);
        deliveryDao = database.deliveryDao();
        executorService = Executors.newFixedThreadPool(4);
        
        // Initialize cached queries
        allDeliveries = deliveryDao.getAllDeliveries();
        activeDeliveries = deliveryDao.getActiveDeliveries();
        todaysDeliveries = deliveryDao.getTodaysDeliveries();
        totalCount = deliveryDao.getTotalCount();
    }
    
    // ========== CREATE OPERATIONS ==========
    
    /**
     * Insert new delivery
     * Automatically generates tracking number and sets defaults
     */
    public void insert(Delivery delivery, OnDeliveryInsertedListener listener) {
        executorService.execute(() -> {
            if (delivery.getTrackingNumber() == null) {
                delivery.setTrackingNumber(generateTrackingNumber());
            }
            long id = deliveryDao.insert(delivery);
            if (listener != null) {
                listener.onInserted(id);
            }
        });
    }
    
    /**
     * Bulk insert deliveries
     */
    public void insertAll(List<Delivery> deliveries, OnDeliveriesBulkInsertedListener listener) {
        executorService.execute(() -> {
            // Generate tracking numbers for deliveries that don't have them
            for (Delivery delivery : deliveries) {
                if (delivery.getTrackingNumber() == null) {
                    delivery.setTrackingNumber(generateTrackingNumber());
                }
            }
            List<Long> ids = deliveryDao.insertAll(deliveries);
            if (listener != null) {
                listener.onInserted(ids);
            }
        });
    }
    
    // ========== READ OPERATIONS ==========
    
    public LiveData<List<Delivery>> getAllDeliveries() {
        return allDeliveries;
    }
    
    public LiveData<Delivery> getDeliveryById(long id) {
        return deliveryDao.getDeliveryById(id);
    }
    
    public LiveData<List<Delivery>> getActiveDeliveries() {
        return activeDeliveries;
    }
    
    public LiveData<List<Delivery>> getTodaysDeliveries() {
        return todaysDeliveries;
    }
    
    public LiveData<List<Delivery>> getDeliveriesByStatus(DeliveryStatus status) {
        return deliveryDao.getDeliveriesByStatus(status);
    }
    
    public LiveData<List<Delivery>> getDeliveriesByPriority(Priority priority) {
        return deliveryDao.getDeliveriesByPriority(priority);
    }
    
    public LiveData<List<Delivery>> getDeliveriesByDate(Date date) {
        return deliveryDao.getDeliveriesByDate(date.getTime());
    }
    
    public LiveData<List<Delivery>> getOverdueDeliveries() {
        return deliveryDao.getOverdueDeliveries(System.currentTimeMillis());
    }
    
    public LiveData<List<Delivery>> searchDeliveries(String query) {
        return deliveryDao.searchDeliveries(query);
    }
    
    // ========== UPDATE OPERATIONS ==========
    
    public void update(Delivery delivery) {
        executorService.execute(() -> deliveryDao.update(delivery));
    }
    
    public void updateStatus(long id, DeliveryStatus status) {
        executorService.execute(() -> deliveryDao.updateStatus(id, status));
    }
    
    public void updateRouteOrder(long id, int order) {
        executorService.execute(() -> deliveryDao.updateRouteOrder(id, order));
    }
    
    /**
     * Mark delivery as completed with all required proof of delivery info
     */
    public void completeDelivery(long id, String recipientName, String signaturePath) {
        executorService.execute(() -> {
            Date now = new Date();
            deliveryDao.completeDelivery(
                id, 
                DeliveryStatus.DELIVERED, 
                now, 
                now, 
                signaturePath, 
                recipientName
            );
        });
    }
    
    /**
     * Mark delivery as failed with reason
     */
    public void markDeliveryFailed(long id, String reason) {
        executorService.execute(() -> 
            deliveryDao.markDeliveryFailed(id, DeliveryStatus.FAILED, reason)
        );
    }
    
    /**
     * Batch update route orders for optimized route
     */
    public void updateRouteOrders(List<Delivery> deliveries) {
        executorService.execute(() -> {
            for (int i = 0; i < deliveries.size(); i++) {
                deliveryDao.updateRouteOrder(deliveries.get(i).getId(), i);
            }
        });
    }
    
    // ========== DELETE OPERATIONS ==========
    
    public void delete(Delivery delivery) {
        executorService.execute(() -> deliveryDao.delete(delivery));
    }
    
    public void deleteById(long id) {
        executorService.execute(() -> deliveryDao.deleteById(id));
    }
    
    public void deleteCompleted() {
        executorService.execute(() -> {
            deliveryDao.deleteByStatus(DeliveryStatus.DELIVERED);
            deliveryDao.deleteByStatus(DeliveryStatus.CANCELLED);
        });
    }
    
    public void deleteAll() {
        executorService.execute(() -> deliveryDao.deleteAll());
    }
    
    // ========== STATISTICS ==========
    
    public LiveData<Integer> getTotalCount() {
        return totalCount;
    }
    
    public LiveData<Integer> getCountByStatus(DeliveryStatus status) {
        return deliveryDao.getCountByStatus(status);
    }
    
    public LiveData<Integer> getTodaysCount() {
        return deliveryDao.getTodaysCount();
    }
    
    public LiveData<Integer> getOverdueCount() {
        return deliveryDao.getOverdueCount(System.currentTimeMillis());
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Generate unique tracking number
     * Format: DEL-YYYYMMDD-XXXXX
     */
    private String generateTrackingNumber() {
        long timestamp = System.currentTimeMillis();
        String dateStr = new java.text.SimpleDateFormat("yyyyMMdd").format(new Date(timestamp));
        String random = String.format("%05d", (int)(Math.random() * 100000));
        return "DEL-" + dateStr + "-" + random;
    }
    
    /**
     * Get synchronous data for background operations
     */
    public void getActiveDeliveriesSync(OnDeliveriesLoadedListener listener) {
        executorService.execute(() -> {
            List<Delivery> deliveries = deliveryDao.getActiveDeliveriesSync();
            if (listener != null) {
                listener.onLoaded(deliveries);
            }
        });
    }
    
    // ========== CALLBACKS ==========
    
    public interface OnDeliveryInsertedListener {
        void onInserted(long id);
    }
    
    public interface OnDeliveriesBulkInsertedListener {
        void onInserted(List<Long> ids);
    }
    
    public interface OnDeliveriesLoadedListener {
        void onLoaded(List<Delivery> deliveries);
    }
}
