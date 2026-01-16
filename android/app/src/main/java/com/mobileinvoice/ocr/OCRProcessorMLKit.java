package com.mobileinvoice.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ML Kit-based OCR Processor - on-device text recognition
 * Replaces Tesseract and HTTP backend with Google ML Kit
 */
public class OCRProcessorMLKit {
    private static final String TAG = "OCRProcessorMLKit";
    private Context context;
    private TextRecognizer recognizer;
    
    // Patterns for field extraction
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\(?\\d{3}\\)?[-\\s.]?\\d{3}[-\\s.]?\\d{4}"
    );
    
    private static final Pattern INVOICE_PATTERN = Pattern.compile(
        "(?:INV|INVOICE|#)[-\\s:]*([A-Z0-9-]+)",
        Pattern.CASE_INSENSITIVE
    );
    
    // Pattern for invoice number that appears near/after "INVOICE" header.
    // Enhanced pattern to handle various formats:
    // - KY112205, JNW112201 (2-3 letters + 6+ digits)
    // - INV-12345, Invoice #12345 (with prefixes)
    // - 12345678 (pure numeric)
    // - ABC123DEF (alphanumeric)
    private static final Pattern HEADER_INVOICE_PATTERN = Pattern.compile(
        "(?:INVOICE\\s*#?\\s*|INV\\s*#?\\s*|#)\\s*([A-Z0-9]{3,}|\\d{5,})\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern ZIP_PATTERN = Pattern.compile(
        "\\b\\d{5}(?:-\\d{4})?\\b"
    );
    
    // Common appliance keywords for item extraction - expanded list
    private static final String[] APPLIANCE_KEYWORDS = {
        "REFRIGERATOR", "FRIDGE", "REFRIG", "FRZR", "FREEZER",
        "WASHER", "DRYER", "WASHING MACHINE", "DRYING MACHINE", "LAUNDRY",
        "DISHWASHER", "DISH WASHER",
        "RANGE", "STOVE", "COOKTOP", "COOK TOP", "OVEN",
        "MICROWAVE", "MW",
        "APPLIANCE", "UNIT"
    };

    public OCRProcessorMLKit(Context context) {
        this.context = context;
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Log.d(TAG, "ML Kit OCR Processor initialized - on-device processing");
    }

    /**
     * Process image with ML Kit and extract invoice fields
     */
    public OCRResult processImage(Uri imageUri) {
        OCRResult result = new OCRResult();
        
        try {
            Log.d(TAG, "Processing image with ML Kit: " + imageUri);
            
            // Load image from URI
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                context.getContentResolver(), imageUri
            );
            
            if (bitmap == null) {
                Log.e(TAG, "Failed to load bitmap from URI");
                result.rawText = "Error: Could not load image";
                return result;
            }
            
            // Create ML Kit InputImage
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            
            // Process synchronously (wrap async in blocking call)
            final Object lock = new Object();
            final boolean[] done = {false};
            final Text[] textResult = {null};
            
            recognizer.process(image)
                .addOnSuccessListener(text -> {
                    synchronized (lock) {
                        textResult[0] = text;
                        done[0] = true;
                        lock.notify();
                    }
                })
                .addOnFailureListener(e -> {
                    synchronized (lock) {
                        Log.e(TAG, "ML Kit recognition failed: " + e.getMessage(), e);
                        done[0] = true;
                        lock.notify();
                    }
                });
            
            // Wait for recognition to complete
            synchronized (lock) {
                while (!done[0]) {
                    try {
                        lock.wait(10000); // 10 second timeout
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Recognition interrupted", e);
                        break;
                    }
                }
            }
            
            if (textResult[0] != null) {
                result = extractInvoiceData(textResult[0]);
                Log.d(TAG, "========= ML KIT EXTRACTION RESULTS =========");
                Log.d(TAG, "Invoice #: " + result.invoiceNumber);
                Log.d(TAG, "Customer: " + result.customerName);
                Log.d(TAG, "Address: " + result.address);
                Log.d(TAG, "Phone: " + result.phone);
                Log.d(TAG, "Items: " + result.items);
                Log.d(TAG, "============================================");
            } else {
                result.rawText = "Error: Recognition timed out or failed";
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Error loading image: " + e.getMessage(), e);
            result.rawText = "Error: " + e.getMessage();
        } catch (Exception e) {
            Log.e(TAG, "Error processing image: " + e.getMessage(), e);
            result.rawText = "Error: " + e.getMessage();
        }
        
        return result;
    }
    
    /**
     * Extract invoice fields from ML Kit Text result
     * Intelligent parsing to identify customer name, address, phone, invoice number
     * Now includes top-right invoice number extraction and item detection
     */
    private OCRResult extractInvoiceData(Text text) {
        OCRResult result = new OCRResult();
        StringBuilder fullText = new StringBuilder();
        
        // Collect all lines from all text blocks WITH POSITION DATA
        List<String> allLines = new ArrayList<>();
        List<RecognizedLine> recognizedLines = new ArrayList<>();
        
        for (Text.TextBlock block : text.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                String lineText = line.getText().trim();
                if (!lineText.isEmpty()) {
                    allLines.add(lineText);
                    fullText.append(lineText).append("\n");
                    
                    android.graphics.Rect bounds = line.getBoundingBox();
                    if (bounds != null) {
                        recognizedLines.add(new RecognizedLine(lineText, bounds));
                    }
                }
            }
        }
        
        result.rawText = fullText.toString();
        Log.d(TAG, "Extracted " + allLines.size() + " lines from image");
        
        // Extract invoice number from top-right corner (primary reference)
        result.invoiceNumber = extractInvoiceNumberNearInvoiceHeader(recognizedLines);

        // Extract items being delivered (prefer table parsing)
        result.items = extractDeliveredItemsFromTable(recognizedLines);
        if (result.items == null || result.items.trim().isEmpty() || result.items.equalsIgnoreCase("No items detected")) {
            result.items = extractDeliveredItems(allLines);
        }
        
        // Find "BILL TO:" line
        int billToIndex = -1;
        for (int i = 0; i < allLines.size(); i++) {
            String line = allLines.get(i).toUpperCase();
            if (line.contains("BILL TO")) {
                billToIndex = i;
                Log.d(TAG, "Found 'BILL TO:' at line " + i + ": " + allLines.get(i));
                break;
            }
        }
        
        if (billToIndex >= 0 && billToIndex + 3 < allLines.size()) {
            // Next 3 lines after "BILL TO:" are: Name, Address, Phone
            String nameLine = allLines.get(billToIndex + 1).trim();
            String addressLine = allLines.get(billToIndex + 2).trim();
            String phoneLine = allLines.get(billToIndex + 3).trim();
            
            Log.d(TAG, "Name line: " + nameLine);
            Log.d(TAG, "Address line: " + addressLine);
            Log.d(TAG, "Phone line: " + phoneLine);
            
            // Extract name - CLEAN: Remove prefixes and ID markers
            if (nameLine.contains("Name:")) {
                nameLine = nameLine.substring(nameLine.indexOf("Name:") + 5).trim();
            }
            
            // Extract invoice ID from name line as fallback: (ID:XXXXXXX)
            if (result.invoiceNumber.isEmpty() || result.invoiceNumber.equals("No invoice number")) {
                Pattern idPattern = Pattern.compile("\\(ID:([^)]+)\\)");
                Matcher idMatcher = idPattern.matcher(nameLine);
                if (idMatcher.find()) {
                    result.invoiceNumber = idMatcher.group(1).trim();
                    Log.d(TAG, "Using fallback invoice ID from name line: " + result.invoiceNumber);
                    // Remove ID and everything after it from name
                    nameLine = nameLine.substring(0, idMatcher.start()).trim();
                }
            }
            
            // Clean name: remove trailing slashes, numbers, extra whitespace
            nameLine = nameLine.replaceAll("[/\\\\].*$", "").trim();
            nameLine = nameLine.replaceAll("\\s+", " ");
            result.customerName = nameLine;
            
            // Extract address - CLEAN: Remove prefix and stop at non-address content
            if (addressLine.contains("Address:")) {
                addressLine = addressLine.substring(addressLine.indexOf("Address:") + 8).trim();
            }
            
            // Stop at email addresses, Serial numbers, pricing, etc.
            addressLine = cleanAddressLine(addressLine);
            
            // Clean address: normalize whitespace
            addressLine = addressLine.replaceAll("\\s+", " ").trim();
            result.address = addressLine;
            
            // Extract phone - CLEAN: Get just the phone number
            if (phoneLine.contains("Phone:")) {
                phoneLine = phoneLine.substring(phoneLine.indexOf("Phone:") + 6).trim();
            }
            Matcher phoneMatcher = PHONE_PATTERN.matcher(phoneLine);
            if (phoneMatcher.find()) {
                String phone = phoneMatcher.group().trim();
                // Normalize phone format: (XXX) XXX-XXXX
                phone = phone.replaceAll("[^0-9]", "");
                if (phone.length() == 10) {
                    phone = "(" + phone.substring(0, 3) + ") " + 
                           phone.substring(3, 6) + "-" + phone.substring(6);
                }
                result.phone = phone;
            }
            
        } else {
            Log.w(TAG, "'BILL TO:' section not found or incomplete - using fallback");
            result = extractWithPatterns(fullText.toString());
        }
        
        // Clean up results
        if (result.customerName.isEmpty()) {
            result.customerName = "Unknown Customer";
        }
        if (result.address.isEmpty()) {
            result.address = "No address found";
        }
        if (result.phone.isEmpty()) {
            result.phone = "No phone";
        }
        if (result.invoiceNumber.isEmpty()) {
            result.invoiceNumber = "No invoice number";
        }
        
        return result;
    }
    
    /**
     * Extract invoice number from top-right corner (primary company reference)
     * Uses geometry: locate the "INVOICE" header in the top-right quadrant,
     * then pick the closest matching line directly beneath it.
     */
    private String extractInvoiceNumberNearInvoiceHeader(List<RecognizedLine> lines) {
        if (lines.isEmpty()) return "";

        // Find bounds to estimate top-right quadrant thresholds
        int minTop = Integer.MAX_VALUE;
        int maxRight = 0;
        for (RecognizedLine line : lines) {
            minTop = Math.min(minTop, line.bounds.top);
            maxRight = Math.max(maxRight, line.bounds.right);
        }

        int rightThreshold = (int) (maxRight * 0.60);
        int topThreshold = minTop + (int) ((getMaxBottom(lines) - minTop) * 0.30);

        RecognizedLine invoiceHeader = null;
        for (RecognizedLine line : lines) {
            if (line.bounds.right >= rightThreshold && line.bounds.top <= topThreshold) {
                String upper = line.text.toUpperCase();
                if (upper.equals("INVOICE") || upper.contains("INVOICE")) {
                    invoiceHeader = line;
                    Log.d(TAG, "Found INVOICE header at bounds " + invoiceHeader.bounds);
                    break;
                }
            }
        }

        String best = "";
        int bestDy = Integer.MAX_VALUE;

        // Primary: closest matching line under INVOICE header in the same x neighborhood
        if (invoiceHeader != null) {
            for (RecognizedLine line : lines) {
                if (line == invoiceHeader) continue;
                if (line.bounds.top <= invoiceHeader.bounds.bottom) continue;
                if (line.bounds.right < rightThreshold) continue;

                // Keep roughly under the header (allow some horizontal drift)
                int dx = Math.abs(line.bounds.centerX() - invoiceHeader.bounds.centerX());
                if (dx > invoiceHeader.bounds.width() * 2) continue;

                String candidate = findInvoiceCandidateInText(line.text);
                if (candidate == null) continue;

                int dy = line.bounds.top - invoiceHeader.bounds.bottom;
                if (dy >= 0 && dy < bestDy) {
                    bestDy = dy;
                    best = candidate;
                }
            }
        }

        if (!best.isEmpty()) {
            Log.d(TAG, "Extracted invoice number near header: " + best);
            return best;
        }

        // Fallback: scan top band for plausible invoice codes
        for (RecognizedLine line : lines) {
            if (line.bounds.top <= topThreshold) {
                String candidate = findInvoiceCandidateInText(line.text);
                if (candidate != null) {
                    Log.d(TAG, "Fallback invoice candidate in top band: " + candidate);
                    return candidate;
                }
            }
        }

        return "";
    }

    private int getMaxBottom(List<RecognizedLine> lines) {
        int max = 0;
        for (RecognizedLine line : lines) {
            max = Math.max(max, line.bounds.bottom);
        }
        return max;
    }

    private String findInvoiceCandidateInText(String text) {
        if (text == null) return null;
        String cleaned = text.trim();
        if (cleaned.isEmpty()) return null;

        // First try the enhanced header pattern
        Matcher matcher = HEADER_INVOICE_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            String candidate = matcher.group(1);
            // Clean up the candidate
            candidate = candidate.replaceAll("^[^A-Z0-9]+|[^A-Z0-9]+$", "");
            // Avoid obvious date/time fragments
            if (candidate.contains(":") || candidate.contains("/") || candidate.length() < 3) {
                return null;
            }
            return candidate;
        }

        // Fallback: look for standalone invoice-like patterns
        // Pattern: letters followed by digits, or long alphanumeric strings
        Pattern fallbackPattern = Pattern.compile("\\b([A-Z]{1,4}\\d{4,}|\\d{6,}| [A-Z0-9]{6,})\\b");
        matcher = fallbackPattern.matcher(cleaned.toUpperCase());
        if (matcher.find()) {
            String candidate = matcher.group(1).trim();
            if (candidate.length() >= 5 && !candidate.contains(":") && !candidate.contains("/")) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Extract item types from the invoice table by using header/column positions.
     * Goal: reliably extract rows like "Refrigerator", "Washer", "Dryer" from
     * the "# Type Model ..." section without being confused by "Term/Warranty" lines.
     */
    private String extractDeliveredItemsFromTable(List<RecognizedLine> lines) {
        if (lines.isEmpty()) return "No items detected";

        RecognizedLine typeHeader = null;
        RecognizedLine modelHeader = null;
        int tableTop = -1;
        int tableBottom = Integer.MAX_VALUE;

        // Find header row containing Type/Model/Price
        for (RecognizedLine line : lines) {
            String upper = line.text.toUpperCase();
            if (upper.contains("TYPE") && upper.contains("MODEL") && (upper.contains("PRICE") || upper.contains("SERIAL"))) {
                tableTop = line.bounds.bottom;
                Log.d(TAG, "Detected items table header row: " + line.text);
                break;
            }
        }

        // Alternate header detection when ML Kit splits header into multiple lines
        for (RecognizedLine line : lines) {
            String upper = line.text.toUpperCase();
            if (upper.equals("TYPE")) typeHeader = line;
            if (upper.equals("MODEL")) modelHeader = line;
        }
        if (tableTop < 0 && typeHeader != null && modelHeader != null) {
            tableTop = Math.max(typeHeader.bounds.bottom, modelHeader.bounds.bottom);
            Log.d(TAG, "Detected split header anchors for items table");
        }

        if (tableTop < 0) return "No items detected";

        // Detect footer markers to stop table scanning
        for (RecognizedLine line : lines) {
            String upper = line.text.toUpperCase();
            if (upper.contains("OTHER SERVICES") || upper.contains("TAX RATE") || upper.contains("SUBTOTAL") || upper.contains("TOTAL")) {
                tableBottom = Math.min(tableBottom, line.bounds.top);
            }
        }

        // Column bounds: from type header to model header; if missing, infer using overall width.
        int minLeft = Integer.MAX_VALUE;
        int maxRight = 0;
        for (RecognizedLine line : lines) {
            minLeft = Math.min(minLeft, line.bounds.left);
            maxRight = Math.max(maxRight, line.bounds.right);
        }

        int typeLeft = (typeHeader != null) ? typeHeader.bounds.left : minLeft;
        int modelLeft = (modelHeader != null) ? modelHeader.bounds.left : (int) (minLeft + (maxRight - minLeft) * 0.35);
        int typeRight = Math.max(modelLeft - 5, typeLeft + 1);

        List<String> items = new ArrayList<>();

        for (RecognizedLine line : lines) {
            if (line.bounds.top < tableTop) continue;
            if (line.bounds.top >= tableBottom) continue;

            // Skip warranty/term/detail lines
            String upper = line.text.toUpperCase();
            if (upper.contains("WARRANTY") || upper.startsWith("TERM") || upper.startsWith("WARRANTY")) {
                continue;
            }

            // Prefer lines that sit in the "Type" column
            int cx = line.bounds.centerX();
            if (cx < typeLeft || cx > typeRight) {
                continue;
            }

            String normalized = normalizeItemType(line.text);
            if (normalized == null) continue;

            // De-dup
            boolean exists = false;
            for (String existing : items) {
                if (existing.equalsIgnoreCase(normalized)) {
                    exists = true;
                    break;
                }
            }
            if (exists) continue;

            items.add(normalized);
            Log.d(TAG, "Parsed item type from table: " + normalized);
        }

        return items.isEmpty() ? "No items detected" : String.join(", ", items);
    }

    private String normalizeItemType(String raw) {
        if (raw == null) return null;
        String upper = raw.trim().toUpperCase();
        if (upper.isEmpty()) return null;
        
        // Filter out: numbers, headers, warranty/terms text
        if (upper.matches("^[#\\d\\s.,$-]+$")) return null;
        if (upper.contains("TYPE") || upper.contains("MODEL") || upper.contains("PRICE") || upper.contains("SERIAL")) return null;
        if (upper.contains("WARRANTY") || upper.contains("TERM") || upper.contains("CONTRACT") || upper.contains("PROTECTION")) return null;
        if (upper.contains("TOTAL") || upper.contains("SUBTOTAL") || upper.contains("TAX") || upper.contains("DEPOSIT")) return null;
        if (upper.length() < 3 || upper.length() > 30) return null; // Reasonable length
        
        // Map common variants to the app's AVAILABLE_ITEMS
        // Refrigerators
        if (upper.contains("REFRIGERATOR") || upper.contains("FRIDGE") || upper.contains("REFRIG")) return "Refrigerator";

        // Washers and Dryers
        if (upper.contains("WASHER") && !upper.contains("DISHWASHER")) return "Washer";
        if (upper.contains("DRYER") || upper.contains("DRYING")) return "Dryer";
        if (upper.contains("WASHER/DRYER") || upper.contains("WASHER DRYER") || upper.contains("LAUNDRY CENTER")) return "Washer/Dryer";

        // Dishwashers
        if (upper.contains("DISHWASHER") || upper.contains("DISH WASHER")) return "Dishwasher";

        // Ranges/Stoves
        if (upper.contains("RANGE") || upper.contains("RNG")) return "Range";
        if (upper.contains("STOVE") || upper.contains("COOKTOP") || upper.contains("COOK TOP")) return "Stove";

        // Other appliances
        if (upper.contains("FREEZER") || upper.contains("FRZR")) return "Freezer";
        if (upper.contains("MICROWAVE") || upper.contains("MW")) return "Microwave";
        if (upper.contains("OVEN") && !upper.contains("MICROWAVE")) return "Oven";

        // Generic/other - only if explicitly says "Appliance" or "Unit"
        if (upper.equals("APPLIANCE") || upper.equals("UNIT")) return "Other";

        return null; // Don't include if it doesn't match known appliance types
    }
    
    /**
     * Extract list of items/appliances being delivered
     * Looks for appliance keywords and product descriptions
     */
    private String extractDeliveredItems(List<String> lines) {
        List<String> items = new ArrayList<>();
        boolean inItemSection = false;
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String upperLine = line.toUpperCase();
            
            // Detect start of items section
            if (upperLine.contains("ITEM") || upperLine.contains("DESCRIPTION") ||
                upperLine.contains("PRODUCT") || upperLine.contains("MODEL")) {
                inItemSection = true;
                Log.d(TAG, "Found item section at line " + i + ": " + line);
                continue;
            }
            
            // Stop at warranty, terms, or footer sections
            if (upperLine.contains("WARRANTY") || upperLine.contains("TERMS") ||
                upperLine.contains("TOTAL") || upperLine.contains("SUBTOTAL") ||
                upperLine.contains("TAX")) {
                inItemSection = false;
                break;
            }
            
            // Extract items: look for appliance keywords or numbered items
            if (inItemSection || containsApplianceKeyword(upperLine)) {
                // Clean and add item if it looks like product description
                if (line.length() > 3 && !line.matches("^[\\d\\s.$]+$")) {
                    String cleanedItem = line.replaceAll("^\\d+[\\s.)]\\s*", "").trim();
                    if (!cleanedItem.isEmpty() && cleanedItem.length() > 2) {
                        // Try to normalize the item type
                        String normalizedItem = normalizeItemType(cleanedItem);
                        if (normalizedItem != null) {
                            // Use normalized name
                            if (!items.contains(normalizedItem)) {
                                items.add(normalizedItem);
                                Log.d(TAG, "Found normalized item: " + normalizedItem + " (from: " + cleanedItem + ")");
                            }
                        } else if (cleanedItem.length() > 3 && cleanedItem.length() < 50) {
                            // Use original if it looks reasonable
                            if (!items.contains(cleanedItem)) {
                                items.add(cleanedItem);
                                Log.d(TAG, "Found item: " + cleanedItem);
                            }
                        }
                    }
                }
            }
        }
        
        // Return comma-separated list
        return items.isEmpty() ? "No items detected" : String.join(", ", items);
    }
    
    /**
     * Parse and organize extracted items into a clean, deduplicated list
     * This method takes the raw OCR items string and returns a properly formatted list
     */
    public static List<String> parseItemsList(String itemsString) {
        List<String> parsedItems = new ArrayList<>();
        
        if (itemsString == null || itemsString.trim().isEmpty() || 
            itemsString.equalsIgnoreCase("No items detected")) {
            return parsedItems;
        }
        
        // Split by common delimiters
        String[] rawItems = itemsString.split("[,;|\\n]");
        
        for (String rawItem : rawItems) {
            String item = rawItem.trim();
            if (item.isEmpty()) continue;
            
            // Clean up the item
            item = item.replaceAll("^[-•*]\\s*", ""); // Remove bullets
            item = item.replaceAll("\\s+", " "); // Normalize spaces
            item = item.trim();
            
            if (item.length() < 2) continue;
            
            // Try to normalize to standard appliance names
            String normalized = normalizeItemTypeForDisplay(item);
            if (normalized != null && !parsedItems.contains(normalized)) {
                parsedItems.add(normalized);
            } else if (!parsedItems.contains(item)) {
                // Keep original if normalization doesn't apply
                parsedItems.add(item);
            }
        }
        
        // Sort for consistent display
        parsedItems.sort(String::compareToIgnoreCase);
        
        return parsedItems;
    }
    
    /**
     * Normalize item type for display purposes (static version for use in other classes)
     */
    public static String normalizeItemTypeForDisplay(String raw) {
        if (raw == null) return null;
        String upper = raw.trim().toUpperCase();
        if (upper.isEmpty()) return null;
        if (upper.matches("^[#\\d]+$")) return null;
        if (upper.contains("TYPE") || upper.contains("MODEL") || upper.contains("PRICE") || upper.contains("SERIAL")) return null;
        if (upper.contains("WARRANTY") || upper.contains("TERM") || upper.contains("CONTRACT")) return null;

        // Map common variants to the app's AVAILABLE_ITEMS
        // Refrigerators
        if (upper.contains("REFRIGERATOR") || upper.contains("FRIDGE") || upper.contains("REFRIG")) return "Refrigerator";

        // Washers and Dryers
        if (upper.contains("WASHER") || upper.contains("WASHING MACHINE")) return "Washer";
        if (upper.contains("DRYER") || upper.contains("DRYING MACHINE")) return "Dryer";
        if (upper.contains("WASHER/DRYER") || upper.contains("WASHER DRYER") || upper.contains("LAUNDRY CENTER")) return "Washer/Dryer";

        // Dishwashers
        if (upper.contains("DISHWASHER") || upper.contains("DISH WASHER")) return "Dishwasher";

        // Ranges/Stoves
        if (upper.contains("RANGE") || upper.contains("RNG")) return "Range";
        if (upper.contains("STOVE") || upper.contains("COOKTOP") || upper.contains("COOK TOP")) return "Stove";

        // Other appliances
        if (upper.contains("FREEZER") || upper.contains("FRZR")) return "Freezer";
        if (upper.contains("MICROWAVE") || upper.contains("MW")) return "Microwave";
        if (upper.contains("OVEN")) return "Oven";

        // Generic/other
        if (upper.contains("APPLIANCE") || upper.contains("UNIT")) return "Other";

        return null;
    }
    
    /**
     * Check if line contains appliance keywords
     */
    private boolean containsApplianceKeyword(String upperLine) {
        for (String keyword : APPLIANCE_KEYWORDS) {
            if (upperLine.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Fallback pattern-based extraction when structure not found
     */
    private OCRResult extractWithPatterns(String text) {
        OCRResult result = new OCRResult();
        result.rawText = text;
        
        // Extract phone
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            result.phone = phoneMatcher.group().trim();
        }
        
        // Extract invoice number
        Matcher invMatcher = INVOICE_PATTERN.matcher(text);
        if (invMatcher.find()) {
            result.invoiceNumber = invMatcher.group(1).trim();
        }
        
        // Try to extract name and address from lines
        String[] lines = text.split("\n");
        List<String> potentialAddressLines = new ArrayList<>();
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // Skip header lines (company name, etc.)
            if (line.toUpperCase().contains("APPLIANCES") || 
                line.toUpperCase().contains("BATTLEFIELD") ||
                line.toUpperCase().contains("INVOICE") ||
                line.toUpperCase().contains("DATE")) {
                continue;
            }
            
            // Lines with street addresses or zip codes are likely customer data
            if (line.matches(".*\\d+.*[A-Za-z]+.*") || ZIP_PATTERN.matcher(line).find()) {
                potentialAddressLines.add(line);
            }
        }
        
        // First non-header line could be customer name
        if (!potentialAddressLines.isEmpty()) {
            result.customerName = potentialAddressLines.get(0);
            if (potentialAddressLines.size() > 1) {
                result.address = String.join(", ", 
                    potentialAddressLines.subList(1, potentialAddressLines.size()));
            }
        }
        
        return result;
    }
    
    private static class RecognizedLine {
        final String text;
        final android.graphics.Rect bounds;

        RecognizedLine(String text, android.graphics.Rect bounds) {
            this.text = text;
            this.bounds = bounds;
        }
    }
    
    /**
     * Clean address line by removing email, pricing, serial numbers, and other non-address content
     */
    private String cleanAddressLine(String address) {
        if (address == null || address.isEmpty()) {
            return "";
        }
        
        // Find the first occurrence of indicators that address has ended
        String[] stopIndicators = {
            "Email:", "@", 
            "A4L/", "Serial #", "Accessory Fee:", "Other Fee:", 
            "Products:", "Service:", "Delivery:", "Term:",
            "Dryer,", "Washer,", // Appliance list indicates address is done
            "$" // Pricing information
        };
        
        int stopIndex = address.length();
        for (String indicator : stopIndicators) {
            int index = address.indexOf(indicator);
            if (index > 0 && index < stopIndex) {
                stopIndex = index;
            }
        }
        
        // Extract only the part before stop indicators
        address = address.substring(0, stopIndex).trim();
        
        // Remove trailing commas or punctuation
        address = address.replaceAll("[,;]+$", "").trim();
        
        return address;
    }
    
    /**
     * Result container for OCR extraction
     */
    public static class OCRResult {
        public String customerName = "";
        public String address = "";
        public String phone = "";
        public String invoiceNumber = "";  // Primary company reference from top-right
        public String items = "";            // Delivered appliances/products
        public String rawText = "";
    }
    
    /**
     * Clean up resources
     */
    public void close() {
        if (recognizer != null) {
            recognizer.close();
        }
    }
}
