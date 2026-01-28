package org.lineageos.aperture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class WrapperActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "Aperture Wrapper Active", Toast.LENGTH_SHORT).show();

        try {
            Intent intent = new Intent();
            intent.setClassName(
                "org.lineageos.aperture",
                "org.lineageos.aperture.CameraActivity"
            );
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
        }

        finish();
    }
}
