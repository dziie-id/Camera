package org.lineageos.aperture

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
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

        // UI Tanpa XML Layout (Full Code)
        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        viewFinder = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        root.addView(viewFinder)

        // Tombol Shutter (Tengah Bawah)
        val shutterBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_camera)
            setBackgroundColor(Color.parseColor("#80FFFFFF")) // Transparan putih
            layoutParams = FrameLayout.LayoutParams(200, 200).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 100
            }
            setOnClickListener { takePhoto() }
        }
        root.addView(shutterBtn)

        // Tombol Switch (Kiri Bawah)
        val switchBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_rotate)
            setBackgroundColor(Color.parseColor("#80FFFFFF"))
            layoutParams = FrameLayout.LayoutParams(150, 150).apply {
                gravity = Gravity.BOTTOM or Gravity.START
                leftMargin = 100
                bottomMargin = 125
            }
            setOnClickListener {
                isUltraWide = !isUltraWide
                startCamera()
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

            imageCapture = ImageCapture.Builder().build()

            // Filter Lensa: Mencoba cari lensa Ultra Wide kalau isUltraWide true
            val cameraSelector = if (isUltraWide) {
                CameraSelector.Builder().addCameraFilter { cameras ->
                    cameras.filter { cam -> 
                        val id = cam.cameraInfo.toString().lowercase()
                        id.contains("id: 2") || id.contains("back 2")
                    }
                }.build()
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                // Balik ke kamera Utama kalau UW gagal
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
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
                    Toast.makeText(baseContext, "Berhasil simpan!", Toast.LENGTH_SHORT).show()
                }
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(baseContext, "Gagal simpan", Toast.LENGTH_SHORT).show()
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
