package org.lineageos.aperture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import java.io.File;

public class CameraWrapperActivity extends Activity {

    private static final int REQ_GCAM = 1001;
    private Uri outputUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File cacheFile = new File(getCacheDir(), "capture.jpg");
        outputUri = Uri.fromFile(cacheFile);

        Intent gcam = GcamDelegate.createIntent(outputUri);
        startActivityForResult(gcam, REQ_GCAM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_GCAM && resultCode == RESULT_OK) {
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
