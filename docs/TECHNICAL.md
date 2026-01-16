# Technical Architecture

Comprehensive technical documentation for Mobile Invoice OCR.

## System Architecture

### High-Level Overview

```
┌─────────────────────────────────────────────────────┐
│                  Presentation Layer                 │
├─────────────────────────────────────────────────────┤
│  MainActivity  │  InvoiceDetailActivity  │  Camera  │
└────────┬────────────────┬────────────────┬──────────┘
         │                │                │
         ▼                ▼                ▼
┌─────────────────────────────────────────────────────┐
│                   Business Logic                    │
├─────────────────────────────────────────────────────┤
│  OCRProcessorMLKit  │  InvoiceAdapter  │  Helpers   │
└────────┬────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────┐
│                   Data Layer                        │
├─────────────────────────────────────────────────────┤
│  Room Database  │  Invoice Entity  │  DAO  │  Repo  │
└─────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────┐
│               External Dependencies                 │
├─────────────────────────────────────────────────────┤
│  ML Kit  │  CameraX  │  Apache POI  │  Material UI  │
└─────────────────────────────────────────────────────┘
```

## Core Components

### 1. OCRProcessorMLKit

**Purpose**: On-device text recognition and field extraction engine

**Location**: `app/src/main/java/com/mobileinvoice/ocr/OCRProcessorMLKit.java`

**Key Responsibilities**:
- Load image from URI via ContentResolver
- Convert to ML Kit InputImage format
- Perform text recognition using TextRecognizer
- Parse extracted text to identify fields
- Return structured OCRResult

**Algorithm Flow**:

```java
processImage(Uri imageUri) → OCRResult
    ↓
1. Load Bitmap from URI
    ↓
2. Create InputImage from Bitmap
    ↓
3. recognizer.process(image) [Async → Sync wrapper]
    ↓
4. extractInvoiceData(Text result)
    ↓
5. Find "BILL TO:" section marker
    ↓
6. Extract next 3 lines:
   - Line 1: Customer Name + (ID:XXXXX)
   - Line 2: Address
   - Line 3: Phone: XXX-XXX-XXXX
    ↓
7. Apply regex patterns for validation
    ↓
8. Return OCRResult{name, address, phone, invoiceNumber}
```

**Extraction Logic**:

```java
private OCRResult extractInvoiceData(Text text) {
    // Step 1: Collect all lines from ML Kit text blocks
    List<String> allLines = new ArrayList<>();
    for (Text.TextBlock block : text.getTextBlocks()) {
        for (Text.Line line : block.getLines()) {
            allLines.add(line.getText().trim());
        }
    }
    
    // Step 2: Find "BILL TO:" marker
    int billToIndex = findLineContaining(allLines, "BILL TO");
    
    // Step 3: Extract structured fields
    String nameLine = allLines.get(billToIndex + 1);
    String addressLine = allLines.get(billToIndex + 2);
    String phoneLine = allLines.get(billToIndex + 3);
    
    // Step 4: Parse name and extract ID
    Pattern idPattern = Pattern.compile("\\(ID:([^)]+)\\)");
    Matcher idMatcher = idPattern.matcher(nameLine);
    if (idMatcher.find()) {
        result.invoiceNumber = idMatcher.group(1);
        nameLine = nameLine.substring(0, idMatcher.start()).trim();
    }
    
    // Step 5: Extract phone with regex
    Pattern phonePattern = Pattern.compile("\\(?\\d{3}\\)?[-\\s.]?\\d{3}[-\\s.]?\\d{4}");
    Matcher phoneMatcher = phonePattern.matcher(phoneLine);
    if (phoneMatcher.find()) {
        result.phone = phoneMatcher.group();
    }
    
    return result;
}
```

**Performance**:
- Average processing time: 2-3 seconds per image
- ML Kit model size: ~10MB (downloads once)
- Memory usage: ~50MB during processing
- Runs on background thread (non-blocking UI)

### 2. Room Database

**Schema**:

```sql
CREATE TABLE invoices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_number TEXT NOT NULL,
    customer_name TEXT NOT NULL,
    address TEXT NOT NULL,
    phone TEXT,
    items TEXT,
    raw_ocr_text TEXT,
    original_image_path TEXT,
    pod_photo_path TEXT,
    signature_path TEXT,
    notes TEXT,
    timestamp INTEGER NOT NULL
);
```

**Entity Definition**:

```java
@Entity(tableName = "invoices")
public class Invoice {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "invoice_number")
    private String invoiceNumber;
    
    @ColumnInfo(name = "customer_name")
    private String customerName;
    
    @ColumnInfo(name = "address")
    private String address;
    
    @ColumnInfo(name = "phone")
    private String phone;
    
    @ColumnInfo(name = "items")
    private String items;
    
    @ColumnInfo(name = "raw_ocr_text")
    private String rawOcrText;
    
    @ColumnInfo(name = "original_image_path")
    private String originalImagePath;
    
    @ColumnInfo(name = "timestamp")
    private long timestamp;
    
    // Getters and setters...
}
```

**DAO Operations**:

```java
@Dao
public interface InvoiceDao {
    @Insert
    long insert(Invoice invoice);  // Returns auto-generated ID
    
    @Update
    void update(Invoice invoice);
    
    @Delete
    void delete(Invoice invoice);
    
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    List<Invoice> getAllInvoicesSync();
    
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    LiveData<List<Invoice>> getAllInvoices();  // For reactive updates
}
```

**Database Access Pattern**:

```java
// Writing (background thread)
new Thread(() -> {
    InvoiceDatabase db = InvoiceDatabase.getInstance(context);
    long newId = db.invoiceDao().insert(invoice);
    runOnUiThread(() -> {
        // Update UI with new invoice
    });
}).start();

// Reading (background thread)
new Thread(() -> {
    List<Invoice> invoices = db.invoiceDao().getAllInvoicesSync();
    runOnUiThread(() -> {
        adapter.setInvoices(invoices);
    });
}).start();
```

### 3. MainActivity

**Lifecycle**:

```java
onCreate()
    ↓
- Initialize database
- Setup ViewBinding
- Register ActivityResultLaunchers (image picker, camera)
- Setup RecyclerView adapters
- Load invoices from database
    ↓
onResume()
    ↓
- Reload invoices (refresh after detail view)
    ↓
onPause()
    ↓
(Data already persisted in database)
```

**Image Selection Flow**:

```java
User taps UPLOAD
    ↓
pickImagesLauncher.launch("image/*")
    ↓
ActivityResultCallback receives List<Uri>
    ↓
selectedImages.addAll(uris)
    ↓
Enable "PROCESS OCR" button
```

**OCR Processing Flow**:

```java
User taps "PROCESS ALL WITH OCR"
    ↓
Start background thread
    ↓
For each Uri in selectedImages:
    1. processImage(uri) → OCRResult
    2. Create Invoice entity
    3. Set extracted fields
    4. database.invoiceDao().insert(invoice)
    5. Add to invoices list
    6. Update progress UI
    ↓
Update RecyclerView
Display "Processing complete!"
```

### 4. ML Kit Integration

**Initialization**:

```java
// In OCRProcessorMLKit constructor
TextRecognizer recognizer = TextRecognition.getClient(
    TextRecognizerOptions.DEFAULT_OPTIONS
);
```

**Text Recognition (Async to Sync)**:

```java
// ML Kit uses async callbacks - wrap in synchronous call
public OCRResult processImage(Uri imageUri) {
    // Create synchronization primitives
    final Object lock = new Object();
    final boolean[] done = {false};
    final Text[] textResult = {null};
    
    // Start async recognition
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
                done[0] = true;
                lock.notify();
            }
        });
    
    // Wait for completion
    synchronized (lock) {
        while (!done[0]) {
            lock.wait(10000);  // 10 second timeout
        }
    }
    
    return extractInvoiceData(textResult[0]);
}
```

**Text Block Structure**:

```
Text
 └── TextBlock[]
      └── Line[]
           └── Element[]
                └── String text
```

Example extracted structure:
```
TextBlock[0]: "APPLIANCES 4 LESS ®"
TextBlock[1]: "Address: 1517 W Battlefield Rd, Springfield, MO 65807"
TextBlock[2]: "Phone: (417)-860-5528"
TextBlock[3]: "BILL TO:"
TextBlock[4]: "Name: DAVID MCKEOWN (ID:CA2I63AMJ3E / Salesperson: KY )"
TextBlock[5]: "Address: 4461 S Roanoke Ave, Springfield, MO 65810"
TextBlock[6]: "Phone: 417-818-1235 Email: dandgdist@gmail.com"
```

## Data Flow

### Complete Invoice Processing Flow

```
User Action → Image Selection
    ↓
MainActivity.pickImagesLauncher
    ↓
selectedImages: List<Uri>
    ↓
User Action → Process OCR
    ↓
Background Thread Created
    ↓
For each Uri:
    │
    ├→ ContentResolver.openInputStream(uri)
    │   ↓
    ├→ MediaStore.Images.Media.getBitmap()
    │   ↓
    ├→ InputImage.fromBitmap(bitmap, 0)
    │   ↓
    ├→ recognizer.process(image)
    │   ↓
    ├→ ML Kit Text Recognition
    │   ↓
    ├→ extractInvoiceData(Text)
    │   ↓
    ├→ OCRResult {name, address, phone, invoiceNumber}
    │   ↓
    ├→ Create Invoice entity
    │   ↓
    ├→ database.invoiceDao().insert(invoice)
    │   ↓
    └→ Add to invoices list
    ↓
runOnUiThread()
    ↓
adapter.setInvoices(invoices)
    ↓
RecyclerView displays invoices
```

## Threading Model

### UI Thread (Main Thread)
- All ViewBinding updates
- RecyclerView adapter notifications
- Button click handlers
- Progress bar updates

### Background Thread (Worker)
- Database operations (insert/update/delete/query)
- Image loading from ContentResolver
- ML Kit text recognition (internally uses thread pool)
- File I/O operations

### Example Pattern:

```java
// Worker thread for heavy operations
new Thread(() -> {
    // Database/ML Kit operations here
    OCRResult result = ocrProcessor.processImage(uri);
    long id = database.invoiceDao().insert(invoice);
    
    // Switch to UI thread for updates
    runOnUiThread(() -> {
        binding.progressBar.setProgress(index);
        adapter.notifyDataSetChanged();
    });
}).start();
```

## Dependency Injection

### Manual DI (Current Implementation)

```java
// Database singleton
InvoiceDatabase database = InvoiceDatabase.getInstance(this);

// OCR processor instance
OCRProcessorMLKit ocrProcessor = new OCRProcessorMLKit(this);

// Adapter with callback
InvoiceAdapter adapter = new InvoiceAdapter(this /* OnInvoiceClickListener */);
```

### Future: Hilt/Dagger Integration

```java
@HiltAndroidApp
public class InvoiceApp extends Application { }

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject InvoiceDatabase database;
    @Inject OCRProcessor ocrProcessor;
}
```

## Error Handling

### ML Kit Recognition Failures

```java
try {
    OCRResult result = ocrProcessor.processImage(uri);
    if (result.rawText.startsWith("Error:")) {
        // Fallback or user notification
        result.customerName = "OCR Failed";
    }
} catch (IOException e) {
    Log.e(TAG, "Failed to load image", e);
    result.rawText = "Error: " + e.getMessage();
}
```

### Database Operation Failures

```java
try {
    long id = database.invoiceDao().insert(invoice);
} catch (SQLiteConstraintException e) {
    // Duplicate invoice number
    Log.e(TAG, "Duplicate invoice", e);
} catch (SQLiteException e) {
    // General database error
    Log.e(TAG, "Database error", e);
}
```

## Performance Optimizations

### 1. Image Loading
- Use `BitmapFactory.Options` to downsample large images
- ML Kit works best with 1024x1024 max resolution
- Reduces memory footprint by 4x-16x

```java
BitmapFactory.Options options = new BitmapFactory.Options();
options.inSampleSize = calculateInSampleSize(options, 1024, 1024);
options.inJustDecodeBounds = false;
Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
```

### 2. Database Queries
- Index on `timestamp` column for fast sorting
- Use `LiveData` for reactive updates (eliminates polling)
- Batch inserts with transactions

```java
@Query("SELECT * FROM invoices ORDER BY timestamp DESC")
@Index(value = ["timestamp"])
LiveData<List<Invoice>> getAllInvoices();
```

### 3. RecyclerView
- ViewHolder pattern (built into RecyclerView)
- DiffUtil for efficient updates
- Image loading with Glide/Coil (future)

### 4. ML Kit Model Management
- Model auto-downloads on first use (~10MB)
- Cached in app's internal storage
- No model loaded in memory until first use

## Security Considerations

### 1. Data Privacy
- All OCR processing happens on-device
- No data sent to external servers
- Invoice images stored in app-private directory
- Database encrypted with SQLCipher (future enhancement)

### 2. Permissions
```xml
<!-- Required -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- Optional for better features -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
    android:maxSdkVersion="29" />
```

### 3. Content Provider Security
- FileProvider for sharing images securely
- No world-readable files
- Scoped storage compliance (Android 10+)

## Testing Strategy

### Unit Tests
- `OCRProcessorMLKit.extractInvoiceData()` with mock Text objects
- Database DAO operations
- Invoice entity field validation

### Instrumented Tests
- End-to-end OCR flow with test images
- Database CRUD operations
- UI interaction tests with Espresso

### Manual Test Cases
1. Single invoice extraction
2. Batch processing (10+ images)
3. Camera capture → OCR
4. Edit extracted data
5. Delete invoice
6. App restart (data persistence)
7. Low memory scenario

## Build Configuration

### Gradle Dependencies

```gradle
dependencies {
    // ML Kit
    implementation 'com.google.mlkit:text-recognition:16.0.0'
    
    // Room Database
    implementation "androidx.room:room-runtime:2.6.0"
    annotationProcessor "androidx.room:room-compiler:2.6.0"
    
    // Camera
    implementation 'androidx.camera:camera-core:1.3.0'
    implementation 'androidx.camera:camera-camera2:1.3.0'
    implementation 'androidx.camera:camera-lifecycle:1.3.0'
    implementation 'androidx.camera:camera-view:1.3.0'
    
    // Apache POI (Excel export)
    implementation 'org.apache.poi:poi:5.2.3'
    implementation 'org.apache.poi:poi-ooxml:5.2.3'
    
    // Material Design
    implementation 'com.google.android.material:material:1.10.0'
}
```

### ProGuard Rules

```proguard
# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
```

## Logging & Debugging

### Debug Logging

```java
private static final String TAG = "OCRProcessorMLKit";

Log.d(TAG, "Processing image: " + uri);
Log.d(TAG, "Extracted " + lines.size() + " lines");
Log.d(TAG, "Found 'BILL TO:' at line " + billToIndex);
Log.d(TAG, "========= ML KIT EXTRACTION RESULTS =========");
Log.d(TAG, "Customer: " + result.customerName);
Log.d(TAG, "Address: " + result.address);
Log.d(TAG, "Phone: " + result.phone);
```

### ADB Logcat Filtering

```bash
# Filter by tag
adb logcat -s OCRProcessorMLKit:D

# Filter by package
adb logcat | grep "com.mobileinvoice.ocr"

# Clear log
adb logcat -c
```

## Deployment

### Debug Build
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
```bash
./gradlew assembleRelease
# Or for Play Store:
./gradlew bundleRelease
```

## Future Enhancements

### Planned Features
1. **Cloud Sync** - Firebase Firestore backup
2. **Batch Export** - Multi-format export (CSV, Excel, PDF)
3. **Custom Templates** - Support for different invoice formats
4. **Barcode Scanning** - QR code invoice lookup
5. **Voice Input** - Dictate notes/corrections
6. **Analytics** - Track extraction accuracy
7. **Multi-language** - Spanish, French invoice support

### Technical Debt
1. Replace manual threading with Kotlin Coroutines
2. Add comprehensive unit test coverage
3. Implement Hilt for dependency injection
4. Migrate to Jetpack Compose for UI
5. Add offline-first sync with WorkManager
6. Implement proper MVVM architecture with ViewModels

## API Reference

See [API.md](API.md) for detailed API documentation.

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for development guidelines.
