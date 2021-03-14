package com.dft.onyx50demo;

import android.app.Activity;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.dft.onyxcamera.ui.reticles.Reticle;

/**
 * Created by BigWheat on 2/10/2018.
 */

public class ValuesUtil {

    public static boolean getReturnRawImage(Activity a) {
        return ((CheckBox) a.findViewById(R.id.returnRawBitmap)).isChecked();
    }

    public static boolean getReturnProcessedImage(Activity a) {
        return ((CheckBox) a.findViewById(R.id.returnProcessedBitmap)).isChecked();
    }

    public static boolean getReturnEnhancedImage(Activity a) {
        return ((CheckBox) a.findViewById(R.id.returnEnhancedBitmap)).isChecked();
    }

    public static boolean getReturnWSQ(Activity a) {
        return ((CheckBox) a.findViewById(R.id.returnWSQ)).isChecked();
    }

    public static boolean getReturnFingerprintTemplate(Activity a) {
        return ((CheckBox) a.findViewById(R.id.returnFingerprintTemplate)).isChecked();
    }

    public static boolean getShowLoadingSpinner(Activity a) {
        return ((CheckBox) a.findViewById(R.id.showLoadingSpinner)).isChecked();
    }

    public static boolean getUseOnyxLive(Activity a) {
        return ((CheckBox) a.findViewById(R.id.useOnyxLive)).isChecked();
    }

    public static boolean getComputeNfiqMetrics(Activity a) {
        return ((CheckBox) a.findViewById(R.id.nfiqMetrics)).isChecked();
    }

    public static boolean getUseFlash(Activity a) {
        return ((CheckBox) a.findViewById(R.id.useFlash)).isChecked();
    }

    public static boolean getThresholdImage(Activity a) {
        return ((CheckBox) a.findViewById(R.id.thresholdImage)).isChecked();
    }

    public static Integer getImageRotation(Activity a) {
        return Integer.valueOf(((Spinner) a.findViewById(R.id.imageRotation)).getSelectedItem().toString());
    }

    public static Reticle.Orientation getReticleOrientation(Activity a) {
        String reticleOrientation = ((Spinner) a.findViewById(R.id.reticleOrientationSpinner)).getSelectedItem().toString();
        Reticle.Orientation orientation = Reticle.Orientation.LEFT;
        if (reticleOrientation.equalsIgnoreCase(Reticle.Orientation.LEFT.toString())) {
            orientation = Reticle.Orientation.LEFT;
        } else if (reticleOrientation.equalsIgnoreCase(Reticle.Orientation.RIGHT.toString())) {
            orientation = Reticle.Orientation.RIGHT;
        } else if (reticleOrientation.equalsIgnoreCase(Reticle.Orientation.THUMB_PORTRAIT.toString())) {
            orientation = Reticle.Orientation.THUMB_PORTRAIT;
        }
        return orientation;
    }

    public static Float getReticleAngle(Activity a) {
        EditText reticleAngleEditText = a.findViewById(R.id.reticleAngleEditText);
        if (reticleAngleEditText.getText() != null && !reticleAngleEditText.getText().toString().equals("")) {
            return Float.valueOf(reticleAngleEditText.getText().toString());
        }
        return null;
    }

    public static Double getCropSizeWidth(Activity a) {
        if (((CheckBox) a.findViewById(R.id.cropSize)).isChecked()) {
            return Double.valueOf(((EditText) a.findViewById(R.id.widthEditText)).getText().toString());
        } else {
            return 300.0;
        }
    }

    public static Double getCropSizeHeight(Activity a) {
        if (((CheckBox) a.findViewById(R.id.cropSize)).isChecked()) {
            return Double.valueOf(((EditText) a.findViewById(R.id.heightEditText)).getText().toString());
        } else {
            return 512.0;
        }
    }

    public static Float getCropFactor(Activity a) {
        if (((CheckBox) a.findViewById(R.id.cropFactor)).isChecked()) {
            return Float.valueOf(((EditText) a.findViewById(R.id.cropFactorEditText)).getText().toString());
        } else {
            return 1.0f;
        }
    }

    public static Double getTargetPixelsPerInch(Activity a) {
        if(((CheckBox) a.findViewById(R.id.targetPixelsPerInch)).isChecked()) {
            return Double.valueOf(((EditText) a.findViewById(R.id.targetPixelsPerInchEditText)).getText().toString());
        } else {
            return -1.0;
        }
    }

    public static boolean getUseManualCapture(Activity a) {
        return ((CheckBox) a.findViewById(R.id.useManualCapture)).isChecked();
    }
}
