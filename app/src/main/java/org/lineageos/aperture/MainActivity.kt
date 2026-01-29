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

        // --- CRASH CATCHER LOGIC ---
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            getSharedPreferences("DEBUG", Context.MODE_PRIVATE).edit()
                .putString("last_error", sw.toString()).apply()
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        // Tampilkan error jika ada crash sebelumnya
        val lastError = getSharedPreferences("DEBUG", Context.MODE_PRIVATE).getString("last_error", null)
        if (lastError != null) {
            showErrorDialog(lastError)
            getSharedPreferences("DEBUG", Context.MODE_PRIVATE).edit().remove("last_error").apply()
        }

        // --- UI SETUP ---
        val root = FrameLayout(this)
        viewFinder = PreviewView(this)
        root.addView(viewFinder)

        val infoTxt = TextView(this).apply {
            text = "Kamera A16 Pro"
            setTextColor(Color.WHITE)
            setPadding(20, 100, 20, 20)
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }
        root.addView(infoTxt)
        
        setContentView(root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
        }
    }

    private fun showErrorDialog(msg: String) {
        val tv = TextView(this).apply {
            text = "SCREENSHOT LOG INI:\n\n$msg"
            setTextColor(Color.RED)
            textSize = 10f
            setPadding(40, 40, 40, 40)
        }
        val scroll = ScrollView(this).apply { addView(tv) }
        AlertDialog.Builder(this).setTitle("Crash Detected").setView(scroll)
            .setPositiveButton("OK") { d, _ -> d.dismiss() }.show()
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
                showErrorDialog(e.stackTraceToString())
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        super.onRequestPermissionsResult(rc, p, g)
        if (rc == 10 && allPermissionsGranted()) startCamera()
    }
}
