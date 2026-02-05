package com.mobileinvoice.ocr.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "invoices")
@TypeConverters(Converters.class)
public class Invoice {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String invoiceNumber;
    private String customerName;
    private String address;
    private String phone;
    private String items; // Comma-separated string
    private String podImagePath1;
    private String podImagePath2;
    private String podImagePath3;
    private String signatureImagePath;
    private String notes;
    private String originalImagePath;
    private String rawOcrText;
    private long timestamp;
    private String status; // Values: "PENDING", "IN_TRANSIT", "DELIVERED", "CANCELLED"
    private int stopTimeMinutes = 30; // Default 30 minutes for appliance delivery

    public Invoice() {
        this.timestamp = System.currentTimeMillis();
        this.status = "PENDING"; // Default to PENDING
        this.stopTimeMinutes = 30; // Default 30 minutes
    }

    // Getters
    public int getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getCustomerName() { return customerName; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getItems() { return items; }
    public String getPodImagePath1() { return podImagePath1; }
    public String getPodImagePath2() { return podImagePath2; }
    public String getPodImagePath3() { return podImagePath3; }
    public String getSignatureImagePath() { return signatureImagePath; }
    public String getNotes() { return notes; }
    public String getOriginalImagePath() { return originalImagePath; }
    public String getRawOcrText() { return rawOcrText; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public int getStopTimeMinutes() { return stopTimeMinutes; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setItems(String items) { this.items = items; }
    public void setPodImagePath1(String podImagePath1) { this.podImagePath1 = podImagePath1; }
    public void setPodImagePath2(String podImagePath2) { this.podImagePath2 = podImagePath2; }
    public void setPodImagePath3(String podImagePath3) { this.podImagePath3 = podImagePath3; }
    public void setSignatureImagePath(String signatureImagePath) { this.signatureImagePath = signatureImagePath; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setOriginalImagePath(String originalImagePath) { this.originalImagePath = originalImagePath; }
    public void setRawOcrText(String rawOcrText) { this.rawOcrText = rawOcrText; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setStatus(String status) { this.status = status; }
    public void setStopTimeMinutes(int stopTimeMinutes) { this.stopTimeMinutes = stopTimeMinutes; }

    // Utility methods for status checking
    public boolean isCompleted() {
        return "DELIVERED".equals(status) || "CANCELLED".equals(status);
    }

    public boolean isActive() {
        return "PENDING".equals(status) || "IN_TRANSIT".equals(status);
    }

    // Set completed status
    public void setCompleted(boolean completed) {
        this.status = completed ? "DELIVERED" : "PENDING";
    }
}
