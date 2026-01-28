package org.lineageos.aperture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CameraActivity extends Activity {

    private static final int REQ_GCAM = 1001;
    private boolean useWide = false; // default 1x

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Button btnShutter = findViewById(R.id.btn_shutter);
        Button btnSwitch = findViewById(R.id.btn_switch);

        btnSwitch.setText("1x");

        btnSwitch.setOnClickListener(v -> {
            useWide = !useWide;
            btnSwitch.setText(useWide ? "0.6x" : "1x");
        });

        btnShutter.setOnClickListener(v -> launchGcam());
    }

    private void launchGcam() {
        try {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

            // Hardcode target SGCam
            intent.setPackage("com.sgcam.android");

            // Hardcode lens hint (best effort)
            intent.putExtra("android.intent.extra.CAMERA_FACING",
                    useWide ? 2 : 0);

            // No sound / no vibration
            intent.putExtra("skipShutterSound", true);

            startActivityForResult(intent, REQ_GCAM);
        } catch (Exception e) {
            Toast.makeText(this, "GCam tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_GCAM && resultCode == RESULT_OK) {
            if (data != null) {
                // FOTO TIDAK DISAVE
                // langsung balik ke caller app
                setResult(RESULT_OK, data);
                finish();
            }
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
