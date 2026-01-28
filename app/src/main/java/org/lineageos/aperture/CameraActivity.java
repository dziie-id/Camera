package org.lineageos.aperture;

import android.app.Activity;
import android.os.Bundle;

public class CameraActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraProxy.launchGcam(this);
        finish();
    }
}
