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

        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
        }

        viewFinder = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        root.addView(viewFinder)

        val shutterBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_camera)
            setBackgroundColor(Color.parseColor("#4DFFFFFF"))
            layoutParams = FrameLayout.LayoutParams(180, 180).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 100
            }
            setOnClickListener { takePhoto() }
        }
        root.addView(shutterBtn)

        val switchBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_rotate)
            setBackgroundColor(Color.parseColor("#4DFFFFFF"))
            layoutParams = FrameLayout.LayoutParams(130, 130).apply {
                gravity = Gravity.BOTTOM or Gravity.START
                leftMargin = 80
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
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().build()

                // Logic filter sensor
                val cameraSelector = if (isUltraWide) {
                    CameraSelector.Builder().addCameraFilter { cameraInfos ->
                        cameraInfos.filter { info ->
                            val str = info.toString().lowercase()
                            str.contains("id: 2") || str.contains("back 2") || str.contains("id: 1")
                        }
                    }.build()
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (exc: Exception) {
                // Jika filter gagal, balik ke kamera utama tanpa crash
                val fallbackProvider = cameraProviderFuture.get()
                fallbackProvider.unbindAll()
                fallbackProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA)
                Toast.makeText(this, "Lensa tidak didukung", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(baseContext, "Berhasil!", Toast.LENGTH_SHORT).show()
                }
                override fun onError(exc: ImageCaptureException) {}
            }
        )
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    // Fix FC saat request permission pertama kali
    override fun onRequestPermissionsResult(rc: Int, p: Array<String>, g: IntArray) {
        super.onRequestPermissionsResult(rc, p, g)
        if (rc == 10 && allPermissionsGranted()) startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
