# Claude Code Implementation Brief - Mobile Invoice OCR

## Context & Repository Status

**Repository**: Mobile_Invoice_OCR (Windows)  
**Branch**: OCR-adjust/develope  
**Platform**: Android (API 26-34)  
**Primary Language**: Java  
**Build System**: Gradle 8.5  

## What This App Does (Product Intent)

This is a **production-ready Android app** for delivery drivers and logistics teams to:

1. **Capture invoice photos** (gallery upload or camera)
2. **Run on-device OCR** to extract customer/delivery fields from the "BILL TO:" section
3. **Persist records locally** (Room/SQLite) - survives app restarts and device reboots
4. **Edit invoice details** - fields, delivered items, POD photos, signatures, notes
5. **Export data** - CSV, Excel-friendly TSV, JSON with Android share sheet

### Expected OCR Extraction Behavior

The app looks for a **"BILL TO:"** marker in invoices, then extracts:
- **Customer name** (line after "BILL TO:")
- **Address** (2nd line after "BILL TO:")
- **Phone** (3rd line with regex pattern matching)
- **Invoice number** (from top-right header near "INVOICE" text, or fallback to `(ID:XXXXX)` pattern)
- **Items** (optional: from item table or keyword detection)

---

## Source of Truth (Avoid Stale Docs)

✅ **Use these as canonical truth:**
- `WHAT_IS_WHAT.md` - Identifies what's current vs legacy
- `GOLDEN_PATH.md` - Build path and canonical approach
- `STATUS.md` - Current implementation status
- `README.md` - Project overview

❌ **Treat as legacy/outdated:**
- Anything describing Tesseract.js web UI as primary
- Flask/Python server as main OCR path (optional fallback only)
- Old workspace snapshots in `archive/` folder

---

## Current Implementation Status

### ✅ Working Features

| Feature | Status | Implementation |
|---------|--------|----------------|
| **Image Upload** | ✅ Working | Multiple file picker, camera integration |
| **On-Device OCR** | ✅ Working | Google ML Kit Text Recognition (95%+ accuracy) |
| **Data Persistence** | ✅ Working | Room database with full CRUD operations |
| **Invoice Management** | ✅ Working | List view, detail editing, delete |
| **Items Selection** | ✅ Working | Multi-select dialog (10 appliance types) |
| **POD Photo Capture** | ✅ Working | Camera integration with persistent storage |
| **Signature Capture** | ✅ Working | Canvas-based signature pad |
| **Export** | ✅ Working | CSV, Excel (TSV), JSON with share dialog |
| **Field Validation** | ✅ Working | Required fields, error handling |

### Current Workflow Confirmation

```
Upload/Camera → Process OCR → Save to DB → Display in List
  ↓                                              ↓
  ↓                                        Tap Invoice
  ↓                                              ↓
  ↓                                    Open Detail Screen
  ↓                                              ↓
  ↓                                    Edit, Add POD/Signature
  ↓                                              ↓
  ↓                                         Save Changes
  ↓                                              ↓
Export CSV/Excel/JSON ← Load All Invoices from DB
```

---

## Code Map (Where to Work)

### Core Android Sources

All paths relative to: `android/app/src/main/java/com/mobileinvoice/ocr/`

#### Primary Activity Files

**MainActivity.java** (309 lines)
- Image upload (gallery picker)
- Camera launch
- OCR processing orchestration
- Invoice list display (RecyclerView)
- Export buttons (CSV, Excel/JSON)
- Database operations (load, save)

**InvoiceDetailActivity.java** (300 lines)
- Load invoice from database by ID
- Edit all fields (invoice #, customer, address, phone, notes)
- Items multi-select dialog (10 appliance types)
- POD photo capture + display
- Signature capture + display
- Field validation (required fields)
- Save changes to database

**CameraActivity.java**
- Camera preview and capture
- Used for invoice photos and POD photos

**SignatureActivity.java** + **SignatureView.java**
- Canvas-based signature drawing
- Touch/mouse/stylus support
- Save signature to file

#### OCR & Export

**OCRProcessorMLKit.java** (614 lines) - **CRITICAL FILE**
- On-device text recognition (Google ML Kit)
- Field extraction logic:
  - `extractInvoiceData()` - Main extraction method
  - `extractInvoiceNumberNearInvoiceHeader()` - Top-right invoice # detection
  - `extractDeliveredItems()` - Item/appliance detection
  - Pattern matching for phone, address, zip code
- Extraction assumptions:
  - Finds "BILL TO:" marker
  - Extracts 3 lines after: name, address, phone
  - Looks for invoice # near "INVOICE" header in top-right quadrant
  - Falls back to `(ID:XXXXX)` pattern in name line

**ExportHelper.java** (263 lines)
- CSV export with proper escaping
- Excel (TSV) export
- JSON export with metadata
- Android FileProvider sharing
- Export location: `Android/data/com.mobileinvoice.ocr/files/exports/`

#### Database Layer

Path: `android/app/src/main/java/com/mobileinvoice/ocr/database/`

**Invoice.java** - Entity with 11 fields:
```java
int id (PK, autoincrement)
String invoiceNumber
String customerName
String address
String phone
String items (comma-separated)
String podImagePath
String signatureImagePath
String notes
String originalImagePath
String rawOcrText
long timestamp
```

**InvoiceDao.java** - CRUD operations:
```java
@Insert long insert(Invoice invoice)
@Update void update(Invoice invoice)
@Delete void delete(Invoice invoice)
@Query List<Invoice> getAllInvoicesSync()
```

**InvoiceDatabase.java** - Room database singleton

**Converters.java** - Type converters

---

## Build Instructions

### From Command Line (Windows)

```bash
cd android
gradlew.bat assembleDebug

# Output: android/app/build/outputs/apk/debug/app-debug.apk

# Install to device
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Quick Build Script

```bash
cd android
.\build-and-install.bat
```

### Verify Build Success

Check for:
- No compilation errors
- APK exists at expected path
- App installs and launches on device/emulator

---

## Common Implementation Tasks

### Task 1: Improve OCR Accuracy

**Focus**: `OCRProcessorMLKit.java`

**Changes**:
- Add alternate labels: "SHIP TO", "CUSTOMER:", "DELIVER TO"
- Improve phone regex for international formats
- Better address parsing (multi-line support)
- Safer bounds checking (prevent IndexOutOfBounds)
- Enhanced invoice number detection (more patterns)
- Item extraction improvements

**Test**:
- Upload invoices with non-standard layouts
- Verify extraction accuracy
- Check edge cases (missing fields, unusual formats)

### Task 2: Polish UX

**Focus**: `MainActivity.java`, `InvoiceDetailActivity.java`, layouts

**Changes**:
- Add loading indicators during OCR processing
- Improve error messages (specific, actionable)
- Better empty states ("No invoices yet" with helpful text)
- Progress bar for batch OCR
- Confirmation dialogs for destructive actions
- Long-press bulk selection (optional)

**Test**:
- Process multiple invoices
- Delete invoices (confirm dialog appears)
- Handle network/permission errors gracefully

### Task 3: Export Fixes

**Focus**: `ExportHelper.java`, `MainActivity.java` export buttons

**Changes**:
- Verify CSV escaping (quotes, commas, newlines)
- Ensure stable column ordering
- Fix MIME types for share intent
- Add export summary toast
- Handle empty invoice list gracefully
- Test with special characters in data

**Test**:
- Export CSV, open in Excel (verify columns align)
- Export JSON, validate structure
- Share via email/Drive
- Export with special characters (quotes, commas)

### Task 4: Field Extraction Enhancements

**Focus**: `OCRProcessorMLKit.java` extraction methods

**Changes**:
- Multi-line address support
- Business name vs person name detection
- Better zip code extraction + validation
- Email extraction (optional)
- Total amount extraction (optional)
- Date extraction (optional)

**Test**:
- Various invoice formats
- Business addresses
- International addresses
- Edge cases (PO boxes, suite numbers)

### Task 5: Database Enhancements

**Focus**: `database/` folder, `MainActivity.java`

**Changes**:
- Add search/filter functionality
- Sort options (date, customer name, invoice #)
- Bulk operations (mark as delivered, export selected)
- Database migration (if schema changes)
- Data validation before save

**Test**:
- Search by customer name
- Sort by different fields
- Bulk delete/export
- Database survives app restart

---

## Acceptance Criteria (Definition of Done)

A task is **complete** when:

### ✅ Build & Deploy
- [ ] App builds with `gradlew.bat assembleDebug` (no errors)
- [ ] APK installs on device/emulator
- [ ] App launches without crashes

### ✅ Core Workflow
- [ ] Upload/Camera → Process OCR → Record appears in list
- [ ] Tap invoice → Open detail screen
- [ ] Edit fields → Save → Reopen → Changes persist
- [ ] Add POD photo → Save → Reopen → Photo displays
- [ ] Add signature → Save → Reopen → Signature displays

### ✅ Export
- [ ] Export CSV → File created in correct location
- [ ] Open CSV in Excel → Data displays correctly
- [ ] Export JSON → Valid JSON structure
- [ ] Share dialog works (email, Drive, etc.)

### ✅ Data Persistence
- [ ] All invoice data survives app restart
- [ ] Images stored permanently in app storage
- [ ] Database operations are atomic (no partial saves)
- [ ] Delete invoice removes all related data

### ✅ Error Handling
- [ ] Missing fields show clear error messages
- [ ] Camera permission denied handled gracefully
- [ ] Empty export list shows helpful message
- [ ] OCR failures don't crash app

---

## Non-Functional Constraints

### Must Maintain
- ✅ Room database persistence (no regressions)
- ✅ FileProvider image storage (don't break file paths)
- ✅ ML Kit as primary OCR (not HTTP/Termux backend)
- ✅ Android API 26+ compatibility

### Code Style
- Keep changes surgical (minimal refactors)
- Preserve existing architecture patterns
- Add logging for debugging (Log.d/Log.e)
- Comments for complex extraction logic

### Testing
- Test on real device if possible (OCR accuracy varies)
- Verify different invoice layouts
- Check edge cases (missing fields, unusual formats)
- Performance: OCR should complete in <5 seconds per image

---

## Important Notes

### Legacy Code (Don't Use)
- ❌ `OCRProcessorHTTP.java` - Optional, not primary path
- ❌ `archive/` folder - Old web UI experiments
- ❌ Tesseract.js references - Superseded by ML Kit

### Optional Features (Not Current Priority)
- Cloud sync (Google Drive, Dropbox)
- PDF export with images
- Barcode scanning
- Statistics dashboard
- Multi-language support
- Dark mode theme

### Known Limitations
- OCR accuracy depends on image quality (95%+ typical)
- Handwritten notes may extract poorly
- Non-standard invoice layouts need manual correction
- ML Kit model downloads once (~10MB)

---

## Quick Reference Commands

```bash
# Build APK
cd android && gradlew.bat assembleDebug

# Install to device
adb install app\build\outputs\apk\debug\app-debug.apk

# View logs
adb logcat -s OCRProcessorMLKit:* MainActivity:* InvoiceDetailActivity:*

# Clear app data (for testing)
adb shell pm clear com.mobileinvoice.ocr

# Uninstall app
adb uninstall com.mobileinvoice.ocr
```

---

## Next Steps (For Claude)

1. **Read and confirm understanding** of this brief
2. **Ask clarifying questions** if anything is unclear
3. **Request specific task** from the user (OCR accuracy, UX polish, export fixes, etc.)
4. **Implement changes** surgically in relevant files
5. **Test changes** match acceptance criteria
6. **Report completion** with summary of what changed

---

**Last Updated**: January 12, 2026  
**Status**: Ready for implementation tasks  
**Primary Contact**: See repository owner

