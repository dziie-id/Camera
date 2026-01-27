package org.lineageos.platform.internal;

import android.content.Context;
import android.os.IBinder;

/**
 * Stub class to allow standalone building of Aperture.
 * This mimics the LineageOS SDK Hardware Manager.
 */
public class LineageHardwareManager {
    private static LineageHardwareManager sInstance;

    public static LineageHardwareManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LineageHardwareManager();
        }
        return sInstance;
    }

    // Berikan nilai default agar aplikasi tidak crash
    public boolean get(int feature) {
        return false; 
    }

    public boolean set(int feature, boolean enable) {
        return false;
    }

    // Tambahkan konstanta yang sering dipanggil Aperture
    public static final int FEATURE_HIGH_BRIGHTNESS_MODE = 1;
    public static final int FEATURE_SUNLIGHT_ENHANCEMENT = 2;
}
