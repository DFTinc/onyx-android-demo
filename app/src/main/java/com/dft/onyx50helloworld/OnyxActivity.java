package com.dft.onyx50helloworld;

import android.app.Activity;

import com.dft.onyxcamera.config.Onyx;

/**
 * Created by BigWheat on 3/4/2018.
 */

public class OnyxActivity extends Activity {

    @Override
    public void onResume() {
        super.onResume();
        setContentView(R.layout.activity_onyx);
        Onyx configuredOnyx = MainApplication.getConfiguredOnyx();
        configuredOnyx.create(this);
        configuredOnyx.capture();
    }
}
