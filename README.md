# PDF Scanner App - Build Instructions

## Project Location
`D:\Smart Detector\scannerapp`

## Files Created
- build.gradle, settings.gradle, gradle.properties, local.properties
- app/build.gradle
- AndroidManifest.xml
- MainActivity.kt (with ML Kit scanner)
- activity_main.xml (UI layout)
- colors.xml

## Features
- 📷 Camera scan with auto edge detection (ML Kit)
- 🖼️ Gallery import
- 📄 Save as PDF (iText)
- Auto document boundary detection

## To Build

### Option 1: Android Studio
1. Open Android Studio
2. File → Open → `D:\Smart Detector\scannerapp`
3. Build → Build APK

### Option 2: Command Line
```cmd
cd D:\Smart Detector\scannerapp
gradlew.bat assembleDebug
```

APK will be at: `app\build\outputs\apk\debug\app-debug.apk`

## Required SDK
- Android SDK at: `C:\Users\AGPP Digital\AppData\Local\Android\Sdk`
- Compile SDK: 34
- Min SDK: 21
