# Usage Guide

Complete instructions for using the Mobile Invoice OCR app.

## Overview

Mobile Invoice OCR automates the extraction of customer information from delivery invoices. Instead of manually typing customer names, addresses, and phone numbers, simply photograph the invoice and let ML Kit extract the data automatically.

## Main Screen

### Upload Section

#### Upload from Gallery
1. Tap **📁 UPLOAD** button
2. Select one or more invoice images from gallery
3. Selected images appear in preview strip
4. Counter shows "Selected X images"

#### Capture with Camera
1. Tap **📷 CAMERA** button
2. Take photo of invoice (ensure "BILL TO:" section is visible)
3. Confirm capture
4. Image added to selection

**Tips for Best Results:**
- Ensure invoice is well-lit (no shadows)
- Keep invoice flat (minimize creases/wrinkles)
- Include entire "BILL TO:" section in frame
- Avoid extreme angles (slight tilt is OK)

### Process OCR

1. With images selected, tap **🧠 PROCESS ALL WITH OCR**
2. Progress bar shows processing status
3. ML Kit extracts data from each image
4. Extracted invoices appear in "Delivery Records" list below

**Processing Time**: ~2-3 seconds per image

### Delivery Records List

Shows all processed invoices with:
- **Invoice Number** (top left)
- **Customer Name**
- **Address**
- **Phone Number**
- **Timestamp** (when processed)

#### Actions:
- **Tap invoice** → View full details
- **Tap trash icon** → Delete invoice
- **Long press** → (Future: bulk select)

## Invoice Detail Screen

### View/Edit Invoice

Displays complete extracted information:

#### Basic Information
- **Invoice #**: Auto-extracted or generated (INV-XXXXXX)
- **Customer Name**: Full name from "BILL TO:" section
- **Address**: Complete delivery address
- **Phone**: Primary contact number

#### Items Section
- List of purchased items (if extracted from table)
- Editable text field for manual entry/correction

#### POD Photo
- **Proof of Delivery** photo
- Tap **ADD POD PHOTO** to capture delivery confirmation
- Shows thumbnail after capture

#### Signature
- Customer signature field
- Tap **ADD SIGNATURE** to capture signature
- Tap **CHANGE SIGNATURE** to replace

#### Notes
- Free-form notes field
- Add delivery instructions, issues, or observations

### Edit Data

All fields are editable:
1. Tap any field to edit
2. Make changes
3. Changes save automatically on back navigation

## Camera Activity

### Capture Invoice

1. Frame invoice in viewfinder
2. Tap **capture button** (bottom center)
3. Review captured image
4. Tap **✓** to confirm or **✗** to retake

**Camera Tips:**
- Use natural lighting when possible
- Avoid flash (causes glare on glossy invoices)
- Hold phone steady (use burst mode for shaky hands)
- Keep invoice edges within frame

## Data Management

### Export Data

#### Export to CSV
1. Tap **EXPORT CSV** button
2. Choose save location
3. CSV file contains all invoice data

**CSV Format:**
```
Invoice Number,Customer Name,Address,Phone,Timestamp,Items,Notes
INV-000001,DAVID MCKEOWN,"4461 S Roanoke Ave, Springfield, MO 65810",417-818-1235,2026-01-11 08:15,Washer,Delivered to side door
```

#### Export to Excel
1. Tap **EXPORT EXCEL** button
2. Choose save location
3. Excel file (.xlsx) with formatted data

### Delete Invoices

**Single Invoice:**
1. Tap trash icon on invoice in list
2. Confirm deletion
3. Invoice removed from database

**Bulk Delete (Future):**
- Long press to enter selection mode
- Select multiple invoices
- Tap delete all

### Database Persistence

All invoices are stored in local SQLite database:
- **Location**: `/data/data/com.mobileinvoice.ocr/databases/invoice_database`
- **Automatic backup**: Not implemented (add to roadmap)
- **Data persists**: After app restart, phone reboot
- **Clear data**: Settings → Apps → Mobile Invoice OCR → Clear Data

## Workflow Examples

### Typical Delivery Route

**Start of Day:**
1. Open app
2. Review yesterday's invoices (if needed)
3. Clear old invoices or export to Excel

**At Each Delivery:**
1. Complete delivery
2. Tap **CAMERA** in app
3. Photograph customer's invoice copy
4. OCR processes automatically
5. Verify extracted data
6. Add POD photo
7. Capture customer signature
8. Add notes if needed
9. Move to next delivery

**End of Day:**
1. Review all deliveries
2. Export to Excel
3. Send report to office

### Batch Processing (Office Use)

**Processing Historical Invoices:**
1. Collect invoice photos in folder
2. Open app on tablet/phone
3. Tap **UPLOAD**
4. Select all invoice images
5. Tap **PROCESS ALL**
6. Wait for batch processing
7. Review and correct any extraction errors
8. Export complete dataset

## Extraction Accuracy

### What Extracts Well
- ✅ Customer names (clean fonts)
- ✅ Addresses (standard formats)
- ✅ Phone numbers (XXX-XXX-XXXX format)
- ✅ Invoice IDs (alphanumeric codes)

### What May Need Correction
- ⚠️ Handwritten notes
- ⚠️ Damaged/wrinkled invoices
- ⚠️ Low contrast text
- ⚠️ Very small fonts (<8pt)
- ⚠️ Non-standard layouts

### Error Handling

**If OCR Extracts Wrong Data:**
1. Open invoice detail screen
2. Tap field to edit
3. Manually correct information
4. Changes save automatically

**If OCR Fails Completely:**
- Result shows: "Unknown Customer", "No address found"
- Manually enter all fields
- Consider retaking photo with better lighting/angle

## Common Issues

### "BILL TO: not found"

**Cause**: Invoice format doesn't match expected layout

**Solution**:
1. Ensure "BILL TO:" text is visible in photo
2. Check if invoice uses different label ("CUSTOMER:", "SHIP TO:")
3. Edit `OCRProcessorMLKit.java` to recognize alternate labels

### Wrong Data Extracted

**Cause**: ML Kit reads company header instead of customer section

**Solution**:
1. Ensure customer section is prominently visible
2. Crop photo to focus on customer area
3. Check extraction logic in code

### Camera Not Working

**Cause**: Permission denied

**Solution**:
1. Settings → Apps → Mobile Invoice OCR → Permissions
2. Enable Camera permission

### App Crashes on OCR

**Cause**: ML Kit model not downloaded

**Solution**:
1. Connect to internet on first launch
2. Wait for ML Kit model download (~10MB)
3. Retry OCR processing

## Best Practices

### Photography
- **Lighting**: Natural daylight or bright indoor lighting
- **Distance**: 12-18 inches from invoice
- **Angle**: Straight-on (±15° is acceptable)
- **Focus**: Tap screen to focus on text
- **Stability**: Use two hands or prop phone

### Data Verification
- Always spot-check extracted data
- Verify phone numbers (most critical for customer contact)
- Confirm address matches what customer provided
- Cross-reference invoice number with company records

### Performance
- Process invoices in batches of 10-20 for best responsiveness
- Clear old invoices weekly to maintain app speed
- Export and backup data regularly

## Keyboard Shortcuts (Future)

- `Space` - Capture photo
- `Enter` - Confirm/Save
- `Esc` - Cancel/Back
- `Ctrl+E` - Export CSV
- `Ctrl+S` - Save current invoice

## Accessibility

### Screen Reader Support
- All buttons have content descriptions
- Fields announce current values
- Navigation accessible via TalkBack

### Large Text
- Respects system font size settings
- Scales up to 200% text size

### Color Contrast
- Material Design 3 ensures WCAG AA compliance
- Dark mode available (system setting)

## Tips & Tricks

1. **Batch Processing**: Select 20+ invoices at once for efficient data entry
2. **Quality Check**: Review first few extractions to ensure format compatibility
3. **Backup Routine**: Export to Excel daily, keep 7-day backup rotation
4. **Field Templates**: Copy/paste common address formats for quick correction
5. **Offline Use**: All OCR works without internet (after initial ML Kit download)

## Next Steps

- Review [TECHNICAL.md](TECHNICAL.md) for architecture details
- Check [API.md](API.md) to customize extraction logic
- See [SETUP.md](SETUP.md) for building from source

## Support

For questions or issues:
- GitHub: https://github.com/Brador82/Mobile-Invoice-Assistant/issues
- Documentation: https://github.com/Brador82/Mobile-Invoice-Assistant/wiki
