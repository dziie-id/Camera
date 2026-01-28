package org.lineageos.aperture

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class ImageSaver(private val context: Context) {

    fun save(onDone: (Uri) -> Unit) {
        val file = File.createTempFile(
            "IMG_",
            ".jpg",
            context.cacheDir
        )

        // NANTI: ImageReader -> Bitmap -> JPEG

        val uri = FileProvider.getUriForFile(
            context,
            "org.lineageos.aperture.fileprovider",
            file
        )
        onDone(uri)
    }
}
