package org.lineageos.aperture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TANGKAP ERROR (LOGCAT MANUAL)
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val pref = getSharedPreferences("DEBUG_LOG", Context.MODE_PRIVATE)
            pref.edit().putString("last_crash", sw.toString()).apply()
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        // TAMPILKAN DIALOG JIKA ADA CRASH
        val pref = getSharedPreferences("DEBUG_LOG", Context.MODE_PRIVATE)
        val logs = pref.getString("last_crash", null)
        if (logs != null) {
            val tv = TextView(this).apply {
                text = logs
                setTextColor(Color.RED)
                setPadding(40, 40, 40, 40)
                textSize = 10f
            }
            val scroll = ScrollView(this).apply { addView(tv) }
            
            AlertDialog.Builder(this)
                .setTitle("Screenshot Error Ini Bang!")
                .setView(scroll)
                .setPositiveButton("Hapus & Lanjut") { _, _ ->
                    pref.edit().remove("last_crash").apply()
                }
                .show()
        }

        // UI DASAR
        val root = FrameLayout(this)
        viewFinder = PreviewView(this)
        root.addView(viewFinder)
        
        val statusText = TextView(this).apply {
            text = "Kamera Siap..."
            setTextColor(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(-2, -2).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL }
        }
        root.addView(statusText)

        setContentView(root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview)
            } catch (e: Exception) {
                // Jangan crash, tampilin error di layar aja
                Toast(e.stackTraceToString())
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun Toast(msg: String) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_LONG).show()
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}
