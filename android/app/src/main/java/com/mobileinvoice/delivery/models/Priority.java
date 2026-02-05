package com.mobileinvoice.delivery.models;

/**
 * Delivery Priority Levels
 * Used for sorting and highlighting urgent deliveries
 */
public enum Priority {
    LOW(1, "Low", "#4CAF50"),
    NORMAL(2, "Normal", "#2196F3"),
    HIGH(3, "High", "#FF9800"),
    URGENT(4, "Urgent", "#F44336");

    private final int value;
    private final String displayName;
    private final String colorHex;

    Priority(int value, String displayName, String colorHex) {
        this.value = value;
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }
}
