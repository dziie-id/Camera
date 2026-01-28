package org.lineageos.aperture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import java.io.File;

public class CameraWrapperActivity extends Activity {

    private Uri outputUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrapper);

        File cacheFile = new File(getCacheDir(), "capture.jpg");
        outputUri = Uri.fromFile(cacheFile);

        Button btnMain = findViewById(R.id.btnMain);
        Button btnUW = findViewById(R.id.btnUW);

        btnMain.setOnClickListener(v -> {
            launchGcam(false);
        });

        btnUW.setOnClickListener(v -> {
            Intent uw = new Intent(this, UltraWideActivity.class);
            uw.setData(outputUri);
            startActivityForResult(uw, 200);
        });
    }

    private void launchGcam(boolean uw) {
        Intent gcam = GcamDelegate.createIntent(outputUri, uw);
        startActivityForResult(gcam, 100);
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

    @Override
    protected void onDestroy() {
        File f = new File(getCacheDir(), "capture.jpg");
        if (f.exists()) f.delete();
        super.onDestroy();
    }
}
