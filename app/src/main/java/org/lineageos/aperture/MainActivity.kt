package org.lineageos.aperture

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var isUltraWide = false
    private lateinit var btn07: TextView
    private lateinit var btn10: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            setBackgroundColor(Color.BLACK)
        }

        // 1. PREVIEW 4:3 (Diturunin dikit biar nggak mepet status bar)
        val screenWidth = resources.displayMetrics.widthPixels
        val previewHeight = (screenWidth * 4) / 3
        
        viewFinder = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(screenWidth, previewHeight).apply {
                gravity = Gravity.TOP
                topMargin = dpToPx(50) 
            }
        }
        root.addView(viewFinder)

        // 2. SHUTTER AREA (Dibikin pendek, tingginya cuma 180dp)
        val bottomBarHeight = dpToPx(180)
        val bottomArea = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(-1, bottomBarHeight).apply {
                gravity = Gravity.BOTTOM
            }
            setBackgroundColor(Color.BLACK)
        }
        
        val shutterRing = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setStroke(dpToPx(5), Color.WHITE)
            }
            layoutParams = FrameLayout.LayoutParams(dpToPx(85), dpToPx(85)).apply {
                gravity = Gravity.CENTER
            }
        }
        val shutterCore = View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
            }
            layoutParams = FrameLayout.LayoutParams(dpToPx(70), dpToPx(70)).apply { gravity = Gravity.CENTER }
            setOnClickListener { takePhoto() }
        }
        shutterRing.addView(shutterCore)
        bottomArea.addView(shutterRing)
        root.addView(bottomArea)

        // 3. AUX LENS SELECTOR (Melayang di atas area Shutter)
        val lensPill = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(8, 5, 8, 5)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#80000000"))
                cornerRadius = 100f
            }
            layoutParams = FrameLayout.LayoutParams(-2, -2).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = bottomBarHeight + dpToPx(20) // Melayang 20dp di atas bar hitam
            }
        }

        btn07 = createLensBtn("0.7", true)
        btn10 = createLensBtn("1.0", false)
        lensPill.addView(btn07)
        lensPill.addView(btn10)
        root.addView(lensPill)

        // 4. FLASH BUTTON (Tetap di atas kanan)
        val flashBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_compass)
            setColorFilter(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = FrameLayout.LayoutParams(120, 120).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = dpToPx(60)
                rightMargin = 40
            }
            setOnClickListener { toggleFlash(this) }
        }
        root.addView(flashBtn)

        setContentView(root)
        updateLensColors()

        if (allPermissionsGranted()) startCamera()
        else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
    }

    private fun dpToPx(dp: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()

    private fun createLensBtn(txt: String, wide: Boolean): TextView {
        return TextView(this).apply {
            text = txt
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(dpToPx(18), dpToPx(10), dpToPx(18), dpToPx(10))
            setOnClickListener {
                if (isUltraWide != wide) {
                    isUltraWide = wide
                    updateLensColors()
                    startCamera()
                }
            }
        }
    }

    private fun updateLensColors() {
        val activeColor = Color.parseColor("#FF00D1D1")
        btn07.setTextColor(if (isUltraWide) activeColor else Color.WHITE)
        btn10.setTextColor(if (!isUltraWide) activeColor else Color.WHITE)
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
                .build()

            val cameraSelector = if (isUltraWide) {
                CameraSelector.Builder().addCameraFilter { it.filter { i -> !i.toString().contains("id: 0") } }.build()
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        imageCapture.takePicture(Executors.newSingleThreadExecutor(), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                
                // Fix rotasi
                val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                image.close()

                // Simpan
                val name = "A16_${System.currentTimeMillis()}.jpg"
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
                }
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    val stream: OutputStream? = contentResolver.openOutputStream(it)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 98, stream!!)
                    stream.close()
                }
                runOnUiThread { Toast.makeText(baseContext, "Cekrak!", Toast.LENGTH_SHORT).show() }
            }
        })
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}
