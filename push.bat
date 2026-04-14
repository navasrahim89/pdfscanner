@echo off
cd /d D:\Smart Detector\scannerapp
git init
git config user.email "navasrahim89@gmail.com"
git config user.name "navasrahim89"
git add .
git commit -m "PDF Scanner app with ML Kit edge detection"
git branch -M master
git remote add origin https://github.com/navasrahim89/pdfscanner.git
git push -u origin master
pause
