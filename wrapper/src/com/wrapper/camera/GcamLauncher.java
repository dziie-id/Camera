package com.wrapper.camera;

import android.app.Activity;
import android.content.Intent;

public class GcamLauncher {

    // SESUAIKAN DENGAN SGCAM LO
    private static final String GCAM_PACKAGE =
            "com.google.android.GoogleCameraEng";

    private static final String GCAM_ACTIVITY =
            "com.google.android.apps.camera.CameraActivity";

    public static Intent buildIntent(Activity activity, float zoom) {
        Intent intent = new Intent();
        intent.setClassName(GCAM_PACKAGE, GCAM_ACTIVITY);

        // DIRECT PHOTO MODE
        intent.putExtra("com.google.android.apps.camera.extra.CAPTURE_MODE", "photo");

        // HARDCODE ZOOM
        intent.putExtra("com.google.android.apps.camera.extra.ZOOM", zoom);

        // SILENT MODE
        intent.putExtra("android.intent.extra.SILENT_MODE", true);

        // MEDIUM QUALITY
        intent.putExtra("android.intent.extra.QUALITY", 85);

        return intent;
    }
}
