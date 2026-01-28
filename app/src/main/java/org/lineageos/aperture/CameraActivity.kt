package org.lineageos.aperture

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CameraActivity : AppCompatActivity() {

    private lateinit var controller: CameraController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        controller = CameraController(this)
        controller.open(CameraIds.MAIN)
    }

    fun onShutterPressed() {
        controller.takePicture { uri ->
            val result = intent
            result.data = uri
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }

    fun switchToUltraWide() {
        controller.switchCamera(CameraIds.ULTRAWIDE)
    }

    fun switchToMain() {
        controller.switchCamera(CameraIds.MAIN)
    }
}
