package org.lineageos.aperture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TANGKAP ERROR DAN SIMPAN KE HP
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val pref = getSharedPreferences("CRASH", Context.MODE_PRIVATE)
            pref.edit().putString("msg", sw.toString()).apply()
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        // CEK APAKAH ADA LOG CRASH SEBELUMNYA
        val pref = getSharedPreferences("CRASH", Context.MODE_PRIVATE)
        val errorLog = pref.getString("msg", null)
        if (errorLog != null) {
            showErrorDialog(errorLog)
            pref.edit().remove("msg").apply()
        }

        // SETUP UI DASAR
        val root = FrameLayout(this)
        viewFinder = PreviewView(this)
        root.addView(viewFinder)
        setContentView(root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun showErrorDialog(log: String) {
        val tv = TextView(this).apply {
            text = "LOG ERROR (SCREENSHOT INI BANG):\n\n$log"
            setTextColor(Color.RED)
            setPadding(40, 40, 40, 40)
            textSize = 12f
        }
        val scroll = ScrollView(this).apply { addView(tv) }
        
        AlertDialog.Builder(this)
            .setTitle("Waduh, Aplikasi Crash!")
            .setView(scroll)
            .setPositiveButton("Siap") { d, _ -> d.dismiss() }
            .show()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
                imageCapture = ImageCapture.Builder().build()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (e: Exception) {
                showErrorDialog(e.stackTraceToString())
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
