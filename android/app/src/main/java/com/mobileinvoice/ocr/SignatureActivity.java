package com.mobileinvoice.ocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignatureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        signatureView = binding.signatureCanvas;
        
        setupToolbar();
        setupButtons();
    }
    
    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupButtons() {
        binding.btnClear.setOnClickListener(v -> {
            signatureView.clear();
            Toast.makeText(this, "Signature cleared", Toast.LENGTH_SHORT).show();
        });
        
        binding.btnSave.setOnClickListener(v -> saveSignature());
    }
    
    private void saveSignature() {
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
            
            // Generate filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date());
            String filename = "signature_" + timestamp + ".png";
            File signatureFile = new File(signaturesDir, filename);
            
            // Save bitmap to file
            FileOutputStream fos = new FileOutputStream(signatureFile);
            signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            
            // Return result
            Intent resultIntent = new Intent();
            resultIntent.putExtra("signature_path", signatureFile.getAbsolutePath());
            resultIntent.setData(Uri.fromFile(signatureFile));
            setResult(RESULT_OK, resultIntent);
            
            Toast.makeText(this, "Signature saved!", Toast.LENGTH_SHORT).show();
            finish();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error saving signature: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }
}
