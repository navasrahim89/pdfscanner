@echo off
cd /d D:\Smart Detector\scannerapp

echo Initializing git...
git init
git config user.email "navasrahim89@gmail.com"
git config user.name "navasrahim89"
git add .
git commit -m "PDF Scanner with ML Kit edge detection"

echo Creating GitHub repo...
git branch -M master
git remote add origin https://github.com/navasrahim89/pdfscanner.git

echo Pushing to GitHub...
git push -u origin master

echo.
echo ========================================
echo DONE! Now go to GitHub Actions to build:
echo https://github.com/navasrahim89/pdfscanner/actions
echo ========================================
pause
