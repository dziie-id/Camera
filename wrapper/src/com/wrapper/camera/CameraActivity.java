package com.wrapper.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class CameraActivity extends Activity {

    private static final int REQ_GCAM = 1001;
    private float currentZoom = 1.0f;
    private Uri cacheUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ImageButton btnShutter = findViewById(R.id.btn_shutter);
        ImageButton btnZoom1x = findViewById(R.id.btn_zoom_1x);
        ImageButton btnZoomUw = findViewById(R.id.btn_zoom_uw);

        btnZoom1x.setOnClickListener(v -> currentZoom = 1.0f);
        btnZoomUw.setOnClickListener(v -> currentZoom = 0.7f);

        btnShutter.setOnClickListener(v -> {
            cacheUri = createCacheUri();
            Intent intent = GcamLauncher.buildIntent(this, currentZoom, cacheUri);
            startActivityForResult(intent, REQ_GCAM);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_GCAM && resultCode == RESULT_OK && cacheUri != null) {
            processPhoto(cacheUri);

            Intent result = new Intent();
            result.setData(cacheUri);
            result.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            setResult(RESULT_OK, result);
            finish();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private Uri createCacheUri() {
        try {
            File file = new File(getCacheDir(), "capture.jpg");
            return FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file
            );
        } catch (Exception e) {
            return null;
        }
    }

    private void processPhoto(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(), uri
            );

            Bitmap watermarked = WatermarkUtil.apply(bitmap);

            FileOutputStream fos =
                    new FileOutputStream(new File(getCacheDir(), "capture.jpg"));
            watermarked.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
