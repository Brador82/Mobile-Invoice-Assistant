# API Reference

Complete API documentation for Mobile Invoice OCR classes and methods.

## OCRProcessorMLKit

Main OCR processing class using Google ML Kit.

### Constructor

```java
public OCRProcessorMLKit(Context context)
```

**Parameters:**
- `context` - Android Context for accessing ContentResolver

**Example:**
```java
OCRProcessorMLKit processor = new OCRProcessorMLKit(this);
```

### processImage()

```java
public OCRResult processImage(Uri imageUri)
```

Process an invoice image and extract customer data.

**Parameters:**
- `imageUri` - URI of the invoice image (from gallery or camera)

**Returns:**
- `OCRResult` - Extracted data (name, address, phone, invoice number)

**Throws:**
- `IOException` - If image cannot be loaded
- `Exception` - If ML Kit processing fails

**Processing Time:** 2-3 seconds per image

**Example:**
```java
OCRProcessorMLKit processor = new OCRProcessorMLKit(context);
Uri imageUri = /* from image picker */;

OCRResult result = processor.processImage(imageUri);
Log.d(TAG, "Customer: " + result.customerName);
Log.d(TAG, "Address: " + result.address);
Log.d(TAG, "Phone: " + result.phone);
```

### close()

```java
public void close()
```

Release ML Kit TextRecognizer resources.

**Example:**
```java
processor.close();
```

**Note:** Call this when done processing to free memory.

---

## OCRResult

Data class holding extracted invoice fields.

### Fields

```java
public class OCRResult {
    public String customerName = "";
    public String address = "";
    public String phone = "";
    public String invoiceNumber = "";
    public String rawText = "";
}
```

**Field Descriptions:**
- `customerName` - Customer's full name (from "Name:" line)
- `address` - Complete delivery address
- `phone` - Primary phone number (XXX-XXX-XXXX format)
- `invoiceNumber` - Invoice ID (extracted from ID:XXXXX or generated)
- `rawText` - Complete extracted text for debugging

**Default Values:**
- `"Unknown Customer"` if name not found
- `"No address found"` if address not found
- `"No phone"` if phone not found
- `"No invoice number"` if ID not found

---

## Invoice (Entity)

Room database entity representing an invoice record.

### Fields

```java
@Entity(tableName = "invoices")
public class Invoice {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String invoiceNumber;
    private String customerName;
    private String address;
    private String phone;
    private String items;
    private String rawOcrText;
    private String originalImagePath;
    private String podPhotoPath;
    private String signaturePath;
    private String notes;
    private long timestamp;
}
```

### Methods

#### Getters
```java
public int getId()
public String getInvoiceNumber()
public String getCustomerName()
public String getAddress()
public String getPhone()
public String getItems()
public String getRawOcrText()
public String getOriginalImagePath()
public String getPodPhotoPath()
public String getSignaturePath()
public String getNotes()
public long getTimestamp()
```

#### Setters
```java
public void setId(int id)
public void setInvoiceNumber(String invoiceNumber)
public void setCustomerName(String customerName)
public void setAddress(String address)
public void setPhone(String phone)
public void setItems(String items)
public void setRawOcrText(String rawOcrText)
public void setOriginalImagePath(String originalImagePath)
public void setPodPhotoPath(String podPhotoPath)
public void setSignaturePath(String signaturePath)
public void setNotes(String notes)
public void setTimestamp(long timestamp)
```

### Example Usage

```java
Invoice invoice = new Invoice();
invoice.setInvoiceNumber("INV-000123");
invoice.setCustomerName("JOHN DOE");
invoice.setAddress("123 Main St, Springfield, MO 65810");
invoice.setPhone("417-555-1234");
invoice.setTimestamp(System.currentTimeMillis());

// Save to database
long id = database.invoiceDao().insert(invoice);
invoice.setId((int) id);
```

---

## InvoiceDao

Room DAO for database operations.

### insert()

```java
@Insert
long insert(Invoice invoice)
```

Insert new invoice into database.

**Parameters:**
- `invoice` - Invoice entity to insert

**Returns:**
- `long` - Auto-generated primary key ID

**Example:**
```java
new Thread(() -> {
    long newId = database.invoiceDao().insert(invoice);
    invoice.setId((int) newId);
}).start();
```

### update()

```java
@Update
void update(Invoice invoice)
```

Update existing invoice.

**Parameters:**
- `invoice` - Invoice entity with modified fields

**Example:**
```java
new Thread(() -> {
    invoice.setCustomerName("Updated Name");
    database.invoiceDao().update(invoice);
}).start();
```

### delete()

```java
@Delete
void delete(Invoice invoice)
```

Delete invoice from database.

**Parameters:**
- `invoice` - Invoice entity to delete

**Example:**
```java
new Thread(() -> {
    database.invoiceDao().delete(invoice);
}).start();
```

### getAllInvoicesSync()

```java
@Query("SELECT * FROM invoices ORDER BY timestamp DESC")
List<Invoice> getAllInvoicesSync()
```

Get all invoices sorted by timestamp (synchronous).

**Returns:**
- `List<Invoice>` - All invoices in database

**Note:** Must be called from background thread

**Example:**
```java
new Thread(() -> {
    List<Invoice> invoices = database.invoiceDao().getAllInvoicesSync();
    runOnUiThread(() -> {
        adapter.setInvoices(invoices);
    });
}).start();
```

### getAllInvoices()

```java
@Query("SELECT * FROM invoices ORDER BY timestamp DESC")
LiveData<List<Invoice>> getAllInvoices()
```

Get all invoices as LiveData (reactive).

**Returns:**
- `LiveData<List<Invoice>>` - Observable list of invoices

**Example:**
```java
database.invoiceDao().getAllInvoices().observe(this, invoices -> {
    adapter.setInvoices(invoices);
});
```

### getInvoiceById()

```java
@Query("SELECT * FROM invoices WHERE id = :id")
LiveData<Invoice> getInvoiceById(int id)
```

Get single invoice by ID.

**Parameters:**
- `id` - Invoice primary key

**Returns:**
- `LiveData<Invoice>` - Observable invoice

### deleteAll()

```java
@Query("DELETE FROM invoices")
void deleteAll()
```

Delete all invoices from database.

**Example:**
```java
new Thread(() -> {
    database.invoiceDao().deleteAll();
}).start();
```

---

## InvoiceDatabase

Room database singleton.

### getInstance()

```java
public static synchronized InvoiceDatabase getInstance(Context context)
```

Get database singleton instance.

**Parameters:**
- `context` - Application context

**Returns:**
- `InvoiceDatabase` - Singleton instance

**Example:**
```java
InvoiceDatabase database = InvoiceDatabase.getInstance(this);
```

### invoiceDao()

```java
public abstract InvoiceDao invoiceDao()
```

Get DAO for invoice operations.

**Returns:**
- `InvoiceDao` - Data access object

**Example:**
```java
InvoiceDao dao = database.invoiceDao();
List<Invoice> invoices = dao.getAllInvoicesSync();
```

---

## Extraction Patterns

### Phone Number Pattern

```java
Pattern PHONE_PATTERN = Pattern.compile(
    "\\(?\\d{3}\\)?[-\\s.]?\\d{3}[-\\s.]?\\d{4}"
);
```

**Matches:**
- `(417) 555-1234`
- `417-555-1234`
- `417.555.1234`
- `4175551234`

### Invoice Number Pattern

```java
Pattern INVOICE_PATTERN = Pattern.compile(
    "(?:INV|INVOICE|#)[-\\s:]*([A-Z0-9-]+)",
    Pattern.CASE_INSENSITIVE
);
```

**Matches:**
- `INV-000123`
- `INVOICE: ABC123`
- `#12345`
- `Invoice #67890`

### ZIP Code Pattern

```java
Pattern ZIP_PATTERN = Pattern.compile(
    "\\b\\d{5}(?:-\\d{4})?\\b"
);
```

**Matches:**
- `65810`
- `65810-1234`

---

## Customization Examples

### Custom Invoice Format

To support different invoice layouts, modify `extractInvoiceData()`:

```java
private OCRResult extractInvoiceData(Text text) {
    // Change marker text
    int customerIndex = findLineContaining(allLines, "CUSTOMER INFO:");
    
    // Adjust line offsets
    String nameLine = allLines.get(customerIndex + 2);  // Different position
    String addressLine = allLines.get(customerIndex + 3);
    
    // Custom parsing logic
    if (nameLine.startsWith("Name:")) {
        result.customerName = nameLine.substring(5).trim();
    }
    
    return result;
}
```

### Additional Field Extraction

Extract email addresses:

```java
Pattern EMAIL_PATTERN = Pattern.compile(
    "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
);

Matcher emailMatcher = EMAIL_PATTERN.matcher(fullText);
if (emailMatcher.find()) {
    result.email = emailMatcher.group();
}
```

### Confidence Threshold

Filter low-confidence extractions:

```java
// In extractInvoiceData()
if (result.customerName.length() < 3) {
    result.customerName = "Unknown Customer";  // Likely misread
}

if (!PHONE_PATTERN.matcher(result.phone).matches()) {
    result.phone = "Invalid phone";  // Regex validation failed
}
```

---

## Error Codes

### OCR Errors

| Code | Message | Cause | Solution |
|------|---------|-------|----------|
| E001 | "Error: Could not load image" | Invalid URI or file not found | Check file permissions |
| E002 | "Error: Recognition timed out" | ML Kit took >10s | Reduce image size |
| E003 | "Error: Recognition failed" | ML Kit internal error | Retry or check logs |
| E004 | "'BILL TO:' section not found" | Invoice format mismatch | Adjust extraction logic |

### Database Errors

| Code | Message | Cause | Solution |
|------|---------|-------|----------|
| D001 | "Duplicate invoice" | Primary key conflict | Check invoice_number uniqueness |
| D002 | "Database locked" | Concurrent access | Use transactions |
| D003 | "Disk full" | Storage exhausted | Clear old data |

---

## Performance Metrics

### OCR Processing

| Metric | Value | Notes |
|--------|-------|-------|
| Single image | 2-3 sec | Pixel 9a, 1024x1024 image |
| Batch (10 images) | 25-30 sec | Sequential processing |
| ML Kit model size | ~10 MB | One-time download |
| Memory usage | 50 MB peak | During recognition |

### Database Operations

| Operation | Time | Notes |
|-----------|------|-------|
| Insert | <10 ms | Single row |
| Query all | 50-100 ms | 100 rows |
| Update | <10 ms | Single row |
| Delete | <10 ms | Single row |

---

## Thread Safety

### Safe Operations
- `InvoiceDatabase.getInstance()` - Synchronized singleton
- All DAO methods - Thread-safe by Room
- `OCRProcessorMLKit.processImage()` - Synchronous, call from background thread

### Unsafe Operations
- ViewBinding updates - Must be on UI thread
- RecyclerView adapter - Must notify on UI thread

### Best Practice

```java
// Worker thread
new Thread(() -> {
    // Database + OCR operations
    OCRResult result = processor.processImage(uri);
    long id = database.invoiceDao().insert(invoice);
    
    // Switch to UI thread
    runOnUiThread(() -> {
        // UI updates
        binding.textView.setText(result.customerName);
        adapter.notifyDataSetChanged();
    });
}).start();
```

---

## Changelog

### v1.0 (Current)
- Initial release with ML Kit integration
- Room database persistence
- Camera capture support
- Basic field extraction

### Planned v1.1
- Export to CSV/Excel
- Batch delete
- Search/filter invoices

### Planned v2.0
- Cloud sync
- Custom templates
- Multi-language support

---

## Support

- **Documentation**: https://github.com/Brador82/Mobile_Invoice_OCR/wiki
- **Issues**: https://github.com/Brador82/Mobile_Invoice_OCR/issues
- **API Questions**: Create issue with `api` label
