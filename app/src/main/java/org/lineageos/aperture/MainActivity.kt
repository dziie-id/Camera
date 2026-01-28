import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import java.io.FileOutputStream

package org.lineageos.aperture

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private var currentLens = CameraSelector.LENS_FACING_BACK
    private var isUW = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startCamera()

        findViewById<ImageButton>(R.id.btnCapture).setOnClickListener { takePhoto() }
        findViewById<Button>(R.id.btnUW).setOnClickListener { switchLens(true) }
        findViewById<Button>(R.id.btnMain).setOnClickListener { switchLens(false) }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // Logic filter lensa manual untuk Ultra Wide
            val cameraSelector = CameraSelector.Builder()
                .addCameraFilter { cameras ->
                    val filtered = cameras.filter { cam ->
                        val id = cam.cameraInfo.toString() // CameraX info string
                        if (isUW) id.contains("id=2") || id.contains("back 1") 
                        else id.contains("id=0") || id.contains("back 0")
                    }
                    if (filtered.isEmpty()) cameras.filter { it.lensFacing == CameraSelector.LENS_FACING_BACK }
                    else filtered
                }.build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch(e: Exception) {}
        }, ContextCompat.getMainExecutor(this))
    }

    private fun switchLens(uw: Boolean) {
        isUW = uw
        startCamera()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        
        // Setup Output (File name & Location)
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val photoFile = File(externalMediaDirs.firstOrNull(), "$name.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
    val savedUri = Uri.fromFile(photoFile)
    applyWatermark(photoFile)
}

private fun applyWatermark(file: File) {
    // 1. Decode bitmap dari hasil foto
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)

    // 2. Siapkan Data Teks (Jam | Tanggal Bulan Tahun)
    val sdf = SimpleDateFormat("HH:mm | dd MMMM yyyy", Locale.getDefault())
    val dateTime = sdf.format(Date())

    // 3. Konfigurasi Ukuran Proposional (Misal 2% dari tinggi gambar)
    val textSize = mutableBitmap.height * 0.025f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = textSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        style = Paint.Style.FILL
    }

    // 4. Hitung Posisi (Pojok Kanan Bawah)
    val margin = textSize 
    val xPos = mutableBitmap.width - paint.measureText(dateTime) - margin
    val yPos = mutableBitmap.height - margin

    // 5. AUTO ADJUST WARNA (Luminance Analysis)
    // Kita cek sample warna di area watermark akan diletakkan
    val sampleSize = 20
    val checkX = xPos.toInt().coerceIn(0, mutableBitmap.width - sampleSize)
    val checkY = yPos.toInt().coerceIn(0, mutableBitmap.height - sampleSize)
    
    val pixel = mutableBitmap.getPixel(checkX, checkY)
    val r = Color.red(pixel)
    val g = Color.green(pixel)
    val b = Color.blue(pixel)
    
    // Rumus Luminance: 0.299R + 0.587G + 0.114B
    val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
    
    if (luminance > 0.5) {
        paint.color = Color.BLACK // Gambar terang, teks hitam
        paint.setShadowLayer(2f, 1f, 1f, Color.WHITE) // Tipis aja biar kebaca
    } else {
        paint.color = Color.WHITE // Gambar gelap, teks putih
        paint.setShadowLayer(2f, 1f, 1f, Color.BLACK)
    }

    // 6. Gambar Teks ke Canvas
    canvas.drawText(dateTime, xPos, yPos, paint)

    // 7. Simpan Kembali ke File
    try {
        val out = FileOutputStream(file)
        mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()
        bitmap.recycle()
        mutableBitmap.recycle()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

                override fun onError(exc: ImageCaptureException) {}
            })
        
        // NO SOUND / NO VIBRATE: Sengaja tidak memanggil MediaActionSound
    }
}
