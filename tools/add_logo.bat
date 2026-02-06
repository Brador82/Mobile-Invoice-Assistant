@echo off
REM Quick logo integration script
echo ============================================
echo Mobile Invoice OCR - Logo Integration
echo ============================================
echo.

REM Check for logo file
if exist "logo.png" (
    echo [OK] Found logo.png
    set LOGO=logo.png
) else if exist "mobile_ocr.png" (
    echo [OK] Found mobile_ocr.png
    set LOGO=mobile_ocr.png
) else if exist "icon.png" (
    echo [OK] Found icon.png
    set LOGO=icon.png
) else (
    echo [ERROR] No logo file found!
    echo.
    echo Please save your logo as one of these names:
    echo   - logo.png
    echo   - mobile_ocr.png
    echo   - icon.png
    echo.
    echo The logo should be in this directory:
    echo   %CD%
    echo.
    pause
    exit /b 1
)

echo.
echo Logo file: %LOGO%
echo.

REM Check if ImageMagick is installed
where magick >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [METHOD] Using ImageMagick
    call generate_app_icons.bat
    goto build
)

echo [INFO] ImageMagick not installed
echo.
echo OPTION 1: Install ImageMagick
echo   Download: https://imagemagick.org/script/download.php
echo   Then run this script again
echo.
echo OPTION 2: Use Online Tool (Recommended)
echo   1. Go to: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
echo   2. Upload: %CD%\%LOGO%
echo   3. Download generated ZIP
echo   4. Extract to: android\app\src\main\res\
echo   5. Run: android\build-and-install.bat
echo.
echo Press any key to open the online tool...
pause >nul
start https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
start explorer "%CD%"
echo.
echo After downloading and extracting the ZIP:
echo Press any key to build and install...
pause >nul

:build
echo.
echo ============================================
echo Building and installing app...
echo ============================================
cd android
call build-and-install.bat
cd ..
echo.
echo ============================================
echo Done! Check your device for the new logo.
echo ============================================
pause
