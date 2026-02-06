@echo off
REM Generate Android App Icons from source image
REM Requires ImageMagick: https://imagemagick.org/script/download.php

setlocal enabledelayedexpansion

REM Check if ImageMagick is installed
where magick >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: ImageMagick not found!
    echo Please install ImageMagick from: https://imagemagick.org/script/download.php
    echo Or use the online tool: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
    pause
    exit /b 1
)

REM Source image
set SOURCE_IMAGE=mobile_ocr.png

REM Check if source image exists
if not exist "%SOURCE_IMAGE%" (
    echo ERROR: Source image not found: %SOURCE_IMAGE%
    echo Please save your icon image as: %SOURCE_IMAGE%
    pause
    exit /b 1
)

echo Generating Android app icons from %SOURCE_IMAGE%...
echo.

REM Define output directories
set RES_DIR=android\app\src\main\res

REM Create directories if they don't exist
for %%D in (mdpi hdpi xhdpi xxhdpi xxxhdpi) do (
    if not exist "%RES_DIR%\mipmap-%%D" mkdir "%RES_DIR%\mipmap-%%D"
)

REM Generate icons for each density
echo Generating mipmap-mdpi (48x48)...
magick "%SOURCE_IMAGE%" -resize 48x48 "%RES_DIR%\mipmap-mdpi\ic_launcher.png"
magick "%SOURCE_IMAGE%" -resize 48x48 "%RES_DIR%\mipmap-mdpi\ic_launcher_round.png"

echo Generating mipmap-hdpi (72x72)...
magick "%SOURCE_IMAGE%" -resize 72x72 "%RES_DIR%\mipmap-hdpi\ic_launcher.png"
magick "%SOURCE_IMAGE%" -resize 72x72 "%RES_DIR%\mipmap-hdpi\ic_launcher_round.png"

echo Generating mipmap-xhdpi (96x96)...
magick "%SOURCE_IMAGE%" -resize 96x96 "%RES_DIR%\mipmap-xhdpi\ic_launcher.png"
magick "%SOURCE_IMAGE%" -resize 96x96 "%RES_DIR%\mipmap-xhdpi\ic_launcher_round.png"

echo Generating mipmap-xxhdpi (144x144)...
magick "%SOURCE_IMAGE%" -resize 144x144 "%RES_DIR%\mipmap-xxhdpi\ic_launcher.png"
magick "%SOURCE_IMAGE%" -resize 144x144 "%RES_DIR%\mipmap-xxhdpi\ic_launcher_round.png"

echo Generating mipmap-xxxhdpi (192x192)...
magick "%SOURCE_IMAGE%" -resize 192x192 "%RES_DIR%\mipmap-xxxhdpi\ic_launcher.png"
magick "%SOURCE_IMAGE%" -resize 192x192 "%RES_DIR%\mipmap-xxxhdpi\ic_launcher_round.png"

echo.
echo ========================================
echo App icons generated successfully!
echo ========================================
echo.
echo Icons have been placed in:
echo   %RES_DIR%\mipmap-mdpi\
echo   %RES_DIR%\mipmap-hdpi\
echo   %RES_DIR%\mipmap-xhdpi\
echo   %RES_DIR%\mipmap-xxhdpi\
echo   %RES_DIR%\mipmap-xxxhdpi\
echo.
echo Next steps:
echo 1. Rebuild your app: cd android ^&^& gradlew clean assembleDebug
echo 2. Install on device to see the new icon
echo.
pause
