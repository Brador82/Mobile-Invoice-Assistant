# Fresh Repository Setup Guide

This guide will help you create a fresh Git repository for your Mobile Invoice OCR app.

## Option 1: Fresh Start (Recommended)

This approach creates a completely new repository with a clean history.

### Step 1: Backup Current Work
```bash
# Create a backup of your current repository
cd c:\Workspace\Projects
cp -r Mobile_Invoice_ocr Mobile_Invoice_ocr_backup
```

### Step 2: Remove Old Git History
```bash
cd c:\Workspace\Projects\Mobile_Invoice_ocr

# Remove the old .git directory
rm -rf .git

# Clean up any build artifacts
cd android
./gradlew clean
cd ..
```

### Step 3: Initialize Fresh Repository
```bash
# Initialize a new Git repository
git init

# Add all files (respecting .gitignore)
git add .

# Create initial commit
git commit -m "Initial commit: Mobile Invoice OCR v2.0

Features:
- ML Kit OCR for invoice scanning
- Route optimization with Google Maps integration
- Drag-and-drop invoice reordering
- Export to CSV with sharing capabilities
- Room database for local storage
- Material Design UI"
```

### Step 4: Create GitHub Repository
1. Go to https://github.com/new
2. Name: `Mobile_Invoice_OCR` (or your preferred name)
3. Description: "Android app for invoice scanning with OCR and route optimization"
4. Set to Public or Private as preferred
5. **Do NOT** initialize with README, .gitignore, or license (we already have these)
6. Click "Create repository"

### Step 5: Push to GitHub
```bash
# Add the remote repository
git remote add origin https://github.com/YOUR_USERNAME/Mobile_Invoice_OCR.git

# Push the code
git branch -M main
git push -u origin main
```

## Option 2: New Branch from Current Repository

If you want to keep the git history but start with a clean branch:

```bash
# Create and switch to a new branch
git checkout -b fresh-start

# Stage all current changes
git add .

# Commit everything
git commit -m "Fresh start: Complete Mobile Invoice OCR v2.0"

# Push to create a new branch on GitHub
git push -u origin fresh-start
```

## Cleaning Up Before Committing

Before creating your fresh repository, consider cleaning up:

### Remove Build Artifacts
```bash
# Clean Android build
cd android
./gradlew clean
cd ..

# Remove generated files
rm -rf android/app/build/
rm -rf android/build/
rm -rf android/.gradle/
```

### Remove Temporary Files
```bash
# Remove log files
rm android/device_log.txt
rm android/device_log_after_install.txt

# Remove session summaries (keep only important docs)
rm SESSION_SUMMARY_*.md
rm CLEANUP_SUMMARY.md

# Remove desktop.ini files
find . -name "desktop.ini" -type f -delete
```

### Remove Sensitive Data
```bash
# If you have any API keys or secrets, remove them
# Check these files:
# - android/app/src/main/res/values/strings.xml (Google Maps API key)
# - android/local.properties
# - Any configuration files with credentials
```

## What's Included in the Fresh Repository

The fresh repository will include:

### Core Application
- `/android/` - Complete Android project with Gradle build system
- Java source files in `/android/app/src/main/java/`
- XML layouts in `/android/app/src/main/res/`

### Documentation
- `README.md` - Main project documentation
- `CHANGELOG.md` - Version history
- `FEATURES.md` - Feature list
- `STATUS.md` - Current project status
- `/docs/` - Detailed guides and technical documentation

### Configuration Files
- `.gitignore` - Ignore patterns for build artifacts
- `LICENSE` - Project license
- `android/build.gradle` - Gradle build configuration

### Quick Reference Guides
- `QUICKREF.md` - Quick reference for common tasks
- `DRAG_DROP_QUICKREF.md` - Drag and drop feature guide
- `ROUTE_QUICKREF.md` - Route optimization guide

## What's Excluded (via .gitignore)

- Build artifacts (`*.apk`, `build/`, `.gradle/`)
- IDE files (`.idea/`, `*.iml`)
- Log files (`*.log`, device logs)
- Temporary files (`*.tmp`, `desktop.ini`)
- Test images (`tools/*.jpg`)
- User-specific workspace files (`*.code-workspace`)
- Archive folder with old code

## Post-Setup Tasks

After creating your fresh repository:

1. **Update README.md** with current project information
2. **Add Topics** on GitHub for discoverability:
   - `android`, `ocr`, `invoice`, `ml-kit`, `route-optimization`, `google-maps`
3. **Create Releases** to tag stable versions
4. **Enable GitHub Actions** for CI/CD if desired
5. **Add a Shield Badge** to show build status
6. **Update Documentation** links if repository URL changed

## Repository Structure Best Practices

```
Mobile_Invoice_OCR/
├── README.md                    # Main entry point
├── CHANGELOG.md                 # Version history
├── LICENSE                      # Open source license
├── .gitignore                   # Ignore patterns
├── android/                     # Android app source
│   ├── app/                     # Main application module
│   ├── build.gradle             # Build configuration
│   └── gradle/                  # Gradle wrapper
├── docs/                        # Detailed documentation
│   ├── QUICKSTART.md
│   ├── BUILD_GUIDE.md
│   └── guides/
└── tools/                       # Development tools/scripts
```

## Troubleshooting

### Large Files Won't Push
If you encounter issues with large files:
```bash
# Check for large files
find . -type f -size +50M

# Consider using Git LFS for large files
git lfs install
git lfs track "*.apk"
```

### Permission Denied on Push
```bash
# Use SSH instead of HTTPS
git remote set-url origin git@github.com:YOUR_USERNAME/Mobile_Invoice_OCR.git
```

### Accidentally Committed Secrets
```bash
# Remove from history (use with caution!)
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch path/to/secret/file" \
  --prune-empty --tag-name-filter cat -- --all
```

## Next Steps

After setting up your fresh repository:

1. Share the repository with collaborators
2. Set up branch protection rules
3. Configure issue templates
4. Add a CONTRIBUTING.md guide
5. Set up continuous integration with GitHub Actions

## Need Help?

- Check the [GitHub Docs](https://docs.github.com)
- Review existing documentation in `/docs/`
- See `QUICKREF.md` for common commands
