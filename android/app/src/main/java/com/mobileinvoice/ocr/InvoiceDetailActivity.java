package com.mobileinvoice.ocr;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.mobileinvoice.ocr.database.Invoice;
import com.mobileinvoice.ocr.database.InvoiceDatabase;
import com.mobileinvoice.ocr.databinding.ActivityInvoiceDetailBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mobileinvoice.ocr.OCRProcessorMLKit;

public class InvoiceDetailActivity extends AppCompatActivity {
    private ActivityInvoiceDetailBinding binding;
    private InvoiceDatabase database;
    private Invoice currentInvoice;
    private int invoiceId;
    private String signaturePath;
    private String podImagePath1;
    private String podImagePath2;
    private String podImagePath3;
    private List<String> selectedItems = new ArrayList<>();
    
    private ActivityResultLauncher<Intent> signatureLauncher;
    private ActivityResultLauncher<Intent> podCameraLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    
    private static final String[] AVAILABLE_ITEMS = {
        "Washer", "Dryer", "Refrigerator", "Dishwasher", 
        "Freezer", "Range", "Oven", "Microwave", "Stove", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInvoiceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        database = InvoiceDatabase.getInstance(this);
        
        setupActivityResultLaunchers();
        loadInvoiceFromDatabase();
        setupClickListeners();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Auto-save when leaving the screen
        autoSaveInvoiceData();
    }
    
    @Override
    public void onBackPressed() {
        // Auto-save before going back
        autoSaveInvoiceData();
        super.onBackPressed();
    }
    
    private void setupActivityResultLaunchers() {
        // Signature launcher
        signatureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Use form_path (full delivery form with terms) for export
                    // Falls back to signature_path if form_path not available
                    String formPath = result.getData().getStringExtra("form_path");
                    signaturePath = formPath != null ? formPath : result.getData().getStringExtra("signature_path");
                    Uri signatureUri = result.getData().getData();

                    if (signatureUri != null) {
                        binding.ivSignature.setImageURI(signatureUri);
                        binding.ivSignature.setVisibility(View.VISIBLE);
                        binding.btnCaptureSignature.setText("Change Signature");
                        Toast.makeText(this, "Delivery form saved!", Toast.LENGTH_SHORT).show();
                    } else if (signaturePath != null) {
                        File sigFile = new File(signaturePath);
                        if (sigFile.exists()) {
                            binding.ivSignature.setImageURI(Uri.fromFile(sigFile));
                            binding.ivSignature.setVisibility(View.VISIBLE);
                            binding.btnCaptureSignature.setText("Change Signature");
                        }
                    }
                }
            }
        );
        
        // POD camera launcher
        podCameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        // Determine which POD slot to use (first empty slot)
                        if (podImagePath1 == null) {
                            podImagePath1 = saveImageToStorage(imageUri, "pod1_" + invoiceId + ".jpg");
                            if (podImagePath1 != null) {
                                binding.ivPod1.setImageURI(Uri.parse(podImagePath1));
                                binding.ivPod1.setVisibility(View.VISIBLE);
                                updatePODButtonText();
                                Toast.makeText(this, "POD photo 1 captured!", Toast.LENGTH_SHORT).show();
                            }
                        } else if (podImagePath2 == null) {
                            podImagePath2 = saveImageToStorage(imageUri, "pod2_" + invoiceId + ".jpg");
                            if (podImagePath2 != null) {
                                binding.ivPod2.setImageURI(Uri.parse(podImagePath2));
                                binding.ivPod2.setVisibility(View.VISIBLE);
                                updatePODButtonText();
                                Toast.makeText(this, "POD photo 2 captured!", Toast.LENGTH_SHORT).show();
                            }
                        } else if (podImagePath3 == null) {
                            podImagePath3 = saveImageToStorage(imageUri, "pod3_" + invoiceId + ".jpg");
                            if (podImagePath3 != null) {
                                binding.ivPod3.setImageURI(Uri.parse(podImagePath3));
                                binding.ivPod3.setVisibility(View.VISIBLE);
                                updatePODButtonText();
                                Toast.makeText(this, "POD photo 3 captured!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Maximum 3 POD photos. Tap a photo to replace it.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        );
        
        // Camera permission launcher
        requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openPODCamera();
                } else {
                    Toast.makeText(this, "Camera permission required for POD capture", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void loadInvoiceFromDatabase() {
        invoiceId = getIntent().getIntExtra("invoice_id", 0);
        
        if (invoiceId > 0) {
            new Thread(() -> {
                currentInvoice = database.invoiceDao().getInvoiceByIdSync(invoiceId);
                
                runOnUiThread(() -> {
                    if (currentInvoice != null) {
                        binding.etInvoiceNumber.setText(currentInvoice.getInvoiceNumber());
                        binding.etCustomerName.setText(currentInvoice.getCustomerName());
                        binding.etAddress.setText(currentInvoice.getAddress());
                        binding.etPhone.setText(currentInvoice.getPhone());
                        binding.etNotes.setText(currentInvoice.getNotes());
                        
                        // Load items
                        if (currentInvoice.getItems() != null && !currentInvoice.getItems().isEmpty()) {
                            // Use the improved parsing method
                            List<String> parsedItems = OCRProcessorMLKit.parseItemsList(currentInvoice.getItems());
                            selectedItems = new ArrayList<>(parsedItems);
                            updateSelectedItemsDisplay();
                        }
                        
                        // Load signature
                        if (currentInvoice.getSignatureImagePath() != null) {
                            signaturePath = currentInvoice.getSignatureImagePath();
                            File sigFile = new File(signaturePath);
                            if (sigFile.exists()) {
                                binding.ivSignature.setImageURI(Uri.fromFile(sigFile));
                                binding.ivSignature.setVisibility(View.VISIBLE);
                                binding.btnCaptureSignature.setText("Change Signature");
                            }
                        }
                        
                        // Load POD images
                        if (currentInvoice.getPodImagePath1() != null) {
                            podImagePath1 = currentInvoice.getPodImagePath1();
                            File podFile = new File(podImagePath1);
                            if (podFile.exists()) {
                                binding.ivPod1.setImageURI(Uri.fromFile(podFile));
                                binding.ivPod1.setVisibility(View.VISIBLE);
                            }
                        }
                        if (currentInvoice.getPodImagePath2() != null) {
                            podImagePath2 = currentInvoice.getPodImagePath2();
                            File podFile = new File(podImagePath2);
                            if (podFile.exists()) {
                                binding.ivPod2.setImageURI(Uri.fromFile(podFile));
                                binding.ivPod2.setVisibility(View.VISIBLE);
                            }
                        }
                        if (currentInvoice.getPodImagePath3() != null) {
                            podImagePath3 = currentInvoice.getPodImagePath3();
                            File podFile = new File(podImagePath3);
                            if (podFile.exists()) {
                                binding.ivPod3.setImageURI(Uri.fromFile(podFile));
                                binding.ivPod3.setVisibility(View.VISIBLE);
                            }
                        }
                        updatePODButtonText();
                    }
                });
            }).start();
        }
    }
    
    private void setupClickListeners() {
        binding.btnCaptureSignature.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignatureActivity.class);

            // Pass customer name and invoice ID for the delivery form
            if (currentInvoice != null) {
                intent.putExtra("customer_name", currentInvoice.getCustomerName());
                intent.putExtra("invoice_id", currentInvoice.getId());
            }

            // Pass the original invoice image path to show as background
            if (currentInvoice != null && currentInvoice.getOriginalImagePath() != null) {
                String imagePath = currentInvoice.getOriginalImagePath();
                android.util.Log.d("InvoiceDetail", "Passing image path to signature: " + imagePath);
                intent.putExtra("invoice_image_path", imagePath);

                // Grant URI permission if it's a content:// URI
                if (imagePath.startsWith("content://")) {
                    intent.setData(android.net.Uri.parse(imagePath));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    android.util.Log.d("InvoiceDetail", "Granted READ permission for content URI");
                }
            } else {
                android.util.Log.d("InvoiceDetail", "No invoice image path available");
                if (currentInvoice == null) {
                    android.util.Log.d("InvoiceDetail", "currentInvoice is null");
                } else {
                    android.util.Log.d("InvoiceDetail", "originalImagePath is null");
                }
            }
            signatureLauncher.launch(intent);
        });
        
        binding.btnCapturePOD.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                    == PackageManager.PERMISSION_GRANTED) {
                openPODCamera();
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
        
        // POD image click listeners - long press to replace
        binding.ivPod1.setOnLongClickListener(v -> {
            showPODOptionsDialog(1);
            return true;
        });
        
        binding.ivPod2.setOnLongClickListener(v -> {
            showPODOptionsDialog(2);
            return true;
        });
        
        binding.ivPod3.setOnLongClickListener(v -> {
            showPODOptionsDialog(3);
            return true;
        });
        
        binding.tvSelectedItems.setOnClickListener(v -> showItemSelectionDialog());
        
        // Address navigation hyperlink
        binding.tvAddressLink.setOnClickListener(v -> {
            String address = binding.etAddress.getText().toString().trim();
            if (!address.isEmpty()) {
                openAddressInMaps(address);
            } else {
                Toast.makeText(this, "No address available", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Phone call hyperlink
        binding.tvPhoneLink.setOnClickListener(v -> {
            String phone = binding.etPhone.getText().toString().trim();
            if (!phone.isEmpty()) {
                openPhoneDialer(phone);
            } else {
                Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnSave.setOnClickListener(v -> saveInvoiceData());
    }
    
    private void openPODCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        podCameraLauncher.launch(intent);
    }
    
    private AlertDialog currentItemDialog = null;

    private void showItemSelectionDialog() {
        // Dismiss any existing dialog first
        if (currentItemDialog != null && currentItemDialog.isShowing()) {
            currentItemDialog.dismiss();
        }

        // Create a custom dialog with better item management
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delivered Items");

        // Create a scrollable layout for items
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);
        layout.setBackgroundColor(0xFF2D2D2D); // Dark background

        // Add current items section
        if (!selectedItems.isEmpty()) {
            TextView currentHeader = new TextView(this);
            currentHeader.setText("Currently Selected Items:");
            currentHeader.setTextSize(16);
            currentHeader.setTextColor(0xFFD4AF37); // Gold color
            currentHeader.setPadding(0, 0, 0, 16);
            layout.addView(currentHeader);

            for (String item : selectedItems) {
                TextView itemView = new TextView(this);
                itemView.setText("• " + item + " (tap to remove)");
                itemView.setTextSize(14);
                itemView.setTextColor(0xFFFFFFFF); // White text
                itemView.setPadding(16, 12, 16, 12);
                itemView.setBackgroundColor(0xFF3D3D3D); // Darker gray
                itemView.setClickable(true);

                // Add margin between items
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 4, 0, 4);
                itemView.setLayoutParams(params);

                itemView.setOnClickListener(v -> {
                    selectedItems.remove(item);
                    updateSelectedItemsDisplay();
                    Toast.makeText(this, item + " removed", Toast.LENGTH_SHORT).show();
                    // Refresh dialog
                    showItemSelectionDialog();
                });
                layout.addView(itemView);
            }

            // Add divider
            View divider = new View(this);
            divider.setBackgroundColor(0xFF555555);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
            dividerParams.setMargins(0, 16, 0, 16);
            divider.setLayoutParams(dividerParams);
            layout.addView(divider);
        }

        // Add available items section
        TextView availableHeader = new TextView(this);
        availableHeader.setText("Available Items:");
        availableHeader.setTextSize(16);
        availableHeader.setTextColor(0xFFD4AF37); // Gold color
        availableHeader.setPadding(0, 0, 0, 16);
        layout.addView(availableHeader);

        for (String availableItem : AVAILABLE_ITEMS) {
            if (!selectedItems.contains(availableItem)) {
                TextView itemView = new TextView(this);
                itemView.setText("+ " + availableItem);
                itemView.setTextSize(14);
                itemView.setTextColor(0xFFCCCCCC); // Light gray text
                itemView.setPadding(16, 12, 16, 12);
                itemView.setBackgroundColor(0xFF1A1A1A); // Very dark background
                itemView.setClickable(true);

                // Add margin between items
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 4, 0, 4);
                itemView.setLayoutParams(params);

                itemView.setOnClickListener(v -> {
                    selectedItems.add(availableItem);
                    updateSelectedItemsDisplay();
                    Toast.makeText(this, availableItem + " added", Toast.LENGTH_SHORT).show();
                    // Refresh dialog
                    showItemSelectionDialog();
                });
                layout.addView(itemView);
            }
        }

        scrollView.addView(layout);
        builder.setView(scrollView);

        builder.setPositiveButton("Done", (dialog, which) -> {
            updateSelectedItemsDisplay();
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        currentItemDialog = builder.create();
        currentItemDialog.show();
    }
    
    private void updateSelectedItemsDisplay() {
        if (selectedItems.isEmpty()) {
            binding.tvSelectedItems.setText("Tap to select delivered items");
            binding.tvSelectedItems.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            // Sort items for consistent display
            selectedItems.sort(String::compareToIgnoreCase);

            // Create formatted display with bullet points
            StringBuilder displayText = new StringBuilder();
            displayText.append("Delivered Items (").append(selectedItems.size()).append("):\n");

            for (int i = 0; i < selectedItems.size(); i++) {
                displayText.append("• ").append(selectedItems.get(i));
                if (i < selectedItems.size() - 1) {
                    displayText.append("\n");
                }
            }

            binding.tvSelectedItems.setText(displayText.toString());
            binding.tvSelectedItems.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        }
    }
    
    /**
     * Update POD button text based on how many photos are captured
     */
    private void updatePODButtonText() {
        int podCount = 0;
        if (podImagePath1 != null) podCount++;
        if (podImagePath2 != null) podCount++;
        if (podImagePath3 != null) podCount++;
        
        if (podCount == 0) {
            binding.btnCapturePOD.setText("Add POD Photo");
        } else if (podCount < 3) {
            binding.btnCapturePOD.setText("Add POD Photo " + (podCount + 1));
        } else {
            binding.btnCapturePOD.setText("3 Photos Captured");
        }
    }
    
    /**
     * Show options dialog for POD photo management
     */
    private void showPODOptionsDialog(int podNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("POD Photo " + podNumber);
        builder.setItems(new String[]{"View Full Size", "Replace Photo", "Delete Photo"}, (dialog, which) -> {
            switch (which) {
                case 0: // View full size
                    viewPODFullSize(podNumber);
                    break;
                case 1: // Replace photo
                    replacePODPhoto(podNumber);
                    break;
                case 2: // Delete photo
                    deletePODPhoto(podNumber);
                    break;
            }
        });
        builder.show();
    }
    
    private void viewPODFullSize(int podNumber) {
        String podPath = null;
        if (podNumber == 1) podPath = podImagePath1;
        else if (podNumber == 2) podPath = podImagePath2;
        else if (podNumber == 3) podPath = podImagePath3;
        
        if (podPath != null) {
            File podFile = new File(podPath);
            if (podFile.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri photoUri = androidx.core.content.FileProvider.getUriForFile(
                    this, 
                    getPackageName() + ".fileprovider", 
                    podFile
                );
                intent.setDataAndType(photoUri, "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void replacePODPhoto(int podNumber) {
        // Clear the specific pod slot and open camera
        if (podNumber == 1) {
            podImagePath1 = null;
            binding.ivPod1.setVisibility(View.GONE);
        } else if (podNumber == 2) {
            podImagePath2 = null;
            binding.ivPod2.setVisibility(View.GONE);
        } else if (podNumber == 3) {
            podImagePath3 = null;
            binding.ivPod3.setVisibility(View.GONE);
        }
        updatePODButtonText();
        openPODCamera();
    }
    
    private void deletePODPhoto(int podNumber) {
        // Delete file and clear slot
        String podPath = null;
        if (podNumber == 1) {
            podPath = podImagePath1;
            podImagePath1 = null;
            binding.ivPod1.setVisibility(View.GONE);
        } else if (podNumber == 2) {
            podPath = podImagePath2;
            podImagePath2 = null;
            binding.ivPod2.setVisibility(View.GONE);
        } else if (podNumber == 3) {
            podPath = podImagePath3;
            podImagePath3 = null;
            binding.ivPod3.setVisibility(View.GONE);
        }
        
        if (podPath != null) {
            File podFile = new File(podPath);
            if (podFile.exists()) {
                podFile.delete();
            }
        }
        
        updatePODButtonText();
        Toast.makeText(this, "POD photo " + podNumber + " deleted", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Auto-save invoice data without closing the activity or showing toast
     * Called on onPause and back press to preserve data
     */
    private void autoSaveInvoiceData() {
        if (currentInvoice == null) return;
        
        // Update invoice with current values
        String invoiceNumber = binding.etInvoiceNumber.getText().toString().trim();
        String customerName = binding.etCustomerName.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        
        // Don't validate on auto-save, just save what we have
        currentInvoice.setInvoiceNumber(invoiceNumber);
        currentInvoice.setCustomerName(customerName);
        currentInvoice.setAddress(address);
        currentInvoice.setPhone(phone);
        currentInvoice.setNotes(binding.etNotes.getText().toString().trim());
        currentInvoice.setItems(String.join(",", selectedItems));
        currentInvoice.setSignatureImagePath(signaturePath);
        currentInvoice.setPodImagePath1(podImagePath1);
        currentInvoice.setPodImagePath2(podImagePath2);
        currentInvoice.setPodImagePath3(podImagePath3);
        
        // Save to database in background
        new Thread(() -> {
            database.invoiceDao().update(currentInvoice);
        }).start();
    }
    
    private void saveInvoiceData() {
        if (currentInvoice == null) {
            Toast.makeText(this, "Error: Invoice not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate required fields
        String invoiceNumber = binding.etInvoiceNumber.getText().toString().trim();
        String customerName = binding.etCustomerName.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        
        if (invoiceNumber.isEmpty()) {
            binding.etInvoiceNumber.setError("Invoice number is required");
            binding.etInvoiceNumber.requestFocus();
            return;
        }
        
        if (customerName.isEmpty()) {
            binding.etCustomerName.setError("Customer name is required");
            binding.etCustomerName.requestFocus();
            return;
        }
        
        // Update invoice with current values
        currentInvoice.setInvoiceNumber(invoiceNumber);
        currentInvoice.setCustomerName(customerName);
        currentInvoice.setAddress(address);
        currentInvoice.setPhone(phone);
        currentInvoice.setNotes(binding.etNotes.getText().toString().trim());
        currentInvoice.setItems(String.join(",", selectedItems));
        currentInvoice.setSignatureImagePath(signaturePath);
        currentInvoice.setPodImagePath1(podImagePath1);
        currentInvoice.setPodImagePath2(podImagePath2);
        currentInvoice.setPodImagePath3(podImagePath3);
        
        // Save to database
        new Thread(() -> {
            database.invoiceDao().update(currentInvoice);
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Invoice saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
    
    private String saveImageToStorage(Uri imageUri, String filename) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;
            
            File storageDir = new File(getFilesDir(), "images");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            
            File imageFile = new File(storageDir, filename);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            outputStream.close();
            inputStream.close();
            
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    
    private void openAddressInMaps(String address) {
        try {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback to any maps app
                mapIntent.setPackage(null);
                startActivity(mapIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open maps application", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openPhoneDialer(String phone) {
        try {
            // Clean phone number for dialing
            String cleanPhone = phone.replaceAll("[^\\d+]", "");
            Uri phoneUri = Uri.parse("tel:" + cleanPhone);
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, phoneUri);
            startActivity(dialIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open phone dialer", Toast.LENGTH_SHORT).show();
        }
    }
}
