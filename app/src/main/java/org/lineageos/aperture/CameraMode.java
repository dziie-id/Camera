package org.lineageos.aperture;

public class CameraMode {
    public static final int MODE_MAIN = 0;
    public static final int MODE_UW = 1;

    private static int currentMode = MODE_MAIN;

    public static void toggle() {
        currentMode = (currentMode == MODE_MAIN) ? MODE_UW : MODE_MAIN;
    }

    public static boolean isUW() {
        return currentMode == MODE_UW;
    }
}
