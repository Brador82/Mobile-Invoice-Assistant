package com.mobileinvoice.ocr;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
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
    private String deliveryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow auto-rotation for signature pad (portrait and landscape)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        // Keep screen on while signing
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding = ActivitySignatureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        signatureView = binding.signatureCanvas;

        // Get intent extras
        customerName = getIntent().getStringExtra("customer_name");
        invoiceId = getIntent().getLongExtra("invoice_id", -1);

        // Set delivery date (current date)
        deliveryDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date());
        binding.tvDeliveryDate.setText(deliveryDate);

        // Set customer name (auto-filled from invoice)
        if (customerName != null && !customerName.isEmpty()) {
            binding.tvCustomerName.setText(customerName);
        } else {
            binding.tvCustomerName.setText("Customer");
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

            // Save raw signature bitmap
            String signatureFilename = "signature_" + (invoiceId > 0 ? invoiceId + "_" : "") + timestamp + ".png";
            File signatureFile = new File(signaturesDir, signatureFilename);
            FileOutputStream fos = new FileOutputStream(signatureFile);
            signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            // Generate full delivery acceptance form with terms
            String formFilename = "delivery_form_" + (invoiceId > 0 ? invoiceId + "_" : "") + timestamp + ".png";
            File formFile = new File(signaturesDir, formFilename);
            Bitmap formBitmap = generateDeliveryForm(signatureBitmap);
            FileOutputStream formFos = new FileOutputStream(formFile);
            formBitmap.compress(Bitmap.CompressFormat.PNG, 100, formFos);
            formFos.flush();
            formFos.close();

            // Return result with all the data
            Intent resultIntent = new Intent();
            resultIntent.putExtra("signature_path", signatureFile.getAbsolutePath());
            resultIntent.putExtra("form_path", formFile.getAbsolutePath());
            resultIntent.putExtra("delivery_date", deliveryDate);
            resultIntent.putExtra("customer_name", customerName);
            resultIntent.putExtra("invoice_id", invoiceId);
            resultIntent.setData(Uri.fromFile(formFile));
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Delivery accepted & form saved!", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Error saving signature: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Generate a full delivery acceptance form image with terms and signature
     */
    private Bitmap generateDeliveryForm(Bitmap signatureBitmap) {
        // Create a document-sized bitmap (Letter size at 150 DPI: 1275 x 1650)
        int width = 1275;
        int height = 1650;
        Bitmap formBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(formBitmap);

        // Background
        canvas.drawColor(Color.WHITE);

        // Paints
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(Color.parseColor("#D4AF37")); // Gold
        headerPaint.setTextSize(36);
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(28);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#D4AF37"));
        labelPaint.setTextSize(22);
        labelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(Color.BLACK);
        valuePaint.setTextSize(22);

        Paint termsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        termsPaint.setColor(Color.DKGRAY);
        termsPaint.setTextSize(18);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#D4AF37"));
        linePaint.setStrokeWidth(3);

        int margin = 60;
        int y = margin;

        // Header
        String header = "DELIVERY ACCEPTANCE SIGN OFF";
        float headerWidth = headerPaint.measureText(header);
        canvas.drawText(header, (width - headerWidth) / 2, y + 36, headerPaint);
        y += 60;

        // Gold line
        canvas.drawLine(margin, y, width - margin, y, linePaint);
        y += 40;

        // Date and Customer info
        canvas.drawText("Delivery Date:", margin, y, labelPaint);
        canvas.drawText(deliveryDate, margin + 180, y, valuePaint);
        y += 35;

        canvas.drawText("Customer Name:", margin, y, labelPaint);
        canvas.drawText(customerName != null ? customerName : "N/A", margin + 200, y, valuePaint);
        y += 50;

        // Terms Section
        canvas.drawText("Terms of Acceptance:", margin, y, titlePaint);
        y += 35;

        String[] terms = {
            "1. Customer or person(s) over the age of 18 present upon delivery &",
            "   installation of appliances, and agree to accepting are installed correctly",
            "   without any harm to the home.",
            "",
            "2. Customer or person(s) over the age of 18 was present for delivery and",
            "   installation agrees that appliances, a test run/overview was provided,",
            "   no damages were noted & that they were installed correctly without any",
            "   damage arising to the house or appliance from the use of customers old",
            "   appliance hoses, and/or improperly maintained or damaged",
            "   faucets/drains/valves, etc."
        };

        for (String line : terms) {
            canvas.drawText(line, margin, y, termsPaint);
            y += 24;
        }
        y += 20;

        // Terms and Conditions
        canvas.drawLine(margin, y, width - margin, y, linePaint);
        y += 30;
        canvas.drawText("TERMS AND CONDITIONS", margin, y, titlePaint);
        y += 35;

        String[] conditions = {
            "Scratch and Dent Goods:",
            "• All delivery sales are final. There is no return or refund on scratch or",
            "  dent product, unless such item is non-repairable.",
            "• Purchaser is solely responsible for appliance(s) back to the store for",
            "  goods exchange.",
            "",
            "Warranty:",
            "• Warranty must begin within 30 days after delivery by contacting the",
            "  manufacturer or call 800-905-0443.",
            "• Customer is responsible for calling customer care as soon as any issue",
            "  arises within the warranty period.",
            "• After 30 days - Customer is still responsible for repair issues and",
            "  delivery/pickup, and labor.",
            "• Warranty does not cover negligence, power surges, or pest infestation."
        };

        for (String line : conditions) {
            canvas.drawText(line, margin, y, termsPaint);
            y += 24;
        }
        y += 30;

        // Signature section
        canvas.drawLine(margin, y, width - margin, y, linePaint);
        y += 30;
        canvas.drawText("Customer Signature:", margin, y, labelPaint);
        y += 20;

        // Draw signature bitmap (scaled to fit)
        int sigWidth = Math.min(signatureBitmap.getWidth(), width - 2 * margin);
        int sigHeight = Math.min(signatureBitmap.getHeight(), 200);
        float scale = Math.min((float) sigWidth / signatureBitmap.getWidth(),
                               (float) sigHeight / signatureBitmap.getHeight());
        int scaledWidth = (int) (signatureBitmap.getWidth() * scale);
        int scaledHeight = (int) (signatureBitmap.getHeight() * scale);

        Bitmap scaledSig = Bitmap.createScaledBitmap(signatureBitmap, scaledWidth, scaledHeight, true);
        canvas.drawBitmap(scaledSig, margin, y, null);
        y += scaledHeight + 10;

        // Signature line
        canvas.drawLine(margin, y, width / 2, y, linePaint);
        y += 25;

        // Date signed
        canvas.drawText("Date: " + deliveryDate, margin, y, valuePaint);
        y += 50;

        // Footer
        Paint footerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        footerPaint.setColor(Color.parseColor("#D4AF37"));
        footerPaint.setTextSize(16);
        String footer = "Please refer to www.ccsarms.com or call 800-905-0443 for warranty registration.";
        float footerWidth = footerPaint.measureText(footer);
        canvas.drawText(footer, (width - footerWidth) / 2, height - margin, footerPaint);

        return formBitmap;
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
