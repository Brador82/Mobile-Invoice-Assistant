package com.mobileinvoice.ocr.database;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Unit tests for Invoice entity
 * Tests all getters, setters, and default values
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class InvoiceTest {

    private Invoice invoice;

    @Before
    public void setUp() {
        invoice = new Invoice();
    }

    // ==================== Constructor Tests ====================

    @Test
    public void testConstructor_timestampIsSet() {
        long before = System.currentTimeMillis();
        Invoice newInvoice = new Invoice();
        long after = System.currentTimeMillis();

        assertTrue("Timestamp should be set on construction",
                   newInvoice.getTimestamp() >= before && newInvoice.getTimestamp() <= after);
    }

    @Test
    public void testConstructor_defaultIdIsZero() {
        assertEquals("Default ID should be 0", 0, invoice.getId());
    }

    @Test
    public void testConstructor_fieldsAreNull() {
        assertNull("InvoiceNumber should be null by default", invoice.getInvoiceNumber());
        assertNull("CustomerName should be null by default", invoice.getCustomerName());
        assertNull("Address should be null by default", invoice.getAddress());
        assertNull("Phone should be null by default", invoice.getPhone());
        assertNull("Items should be null by default", invoice.getItems());
        assertNull("PodImagePath1 should be null by default", invoice.getPodImagePath1());
        assertNull("PodImagePath2 should be null by default", invoice.getPodImagePath2());
        assertNull("PodImagePath3 should be null by default", invoice.getPodImagePath3());
        assertNull("SignatureImagePath should be null by default", invoice.getSignatureImagePath());
        assertNull("Notes should be null by default", invoice.getNotes());
        assertNull("OriginalImagePath should be null by default", invoice.getOriginalImagePath());
        assertNull("RawOcrText should be null by default", invoice.getRawOcrText());
    }

    // ==================== ID Tests ====================

    @Test
    public void testSetId_positiveValue() {
        invoice.setId(1);
        assertEquals(1, invoice.getId());
    }

    @Test
    public void testSetId_largeValue() {
        invoice.setId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, invoice.getId());
    }

    @Test
    public void testSetId_zero() {
        invoice.setId(0);
        assertEquals(0, invoice.getId());
    }

    // ==================== Invoice Number Tests ====================

    @Test
    public void testSetInvoiceNumber_normalValue() {
        invoice.setInvoiceNumber("INV-001");
        assertEquals("INV-001", invoice.getInvoiceNumber());
    }

    @Test
    public void testSetInvoiceNumber_alphanumeric() {
        invoice.setInvoiceNumber("KY112205");
        assertEquals("KY112205", invoice.getInvoiceNumber());
    }

    @Test
    public void testSetInvoiceNumber_emptyString() {
        invoice.setInvoiceNumber("");
        assertEquals("", invoice.getInvoiceNumber());
    }

    @Test
    public void testSetInvoiceNumber_null() {
        invoice.setInvoiceNumber(null);
        assertNull(invoice.getInvoiceNumber());
    }

    @Test
    public void testSetInvoiceNumber_specialCharacters() {
        invoice.setInvoiceNumber("INV-2024/001#A");
        assertEquals("INV-2024/001#A", invoice.getInvoiceNumber());
    }

    @Test
    public void testSetInvoiceNumber_longValue() {
        String longInvoice = "INV-" + "A".repeat(100);
        invoice.setInvoiceNumber(longInvoice);
        assertEquals(longInvoice, invoice.getInvoiceNumber());
    }

    // ==================== Customer Name Tests ====================

    @Test
    public void testSetCustomerName_normalValue() {
        invoice.setCustomerName("John Doe");
        assertEquals("John Doe", invoice.getCustomerName());
    }

    @Test
    public void testSetCustomerName_withSpecialChars() {
        invoice.setCustomerName("John O'Brien-Smith");
        assertEquals("John O'Brien-Smith", invoice.getCustomerName());
    }

    @Test
    public void testSetCustomerName_unicode() {
        invoice.setCustomerName("José García");
        assertEquals("José García", invoice.getCustomerName());
    }

    @Test
    public void testSetCustomerName_emptyString() {
        invoice.setCustomerName("");
        assertEquals("", invoice.getCustomerName());
    }

    @Test
    public void testSetCustomerName_null() {
        invoice.setCustomerName(null);
        assertNull(invoice.getCustomerName());
    }

    // ==================== Address Tests ====================

    @Test
    public void testSetAddress_normalValue() {
        invoice.setAddress("123 Main Street, City, ST 12345");
        assertEquals("123 Main Street, City, ST 12345", invoice.getAddress());
    }

    @Test
    public void testSetAddress_multiline() {
        invoice.setAddress("123 Main Street\nApt 4B\nCity, ST 12345");
        assertEquals("123 Main Street\nApt 4B\nCity, ST 12345", invoice.getAddress());
    }

    @Test
    public void testSetAddress_emptyString() {
        invoice.setAddress("");
        assertEquals("", invoice.getAddress());
    }

    @Test
    public void testSetAddress_null() {
        invoice.setAddress(null);
        assertNull(invoice.getAddress());
    }

    // ==================== Phone Tests ====================

    @Test
    public void testSetPhone_formattedValue() {
        invoice.setPhone("(555) 123-4567");
        assertEquals("(555) 123-4567", invoice.getPhone());
    }

    @Test
    public void testSetPhone_unformattedValue() {
        invoice.setPhone("5551234567");
        assertEquals("5551234567", invoice.getPhone());
    }

    @Test
    public void testSetPhone_emptyString() {
        invoice.setPhone("");
        assertEquals("", invoice.getPhone());
    }

    @Test
    public void testSetPhone_null() {
        invoice.setPhone(null);
        assertNull(invoice.getPhone());
    }

    // ==================== Items Tests ====================

    @Test
    public void testSetItems_singleItem() {
        invoice.setItems("Refrigerator");
        assertEquals("Refrigerator", invoice.getItems());
    }

    @Test
    public void testSetItems_multipleItems() {
        invoice.setItems("Refrigerator, Washer, Dryer");
        assertEquals("Refrigerator, Washer, Dryer", invoice.getItems());
    }

    @Test
    public void testSetItems_emptyString() {
        invoice.setItems("");
        assertEquals("", invoice.getItems());
    }

    @Test
    public void testSetItems_null() {
        invoice.setItems(null);
        assertNull(invoice.getItems());
    }

    // ==================== POD Image Path Tests ====================

    @Test
    public void testSetPodImagePath1_normalValue() {
        invoice.setPodImagePath1("/storage/images/pod1.jpg");
        assertEquals("/storage/images/pod1.jpg", invoice.getPodImagePath1());
    }

    @Test
    public void testSetPodImagePath2_normalValue() {
        invoice.setPodImagePath2("/storage/images/pod2.jpg");
        assertEquals("/storage/images/pod2.jpg", invoice.getPodImagePath2());
    }

    @Test
    public void testSetPodImagePath3_normalValue() {
        invoice.setPodImagePath3("/storage/images/pod3.jpg");
        assertEquals("/storage/images/pod3.jpg", invoice.getPodImagePath3());
    }

    @Test
    public void testSetAllPodPaths() {
        invoice.setPodImagePath1("/path/pod1.jpg");
        invoice.setPodImagePath2("/path/pod2.jpg");
        invoice.setPodImagePath3("/path/pod3.jpg");

        assertEquals("/path/pod1.jpg", invoice.getPodImagePath1());
        assertEquals("/path/pod2.jpg", invoice.getPodImagePath2());
        assertEquals("/path/pod3.jpg", invoice.getPodImagePath3());
    }

    @Test
    public void testSetPodImagePath_null() {
        invoice.setPodImagePath1("/path/exists.jpg");
        invoice.setPodImagePath1(null);
        assertNull(invoice.getPodImagePath1());
    }

    // ==================== Signature Image Path Tests ====================

    @Test
    public void testSetSignatureImagePath_normalValue() {
        invoice.setSignatureImagePath("/storage/images/signature.jpg");
        assertEquals("/storage/images/signature.jpg", invoice.getSignatureImagePath());
    }

    @Test
    public void testSetSignatureImagePath_emptyString() {
        invoice.setSignatureImagePath("");
        assertEquals("", invoice.getSignatureImagePath());
    }

    @Test
    public void testSetSignatureImagePath_null() {
        invoice.setSignatureImagePath(null);
        assertNull(invoice.getSignatureImagePath());
    }

    // ==================== Notes Tests ====================

    @Test
    public void testSetNotes_normalValue() {
        invoice.setNotes("Deliver to back door");
        assertEquals("Deliver to back door", invoice.getNotes());
    }

    @Test
    public void testSetNotes_multiline() {
        invoice.setNotes("Note 1\nNote 2\nNote 3");
        assertEquals("Note 1\nNote 2\nNote 3", invoice.getNotes());
    }

    @Test
    public void testSetNotes_longValue() {
        String longNotes = "A".repeat(1000);
        invoice.setNotes(longNotes);
        assertEquals(longNotes, invoice.getNotes());
    }

    @Test
    public void testSetNotes_emptyString() {
        invoice.setNotes("");
        assertEquals("", invoice.getNotes());
    }

    @Test
    public void testSetNotes_null() {
        invoice.setNotes(null);
        assertNull(invoice.getNotes());
    }

    // ==================== Original Image Path Tests ====================

    @Test
    public void testSetOriginalImagePath_normalValue() {
        invoice.setOriginalImagePath("/storage/images/invoice.jpg");
        assertEquals("/storage/images/invoice.jpg", invoice.getOriginalImagePath());
    }

    @Test
    public void testSetOriginalImagePath_null() {
        invoice.setOriginalImagePath(null);
        assertNull(invoice.getOriginalImagePath());
    }

    // ==================== Raw OCR Text Tests ====================

    @Test
    public void testSetRawOcrText_normalValue() {
        invoice.setRawOcrText("INVOICE\nCustomer: John Doe\nTotal: $100.00");
        assertEquals("INVOICE\nCustomer: John Doe\nTotal: $100.00", invoice.getRawOcrText());
    }

    @Test
    public void testSetRawOcrText_longText() {
        String longText = "Line\n".repeat(1000);
        invoice.setRawOcrText(longText);
        assertEquals(longText, invoice.getRawOcrText());
    }

    @Test
    public void testSetRawOcrText_null() {
        invoice.setRawOcrText(null);
        assertNull(invoice.getRawOcrText());
    }

    // ==================== Timestamp Tests ====================

    @Test
    public void testSetTimestamp_normalValue() {
        long timestamp = 1704067200000L; // Jan 1, 2024
        invoice.setTimestamp(timestamp);
        assertEquals(timestamp, invoice.getTimestamp());
    }

    @Test
    public void testSetTimestamp_zero() {
        invoice.setTimestamp(0);
        assertEquals(0, invoice.getTimestamp());
    }

    @Test
    public void testSetTimestamp_negativeValue() {
        // Negative timestamps represent dates before Unix epoch
        invoice.setTimestamp(-1000000000L);
        assertEquals(-1000000000L, invoice.getTimestamp());
    }

    // ==================== Complete Invoice Tests ====================

    @Test
    public void testCompleteInvoice() {
        invoice.setId(1);
        invoice.setInvoiceNumber("INV-2024-001");
        invoice.setCustomerName("John Doe");
        invoice.setAddress("123 Main Street, City, ST 12345");
        invoice.setPhone("(555) 123-4567");
        invoice.setItems("Refrigerator, Washer, Dryer");
        invoice.setPodImagePath1("/storage/pod1.jpg");
        invoice.setPodImagePath2("/storage/pod2.jpg");
        invoice.setPodImagePath3("/storage/pod3.jpg");
        invoice.setSignatureImagePath("/storage/signature.jpg");
        invoice.setNotes("Handle with care");
        invoice.setOriginalImagePath("/storage/original.jpg");
        invoice.setRawOcrText("Full OCR text here");
        invoice.setTimestamp(1704067200000L);

        assertEquals(1, invoice.getId());
        assertEquals("INV-2024-001", invoice.getInvoiceNumber());
        assertEquals("John Doe", invoice.getCustomerName());
        assertEquals("123 Main Street, City, ST 12345", invoice.getAddress());
        assertEquals("(555) 123-4567", invoice.getPhone());
        assertEquals("Refrigerator, Washer, Dryer", invoice.getItems());
        assertEquals("/storage/pod1.jpg", invoice.getPodImagePath1());
        assertEquals("/storage/pod2.jpg", invoice.getPodImagePath2());
        assertEquals("/storage/pod3.jpg", invoice.getPodImagePath3());
        assertEquals("/storage/signature.jpg", invoice.getSignatureImagePath());
        assertEquals("Handle with care", invoice.getNotes());
        assertEquals("/storage/original.jpg", invoice.getOriginalImagePath());
        assertEquals("Full OCR text here", invoice.getRawOcrText());
        assertEquals(1704067200000L, invoice.getTimestamp());
    }

    // ==================== Edge Cases ====================

    @Test
    public void testOverwriteValues() {
        invoice.setCustomerName("Initial Name");
        assertEquals("Initial Name", invoice.getCustomerName());

        invoice.setCustomerName("Updated Name");
        assertEquals("Updated Name", invoice.getCustomerName());
    }

    @Test
    public void testClearValues() {
        invoice.setCustomerName("John Doe");
        invoice.setCustomerName(null);
        assertNull(invoice.getCustomerName());
    }

    @Test
    public void testWhitespaceValues() {
        invoice.setCustomerName("  John Doe  ");
        assertEquals("  John Doe  ", invoice.getCustomerName());

        invoice.setInvoiceNumber("\tINV-001\n");
        assertEquals("\tINV-001\n", invoice.getInvoiceNumber());
    }

    @Test
    public void testUnicodeInAllFields() {
        invoice.setCustomerName("日本語 테스트 العربية");
        invoice.setAddress("123 Calle España, México");
        invoice.setNotes("Emoji test: 📦🚚✅");

        assertEquals("日本語 테스트 العربية", invoice.getCustomerName());
        assertEquals("123 Calle España, México", invoice.getAddress());
        assertEquals("Emoji test: 📦🚚✅", invoice.getNotes());
    }

    // ==================== Field Independence Tests ====================

    @Test
    public void testFieldsAreIndependent() {
        Invoice invoice1 = new Invoice();
        Invoice invoice2 = new Invoice();

        invoice1.setCustomerName("Customer 1");
        invoice2.setCustomerName("Customer 2");

        assertEquals("Customer 1", invoice1.getCustomerName());
        assertEquals("Customer 2", invoice2.getCustomerName());
    }

    @Test
    public void testTimestampIndependence() {
        Invoice invoice1 = new Invoice();
        try {
            Thread.sleep(10); // Small delay
        } catch (InterruptedException e) {
            // Ignore
        }
        Invoice invoice2 = new Invoice();

        assertTrue("Timestamps should be different",
                   invoice2.getTimestamp() >= invoice1.getTimestamp());
    }
}
