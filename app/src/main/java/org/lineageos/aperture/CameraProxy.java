package org.lineageos.aperture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class CameraProxy {

    public static void launchGcam(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setPackage("com.google.android.GoogleCamera");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
}
