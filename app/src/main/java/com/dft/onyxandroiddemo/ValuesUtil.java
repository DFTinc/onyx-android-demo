package com.dft.onyxandroiddemo;

import android.app.Activity;
import android.widget.Spinner;
import android.widget.Switch;

import com.dft.onyxcamera.ui.reticles.Reticle;

/**
 * Created by BigWheat on 2/10/2018.
 */

public class ValuesUtil {

    public static boolean getReturnRawImage(Activity a) {
        return ((Switch) a.findViewById(R.id.setReturnRawImage)).isChecked();
    }

    public static boolean getReturnProcessedImage(Activity a) {
        return ((Switch) a.findViewById(R.id.setReturnProcessedImage)).isChecked();
    }

    public static boolean getReturnWSQ(Activity a) {
        return ((Switch) a.findViewById(R.id.setReturnWSQ)).isChecked();
    }

    public static boolean getReturnFingerprintTemplate(Activity a) {
        return ((Switch) a.findViewById(R.id.setReturnFingerprintTemplate)).isChecked();
    }

    public static boolean getUseOnyxLive(Activity a) {
        return ((Switch) a.findViewById(R.id.setUseOnyxLive)).isChecked();
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

}
