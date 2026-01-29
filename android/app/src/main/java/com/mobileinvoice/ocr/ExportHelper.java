package com.mobileinvoice.ocr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.mobileinvoice.ocr.database.Invoice;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    /**
     * Export invoices to HTML with embedded Base64 images, zipped and ready to share
     * This is the recommended export method for delivery drivers
     */
    public void exportToHTMLZip(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            Toast.makeText(context, "No invoices to export", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create folder structure: Delivery_Docket_MM-DD-YYYY
            String dateFolder = new SimpleDateFormat("'Delivery_Docket_'MM-dd-yyyy", Locale.US).format(new Date());
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File mainExportDir = new File(downloadsDir, "MobileInvoiceOCR");
            File deliveryDir = new File(mainExportDir, dateFolder);
            
            if (!deliveryDir.exists()) {
                deliveryDir.mkdirs();
            }

            int successCount = 0;

            // Generate individual HTML files for each customer
            for (Invoice invoice : invoices) {
                try {
                    String htmlContent = generateHTMLDeliveryCard(invoice);
                    String filename = sanitizeFilename(invoice.getCustomerName() + "_" + invoice.getInvoiceNumber()) + ".html";
                    File htmlFile = new File(deliveryDir, filename);
                    
                    FileOutputStream fos = new FileOutputStream(htmlFile);
                    fos.write(htmlContent.getBytes("UTF-8"));
                    fos.close();
                    
                    successCount++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (successCount > 0) {
                // Create summary index.html
                String indexHTML = generateIndexHTML(invoices);
                File indexFile = new File(deliveryDir, "index.html");
                FileOutputStream fos = new FileOutputStream(indexFile);
                fos.write(indexHTML.getBytes("UTF-8"));
                fos.close();

                // ZIP the entire folder
                String zipFilename = dateFolder + ".zip";
                File zipFile = new File(mainExportDir, zipFilename);
                zipFolder(deliveryDir, zipFile);

                // Show success and offer sharing options
                showShareDialog(zipFile, successCount);

                // Trigger callback for cleanup dialog
                if (callback != null) {
                    callback.onExportComplete(zipFile, successCount);
                }
            } else {
                Toast.makeText(context, "Failed to export delivery cards", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error during export: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Generate HTML delivery card with embedded images
     */
    private String generateHTMLDeliveryCard(Invoice invoice) {
        StringBuilder html = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US);

        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("<title>Delivery Card - ").append(escapeHTML(invoice.getCustomerName())).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; max-width: 800px; margin: 20px auto; padding: 20px; background: #f5f5f5; }\n");
        html.append(".card { background: white; border-radius: 8px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append("h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }\n");
        html.append("h2 { color: #34495e; margin-top: 30px; }\n");
        html.append(".info-grid { display: grid; grid-template-columns: 150px 1fr; gap: 15px; margin: 20px 0; }\n");
        html.append(".label { font-weight: bold; color: #7f8c8d; }\n");
        html.append(".value { color: #2c3e50; }\n");
        html.append(".image-section { margin: 30px 0; }\n");
        html.append(".image-section img { max-width: 100%; border-radius: 4px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); margin: 10px 0; }\n");
        html.append(".footer { margin-top: 40px; padding-top: 20px; border-top: 2px solid #ecf0f1; text-align: center; color: #95a5a6; font-size: 12px; }\n");
        html.append("@media print { body { background: white; } .card { box-shadow: none; } }\n");
        html.append("</style>\n</head>\n<body>\n");
        
        html.append("<div class='card'>\n");
        html.append("<h1>🚚 Delivery Card</h1>\n");
        
        // Customer Information
        html.append("<div class='info-grid'>\n");
        html.append("<div class='label'>Invoice #:</div><div class='value'>").append(escapeHTML(invoice.getInvoiceNumber())).append("</div>\n");
        html.append("<div class='label'>Customer:</div><div class='value'>").append(escapeHTML(invoice.getCustomerName())).append("</div>\n");
        html.append("<div class='label'>Address:</div><div class='value'>").append(escapeHTML(invoice.getAddress())).append("</div>\n");
        html.append("<div class='label'>Phone:</div><div class='value'>").append(escapeHTML(invoice.getPhone())).append("</div>\n");
        html.append("<div class='label'>Date/Time:</div><div class='value'>").append(dateFormat.format(new Date(invoice.getTimestamp()))).append("</div>\n");
        html.append("<div class='label'>Items:</div><div class='value'>").append(escapeHTML(invoice.getItems())).append("</div>\n");
        if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
            html.append("<div class='label'>Notes:</div><div class='value'>").append(escapeHTML(invoice.getNotes())).append("</div>\n");
        }
        html.append("</div>\n");
        
        // Original Invoice Image
        if (invoice.getOriginalImagePath() != null && !invoice.getOriginalImagePath().isEmpty()) {
            String base64 = imageToBase64(invoice.getOriginalImagePath());
            if (base64 != null) {
                html.append("<h2>📄 Original Invoice</h2>\n");
                html.append("<div class='image-section'>\n");
                html.append("<img src='data:image/jpeg;base64,").append(base64).append("' alt='Original Invoice'>\n");
                html.append("</div>\n");
            }
        }
        
        // POD Photos
        boolean hasPOD = false;
        String[] podPaths = {invoice.getPodImagePath1(), invoice.getPodImagePath2(), invoice.getPodImagePath3()};
        for (int i = 0; i < podPaths.length; i++) {
            if (podPaths[i] != null && !podPaths[i].isEmpty()) {
                if (!hasPOD) {
                    html.append("<h2>📷 Proof of Delivery Photos</h2>\n");
                    html.append("<div class='image-section'>\n");
                    hasPOD = true;
                }
                String base64 = imageToBase64(podPaths[i]);
                if (base64 != null) {
                    html.append("<h3>Photo ").append(i + 1).append("</h3>\n");
                    html.append("<img src='data:image/jpeg;base64,").append(base64).append("' alt='POD Photo ").append(i + 1).append("'>\n");
                }
            }
        }
        if (hasPOD) {
            html.append("</div>\n");
        }
        
        // Signature
        if (invoice.getSignatureImagePath() != null && !invoice.getSignatureImagePath().isEmpty()) {
            String base64 = imageToBase64(invoice.getSignatureImagePath());
            if (base64 != null) {
                html.append("<h2>✍️ Customer Signature</h2>\n");
                html.append("<div class='image-section'>\n");
                html.append("<img src='data:image/jpeg;base64,").append(base64).append("' alt='Signature'>\n");
                html.append("</div>\n");
            }
        }
        
        // Footer
        html.append("<div class='footer'>\n");
        html.append("<p>Generated by Mobile Invoice OCR App<br>");
        html.append("Export Date: ").append(new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US).format(new Date())).append("</p>\n");
        html.append("</div>\n");
        
        html.append("</div>\n</body>\n</html>");
        
        return html.toString();
    }

    /**
     * Generate index HTML with list of all deliveries
     */
    private String generateIndexHTML(List<Invoice> invoices) {
        StringBuilder html = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("<title>Delivery Docket - ").append(dateFormat.format(new Date())).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; max-width: 1000px; margin: 20px auto; padding: 20px; background: #f5f5f5; }\n");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 8px; margin-bottom: 30px; }\n");
        html.append("h1 { margin: 0; }\n");
        html.append(".summary { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }\n");
        html.append(".delivery-list { background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append(".delivery-item { padding: 20px; border-bottom: 1px solid #ecf0f1; }\n");
        html.append(".delivery-item:hover { background: #f8f9fa; }\n");
        html.append(".delivery-item:last-child { border-bottom: none; }\n");
        html.append(".delivery-number { font-size: 24px; font-weight: bold; color: #3498db; }\n");
        html.append(".customer-name { font-size: 18px; margin: 10px 0; }\n");
        html.append(".details { color: #7f8c8d; font-size: 14px; }\n");
        html.append("a { color: #3498db; text-decoration: none; font-weight: bold; }\n");
        html.append("a:hover { text-decoration: underline; }\n");
        html.append(".stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; }\n");
        html.append(".stat-box { text-align: center; padding: 15px; background: #ecf0f1; border-radius: 4px; }\n");
        html.append(".stat-number { font-size: 32px; font-weight: bold; color: #2c3e50; }\n");
        html.append(".stat-label { color: #7f8c8d; font-size: 14px; }\n");
        html.append("</style>\n</head>\n<body>\n");
        
        html.append("<div class='header'>\n");
        html.append("<h1>📦 Delivery Docket</h1>\n");
        html.append("<p>Date: ").append(dateFormat.format(new Date())).append("</p>\n");
        html.append("</div>\n");
        
        // Summary Statistics
        int totalInvoices = invoices.size();
        int withPOD = 0;
        int withSignature = 0;
        
        for (Invoice inv : invoices) {
            if ((inv.getPodImagePath1() != null && !inv.getPodImagePath1().isEmpty()) ||
                (inv.getPodImagePath2() != null && !inv.getPodImagePath2().isEmpty()) ||
                (inv.getPodImagePath3() != null && !inv.getPodImagePath3().isEmpty())) {
                withPOD++;
            }
            if (inv.getSignatureImagePath() != null && !inv.getSignatureImagePath().isEmpty()) {
                withSignature++;
            }
        }
        
        html.append("<div class='summary'>\n");
        html.append("<h2>Summary</h2>\n");
        html.append("<div class='stats'>\n");
        html.append("<div class='stat-box'><div class='stat-number'>").append(totalInvoices).append("</div><div class='stat-label'>Total Deliveries</div></div>\n");
        html.append("<div class='stat-box'><div class='stat-number'>").append(withPOD).append("</div><div class='stat-label'>With POD Photos</div></div>\n");
        html.append("<div class='stat-box'><div class='stat-number'>").append(withSignature).append("</div><div class='stat-label'>With Signatures</div></div>\n");
        html.append("</div>\n</div>\n");
        
        // Delivery List
        html.append("<div class='delivery-list'>\n");
        for (int i = 0; i < invoices.size(); i++) {
            Invoice inv = invoices.get(i);
            String filename = sanitizeFilename(inv.getCustomerName() + "_" + inv.getInvoiceNumber()) + ".html";
            
            html.append("<div class='delivery-item'>\n");
            html.append("<div class='delivery-number'>#").append(i + 1).append(" - ").append(escapeHTML(inv.getInvoiceNumber())).append("</div>\n");
            html.append("<div class='customer-name'>").append(escapeHTML(inv.getCustomerName())).append("</div>\n");
            html.append("<div class='details'>").append(escapeHTML(inv.getAddress())).append("</div>\n");
            html.append("<div class='details'>").append(escapeHTML(inv.getPhone())).append(" • Items: ").append(escapeHTML(inv.getItems())).append("</div>\n");
            html.append("<div style='margin-top: 10px;'><a href='").append(filename).append("'>View Delivery Card →</a></div>\n");
            html.append("</div>\n");
        }
        html.append("</div>\n");
        
        html.append("</body>\n</html>");
        return html.toString();
    }

    /**
     * Convert image file to Base64 string
     */
    private String imageToBase64(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) return null;
            
            // Load and compress image if it's too large
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap == null) return null;
            
            // Resize if image is very large (to keep HTML file size reasonable)
            int maxDimension = 1200;
            if (bitmap.getWidth() > maxDimension || bitmap.getHeight() > maxDimension) {
                float scale = Math.min((float) maxDimension / bitmap.getWidth(), (float) maxDimension / bitmap.getHeight());
                int newWidth = Math.round(bitmap.getWidth() * scale);
                int newHeight = Math.round(bitmap.getHeight() * scale);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            byte[] imageBytes = baos.toByteArray();
            
            bitmap.recycle();
            return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ZIP an entire folder
     */
    private void zipFolder(File sourceFolder, File zipFile) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        zipFolderRecursive(sourceFolder, sourceFolder.getName(), zos);
        zos.close();
    }

    /**
     * Recursively zip folder contents
     */
    private void zipFolderRecursive(File folder, String parentPath, ZipOutputStream zos) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                zipFolderRecursive(file, parentPath + "/" + file.getName(), zos);
            } else {
                FileInputStream fis = new FileInputStream(file);
                ZipEntry zipEntry = new ZipEntry(parentPath + "/" + file.getName());
                zos.putNextEntry(zipEntry);
                
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                
                zos.closeEntry();
                fis.close();
            }
        }
    }

    /**
     * Show share dialog after successful export
     */
    private void showShareDialog(File zipFile, int invoiceCount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("✓ Export Complete");
        builder.setMessage("Successfully exported " + invoiceCount + " delivery cards.\n\n" +
                          "File: " + zipFile.getName() + "\n" +
                          "Size: " + (zipFile.length() / 1024) + " KB\n\n" +
                          "How would you like to share this?");
        
        builder.setPositiveButton("Share Now", (dialog, which) -> {
            shareZipFile(zipFile);
        });
        
        builder.setNeutralButton("View Location", (dialog, which) -> {
            Toast.makeText(context, "Saved to:\n" + zipFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        });
        
        builder.setNegativeButton("Done", null);
        builder.show();
    }

    /**
     * Share ZIP file using Android share sheet
     */
    private void shareZipFile(File zipFile) {
        try {
            Uri zipUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                zipFile
            );
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/zip");
            shareIntent.putExtra(Intent.EXTRA_STREAM, zipUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Delivery Docket - " + new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date()));
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Attached delivery docket with " + zipFile.getName());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            context.startActivity(Intent.createChooser(shareIntent, "Share Delivery Docket"));
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error sharing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sanitize filename for safe file system use
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) return "Unknown";
        // Remove or replace invalid characters
        String safe = filename.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        // Limit length
        if (safe.length() > 50) {
            safe = safe.substring(0, 50);
        }
        return safe;
    }

    /**
     * Escape HTML special characters
     */
    private String escapeHTML(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
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
