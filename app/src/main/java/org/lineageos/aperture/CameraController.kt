package org.lineageos.aperture

import android.content.Context
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager

class CameraController(private val context: Context) {

    private val manager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun open(cameraId: String) {
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {}
            override fun onDisconnected(camera: CameraDevice) {}
            override fun onError(camera: CameraDevice, error: Int) {}
        }, null)
    }

    fun switchCamera(cameraId: String) {
        open(cameraId)
    }

    fun takePicture(callback: (android.net.Uri) -> Unit) {
        ImageSaver(context).save(callback)
    }
}
