package org.lineageos.aperture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class UltraWideActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri output = getIntent().getData();
        Intent gcam = GcamDelegate.createIntent(output, true);
        startActivityForResult(gcam, 300);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        setResult(res);
        finish();
    }
}
