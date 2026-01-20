package com.mobileinvoice.ocr;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mobileinvoice.ocr.database.Invoice;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for displaying delivery stops in the route optimization view
 * with drag-and-drop reordering support
 */
public class RouteStopAdapter extends RecyclerView.Adapter<RouteStopAdapter.StopViewHolder> {
    
    private List<RouteOptimizer.RoutePoint> stops = new ArrayList<>();
    private OnStopClickListener listener;
    private OnStartDragListener dragListener;
    
    public interface OnStopClickListener {
        void onCallClick(RouteOptimizer.RoutePoint stop);
        void onNavigateClick(RouteOptimizer.RoutePoint stop);
    }
    
    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
    
    public RouteStopAdapter(OnStopClickListener listener, OnStartDragListener dragListener) {
        this.listener = listener;
        this.dragListener = dragListener;
    }
    
    public void setStops(List<RouteOptimizer.RoutePoint> stops) {
        this.stops = stops;
        updateStopNumbers();
        notifyDataSetChanged();
    }
    
    public List<RouteOptimizer.RoutePoint> getStops() {
        return stops;
    }
    
    /**
     * Move item from one position to another
     */
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(stops, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(stops, i, i - 1);
            }
        }
        updateStopNumbers();
        notifyItemMoved(fromPosition, toPosition);
    }
    
    /**
     * Update order indices after reordering
     */
    private void updateStopNumbers() {
        for (int i = 0; i < stops.size(); i++) {
            stops.get(i).orderIndex = i + 1;
        }
    }
    
    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_route_stop, parent, false);
        return new StopViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        RouteOptimizer.RoutePoint stop = stops.get(position);
        holder.bind(stop, listener, dragListener);
    }
    
    @Override
    public int getItemCount() {
        return stops.size();
    }
    
    static class StopViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStopNumber;
        private final TextView tvCustomerName;
        private final TextView tvAddress;
        private final TextView tvStopInfo;
        private final ImageButton btnCall;
        private final ImageButton btnNavigate;
        private final ImageView dragHandle;
        
        public StopViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStopNumber = itemView.findViewById(R.id.tvStopNumber);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStopInfo = itemView.findViewById(R.id.tvStopInfo);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
            dragHandle = itemView.findViewById(R.id.dragHandle);
        }
        
        public void bind(RouteOptimizer.RoutePoint stop, OnStopClickListener listener, 
                        OnStartDragListener dragListener) {
            Invoice invoice = stop.invoice;
            
            // Stop number
            tvStopNumber.setText(String.valueOf(stop.orderIndex));
            
            // Customer info
            tvCustomerName.setText(invoice.getCustomerName());
            tvAddress.setText(invoice.getAddress());
            
            // Stop details
            String info = String.format("Items: %s", invoice.getItems());
            tvStopInfo.setText(info);
            
            // Call button
            btnCall.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCallClick(stop);
                }
            });
            
            // Navigate button
            btnNavigate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNavigateClick(stop);
                }
            });
            
            // Drag handle
            dragHandle.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN && dragListener != null) {
                    dragListener.onStartDrag(this);
                    return true;
                }
                return false;
            });
        }
    }
}
