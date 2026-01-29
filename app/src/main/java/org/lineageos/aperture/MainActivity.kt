package org.lineageos.aperture

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
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
    private var isUltraWide = false
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI FULL SCREEN
        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            setBackgroundColor(Color.BLACK)
        }

        viewFinder = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(-1, -1)
        }
        root.addView(viewFinder)

        // TOMBOL SHUTTER (BULAT PUTIH)
        val shutterBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_camera)
            setBackgroundColor(Color.WHITE)
            // Bikin bulat manual pake padding
            setPadding(30, 30, 30, 30)
            layoutParams = FrameLayout.LayoutParams(200, 200).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 120
            }
            setOnClickListener { takePhoto() }
        }
        root.addView(shutterBtn)

        // TOMBOL SWITCH ULTRA-WIDE (KIRI)
        val switchBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_rotate)
            setBackgroundColor(Color.parseColor("#4D000000"))
            layoutParams = FrameLayout.LayoutParams(130, 130).apply {
                gravity = Gravity.BOTTOM or Gravity.START
                leftMargin = 100
                bottomMargin = 155
            }
            setOnClickListener {
                isUltraWide = !isUltraWide
                startCamera()
                Toast.makeText(context, if(isUltraWide) "Ultra Wide" else "Main Lens", Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(switchBtn)

        setContentView(root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // Logic Filter Lensa buat Redmi Note 13 (A16 Base)
            val cameraSelector = if (isUltraWide) {
                CameraSelector.Builder().addCameraFilter { cameraInfos ->
                    cameraInfos.filter { info ->
                        val s = info.toString().lowercase()
                        s.contains("id: 2") || s.contains("back 2") || s.contains("id: 1")
                    }
                }.build()
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
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
                    Toast.makeText(baseContext, "Cekrak! Foto Disimpan", Toast.LENGTH_SHORT).show()
                }
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(baseContext, "Gagal simpan: ${exc.message}", Toast.LENGTH_SHORT).show()
                }
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
