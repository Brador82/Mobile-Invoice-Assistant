package com.mobileinvoice.ocr;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mobileinvoice.ocr.database.Invoice;
import com.mobileinvoice.ocr.databinding.ItemInvoiceBinding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {
    private List<Invoice> invoices = new ArrayList<>();
    private OnInvoiceClickListener listener;

    public interface OnInvoiceClickListener {
        void onViewDetails(Invoice invoice);
        void onDelete(Invoice invoice);
        void onOrderChanged(List<Invoice> reorderedList);
        void onDeliveryCompleteChanged(Invoice invoice, boolean isComplete);
    }

    public InvoiceAdapter(OnInvoiceClickListener listener) {
        this.listener = listener;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
        notifyDataSetChanged();
    }
    
    public List<Invoice> getInvoices() {
        return invoices;
    }
    
    /**
     * Move item from one position to another (for drag-and-drop)
     */
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(invoices, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(invoices, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }
    
    /**
     * Called when drag-and-drop is completed
     */
    public void onItemMoveComplete() {
        if (listener != null) {
            listener.onOrderChanged(new ArrayList<>(invoices));
        }
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInvoiceBinding binding = ItemInvoiceBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new InvoiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        holder.bind(invoices.get(position));
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    class InvoiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemInvoiceBinding binding;

        InvoiceViewHolder(ItemInvoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Invoice invoice) {
            binding.tvInvoiceNumber.setText(invoice.getInvoiceNumber() != null ?
                invoice.getInvoiceNumber() : "INV-" + invoice.getId());
            binding.tvCustomerName.setText(invoice.getCustomerName() != null ?
                invoice.getCustomerName() : "Unknown Customer");

            String address = invoice.getAddress() != null ? invoice.getAddress() : "No address";
            binding.tvAddress.setText(address);

            // Make address clickable to open in maps
            binding.tvAddress.setOnClickListener(v -> {
                if (invoice.getAddress() != null && !invoice.getAddress().isEmpty()) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(invoice.getAddress()));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
                        v.getContext().startActivity(mapIntent);
                    }
                }
            });

            binding.btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(invoice);
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(invoice);
                }
            });

            // Set checkbox state based on delivery status
            binding.cbDeliveryComplete.setOnCheckedChangeListener(null); // Clear listener first
            binding.cbDeliveryComplete.setChecked(invoice.isCompleted());

            // Handle checkbox toggle
            binding.cbDeliveryComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onDeliveryCompleteChanged(invoice, isChecked);
                }
            });
        }
    }
}
