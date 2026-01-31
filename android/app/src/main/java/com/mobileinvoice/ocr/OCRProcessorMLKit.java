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
 * Improved OCR Processor for Appliances 4 Less invoices
 * Optimized for consistent invoice format with robust field extraction
 */
public class OCRProcessorMLKit {
    private static final String TAG = "OCRProcessorMLKit";
    
    private final Context context;
    private final TextRecognizer recognizer;
    
    // Patterns for cleaning and extraction
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\(?\\d{3}\\)?[-\\s.]?\\d{3}[-\\s.]?\\d{4}"
    );
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );
    
    // Matches labeled invoice numbers: "Invoice #12345", "Order #12345", "INV-12345"
    private static final Pattern LABELED_INVOICE_PATTERN = Pattern.compile(
        "(?i)(?:invoice|order|inv|ref)\\s*[#:.-]?\\s*(\\d{4,8})",
        Pattern.CASE_INSENSITIVE
    );

    // Matches specific invoice formats: RX1P2204, JWA1220F (but NOT zip codes like 65807)
    private static final Pattern INVOICE_CODE_PATTERN = Pattern.compile(
        "\\b([A-Z]{2}\\d[A-Z]\\d{4}[A-Z]?)\\b"  // Must have letter after first digit (e.g., RX1P2204)
    );

    // Zip code pattern to exclude from invoice number matching
    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile(
        "\\b\\d{5}(?:-\\d{4})?\\b"
    );
    
    // Patterns for cleaning customer name
    private static final Pattern ID_PATTERN = Pattern.compile(
        "\\(?ID:?\\s*[^)]+\\)|/\\s*Salesperson:?\\s*\\w+|\\([^)]*Salesperson[^)]*\\)",
        Pattern.CASE_INSENSITIVE
    );
    
    // Known appliance types from your docs
    private static final String[] APPLIANCE_TYPES = {
        "Washer", "Dryer", "Refrigerator", "Dishwasher", "Freezer",
        "Range", "Oven", "Microwave", "Stove", "Other"
    };

    public OCRProcessorMLKit(Context context) {
        this.context = context;
        this.recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    /**
     * Process invoice image and extract customer data
     */
    public OCRResult processImage(Uri imageUri) {
        OCRResult result = new OCRResult();
        
        try {
            // Load image
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                context.getContentResolver(), imageUri
            );
            
            if (bitmap == null) {
                result.rawText = "Error: Could not load image";
                return result;
            }
            
            // Convert to ML Kit format
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            
            // Process with ML Kit (synchronous wrapper)
            Text mlKitText = processImageSync(image);
            
            if (mlKitText == null) {
                result.rawText = "Error: ML Kit recognition failed";
                return result;
            }
            
            // Extract structured data
            result = extractInvoiceData(mlKitText);
            
            Log.d(TAG, "========= EXTRACTION RESULTS =========");
            Log.d(TAG, "Customer: " + result.customerName);
            Log.d(TAG, "Address: " + result.address);
            Log.d(TAG, "Phone: " + result.phone);
            Log.d(TAG, "Invoice #: " + result.invoiceNumber);
            Log.d(TAG, "Items: " + result.items);
            Log.d(TAG, "=====================================");
            
        } catch (IOException e) {
            Log.e(TAG, "Error loading image", e);
            result.rawText = "Error: " + e.getMessage();
        }
        
        return result;
    }
    
    /**
     * Synchronous wrapper for ML Kit async processing
     */
    private Text processImageSync(InputImage image) {
        final Object lock = new Object();
        final boolean[] done = {false};
        final Text[] result = {null};
        
        recognizer.process(image)
            .addOnSuccessListener(text -> {
                synchronized (lock) {
                    result[0] = text;
                    done[0] = true;
                    lock.notify();
                }
            })
            .addOnFailureListener(e -> {
                synchronized (lock) {
                    Log.e(TAG, "ML Kit recognition failed", e);
                    done[0] = true;
                    lock.notify();
                }
            });
        
        synchronized (lock) {
            try {
                while (!done[0]) {
                    lock.wait(10000); // 10 second timeout
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while waiting for ML Kit", e);
            }
        }
        
        return result[0];
    }
    
    /**
     * Extract invoice data from ML Kit text result
     * Optimized for Appliances 4 Less invoice format
     */
    private OCRResult extractInvoiceData(Text text) {
        OCRResult result = new OCRResult();
        
        // Collect all text lines
        List<String> allLines = new ArrayList<>();
        StringBuilder rawText = new StringBuilder();
        
        for (Text.TextBlock block : text.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                String lineText = line.getText().trim();
                if (!lineText.isEmpty()) {
                    allLines.add(lineText);
                    rawText.append(lineText).append("\n");
                }
            }
        }
        
        result.rawText = rawText.toString();
        
        // Extract invoice number (usually in header)
        result.invoiceNumber = extractInvoiceNumber(allLines);
        
        // Find "BILL TO:" section
        int billToIndex = findLineContaining(allLines, "BILL TO");
        
        if (billToIndex == -1) {
            // Fallback: look for "Name:" or address patterns
            Log.w(TAG, "BILL TO not found, using fallback extraction");
            extractWithFallback(allLines, result);
        } else {
            // Extract data starting from BILL TO section
            extractFromBillToSection(allLines, billToIndex, result);
        }
        
        // Extract items
        result.items = extractItems(allLines);
        
        // Apply defaults if extraction failed
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
            result.invoiceNumber = "INV-" + System.currentTimeMillis();
        }
        
        return result;
    }
    
    /**
     * Extract from BILL TO section (primary method)
     */
    private void extractFromBillToSection(List<String> lines, int billToIndex, OCRResult result) {
        // Look for lines after "BILL TO:"
        for (int i = billToIndex + 1; i < Math.min(billToIndex + 10, lines.size()); i++) {
            String line = lines.get(i).trim();
            
            // Skip empty lines
            if (line.isEmpty()) continue;
            
            // Customer Name - MUST start with "Name:" to avoid header text
            if (result.customerName.isEmpty() && line.toLowerCase().startsWith("name:")) {
                result.customerName = extractCustomerName(line);
            }
            
            // Address - Must start with "Address:" (be strict to avoid Name: line)
            else if (result.address.isEmpty() && line.toLowerCase().startsWith("address:")) {
                result.address = extractAddress(line);
            }
            
            // Phone (contains phone pattern)
            else if (result.phone.isEmpty() && 
                     (line.toLowerCase().contains("phone") || PHONE_PATTERN.matcher(line).find())) {
                result.phone = extractPhone(line);
            }
        }
    }
    
    /**
     * Fallback extraction when BILL TO is not found
     */
    private void extractWithFallback(List<String> lines, OCRResult result) {
        for (String line : lines) {
            // Look for name pattern
            if (result.customerName.isEmpty() && 
                line.toLowerCase().startsWith("name:")) {
                result.customerName = extractCustomerName(line);
            }
            
            // Look for address pattern
            if (result.address.isEmpty() && 
                (line.toLowerCase().startsWith("address:") || 
                 (line.matches(".*\\d+\\s+[A-Z].*") && line.length() > 10))) {
                result.address = extractAddress(line);
            }
            
            // Look for phone
            if (result.phone.isEmpty() && PHONE_PATTERN.matcher(line).find()) {
                result.phone = extractPhone(line);
            }
        }
    }
    
    /**
     * Extract and clean customer name
     * Removes: "Name:", ID info, Salesperson info
     * Handles concatenated names like "KENMARTIN"
     */
    private String extractCustomerName(String line) {
        String name = line;
        
        // Remove "Name:" prefix
        name = name.replaceFirst("(?i)^name:\\s*", "");
        
        // Remove ID and Salesperson information
        name = ID_PATTERN.matcher(name).replaceAll("");
        
        // Remove extra whitespace and slashes
        name = name.replaceAll("\\s*/\\s*", " ");
        name = name.replaceAll("\\s+", " ");
        name = name.trim();
        
        // Handle concatenated names (e.g., "KENMARTIN" → "Ken Martin")
        name = splitConcatenatedName(name);
        
        // Convert to title case for consistency
        if (!name.isEmpty()) {
            name = toTitleCase(name);
        }
        
        return name;
    }
    
    /**
     * Split concatenated names like "KENMARTIN" into "KEN MARTIN"
     * Uses heuristic: if name is all uppercase with no spaces and >6 chars,
     * try to split at transition from lowercase pattern to uppercase pattern
     */
    private String splitConcatenatedName(String name) {
        // Only process if name is single word, all uppercase, and reasonably long
        if (name.contains(" ") || !name.equals(name.toUpperCase()) || name.length() < 6) {
            return name;
        }
        
        // Common first names to look for as prefixes
        String[] commonFirstNames = {
            "KEN", "JON", "JOHN", "DAVID", "MIKE", "ROBERT", "JAMES",
            "MARY", "JUDY", "LINDA", "PATRICIA", "JENNIFER", "SUSAN"
        };
        
        // Check if name starts with a common first name
        for (String firstName : commonFirstNames) {
            if (name.startsWith(firstName) && name.length() > firstName.length()) {
                String lastName = name.substring(firstName.length());
                // Only split if the remainder looks like a valid last name (3+ chars)
                if (lastName.length() >= 3) {
                    return firstName + " " + lastName;
                }
            }
        }
        
        // Fallback: Try to split at midpoint for names like "KENMARTIN" (4+6)
        // This is a reasonable heuristic for two-word names
        if (name.length() >= 8 && name.length() <= 15) {
            int midPoint = name.length() / 2;
            // Try splitting around the middle (±1 char)
            for (int i = midPoint - 1; i <= midPoint + 1; i++) {
                if (i > 2 && i < name.length() - 2) {
                    String first = name.substring(0, i);
                    String last = name.substring(i);
                    // Both parts should be reasonable length
                    if (first.length() >= 3 && last.length() >= 3) {
                        return first + " " + last;
                    }
                }
            }
        }
        
        // If we can't confidently split, return as-is
        return name;
    }
    
    /**
     * Extract and clean address
     * Stops at email or excessive length
     */
    private String extractAddress(String line) {
        String address = line;
        
        // Remove "Address:" prefix
        address = address.replaceFirst("(?i)^address:\\s*", "");
        
        // Stop at email if present
        Matcher emailMatcher = EMAIL_PATTERN.matcher(address);
        if (emailMatcher.find()) {
            address = address.substring(0, emailMatcher.start()).trim();
        }
        
        // Stop at phone if present (sometimes address and phone are on same line)
        Matcher phoneMatcher = PHONE_PATTERN.matcher(address);
        if (phoneMatcher.find()) {
            address = address.substring(0, phoneMatcher.start()).trim();
        }
        
        // Clean up whitespace
        address = address.replaceAll("\\s+", " ").trim();
        
        return address;
    }
    
    /**
     * Extract phone number with consistent formatting
     */
    private String extractPhone(String line) {
        Matcher matcher = PHONE_PATTERN.matcher(line);
        if (matcher.find()) {
            String phone = matcher.group();
            
            // Normalize to (XXX) XXX-XXXX format
            String digits = phone.replaceAll("\\D", "");
            if (digits.length() == 10) {
                return String.format("(%s) %s-%s",
                    digits.substring(0, 3),
                    digits.substring(3, 6),
                    digits.substring(6, 10)
                );
            }
            
            return phone;
        }
        return "";
    }
    
    /**
     * Extract invoice number from header area
     * Priority: 1) Labeled invoice numbers, 2) Invoice code patterns
     * Excludes: Zip codes, address content
     */
    private String extractInvoiceNumber(List<String> lines) {
        // Skip lines that are clearly address content
        List<String> headerLines = new ArrayList<>();
        for (int i = 0; i < Math.min(15, lines.size()); i++) {
            String line = lines.get(i);
            String lower = line.toLowerCase();

            // Skip address-related lines
            if (lower.contains("address:") || lower.contains("bill to") ||
                lower.contains("missouri") || lower.contains("springfield") ||
                lower.contains("street") || lower.contains("avenue") ||
                lower.contains("road") || lower.contains("drive") ||
                lower.contains("city") || lower.contains("state")) {
                continue;
            }

            headerLines.add(line);
        }

        // First priority: Look for explicitly labeled invoice numbers
        for (String line : headerLines) {
            Matcher labeledMatcher = LABELED_INVOICE_PATTERN.matcher(line);
            if (labeledMatcher.find()) {
                String invoiceNum = labeledMatcher.group(1);
                // Verify it's not a zip code
                if (!ZIP_CODE_PATTERN.matcher(invoiceNum).matches() || invoiceNum.length() > 5) {
                    Log.d(TAG, "Found labeled invoice number: " + invoiceNum);
                    return invoiceNum;
                }
            }
        }

        // Second priority: Look for invoice code patterns (e.g., RX1P2204)
        for (String line : headerLines) {
            Matcher codeMatcher = INVOICE_CODE_PATTERN.matcher(line);
            if (codeMatcher.find()) {
                String invoiceCode = codeMatcher.group(1);
                Log.d(TAG, "Found invoice code: " + invoiceCode);
                return invoiceCode;
            }
        }

        // Third priority: Look for any standalone number that looks like an invoice (6+ digits)
        Pattern standaloneNumber = Pattern.compile("\\b(\\d{6,10})\\b");
        for (String line : headerLines) {
            // Skip if line contains state names or address keywords
            if (line.toLowerCase().contains("phone") || line.toLowerCase().contains("email")) {
                continue;
            }

            Matcher numMatcher = standaloneNumber.matcher(line);
            if (numMatcher.find()) {
                String num = numMatcher.group(1);
                // Exclude if it looks like a phone number (10 digits starting with common area codes)
                if (num.length() == 10 && (num.startsWith("417") || num.startsWith("573") ||
                    num.startsWith("816") || num.startsWith("314"))) {
                    continue;
                }
                Log.d(TAG, "Found standalone invoice number: " + num);
                return num;
            }
        }

        return "";
    }
    
    /**
     * Extract appliance items from invoice
     * Handles multiple items in various formats
     */
    private String extractItems(List<String> lines) {
        List<String> foundItems = new ArrayList<>();
        
        // First pass: Look for explicit "Type:" fields
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            
            // Check if line starts with "Type:"
            if (line.toLowerCase().startsWith("type:")) {
                String item = line.replaceFirst("(?i)^type:\\s*", "").trim();
                
                // Clean up model/serial info if present
                item = item.split("(?i)\\s+(model|serial)")[0].trim();
                
                if (!item.isEmpty() && isValidAppliance(item)) {
                    // Normalize to standard appliance name
                    String normalized = normalizeAppliance(item);
                    if (!foundItems.contains(normalized)) {
                        foundItems.add(normalized);
                    }
                }
            }
        }
        
        // Second pass: If no "Type:" fields found, search for known appliance keywords
        if (foundItems.isEmpty()) {
            for (String line : lines) {
                for (String appliance : APPLIANCE_TYPES) {
                    if (line.toLowerCase().contains(appliance.toLowerCase())) {
                        if (!foundItems.contains(appliance)) {
                            foundItems.add(appliance);
                        }
                    }
                }
            }
        }
        
        return String.join(",", foundItems);
    }
    
    /**
     * Normalize appliance name to standard format
     */
    private String normalizeAppliance(String item) {
        String lower = item.toLowerCase();
        
        // Check each known appliance type
        for (String appliance : APPLIANCE_TYPES) {
            if (lower.contains(appliance.toLowerCase())) {
                return appliance;
            }
        }
        
        return item; // Return as-is if no match
    }
    
    /**
     * Check if item is a valid appliance type
     */
    private boolean isValidAppliance(String item) {
        for (String appliance : APPLIANCE_TYPES) {
            if (item.toLowerCase().contains(appliance.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Find line containing specific text (case-insensitive)
     */
    private int findLineContaining(List<String> lines, String searchText) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).toLowerCase().contains(searchText.toLowerCase())) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Convert string to title case
     */
    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Close ML Kit resources
     */
    public void close() {
        if (recognizer != null) {
            recognizer.close();
        }
    }
    
    /**
     * Parse items string into a list
     * Helper method for backward compatibility
     */
    public static List<String> parseItemsList(String itemsString) {
        List<String> items = new ArrayList<>();
        if (itemsString != null && !itemsString.isEmpty()) {
            String[] parts = itemsString.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    items.add(trimmed);
                }
            }
        }
        return items;
    }
    
    /**
     * Result class holding extracted invoice data
     */
    public static class OCRResult {
        public String customerName = "";
        public String address = "";
        public String phone = "";
        public String invoiceNumber = "";
        public String items = "";
        public String rawText = "";
    }
}
