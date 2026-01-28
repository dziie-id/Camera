package com.wrapper.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class CameraActivity extends Activity {

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
            GcamLauncher.launch(this, currentZoom);
        });
    }
}
package com.wrapper.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class CameraActivity extends Activity {

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
            GcamLauncher.launch(this, currentZoom);
        });
    }
}
