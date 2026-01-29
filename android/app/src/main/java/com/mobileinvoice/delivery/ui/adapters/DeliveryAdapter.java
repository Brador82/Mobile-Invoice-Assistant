package com.mobileinvoice.delivery.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mobileinvoice.delivery.data.entities.Delivery;
import com.mobileinvoice.delivery.models.DeliveryStatus;
import com.mobileinvoice.ocr.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Modern RecyclerView Adapter for Deliveries
 * 
 * Features:
 * - ListAdapter with DiffUtil for efficient updates
 * - Material Design 3 card layout
 * - Color-coded status indicators
 * - Swipe-to-action support
 * - Click and long-click handling
 */
public class DeliveryAdapter extends ListAdapter<Delivery, DeliveryAdapter.DeliveryViewHolder> {
    
    private OnDeliveryClickListener clickListener;
    private OnDeliveryLongClickListener longClickListener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    public DeliveryAdapter() {
        super(DIFF_CALLBACK);
    }
    
    /**
     * DiffUtil callback for efficient list updates
     * Only updates items that actually changed
     */
    private static final DiffUtil.ItemCallback<Delivery> DIFF_CALLBACK = new DiffUtil.ItemCallback<Delivery>() {
        @Override
        public boolean areItemsTheSame(@NonNull Delivery oldItem, @NonNull Delivery newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Delivery oldItem, @NonNull Delivery newItem) {
            return oldItem.getStatus() == newItem.getStatus()
                && oldItem.getRouteOrder() == newItem.getRouteOrder()
                && oldItem.getPriority() == newItem.getPriority()
                && equalsOrBothNull(oldItem.getCustomerName(), newItem.getCustomerName());
        }
        
        private boolean equalsOrBothNull(Object a, Object b) {
            if (a == null && b == null) return true;
            if (a == null || b == null) return false;
            return a.equals(b);
        }
    };
    
    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_delivery_card, parent, false);
        return new DeliveryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        Delivery delivery = getItem(position);
        holder.bind(delivery);
    }
    
    // ========== VIEW HOLDER ==========
    
    class DeliveryViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView tvCustomerName;
        private final TextView tvAddress;
        private final TextView tvTrackingNumber;
        private final TextView tvTimeWindow;
        private final TextView tvPackageCount;
        private final TextView tvStatus;
        private final ImageView ivPriority;
        private final ImageView ivPhone;
        private final ImageView ivNavigate;
        private final View statusIndicator;
        
        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_delivery);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvTrackingNumber = itemView.findViewById(R.id.tv_tracking_number);
            tvTimeWindow = itemView.findViewById(R.id.tv_time_window);
            tvPackageCount = itemView.findViewById(R.id.tv_package_count);
            tvStatus = itemView.findViewById(R.id.tv_status);
            ivPriority = itemView.findViewById(R.id.iv_priority);
            ivPhone = itemView.findViewById(R.id.iv_phone);
            ivNavigate = itemView.findViewById(R.id.iv_navigate);
            statusIndicator = itemView.findViewById(R.id.view_status_indicator);
            
            // Click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onDeliveryClick(getItem(position));
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onDeliveryLongClick(getItem(position));
                    return true;
                }
                return false;
            });
        }
        
        public void bind(Delivery delivery) {
            // Customer name
            tvCustomerName.setText(delivery.getCustomerName() != null 
                ? delivery.getCustomerName() 
                : "Unknown Customer");
            
            // Address
            tvAddress.setText(delivery.getFullAddress());
            
            // Tracking number
            tvTrackingNumber.setText(delivery.getTrackingNumber());
            
            // Time window
            if (delivery.getTimeWindowStart() != null) {
                tvTimeWindow.setText(delivery.getTimeWindow());
                tvTimeWindow.setVisibility(View.VISIBLE);
            } else {
                tvTimeWindow.setVisibility(View.GONE);
            }
            
            // Package count
            String packageText = delivery.getPackageCount() + " package" 
                + (delivery.getPackageCount() != 1 ? "s" : "");
            tvPackageCount.setText(packageText);
            
            // Status
            DeliveryStatus status = delivery.getStatus();
            tvStatus.setText(status.getDisplayName());
            tvStatus.setTextColor(Color.parseColor(status.getColorHex()));
            
            // Status indicator (colored bar on left)
            statusIndicator.setBackgroundColor(Color.parseColor(status.getColorHex()));
            
            // Priority indicator
            if (delivery.getPriority().getValue() >= 3) { // HIGH or URGENT
                ivPriority.setVisibility(View.VISIBLE);
                ivPriority.setColorFilter(Color.parseColor(delivery.getPriority().getColorHex()));
            } else {
                ivPriority.setVisibility(View.GONE);
            }
            
            // Overdue indicator
            if (delivery.isOverdue()) {
                cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Light red
            } else {
                cardView.setCardBackgroundColor(Color.WHITE);
            }
        }
    }
    
    // ========== CLICK LISTENERS ==========
    
    public interface OnDeliveryClickListener {
        void onDeliveryClick(Delivery delivery);
    }
    
    public interface OnDeliveryLongClickListener {
        void onDeliveryLongClick(Delivery delivery);
    }
    
    public void setOnDeliveryClickListener(OnDeliveryClickListener listener) {
        this.clickListener = listener;
    }
    
    public void setOnDeliveryLongClickListener(OnDeliveryLongClickListener listener) {
        this.longClickListener = listener;
    }
}
