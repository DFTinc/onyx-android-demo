package com.dft.onyx50helloworld;

import android.app.Application;

import com.dft.onyxcamera.config.Onyx;

/**
 * Created by BigWheat on 3/4/2018.
 */

public class MainApplication extends Application {

    private static Onyx configuredOnyx;

    public void setConfiguredOnyx(Onyx configuredOnyx) {
        this.configuredOnyx = configuredOnyx;
    }

    public static Onyx getConfiguredOnyx() {
        return configuredOnyx;
    }
}
