package com.mobileinvoice.ocr.database;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Instrumentation tests for InvoiceDao
 * Tests all CRUD operations and queries against an in-memory database
 */
@RunWith(AndroidJUnit4.class)
public class InvoiceDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private InvoiceDatabase database;
    private InvoiceDao invoiceDao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, InvoiceDatabase.class)
                .allowMainThreadQueries()
                .build();
        invoiceDao = database.invoiceDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    // ==================== Helper Methods ====================

    private Invoice createTestInvoice(String invoiceNumber, String customerName) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomerName(customerName);
        invoice.setAddress("123 Test Street");
        invoice.setPhone("(555) 123-4567");
        invoice.setItems("Refrigerator");
        invoice.setTimestamp(System.currentTimeMillis());
        return invoice;
    }

    private <T> T getValueBlocking(LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);

        liveData.observeForever(value -> {
            data[0] = value;
            latch.countDown();
        });

        latch.await(2, TimeUnit.SECONDS);
        return (T) data[0];
    }

    // ==================== Insert Tests ====================

    @Test
    public void testInsert_singleInvoice() {
        Invoice invoice = createTestInvoice("INV-001", "John Doe");
        long id = invoiceDao.insert(invoice);

        assertTrue("Insert should return positive ID", id > 0);
    }

    @Test
    public void testInsert_multipleInvoices() {
        Invoice invoice1 = createTestInvoice("INV-001", "John Doe");
        Invoice invoice2 = createTestInvoice("INV-002", "Jane Smith");

        long id1 = invoiceDao.insert(invoice1);
        long id2 = invoiceDao.insert(invoice2);

        assertTrue("First ID should be positive", id1 > 0);
        assertTrue("Second ID should be positive", id2 > 0);
        assertNotEquals("IDs should be different", id1, id2);
    }

    @Test
    public void testInsert_duplicateInvoiceNumbers() {
        // Room allows duplicate invoice numbers (no unique constraint)
        Invoice invoice1 = createTestInvoice("INV-001", "John Doe");
        Invoice invoice2 = createTestInvoice("INV-001", "Jane Smith"); // Same invoice number

        long id1 = invoiceDao.insert(invoice1);
        long id2 = invoiceDao.insert(invoice2);

        assertTrue("Both inserts should succeed", id1 > 0 && id2 > 0);
        assertNotEquals("IDs should still be different", id1, id2);
    }

    @Test
    public void testInsert_withAllFields() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-FULL");
        invoice.setCustomerName("Complete Customer");
        invoice.setAddress("456 Full Address, City, ST 12345");
        invoice.setPhone("(555) 987-6543");
        invoice.setItems("Washer, Dryer");
        invoice.setPodImagePath1("/path/pod1.jpg");
        invoice.setPodImagePath2("/path/pod2.jpg");
        invoice.setPodImagePath3("/path/pod3.jpg");
        invoice.setSignatureImagePath("/path/signature.jpg");
        invoice.setNotes("Special instructions");
        invoice.setOriginalImagePath("/path/original.jpg");
        invoice.setRawOcrText("Full OCR text");

        long id = invoiceDao.insert(invoice);
        assertTrue("Insert with all fields should succeed", id > 0);
    }

    // ==================== Query Tests ====================

    @Test
    public void testGetAllInvoicesSync_empty() {
        List<Invoice> invoices = invoiceDao.getAllInvoicesSync();
        assertNotNull("Result should not be null", invoices);
        assertTrue("Result should be empty", invoices.isEmpty());
    }

    @Test
    public void testGetAllInvoicesSync_withData() {
        invoiceDao.insert(createTestInvoice("INV-001", "John Doe"));
        invoiceDao.insert(createTestInvoice("INV-002", "Jane Smith"));

        List<Invoice> invoices = invoiceDao.getAllInvoicesSync();

        assertEquals("Should return 2 invoices", 2, invoices.size());
    }

    @Test
    public void testGetAllInvoicesSync_orderByTimestampDesc() throws InterruptedException {
        Invoice older = createTestInvoice("INV-OLD", "Old Customer");
        older.setTimestamp(1000L);
        invoiceDao.insert(older);

        Thread.sleep(10); // Small delay to ensure different timestamps

        Invoice newer = createTestInvoice("INV-NEW", "New Customer");
        newer.setTimestamp(2000L);
        invoiceDao.insert(newer);

        List<Invoice> invoices = invoiceDao.getAllInvoicesSync();

        assertEquals("Should return 2 invoices", 2, invoices.size());
        assertEquals("First should be newer (DESC order)", "INV-NEW", invoices.get(0).getInvoiceNumber());
        assertEquals("Second should be older", "INV-OLD", invoices.get(1).getInvoiceNumber());
    }

    @Test
    public void testGetInvoiceByIdSync_exists() {
        Invoice original = createTestInvoice("INV-001", "John Doe");
        long id = invoiceDao.insert(original);

        Invoice retrieved = invoiceDao.getInvoiceByIdSync((int) id);

        assertNotNull("Should find invoice by ID", retrieved);
        assertEquals("INV-001", retrieved.getInvoiceNumber());
        assertEquals("John Doe", retrieved.getCustomerName());
    }

    @Test
    public void testGetInvoiceByIdSync_notExists() {
        Invoice retrieved = invoiceDao.getInvoiceByIdSync(999);
        assertNull("Should return null for non-existent ID", retrieved);
    }

    @Test
    public void testGetAllInvoices_liveData() throws InterruptedException {
        invoiceDao.insert(createTestInvoice("INV-001", "John Doe"));

        LiveData<List<Invoice>> liveData = invoiceDao.getAllInvoices();
        List<Invoice> invoices = getValueBlocking(liveData);

        assertNotNull("LiveData should return value", invoices);
        assertEquals("Should have 1 invoice", 1, invoices.size());
    }

    @Test
    public void testGetInvoiceById_liveData() throws InterruptedException {
        Invoice original = createTestInvoice("INV-001", "John Doe");
        long id = invoiceDao.insert(original);

        LiveData<Invoice> liveData = invoiceDao.getInvoiceById((int) id);
        Invoice invoice = getValueBlocking(liveData);

        assertNotNull("LiveData should return invoice", invoice);
        assertEquals("INV-001", invoice.getInvoiceNumber());
    }

    @Test
    public void testGetInvoiceCount() throws InterruptedException {
        LiveData<Integer> countLiveData = invoiceDao.getInvoiceCount();
        Integer initialCount = getValueBlocking(countLiveData);
        assertEquals("Initial count should be 0", Integer.valueOf(0), initialCount);

        invoiceDao.insert(createTestInvoice("INV-001", "Customer 1"));
        invoiceDao.insert(createTestInvoice("INV-002", "Customer 2"));

        // Re-observe to get updated count
        countLiveData = invoiceDao.getInvoiceCount();
        Integer newCount = getValueBlocking(countLiveData);
        assertEquals("Count should be 2", Integer.valueOf(2), newCount);
    }

    // ==================== Update Tests ====================

    @Test
    public void testUpdate_singleField() {
        Invoice invoice = createTestInvoice("INV-001", "John Doe");
        long id = invoiceDao.insert(invoice);

        Invoice retrieved = invoiceDao.getInvoiceByIdSync((int) id);
        retrieved.setCustomerName("John Updated");
        invoiceDao.update(retrieved);

        Invoice updated = invoiceDao.getInvoiceByIdSync((int) id);
        assertEquals("Name should be updated", "John Updated", updated.getCustomerName());
        assertEquals("Invoice number should remain same", "INV-001", updated.getInvoiceNumber());
    }

    @Test
    public void testUpdate_multipleFields() {
        Invoice invoice = createTestInvoice("INV-001", "John Doe");
        long id = invoiceDao.insert(invoice);

        Invoice retrieved = invoiceDao.getInvoiceByIdSync((int) id);
        retrieved.setCustomerName("Updated Customer");
        retrieved.setAddress("Updated Address");
        retrieved.setPhone("(555) 999-8888");
        retrieved.setItems("Updated Items");
        retrieved.setNotes("Updated Notes");
        invoiceDao.update(retrieved);

        Invoice updated = invoiceDao.getInvoiceByIdSync((int) id);
        assertEquals("Updated Customer", updated.getCustomerName());
        assertEquals("Updated Address", updated.getAddress());
        assertEquals("(555) 999-8888", updated.getPhone());
        assertEquals("Updated Items", updated.getItems());
        assertEquals("Updated Notes", updated.getNotes());
    }

    @Test
    public void testUpdate_imagePaths() {
        Invoice invoice = createTestInvoice("INV-001", "John Doe");
        long id = invoiceDao.insert(invoice);

        Invoice retrieved = invoiceDao.getInvoiceByIdSync((int) id);
        retrieved.setPodImagePath1("/new/pod1.jpg");
        retrieved.setSignatureImagePath("/new/signature.jpg");
        invoiceDao.update(retrieved);

        Invoice updated = invoiceDao.getInvoiceByIdSync((int) id);
        assertEquals("/new/pod1.jpg", updated.getPodImagePath1());
        assertEquals("/new/signature.jpg", updated.getSignatureImagePath());
    }

    @Test
    public void testUpdate_doesNotAffectOthers() {
        Invoice invoice1 = createTestInvoice("INV-001", "Customer 1");
        Invoice invoice2 = createTestInvoice("INV-002", "Customer 2");
        long id1 = invoiceDao.insert(invoice1);
        long id2 = invoiceDao.insert(invoice2);

        Invoice retrieved1 = invoiceDao.getInvoiceByIdSync((int) id1);
        retrieved1.setCustomerName("Updated Customer 1");
        invoiceDao.update(retrieved1);

        Invoice retrieved2 = invoiceDao.getInvoiceByIdSync((int) id2);
        assertEquals("Customer 2", retrieved2.getCustomerName());
    }

    // ==================== Delete Tests ====================

    @Test
    public void testDelete_singleInvoice() {
        Invoice invoice = createTestInvoice("INV-001", "John Doe");
        long id = invoiceDao.insert(invoice);

        Invoice retrieved = invoiceDao.getInvoiceByIdSync((int) id);
        invoiceDao.delete(retrieved);

        Invoice deleted = invoiceDao.getInvoiceByIdSync((int) id);
        assertNull("Invoice should be deleted", deleted);
    }

    @Test
    public void testDelete_doesNotAffectOthers() {
        Invoice invoice1 = createTestInvoice("INV-001", "Customer 1");
        Invoice invoice2 = createTestInvoice("INV-002", "Customer 2");
        long id1 = invoiceDao.insert(invoice1);
        long id2 = invoiceDao.insert(invoice2);

        Invoice toDelete = invoiceDao.getInvoiceByIdSync((int) id1);
        invoiceDao.delete(toDelete);

        assertNull("Deleted invoice should be gone", invoiceDao.getInvoiceByIdSync((int) id1));
        assertNotNull("Other invoice should remain", invoiceDao.getInvoiceByIdSync((int) id2));
    }

    @Test
    public void testDeleteAll() {
        invoiceDao.insert(createTestInvoice("INV-001", "Customer 1"));
        invoiceDao.insert(createTestInvoice("INV-002", "Customer 2"));
        invoiceDao.insert(createTestInvoice("INV-003", "Customer 3"));

        List<Invoice> before = invoiceDao.getAllInvoicesSync();
        assertEquals("Should have 3 invoices", 3, before.size());

        invoiceDao.deleteAll();

        List<Invoice> after = invoiceDao.getAllInvoicesSync();
        assertTrue("All invoices should be deleted", after.isEmpty());
    }

    @Test
    public void testDeleteAll_emptyDatabase() {
        // Should not throw exception
        invoiceDao.deleteAll();

        List<Invoice> invoices = invoiceDao.getAllInvoicesSync();
        assertTrue("Database should still be empty", invoices.isEmpty());
    }

    // ==================== Data Integrity Tests ====================

    @Test
    public void testDataIntegrity_allFieldsPreserved() {
        Invoice original = new Invoice();
        original.setInvoiceNumber("INV-INTEGRITY");
        original.setCustomerName("Test Customer");
        original.setAddress("123 Test St, Test City, TS 12345");
        original.setPhone("(555) 123-4567");
        original.setItems("Refrigerator, Washer, Dryer");
        original.setPodImagePath1("/storage/pod1.jpg");
        original.setPodImagePath2("/storage/pod2.jpg");
        original.setPodImagePath3("/storage/pod3.jpg");
        original.setSignatureImagePath("/storage/signature.jpg");
        original.setNotes("Test notes with\nmultiple lines");
        original.setOriginalImagePath("/storage/original.jpg");
        original.setRawOcrText("Raw OCR text from image");
        original.setTimestamp(1704067200000L);

        long id = invoiceDao.insert(original);
        Invoice retrieved = invoiceDao.getInvoiceByIdSync((int) id);

        assertEquals(original.getInvoiceNumber(), retrieved.getInvoiceNumber());
        assertEquals(original.getCustomerName(), retrieved.getCustomerName());
        assertEquals(original.getAddress(), retrieved.getAddress());
        assertEquals(original.getPhone(), retrieved.getPhone());
        assertEquals(original.getItems(), retrieved.getItems());
        assertEquals(original.getPodImagePath1(), retrieved.getPodImagePath1());
        assertEquals(original.getPodImagePath2(), retrieved.getPodImagePath2());
        assertEquals(original.getPodImagePath3(), retrieved.getPodImagePath3());
        assertEquals(original.getSignatureImagePath(), retrieved.getSignatureImagePath());
        assertEquals(original.getNotes(), retrieved.getNotes());
        assertEquals(original.getOriginalImagePath(), retrieved.getOriginalImagePath());
        assertEquals(original.getRawOcrText(), retrieved.getRawOcrText());
        assertEquals(original.getTimestamp(), retrieved.getTimestamp());
    }

    @Test
    public void testDataIntegrity_specialCharacters() {
        Invoice invoice = createTestInvoice("INV-SPECIAL", "John O'Brien-Smith");
        invoice.setAddress("123 Main St, Suite #5, \"The Building\"");
        invoice.setNotes("Notes with 'quotes' and \"double quotes\" and <special> chars & more");

        long id = invoiceDao.insert(invoice);
        Invoice retrieved = invoiceDao.getInvoiceByIdSync((int) id);

        assertEquals("John O'Brien-Smith", retrieved.getCustomerName());
        assertEquals("123 Main St, Suite #5, \"The Building\"", retrieved.getAddress());
        assertTrue(retrieved.getNotes().contains("'quotes'"));
        assertTrue(retrieved.getNotes().contains("\"double quotes\""));
    }

    @Test
    public void testDataIntegrity_unicodeCharacters() {
        Invoice invoice = createTestInvoice("INV-UNICODE", "José García 日本語");
        invoice.setAddress("Calle España #123, México 📍");
        invoice.setNotes("Notes with emoji: 📦🚚✅");

        long id = invoiceDao.insert(invoice);
        Invoice retrieved = invoiceDao.getInvoiceByIdSync((int) id);

        assertEquals("José García 日本語", retrieved.getCustomerName());
        assertTrue(retrieved.getAddress().contains("México"));
        assertTrue(retrieved.getNotes().contains("📦"));
    }

    @Test
    public void testDataIntegrity_nullFields() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-NULLS");
        invoice.setCustomerName("Test Customer");
        // All other fields remain null

        long id = invoiceDao.insert(invoice);
        Invoice retrieved = invoiceDao.getInvoiceByIdSync((int) id);

        assertEquals("INV-NULLS", retrieved.getInvoiceNumber());
        assertNull(retrieved.getAddress());
        assertNull(retrieved.getPhone());
        assertNull(retrieved.getItems());
        assertNull(retrieved.getPodImagePath1());
        assertNull(retrieved.getSignatureImagePath());
    }

    @Test
    public void testDataIntegrity_emptyStrings() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("");
        invoice.setCustomerName("");
        invoice.setAddress("");
        invoice.setPhone("");
        invoice.setItems("");
        invoice.setNotes("");

        long id = invoiceDao.insert(invoice);
        Invoice retrieved = invoiceDao.getInvoiceByIdSync((int) id);

        assertEquals("", retrieved.getInvoiceNumber());
        assertEquals("", retrieved.getCustomerName());
        assertEquals("", retrieved.getAddress());
    }

    // ==================== Performance Tests ====================

    @Test
    public void testBulkInsert() {
        int count = 100;

        for (int i = 0; i < count; i++) {
            Invoice invoice = createTestInvoice("INV-" + i, "Customer " + i);
            invoiceDao.insert(invoice);
        }

        List<Invoice> all = invoiceDao.getAllInvoicesSync();
        assertEquals("Should insert all " + count + " invoices", count, all.size());
    }

    @Test
    public void testQueryPerformance() {
        // Insert 50 invoices
        for (int i = 0; i < 50; i++) {
            Invoice invoice = createTestInvoice("INV-" + i, "Customer " + i);
            invoice.setTimestamp(System.currentTimeMillis() - (i * 1000)); // Different timestamps
            invoiceDao.insert(invoice);
        }

        long startTime = System.currentTimeMillis();
        List<Invoice> all = invoiceDao.getAllInvoicesSync();
        long endTime = System.currentTimeMillis();

        assertEquals(50, all.size());
        assertTrue("Query should complete in < 1 second", (endTime - startTime) < 1000);
    }
}
