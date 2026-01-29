package org.lineageos.aperture;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;

public class MainActivity extends AppCompatActivity {
    private PreviewView previewView;
    private boolean isUltraWide = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Layout simpel pake kode (biar ente gak ribet buat XML lagi)
        previewView = new PreviewView(this);
        setContentView(previewView);

        // Tekan layar buat ganti lensa (Main <-> UW)
        previewView.setOnClickListener(v -> {
            isUltraWide = !isUltraWide;
            startCamera();
        });

        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                
                // Selector buat milih lensa
                CameraSelector selector = isUltraWide ?
                    new CameraSelector.Builder().addCameraFilter(cameras -> {
                        // Logic buat nyari lensa Ultra Wide (biasanya ID 2/lens back)
                        return cameras; 
                    }).build() : CameraSelector.DEFAULT_BACK_CAMERA;

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, selector, preview);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
}
