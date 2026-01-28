package org.lineageos.aperture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import androidx.core.content.FileProvider;
import java.io.File;

public class CameraActivity extends Activity {

    private Uri outputUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Button shutter = findViewById(R.id.btn_shutter);
        Button toggle = findViewById(R.id.btn_toggle);

        toggle.setText("1x");

        toggle.setOnClickListener(v -> {
            CameraMode.toggle();
            toggle.setText(CameraMode.isUW() ? "UW" : "1x");
        });

        shutter.setOnClickListener(v -> launchCamera());
    }

    private void launchCamera() {
        File file = new File(getCacheDir(), "photo.jpg");
        outputUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.setPackage("com.google.android.GoogleCamera");

        // hint lensa (GCam ngerti)
        intent.putExtra("android.intent.extra.CAMERA_FACING",
                CameraMode.isUW() ? 2 : 0);

        intent.putExtra("output", outputUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (res == RESULT_OK) {
            Intent result = new Intent();
            result.setData(outputUri);
            setResult(RESULT_OK, result);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }
}
