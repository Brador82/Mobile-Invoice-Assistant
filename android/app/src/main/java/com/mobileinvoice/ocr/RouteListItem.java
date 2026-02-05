package com.mobileinvoice.ocr;

/**
 * Wrapper class for heterogeneous RecyclerView items in the route navigation list.
 * Supports two types: regular delivery stops and section headers.
 */
public class RouteListItem {
    public static final int TYPE_STOP = 0;
    public static final int TYPE_HEADER = 1;

    private int type;
    private RouteOptimizer.RoutePoint routePoint; // For TYPE_STOP
    private String headerTitle; // For TYPE_HEADER
    private int completedCount; // For TYPE_HEADER
    private boolean isExpanded; // For TYPE_HEADER

    // Private constructor - use factory methods
    private RouteListItem() {}

    /**
     * Factory method to create a stop item
     * @param point The route point containing delivery information
     * @return RouteListItem of TYPE_STOP
     */
    public static RouteListItem createStopItem(RouteOptimizer.RoutePoint point) {
        RouteListItem item = new RouteListItem();
        item.type = TYPE_STOP;
        item.routePoint = point;
        return item;
    }

    /**
     * Factory method to create a header item
     * @param count Number of completed deliveries
     * @param expanded Whether the section is expanded
     * @return RouteListItem of TYPE_HEADER
     */
    public static RouteListItem createHeaderItem(int count, boolean expanded) {
        RouteListItem item = new RouteListItem();
        item.type = TYPE_HEADER;
        item.headerTitle = "Completed";
        item.completedCount = count;
        item.isExpanded = expanded;
        return item;
    }

    // Getters
    public int getType() {
        return type;
    }

    public RouteOptimizer.RoutePoint getRoutePoint() {
        return routePoint;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    // Setter for expand state
    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
    }
}
