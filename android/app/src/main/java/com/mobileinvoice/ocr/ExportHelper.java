package com.mobileinvoice.ocr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.mobileinvoice.ocr.database.Invoice;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportHelper {
    private final Context context;
    private ExportCompleteCallback callback;
    
    public interface ExportCompleteCallback {
        void onExportComplete(File exportFolder, int invoiceCount);
    }
    
    public ExportHelper(Context context) {
        this.context = context;
    }
    
    public void setExportCompleteCallback(ExportCompleteCallback callback) {
        this.callback = callback;
    }

    /**
     * Export invoices to Markdown format, including images for signature and up to three POD photos
     */
    public void exportToMarkdown(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            Toast.makeText(context, "No invoices to export", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder md = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        int cardNum = 1;
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File exportDir = new File(downloadsDir, "MobileInvoiceOCR");
        if (!exportDir.exists()) exportDir.mkdirs();
        for (Invoice invoice : invoices) {
            md.append("---\n");
            md.append("### Delivery Card " + cardNum + "\n\n");
            md.append("**Invoice Number:** " + escapeMD(invoice.getInvoiceNumber()) + "  \n");
            md.append("**Customer Name:** " + escapeMD(invoice.getCustomerName()) + "  \n");
            md.append("**Address:** " + escapeMD(invoice.getAddress()) + "  \n");
            md.append("**Phone:** " + escapeMD(invoice.getPhone()) + "  \n\n");
            md.append("**Timestamp:** " + escapeMD(dateFormat.format(new Date(invoice.getTimestamp()))) + "  \n");
            md.append("**Items:** " + escapeMD(invoice.getItems()) + "  \n");
            // POD Images
            String[] podPaths = { invoice.getPodImagePath1(), invoice.getPodImagePath2(), invoice.getPodImagePath3() };
            for (int i = 0; i < podPaths.length; i++) {
                String podPath = podPaths[i];
                if (podPath != null && !podPath.isEmpty()) {
                    File podSrc = new File(podPath);
                    File podDest = new File(exportDir, "pod_photo_" + cardNum + "_" + (i+1) + ".jpg");
                    try { copyImageFile(podSrc, podDest); } catch (Exception e) { e.printStackTrace(); }
                    md.append("**POD Photo " + (i+1) + ":** ![](pod_photo_" + cardNum + "_" + (i+1) + ".jpg)  \n");
                }
            }
            // Signature
            if (invoice.getSignatureImagePath() != null && !invoice.getSignatureImagePath().isEmpty()) {
                File sigSrc = new File(invoice.getSignatureImagePath());
                File sigDest = new File(exportDir, "signature_" + cardNum + ".jpg");
                try { copyImageFile(sigSrc, sigDest); } catch (Exception e) { e.printStackTrace(); }
                md.append("**Signature:** ![](signature_" + cardNum + ".jpg)  \n");
            }
            // Original Invoice Image
            if (invoice.getOriginalImagePath() != null && !invoice.getOriginalImagePath().isEmpty()) {
                File invSrc = new File(invoice.getOriginalImagePath());
                File invDest = new File(exportDir, "invoice_" + cardNum + ".jpg");
                try { copyImageFile(invSrc, invDest); } catch (Exception e) { e.printStackTrace(); }
                md.append("**Invoice Image:** ![](invoice_" + cardNum + ".jpg)  \n");
            }
            md.append("**Notes:** " + escapeMD(invoice.getNotes()) + "  \n");
            md.append("\n---\n\n");
            cardNum++;
        }
        // Save to file
        String filename = "invoices_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".md";
        File file = saveToFile(exportDir, filename, md.toString());
        if (file != null) {
            Toast.makeText(context, "✓ Exported " + invoices.size() + " invoices to:\nDownloads/MobileInvoiceOCR/" + filename, Toast.LENGTH_LONG).show();
            
            // Trigger callback for cleanup dialog
            if (callback != null) {
                callback.onExportComplete(exportDir, invoices.size());
            }
        }
    }

    // Helper for Markdown escaping
    private String escapeMD(String value) {
        if (value == null) return "";
        return value.replace("|", "\\|").replace("*", "\\*").replace("_", "\\_");
    }
    
    /**
     * Export invoices to folder-based card format in Downloads folder
     */
    public void exportToCardFolders(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            Toast.makeText(context, "No invoices to export", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create parent folder with date: "Deliveries (MM/DD/YYYY)"
        String dateFolder = new SimpleDateFormat("'Deliveries ('MM/dd/yyyy')'", Locale.US).format(new Date());
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File mainExportDir = new File(downloadsDir, "MobileInvoiceOCR/" + dateFolder);
        
        if (!mainExportDir.exists()) {
            mainExportDir.mkdirs();
        }
        
        int successCount = 0;
        
        for (Invoice invoice : invoices) {
            try {
                // Create individual card folder
                String cardFolderName = createCardFolderName(invoice);
                File cardDir = new File(mainExportDir, cardFolderName);
                cardDir.mkdirs();
                
                // Save invoice data as JSON
                saveInvoiceDataToCard(cardDir, invoice);
                
                // Copy associated images
                copyImagesToCard(cardDir, invoice);
                
                successCount++;
                
            } catch (Exception e) {
                e.printStackTrace();
                // Continue with other invoices
            }
        }
        
        if (successCount > 0) {
            // Create a summary file
            File summaryFile = createExportSummary(mainExportDir, invoices, successCount);
            
            // Show success message with location
            Toast.makeText(context, "✓ Exported " + successCount + " delivery cards to:\nDownloads/MobileInvoiceOCR/" + mainExportDir.getName() + "\n\nOpen Files app to share", Toast.LENGTH_LONG).show();
            
            // Trigger callback for cleanup dialog
            if (callback != null) {
                callback.onExportComplete(mainExportDir, successCount);
            }
        } else {
            Toast.makeText(context, "Failed to export any delivery cards", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Export invoices to Excel-compatible TSV format
     */
    public void exportToExcel(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            Toast.makeText(context, "No invoices to export", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder tsv = new StringBuilder();
        
        // TSV Header
        tsv.append("Invoice #\tCustomer Name\tAddress\tPhone\tItems\tPOD Image\tSignature\tNotes\tTimestamp\n");
        
        // TSV Data
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        for (Invoice invoice : invoices) {
            tsv.append(escapeTSV(invoice.getInvoiceNumber())).append("\t");
            tsv.append(escapeTSV(invoice.getCustomerName())).append("\t");
            tsv.append(escapeTSV(invoice.getAddress())).append("\t");
            tsv.append(escapeTSV(invoice.getPhone())).append("\t");
            tsv.append(escapeTSV(invoice.getItems())).append("\t");
            tsv.append(escapeTSV((invoice.getPodImagePath1() != null && !invoice.getPodImagePath1().isEmpty()) || (invoice.getPodImagePath2() != null && !invoice.getPodImagePath2().isEmpty()) || (invoice.getPodImagePath3() != null && !invoice.getPodImagePath3().isEmpty()) ? "Yes" : "No")).append("\t");
            tsv.append(escapeTSV(invoice.getSignatureImagePath() != null ? "Yes" : "No")).append("\t");
            tsv.append(escapeTSV(invoice.getNotes())).append("\t");
            tsv.append(escapeTSV(dateFormat.format(new Date(invoice.getTimestamp())))).append("\n");
        }
        
        // Save to file
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File exportDir = new File(downloadsDir, "MobileInvoiceOCR");
        String filename = "invoices_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".xls";
        File file = saveToFile(exportDir, filename, tsv.toString());
        
        if (file != null) {
            Toast.makeText(context, "✓ Exported " + invoices.size() + " invoices to:\nDownloads/MobileInvoiceOCR/" + filename + "\n\nOpen Files app to share", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Export invoices to JSON format with full data
     */
    public void exportToJSON(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            Toast.makeText(context, "No invoices to export", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            JSONObject root = new JSONObject();
            root.put("export_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
            root.put("total_invoices", invoices.size());
            
            JSONArray invoicesArray = new JSONArray();
            
            for (Invoice invoice : invoices) {
                JSONObject invoiceObj = new JSONObject();
                invoiceObj.put("id", invoice.getId());
                invoiceObj.put("invoice_number", invoice.getInvoiceNumber());
                invoiceObj.put("customer_name", invoice.getCustomerName());
                invoiceObj.put("address", invoice.getAddress());
                invoiceObj.put("phone", invoice.getPhone());
                invoiceObj.put("items", invoice.getItems());
                
                // POD Images
                JSONArray podImages = new JSONArray();
                if (invoice.getPodImagePath1() != null) podImages.put(invoice.getPodImagePath1());
                if (invoice.getPodImagePath2() != null) podImages.put(invoice.getPodImagePath2());
                if (invoice.getPodImagePath3() != null) podImages.put(invoice.getPodImagePath3());
                invoiceObj.put("pod_image_paths", podImages);
                
                invoiceObj.put("signature_image_path", invoice.getSignatureImagePath());
                invoiceObj.put("notes", invoice.getNotes());
                invoiceObj.put("original_image_path", invoice.getOriginalImagePath());
                invoiceObj.put("raw_ocr_text", invoice.getRawOcrText());
                invoiceObj.put("timestamp", invoice.getTimestamp());
                
                invoicesArray.put(invoiceObj);
            }
            
            root.put("invoices", invoicesArray);
            
            // Save to file
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File exportDir = new File(downloadsDir, "MobileInvoiceOCR");
            String filename = "invoices_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".json";
            File file = saveToFile(exportDir, filename, root.toString(4)); // Pretty print with indent
            
            if (file != null) {
                Toast.makeText(context, "✓ Exported " + invoices.size() + " invoices to:\nDownloads/MobileInvoiceOCR/" + filename + "\n\nOpen Files app to share", Toast.LENGTH_LONG).show();
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error creating JSON export", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Export summary statistics
     */
    public String getExportSummary(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            return "No invoices available for export";
        }
        
        int totalInvoices = invoices.size();
        int withPOD = 0;
        int withSignature = 0;
        int withItems = 0;
        
        for (Invoice invoice : invoices) {
            if ((invoice.getPodImagePath1() != null && !invoice.getPodImagePath1().isEmpty()) ||
                (invoice.getPodImagePath2() != null && !invoice.getPodImagePath2().isEmpty()) ||
                (invoice.getPodImagePath3() != null && !invoice.getPodImagePath3().isEmpty())) {
                withPOD++;
            }
            if (invoice.getSignatureImagePath() != null && !invoice.getSignatureImagePath().isEmpty()) {
                withSignature++;
            }
            if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                withItems++;
            }
        }
        
        return String.format(Locale.US,
            "Export Summary:\n" +
            "Total Invoices: %d\n" +
            "With POD Photos: %d (%.1f%%)\n" +
            "With Signatures: %d (%.1f%%)\n" +
            "With Items: %d (%.1f%%)",
            totalInvoices,
            withPOD, (withPOD * 100.0f / totalInvoices),
            withSignature, (withSignature * 100.0f / totalInvoices),
            withItems, (withItems * 100.0f / totalInvoices)
        );
    }
    
    /**
     * Escape CSV special characters
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * Escape TSV special characters
     */
    private String escapeTSV(String value) {
        if (value == null) return "";
        
        // Replace tabs and newlines with spaces
        return value.replace("\t", " ").replace("\n", " ").replace("\r", "");
    }
    
    /**
     * Save content to file in Downloads folder
     */
    private File saveToFile(File directory, String filename, String content) {
        try {
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            File file = new File(directory, filename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
            
            return file;
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving export file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    
    /**
     * Share file using system share dialog
     */
    private void shareFile(File file, String mimeType, String title) {
        try {
            Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
            );
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(mimeType);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoice Export");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            context.startActivity(Intent.createChooser(shareIntent, title));
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error sharing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Share file via email
     */
    public void shareViaEmail(File file, String mimeType, String subject, String body) {
        try {
            Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
            );
            
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType(mimeType);
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject != null ? subject : "Invoice Export - " + file.getName());
            emailIntent.putExtra(Intent.EXTRA_TEXT, body != null ? body : "Please find attached invoice export.");
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Filter to show only email apps
            Intent chooser = Intent.createChooser(emailIntent, "Send via Email");
            if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(chooser);
            } else {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error sending via email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Share file via QuickShare/Nearby Share (general share menu)
     */
    public void shareViaQuickShare(File file, String mimeType) {
        try {
            Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
            );
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(mimeType);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoice Export");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Sharing invoice export file: " + file.getName());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // This will show all share options including QuickShare, Nearby Share, Bluetooth, etc.
            context.startActivity(Intent.createChooser(shareIntent, "Share via"));
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error sharing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Share file via SMS/MMS (text message)
     * Note: MMS can include attachments, plain SMS cannot
     */
    public void shareViaSMS(File file, String mimeType) {
        try {
            Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
            );
            
            Intent smsIntent = new Intent(Intent.ACTION_SEND);
            smsIntent.setType(mimeType);
            smsIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            smsIntent.putExtra(Intent.EXTRA_TEXT, "Invoice export attached: " + file.getName());
            smsIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Try to filter for SMS/MMS apps
            Intent chooser = Intent.createChooser(smsIntent, "Send via SMS/MMS");
            if (smsIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(chooser);
            } else {
                Toast.makeText(context, "No SMS app found", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error sending via SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show export options dialog with different sharing methods
     */
    public void showExportOptionsDialog(File exportedFile, String mimeType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Share: " + exportedFile.getName());
        
        // Remove setMessage to avoid conflict with setItems
        String[] options = {"Email", "QuickShare/Nearby", "SMS/MMS", "More Options"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Email
                    shareViaEmail(exportedFile, mimeType, 
                        "Invoice Export - " + new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date()),
                        "Please find attached the invoice export file.");
                    break;
                case 1: // QuickShare
                    shareViaQuickShare(exportedFile, mimeType);
                    break;
                case 2: // SMS/MMS
                    shareViaSMS(exportedFile, mimeType);
                    break;
                case 3: // More Options
                    shareFile(exportedFile, mimeType, "Share Export");
                    break;
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    /**
     * Create a folder name for the delivery card
     */
    private String createCardFolderName(Invoice invoice) {
        String customerName = invoice.getCustomerName() != null ? 
            invoice.getCustomerName().replaceAll("[^a-zA-Z0-9\\s]", "").trim() : "Unknown";
        String invoiceNum = invoice.getInvoiceNumber() != null ? 
            invoice.getInvoiceNumber().replaceAll("[^a-zA-Z0-9]", "") : "INV" + invoice.getId();
        
        // Limit length and clean up
        if (customerName.length() > 20) {
            customerName = customerName.substring(0, 20);
        }
        
        return customerName + "_" + invoiceNum;
    }
    
    /**
     * Save invoice data as JSON to the card folder
     */
    private void saveInvoiceDataToCard(File cardDir, Invoice invoice) throws Exception {
        JSONObject invoiceObj = new JSONObject();
        invoiceObj.put("id", invoice.getId());
        invoiceObj.put("invoice_number", invoice.getInvoiceNumber());
        invoiceObj.put("customer_name", invoice.getCustomerName());
        invoiceObj.put("address", invoice.getAddress());
        invoiceObj.put("phone", invoice.getPhone());
        invoiceObj.put("items", invoice.getItems());
        invoiceObj.put("notes", invoice.getNotes());
        invoiceObj.put("timestamp", invoice.getTimestamp());
        invoiceObj.put("export_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
        
        // Add image filenames if they exist
        if (invoice.getPodImagePath1() != null) {
            invoiceObj.put("pod_image_1", "pod_photo_1.jpg");
        }
        if (invoice.getPodImagePath2() != null) {
            invoiceObj.put("pod_image_2", "pod_photo_2.jpg");
        }
        if (invoice.getPodImagePath3() != null) {
            invoiceObj.put("pod_image_3", "pod_photo_3.jpg");
        }
        if (invoice.getSignatureImagePath() != null) {
            invoiceObj.put("signature_image", "signature.jpg");
        }
        if (invoice.getOriginalImagePath() != null) {
            invoiceObj.put("original_invoice_image", "original_invoice.jpg");
        }
        
        File dataFile = new File(cardDir, "delivery_info.json");
        FileOutputStream fos = new FileOutputStream(dataFile);
        fos.write(invoiceObj.toString(4).getBytes());
        fos.close();
    }
    
    /**
     * Copy associated images to the card folder
     */
    private void copyImagesToCard(File cardDir, Invoice invoice) {
        try {
            // Copy POD images
            String[] podPaths = { invoice.getPodImagePath1(), invoice.getPodImagePath2(), invoice.getPodImagePath3() };
            for (int i = 0; i < podPaths.length; i++) {
                if (podPaths[i] != null) {
                    copyImageFile(new File(podPaths[i]), new File(cardDir, "pod_photo_" + (i+1) + ".jpg"));
                }
            }
            
            // Copy signature image
            if (invoice.getSignatureImagePath() != null) {
                copyImageFile(new File(invoice.getSignatureImagePath()), new File(cardDir, "signature.jpg"));
            }
            
            // Copy original invoice image
            if (invoice.getOriginalImagePath() != null) {
                copyImageFile(new File(invoice.getOriginalImagePath()), new File(cardDir, "original_invoice.jpg"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Copy an image file to a new location
     */
    private void copyImageFile(File source, File destination) throws IOException {
        if (!source.exists()) return;
        
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(destination);
        
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        
        fis.close();
        fos.close();
    }
    
    /**
     * Create an export summary file
     */
    private File createExportSummary(File mainExportDir, List<Invoice> invoices, int successCount) {
        try {
            JSONObject summary = new JSONObject();
            summary.put("export_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
            summary.put("total_invoices", invoices.size());
            summary.put("successful_exports", successCount);
            summary.put("export_format", "folder_cards");
            
            JSONArray cardFolders = new JSONArray();
            File[] cardDirs = mainExportDir.listFiles(File::isDirectory);
            if (cardDirs != null) {
                for (File cardDir : cardDirs) {
                    cardFolders.put(cardDir.getName());
                }
            }
            summary.put("card_folders", cardFolders);
            
            File summaryFile = new File(mainExportDir, "export_summary.json");
            FileOutputStream fos = new FileOutputStream(summaryFile);
            fos.write(summary.toString(4).getBytes());
            fos.close();
            
            return summaryFile;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Share the entire export folder
     */
    private void shareFolder(File folder, String title) {
        try {
            // For folder sharing, we'll share the summary file which represents the export
            File summaryFile = new File(folder, "export_summary.json");
            if (summaryFile.exists()) {
                shareFile(summaryFile, "application/json", title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
