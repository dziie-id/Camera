package org.lineageos.aperture

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.AspectRatioStrategy
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
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var isUltraWide = false
    private lateinit var cameraExecutor: ExecutorService
    
    private lateinit var btn07: TextView
    private lateinit var btn10: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            setBackgroundColor(Color.BLACK)
        }

        // 1. Preview Area (Dasar)
        val screenWidth = resources.displayMetrics.widthPixels
        val previewHeight = (screenWidth * 4) / 3
        
        viewFinder = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(screenWidth, previewHeight).apply {
                gravity = Gravity.TOP
                topMargin = 150 
            }
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
        root.addView(viewFinder)

        // 2. Shutter Button (Di bawah Preview)
        val shutterContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(220, 220).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 150
            }
        }
        val ring = View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setStroke(8, Color.WHITE)
            }
            layoutParams = FrameLayout.LayoutParams(-1, -1)
        }
        val core = View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
            }
            layoutParams = FrameLayout.LayoutParams(170, 170).apply { gravity = Gravity.CENTER }
            setOnClickListener { takePhoto() }
        }
        shutterContainer.addView(ring)
        shutterContainer.addView(core)
        root.addView(shutterContainer)

        // 3. Floating Lens Selector (HARUS DI TULIS TERAKHIR BIAR DI ATAS PREVIEW)
        val lensPill = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(10, 8, 10, 8)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#99000000"))
                cornerRadius = 100f
            }
            layoutParams = FrameLayout.LayoutParams(-2, -2).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                topMargin = previewHeight - 50 // Pas melayang di batas bawah preview
            }
        }

        btn07 = createLensBtn("0.7", true)
        btn10 = createLensBtn("1.0", false)
        lensPill.addView(btn07)
        lensPill.addView(btn10)
        root.addView(lensPill)

        // 4. Flash Button (Atas)
        val flashBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_compass)
            setColorFilter(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = FrameLayout.LayoutParams(120, 120).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = 30
                rightMargin = 40
            }
            setOnClickListener { toggleFlash(this) }
        }
        root.addView(flashBtn)

        setContentView(root)
        updateLensColors()

        if (allPermissionsGranted()) startCamera()
        else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun createLensBtn(txt: String, isWide: Boolean): TextView {
        return TextView(this).apply {
            text = txt
            textSize = 12f
            setPadding(35, 15, 35, 15)
            gravity = Gravity.CENTER
            setOnClickListener {
                if (isUltraWide != isWide) {
                    isUltraWide = isWide
                    updateLensColors()
                    startCamera()
                }
            }
        }
    }

    private fun updateLensColors() {
        val activeBg = GradientDrawable().apply {
            setColor(Color.parseColor("#FF00D1D1"))
            cornerRadius = 100f
        }
        if (isUltraWide) {
            btn07.background = activeBg
            btn07.setTextColor(Color.BLACK)
            btn10.background = null
            btn10.setTextColor(Color.WHITE)
        } else {
            btn10.background = activeBg
            btn10.setTextColor(Color.BLACK)
            btn07.background = null
            btn07.setTextColor(Color.WHITE)
        }
    }

    private fun toggleFlash(btn: ImageButton) {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        btn.setColorFilter(if (flashMode == ImageCapture.FLASH_MODE_OFF) Color.WHITE else Color.YELLOW)
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val resSelector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build()

            val preview = Preview.Builder().setResolutionSelector(resSelector).build()
                .also { it.setSurfaceProvider(viewFinder.surfaceProvider) }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(flashMode)
                .setResolutionSelector(resSelector)
                .build()

            // Logic deteksi lensa Aux khusus Redmi (Lensa ID 2 atau 3)
            val cameraSelector = if (isUltraWide) {
                CameraSelector.Builder().addCameraFilter { cameraInfos ->
                    val filtered = cameraInfos.filter { 
                        val s = it.toString().lowercase()
                        s.contains("id: 2") || s.contains("back 1") || s.contains("id: 1")
                    }
                    if (filtered.isNotEmpty()) filtered else cameraInfos.filter { !it.toString().contains("id: 0") }
                }.build()
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                Toast.makeText(this, "Lensa tidak didukung ROM ini", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val name = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
            }
        }

        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build(),
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(out: ImageCapture.OutputFileResults) {
                    Toast.makeText(baseContext, "Cekrak!", Toast.LENGTH_SHORT).show()
                }
                override fun onError(e: ImageCaptureException) {}
            }
        )
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
