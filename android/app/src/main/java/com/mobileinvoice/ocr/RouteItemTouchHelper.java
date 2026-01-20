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
        // Allow vertical dragging only
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
        
        // Update adapter
        adapter.onItemMove(fromPosition, toPosition);
        
        // Notify listener
        if (listener != null) {
            listener.onItemMoved(fromPosition, toPosition);
        }
        
        return true;
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
    }
    
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                           @NonNull RecyclerView.ViewHolder viewHolder,
                           float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // Add shadow/elevation effect while dragging
            viewHolder.itemView.setTranslationY(dY);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
