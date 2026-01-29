package com.mobileinvoice.ocr;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mobileinvoice.ocr.database.Invoice;
import com.mobileinvoice.ocr.database.InvoiceDatabase;
import com.mobileinvoice.ocr.databinding.ActivityMainBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
                    // Rotate images if needed and add to list
                    for (Uri uri : uris) {
                        Uri correctedUri = rotateImageIfNeeded(uri);
                        selectedImages.add(correctedUri);
                    }
                    updateImageCount();
                    binding.btnClearQueue.setVisibility(android.view.View.VISIBLE);
                    binding.imagePreviewRecycler.setVisibility(android.view.View.VISIBLE);
                    Toast.makeText(this, "Selected " + uris.size() + " images (rotation corrected)", Toast.LENGTH_SHORT).show();
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
                        // Rotate image if needed before adding to list
                        Uri correctedUri = rotateImageIfNeeded(imageUri);
                        selectedImages.add(correctedUri);
                        updateImageCount();
                        binding.btnClearQueue.setVisibility(android.view.View.VISIBLE);
                        binding.imagePreviewRecycler.setVisibility(android.view.View.VISIBLE);
                        Toast.makeText(this, "Photo captured and corrected!", Toast.LENGTH_SHORT).show();
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
        
        // Clear Queue button - manually clear selected images before processing
        binding.btnClearQueue.setOnClickListener(v -> {
            if (!selectedImages.isEmpty()) {
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Clear Image Queue?")
                    .setMessage("This will clear " + selectedImages.size() + " selected image(s) that haven't been processed yet.")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        selectedImages.clear();
                        updateImageCount();
                        binding.btnClearQueue.setVisibility(android.view.View.GONE);
                        Toast.makeText(this, "Image queue cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
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
                    
                    // Clear the selected images queue to prevent duplicates
                    selectedImages.clear();
                    updateImageCount();
                    
                    Toast.makeText(this, "OCR processing completed", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });
        
        // Export button - now exports as HTML ZIP (recommended for delivery drivers)
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
                    exportHelper.exportToHTMLZip(exportInvoices);
                });
            }).start();
        });

        // Export as Markdown button (legacy)
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
    
    private void updateImageCount() {
        if (selectedImages.isEmpty()) {
            binding.tvStatus.setText("Ready. Upload or capture invoice images.");
            binding.btnProcessOCR.setEnabled(false);
        } else {
            binding.tvStatus.setText("Selected " + selectedImages.size() + " images");
            binding.btnProcessOCR.setEnabled(true);
        }
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
    
    /**
     * Rotate image based on EXIF orientation data
     * Returns a new URI pointing to the corrected image
     */
    private Uri rotateImageIfNeeded(Uri sourceUri) {
        try {
            // Load bitmap from URI
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (originalBitmap == null) {
                android.util.Log.e("MainActivity", "Failed to load bitmap from URI");
                return sourceUri; // Return original if we can't load it
            }
            
            int rotation = 0;
            
            // FORCE rotate landscape images to portrait (for invoice OCR)
            if (originalBitmap.getWidth() > originalBitmap.getHeight()) {
                android.util.Log.d("MainActivity", "Image is landscape, forcing 90° CCW rotation for invoices");
                rotation = -90; // Negative = counterclockwise
            } else {
                // Read EXIF orientation for portrait images
                InputStream exifStream = getContentResolver().openInputStream(sourceUri);
                ExifInterface exif = new ExifInterface(exifStream);
                int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                );
                exifStream.close();
                
                // Calculate rotation angle from EXIF
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotation = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotation = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotation = 270;
                        break;
                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        // No rotation needed
                        break;
                }
            }
            
            // If no rotation needed, return original
            if (rotation == 0) {
                android.util.Log.d("MainActivity", "Image already properly oriented");
                originalBitmap.recycle();
                return sourceUri;
            }
            
            android.util.Log.d("MainActivity", "Rotating image by " + rotation + " degrees");
            
            // Apply rotation
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedBitmap = Bitmap.createBitmap(
                originalBitmap, 0, 0,
                originalBitmap.getWidth(), originalBitmap.getHeight(),
                matrix, true
            );
            originalBitmap.recycle();
            
            // Save rotated bitmap to a new file in app's private storage
            File imagesDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Invoices");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File rotatedFile = new File(imagesDir, "IMG_" + timestamp + "_corrected.jpg");
            
            FileOutputStream fos = new FileOutputStream(rotatedFile);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            fos.close();
            rotatedBitmap.recycle();
            
            android.util.Log.d("MainActivity", "Saved rotated image to: " + rotatedFile.getAbsolutePath());
            
            // Return URI for the corrected image
            return Uri.fromFile(rotatedFile);
            
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error rotating image: " + e.getMessage());
            e.printStackTrace();
            return sourceUri; // Return original if rotation fails
        }
    }
}
