package com.mobileinvoice.ocr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mobileinvoice.ocr.database.Invoice;
import com.mobileinvoice.ocr.database.InvoiceDatabase;
import com.mobileinvoice.ocr.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements InvoiceAdapter.OnInvoiceClickListener {
    private ActivityMainBinding binding;
    private List<Uri> selectedImages = new ArrayList<>();
    private InvoiceAdapter invoiceAdapter;
    private List<Invoice> invoices = new ArrayList<>();
    private InvoiceDatabase database;
    
    private ActivityResultLauncher<String> pickImagesLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        database = InvoiceDatabase.getInstance(this);
        
        setupActivityResultLaunchers();
        setupClickListeners();
        setupRecyclerViews();
        loadInvoicesFromDatabase();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadInvoicesFromDatabase();
    }
    
    private void setupActivityResultLaunchers() {
        // Image picker launcher
        pickImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    selectedImages.addAll(uris);
                    binding.tvStatus.setText("Selected " + selectedImages.size() + " images");
                    binding.btnProcessOCR.setEnabled(true);
                    binding.imagePreviewRecycler.setVisibility(android.view.View.VISIBLE);
                    Toast.makeText(this, "Selected " + uris.size() + " images", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        selectedImages.add(imageUri);
                        binding.tvStatus.setText("Selected " + selectedImages.size() + " images");
                        binding.btnProcessOCR.setEnabled(true);
                        binding.imagePreviewRecycler.setVisibility(android.view.View.VISIBLE);
                        Toast.makeText(this, "Photo captured!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        
        // Camera permission launcher
        requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void setupClickListeners() {
        // Upload button
        binding.btnUpload.setOnClickListener(v -> {
            pickImagesLauncher.launch("image/*");
        });
        
        // Camera button
        binding.btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
        
        // Process OCR button
        binding.btnProcessOCR.setOnClickListener(v -> {
            Toast.makeText(this, "Processing " + selectedImages.size() + " images with ML Kit...", 
                Toast.LENGTH_LONG).show();
            binding.progressBar.setVisibility(android.view.View.VISIBLE);
            binding.progressBar.setMax(selectedImages.size());
            binding.tvProgress.setVisibility(android.view.View.VISIBLE);
            binding.tvProgress.setText("Processing 0/" + selectedImages.size());
            
            // Process images with ML Kit (on-device) in background
            new Thread(() -> {
                OCRProcessorMLKit ocrProcessor = new OCRProcessorMLKit(this);
                
                for (int i = 0; i < selectedImages.size(); i++) {
                    final int index = i;
                    Uri imageUri = selectedImages.get(i);
                    
                    // Update progress on UI thread
                    runOnUiThread(() -> {
                        binding.progressBar.setProgress(index);
                        binding.tvProgress.setText("Processing " + index + "/" + selectedImages.size());
                    });
                    
                    // Perform OCR with ML Kit (on-device)
                    OCRProcessorMLKit.OCRResult result = ocrProcessor.processImage(imageUri);
                    
                    // Create invoice from OCR result
                    Invoice invoice = new Invoice();
                    boolean hasInvoiceNumber = result.invoiceNumber != null
                        && !result.invoiceNumber.trim().isEmpty()
                        && !result.invoiceNumber.equalsIgnoreCase("No invoice number");
                    invoice.setInvoiceNumber(hasInvoiceNumber
                        ? result.invoiceNumber.trim()
                        : "INV-" + String.format("%06d", invoices.size() + 1));
                    invoice.setCustomerName(result.customerName.isEmpty() ? 
                        "Unknown Customer" : result.customerName);
                    invoice.setAddress(result.address.isEmpty() ? 
                        "No address found" : result.address);
                    invoice.setPhone(result.phone.isEmpty() ? 
                        "No phone" : result.phone);
                    boolean hasItems = result.items != null
                        && !result.items.trim().isEmpty()
                        && !result.items.equalsIgnoreCase("No items detected");
                    invoice.setItems(hasItems ? result.items.trim() : "");
                    invoice.setRawOcrText(result.rawText);
                    invoice.setOriginalImagePath(imageUri.toString());
                    invoice.setTimestamp(System.currentTimeMillis());
                    
                    // Save to database
                    long newId = database.invoiceDao().insert(invoice);
                    invoice.setId((int) newId);
                    invoices.add(invoice);
                }
                
                // Clean up ML Kit resources
                ocrProcessor.close();
                
                // Update UI on completion
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(android.view.View.GONE);
                    binding.tvProgress.setVisibility(android.view.View.GONE);
                    binding.tvStatus.setText("Processing complete! " + invoices.size() + " invoices extracted.");
                    invoiceAdapter.setInvoices(invoices);
                    updateRecordCount();
                    Toast.makeText(this, "OCR processing completed", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });
        
        // Export button - now exports delivery cards
        binding.btnExportCSV.setOnClickListener(v -> {
            if (invoices.isEmpty()) {
                Toast.makeText(this, "No invoices to export", Toast.LENGTH_SHORT).show();
                return;
            }
            ExportHelper exportHelper = new ExportHelper(this);
            exportHelper.setExportCompleteCallback((exportFolder, count) -> {
                showCleanupDialog(count);
            });
            new Thread(() -> {
                List<Invoice> exportInvoices = database.invoiceDao().getAllInvoicesSync();
                runOnUiThread(() -> {
                    exportHelper.exportToCardFolders(exportInvoices);
                });
            }).start();
        });

        // Export as Markdown button
        binding.btnExportMarkdown.setOnClickListener(v -> {
            if (invoices.isEmpty()) {
                Toast.makeText(this, "No invoices to export", Toast.LENGTH_SHORT).show();
                return;
            }
            ExportHelper exportHelper = new ExportHelper(this);
            exportHelper.setExportCompleteCallback((exportFolder, count) -> {
                showCleanupDialog(count);
            });
            new Thread(() -> {
                List<Invoice> exportInvoices = database.invoiceDao().getAllInvoicesSync();
                runOnUiThread(() -> {
                    exportHelper.exportToMarkdown(exportInvoices);
                });
            }).start();
        });

        // Optimize Route button - Launch route optimization activity
        binding.btnOptimizeRoute.setOnClickListener(v -> {
            if (invoices.isEmpty()) {
                Toast.makeText(this, "No deliveries to route. Add invoices first.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, RouteMapActivity.class);
            startActivity(intent);
        });
    }
    
    private void setupRecyclerViews() {
        invoiceAdapter = new InvoiceAdapter(this);
        binding.invoiceRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.invoiceRecycler.setAdapter(invoiceAdapter);
        
        // Setup drag-and-drop for reordering
        ItemMoveCallback itemMoveCallback = new ItemMoveCallback(new ItemMoveCallback.ItemTouchHelperContract() {
            @Override
            public void onRowMoved(int fromPosition, int toPosition) {
                invoiceAdapter.onItemMove(fromPosition, toPosition);
            }
            
            @Override
            public void onRowSelected(RecyclerView.ViewHolder viewHolder) {
                // Add visual feedback when dragging starts
                viewHolder.itemView.setAlpha(0.7f);
                viewHolder.itemView.setScaleX(1.05f);
                viewHolder.itemView.setScaleY(1.05f);
            }
            
            @Override
            public void onRowClear(RecyclerView.ViewHolder viewHolder) {
                // Remove visual feedback when dragging ends
                viewHolder.itemView.setAlpha(1.0f);
                viewHolder.itemView.setScaleX(1.0f);
                viewHolder.itemView.setScaleY(1.0f);
                
                // Notify that order changed
                invoiceAdapter.onItemMoveComplete();
            }
        });
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemMoveCallback);
        itemTouchHelper.attachToRecyclerView(binding.invoiceRecycler);
        
        binding.imagePreviewRecycler.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        
        updateRecordCount();
    }
    
    private void createSampleInvoices(int count) {
        // Create sample invoices from OCR processing
        String[] sampleNames = {"John Smith", "Jane Doe", "Bob Johnson", "Alice Williams", "Mike Brown"};
        String[] sampleAddresses = {
            "123 Main St, New York, NY 10001",
            "456 Oak Ave, Los Angeles, CA 90001",
            "789 Pine Rd, Chicago, IL 60601",
            "321 Elm St, Houston, TX 77001",
            "654 Maple Dr, Phoenix, AZ 85001"
        };
        String[] samplePhones = {"555-0101", "555-0102", "555-0103", "555-0104", "555-0105"};
        
        for (int i = 0; i < count; i++) {
            Invoice invoice = new Invoice();
            invoice.setId(invoices.size() + 1);
            invoice.setInvoiceNumber("INV-" + String.format("%06d", invoices.size() + 1));
            invoice.setCustomerName(sampleNames[i % sampleNames.length]);
            invoice.setAddress(sampleAddresses[i % sampleAddresses.length]);
            invoice.setPhone(samplePhones[i % samplePhones.length]);
            invoice.setItems("Sample items");
            invoices.add(invoice);
        }
        
        invoiceAdapter.setInvoices(invoices);
        updateRecordCount();
    }
    
    private void updateRecordCount() {
        String countText = invoices.isEmpty() ? 
            "No invoices yet" : invoices.size() + " invoice(s)";
        binding.tvRecordCount.setText(countText);
    }
    
    private void loadInvoicesFromDatabase() {
        new Thread(() -> {
            List<Invoice> dbInvoices = database.invoiceDao().getAllInvoicesSync();
            runOnUiThread(() -> {
                invoices.clear();
                invoices.addAll(dbInvoices);
                invoiceAdapter.setInvoices(invoices);
                updateRecordCount();
            });
        }).start();
    }
    
    private void openCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        cameraLauncher.launch(intent);
    }

    @Override
    public void onViewDetails(Invoice invoice) {
        Intent intent = new Intent(this, InvoiceDetailActivity.class);
        intent.putExtra("invoice_id", invoice.getId());
        intent.putExtra("customer_name", invoice.getCustomerName());
        intent.putExtra("address", invoice.getAddress());
        intent.putExtra("phone", invoice.getPhone());
        startActivity(intent);
    }

    @Override
    public void onDelete(Invoice invoice) {
        new Thread(() -> {
            database.invoiceDao().delete(invoice);
            runOnUiThread(() -> {
                // Reload from database to keep UI in sync
                loadInvoicesFromDatabase();
                Toast.makeText(this, "Invoice deleted", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
    
    @Override
    public void onOrderChanged(List<Invoice> reorderedList) {
        // Update the main invoices list with new order
        invoices.clear();
        invoices.addAll(reorderedList);
        
        // Optional: Save order to database (you could add a displayOrder field to Invoice entity)
        Toast.makeText(this, "Order updated - Long press to drag invoices", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Show dialog asking if user wants to clear all data after export
     */
    private void showCleanupDialog(int exportedCount) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Export Complete")
            .setMessage(exportedCount + " invoices exported successfully to Downloads/MobileInvoiceOCR.\n\nWould you like to clear all data to start fresh?")
            .setPositiveButton("Clear All Data", (dialog, which) -> {
                clearAllData();
            })
            .setNegativeButton("Keep Data", (dialog, which) -> {
                Toast.makeText(this, "Data retained. Ready for next export.", Toast.LENGTH_SHORT).show();
            })
            .setIcon(android.R.drawable.ic_dialog_info)
            .show();
    }
    
    /**
     * Clear all invoice data from database and refresh UI
     */
    private void clearAllData() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Confirm Clear All")
            .setMessage("This will permanently delete all " + invoices.size() + " invoices from the app. Exported data in Downloads will NOT be affected.\n\nAre you sure?")
            .setPositiveButton("Yes, Clear All", (dialog, which) -> {
                new Thread(() -> {
                    // Delete all invoices from database
                    database.invoiceDao().deleteAll();
                    
                    runOnUiThread(() -> {
                        // Clear UI
                        invoices.clear();
                        invoiceAdapter.setInvoices(invoices);
                        updateRecordCount();
                        Toast.makeText(this, "All data cleared. Ready for new deliveries!", Toast.LENGTH_LONG).show();
                    });
                }).start();
            })
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
}
