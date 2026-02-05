package com.mobileinvoice.delivery.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.mobileinvoice.delivery.data.entities.Delivery;
import com.mobileinvoice.delivery.models.DeliveryStatus;
import com.mobileinvoice.delivery.models.Priority;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for Delivery operations
 * Provides reactive queries with LiveData for automatic UI updates
 */
@Dao
public interface DeliveryDao {
    
    // ========== CREATE ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Delivery delivery);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Delivery> deliveries);
    
    // ========== READ ==========
    @Query("SELECT * FROM deliveries ORDER BY route_order ASC, scheduled_date ASC")
    LiveData<List<Delivery>> getAllDeliveries();
    
    @Query("SELECT * FROM deliveries WHERE id = :id LIMIT 1")
    LiveData<Delivery> getDeliveryById(long id);
    
    @Query("SELECT * FROM deliveries WHERE tracking_number = :trackingNumber LIMIT 1")
    LiveData<Delivery> getDeliveryByTrackingNumber(String trackingNumber);
    
    // Status-based queries
    @Query("SELECT * FROM deliveries WHERE status = :status ORDER BY route_order ASC")
    LiveData<List<Delivery>> getDeliveriesByStatus(DeliveryStatus status);
    
    @Query("SELECT * FROM deliveries WHERE status IN (:statuses) ORDER BY route_order ASC")
    LiveData<List<Delivery>> getDeliveriesByStatuses(List<DeliveryStatus> statuses);
    
    // Priority queries
    @Query("SELECT * FROM deliveries WHERE priority = :priority ORDER BY scheduled_date ASC")
    LiveData<List<Delivery>> getDeliveriesByPriority(Priority priority);
    
    // Date-based queries
    @Query("SELECT * FROM deliveries WHERE DATE(scheduled_date / 1000, 'unixepoch') = DATE(:date / 1000, 'unixepoch') ORDER BY route_order ASC")
    LiveData<List<Delivery>> getDeliveriesByDate(long date);
    
    @Query("SELECT * FROM deliveries WHERE scheduled_date >= :startDate AND scheduled_date <= :endDate ORDER BY scheduled_date ASC")
    LiveData<List<Delivery>> getDeliveriesInDateRange(Date startDate, Date endDate);
    
    // Active deliveries (pending or in transit)
    @Query("SELECT * FROM deliveries WHERE status IN ('PENDING', 'IN_TRANSIT') ORDER BY priority DESC, route_order ASC")
    LiveData<List<Delivery>> getActiveDeliveries();
    
    // Today's deliveries
    @Query("SELECT * FROM deliveries WHERE DATE(scheduled_date / 1000, 'unixepoch') = DATE('now') ORDER BY route_order ASC")
    LiveData<List<Delivery>> getTodaysDeliveries();
    
    // Overdue deliveries
    @Query("SELECT * FROM deliveries WHERE scheduled_date < :currentTime AND status NOT IN ('DELIVERED', 'CANCELLED') ORDER BY scheduled_date ASC")
    LiveData<List<Delivery>> getOverdueDeliveries(long currentTime);
    
    // Search queries
    @Query("SELECT * FROM deliveries WHERE " +
           "customer_name LIKE '%' || :query || '%' OR " +
           "tracking_number LIKE '%' || :query || '%' OR " +
           "street_address LIKE '%' || :query || '%' OR " +
           "city LIKE '%' || :query || '%' " +
           "ORDER BY scheduled_date DESC")
    LiveData<List<Delivery>> searchDeliveries(String query);
    
    // ========== UPDATE ==========
    @Update
    int update(Delivery delivery);
    
    @Query("UPDATE deliveries SET status = :status WHERE id = :id")
    int updateStatus(long id, DeliveryStatus status);
    
    @Query("UPDATE deliveries SET route_order = :order WHERE id = :id")
    int updateRouteOrder(long id, int order);
    
    @Query("UPDATE deliveries SET " +
           "status = :status, " +
           "completed_at = :completedAt, " +
           "actual_arrival = :actualArrival, " +
           "signature_path = :signaturePath, " +
           "recipient_name = :recipientName " +
           "WHERE id = :id")
    int completeDelivery(long id, DeliveryStatus status, Date completedAt, 
                        Date actualArrival, String signaturePath, String recipientName);
    
    @Query("UPDATE deliveries SET " +
           "status = :status, " +
           "failure_reason = :reason, " +
           "retry_count = retry_count + 1 " +
           "WHERE id = :id")
    int markDeliveryFailed(long id, DeliveryStatus status, String reason);
    
    // ========== DELETE ==========
    @Delete
    int delete(Delivery delivery);
    
    @Query("DELETE FROM deliveries WHERE id = :id")
    int deleteById(long id);
    
    @Query("DELETE FROM deliveries WHERE status = :status")
    int deleteByStatus(DeliveryStatus status);
    
    @Query("DELETE FROM deliveries")
    int deleteAll();
    
    // ========== STATISTICS ==========
    @Query("SELECT COUNT(*) FROM deliveries")
    LiveData<Integer> getTotalCount();
    
    @Query("SELECT COUNT(*) FROM deliveries WHERE status = :status")
    LiveData<Integer> getCountByStatus(DeliveryStatus status);
    
    @Query("SELECT COUNT(*) FROM deliveries WHERE DATE(scheduled_date / 1000, 'unixepoch') = DATE('now')")
    LiveData<Integer> getTodaysCount();
    
    @Query("SELECT COUNT(*) FROM deliveries WHERE " +
           "scheduled_date < :currentTime AND " +
           "status NOT IN ('DELIVERED', 'CANCELLED')")
    LiveData<Integer> getOverdueCount(long currentTime);
    
    // ========== SYNCHRONOUS QUERIES (for background operations) ==========
    @Query("SELECT * FROM deliveries WHERE id = :id LIMIT 1")
    Delivery getDeliveryByIdSync(long id);
    
    @Query("SELECT * FROM deliveries ORDER BY route_order ASC")
    List<Delivery> getAllDeliveriesSync();
    
    @Query("SELECT * FROM deliveries WHERE status IN ('PENDING', 'IN_TRANSIT') ORDER BY route_order ASC")
    List<Delivery> getActiveDeliveriesSync();
}
