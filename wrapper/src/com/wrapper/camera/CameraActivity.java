package com.wrapper.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;

import java.io.OutputStream;

public class CameraActivity extends Activity {

    private static final int REQ_GCAM = 1001;
    private float currentZoom = 1.0f;

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
            Intent intent = GcamLauncher.buildIntent(this, currentZoom);
            startActivityForResult(intent, REQ_GCAM);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_GCAM && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri photoUri = data.getData();
                processPhoto(photoUri);

                // RETURN KE APP PEMANGGIL
                Intent result = new Intent();
                result.setData(photoUri);
                setResult(RESULT_OK, result);

                // AUTO CLOSE
                finish();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    private void processPhoto(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(), uri
            );

            Bitmap watermarked = WatermarkUtil.apply(bitmap);

            OutputStream os = getContentResolver().openOutputStream(uri);
            watermarked.compress(Bitmap.CompressFormat.JPEG, 85, os);
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
