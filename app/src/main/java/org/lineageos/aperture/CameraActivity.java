package org.lineageos.aperture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.FileProvider;

import java.io.File;

public class CameraActivity extends Activity {

    private static final int REQ_CAMERA = 1001;
    private Uri outputUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File cacheFile = new File(getCacheDir(), "photo.jpg");
        outputUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                cacheFile
        );

        CameraProxy.launchGcam(this, outputUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CAMERA && resultCode == RESULT_OK) {
            Intent result = new Intent();
            result.setData(outputUri);
            setResult(RESULT_OK, result);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }
}
