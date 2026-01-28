package org.lineageos.aperture;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

public class CameraProxy {

    private static final String GCAM_PACKAGE = "com.google.android.GoogleCamera";

    public static boolean launch(Activity activity, Uri output, boolean uw) {
        // 1. Try GCam
        if (isInstalled(activity, GCAM_PACKAGE)) {
            try {
                Intent gcam = new Intent("android.media.action.IMAGE_CAPTURE");
                gcam.setPackage(GCAM_PACKAGE);
                gcam.putExtra("output", output);

                // hint lens
                gcam.putExtra("android.intent.extra.CAMERA_FACING",
                        uw ? 2 : 0);

                gcam.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                activity.startActivityForResult(gcam, 1001);
                return true;
            } catch (Exception ignored) {}
        }

        // 2. Fallback generic camera
        try {
            Intent generic = new Intent("android.media.action.IMAGE_CAPTURE");
            generic.putExtra("output", output);
            generic.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.startActivityForResult(generic, 1001);
            return true;
        } catch (Exception ignored) {}

        return false;
    }

    private static boolean isInstalled(Activity a, String pkg) {
        try {
            a.getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
