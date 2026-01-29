package org.lineageos.aperture

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var isUltraWide = false
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Layout Utama
        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            setBackgroundColor(Color.BLACK)
        }

        // Preview Area (Dibuat 4:3 biar kayak GCam Go, tidak full screen)
        val previewContainer = FrameLayout(this).apply {
            val width = resources.displayMetrics.widthPixels
            val height = (width * 4) / 3 
            layoutParams = FrameLayout.LayoutParams(width, height).apply {
                topMargin = 150 // Kasih space di atas buat tombol flash/settings
            }
        }
        viewFinder = PreviewView(this).apply { layoutParams = ViewGroup.LayoutParams(-1, -1) }
        previewContainer.addView(viewFinder)
        root.addView(previewContainer)

        // Tombol Flash (Pojok Kanan Atas)
        val flashBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.btn_star_big_off) // Pake icon default dulu
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = FrameLayout.LayoutParams(150, 150).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = 30
                rightMargin = 30
            }
            setOnClickListener { toggleFlash(this) }
        }
        root.addView(flashBtn)

        // Floating Lens Selector (Di atas tombol shutter)
        val lensLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(-2, -2).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 380 // Melayang di atas shutter
            }
        }

        val btnUW = createLensButton("0.7x") { 
            isUltraWide = true
            startCamera() 
        }
        val btnMain = createLensButton("1.0x") { 
            isUltraWide = false
            startCamera() 
        }
        
        lensLayout.addView(btnUW)
        lensLayout.addView(btnMain)
        root.addView(lensLayout)

        // Tombol Shutter (Gaya GCam)
        val shutterContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(220, 220).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 80
            }
            // Ring luar
            setBackgroundResource(android.R.drawable.presence_online) // Dummy ring
        }
        val shutterBtn = View(this).apply {
            setBackgroundColor(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(160, 160).apply { gravity = Gravity.CENTER }
            setOnClickListener { takePhoto() }
        }
        shutterContainer.addView(shutterBtn)
        root.addView(shutterContainer)

        setContentView(root)

        if (allPermissionsGranted()) startCamera()
        else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun createLensButton(label: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            setTextColor(Color.WHITE)
            setPadding(30, 15, 30, 15)
            setBackgroundColor(Color.parseColor("#80000000"))
            setOnClickListener { action() }
            val params = LinearLayout.LayoutParams(-2, -2)
            params.setMargins(10, 0, 10, 0)
            layoutParams = params
        }
    }

    private fun toggleFlash(btn: ImageButton) {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        val msg = when(flashMode) {
            ImageCapture.FLASH_MODE_ON -> "Flash ON"
            ImageCapture.FLASH_MODE_AUTO -> "Flash Auto"
            else -> "Flash OFF"
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        startCamera() // Refresh camera dengan mode flash baru
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3) // Paksa 4:3
                .build().also { it.setSurfaceProvider(viewFinder.surfaceProvider) }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // Biar dapet Megapixel tinggi
                .setFlashMode(flashMode)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()

            val cameraSelector = if (isUltraWide) {
                CameraSelector.Builder().addCameraFilter { it.filter { i -> i.toString().contains("id: 2") || i.toString().contains("id: 1") } }.build()
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_$name")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(baseContext, "Tersimpan ke Galeri", Toast.LENGTH_SHORT).show()
                }
                override fun onError(exc: ImageCaptureException) {}
            }
        )
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
