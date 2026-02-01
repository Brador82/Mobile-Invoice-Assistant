package com.mobileinvoice.ocr;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.mobileinvoice.ocr.database.Invoice;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter for displaying delivery stops in the route optimization view
 * with drag-and-drop reordering support, collapsible completed section,
 * ETA display, priority controls, and stop time adjustment
 */
public class RouteStopAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Colors for the black/gold theme
    private static final int COLOR_GOLD = 0xFFD4AF37;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_LIGHT_GRAY = 0xFFCCCCCC;
    private static final int COLOR_GRAY = 0xFF999999;

    private List<RouteListItem> items = new ArrayList<>();
    private List<RouteOptimizer.RoutePoint> activeStops = new ArrayList<>();
    private List<RouteOptimizer.RoutePoint> completedStops = new ArrayList<>();
    private boolean completedExpanded = false;
    private Set<Integer> expandedPositions = new HashSet<>();
    private OnStopClickListener listener;
    private OnStartDragListener dragListener;
    private OnStopChangeListener changeListener;

    public interface OnStopClickListener {
        void onCallClick(RouteOptimizer.RoutePoint stop);
        void onNavigateClick(RouteOptimizer.RoutePoint stop);
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public interface OnStopChangeListener {
        void onMakeFirst(RouteOptimizer.RoutePoint stop);
        void onMakeLast(RouteOptimizer.RoutePoint stop);
        void onStopTimeChanged(RouteOptimizer.RoutePoint stop, int newTimeMinutes);
        void onRouteOrderChanged();
        void onCompletedChanged(RouteOptimizer.RoutePoint stop, boolean completed);
    }

    public RouteStopAdapter(OnStopClickListener listener, OnStartDragListener dragListener) {
        this.listener = listener;
        this.dragListener = dragListener;
    }

    public void setOnStopChangeListener(OnStopChangeListener listener) {
        this.changeListener = listener;
    }

    /**
     * Set stops and split into active and completed sections
     */
    public void setStops(List<RouteOptimizer.RoutePoint> stops) {
        activeStops.clear();
        completedStops.clear();
        expandedPositions.clear();

        for (RouteOptimizer.RoutePoint stop : stops) {
            if (stop.invoice.isCompleted()) {
                completedStops.add(stop);
            } else {
                activeStops.add(stop);
            }
        }

        updateStopNumbers();
        rebuildItemsList();
    }

    /**
     * Rebuild the heterogeneous items list with sections
     */
    private void rebuildItemsList() {
        items.clear();

        for (RouteOptimizer.RoutePoint stop : activeStops) {
            items.add(RouteListItem.createStopItem(stop));
        }

        if (!completedStops.isEmpty()) {
            items.add(RouteListItem.createHeaderItem(completedStops.size(), completedExpanded));

            if (completedExpanded) {
                for (RouteOptimizer.RoutePoint stop : completedStops) {
                    items.add(RouteListItem.createStopItem(stop));
                }
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Toggle the completed section visibility
     */
    public void toggleCompletedSection() {
        completedExpanded = !completedExpanded;
        rebuildItemsList();
    }

    /**
     * Get only active stops (for route calculation)
     */
    public List<RouteOptimizer.RoutePoint> getActiveStops() {
        return new ArrayList<>(activeStops);
    }

    /**
     * Get all stops (active + completed)
     */
    public List<RouteOptimizer.RoutePoint> getAllStops() {
        List<RouteOptimizer.RoutePoint> all = new ArrayList<>(activeStops);
        all.addAll(completedStops);
        return all;
    }

    /**
     * Legacy method for compatibility - returns active stops
     */
    public List<RouteOptimizer.RoutePoint> getStops() {
        return getActiveStops();
    }

    /**
     * Check if item at position is completed or a header
     */
    public boolean isCompletedItem(int position) {
        if (position < 0 || position >= items.size()) return false;
        RouteListItem item = items.get(position);
        if (item.getType() == RouteListItem.TYPE_HEADER) return true;
        return item.getRoutePoint().invoice.isCompleted();
    }

    /**
     * Move item from one position to another (only active items)
     */
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= items.size() ||
            toPosition < 0 || toPosition >= items.size()) {
            return false;
        }

        RouteListItem fromItem = items.get(fromPosition);
        RouteListItem toItem = items.get(toPosition);

        if (fromItem.getType() == RouteListItem.TYPE_HEADER ||
            toItem.getType() == RouteListItem.TYPE_HEADER) {
            return false;
        }

        if (fromItem.getRoutePoint().invoice.isCompleted() ||
            toItem.getRoutePoint().invoice.isCompleted()) {
            return false;
        }

        if (fromPosition >= activeStops.size() || toPosition >= activeStops.size()) {
            return false;
        }

        RouteOptimizer.RoutePoint movedItem = activeStops.remove(fromPosition);
        activeStops.add(toPosition, movedItem);

        updateStopNumbers();

        RouteListItem movedListItem = items.remove(fromPosition);
        items.add(toPosition, movedListItem);

        notifyItemMoved(fromPosition, toPosition);

        if (changeListener != null) {
            changeListener.onRouteOrderChanged();
        }

        return true;
    }

    /**
     * Update order indices after reordering (only for active stops)
     */
    private void updateStopNumbers() {
        int order = 1;
        for (RouteOptimizer.RoutePoint stop : activeStops) {
            stop.orderIndex = order++;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == RouteListItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_stop, parent, false);
            return new StopViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RouteListItem item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item, this::toggleCompletedSection);
        } else if (holder instanceof StopViewHolder) {
            RouteOptimizer.RoutePoint stop = item.getRoutePoint();
            boolean isCompleted = stop.invoice.isCompleted();
            boolean isExpanded = expandedPositions.contains(position);
            ((StopViewHolder) holder).bind(stop, listener, dragListener, changeListener,
                isCompleted, isExpanded, position, this::toggleItemExpansion);
        }
    }

    private void toggleItemExpansion(int position) {
        if (expandedPositions.contains(position)) {
            expandedPositions.remove(position);
        } else {
            expandedPositions.add(position);
        }
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder for section headers
     */
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvHeaderTitle;
        private final ImageView ivExpandIcon;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderTitle = itemView.findViewById(R.id.tvHeaderTitle);
            ivExpandIcon = itemView.findViewById(R.id.ivExpandIcon);
        }

        public void bind(RouteListItem item, Runnable onClickListener) {
            tvHeaderTitle.setText(item.getHeaderTitle() + " (" + item.getCompletedCount() + ")");

            int iconRes = item.isExpanded() ?
                android.R.drawable.arrow_up_float :
                android.R.drawable.arrow_down_float;
            ivExpandIcon.setImageResource(iconRes);

            itemView.setOnClickListener(v -> onClickListener.run());
        }
    }

    /**
     * Callback interface for item expansion
     */
    interface OnItemExpandListener {
        void onToggleExpand(int position);
    }

    /**
     * ViewHolder for delivery stops with all new features
     */
    static class StopViewHolder extends RecyclerView.ViewHolder {
        // Main content views
        private final LinearLayout mainContent;
        private final TextView tvStopNumber;
        private final TextView tvCustomerName;
        private final TextView tvAddress;
        private final TextView tvStopInfo;
        private final TextView tvETA;
        private final TextView tvStopTime;
        private final TextView tvPriorityBadge;
        private final ImageButton btnCall;
        private final ImageButton btnNavigate;
        private final ImageButton btnExpand;
        private final ImageView dragHandle;
        private final CheckBox cbCompleted;

        // Expandable panel views
        private final LinearLayout expandablePanel;
        private final Button btnMakeFirst;
        private final Button btnMakeLast;
        private final ImageButton btnDecreaseTime;
        private final ImageButton btnIncreaseTime;
        private final TextView tvStopTimeValue;
        private final Button btnTime5;
        private final Button btnTime10;
        private final Button btnTime15;
        private final Button btnTime30;
        private final Button btnTime45;

        public StopViewHolder(@NonNull View itemView) {
            super(itemView);

            // Main content
            mainContent = itemView.findViewById(R.id.mainContent);
            tvStopNumber = itemView.findViewById(R.id.tvStopNumber);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStopInfo = itemView.findViewById(R.id.tvStopInfo);
            tvETA = itemView.findViewById(R.id.tvETA);
            tvStopTime = itemView.findViewById(R.id.tvStopTime);
            tvPriorityBadge = itemView.findViewById(R.id.tvPriorityBadge);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
            btnExpand = itemView.findViewById(R.id.btnExpand);
            dragHandle = itemView.findViewById(R.id.dragHandle);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);

            // Expandable panel
            expandablePanel = itemView.findViewById(R.id.expandablePanel);
            btnMakeFirst = itemView.findViewById(R.id.btnMakeFirst);
            btnMakeLast = itemView.findViewById(R.id.btnMakeLast);
            btnDecreaseTime = itemView.findViewById(R.id.btnDecreaseTime);
            btnIncreaseTime = itemView.findViewById(R.id.btnIncreaseTime);
            tvStopTimeValue = itemView.findViewById(R.id.tvStopTimeValue);
            btnTime5 = itemView.findViewById(R.id.btnTime5);
            btnTime10 = itemView.findViewById(R.id.btnTime10);
            btnTime15 = itemView.findViewById(R.id.btnTime15);
            btnTime30 = itemView.findViewById(R.id.btnTime30);
            btnTime45 = itemView.findViewById(R.id.btnTime45);
        }

        public void bind(RouteOptimizer.RoutePoint stop, OnStopClickListener listener,
                        OnStartDragListener dragListener, OnStopChangeListener changeListener,
                        boolean isCompleted, boolean isExpanded, int position,
                        OnItemExpandListener expandListener) {

            Invoice invoice = stop.invoice;

            // Stop number
            tvStopNumber.setText(String.valueOf(stop.orderIndex));

            // Customer info - Gold name, white address
            tvCustomerName.setText(invoice.getCustomerName());
            tvAddress.setText(invoice.getAddress());

            // Items info
            String items = invoice.getItems();
            if (items != null && !items.isEmpty()) {
                tvStopInfo.setText("Items: " + items);
            } else {
                tvStopInfo.setText("No items specified");
            }

            // ETA
            String eta = stop.getFormattedETA();
            if (eta != null && !eta.equals("N/A")) {
                tvETA.setText("ETA: " + eta);
                tvETA.setVisibility(View.VISIBLE);
            } else {
                tvETA.setVisibility(View.GONE);
            }

            // Stop time
            tvStopTime.setText("(" + stop.stopTimeMinutes + " min stop)");

            // Priority badge
            String priorityText = stop.getPriorityText();
            if (priorityText != null) {
                tvPriorityBadge.setText(priorityText);
                tvPriorityBadge.setVisibility(View.VISIBLE);
            } else {
                tvPriorityBadge.setVisibility(View.GONE);
            }

            // Expandable panel state
            expandablePanel.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            btnExpand.setImageResource(isExpanded ?
                android.R.drawable.arrow_up_float :
                android.R.drawable.arrow_down_float);

            // Update stop time value in panel
            tvStopTimeValue.setText(stop.stopTimeMinutes + " minutes");

            // Apply completed/active styling
            if (isCompleted) {
                // Gray overlay effect - slightly dimmed but still readable
                itemView.setAlpha(0.7f);

                // Gray text colors for completed items
                int completedTextColor = 0xFF888888; // Medium gray
                tvCustomerName.setTextColor(completedTextColor);
                tvAddress.setTextColor(completedTextColor);
                tvStopInfo.setTextColor(completedTextColor);
                tvETA.setTextColor(completedTextColor);
                tvStopTime.setTextColor(completedTextColor);

                // Add strikethrough to all text fields
                tvCustomerName.setPaintFlags(tvCustomerName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvAddress.setPaintFlags(tvAddress.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvStopInfo.setPaintFlags(tvStopInfo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvETA.setPaintFlags(tvETA.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvStopTime.setPaintFlags(tvStopTime.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                // Gray out the stop number badge
                tvStopNumber.setTextColor(0xFF666666);

                // Hide drag handle and expand for completed (can't reorder completed)
                dragHandle.setVisibility(View.GONE);
                btnExpand.setVisibility(View.GONE);
            } else {
                // Active card styling - full colors
                itemView.setAlpha(1.0f);
                tvCustomerName.setTextColor(COLOR_GOLD);
                tvAddress.setTextColor(COLOR_WHITE);
                tvStopInfo.setTextColor(COLOR_LIGHT_GRAY);
                tvETA.setTextColor(0xFFFFD700); // Bright gold for ETA
                tvStopTime.setTextColor(COLOR_GRAY);
                tvStopNumber.setTextColor(0xFF1A1A1A); // Dark for contrast on gold badge

                // Remove strikethrough for active items
                tvCustomerName.setPaintFlags(tvCustomerName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvAddress.setPaintFlags(tvAddress.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvStopInfo.setPaintFlags(tvStopInfo.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvETA.setPaintFlags(tvETA.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvStopTime.setPaintFlags(tvStopTime.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

                dragHandle.setVisibility(View.VISIBLE);
                btnExpand.setVisibility(View.VISIBLE);
            }

            // Completed checkbox - always visible
            cbCompleted.setOnCheckedChangeListener(null); // Clear old listener first
            cbCompleted.setChecked(isCompleted);
            cbCompleted.setOnCheckedChangeListener((buttonView, checked) -> {
                if (changeListener != null) {
                    changeListener.onCompletedChanged(stop, checked);
                }
            });

            // Call button
            btnCall.setOnClickListener(v -> {
                if (listener != null) listener.onCallClick(stop);
            });

            // Navigate button
            btnNavigate.setOnClickListener(v -> {
                if (listener != null) listener.onNavigateClick(stop);
            });

            // Expand button
            btnExpand.setOnClickListener(v -> {
                if (expandListener != null) expandListener.onToggleExpand(position);
            });

            // Main content click also toggles expansion
            mainContent.setOnClickListener(v -> {
                if (!isCompleted && expandListener != null) {
                    expandListener.onToggleExpand(position);
                }
            });

            // Drag handle
            if (!isCompleted) {
                dragHandle.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN && dragListener != null) {
                        dragListener.onStartDrag(this);
                        return true;
                    }
                    return false;
                });
            } else {
                dragHandle.setOnTouchListener(null);
            }

            // Priority buttons
            btnMakeFirst.setOnClickListener(v -> {
                if (changeListener != null) {
                    changeListener.onMakeFirst(stop);
                }
            });

            btnMakeLast.setOnClickListener(v -> {
                if (changeListener != null) {
                    changeListener.onMakeLast(stop);
                }
            });

            // Time adjustment buttons
            btnDecreaseTime.setOnClickListener(v -> {
                int newTime = Math.max(5, stop.stopTimeMinutes - 5);
                if (changeListener != null) {
                    changeListener.onStopTimeChanged(stop, newTime);
                }
            });

            btnIncreaseTime.setOnClickListener(v -> {
                int newTime = Math.min(120, stop.stopTimeMinutes + 5);
                if (changeListener != null) {
                    changeListener.onStopTimeChanged(stop, newTime);
                }
            });

            // Time preset buttons
            setupTimePresetButton(btnTime5, 5, stop, changeListener);
            setupTimePresetButton(btnTime10, 10, stop, changeListener);
            setupTimePresetButton(btnTime15, 15, stop, changeListener);
            setupTimePresetButton(btnTime30, 30, stop, changeListener);
            setupTimePresetButton(btnTime45, 45, stop, changeListener);

            // Highlight selected time preset
            updateTimePresetSelection(stop.stopTimeMinutes);
        }

        private void setupTimePresetButton(Button button, int minutes,
                                           RouteOptimizer.RoutePoint stop,
                                           OnStopChangeListener changeListener) {
            button.setOnClickListener(v -> {
                if (changeListener != null) {
                    changeListener.onStopTimeChanged(stop, minutes);
                }
            });
        }

        private void updateTimePresetSelection(int selectedMinutes) {
            // Reset all buttons to unselected state
            setPresetButtonSelected(btnTime5, selectedMinutes == 5);
            setPresetButtonSelected(btnTime10, selectedMinutes == 10);
            setPresetButtonSelected(btnTime15, selectedMinutes == 15);
            setPresetButtonSelected(btnTime30, selectedMinutes == 30);
            setPresetButtonSelected(btnTime45, selectedMinutes == 45);
        }

        private void setPresetButtonSelected(Button button, boolean selected) {
            if (selected) {
                button.setBackgroundResource(R.drawable.button_time_preset_selected);
                button.setTextColor(COLOR_GOLD);
            } else {
                button.setBackgroundResource(R.drawable.button_time_preset);
                button.setTextColor(COLOR_LIGHT_GRAY);
            }
        }
    }
}
