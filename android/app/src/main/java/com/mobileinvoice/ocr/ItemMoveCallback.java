package com.mobileinvoice.ocr;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ItemTouchHelper callback for drag-and-drop reordering of invoices
 */
public class ItemMoveCallback extends ItemTouchHelper.Callback {
    private final ItemTouchHelperContract contract;
    
    public interface ItemTouchHelperContract {
        void onRowMoved(int fromPosition, int toPosition);
        void onRowSelected(RecyclerView.ViewHolder viewHolder);
        void onRowClear(RecyclerView.ViewHolder viewHolder);
    }
    
    public ItemMoveCallback(ItemTouchHelperContract contract) {
        this.contract = contract;
    }
    
    @Override
    public boolean isLongPressDragEnabled() {
        return true; // Enable long-press to start drag
    }
    
    @Override
    public boolean isItemViewSwipeEnabled() {
        return false; // Disable swipe (we have delete button)
    }
    
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, 
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        // Enable up and down dragging
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }
    
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                         @NonNull RecyclerView.ViewHolder viewHolder,
                         @NonNull RecyclerView.ViewHolder target) {
        contract.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }
    
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Not used - swipe disabled
    }
    
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof InvoiceAdapter.InvoiceViewHolder) {
                contract.onRowSelected(viewHolder);
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }
    
    @Override
    public void clearView(@NonNull RecyclerView recyclerView, 
                         @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (viewHolder instanceof InvoiceAdapter.InvoiceViewHolder) {
            contract.onRowClear(viewHolder);
        }
    }
}
