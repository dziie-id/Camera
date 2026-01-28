package org.lineageos.aperture;

import android.content.Intent;
import android.net.Uri;

public class GcamDelegate {

    public static Intent createIntent(Uri output, boolean uw) {
        Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
        i.setPackage("com.sgc.camera");

        i.putExtra("output", output);
        i.putExtra("disable_aux", true);

        if (uw) {
            i.putExtra("camera_mode", "ultrawide");
            i.putExtra("lens_id", 2);
        } else {
            i.putExtra("camera_mode", "main");
            i.putExtra("lens_id", 0);
        }

        return i;
    }
}
