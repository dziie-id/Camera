package org.lineageos.aperture

import java.text.SimpleDateFormat
import java.util.*

object WatermarkUtil {
    fun getText(): String {
        val sdf = SimpleDateFormat(
            "HH:mm | dd MMM yyyy",
            Locale.getDefault()
        )
        return sdf.format(Date())
    }
}
