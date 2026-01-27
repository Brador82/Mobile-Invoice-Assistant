# 📱 Mobile Invoice OCR

**A production-ready Android app for invoice scanning, data extraction, and delivery management.**

![Version](https://img.shields.io/badge/version-1.2.0-blue)
![Status](https://img.shields.io/badge/status-production-green)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![License](https://img.shields.io/badge/license-MIT-orange)

## ✨ Features

- 📸 **Photo Capture** - Camera integration for invoice scanning
- 🤖 **On-Device OCR** - Google ML Kit text recognition (95%+ accuracy)
- 💾 **Persistent Storage** - Room database with full CRUD operations
- ✍️ **Signature Capture** - Digital signature pad
- 📦 **Items Selection** - Multi-select for 10 appliance types
- 🚚 **POD Photos** - Proof of delivery capture (3 photos per invoice)
- 🗺️ **Split-Screen Route Optimization** - Map + interactive delivery list 🆕
- 🔄 **Drag-and-Drop Reordering** - Long-press to manually adjust route order 🆕
- 📱 **In-App Navigation** - Call customers and navigate without leaving the app 🆕
- 🧭 **Dual Navigation Modes** - Full route or individual stop navigation 🆕
- 🗺️ **Expand/Collapse Map** - Toggle between split-view and full-screen map 🆕
- 📤 **Export** - CSV, Excel (TSV), JSON, and Markdown formats
- ☁️ **Cloud Sharing** - Direct export to Google Drive, Dropbox, OneDrive
- 💾 **Data Persistence** - Survives app restart and device reboot

## 🚀 Quick Start

### Prerequisites
- Android device with API 26+ (Android 8.0+)
- **Option A:** Android Studio for GUI development
- **Option B:** VS Code + Gradle for lightweight development

### Build with VS Code (Recommended)

```bash
# Navigate to android folder
cd android

# Build debug APK
./gradlew assembleDebug      # Linux/macOS
.\gradlew assembleDebug      # Windows PowerShell

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Detailed guide:** [docs/guides/VSCODE_BUILD_GUIDE.md](docs/guides/VSCODE_BUILD_GUIDE.md) ⭐

### Build with Android Studio

1. Open Android Studio
2. Open folder: `android/`
3. Sync Gradle (automatic)
4. Click ▶️ Run button

**Detailed guide:** [docs/guides/BUILD_GUIDE.md](docs/guides/BUILD_GUIDE.md)

## 📖 Documentation

### Essential Reading
- 📚 [Quick Reference](QUICKREF.md) - Commands, features, troubleshooting
- 📊 [Current Status](STATUS.md) - Implementation status and architecture
- 🎯 [Feature List](FEATURES.md) - Complete feature checklist
- 📁 [Project Structure](PROJECT_STRUCTURE.md) - Clean folder organization

### Build Guides
- 🆚 [VS Code Build Guide](docs/guides/VSCODE_BUILD_GUIDE.md) - Lightweight development ⭐ NEW
- 🎨 [Android Studio Guide](docs/guides/BUILD_GUIDE.md) - Full IDE experience
- ⚡ [Quick Start](docs/guides/QUICKSTART.md) - 5-minute setup

### Advanced
- 🔧 [Implementation Summary](docs/IMPLEMENTATION_SUMMARY.md) - Technical details
- 🔗 [Integration Guide](docs/guides/INTEGRATION.md) - API integration
- 📝 [Changelog](CHANGELOG.md) - Version history
- 🤝 [Contributing](docs/CONTRIBUTING.md) - How to contribute

## 🏗️ Architecture

```
┌─────────────────────────────────────────────┐
│          MainActivity                        │
│  Upload → Process → Display                 │
└─────────────┬───────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────────┐
│       OCRProcessorMLKit                      │
│  Google ML Kit (On-Device)                  │
└─────────────┬───────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────────┐
│       Room Database                          │
│  SQLite with persistence                    │
└─────────────┬───────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────────┐
│    InvoiceDetailActivity                     │
│  Edit → Add POD → Sign → Save               │
└─────────────┬───────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────────┐
│       ExportHelper                           │
│  CSV → Excel → JSON → Share                 │
└─────────────────────────────────────────────┘
```

## 📱 App Features

See [FEATURES.md](FEATURES.md) for a complete list of implemented features with details.

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| **Language** | Java 8 |
| **Platform** | Android (API 26-34) |
| **OCR Engine** | Google ML Kit Text Recognition |
| **Database** | Room (SQLite) |
| **Build System** | Gradle 8.5 |
| **IDE Support** | Android Studio, VS Code |
| **UI** | Material Design 3 |
| **Camera** | CameraX |

## 📦 Project Structure

```
Mobile-Invoice-Assistant/
├── android/              # Android application
│   ├── app/src/main/java/  # Source code
│   ├── build.gradle        # Dependencies
│   └── *.bat               # Build scripts
├── docs/                 # Documentation
│   └── guides/           # Build guides
├── archive/              # Legacy files (not tracked)
└── README.md             # This file
```

**Full structure:** [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)

## 🧪 Testing

```bash
# Run unit tests
./gradlew test              # Linux/macOS
.\gradlew test              # Windows PowerShell

# Run instrumentation tests (requires device)
./gradlew connectedAndroidTest

# View logs
adb logcat | grep "mobileinvoice"      # Linux/macOS
adb logcat | findstr "mobileinvoice"   # Windows
```

## 🤝 Contributing

We welcome contributions! Please see [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) for guidelines.

## 📄 License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Google ML Kit for on-device OCR
- Android Open Source Project
- Material Design components

## 📞 Support

- 🐛 Issues: [GitHub Issues](https://github.com/Brador82/Mobile-Invoice-Assistant/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/Brador82/Mobile-Invoice-Assistant/discussions)

## 🗺️ Roadmap

### Completed
- [x] Cloud sharing (Google Drive, Dropbox, OneDrive) - v1.2.0
- [x] Route optimization with split-screen map - v1.1.0
- [x] Drag-and-drop reordering - v1.1.0

### Planned
- [ ] PDF export with images
- [ ] Barcode scanning
- [ ] Statistics dashboard
- [ ] Multi-language support
- [ ] Dark mode theme

---

**Made with ❤️ for delivery drivers and logistics teams**

**Status:** Production Ready 🚀 | **Version:** 1.2.0 | **Last Updated:** January 27, 2026
