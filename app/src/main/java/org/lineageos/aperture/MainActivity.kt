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
                    // Di sini nanti proses Watermark disisipkan ke Bitmap
                    // Karena ente minta standard, hasil save CameraX sudah cukup tajam
                }
                override fun onError(exc: ImageCaptureException) {}
            })
        
        // NO SOUND / NO VIBRATE: Sengaja tidak memanggil MediaActionSound
    }
}
