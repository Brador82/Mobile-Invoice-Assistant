package com.mobileinvoice.ocr;

import com.mobileinvoice.ocr.database.Invoice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for ExportHelper
 * Tests the export formatting logic and data transformation
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class ExportHelperTest {

    private List<Invoice> testInvoices;

    @Before
    public void setUp() {
        testInvoices = new ArrayList<>();
    }

    // ==================== Test Helper Methods ====================

    private Invoice createTestInvoice(int id, String invoiceNumber, String customerName,
                                       String address, String phone, String items) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomerName(customerName);
        invoice.setAddress(address);
        invoice.setPhone(phone);
        invoice.setItems(items);
        invoice.setTimestamp(System.currentTimeMillis());
        return invoice;
    }

    // ==================== CSV Escaping Tests ====================

    @Test
    public void testEscapeCSV_simpleString() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeCSV = ExportHelper.class.getDeclaredMethod("escapeCSV", String.class);
        escapeCSV.setAccessible(true);

        String result = (String) escapeCSV.invoke(helper, "Simple text");
        assertEquals("Simple text", result);
    }

    @Test
    public void testEscapeCSV_withComma() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeCSV = ExportHelper.class.getDeclaredMethod("escapeCSV", String.class);
        escapeCSV.setAccessible(true);

        String result = (String) escapeCSV.invoke(helper, "Text, with comma");
        assertEquals("\"Text, with comma\"", result);
    }

    @Test
    public void testEscapeCSV_withQuotes() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeCSV = ExportHelper.class.getDeclaredMethod("escapeCSV", String.class);
        escapeCSV.setAccessible(true);

        String result = (String) escapeCSV.invoke(helper, "Text with \"quotes\"");
        assertEquals("\"Text with \"\"quotes\"\"\"", result);
    }

    @Test
    public void testEscapeCSV_withNewline() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeCSV = ExportHelper.class.getDeclaredMethod("escapeCSV", String.class);
        escapeCSV.setAccessible(true);

        String result = (String) escapeCSV.invoke(helper, "Line 1\nLine 2");
        assertEquals("\"Line 1\nLine 2\"", result);
    }

    @Test
    public void testEscapeCSV_nullValue() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeCSV = ExportHelper.class.getDeclaredMethod("escapeCSV", String.class);
        escapeCSV.setAccessible(true);

        String result = (String) escapeCSV.invoke(helper, (String) null);
        assertEquals("", result);
    }

    @Test
    public void testEscapeCSV_emptyString() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeCSV = ExportHelper.class.getDeclaredMethod("escapeCSV", String.class);
        escapeCSV.setAccessible(true);

        String result = (String) escapeCSV.invoke(helper, "");
        assertEquals("", result);
    }

    @Test
    public void testEscapeCSV_complexString() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeCSV = ExportHelper.class.getDeclaredMethod("escapeCSV", String.class);
        escapeCSV.setAccessible(true);

        String result = (String) escapeCSV.invoke(helper, "John \"Johnny\" Doe, Jr.");
        assertEquals("\"John \"\"Johnny\"\" Doe, Jr.\"", result);
    }

    // ==================== TSV Escaping Tests ====================

    @Test
    public void testEscapeTSV_simpleString() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeTSV = ExportHelper.class.getDeclaredMethod("escapeTSV", String.class);
        escapeTSV.setAccessible(true);

        String result = (String) escapeTSV.invoke(helper, "Simple text");
        assertEquals("Simple text", result);
    }

    @Test
    public void testEscapeTSV_withTab() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeTSV = ExportHelper.class.getDeclaredMethod("escapeTSV", String.class);
        escapeTSV.setAccessible(true);

        String result = (String) escapeTSV.invoke(helper, "Text\twith\ttabs");
        assertEquals("Text with tabs", result);
    }

    @Test
    public void testEscapeTSV_withNewline() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeTSV = ExportHelper.class.getDeclaredMethod("escapeTSV", String.class);
        escapeTSV.setAccessible(true);

        String result = (String) escapeTSV.invoke(helper, "Line 1\nLine 2");
        assertEquals("Line 1 Line 2", result);
    }

    @Test
    public void testEscapeTSV_withCarriageReturn() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeTSV = ExportHelper.class.getDeclaredMethod("escapeTSV", String.class);
        escapeTSV.setAccessible(true);

        String result = (String) escapeTSV.invoke(helper, "Line 1\r\nLine 2");
        assertEquals("Line 1 Line 2", result);
    }

    @Test
    public void testEscapeTSV_nullValue() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeTSV = ExportHelper.class.getDeclaredMethod("escapeTSV", String.class);
        escapeTSV.setAccessible(true);

        String result = (String) escapeTSV.invoke(helper, (String) null);
        assertEquals("", result);
    }

    // ==================== Markdown Escaping Tests ====================

    @Test
    public void testEscapeMD_simpleString() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeMD = ExportHelper.class.getDeclaredMethod("escapeMD", String.class);
        escapeMD.setAccessible(true);

        String result = (String) escapeMD.invoke(helper, "Simple text");
        assertEquals("Simple text", result);
    }

    @Test
    public void testEscapeMD_withPipe() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeMD = ExportHelper.class.getDeclaredMethod("escapeMD", String.class);
        escapeMD.setAccessible(true);

        String result = (String) escapeMD.invoke(helper, "Text | with | pipes");
        assertEquals("Text \\| with \\| pipes", result);
    }

    @Test
    public void testEscapeMD_withAsterisks() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeMD = ExportHelper.class.getDeclaredMethod("escapeMD", String.class);
        escapeMD.setAccessible(true);

        String result = (String) escapeMD.invoke(helper, "Text *with* asterisks");
        assertEquals("Text \\*with\\* asterisks", result);
    }

    @Test
    public void testEscapeMD_withUnderscores() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeMD = ExportHelper.class.getDeclaredMethod("escapeMD", String.class);
        escapeMD.setAccessible(true);

        String result = (String) escapeMD.invoke(helper, "Text _with_ underscores");
        assertEquals("Text \\_with\\_ underscores", result);
    }

    @Test
    public void testEscapeMD_nullValue() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeMD = ExportHelper.class.getDeclaredMethod("escapeMD", String.class);
        escapeMD.setAccessible(true);

        String result = (String) escapeMD.invoke(helper, (String) null);
        assertEquals("", result);
    }

    @Test
    public void testEscapeMD_combinedSpecialChars() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeMD = ExportHelper.class.getDeclaredMethod("escapeMD", String.class);
        escapeMD.setAccessible(true);

        String result = (String) escapeMD.invoke(helper, "Item | *Bold* | _Italic_");
        assertEquals("Item \\| \\*Bold\\* \\| \\_Italic\\_", result);
    }

    // ==================== Card Folder Name Tests ====================

    @Test
    public void testCreateCardFolderName_normal() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method createCardFolderName = ExportHelper.class.getDeclaredMethod("createCardFolderName", Invoice.class);
        createCardFolderName.setAccessible(true);

        Invoice invoice = createTestInvoice(1, "INV001", "John Doe", "123 Main St", "(555) 123-4567", "Refrigerator");
        String result = (String) createCardFolderName.invoke(helper, invoice);

        assertEquals("John Doe_INV001", result);
    }

    @Test
    public void testCreateCardFolderName_specialCharacters() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method createCardFolderName = ExportHelper.class.getDeclaredMethod("createCardFolderName", Invoice.class);
        createCardFolderName.setAccessible(true);

        Invoice invoice = createTestInvoice(1, "INV-001", "John's Place & Co.", "123 Main St", "(555) 123-4567", "Refrigerator");
        String result = (String) createCardFolderName.invoke(helper, invoice);

        // Special characters should be removed
        assertTrue("Folder name should not contain special chars", !result.contains("'"));
        assertTrue("Folder name should not contain special chars", !result.contains("&"));
    }

    @Test
    public void testCreateCardFolderName_longCustomerName() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method createCardFolderName = ExportHelper.class.getDeclaredMethod("createCardFolderName", Invoice.class);
        createCardFolderName.setAccessible(true);

        Invoice invoice = createTestInvoice(1, "INV001", "Very Long Customer Name That Exceeds Twenty Characters",
                                            "123 Main St", "(555) 123-4567", "Refrigerator");
        String result = (String) createCardFolderName.invoke(helper, invoice);

        // Customer name should be truncated to 20 chars
        assertTrue("Folder name should be limited in length", result.length() <= 30);
    }

    @Test
    public void testCreateCardFolderName_nullCustomerName() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method createCardFolderName = ExportHelper.class.getDeclaredMethod("createCardFolderName", Invoice.class);
        createCardFolderName.setAccessible(true);

        Invoice invoice = createTestInvoice(1, "INV001", null, "123 Main St", "(555) 123-4567", "Refrigerator");
        String result = (String) createCardFolderName.invoke(helper, invoice);

        assertTrue("Should use 'Unknown' for null customer name", result.contains("Unknown"));
    }

    @Test
    public void testCreateCardFolderName_nullInvoiceNumber() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method createCardFolderName = ExportHelper.class.getDeclaredMethod("createCardFolderName", Invoice.class);
        createCardFolderName.setAccessible(true);

        Invoice invoice = createTestInvoice(5, null, "John Doe", "123 Main St", "(555) 123-4567", "Refrigerator");
        String result = (String) createCardFolderName.invoke(helper, invoice);

        // Should use INV + id for null invoice number
        assertTrue("Should fallback to INV + ID", result.contains("INV5"));
    }

    // ==================== Export Summary Tests ====================

    @Test
    public void testGetExportSummary_emptyList() {
        ExportHelper helper = new ExportHelper(null);
        String summary = helper.getExportSummary(new ArrayList<>());
        assertEquals("No invoices available for export", summary);
    }

    @Test
    public void testGetExportSummary_nullList() {
        ExportHelper helper = new ExportHelper(null);
        String summary = helper.getExportSummary(null);
        assertEquals("No invoices available for export", summary);
    }

    @Test
    public void testGetExportSummary_singleInvoice() {
        ExportHelper helper = new ExportHelper(null);

        Invoice invoice = createTestInvoice(1, "INV001", "John Doe", "123 Main St", "(555) 123-4567", "Refrigerator");
        invoice.setPodImagePath1("/path/to/pod.jpg");
        invoice.setSignatureImagePath("/path/to/signature.jpg");

        testInvoices.add(invoice);

        String summary = helper.getExportSummary(testInvoices);

        assertTrue("Summary should contain total count", summary.contains("Total Invoices: 1"));
        assertTrue("Summary should contain POD count", summary.contains("With POD Photos: 1"));
        assertTrue("Summary should contain signature count", summary.contains("With Signatures: 1"));
        assertTrue("Summary should contain items count", summary.contains("With Items: 1"));
    }

    @Test
    public void testGetExportSummary_multipleInvoices() {
        ExportHelper helper = new ExportHelper(null);

        // Invoice with POD and signature
        Invoice invoice1 = createTestInvoice(1, "INV001", "John Doe", "123 Main St", "(555) 123-4567", "Refrigerator");
        invoice1.setPodImagePath1("/path/to/pod.jpg");
        invoice1.setSignatureImagePath("/path/to/signature.jpg");

        // Invoice with only POD
        Invoice invoice2 = createTestInvoice(2, "INV002", "Jane Smith", "456 Oak Ave", "(555) 987-6543", "Washer");
        invoice2.setPodImagePath1("/path/to/pod2.jpg");

        // Invoice with nothing
        Invoice invoice3 = createTestInvoice(3, "INV003", "Bob Johnson", "789 Pine Rd", "(555) 555-5555", null);

        testInvoices.add(invoice1);
        testInvoices.add(invoice2);
        testInvoices.add(invoice3);

        String summary = helper.getExportSummary(testInvoices);

        assertTrue("Summary should contain total count of 3", summary.contains("Total Invoices: 3"));
        assertTrue("Summary should contain POD count of 2", summary.contains("With POD Photos: 2"));
        assertTrue("Summary should contain signature count of 1", summary.contains("With Signatures: 1"));
        assertTrue("Summary should contain items count of 2", summary.contains("With Items: 2"));
    }

    @Test
    public void testGetExportSummary_percentages() {
        ExportHelper helper = new ExportHelper(null);

        // 2 out of 4 invoices with POD (50%)
        for (int i = 0; i < 4; i++) {
            Invoice invoice = createTestInvoice(i, "INV00" + i, "Customer " + i, "Address " + i, "(555) 000-000" + i, "Items");
            if (i < 2) {
                invoice.setPodImagePath1("/path/pod" + i + ".jpg");
            }
            testInvoices.add(invoice);
        }

        String summary = helper.getExportSummary(testInvoices);

        assertTrue("Summary should contain 50% for POD", summary.contains("50.0%"));
    }

    @Test
    public void testGetExportSummary_multiplePodPaths() {
        ExportHelper helper = new ExportHelper(null);

        // Invoice with multiple POD paths
        Invoice invoice = createTestInvoice(1, "INV001", "John Doe", "123 Main St", "(555) 123-4567", "Refrigerator");
        invoice.setPodImagePath2("/path/to/pod2.jpg"); // Only path2, not path1

        testInvoices.add(invoice);

        String summary = helper.getExportSummary(testInvoices);

        assertTrue("Summary should count invoice with only pod2", summary.contains("With POD Photos: 1"));
    }

    @Test
    public void testGetExportSummary_pod3Only() {
        ExportHelper helper = new ExportHelper(null);

        Invoice invoice = createTestInvoice(1, "INV001", "John Doe", "123 Main St", "(555) 123-4567", "Refrigerator");
        invoice.setPodImagePath3("/path/to/pod3.jpg"); // Only path3

        testInvoices.add(invoice);

        String summary = helper.getExportSummary(testInvoices);

        assertTrue("Summary should count invoice with only pod3", summary.contains("With POD Photos: 1"));
    }

    @Test
    public void testGetExportSummary_emptyPodPath() {
        ExportHelper helper = new ExportHelper(null);

        Invoice invoice = createTestInvoice(1, "INV001", "John Doe", "123 Main St", "(555) 123-4567", "Refrigerator");
        invoice.setPodImagePath1(""); // Empty string, not null

        testInvoices.add(invoice);

        String summary = helper.getExportSummary(testInvoices);

        assertTrue("Summary should not count empty POD paths", summary.contains("With POD Photos: 0"));
    }

    @Test
    public void testGetExportSummary_emptyItems() {
        ExportHelper helper = new ExportHelper(null);

        Invoice invoice = createTestInvoice(1, "INV001", "John Doe", "123 Main St", "(555) 123-4567", "");

        testInvoices.add(invoice);

        String summary = helper.getExportSummary(testInvoices);

        assertTrue("Summary should not count empty items", summary.contains("With Items: 0"));
    }

    // ==================== Export Callback Tests ====================

    @Test
    public void testExportCompleteCallback_interface() {
        ExportHelper helper = new ExportHelper(null);

        final boolean[] callbackInvoked = {false};
        final int[] invoiceCount = {0};

        helper.setExportCompleteCallback((exportFolder, count) -> {
            callbackInvoked[0] = true;
            invoiceCount[0] = count;
        });

        // Callback is set but we can't easily trigger it without context
        // This test verifies the callback mechanism exists
        assertNotNull("Helper should support callbacks", helper);
    }

    // ==================== Data Integrity Tests ====================

    @Test
    public void testInvoiceDataPreservation() {
        Invoice invoice = createTestInvoice(1, "INV-2024-001", "John \"Johnny\" Doe",
                                            "123 Main St, Apt 4B", "(555) 123-4567", "Refrigerator, Washer");
        invoice.setNotes("Special instructions:\n- Handle with care\n- Call before delivery");

        // Verify all data is preserved
        assertEquals("INV-2024-001", invoice.getInvoiceNumber());
        assertEquals("John \"Johnny\" Doe", invoice.getCustomerName());
        assertEquals("123 Main St, Apt 4B", invoice.getAddress());
        assertEquals("(555) 123-4567", invoice.getPhone());
        assertEquals("Refrigerator, Washer", invoice.getItems());
        assertTrue(invoice.getNotes().contains("Handle with care"));
    }

    @Test
    public void testTimestampPreservation() {
        long before = System.currentTimeMillis();
        Invoice invoice = new Invoice();
        long after = System.currentTimeMillis();

        assertTrue("Timestamp should be set on creation",
                   invoice.getTimestamp() >= before && invoice.getTimestamp() <= after);
    }

    // ==================== Edge Cases ====================

    @Test
    public void testEscapeCSV_onlySpecialChars() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeCSV = ExportHelper.class.getDeclaredMethod("escapeCSV", String.class);
        escapeCSV.setAccessible(true);

        String result = (String) escapeCSV.invoke(helper, ",,,");
        assertEquals("\",,,\"", result);
    }

    @Test
    public void testEscapeTSV_onlyTabs() throws Exception {
        ExportHelper helper = new ExportHelper(null);
        Method escapeTSV = ExportHelper.class.getDeclaredMethod("escapeTSV", String.class);
        escapeTSV.setAccessible(true);

        String result = (String) escapeTSV.invoke(helper, "\t\t\t");
        assertEquals("   ", result);
    }

    @Test
    public void testLargeInvoiceList() {
        ExportHelper helper = new ExportHelper(null);

        // Create 100 invoices
        for (int i = 0; i < 100; i++) {
            Invoice invoice = createTestInvoice(i, "INV" + i, "Customer " + i,
                                                "Address " + i, "(555) 000-" + String.format("%04d", i),
                                                "Item " + i);
            if (i % 2 == 0) {
                invoice.setPodImagePath1("/path/pod" + i + ".jpg");
            }
            if (i % 3 == 0) {
                invoice.setSignatureImagePath("/path/sig" + i + ".jpg");
            }
            testInvoices.add(invoice);
        }

        String summary = helper.getExportSummary(testInvoices);

        assertTrue("Summary should handle 100 invoices", summary.contains("Total Invoices: 100"));
        assertTrue("Summary should have 50 PODs (every other)", summary.contains("With POD Photos: 50"));
        assertTrue("Summary should have 34 signatures (every third)", summary.contains("With Signatures: 34"));
    }
}
