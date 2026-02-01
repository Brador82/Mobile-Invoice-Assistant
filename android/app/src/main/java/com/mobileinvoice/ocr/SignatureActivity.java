package com.mobileinvoice.ocr;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.mobileinvoice.ocr.databinding.ActivitySignatureBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignatureActivity extends AppCompatActivity {
    private ActivitySignatureBinding binding;
    private SignatureView signatureView;
    private String customerName;
    private long invoiceId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Force landscape orientation for signature pad
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Keep screen on while signing
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding = ActivitySignatureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        signatureView = binding.signatureCanvas;

        // Get intent extras
        customerName = getIntent().getStringExtra("customer_name");
        invoiceId = getIntent().getLongExtra("invoice_id", -1);
        String deliveredBy = getIntent().getStringExtra("delivered_by");

        // Set delivery date (current date)
        String currentDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date());
        binding.tvDeliveryDate.setText(currentDate);

        // Set customer name
        if (customerName != null && !customerName.isEmpty()) {
            binding.tvCustomerName.setText(customerName);
        } else {
            binding.tvCustomerName.setText("Customer");
        }

        // Set delivered by if provided (remember from last delivery)
        if (deliveredBy != null && !deliveredBy.isEmpty()) {
            binding.etDeliveredBy.setText(deliveredBy);
        }

        setupButtons();
    }

    private void setupButtons() {
        binding.btnClear.setOnClickListener(v -> {
            signatureView.clear();
            Toast.makeText(this, "Signature cleared", Toast.LENGTH_SHORT).show();
        });

        binding.btnSave.setOnClickListener(v -> saveSignature());
    }

    private void saveSignature() {
        // Validate signature
        if (!signatureView.hasSignature()) {
            Toast.makeText(this, "Please sign before saving", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate delivered by
        String deliveredBy = binding.etDeliveredBy.getText().toString().trim();
        if (deliveredBy.isEmpty()) {
            Toast.makeText(this, "Please enter driver name", Toast.LENGTH_SHORT).show();
            binding.etDeliveredBy.requestFocus();
            return;
        }

        Bitmap signatureBitmap = signatureView.getSignatureBitmap();

        if (signatureBitmap == null) {
            Toast.makeText(this, "No signature to save", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create signatures directory
            File signaturesDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "Signatures");
            if (!signaturesDir.exists()) {
                signaturesDir.mkdirs();
            }

            // Generate filename with timestamp and invoice ID
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date());
            String filename = "signature_" + (invoiceId > 0 ? invoiceId + "_" : "") + timestamp + ".png";
            File signatureFile = new File(signaturesDir, filename);

            // Save bitmap to file
            FileOutputStream fos = new FileOutputStream(signatureFile);
            signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            // Return result with all the data
            Intent resultIntent = new Intent();
            resultIntent.putExtra("signature_path", signatureFile.getAbsolutePath());
            resultIntent.putExtra("delivered_by", deliveredBy);
            resultIntent.putExtra("delivery_date", binding.tvDeliveryDate.getText().toString());
            resultIntent.putExtra("customer_name", customerName);
            resultIntent.putExtra("invoice_id", invoiceId);
            resultIntent.setData(Uri.fromFile(signatureFile));
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Delivery accepted & signature saved!", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Error saving signature: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Confirm if user wants to leave without saving
        if (signatureView.hasSignature()) {
            new AlertDialog.Builder(this)
                .setTitle("Discard Signature?")
                .setMessage("You have an unsigned delivery. Are you sure you want to go back?")
                .setPositiveButton("Discard", (d, w) -> super.onBackPressed())
                .setNegativeButton("Cancel", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
}
