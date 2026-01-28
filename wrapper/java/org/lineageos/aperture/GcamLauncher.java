package com.wrapper.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

public class GcamLauncher {

    private static final String GCAM_PACKAGE =
            "com.google.android.GoogleCameraEng";

    private static final String GCAM_ACTIVITY =
            "com.google.android.apps.camera.CameraActivity";

    public static Intent buildIntent(Activity activity, float zoom, Uri output) {
        Intent intent = new Intent();
        intent.setClassName(GCAM_PACKAGE, GCAM_ACTIVITY);

        intent.putExtra("com.google.android.apps.camera.extra.CAPTURE_MODE", "photo");
        intent.putExtra("com.google.android.apps.camera.extra.ZOOM", zoom);
        intent.putExtra("android.intent.extra.SILENT_MODE", true);
        intent.putExtra("android.intent.extra.QUALITY", 85);

        // TEMP OUTPUT (CACHE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        return intent;
    }
}
