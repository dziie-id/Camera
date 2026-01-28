// Snippet simpel buat switch lensa
private fun setupCamera(lensFacing: Int) {
    val cameraSelector = CameraSelector.Builder()
        .addCameraFilter { cameras ->
            // Filter manual berdasarkan ID (biasanya UW ada di list terakhir/kedua)
            cameras.filter { it.cameraInfo.toString().contains("back") } 
        }
        .build()
    // Logic ganti lensa pas tombol 0.6x / 1x ditekan
}

