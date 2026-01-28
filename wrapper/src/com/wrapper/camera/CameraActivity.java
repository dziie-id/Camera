package com.wrapper.camera;

import android.graphics.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WatermarkUtil {

    public static Bitmap apply(Bitmap src) {
        Bitmap result = src.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(36f);
        paint.setShadowLayer(3f, 2f, 2f, Color.BLACK);

        String text = new SimpleDateFormat(
                "HH:mm | dd-MM-yyyy",
                Locale.getDefault()
        ).format(new Date());

        float x = result.getWidth() - paint.measureText(text) - 32;
        float y = result.getHeight() - 32;

        canvas.drawText(text, x, y, paint);
        return result;
    }
}
