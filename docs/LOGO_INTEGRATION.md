# Logo Integration Guide - Mobile Invoice OCR

## Your Logo
The Mobile Invoice OCR logo features:
- Stylized document/invoice with orange and black colors
- Camera icon in the center
- Orange "MOBILE INVOICE OCR" text
- Professional delivery/scanning aesthetic

## Integration Steps

### Method 1: Automated Generation (Recommended)

#### Prerequisites
- ImageMagick installed (https://imagemagick.org/script/download.php)
- Logo saved as PNG (1024x1024 pixels recommended)

#### Steps

1. **Prepare Your Logo**
   - Save the logo as: `c:\Workspace\Projects\Mobile_Invoice_ocr\mobile_ocr.png`
   - Recommended size: 1024x1024 pixels (minimum 512x512)
   - Format: PNG with transparent or white background
   - Make sure it's square (same width and height)

2. **Run the Generator**
   ```bash
   cd c:\Workspace\Projects\Mobile_Invoice_ocr
   generate_app_icons.bat
   ```

3. **Verify Output**
   The script creates icons in these sizes:
   - `mipmap-mdpi` - 48x48 pixels
   - `mipmap-hdpi` - 72x72 pixels
   - `mipmap-xhdpi` - 96x96 pixels
   - `mipmap-xxhdpi` - 144x144 pixels
   - `mipmap-xxxhdpi` - 192x192 pixels

4. **Build and Install**
   ```bash
   cd android
   .\build-and-install.bat
   ```

5. **Check on Device**
   - Look at the app icon in the launcher
   - Check the app switcher (recent apps)
   - Verify all sizes look crisp

---

### Method 2: Online Tool (No Installation Required)

If ImageMagick is not installed:

#### Steps

1. **Go to Android Asset Studio**
   - URL: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html

2. **Upload Your Logo**
   - Click "Image" tab
   - Upload your logo PNG file
   - Adjust padding if needed (usually 10-20%)

3. **Configure Settings**
   - **Name:** `ic_launcher` (default, leave as-is)
   - **Shape:** 
     - Choose "Square" for full logo visibility
     - Or "Circle" for modern rounded icon
   - **Background:** 
     - "Transparent" if logo has its own background
     - "White" or custom color if needed

4. **Download**
   - Click "Download" button
   - Saves as `ic_launcher.zip`

5. **Extract and Copy**
   ```bash
   # Extract the ZIP file
   # Copy contents to your project:
   ```
   
   From the extracted ZIP, copy:
   ```
   res/mipmap-mdpi/*
   res/mipmap-hdpi/*
   res/mipmap-xhdpi/*
   res/mipmap-xxhdpi/*
   res/mipmap-xxxhdpi/*
   ```
   
   To:
   ```
   c:\Workspace\Projects\Mobile_Invoice_ocr\android\app\src\main\res\
   ```

6. **Build and Install**
   ```bash
   cd c:\Workspace\Projects\Mobile_Invoice_ocr\android
   .\build-and-install.bat
   ```

---

### Method 3: Manual Resizing (Advanced)

If you want to do it manually with any image editor:

Create these files with exact dimensions:

```
android/app/src/main/res/
├── mipmap-mdpi/
│   ├── ic_launcher.png (48x48)
│   └── ic_launcher_round.png (48x48)
├── mipmap-hdpi/
│   ├── ic_launcher.png (72x72)
│   └── ic_launcher_round.png (72x72)
├── mipmap-xhdpi/
│   ├── ic_launcher.png (96x96)
│   └── ic_launcher_round.png (96x96)
├── mipmap-xxhdpi/
│   ├── ic_launcher.png (144x144)
│   └── ic_launcher_round.png (144x144)
└── mipmap-xxxhdpi/
    ├── ic_launcher.png (192x192)
    └── ic_launcher_round.png (192x192)
```

## Design Tips for Your Logo

### Icon Optimization
Your logo has orange/black colors which work great for an app icon:

1. **Simplified Version for Small Sizes**
   - For mdpi/hdpi (48-72px), consider simplifying details
   - Focus on the camera and document outline
   - Ensure text is readable at small sizes

2. **Background**
   - If using transparent background, add subtle shadow
   - Or use white/light gray background for contrast
   - Orange background could work if you invert the colors

3. **Safe Zone**
   - Keep important elements in the center 80% of the icon
   - Android may crop edges for adaptive icons
   - Camera icon should be clearly visible

4. **Testing**
   - Test on different Android versions
   - Check light and dark launcher themes
   - Verify icon looks good in app switcher

## Adaptive Icon (Android 8.0+)

For modern Android devices with adaptive icons:

### Current Setup
The app already has adaptive icon configuration:
- `mipmap-anydpi-v26/ic_launcher.xml`
- `mipmap-anydpi-v26/ic_launcher_round.xml`
- `drawable/ic_launcher_foreground.xml`

### To Customize Adaptive Icon

1. **Create Foreground Layer**
   Edit: `android/app/src/main/res/drawable/ic_launcher_foreground.xml`
   - This should be your logo (transparent background)
   - Size: 108x108dp (with 66dp safe zone in center)

2. **Create Background Layer** (optional)
   - Solid color or gradient
   - Current: Uses default colors
   - Can be PNG or vector drawable

3. **Preview Adaptive Icons**
   - Android Studio: Resource Manager → ic_launcher
   - Or use online tool: https://adapticon.tooo.io/

## Verification Checklist

After integration:

- [ ] Logo appears in app launcher
- [ ] Logo appears in recent apps (app switcher)
- [ ] Logo is crisp on all screen densities
- [ ] Colors are vibrant and clear
- [ ] Camera icon is visible at all sizes
- [ ] Text is readable (or omit if too small)
- [ ] Logo works on light and dark launcher backgrounds
- [ ] Adaptive icon (Android 8+) looks good with shape masks

## Troubleshooting

### Logo looks blurry
- Increase source image resolution (use 1024x1024)
- Save as PNG, not JPEG
- Ensure no compression artifacts

### Logo is cropped
- Add more padding in Asset Studio
- Keep important elements within 80% safe zone
- Check adaptive icon preview

### Colors look washed out
- Increase contrast
- Use brighter orange
- Add drop shadow or outline

### Logo too complex for small sizes
- Create simplified version for mdpi/hdpi
- Use `ic_launcher.png` (square) for detailed version
- Use `ic_launcher_round.png` for simplified version

## Quick Command Reference

```bash
# Generate icons (with ImageMagick)
cd c:\Workspace\Projects\Mobile_Invoice_ocr
generate_app_icons.bat

# Build and install app
cd android
.\build-and-install.bat

# Check installed icon
adb shell pm list packages | findstr mobileinvoice
adb shell monkey -p com.mobileinvoice.ocr 1
```

## Next Steps

After integrating the logo:

1. **Test on real device** - Icons look different than emulator
2. **Update Play Store listing** - Use same logo for consistency
3. **Create feature graphic** - 1024x500px banner for Play Store
4. **Screenshots** - Update screenshots showing new icon
5. **Marketing materials** - Use consistent branding

## Resources

- Android Icon Guidelines: https://developer.android.com/guide/practices/ui_guidelines/icon_design_launcher
- Material Design Icons: https://material.io/design/iconography/product-icons.html
- Android Asset Studio: https://romannurik.github.io/AndroidAssetStudio/
- Adaptive Icon Preview: https://adapticon.tooo.io/
- ImageMagick Download: https://imagemagick.org/script/download.php
