package org.lineageos.aperture;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private CameraControl cameraControl;

    /* ================= onCreate ================= */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. blokir screenshot & screen record
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        // 2. kunci portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_camera);

        // 3. sembunyikan status bar & navbar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        previewView = findViewById(R.id.previewView);

        startCamera();

        Button btnWide = findViewById(R.id.btnWide);
        Button btnNormal = findViewById(R.id.btnNormal);

        btnWide.setOnClickListener(v -> switchToWide());
        btnNormal.setOnClickListener(v -> switchToNormal());
    }

    /* ================= CAMERA ================= */

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                        .build();

                provider.unbindAll();

                Camera camera = provider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                );

                cameraControl = camera.getCameraControl();

                switchToNormal(); // default 1x

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /* ================= ZOOM ================= */

    private void switchToWide() {
        if (cameraControl != null)
            cameraControl.setLinearZoom(0.0f);
        updateZoomUI(true);
    }

    private void switchToNormal() {
        if (cameraControl != null)
            cameraControl.setLinearZoom(0.5f);
        updateZoomUI(false);
    }

    private void updateZoomUI(boolean wide) {
        ((Button)findViewById(R.id.btnWide))
                .setBackgroundResource(wide
                        ? R.drawable.bg_zoom_button_active
                        : R.drawable.bg_zoom_button);

        ((Button)findViewById(R.id.btnNormal))
                .setBackgroundResource(wide
                        ? R.drawable.bg_zoom_button
                        : R.drawable.bg_zoom_button_active);
    }

    /* ================= SHUTTER ================= */

    public void takePicture(View v) {
        if (imageCapture == null) return;

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy proxy) {
                        Image img = proxy.getImage();
                        if (img != null) {
                            Bitmap bmp = imageToBitmap(img);
                            Bitmap result = applyWatermark(bmp);
                            sendResult(result);
                            img.close();
                        }
                        proxy.close();
                    }
                }
        );
    }

    /* ================= INTENT RESULT ================= */

    private static final String RESULT_IMAGE_KEY =
            "org.lineageos.aperture.RESULT_IMAGE_JPEG";

    private void sendResult(Bitmap bmp) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, os);

        Intent data = new Intent();
        data.putExtra(RESULT_IMAGE_KEY, os.toByteArray());

        setResult(RESULT_OK, data);
        finish();
    }

    /* ================= WATERMARK ================= */

    private Bitmap imageToBitmap(Image img) {
        ByteBuffer buf = img.getPlanes()[0].getBuffer();
        byte[] b = new byte[buf.remaining()];
        buf.get(b);
        return android.graphics.BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    private Bitmap applyWatermark(Bitmap src) {
        Bitmap out = src.copy(Bitmap.Config.ARGB_8888, true);
        Canvas c = new Canvas(out);

        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setTextSize(src.getWidth() * 0.035f);
        p.setAntiAlias(true);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setShadowLayer(4f, 2f, 2f, Color.BLACK);

        String text = time + "  |  " + date;
        float x = src.getWidth() - p.measureText(text) - (src.getWidth() * 0.04f);
        float y = src.getHeight() - (src.getHeight() * 0.04f);

        c.drawText(text, x, y, p);
        return out;
    }

    /* ================= FAIL SAFE ================= */

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
    }
            
