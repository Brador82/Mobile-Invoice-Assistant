package com.mobileinvoice.ocr;

import android.graphics.Canvas;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ItemTouchHelper for drag-and-drop reordering of delivery stops
 */
public class RouteItemTouchHelper extends ItemTouchHelper.Callback {

    private final RouteStopAdapter adapter;
    private final OnItemMovedListener listener;
    private boolean itemMoved = false; // Track if any moves occurred during this drag

    public interface OnItemMovedListener {
        void onItemMoved(int fromPosition, int toPosition);
    }

    public RouteItemTouchHelper(RouteStopAdapter adapter, OnItemMovedListener listener) {
        this.adapter = adapter;
        this.listener = listener;
    }
    
    @Override
    public boolean isLongPressDragEnabled() {
        // We handle drag start manually via the drag handle
        return false;
    }
    
    @Override
    public boolean isItemViewSwipeEnabled() {
        // Disable swipe to dismiss
        return false;
    }
    
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        // Prevent dragging headers
        if (viewHolder instanceof RouteStopAdapter.HeaderViewHolder) {
            return makeMovementFlags(0, 0); // No movement allowed
        }

        // Prevent dragging completed items
        if (viewHolder instanceof RouteStopAdapter.StopViewHolder) {
            int position = viewHolder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && adapter.isCompletedItem(position)) {
                return makeMovementFlags(0, 0); // No movement allowed
            }
        }

        // Allow vertical dragging for active items only
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }
    
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                         @NonNull RecyclerView.ViewHolder viewHolder,
                         @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();

        if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
            return false;
        }

        // Update adapter - returns false if move is not allowed
        boolean moved = adapter.onItemMove(fromPosition, toPosition);

        // Track that a move occurred (we'll notify listener when drag completes)
        if (moved) {
            itemMoved = true;
        }

        return moved;
    }
    
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Not used - swipe disabled
    }
    
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder != null) {
                // Add elevation when dragging
                viewHolder.itemView.setAlpha(0.7f);
                viewHolder.itemView.setScaleX(1.05f);
                viewHolder.itemView.setScaleY(1.05f);
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }
    
    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                         @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        // Reset visual state when drag ends
        viewHolder.itemView.setAlpha(1.0f);
        viewHolder.itemView.setScaleX(1.0f);
        viewHolder.itemView.setScaleY(1.0f);

        // Notify listener that reordering is complete (only if items were moved)
        // This updates the map once at the end, not on every intermediate move
        if (itemMoved && listener != null) {
            listener.onItemMoved(-1, -1); // -1, -1 signals "reorder complete"
            itemMoved = false;
        }
    }
    
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                           @NonNull RecyclerView.ViewHolder viewHolder,
                           float dX, float dY, int actionState, boolean isCurrentlyActive) {
        // Let the parent handle all drawing - don't manually set translationY
        // as it conflicts with ItemTouchHelper's built-in drag animation
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
