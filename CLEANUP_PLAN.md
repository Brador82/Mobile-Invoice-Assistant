# Project Cleanup Plan

**Created:** February 3, 2026
**Status:** PLAN ONLY - Do not execute until ready

This document outlines a structured approach to organizing and cleaning up the Mobile Invoice OCR project. Execute these steps only when you have time and a backup.

---

## Phase 1: Documentation Consolidation (Low Risk)

### Problem
There are 50+ markdown files with significant duplication and overlap.

### Current Documentation Files (52 total)

**Root Level (25 files):**
- API.md, CONTRIBUTING.md, SETUP.md, TECHNICAL.md, USAGE.md
- GOLDEN_PATH.md, QUICKREF.md, WHAT_IS_WHAT.md
- CLAUDE_CODE_BRIEF.md, LINUX_QUICK_SETUP.md
- ROUTE_OPTIMIZATION_IMPLEMENTATION.md, ROUTE_QUICKREF.md, SETUP_ROUTE_OPTIMIZATION.md
- DRAG_DROP_IMPLEMENTATION.md, DRAG_DROP_QUICKREF.md
- FRESH_REPO_SETUP.md, SPLIT_SCREEN_MAP_GUIDE.md
- EXPORT_SHARING_GUIDE.md, PLAY_STORE_PUBLISHING.md, PRIVACY_POLICY.md
- PLAY_STORE_LISTING_TEMPLATES.md
- OCR_FIXES.md, OCR_RESTORATION_SUMMARY.md, OCR_QUICKSTART.md
- LOGO_INTEGRATION.md, BUILD_GUIDE_DELIVERY_APP.md
- DESIGN_COMPARISON.md, QUICK_REFERENCE.md, FRESH_BUILD_SUMMARY.md
- VISUAL_ARCHITECTURE.md, DELIVERY_APP_ARCHITECTURE.md
- CHANGELOG.md, DOCS_REVIEW.md, FEATURES.md
- ICON_DESIGN_GUIDE.md, README.md, STATUS.md, CLEANUP_PLAN.md

**docs/ Folder (12 files):**
- IMPLEMENTATION_SUMMARY.md, TECHNICAL.md (duplicate!)
- API.md (duplicate!), CONTRIBUTING.md (duplicate!)
- SETUP.md (duplicate!), USAGE.md (duplicate!)
- ROUTE_OPTIMIZATION_GUIDE.md, GOOGLE_MAPS_SETUP.md
- DRAG_DROP_REORDERING.md
- guides/BUILD_GUIDE.md, guides/INTEGRATION.md
- guides/QUICKSTART.md, guides/VSCODE_BUILD_GUIDE.md

**Mobile_Invoice_Update_2.0/ (1 file):**
- CHANGELOG.md (old version)

### Recommended Structure

```
Mobile_Invoice_ocr/
├── README.md                    # Main overview (keep)
├── CHANGELOG.md                 # Version history (keep)
├── STATUS.md                    # Current status (keep)
├── FEATURES.md                  # Feature checklist (keep)
├── PRIVACY_POLICY.md            # Legal (keep)
├── docs/
│   ├── SETUP.md                 # Combined setup guide
│   ├── USAGE.md                 # How to use the app
│   ├── TECHNICAL.md             # Architecture & implementation
│   ├── CONTRIBUTING.md          # For contributors
│   ├── API.md                   # API reference
│   ├── TROUBLESHOOTING.md       # Common issues (NEW - consolidate)
│   └── guides/
│       ├── BUILD_GUIDE.md       # How to build
│       ├── GOOGLE_MAPS_SETUP.md # Maps API setup
│       └── PLAY_STORE.md        # Publishing guide (consolidate)
└── archive/                     # OLD docs (move, don't delete)
    └── (all other .md files)
```

### Action Items

1. **Keep these files (8 essential):**
   - README.md, CHANGELOG.md, STATUS.md, FEATURES.md
   - PRIVACY_POLICY.md
   - docs/SETUP.md, docs/USAGE.md, docs/TECHNICAL.md

2. **Consolidate these into docs/TROUBLESHOOTING.md:**
   - OCR_FIXES.md, OCR_RESTORATION_SUMMARY.md, OCR_QUICKSTART.md

3. **Consolidate these into docs/guides/PLAY_STORE.md:**
   - PLAY_STORE_PUBLISHING.md, PLAY_STORE_LISTING_TEMPLATES.md

4. **Move to archive/ folder:**
   - All QUICKREF files (redundant with STATUS.md)
   - All IMPLEMENTATION files (redundant with TECHNICAL.md)
   - Old design/comparison docs
   - Mobile_Invoice_Update_2.0/ entire folder

5. **Delete duplicates:**
   - docs/API.md (duplicate of root)
   - docs/CONTRIBUTING.md (duplicate)
   - docs/SETUP.md (duplicate) - keep one
   - docs/USAGE.md (duplicate) - keep one

---

## Phase 2: Android Resource Cleanup (Medium Risk)

### Unused/Duplicate Resources

Check these locations for unused files:

```
android/app/src/main/res/
├── drawable/          # Check for unused icons/backgrounds
├── drawable-v24/      # May have duplicates
├── layout/            # Check for unused layouts
├── mipmap-*/          # Icon folders - check for old icons
└── values/            # Check for unused colors/strings
```

### Action Items

1. **Run Android Lint:**
   ```bash
   cd android
   ./gradlew lint
   ```
   This will identify unused resources.

2. **Check for duplicate drawables:**
   - ic_launcher files (PNG vs XML - keep XML only)
   - Old button backgrounds
   - Unused icons

3. **Review layout files:**
   - activity_signature.xml vs layout-land/activity_signature.xml (both needed)
   - Check for any test/unused layouts

---

## Phase 3: Java Code Cleanup (Higher Risk)

### Potential Unused Files

Review these for removal:

```
android/app/src/main/java/com/mobileinvoice/ocr/
├── OCRProcessor.java          # Old Tesseract - may be unused
├── OCRProcessorHTTP.java      # Old HTTP version - may be unused
└── (check for any test files)
```

### Action Items

1. **Verify OCRProcessorMLKit is the only OCR used**
2. **Check for dead code paths**
3. **Remove commented-out code blocks**
4. **Run ProGuard analysis for unused code**

---

## Phase 4: Git Cleanup (Use Caution)

### Large Files in History

Check for large files that bloat the repo:

```bash
git rev-list --objects --all | \
  git cat-file --batch-check='%(objecttype) %(objectname) %(objectsize) %(rest)' | \
  sed -n 's/^blob //p' | \
  sort -rnk2 | \
  head -20
```

### Untracked Files

Current untracked files (from git status):
- .claude/ folders
- Various new drawable XMLs
- crash_log.txt, export_error.txt, nul

### Action Items

1. **Add to .gitignore:**
   ```
   crash_log.txt
   export_error.txt
   nul
   *.log
   ```

2. **Decide on .claude/ folders:**
   - Keep if using Claude Code features
   - Add to .gitignore if not needed in repo

3. **Clean up local files:**
   ```bash
   git clean -n  # Preview what would be deleted
   git clean -fd # Actually delete (CAREFUL!)
   ```

---

## Phase 5: Build Artifacts Cleanup

### Locations to Clean

```
android/build/              # Gradle build cache
android/app/build/          # App build outputs
android/.gradle/            # Gradle cache
```

### Action Items

```bash
cd android
./gradlew clean
```

This removes all build artifacts. Safe to run anytime.

---

## Execution Checklist

When ready to execute this plan:

- [ ] **BACKUP FIRST**: `git stash` or commit all changes
- [ ] **Phase 1**: Move docs to archive (low risk)
- [ ] **Phase 2**: Run lint, remove unused resources
- [ ] **Phase 3**: Review Java files carefully
- [ ] **Phase 4**: Update .gitignore, clean untracked
- [ ] **Phase 5**: Run `./gradlew clean`
- [ ] **TEST**: Build and install app, verify everything works
- [ ] **COMMIT**: Create a "cleanup" commit

---

## Estimated Time

| Phase | Time | Risk |
|-------|------|------|
| Phase 1: Docs | 30 min | Low |
| Phase 2: Resources | 20 min | Medium |
| Phase 3: Java | 30 min | Higher |
| Phase 4: Git | 15 min | Medium |
| Phase 5: Build | 5 min | None |
| **Total** | **~2 hours** | |

---

## Notes

- **Don't rush this** - the app works fine as-is
- **Archive, don't delete** - move files to archive/ folder first
- **Test after each phase** - build and install to verify
- **This can wait** - focus on features/bugs first

---

*This plan was created by Claude Code on February 3, 2026*
