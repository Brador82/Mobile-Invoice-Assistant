# 📤 Export Guide

## Overview

The Mobile Invoice OCR app exports your invoices to the Downloads folder, where you can easily share them using your device's built-in file manager and sharing options.

## Export Locations

All exports are saved to:
```
/storage/emulated/0/Download/MobileInvoiceOCR/
```

Access via:
- Files app → Downloads → MobileInvoiceOCR
- My Files → Downloads → MobileInvoiceOCR
- Any file manager app

## How to Export

1. **Export Your Data**
   - Tap "Export Delivery Cards" or "Export Markdown"
   - Wait for the export process to complete
   - You'll see a success message with the file location

2. **Share the Export**
   - Open your Files/My Files app
   - Navigate to Downloads → MobileInvoiceOCR
   - Long-press the exported file or folder
   - Tap "Share" to choose your sharing method:
     - **Email** - Gmail, Outlook, etc.
     - **Cloud Storage** - Google Drive, Dropbox, OneDrive
     - **Messaging** - WhatsApp, SMS/MMS, Messenger
     - **QuickShare/Nearby** - Quick device-to-device transfer
     - **Bluetooth** - Send to nearby devices
     - And more...

## Export Formats

### Delivery Card Folders
- **File Type**: Folder with JSON files and images
- **Location**: `Downloads/MobileInvoiceOCR/Deliveries (MM/DD/YYYY)/`
- **Contains**: Individual card folders with data and photos
- **Best For**: Complete delivery records with images

### Markdown Export
- **File Type**: .md (Markdown)
- **Location**: `Downloads/MobileInvoiceOCR/invoices_YYYYMMDD_HHMMSS.md`
- **Contains**: Formatted text with embedded image references
- **Best For**: Documentation, GitHub, email

### Excel Export
- **File Type**: .xls (Tab-separated values)
- **Location**: `Downloads/MobileInvoiceOCR/invoices_YYYYMMDD_HHMMSS.xls`
- **Contains**: Spreadsheet data
- **Best For**: Data analysis, record keeping

### JSON Export  
- **File Type**: .json
- **Location**: `Downloads/MobileInvoiceOCR/invoices_YYYYMMDD_HHMMSS.json`
- **Contains**: Structured data with all invoice details
- **Best For**: System integration, backup

## Tips

1. **Find Your Exports**: Look for the timestamp in the filename
2. **Batch Sharing**: Select multiple files in file manager to share at once
3. **Cloud Backup**: Share to Google Drive for automatic backup
4. **Email Attachments**: Most email apps support multiple file attachments
5. **Compress Large Exports**: Use file manager's "Compress" feature for folders

## OCR Extraction Improvements

The OCR now intelligently extracts:
- **Customer Name**: Clean name without IDs or extra characters
- **Address**: Street address only (stops at email, pricing, etc.)
- **Phone**: Formatted as (XXX) XXX-XXXX
- **Invoice Number**: Company reference from invoice header
- **Items**: Appliances/products delivered

## Troubleshooting

### Can't Find Export
- Check Downloads → MobileInvoiceOCR folder
- Make sure export completed successfully (check toast message)
- Try a different file manager app

### Address Too Long / Contains Email
- This has been fixed in the latest version
- Re-process the invoice to get clean addresses
- Address now stops at email/pricing information

### Want to Share Immediately
- Open Files app right after export completes
- The newest files appear at the top
- Long-press and tap "Share"

---

**Last Updated**: January 20, 2026
**Version**: 1.1.1
**Feature Status**: ✅ Simplified Export Flow
