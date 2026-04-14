package com.scanner.pdf

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    
    private val CAMERA_REQUEST = 100
    private val STORAGE_REQUEST = 101
    
    private lateinit var imageView: ImageView
    private lateinit var btnScan: Button
    private lateinit var btnGallery: Button
    private lateinit var btnSavePdf: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    
    private var scannedBitmap: Bitmap? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        checkPermissions()
    }
    
    private fun initViews() {
        imageView = findViewById(R.id.imageView)
        btnScan = findViewById(R.id.btnScan)
        btnGallery = findViewById(R.id.btnGallery)
        btnSavePdf = findViewById(R.id.btnSavePdf)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        
        btnScan.setOnClickListener { startDocumentScan() }
        btnGallery.setOnClickListener { openGallery() }
        btnSavePdf.setOnClickListener { saveAsPdf() }
        
        btnSavePdf.isEnabled = false
    }
    
    private fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST)
        }
    }
    
    private fun setupScanner() {
        val options = DocumentScannerOptions.Builder()
            .setScannerMode(DocumentScannerOptions.SCANNER_MODE_FULL)
            .setResultFormat(DocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setPageLimit(1)
            .build()
        
        val scanner = GmsDocumentScanning.getClient(options)
        
        btnScan.setOnClickListener {
            scanner.getStartScanIntent(this)
                .addOnSuccessListener { intent ->
                    startActivityForResult(intent, CAMERA_REQUEST)
                }
                .addOnFailureListener { e ->
                    showStatus("Scanner error: " + e.message)
                }
        }
    }
    
    private fun startDocumentScan() {
        val options = DocumentScannerOptions.Builder()
            .setScannerMode(DocumentScannerOptions.SCANNER_MODE_FULL)
            .setResultFormat(DocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setPageLimit(1)
            .build()
        
        val scanner = GmsDocumentScanning.getClient(options)
        
        scanner.getStartScanIntent(this)
            .addOnSuccessListener { intent ->
                startActivityForResult(intent, CAMERA_REQUEST)
            }
            .addOnFailureListener { e ->
                showStatus("Scanner error: " + e.message)
            }
    }
    
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 200)
    }
    
    private fun saveAsPdf() {
        val bitmap = scannedBitmap ?: return
        
        progressBar.visibility = View.VISIBLE
        btnSavePdf.isEnabled = false
        
        Thread {
            try {
                val pdfDir = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Scans")
                if (!pdfDir.exists()) pdfDir.mkdirs()
                
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val pdfFile = File(pdfDir, "scan_$timestamp.pdf")
                
                val stream = FileOutputStream(pdfFile)
                
                val pdfWriter = PdfWriter(stream)
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)
                
                // Save bitmap to temp file
                val tempFile = File(cacheDir, "temp_scan.jpg")
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(tempFile))
                
                val imageData = ImageDataFactory.create(tempFile.absolutePath)
                val pdfImage = Image(imageData)
                
                // Scale to fit page
                val pageWidth = PageSize.A4.width - 40
                val pageHeight = PageSize.A4.height - 40
                val scale = minOf(pageWidth / pdfImage.imageWidth, pageHeight / pdfImage.imageHeight)
                pdfImage.scale(scale, scale)
                
                document.add(pdfImage)
                document.close()
                
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    showStatus("PDF saved: ${pdfFile.name}")
                    btnSavePdf.isEnabled = true
                    
                    // Open PDF
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.fromFile(pdfFile), "application/pdf")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    showStatus("Error: " + e.message)
                    btnSavePdf.isEnabled = true
                }
            }
        }.start()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == CAMERA_REQUEST) {
            val result = GmsDocumentScanningResult.fromActivityResultIntent(data)
            result?.let { scanningResult ->
                val pages = scanningResult.pages
                if (pages.isNotEmpty()) {
                    val page = pages[0]
                    val imageUri = page.imageUri
                    scannedImageUri = imageUri
                    
                    contentResolver.openInputStream(imageUri)?.use { stream ->
                        scannedBitmap = BitmapFactory.decodeStream(stream)
                        runOnUiThread {
                            imageView.setImageBitmap(scannedBitmap)
                            btnSavePdf.isEnabled = true
                            showStatus("Document scanned! Tap 'Save as PDF' to export")
                        }
                    }
                }
            }
        } else if (requestCode == 200 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                scannedImageUri = uri
                contentResolver.openInputStream(uri)?.use { stream ->
                    scannedBitmap = BitmapFactory.decodeStream(stream)
                    runOnUiThread {
                        imageView.setImageBitmap(scannedBitmap)
                        btnSavePdf.isEnabled = true
                        showStatus("Image loaded! Tap 'Save as PDF' to export")
                    }
                }
            }
        }
    }
    
    private fun showStatus(message: String) {
        statusText.text = message
        statusText.visibility = View.VISIBLE
    }
}
