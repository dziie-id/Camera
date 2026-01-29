package org.lineageos.aperture

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
    private var isUltraWide = false
    private lateinit var btn07: TextView
    private lateinit var btn10: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root Layout
        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            setBackgroundColor(Color.BLACK)
        }

        // 1. PREVIEW 4:3 (Base UI)
        val screenWidth = resources.displayMetrics.widthPixels
        val previewHeight = (screenWidth * 4) / 3
        viewFinder = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(screenWidth, previewHeight).apply {
                gravity = Gravity.TOP
                topMargin = 100
            }
        }
        root.addView(viewFinder)

        // 2. FLOATING LENS SELECTOR (Urutan di bawah preview agar melayang)
        val lensPill = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            elevation = 20f // Biar melayang di atas segalanya
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#99000000"))
                cornerRadius = 100f
            }
            layoutParams = FrameLayout.LayoutParams(-2, -2).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                topMargin = previewHeight + 30 // Melayang tepat di bawah preview
            }
        }

        btn07 = createLensBtn("0.7", true)
        btn10 = createLensBtn("1.0", false)
        lensPill.addView(btn07)
        lensPill.addView(btn10)
        root.addView(lensPill)

        // 3. SHUTTER BUTTON (Pixel Style)
        val shutterRing = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(230, 230).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 100
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setStroke(10, Color.WHITE)
            }
        }
        val shutterCore = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(180, 180).apply { gravity = Gravity.CENTER }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
            }
            setOnClickListener { takePhoto() }
        }
        shutterRing.addView(shutterCore)
        root.addView(shutterRing)

        setContentView(root)
        updateLensColors()

        if (allPermissionsGranted()) startCamera()
        else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
    }

    private fun createLensBtn(txt: String, wide: Boolean): TextView {
        return TextView(this).apply {
            text = txt
            setTextColor(Color.WHITE)
            setPadding(45, 20, 45, 20)
            textSize = 14f
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
                .setResolutionSelector(resSelector)
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
                val bitmap = imageProxyToBitmap(image)
                image.close()
                
                // Tambahkan Watermark
                val watermarkedBitmap = addWatermark(bitmap)
                saveBitmapToGallery(watermarkedBitmap)
                
                runOnUiThread { Toast.makeText(baseContext, "Foto disimpan dengan Watermark!", Toast.LENGTH_SHORT).show() }
            }
        })
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun addWatermark(src: Bitmap): Bitmap {
        val result = src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = result.width / 40f
            isAntiAlias = true
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }
        val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        val text = "Captured on Android 16 Pro | $date"
        canvas.drawText(text, 50f, result.height - 50f, paint)
        return result
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val name = "IMG_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            val stream: OutputStream? = contentResolver.openOutputStream(it)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream!!)
            stream.close()
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}
