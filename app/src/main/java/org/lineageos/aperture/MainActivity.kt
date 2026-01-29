package org.lineageos.aperture

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Buat UI via Code (Gak perlu XML layout biar gak error resource)
        val root = androidx.constraintlayout.widget.ConstraintLayout(this)
        viewFinder = PreviewView(this).apply { id = View.generateViewId() }
        root.addView(viewFinder)

        val shutterBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_camera)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { takePhoto() }
        }
        root.addView(shutterBtn)

        val switchBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_rotate)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { switchCamera() }
        }
        root.addView(switchBtn)

        // Sederhanain layouting (Positioning tombol)
        setContentView(root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 10)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            // Logic paksa Ultra Wide (Biasanya ID 2)
            CameraSelector.Builder().addCameraFilter { cameras ->
                cameras.filter { it.cameraInfo.toString().contains("back 2") || it.cameraInfo.toString().contains("id: 2") }
            }.build()
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
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

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                // Fallback kalau ID 2 gak ketemu
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
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Aperture")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()

        // Capture tanpa suara (silent) karena kita gak panggil MediaActionSound
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(baseContext, "Foto disimpan!", Toast.LENGTH_SHORT).show()
                }
                override fun onError(exc: ImageCaptureException) {}
            }
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
