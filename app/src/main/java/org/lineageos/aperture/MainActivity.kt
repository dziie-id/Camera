package org.lineageos.aperture

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private var isUW = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Jalankan kamera default (1x)
        startCamera()

        // Tombol Shutter
        findViewById<ImageButton>(R.id.btnCapture).setOnClickListener {
            takePhoto()
        }

        // Tombol Ultra Wide (0.6x)
        findViewById<Button>(R.id.btnUW).setOnClickListener {
            if (!isUW) {
                isUW = true
                startCamera()
            }
        }

        // Tombol Main (1.0x)
        findViewById<Button>(R.id.btnMain).setOnClickListener {
            if (isUW) {
                isUW = false
                startCamera()
            }
        }
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

            // Filter Lensa: ID 0 untuk Main, ID 2 biasanya untuk UltraWide di Redmi Note 13
            val cameraSelector = CameraSelector.Builder()
                .addCameraFilter { cameras ->
                    val filtered = cameras.filter { cam ->
                        val id = cam.cameraInfo.toString()
                        if (isUW) id.contains("id=2") || id.contains("back 1")
                        else id.contains("id=0") || id.contains("back 0")
                    }
                    filtered.ifEmpty { cameras.filter { it.lensFacing == CameraSelector.LENS_FACING_BACK } }
                }.build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Simpan ke Cache agar tidak muncul di Gallery
        val photoFile = File(cacheDir, "direct_capture.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // 1. Proses Watermark
                    applyWatermark(photoFile)

                    // 2. Beri izin dan kirim URI ke aplikasi pemanggil (Ojol/WA)
                    val uri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "${packageName}.provider",
                        photoFile
                    )

                    val resultIntent = Intent()
                    resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    resultIntent.setData(uri) 
                    
                    // Support beberapa aplikasi yang minta via Extra Output
                    resultIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri)
                    
                    setResult(RESULT_OK, resultIntent)
                    
                    // 3. Tutup kamera, balik ke aplikasi asal
                    finish()
                }

                override fun onError(exc: ImageCaptureException) {
                    finish()
                }
            })
    }

    private fun applyWatermark(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        
        // Handle Rotasi Gambar
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val mutableBitmap = rotatedBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        // Format: Jam | Tanggal Bulan Tahun
        val sdf = SimpleDateFormat("HH:mm | dd MMMM yyyy", Locale.getDefault())
        val dateTime = sdf.format(Date())

        // Ukuran teks 2.5% dari tinggi foto
        val textSize = mutableBitmap.height * 0.025f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val margin = textSize
        val xPos = mutableBitmap.width - paint.measureText(dateTime) - margin
        val yPos = mutableBitmap.height - margin

        // Cek Luminance area watermark (Auto adjust warna teks)
        val pixel = mutableBitmap.getPixel(xPos.toInt().coerceIn(0, mutableBitmap.width-1), yPos.toInt().coerceIn(0, mutableBitmap.height-1))
        val luminance = (0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)) / 255

        // Jika area terang -> Teks Hitam. Jika area gelap -> Teks Putih.
        if (luminance > 0.5) {
            paint.color = Color.BLACK
            paint.setShadowLayer(2f, 1f, 1f, Color.WHITE)
        } else {
            paint.color = Color.WHITE
            paint.setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }

        canvas.drawText(dateTime, xPos, yPos, paint)

        // Simpan balik ke cache dengan kualitas 85 (Cukup tajam untuk struk)
        val out = FileOutputStream(file)
        mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        out.flush()
        out.close()
        
        // Cleanup memori
        bitmap.recycle()
        rotatedBitmap.recycle()
        mutableBitmap.recycle()
    }
}
