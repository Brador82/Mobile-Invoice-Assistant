# Privacy Policy for Mobile Invoice Assistant

**Last Updated:** January 20, 2026

## Introduction

Mobile Invoice Assistant ("we", "our", or "the app") is committed to protecting your privacy. This privacy policy explains how our Android application collects, uses, and protects your information.

## Information We Collect

### Data Stored Locally on Your Device

Mobile Invoice Assistant stores all data locally on your Android device. We collect and store:

1. **Invoice Information:**
   - Customer names and addresses
   - Phone numbers
   - Invoice numbers
   - Delivery notes
   - Item descriptions

2. **Images:**
   - Invoice photos
   - Proof of Delivery (POD) photos
   - Digital signatures

3. **Location Data (Optional):**
   - Your current location for route optimization start point
   - Only collected when you use the route optimization feature
   - Never shared or transmitted

### Data NOT Collected

We do NOT collect, transmit, or have access to:
- Personal identifying information beyond what you manually enter
- Browsing history
- Device information
- Usage analytics
- Crash reports
- Any data from your device

## How We Use Your Information

All data processing happens **entirely on your device**:

1. **OCR Processing:** Google ML Kit processes invoice images locally on your device to extract text
2. **Route Optimization:** Calculates delivery routes using locally stored addresses
3. **Data Storage:** All information saved to local SQLite database on your device
4. **Photo Storage:** Images saved to app's private storage directory

## Data Storage and Security

### Local Storage Only

- All data remains on your Android device
- Stored in app's private directory (inaccessible to other apps)
- Automatically deleted if you uninstall the app
- No cloud storage or remote servers

### Security Measures

- App uses Android's built-in security features
- Private storage prevents access by other apps
- No internet connection required for core features

## Third-Party Services

### Google ML Kit (On-Device)

- Performs text recognition (OCR) entirely on your device
- No data sent to Google servers
- No internet connection required
- Privacy-preserving technology

### Google Maps SDK (Optional)

- Used only when you activate route optimization
- Requires internet connection for geocoding addresses
- Subject to Google Maps Platform Terms of Service
- Only your current location and delivery addresses are accessed
- You can decline location permission and still use all other features

### Android System Permissions

The app requests these permissions:

1. **Camera** - To photograph invoices and POD
   - Only when you tap camera buttons
   - Photos saved locally only

2. **Storage** - To save and access photos
   - Required for POD and signature features
   - All data in app's private directory

3. **Location (Optional)** - For route optimization starting point
   - Only when using route optimization
   - You can deny and still use the app
   - Never transmitted except to Google Maps for geocoding

## Data Sharing

**We do NOT share your data with any third parties.**

### User-Initiated Sharing Only

The ONLY time data leaves your device is when YOU choose to:
- Export to CSV/Excel/JSON
- Share via email, Google Drive, Dropbox, etc.
- Use Android's built-in share dialog

These actions are entirely controlled by you and use your choice of apps.

## Data Retention

### On Your Device

- Data persists until you:
  - Delete individual invoices
  - Use "Clear All Data" option
  - Uninstall the app

### No Remote Storage

- We do not store any of your data on remote servers
- We have no access to your information
- Data cannot be recovered after uninstall

## Children's Privacy

Mobile Invoice Assistant is intended for business and professional use. We do not knowingly collect information from children under 13. If you are under 13, please do not use this app.

## Your Rights and Choices

### You Have Complete Control:

1. **Access:** All your data is visible in the app
2. **Export:** Export all data to various formats anytime
3. **Delete:** Delete individual items or all data
4. **Share:** Choose when and how to share data

### Permissions Control:

- Grant or revoke camera permission anytime in Android settings
- Grant or revoke location permission anytime
- App functions work without location permission

## Changes to This Privacy Policy

We may update this privacy policy from time to time. Updates will be posted within the app and on this page with a new "Last Updated" date.

## Contact Us

If you have questions about this privacy policy, please contact us:

**Email:** [Your contact email]
**GitHub:** https://github.com/Brador82/Mobile-Invoice-Assistant

## Legal Compliance

This app complies with:
- Google Play Developer Program Policies
- Android Platform Security
- GDPR (when applicable) - as no personal data is transmitted
- CCPA (when applicable) - as no personal data is sold or shared

## Summary

**In Plain English:**

✅ Everything stays on your phone  
✅ No cloud storage or servers  
✅ No tracking or analytics  
✅ You control all data  
✅ OCR works offline  
✅ Only you can share your data  
✅ Delete anytime  

**We take your privacy seriously. Your data is yours, and it stays on your device.**

---

**Mobile Invoice Assistant**  
Version 1.0.0  
Privacy Policy Version 1.0  
Effective Date: January 20, 2026
