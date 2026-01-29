# Contributing to Mobile Invoice OCR

Thank you for considering contributing to Mobile Invoice OCR! This document provides guidelines for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Submitting Changes](#submitting-changes)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors, regardless of experience level, gender, gender identity, sexual orientation, disability, appearance, body size, race, ethnicity, age, religion, or nationality.

### Our Standards

**Positive behaviors:**
- Using welcoming and inclusive language
- Being respectful of differing viewpoints
- Gracefully accepting constructive criticism
- Focusing on what is best for the community
- Showing empathy towards others

**Unacceptable behaviors:**
- Harassment, trolling, or insulting/derogatory comments
- Public or private harassment
- Publishing others' private information
- Other conduct inappropriate in a professional setting

## Getting Started

### Prerequisites

Before contributing, ensure you have:
- Android Studio Hedgehog (2023.1.1) or later
- JDK 8 or higher
- Git installed
- GitHub account
- Basic knowledge of Java/Android development

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
```bash
git clone https://github.com/YOUR_USERNAME/Mobile_Invoice_OCR.git
cd Mobile_Invoice_OCR
```

3. Add upstream remote:
```bash
git remote add upstream https://github.com/Brador82/Mobile_Invoice_OCR.git
```

4. Create a feature branch:
```bash
git checkout -b feature/your-feature-name
```

## Development Setup

### Environment Configuration

1. **Open project in Android Studio**
   - File → Open → Select `android` folder

2. **Sync Gradle**
   - Android Studio will auto-sync dependencies

3. **Run app**
   - Connect device or start emulator
   - Click Run button

### Project Structure

```
android/
├── app/src/main/java/com/mobileinvoice/ocr/
│   ├── MainActivity.java              # Main screen
│   ├── OCRProcessorMLKit.java         # OCR engine
│   ├── InvoiceDetailActivity.java     # Detail view
│   ├── CameraActivity.java            # Camera capture
│   ├── InvoiceAdapter.java            # RecyclerView adapter
│   └── database/
│       ├── Invoice.java               # Entity
│       ├── InvoiceDao.java            # DAO
│       └── InvoiceDatabase.java       # Database singleton
├── app/src/main/res/                  # Layouts, strings, etc.
└── app/src/test/                      # Unit tests
```

## Coding Standards

### Java Style Guide

Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

**Key points:**
- Indentation: 4 spaces (no tabs)
- Line length: 100 characters max
- Braces: Required for all control structures
- Naming:
  - Classes: `PascalCase`
  - Methods: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Private fields: prefix with `m` (e.g., `mDatabase`)

### Code Formatting

Use Android Studio's built-in formatter:
- `Ctrl+Alt+L` (Windows/Linux)
- `Cmd+Option+L` (Mac)

### Comments and Documentation

```java
/**
 * Process invoice image and extract customer data.
 * 
 * Uses Google ML Kit for on-device text recognition, then applies
 * intelligent parsing to identify customer name, address, and phone.
 *
 * @param imageUri URI of the invoice image to process
 * @return OCRResult containing extracted fields
 * @throws IOException if image cannot be loaded
 */
public OCRResult processImage(Uri imageUri) throws IOException {
    // Implementation...
}
```

### Logging

Use Android Log with consistent tags:

```java
private static final String TAG = "OCRProcessorMLKit";

Log.d(TAG, "Processing image: " + uri);  // Debug
Log.i(TAG, "OCR complete");              // Info
Log.w(TAG, "No 'BILL TO:' found");      // Warning
Log.e(TAG, "Failed to load image", e);   // Error
```

### Error Handling

```java
// Good
try {
    Bitmap bitmap = loadBitmap(uri);
    if (bitmap == null) {
        Log.e(TAG, "Bitmap is null");
        return createErrorResult("Failed to load image");
    }
    // Process bitmap...
} catch (IOException e) {
    Log.e(TAG, "IO error loading image", e);
    return createErrorResult("Error: " + e.getMessage());
}

// Bad
try {
    Bitmap bitmap = loadBitmap(uri);
    // Process without null check
} catch (Exception e) {
    // Empty catch block
}
```

## Submitting Changes

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding/updating tests
- `chore`: Maintenance tasks

**Examples:**
```
feat(ocr): add support for Spanish invoices

Implement language detection and Spanish text patterns for
invoice field extraction.

Closes #42
```

```
fix(database): prevent duplicate invoice entries

Add unique constraint on invoice_number column and handle
SQLiteConstraintException gracefully.

Fixes #38
```

### Pull Request Process

1. **Update your branch**
   ```bash
   git fetch upstream
   git rebase upstream/OCR-adjust/develope
   ```

2. **Run tests**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

3. **Push changes**
   ```bash
   git push origin feature/your-feature-name
   ```

4. **Create Pull Request**
   - Go to GitHub
   - Click "New Pull Request"
   - Select your feature branch
   - Fill out PR template

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Manual testing completed
- [ ] Screenshots attached (if UI change)

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] No new warnings generated

## Related Issues
Closes #XX
```

### Review Process

- PRs require at least 1 approval
- Address review comments promptly
- Keep PR scope focused (one feature/fix per PR)
- Squash commits before merge (if requested)

## Testing Guidelines

### Unit Tests

Location: `app/src/test/java/`

```java
@Test
public void testExtractPhoneNumber() {
    String text = "Phone: (417) 555-1234";
    String phone = extractPhone(text);
    assertEquals("(417) 555-1234", phone);
}
```

### Instrumented Tests

Location: `app/src/androidTest/java/`

```java
@RunWith(AndroidJUnit4.class)
public class OCRTest {
    @Test
    public void testInvoiceExtraction() {
        // Load test invoice image
        // Process with OCR
        // Assert expected fields
    }
}
```

### Manual Testing Checklist

Before submitting PR:
- [ ] App builds without errors
- [ ] OCR extraction works on test invoices
- [ ] Database operations complete successfully
- [ ] No crashes on device
- [ ] UI responsive on different screen sizes
- [ ] Works on Android API 26+

## Documentation

### Code Documentation

- Document all public methods
- Explain non-obvious logic
- Add TODO comments for future work
- Update README.md for new features

### User Documentation

Update these files as needed:
- `README.md` - Overview and quick start
- `docs/USAGE.md` - User guide
- `docs/SETUP.md` - Installation instructions
- `docs/TECHNICAL.md` - Architecture details
- `docs/API.md` - API reference
- `CHANGELOG.md` - Version history

## Common Contribution Scenarios

### Adding Support for New Invoice Format

1. Analyze invoice structure
2. Modify `OCRProcessorMLKit.extractInvoiceData()`
3. Add test cases with sample invoices
4. Update documentation with format details
5. Submit PR with before/after examples

### Fixing OCR Accuracy Issue

1. Collect problematic invoice samples
2. Add debug logging to identify issue
3. Adjust extraction patterns/logic
4. Test with original samples
5. Document fix in CHANGELOG.md

### Adding New Feature

1. Open issue to discuss feature
2. Get approval from maintainers
3. Implement feature on feature branch
4. Add tests
5. Update documentation
6. Submit PR

## Getting Help

- **Questions**: Open a GitHub Discussion
- **Bugs**: Create an issue with `bug` label
- **Features**: Create an issue with `enhancement` label
- **Security**: Email security@github.com (do not open public issue)

## Recognition

Contributors will be recognized in:
- CHANGELOG.md (for significant contributions)
- GitHub Contributors page
- Release notes

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.

---

Thank you for contributing to Mobile Invoice OCR! 🎉
