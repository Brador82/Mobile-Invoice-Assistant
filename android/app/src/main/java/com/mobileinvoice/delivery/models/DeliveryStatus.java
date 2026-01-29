package com.mobileinvoice.delivery.models;

/**
 * Delivery Status Enum
 * Represents the lifecycle of a delivery from assignment to completion
 */
public enum DeliveryStatus {
    PENDING("Pending", "#FFA726"),           // Orange - Not yet started
    IN_TRANSIT("In Transit", "#42A5F5"),     // Blue - Currently being delivered
    DELIVERED("Delivered", "#66BB6A"),       // Green - Successfully delivered
    FAILED("Failed", "#EF5350"),             // Red - Delivery attempt failed
    RESCHEDULED("Rescheduled", "#AB47BC"),   // Purple - Rescheduled for later
    CANCELLED("Cancelled", "#78909C");       // Grey - Cancelled by customer/system

    private final String displayName;
    private final String colorHex;

    DeliveryStatus(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public boolean isCompleted() {
        return this == DELIVERED || this == CANCELLED;
    }

    public boolean isActive() {
        return this == PENDING || this == IN_TRANSIT;
    }
}
