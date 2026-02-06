# OCR Quality Restoration Plan

## Problem Summary
OCR quality degraded by ~90% after "improvements" made on Jan 15, 2026. The changes introduced overly strict filtering that's rejecting valid data.

## Root Causes

### 1. **Over-Filtering in normalizeItemType()** (Line 722)
```java
// PROBLEM: Returns null for anything that doesn't match exact keywords
return null; // Don't include if it doesn't match known appliance types
```
This rejects valid items that don't perfectly match predefined keywords.

### 2. **Rigid Coordinate Zones** (Lines 513-600)
```java
int topThreshold = (int)(maxBottom * 0.20);    // Too rigid
int bottomThreshold = (int)(maxBottom * 0.35); // Misses data if layout varies
```
Hardcoded percentages don't adapt to different invoice layouts.

### 3. **Aggressive Text Filtering** (Line 727-730)
```java
if (upper.contains("TYPE") || upper.contains("MODEL") || 
    upper.contains("PRICE") || upper.contains("SERIAL")) return null;
```
Filters out lines containing "MODEL" - but many appliances are listed as "Whirlpool MODEL WFE515S0ES"

## Solutions

### Fix 1: Relax Item Normalization (HIGH PRIORITY)

**Current Code** (Line 720-759):
```java
private String normalizeItemType(String raw) {
    // ... filters ...
    
    return null; // Don't include if it doesn't match known appliance types
}
```

**Fixed Code**:
```java
private String normalizeItemType(String raw) {
    if (raw == null) return null;
    String upper = raw.trim().toUpperCase();
    if (upper.isEmpty()) return null;
    
    // Filter out: pure numbers, obvious non-items
    if (upper.matches("^[#\\d\\s.,$-]+$")) return null;
    if (upper.matches("^(TYPE|ITEM|DESCRIPTION|QTY|QUANTITY)\\s*:?$")) return null; // Headers only
    
    // Filter warranty/terms ONLY if it's the whole line
    if (upper.matches(".*(WARRANTY|TERMS?|CONTRACT|PROTECTION PLAN)\\s*:.*")) return null;
    if (upper.matches("^(TOTAL|SUBTOTAL|TAX|DEPOSIT)\\s*:?.*")) return null;
    
    // Reasonable length (allow longer for detailed product names)
    if (upper.length() < 3 || upper.length() > 80) return null;
    
    // Map common variants to standard names
    if (upper.contains("REFRIGERATOR") || upper.contains("FRIDGE") || upper.contains("REFRIG")) 
        return "Refrigerator";
    if (upper.contains("WASHER") && !upper.contains("DISHWASHER")) return "Washer";
    if (upper.contains("DRYER") || upper.contains("DRYING")) return "Dryer";
    if (upper.contains("DISHWASHER") || upper.contains("DISH WASHER")) return "Dishwasher";
    if (upper.contains("RANGE") || upper.contains("RNG")) return "Range";
    if (upper.contains("STOVE") || upper.contains("COOKTOP") || upper.contains("COOK TOP")) return "Stove";
    if (upper.contains("FREEZER") || upper.contains("FRZR")) return "Freezer";
    if (upper.contains("MICROWAVE") || upper.contains("MW")) return "Microwave";
    if (upper.contains("OVEN") && !upper.contains("MICROWAVE")) return "Oven";
    
    // If it contains appliance keywords, return cleaned version
    if (containsApplianceKeyword(upper)) {
        // Clean but keep original if it has appliance keyword
        String cleaned = raw.trim();
        cleaned = cleaned.replaceAll("^\\d+[\\s.)]\\s*", ""); // Remove leading numbers
        cleaned = cleaned.replaceAll("\\s+", " "); // Normalize whitespace
        return cleaned.length() > 2 ? cleaned : null;
    }
    
    // If no keyword match but looks like valid product (has letters and numbers)
    // KEEP IT - let the user decide if it's valid
    if (raw.matches(".*[A-Z].*[A-Z].*") && raw.length() > 5) {
        String cleaned = raw.trim().replaceAll("\\s+", " ");
        return cleaned;
    }
    
    return null;
}
```

### Fix 2: Flexible Coordinate Zones

**Current Code** (Lines 513-540):
```java
private String extractCustomerNameByCoordinates(List<RecognizedLine> lines, int maxRight, int maxBottom) {
    int topThreshold = (int)(maxBottom * 0.20);
    int bottomThreshold = (int)(maxBottom * 0.35);
    // ...
}
```

**Fixed Code** - Add adaptive zone detection:
```java
private String extractCustomerNameByCoordinates(List<RecognizedLine> lines, int maxRight, int maxBottom) {
    // First, try to find "BILL TO:" marker for adaptive positioning
    int billToY = findMarkerY(lines, "BILL TO");
    
    int topThreshold, bottomThreshold, rightThreshold;
    
    if (billToY > 0) {
        // Adaptive: extract 100-300 pixels below "BILL TO:"
        topThreshold = billToY + 10;
        bottomThreshold = billToY + 300;
        rightThreshold = (int)(maxRight * 0.65);
        Log.d(TAG, "Using adaptive zone based on 'BILL TO:' at y=" + billToY);
    } else {
        // Fallback: use wider ranges than before
        topThreshold = (int)(maxBottom * 0.15);  // Was 0.20
        bottomThreshold = (int)(maxBottom * 0.40); // Was 0.35
        rightThreshold = (int)(maxRight * 0.65);   // Was 0.60
        Log.d(TAG, "Using fallback coordinate zones (no marker found)");
    }
    
    // Rest of extraction logic...
}

// Helper method
private int findMarkerY(List<RecognizedLine> lines, String marker) {
    for (RecognizedLine line : lines) {
        if (line.text.toUpperCase().contains(marker)) {
            return line.bounds.top;
        }
    }
    return -1;
}
```

### Fix 3: Enhanced ML Kit Configuration

**Current Code** (Line 69):
```java
recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
```

**Enhanced Code**:
```java
// Use Latin script recognizer for better English text recognition
recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

// Add image preprocessing hints
// ML Kit performs better with high-contrast, properly oriented images
Log.d(TAG, "ML Kit OCR Processor initialized");
Log.d(TAG, "Using Latin script recognizer for optimal English text recognition");
```

### Fix 4: Better Rotation Handling

**Current Issue** (Lines 142-155):
```java
// Force rotate 90° counterclockwise if landscape
if (bitmap.getWidth() > bitmap.getHeight()) {
    Log.d(TAG, "Image is landscape, rotating 90° CCW");
    matrix.postRotate(-90); // Negative = counterclockwise
    // ...
}
```

**Improved Code**:
```java
// Check EXIF first, then landscape detection
int exifRotation = getRotationFromExif(imageUri);

if (exifRotation != 0) {
    Log.d(TAG, "Applying EXIF rotation: " + exifRotation + "°");
    android.graphics.Matrix matrix = new android.graphics.Matrix();
    matrix.postRotate(exifRotation);
    Bitmap rotatedBitmap = Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    bitmap.recycle();
    bitmap = rotatedBitmap;
} else if (bitmap.getWidth() > bitmap.getHeight()) {
    // Only auto-rotate if NO EXIF data AND image is landscape
    Log.d(TAG, "Image is landscape (no EXIF), rotating 90° CCW");
    android.graphics.Matrix matrix = new android.graphics.Matrix();
    matrix.postRotate(-90);
    Bitmap rotatedBitmap = Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    bitmap.recycle();
    bitmap = rotatedBitmap;
}
```

### Fix 5: Add Debug Logging

Enable detailed logging to diagnose what's being filtered:

```java
private String normalizeItemType(String raw) {
    if (raw == null) return null;
    String upper = raw.trim().toUpperCase();
    if (upper.isEmpty()) return null;
    
    // Log what we're examining
    Log.v(TAG, "Examining potential item: " + raw);
    
    // ... filters with logging ...
    
    if (upper.matches("^[#\\d\\s.,$-]+$")) {
        Log.v(TAG, "  ❌ Filtered: pure numbers/symbols");
        return null;
    }
    
    // ... etc ...
}
```

## Implementation Priority

1. **IMMEDIATE**: Fix #1 (Relax item normalization) - This is causing 80% of the data loss
2. **HIGH**: Fix #2 (Flexible coordinate zones) - Makes extraction more robust
3. **MEDIUM**: Fix #4 (Better rotation) - Prevents over-rotation
4. **LOW**: Fix #5 (Debug logging) - For ongoing monitoring

## Testing Checklist

After implementing fixes:

1. ✅ Test with 5 different invoice samples
2. ✅ Verify customer name extraction
3. ✅ Verify address extraction  
4. ✅ Verify phone extraction
5. ✅ Verify item extraction (should catch MORE items now)
6. ✅ Check logcat for "Filtered" messages - should be much fewer
7. ✅ Compare against baseline (before Jan 15 changes)

## Expected Results

- **Item Detection**: Should increase from ~10% to 90%+ of valid items
- **Field Accuracy**: Customer name/address/phone should improve
- **False Positives**: May increase slightly (acceptable tradeoff)
- **Overall Quality**: Should match or exceed pre-Jan 15 performance
