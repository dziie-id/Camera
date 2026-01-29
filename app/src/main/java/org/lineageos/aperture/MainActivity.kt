package org.lineageos.aperture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TANGKAP ERROR DAN SIMPAN KE FILE TXT
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val log = sw.toString()

            try {
                // Simpan ke folder Download agar mudah dicari
                val path = Environment.getExternalContext().path + "/Download/kamera_crash.txt"
                val file = File(path)
                FileOutputStream(file).use { it.write(log.toByteArray()) }
            } catch (e: Exception) {
                // Jika folder publik gagal, simpan ke folder internal app
                val file = File(getExternalFilesDir(null), "crash_log.txt")
                FileOutputStream(file).use { it.write(log.toByteArray()) }
            }
            
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        // TAMPILAN STANDAR BIAR GAK KOSONG
        val root = FrameLayout(this)
        val tv = TextView(this).apply {
            text = "Kamera Sedang Inisialisasi...\nCek file kamera_crash.txt di folder Download jika app menutup."
            setTextColor(Color.WHITE)
            setGravity(android.view.Gravity.CENTER)
        }
        root.addView(tv)
        setContentView(root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 10)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                // Kita panggil paksa error buat ngetes log jika perlu
                // throw RuntimeException("Tes Log Manual") 
            } catch (e: Exception) {
                // Handle error
            }
        }, ContextCompat.getMainExecutor(this))
    }
}
