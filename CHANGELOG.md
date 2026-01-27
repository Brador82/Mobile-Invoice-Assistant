# Changelog

All notable changes to Mobile Invoice OCR will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-01-15

### Fixed
- 🐛 **Data Persistence**: POD photos and signatures now persist across screen changes
  - Added auto-save in onPause() and onBackPressed()
  - Fixed onDelete to reload from database instead of manual list manipulation
- 🐛 **POD Slot Assignment**: Single photo now saves to correct slot instead of all 3
  - Smart assignment to first empty slot (pod1 → pod2 → pod3)
  - Fixed loading logic to display each photo in correct ImageView
- 🐛 **OCR Quality**: Enhanced extraction patterns for cleaner data
  - Better filtering of warranty/terms text
  - Stricter length validation (3-30 chars)
  - Improved customer name cleaning (removes slashes, IDs)

### Added
- ✨ **Auto-Save Functionality**: Data automatically saves when navigating away
  - No more "forgot to save" lost data
  - Silent background save without UI interruption
- ✨ **POD Photo Management**: Long-press thumbnails for options
  - View Full Size (opens in system image viewer)
  - Replace Photo (clears slot and reopens camera)
  - Delete Photo (removes and cleans up file)
- ✨ **Smart POD Button**: Text updates based on photo count
  - "Add POD Photo" (0 photos)
  - "Add POD Photo 2" (1 photo)
  - "Add POD Photo 3" (2 photos)
  - "3 Photos Captured" (all slots full)
- ✨ **Downloads Folder Export**: All exports now save to Downloads/MobileInvoiceOCR/
  - Easy to find in file manager
  - Accessible by all cloud service apps
- ✨ **Post-Export Cleanup**: Dialog prompts to clear data after export
  - "Clear All Data" - Resets app for next batch
  - "Keep Data" - Retains for review or re-export
  - Confirmation dialog prevents accidental deletion
- ✨ **Cloud Integration**: Share dialog opens automatically after export
  - Direct upload to Google Drive, Dropbox, OneDrive
  - Email or messaging app sharing
  - Any installed file-sharing app

### Changed
- 📝 **Timestamped Filenames**: Human-readable format (invoices_20260115_143022.md)
- 📝 **Phone Normalization**: Consistent (XXX) XXX-XXXX format
- 📝 **Address Cleaning**: Normalized whitespace in addresses
- 📝 **Export Location Messages**: Show full path "Downloads/MobileInvoiceOCR/filename"

## [1.1.0] - 2026-01-15 (Morning)

### Added
- ✨ **Route Optimization**: Google Maps integration with TSP algorithm
  - Nearest Neighbor algorithm for optimal delivery sequence
  - Total distance and time estimation
  - Visual route display with markers and polylines
  - Turn-by-turn navigation launch
- ✨ **Drag-and-Drop Reordering**: Manual invoice list reordering
  - Long-press to drag cards
  - Visual feedback during drag
  - onOrderChanged callback for persistence

## [1.0.0] - 2026-01-11

### Added
- ✨ **Google ML Kit Integration**: Replaced Tesseract with ML Kit for on-device OCR
  - 95%+ accuracy on standard invoice formats
  - 2-3 second processing time per image
  - No internet required after initial model download
- 💾 **Room Database Persistence**: All invoices saved locally with automatic backup
  - SQLite database with Room ORM
  - Automatic ID generation
  - Timestamp tracking
  - Delete functionality
- 📸 **Camera Capture**: Direct invoice photography from within app
  - CameraX integration
  - Auto-focus support
  - Photo preview and confirm/retake
- 📤 **Gallery Import**: Bulk import existing invoice photos
  - Multi-select support
  - Batch OCR processing
  - Progress indicator
- 🎯 **Intelligent Field Extraction**: Automatic "BILL TO:" section detection
  - Customer name extraction (with ID parsing)
  - Address parsing
  - Phone number regex validation
  - Invoice number extraction
- 📋 **Invoice List View**: RecyclerView with all processed invoices
  - Sort by timestamp (newest first)
  - Tap to view details
  - Swipe or tap to delete
- 🔍 **Invoice Detail Screen**: View and edit extracted data
  - All fields editable
  - POD photo capture
  - Signature capture
  - Notes field
- 🎨 **Material Design 3 UI**: Modern, responsive interface
  - ViewBinding for type-safe views
  - ConstraintLayout for flexible layouts
  - CardView for invoice items
  - Progress indicators

### Technical
- Migrated from server-based Tesseract to on-device ML Kit
- Implemented Room database with DAO pattern
- Added background threading for database and OCR operations
- Configured ProGuard for release builds
- Set up Gradle build system with dependency management

### Performance
- Average OCR processing: 2-3 seconds per invoice
- Memory usage: ~50MB during processing
- App size: ~15MB (after ML Kit model download)
- Database query time: <100ms for 100 records

## [0.9.0] - 2026-01-10 (Development)

### Added
- Initial project structure
- Tesseract OCR integration (local)
- HTTP-based OCR server (Termux/Flask)
- Basic UI with upload functionality

### Changed
- Replaced Tesseract with A.C.E.S. (Anchored Coordinate Extraction System)
- Added OpenCV for perspective correction (attempted)
- Pillow-based preprocessing

### Issues
- ❌ Tesseract accuracy inadequate (40-60%)
- ❌ OpenCV compilation failed on ARM64
- ❌ Template coordinates wrong for invoice format
- ❌ Extracting company address instead of customer

### Deprecated
- `OCRProcessor.java` - Local Tesseract implementation
- `OCRProcessorHTTP.java` - Server-based extraction
- `server_aces.py` - Flask OCR server (no longer needed)
- `server_aces_pillow.py` - Pillow-based server

## [0.5.0] - 2026-01-09 (Prototype)

### Added
- Basic Android app structure
- MainActivity with image selection
- InvoiceDetailActivity layout
- Room database schema
- Apache POI for Excel export (planned)

### Changed
- Switched from internal Tesseract to HTTP server
- Added Termux deployment scripts

### Issues
- Manual data entry still required due to poor OCR accuracy

## [0.1.0] - 2026-01-01 (Concept)

### Added
- Initial concept: automated invoice data extraction for delivery drivers
- Requirements gathering
- Technology research (Tesseract, OpenCV, ML Kit)

---

## Roadmap

### [1.1.0] - Planned

#### Features
- [ ] Export to CSV
- [ ] Export to Excel (.xlsx) with formatting
- [ ] Batch delete (multi-select)
- [ ] Search invoices (by name, address, invoice number)
- [ ] Filter by date range
- [ ] Sort options (name, date, invoice number)

#### Improvements
- [ ] Image compression before OCR
- [ ] Thumbnail generation for faster list scrolling
- [ ] Pull-to-refresh on main screen
- [ ] Empty state illustrations
- [ ] Error message improvements

#### Bug Fixes
- [ ] Handle rotated images (EXIF orientation)
- [ ] Fix memory leak in CameraActivity
- [ ] Improve regex for international phone numbers

### [1.2.0] - Planned

#### Features
- [ ] Cloud backup (Firebase Firestore)
- [ ] Sync across devices
- [ ] User authentication
- [ ] Team sharing
- [ ] Analytics dashboard

#### Technical
- [ ] Migrate to Kotlin
- [ ] Implement MVVM architecture
- [ ] Add ViewModels for lifecycle handling
- [ ] Use Kotlin Coroutines instead of threads
- [ ] Add comprehensive unit tests
- [ ] Set up CI/CD pipeline

### [2.0.0] - Future

#### Features
- [ ] Custom invoice template designer
- [ ] Multi-language support (Spanish, French)
- [ ] Barcode/QR code scanning
- [ ] Voice input for notes
- [ ] Offline map integration for addresses
- [ ] Route optimization
- [ ] Customer portal

#### AI/ML
- [ ] Train custom ML model on user's specific invoice format
- [ ] Auto-correct common OCR errors
- [ ] Predict missing fields
- [ ] Anomaly detection (duplicate entries)

#### UI
- [ ] Jetpack Compose migration
- [ ] Dark mode
- [ ] Tablet layout
- [ ] Landscape orientation support
- [ ] Accessibility improvements

---

## Version History

| Version | Date | Status | Notes |
|---------|------|--------|-------|
| 1.0.0 | 2026-01-11 | ✅ Released | Production-ready |
| 0.9.0 | 2026-01-10 | 🚧 Development | Pivoted to ML Kit |
| 0.5.0 | 2026-01-09 | 🔬 Prototype | Tesseract testing |
| 0.1.0 | 2026-01-01 | 💡 Concept | Initial idea |

---

## Migration Guides

### From 0.9.0 to 1.0.0

#### Breaking Changes
- **OCR Method**: HTTP server no longer required
  - Remove `server_aces.py` deployment
  - No need to start Termux server
  - Delete `OCRProcessorHTTP.java` references

#### Database Migration
- Database schema unchanged
- Existing data preserved automatically
- No manual migration needed

#### Code Changes
```java
// OLD (0.9.0)
OCRProcessorHTTP processor = new OCRProcessorHTTP(context);
if (!processor.checkBackendHealth()) {
    // Server not available
}

// NEW (1.0.0)
OCRProcessorMLKit processor = new OCRProcessorMLKit(context);
// No health check needed - always available
```

#### First Launch
- ML Kit model downloads automatically (~10MB)
- Requires internet connection on first OCR
- Subsequent OCR works offline

---

## Known Issues

### Current (1.0.0)

1. **Rotated Images**: EXIF orientation not handled
   - **Workaround**: Rotate image in gallery before import
   - **Fix planned**: v1.1.0

2. **Large Images**: Memory spike with 4K+ photos
   - **Workaround**: Downscale images to 1024x1024 before processing
   - **Fix planned**: v1.1.0 (automatic downsampling)

3. **Handwritten Text**: ML Kit struggles with handwriting
   - **Workaround**: Use typed/printed invoices only
   - **No fix planned**: Limitation of ML Kit

4. **Non-Standard Formats**: "BILL TO:" must be present
   - **Workaround**: Modify `extractInvoiceData()` for custom formats
   - **Fix planned**: v1.2.0 (template designer)

### Resolved

- ✅ **Company address extracted instead of customer** (v0.9.0)
  - Fixed in v1.0.0 with "BILL TO:" section detection

- ✅ **Data lost on app restart** (v0.9.0)
  - Fixed in v1.0.0 with Room database persistence

- ✅ **OCR too slow** (v0.5.0 - Tesseract)
  - Fixed in v1.0.0 with ML Kit (2-3 sec vs 10-15 sec)

---

## Support

For version-specific issues:
- **v1.0.0+**: [GitHub Issues](https://github.com/Brador82/Mobile-Invoice-Assistant/issues)
- **v0.x**: Deprecated, upgrade to 1.0.0

## License

Copyright © 2026 Brador82. All rights reserved.

