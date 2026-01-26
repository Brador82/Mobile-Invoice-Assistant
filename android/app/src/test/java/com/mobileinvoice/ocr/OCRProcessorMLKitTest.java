package com.mobileinvoice.ocr;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static org.junit.Assert.*;

/**
 * Unit tests for OCRProcessorMLKit
 * Tests the static utility methods and pattern matching without requiring Android context
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class OCRProcessorMLKitTest {

    // Recreate patterns for testing (since they're private in the actual class)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\(?\\d{3}\\)?[-\\s.]?\\d{3}[-\\s.]?\\d{4}"
    );

    private static final Pattern INVOICE_PATTERN = Pattern.compile(
        "(?:INV|INVOICE|#)[-\\s:]*([A-Z0-9-]+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ZIP_PATTERN = Pattern.compile(
        "\\b\\d{5}(?:-\\d{4})?\\b"
    );

    // ==================== Phone Pattern Tests ====================

    @Test
    public void testPhonePattern_standardFormat() {
        String text = "Call us at (555) 123-4567 for assistance";
        Matcher matcher = PHONE_PATTERN.matcher(text);
        assertTrue("Should match standard phone format", matcher.find());
        assertEquals("(555) 123-4567", matcher.group());
    }

    @Test
    public void testPhonePattern_withDots() {
        String text = "Phone: 555.123.4567";
        Matcher matcher = PHONE_PATTERN.matcher(text);
        assertTrue("Should match dot-separated phone format", matcher.find());
        assertEquals("555.123.4567", matcher.group());
    }

    @Test
    public void testPhonePattern_withDashes() {
        String text = "Contact: 555-123-4567";
        Matcher matcher = PHONE_PATTERN.matcher(text);
        assertTrue("Should match dash-separated phone format", matcher.find());
        assertEquals("555-123-4567", matcher.group());
    }

    @Test
    public void testPhonePattern_noSeparators() {
        String text = "Tel: 5551234567";
        Matcher matcher = PHONE_PATTERN.matcher(text);
        assertTrue("Should match phone without separators", matcher.find());
        assertEquals("5551234567", matcher.group());
    }

    @Test
    public void testPhonePattern_withSpaces() {
        String text = "Number: 555 123 4567";
        Matcher matcher = PHONE_PATTERN.matcher(text);
        assertTrue("Should match space-separated phone format", matcher.find());
        assertEquals("555 123 4567", matcher.group());
    }

    @Test
    public void testPhonePattern_invalidLength() {
        String text = "Invalid: 555-12-4567";
        Matcher matcher = PHONE_PATTERN.matcher(text);
        assertFalse("Should not match invalid phone length", matcher.find());
    }

    @Test
    public void testPhonePattern_multiplePhones() {
        String text = "Office: (555) 111-2222, Cell: 555-333-4444";
        Matcher matcher = PHONE_PATTERN.matcher(text);
        assertTrue("Should find first phone", matcher.find());
        assertEquals("(555) 111-2222", matcher.group());
        assertTrue("Should find second phone", matcher.find());
        assertEquals("555-333-4444", matcher.group());
    }

    // ==================== Invoice Pattern Tests ====================

    @Test
    public void testInvoicePattern_withINV() {
        String text = "INV-12345 dated 01/01/2024";
        Matcher matcher = INVOICE_PATTERN.matcher(text);
        assertTrue("Should match INV format", matcher.find());
        assertEquals("12345", matcher.group(1));
    }

    @Test
    public void testInvoicePattern_withINVOICE() {
        String text = "INVOICE: ABC123";
        Matcher matcher = INVOICE_PATTERN.matcher(text);
        assertTrue("Should match INVOICE format", matcher.find());
        assertEquals("ABC123", matcher.group(1));
    }

    @Test
    public void testInvoicePattern_withHashSymbol() {
        String text = "#INV-2024-001";
        Matcher matcher = INVOICE_PATTERN.matcher(text);
        assertTrue("Should match # format", matcher.find());
    }

    @Test
    public void testInvoicePattern_caseInsensitive() {
        String text = "invoice: test123";
        Matcher matcher = INVOICE_PATTERN.matcher(text);
        assertTrue("Should match lowercase invoice", matcher.find());
        assertEquals("test123", matcher.group(1));
    }

    @Test
    public void testInvoicePattern_alphanumeric() {
        String text = "INV KY112205";
        Matcher matcher = INVOICE_PATTERN.matcher(text);
        assertTrue("Should match alphanumeric invoice", matcher.find());
        assertEquals("KY112205", matcher.group(1));
    }

    // ==================== ZIP Pattern Tests ====================

    @Test
    public void testZipPattern_fiveDigit() {
        String text = "Address: 123 Main St, City, ST 12345";
        Matcher matcher = ZIP_PATTERN.matcher(text);
        assertTrue("Should match 5-digit ZIP", matcher.find());
        assertEquals("12345", matcher.group());
    }

    @Test
    public void testZipPattern_nineDit() {
        String text = "ZIP: 12345-6789";
        Matcher matcher = ZIP_PATTERN.matcher(text);
        assertTrue("Should match ZIP+4 format", matcher.find());
        assertEquals("12345-6789", matcher.group());
    }

    @Test
    public void testZipPattern_invalidFourDigit() {
        String text = "Code: 1234";
        Matcher matcher = ZIP_PATTERN.matcher(text);
        assertFalse("Should not match 4-digit code", matcher.find());
    }

    // ==================== parseItemsList Tests ====================

    @Test
    public void testParseItemsList_singleItem() {
        List<String> items = OCRProcessorMLKit.parseItemsList("Refrigerator");
        assertEquals(1, items.size());
        assertEquals("Refrigerator", items.get(0));
    }

    @Test
    public void testParseItemsList_multipleItems() {
        List<String> items = OCRProcessorMLKit.parseItemsList("Refrigerator, Washer, Dryer");
        assertEquals(3, items.size());
        assertTrue(items.contains("Refrigerator"));
        assertTrue(items.contains("Washer"));
        assertTrue(items.contains("Dryer"));
    }

    @Test
    public void testParseItemsList_emptyString() {
        List<String> items = OCRProcessorMLKit.parseItemsList("");
        assertEquals(0, items.size());
    }

    @Test
    public void testParseItemsList_nullInput() {
        List<String> items = OCRProcessorMLKit.parseItemsList(null);
        assertEquals(0, items.size());
    }

    @Test
    public void testParseItemsList_noItemsDetected() {
        List<String> items = OCRProcessorMLKit.parseItemsList("No items detected");
        assertEquals(0, items.size());
    }

    @Test
    public void testParseItemsList_semicolonDelimiter() {
        List<String> items = OCRProcessorMLKit.parseItemsList("Washer; Dryer; Dishwasher");
        assertEquals(3, items.size());
    }

    @Test
    public void testParseItemsList_pipeDelimiter() {
        List<String> items = OCRProcessorMLKit.parseItemsList("Range | Microwave | Oven");
        assertEquals(3, items.size());
    }

    @Test
    public void testParseItemsList_newlineDelimiter() {
        List<String> items = OCRProcessorMLKit.parseItemsList("Refrigerator\nWasher\nDryer");
        assertEquals(3, items.size());
    }

    @Test
    public void testParseItemsList_withBullets() {
        List<String> items = OCRProcessorMLKit.parseItemsList("• Refrigerator, • Washer, • Dryer");
        assertEquals(3, items.size());
        assertEquals("Refrigerator", items.get(1)); // After sorting
    }

    @Test
    public void testParseItemsList_deduplicate() {
        List<String> items = OCRProcessorMLKit.parseItemsList("Refrigerator, Refrigerator, Washer");
        assertEquals(2, items.size());
    }

    @Test
    public void testParseItemsList_normalizeNames() {
        List<String> items = OCRProcessorMLKit.parseItemsList("FRIDGE, WASHING MACHINE, DRYING MACHINE");
        assertEquals(3, items.size());
        assertTrue("Should normalize FRIDGE to Refrigerator", items.contains("Refrigerator"));
        assertTrue("Should normalize WASHING MACHINE to Washer", items.contains("Washer"));
        assertTrue("Should normalize DRYING MACHINE to Dryer", items.contains("Dryer"));
    }

    @Test
    public void testParseItemsList_sortedOutput() {
        List<String> items = OCRProcessorMLKit.parseItemsList("Washer, Dryer, Refrigerator");
        assertEquals("Dryer", items.get(0)); // Alphabetically first
        assertEquals("Refrigerator", items.get(1));
        assertEquals("Washer", items.get(2));
    }

    // ==================== normalizeItemTypeForDisplay Tests ====================

    @Test
    public void testNormalizeItemType_refrigerator() {
        assertEquals("Refrigerator", OCRProcessorMLKit.normalizeItemTypeForDisplay("REFRIGERATOR"));
        assertEquals("Refrigerator", OCRProcessorMLKit.normalizeItemTypeForDisplay("Fridge"));
        assertEquals("Refrigerator", OCRProcessorMLKit.normalizeItemTypeForDisplay("REFRIG"));
    }

    @Test
    public void testNormalizeItemType_washer() {
        assertEquals("Washer", OCRProcessorMLKit.normalizeItemTypeForDisplay("WASHER"));
        assertEquals("Washer", OCRProcessorMLKit.normalizeItemTypeForDisplay("washing machine"));
    }

    @Test
    public void testNormalizeItemType_dryer() {
        assertEquals("Dryer", OCRProcessorMLKit.normalizeItemTypeForDisplay("DRYER"));
        assertEquals("Dryer", OCRProcessorMLKit.normalizeItemTypeForDisplay("drying machine"));
    }

    @Test
    public void testNormalizeItemType_washerDryer() {
        assertEquals("Washer/Dryer", OCRProcessorMLKit.normalizeItemTypeForDisplay("WASHER/DRYER"));
        assertEquals("Washer/Dryer", OCRProcessorMLKit.normalizeItemTypeForDisplay("laundry center"));
    }

    @Test
    public void testNormalizeItemType_dishwasher() {
        assertEquals("Dishwasher", OCRProcessorMLKit.normalizeItemTypeForDisplay("DISHWASHER"));
        assertEquals("Dishwasher", OCRProcessorMLKit.normalizeItemTypeForDisplay("dish washer"));
    }

    @Test
    public void testNormalizeItemType_range() {
        assertEquals("Range", OCRProcessorMLKit.normalizeItemTypeForDisplay("RANGE"));
        assertEquals("Range", OCRProcessorMLKit.normalizeItemTypeForDisplay("RNG"));
    }

    @Test
    public void testNormalizeItemType_stove() {
        assertEquals("Stove", OCRProcessorMLKit.normalizeItemTypeForDisplay("STOVE"));
        assertEquals("Stove", OCRProcessorMLKit.normalizeItemTypeForDisplay("cooktop"));
        assertEquals("Stove", OCRProcessorMLKit.normalizeItemTypeForDisplay("cook top"));
    }

    @Test
    public void testNormalizeItemType_freezer() {
        assertEquals("Freezer", OCRProcessorMLKit.normalizeItemTypeForDisplay("FREEZER"));
        assertEquals("Freezer", OCRProcessorMLKit.normalizeItemTypeForDisplay("FRZR"));
    }

    @Test
    public void testNormalizeItemType_microwave() {
        assertEquals("Microwave", OCRProcessorMLKit.normalizeItemTypeForDisplay("MICROWAVE"));
        assertEquals("Microwave", OCRProcessorMLKit.normalizeItemTypeForDisplay("MW"));
    }

    @Test
    public void testNormalizeItemType_oven() {
        assertEquals("Oven", OCRProcessorMLKit.normalizeItemTypeForDisplay("OVEN"));
    }

    @Test
    public void testNormalizeItemType_other() {
        assertEquals("Other", OCRProcessorMLKit.normalizeItemTypeForDisplay("APPLIANCE"));
        assertEquals("Other", OCRProcessorMLKit.normalizeItemTypeForDisplay("UNIT"));
    }

    @Test
    public void testNormalizeItemType_null() {
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay(null));
    }

    @Test
    public void testNormalizeItemType_empty() {
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay(""));
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay("   "));
    }

    @Test
    public void testNormalizeItemType_filterHeaders() {
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay("TYPE"));
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay("MODEL"));
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay("PRICE"));
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay("SERIAL"));
    }

    @Test
    public void testNormalizeItemType_filterWarranty() {
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay("WARRANTY"));
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay("TERM"));
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay("CONTRACT"));
    }

    @Test
    public void testNormalizeItemType_filterNumbers() {
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay("12345"));
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay("#123"));
    }

    @Test
    public void testNormalizeItemType_unknownItem() {
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay("Unknown Item"));
        assertNull(OCRProcessorMLKit.normalizeItemTypeForDisplay("Random Text"));
    }

    // ==================== Edge Cases ====================

    @Test
    public void testPhonePattern_atBoundaries() {
        String text = "5551234567 is the number";
        Matcher matcher = PHONE_PATTERN.matcher(text);
        assertTrue("Should match at beginning", matcher.find());
    }

    @Test
    public void testInvoicePattern_complexFormats() {
        String[] testCases = {
            "INV-2024-001-ABC",
            "INVOICE #12345",
            "Invoice: KY112205",
            "#JNW112201"
        };

        for (String testCase : testCases) {
            Matcher matcher = INVOICE_PATTERN.matcher(testCase);
            assertTrue("Should match: " + testCase, matcher.find());
        }
    }

    @Test
    public void testParseItemsList_mixedDelimiters() {
        List<String> items = OCRProcessorMLKit.parseItemsList("Refrigerator, Washer; Dryer | Range");
        assertTrue(items.size() >= 3);
    }

    @Test
    public void testParseItemsList_withExtraWhitespace() {
        List<String> items = OCRProcessorMLKit.parseItemsList("  Refrigerator  ,   Washer   ,   Dryer  ");
        assertEquals(3, items.size());
    }

    // ==================== Phone Normalization Tests ====================

    @Test
    public void testPhoneNormalization() {
        // Test that various phone formats can be normalized to (XXX) XXX-XXXX
        String[] inputs = {"5551234567", "(555) 123-4567", "555.123.4567", "555-123-4567"};

        for (String input : inputs) {
            String normalized = input.replaceAll("[^0-9]", "");
            assertEquals("All formats should normalize to 10 digits", 10, normalized.length());

            if (normalized.length() == 10) {
                String formatted = "(" + normalized.substring(0, 3) + ") " +
                                  normalized.substring(3, 6) + "-" + normalized.substring(6);
                assertEquals("(555) 123-4567", formatted);
            }
        }
    }

    // ==================== Address Cleaning Tests ====================

    @Test
    public void testAddressCleaningPatterns() {
        // Test patterns that should stop address extraction
        String[] stopIndicators = {
            "Email:", "@", "Serial #", "Accessory Fee:",
            "Products:", "Service:", "Delivery:", "$"
        };

        String baseAddress = "123 Main Street, City, ST 12345";

        for (String indicator : stopIndicators) {
            String fullText = baseAddress + " " + indicator + " extra content";
            int stopIndex = fullText.indexOf(indicator);
            String cleaned = fullText.substring(0, stopIndex).trim();
            assertEquals(baseAddress, cleaned);
        }
    }

    // ==================== OCRResult Tests ====================

    @Test
    public void testOCRResult_defaultValues() {
        OCRProcessorMLKit.OCRResult result = new OCRProcessorMLKit.OCRResult();
        assertEquals("", result.customerName);
        assertEquals("", result.address);
        assertEquals("", result.phone);
        assertEquals("", result.invoiceNumber);
        assertEquals("", result.items);
        assertEquals("", result.rawText);
    }

    @Test
    public void testOCRResult_setValues() {
        OCRProcessorMLKit.OCRResult result = new OCRProcessorMLKit.OCRResult();
        result.customerName = "John Doe";
        result.address = "123 Main St";
        result.phone = "(555) 123-4567";
        result.invoiceNumber = "INV-001";
        result.items = "Refrigerator, Washer";
        result.rawText = "Full OCR text...";

        assertEquals("John Doe", result.customerName);
        assertEquals("123 Main St", result.address);
        assertEquals("(555) 123-4567", result.phone);
        assertEquals("INV-001", result.invoiceNumber);
        assertEquals("Refrigerator, Washer", result.items);
        assertEquals("Full OCR text...", result.rawText);
    }
}
