package com.mobileinvoice.delivery.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.room.ColumnInfo;

import com.mobileinvoice.delivery.models.DeliveryStatus;
import com.mobileinvoice.delivery.models.Priority;
import com.mobileinvoice.delivery.data.converters.DateConverter;
import com.mobileinvoice.delivery.data.converters.EnumConverters;

import java.util.Date;

/**
 * Delivery Entity - Core data model for delivery management
 * 
 * Features:
 * - Auto-generated IDs
 * - Customer and address information
 * - GPS coordinates for routing
 * - Time windows for scheduling
 * - Status tracking with timestamps
 * - Package details and special instructions
 * - Proof of delivery data (signature, photos)
 */
@Entity(tableName = "deliveries")
@TypeConverters({DateConverter.class, EnumConverters.class})
public class Delivery {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "tracking_number")
    private String trackingNumber;
    
    // Customer Information
    @ColumnInfo(name = "customer_name")
    private String customerName;
    
    @ColumnInfo(name = "customer_phone")
    private String customerPhone;
    
    @ColumnInfo(name = "customer_email")
    private String customerEmail;
    
    // Address
    @ColumnInfo(name = "street_address")
    private String streetAddress;
    
    private String city;
    private String state;
    
    @ColumnInfo(name = "zip_code")
    private String zipCode;
    
    // GPS Coordinates
    private Double latitude;
    private Double longitude;
    
    // Delivery Details
    @ColumnInfo(name = "package_description")
    private String packageDescription;
    
    @ColumnInfo(name = "package_count")
    private int packageCount;
    
    @ColumnInfo(name = "package_weight")
    private Double packageWeight; // in pounds
    
    @ColumnInfo(name = "special_instructions")
    private String specialInstructions;
    
    // Status and Priority
    private DeliveryStatus status;
    private Priority priority;
    
    // Time Management
    @ColumnInfo(name = "created_at")
    private Date createdAt;
    
    @ColumnInfo(name = "scheduled_date")
    private Date scheduledDate;
    
    @ColumnInfo(name = "time_window_start")
    private String timeWindowStart; // e.g., "09:00"
    
    @ColumnInfo(name = "time_window_end")
    private String timeWindowEnd; // e.g., "12:00"
    
    @ColumnInfo(name = "completed_at")
    private Date completedAt;
    
    // Route Optimization
    @ColumnInfo(name = "route_order")
    private int routeOrder;
    
    @ColumnInfo(name = "estimated_arrival")
    private Date estimatedArrival;
    
    @ColumnInfo(name = "actual_arrival")
    private Date actualArrival;
    
    // Proof of Delivery
    @ColumnInfo(name = "signature_path")
    private String signaturePath;
    
    @ColumnInfo(name = "pod_photo_paths")
    private String podPhotoPaths; // Comma-separated paths
    
    @ColumnInfo(name = "delivery_notes")
    private String deliveryNotes;
    
    @ColumnInfo(name = "recipient_name")
    private String recipientName;
    
    // Failure Information
    @ColumnInfo(name = "failure_reason")
    private String failureReason;
    
    @ColumnInfo(name = "retry_count")
    private int retryCount;

    // Constructor
    public Delivery() {
        this.createdAt = new Date();
        this.status = DeliveryStatus.PENDING;
        this.priority = Priority.NORMAL;
        this.packageCount = 1;
        this.retryCount = 0;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getPackageDescription() { return packageDescription; }
    public void setPackageDescription(String packageDescription) { this.packageDescription = packageDescription; }

    public int getPackageCount() { return packageCount; }
    public void setPackageCount(int packageCount) { this.packageCount = packageCount; }

    public Double getPackageWeight() { return packageWeight; }
    public void setPackageWeight(Double packageWeight) { this.packageWeight = packageWeight; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Date scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getTimeWindowStart() { return timeWindowStart; }
    public void setTimeWindowStart(String timeWindowStart) { this.timeWindowStart = timeWindowStart; }

    public String getTimeWindowEnd() { return timeWindowEnd; }
    public void setTimeWindowEnd(String timeWindowEnd) { this.timeWindowEnd = timeWindowEnd; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public int getRouteOrder() { return routeOrder; }
    public void setRouteOrder(int routeOrder) { this.routeOrder = routeOrder; }

    public Date getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(Date estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public Date getActualArrival() { return actualArrival; }
    public void setActualArrival(Date actualArrival) { this.actualArrival = actualArrival; }

    public String getSignaturePath() { return signaturePath; }
    public void setSignaturePath(String signaturePath) { this.signaturePath = signaturePath; }

    public String getPodPhotoPaths() { return podPhotoPaths; }
    public void setPodPhotoPaths(String podPhotoPaths) { this.podPhotoPaths = podPhotoPaths; }

    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    // Utility Methods
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (streetAddress != null) address.append(streetAddress);
        if (city != null) address.append(", ").append(city);
        if (state != null) address.append(", ").append(state);
        if (zipCode != null) address.append(" ").append(zipCode);
        return address.toString();
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    public boolean isOverdue() {
        if (scheduledDate == null || status.isCompleted()) return false;
        return new Date().after(scheduledDate);
    }

    public String getTimeWindow() {
        if (timeWindowStart != null && timeWindowEnd != null) {
            return timeWindowStart + " - " + timeWindowEnd;
        }
        return "Anytime";
    }
}
