package org.lineageos.aperture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class WrapperActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Intent intent = getPackageManager()
                    .getLaunchIntentForPackage("com.google.android.apps.camera.go");
            if (intent != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "GCam Go not installed", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Camera launch failed", Toast.LENGTH_LONG).show();
        }
        finish();
    }
}
