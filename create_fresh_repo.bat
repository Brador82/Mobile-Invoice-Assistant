@echo off
REM ============================================
REM Fresh Repository Setup Script
REM Mobile Invoice OCR Project
REM ============================================

echo.
echo ================================================
echo   Mobile Invoice OCR - Fresh Repository Setup
echo ================================================
echo.

REM Check if Git is installed
where git >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Git is not installed or not in PATH
    echo Please install Git from https://git-scm.com/
    pause
    exit /b 1
)

echo [1/6] Checking current directory...
cd /d "%~dp0"
echo Current directory: %CD%
echo.

echo [2/6] Cleaning build artifacts...
if exist "android\app\build" (
    echo Removing android\app\build...
    rmdir /s /q "android\app\build"
)
if exist "android\build" (
    echo Removing android\build...
    rmdir /s /q "android\build"
)
if exist "android\.gradle" (
    echo Removing android\.gradle...
    rmdir /s /q "android\.gradle"
)
echo Build artifacts cleaned.
echo.

echo [3/6] Removing temporary files...
if exist "android\device_log.txt" del /q "android\device_log.txt"
if exist "android\device_log_after_install.txt" del /q "android\device_log_after_install.txt"
if exist "SESSION_SUMMARY_*.md" del /q "SESSION_SUMMARY_*.md"
if exist "CLEANUP_SUMMARY.md" del /q "CLEANUP_SUMMARY.md"
for /r %%i in (desktop.ini) do @if exist "%%i" del /q "%%i"
echo Temporary files removed.
echo.

echo [4/6] Checking current Git status...
git status
echo.

echo ================================================
echo   NEXT STEPS - Please choose an option:
echo ================================================
echo.
echo OPTION 1: Create Fresh Repository (New Git History)
echo --------------------------------------------------
echo   1. Run: git remote -v
echo      (Note your current remote URL if you want to keep it)
echo.
echo   2. Run: rm -rf .git
echo      (Removes old Git history - CANNOT BE UNDONE)
echo.
echo   3. Run: git init
echo      (Creates new Git repository)
echo.
echo   4. Run: git add .
echo      (Stages all files)
echo.
echo   5. Run: git commit -m "Initial commit: Mobile Invoice OCR v2.0"
echo      (Creates first commit)
echo.
echo   6. Create new repository on GitHub:
echo      https://github.com/new
echo      Name: Mobile_Invoice_OCR
echo      Don't initialize with README
echo.
echo   7. Run: git remote add origin https://github.com/YOUR_USERNAME/Mobile_Invoice_OCR.git
echo      (Replace YOUR_USERNAME with your GitHub username)
echo.
echo   8. Run: git branch -M main
echo      (Rename branch to main)
echo.
echo   9. Run: git push -u origin main
echo      (Push to GitHub)
echo.
echo.
echo OPTION 2: Keep Current Repository (Clean Commit)
echo --------------------------------------------------
echo   1. Run: git add .
echo      (Stages all changes)
echo.
echo   2. Run: git commit -m "Complete Mobile Invoice OCR v2.0 with all features"
echo      (Commits all changes)
echo.
echo   3. Run: git push
echo      (Push to current repository)
echo.
echo.
echo OPTION 3: Create New Branch (Keep History)
echo --------------------------------------------------
echo   1. Run: git checkout -b v2.0-release
echo      (Creates and switches to new branch)
echo.
echo   2. Run: git add .
echo      (Stages all changes)
echo.
echo   3. Run: git commit -m "Release v2.0 with complete features"
echo      (Commits all changes)
echo.
echo   4. Run: git push -u origin v2.0-release
echo      (Push new branch to GitHub)
echo.
echo   5. Create Pull Request on GitHub to merge into main
echo.
echo.
echo ================================================
echo   FILES INCLUDED IN REPOSITORY:
echo ================================================
echo   - android/              (Complete Android project)
echo   - docs/                 (Documentation and guides)
echo   - README.md             (Main documentation)
echo   - CHANGELOG.md          (Version history)
echo   - FEATURES.md           (Feature list)
echo   - LICENSE               (MIT License)
echo   - .gitignore            (Updated ignore patterns)
echo.
echo   FILES EXCLUDED (via .gitignore):
echo   - Build artifacts (*.apk, build/, .gradle/)
echo   - Log files (*.log, device logs)
echo   - IDE files (.idea/, *.iml)
echo   - Workspace files (*.code-workspace)
echo   - Temporary files (desktop.ini, *.tmp)
echo.
echo ================================================
echo.

choice /c 123 /m "Which option do you want to use? (1/2/3)"
if errorlevel 3 goto option3
if errorlevel 2 goto option2
if errorlevel 1 goto option1

:option1
echo.
echo ================================================
echo   Preparing for OPTION 1 (Fresh Repository)
echo ================================================
echo.
echo IMPORTANT: This will DELETE your current Git history!
echo.
choice /m "Are you sure you want to continue?"
if errorlevel 2 goto end

echo.
echo Creating backup of .git folder...
if exist ".git" (
    xcopy /s /e /i /q ".git" ".git_backup" >nul
    echo Backup created: .git_backup
)
echo.

echo Removing old Git repository...
if exist ".git" rmdir /s /q ".git"
echo.

echo Initializing new Git repository...
git init
echo.

echo Adding all files...
git add .
echo.

echo Creating initial commit...
git commit -m "Initial commit: Mobile Invoice OCR v2.0

Features:
- ML Kit OCR for invoice scanning
- Route optimization with Google Maps integration
- Drag-and-drop invoice reordering  
- Export to CSV with sharing capabilities
- Room database for local storage
- Material Design UI"
echo.

echo.
echo ================================================
echo   SUCCESS! Fresh repository created.
echo ================================================
echo.
echo Next steps:
echo   1. Create new repository on GitHub: https://github.com/new
echo   2. Run: git remote add origin YOUR_GITHUB_URL
echo   3. Run: git branch -M main
echo   4. Run: git push -u origin main
echo.
goto end

:option2
echo.
echo ================================================
echo   Processing OPTION 2 (Clean Commit)
echo ================================================
echo.

echo Staging all changes...
git add .
echo.

echo Creating commit...
git commit -m "Complete Mobile Invoice OCR v2.0 with all features

Updates:
- Route optimization with Google Maps
- Drag-and-drop reordering
- Enhanced export capabilities
- Improved OCR accuracy
- Updated documentation"
echo.

echo.
echo Commit created! Review with: git log
echo.
choice /m "Do you want to push to remote now?"
if errorlevel 2 goto end

echo Pushing to remote...
git push
echo.

echo.
echo ================================================
echo   SUCCESS! Changes committed and pushed.
echo ================================================
echo.
goto end

:option3
echo.
echo ================================================
echo   Processing OPTION 3 (New Branch)
echo ================================================
echo.

set /p branchname="Enter branch name (e.g., v2.0-release): "
if "%branchname%"=="" set branchname=v2.0-release

echo Creating and switching to branch: %branchname%
git checkout -b %branchname%
echo.

echo Staging all changes...
git add .
echo.

echo Creating commit...
git commit -m "Release %branchname%: Complete Mobile Invoice OCR

Features:
- Route optimization with Google Maps integration
- Drag-and-drop invoice reordering
- Export to CSV/Excel/JSON/Markdown
- Enhanced OCR with ML Kit
- Comprehensive documentation"
echo.

echo Pushing branch to remote...
git push -u origin %branchname%
echo.

echo.
echo ================================================
echo   SUCCESS! New branch created and pushed.
echo ================================================
echo.
echo Next steps:
echo   1. Go to your GitHub repository
echo   2. Create a Pull Request from %branchname% to main
echo   3. Review and merge when ready
echo.
goto end

:end
echo.
echo ================================================
echo   Setup script completed!
echo ================================================
echo.
echo For more information, see: FRESH_REPO_SETUP.md
echo.
pause
