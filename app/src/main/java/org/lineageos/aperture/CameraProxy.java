package org.lineageos.aperture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class CameraProxy {

    public static void launchGcam(Activity activity, Uri output) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.setPackage("com.google.android.GoogleCamera");
        intent.putExtra("output", output);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, 1001);
    }
}
