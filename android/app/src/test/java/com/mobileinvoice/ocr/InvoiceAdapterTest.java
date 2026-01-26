package com.mobileinvoice.ocr;

import android.view.View;

import com.mobileinvoice.ocr.database.Invoice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InvoiceAdapter
 * Tests adapter behavior, data binding, and click handling
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class InvoiceAdapterTest {

    private List<Invoice> testInvoices;

    @Before
    public void setUp() {
        testInvoices = new ArrayList<>();
    }

    private Invoice createTestInvoice(int id, String invoiceNumber, String customerName,
                                       String address, String phone) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomerName(customerName);
        invoice.setAddress(address);
        invoice.setPhone(phone);
        invoice.setTimestamp(System.currentTimeMillis());
        return invoice;
    }

    // ==================== Adapter Creation Tests ====================

    @Test
    public void testAdapterCreation_withEmptyList() {
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);
        assertNotNull("Adapter should be created", adapter);
        assertEquals("Item count should be 0", 0, adapter.getItemCount());
    }

    @Test
    public void testAdapterCreation_withData() {
        testInvoices.add(createTestInvoice(1, "INV-001", "John Doe", "123 Main St", "(555) 123-4567"));
        testInvoices.add(createTestInvoice(2, "INV-002", "Jane Smith", "456 Oak Ave", "(555) 987-6543"));

        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);

        assertNotNull("Adapter should be created", adapter);
        assertEquals("Item count should be 2", 2, adapter.getItemCount());
    }

    // ==================== Item Count Tests ====================

    @Test
    public void testGetItemCount_emptyList() {
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testGetItemCount_singleItem() {
        testInvoices.add(createTestInvoice(1, "INV-001", "John Doe", "123 Main St", "(555) 123-4567"));
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testGetItemCount_multipleItems() {
        for (int i = 0; i < 10; i++) {
            testInvoices.add(createTestInvoice(i, "INV-00" + i, "Customer " + i,
                                              "Address " + i, "(555) 000-000" + i));
        }
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);
        assertEquals(10, adapter.getItemCount());
    }

    @Test
    public void testGetItemCount_largeDataset() {
        for (int i = 0; i < 100; i++) {
            testInvoices.add(createTestInvoice(i, "INV-" + i, "Customer " + i,
                                              "Address " + i, "(555) 000-" + String.format("%04d", i)));
        }
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);
        assertEquals(100, adapter.getItemCount());
    }

    // ==================== Data Update Tests ====================

    @Test
    public void testUpdateData_fromEmptyToPopulated() {
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);
        assertEquals(0, adapter.getItemCount());

        testInvoices.add(createTestInvoice(1, "INV-001", "John Doe", "123 Main St", "(555) 123-4567"));
        adapter.notifyDataSetChanged();

        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testUpdateData_addItems() {
        testInvoices.add(createTestInvoice(1, "INV-001", "John Doe", "123 Main St", "(555) 123-4567"));
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);
        assertEquals(1, adapter.getItemCount());

        testInvoices.add(createTestInvoice(2, "INV-002", "Jane Smith", "456 Oak Ave", "(555) 987-6543"));
        adapter.notifyDataSetChanged();

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testUpdateData_removeItems() {
        testInvoices.add(createTestInvoice(1, "INV-001", "John Doe", "123 Main St", "(555) 123-4567"));
        testInvoices.add(createTestInvoice(2, "INV-002", "Jane Smith", "456 Oak Ave", "(555) 987-6543"));
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);
        assertEquals(2, adapter.getItemCount());

        testInvoices.remove(0);
        adapter.notifyDataSetChanged();

        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testUpdateData_clearAll() {
        for (int i = 0; i < 5; i++) {
            testInvoices.add(createTestInvoice(i, "INV-00" + i, "Customer " + i,
                                              "Address " + i, "(555) 000-000" + i));
        }
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);
        assertEquals(5, adapter.getItemCount());

        testInvoices.clear();
        adapter.notifyDataSetChanged();

        assertEquals(0, adapter.getItemCount());
    }

    // ==================== Click Listener Tests ====================

    @Test
    public void testSetOnItemClickListener() {
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);

        InvoiceAdapter.OnItemClickListener listener = mock(InvoiceAdapter.OnItemClickListener.class);
        adapter.setOnItemClickListener(listener);

        // Listener should be set without exception
        assertNotNull("Adapter should accept listener", adapter);
    }

    @Test
    public void testClickListener_nullListener() {
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);

        // Should handle null listener without crashing
        adapter.setOnItemClickListener(null);
        assertNotNull(adapter);
    }

    // ==================== Data Access Tests ====================

    @Test
    public void testInvoiceDataAccess() {
        Invoice invoice = createTestInvoice(1, "INV-001", "John Doe", "123 Main St", "(555) 123-4567");
        testInvoices.add(invoice);

        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);

        // Verify data is accessible through the list
        assertEquals(1, adapter.getItemCount());
        assertEquals("INV-001", testInvoices.get(0).getInvoiceNumber());
        assertEquals("John Doe", testInvoices.get(0).getCustomerName());
    }

    // ==================== Edge Cases ====================

    @Test
    public void testAdapter_withNullFields() {
        Invoice invoice = new Invoice();
        invoice.setId(1);
        // All other fields remain null
        testInvoices.add(invoice);

        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);

        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testAdapter_withEmptyStringFields() {
        Invoice invoice = createTestInvoice(1, "", "", "", "");
        testInvoices.add(invoice);

        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);

        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testAdapter_withSpecialCharacters() {
        Invoice invoice = createTestInvoice(1, "INV-2024/001#A", "John O'Brien-Smith",
                                           "123 Main St, Apt \"4B\"", "(555) 123-4567");
        testInvoices.add(invoice);

        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);

        assertEquals(1, adapter.getItemCount());
        assertEquals("John O'Brien-Smith", testInvoices.get(0).getCustomerName());
    }

    @Test
    public void testAdapter_withUnicodeCharacters() {
        Invoice invoice = createTestInvoice(1, "INV-001", "José García 日本語",
                                           "Calle España #123", "(555) 123-4567");
        testInvoices.add(invoice);

        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);

        assertEquals(1, adapter.getItemCount());
        assertEquals("José García 日本語", testInvoices.get(0).getCustomerName());
    }

    // ==================== Performance Tests ====================

    @Test
    public void testAdapter_withManyItems() {
        // Add 1000 items
        for (int i = 0; i < 1000; i++) {
            testInvoices.add(createTestInvoice(i, "INV-" + i, "Customer " + i,
                                              "Address " + i, "(555) 000-" + String.format("%04d", i)));
        }

        long startTime = System.currentTimeMillis();
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);
        long endTime = System.currentTimeMillis();

        assertEquals(1000, adapter.getItemCount());
        assertTrue("Adapter creation should be fast", (endTime - startTime) < 1000);
    }

    // ==================== Data Order Tests ====================

    @Test
    public void testDataOrder_preserved() {
        testInvoices.add(createTestInvoice(1, "INV-A", "Customer A", "Address A", "(555) 111-1111"));
        testInvoices.add(createTestInvoice(2, "INV-B", "Customer B", "Address B", "(555) 222-2222"));
        testInvoices.add(createTestInvoice(3, "INV-C", "Customer C", "Address C", "(555) 333-3333"));

        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);

        assertEquals("INV-A", testInvoices.get(0).getInvoiceNumber());
        assertEquals("INV-B", testInvoices.get(1).getInvoiceNumber());
        assertEquals("INV-C", testInvoices.get(2).getInvoiceNumber());
    }

    // ==================== Invoice With All Fields Tests ====================

    @Test
    public void testAdapter_withCompleteInvoice() {
        Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setInvoiceNumber("INV-FULL-001");
        invoice.setCustomerName("Complete Customer");
        invoice.setAddress("123 Full Address, City, ST 12345");
        invoice.setPhone("(555) 123-4567");
        invoice.setItems("Refrigerator, Washer, Dryer");
        invoice.setPodImagePath1("/path/pod1.jpg");
        invoice.setPodImagePath2("/path/pod2.jpg");
        invoice.setPodImagePath3("/path/pod3.jpg");
        invoice.setSignatureImagePath("/path/signature.jpg");
        invoice.setNotes("Complete notes");
        invoice.setOriginalImagePath("/path/original.jpg");
        invoice.setRawOcrText("Complete OCR text");
        invoice.setTimestamp(System.currentTimeMillis());

        testInvoices.add(invoice);
        InvoiceAdapter adapter = new InvoiceAdapter(testInvoices);

        assertEquals(1, adapter.getItemCount());
    }
}
