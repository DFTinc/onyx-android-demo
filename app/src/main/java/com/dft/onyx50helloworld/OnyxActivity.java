package com.dft.onyx50helloworld;

import android.app.Activity;
import android.os.Bundle;

import com.dft.onyxcamera.config.Onyx;

/**
 * Created by BigWheat on 3/4/2018.
 */

public class OnyxActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Onyx configuredOnyx = MainApplication.getConfiguredOnyx();
        configuredOnyx.create(this);
        configuredOnyx.capture();
    }
}
