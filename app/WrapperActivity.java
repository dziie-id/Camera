package org.lineageos.aperture;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class WrapperActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Camera Wrapper OK", Toast.LENGTH_SHORT).show();
        finish();
    }
}
