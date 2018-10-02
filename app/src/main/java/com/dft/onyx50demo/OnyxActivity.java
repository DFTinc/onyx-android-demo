package com.dft.onyx50demo;

import android.app.Activity;
import android.os.Bundle;

import com.dft.onyxcamera.config.Onyx;

/**
 * Example activity for running Onyx that has been previously configured.
 */

public class OnyxActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Setting the activity being used to run Onyx here so that it can be finished from
        // the SuccessCallback in OnyxSetupActivity
        MainApplication.setActivityForRunningOnyx(this);

        // Get the configured Onyx that was returned from the OnyxCallback
        Onyx configuredOnyx = MainApplication.getConfiguredOnyx();

        // Creates Onyx in this activity
        configuredOnyx.create(this);

        // Make Onyx start the capture process
        configuredOnyx.capture();
    }
}
