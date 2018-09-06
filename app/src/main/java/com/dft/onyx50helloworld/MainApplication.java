package com.dft.onyx50helloworld;

import android.app.Activity;
import android.app.Application;

import com.dft.onyxcamera.config.Onyx;
import com.dft.onyxcamera.config.OnyxConfiguration;
import com.dft.onyxcamera.config.OnyxError;
import com.dft.onyxcamera.config.OnyxResult;

/**
 * Main application to hold Onyx objects
 */

public class MainApplication extends Application {
    private static Onyx configuredOnyx;
    private static OnyxResult onyxResult;
    private static OnyxError onyxError;
    private static Activity activityForRunningOnyx;

    public static void setActivityForRunningOnyx(OnyxActivity activityForRunningOnyx) {
        MainApplication.activityForRunningOnyx = activityForRunningOnyx;
    }

    public static Activity getActivityForRunningOnyx() {
        return activityForRunningOnyx;
    }

    public void setConfiguredOnyx(Onyx configuredOnyx) {
        MainApplication.configuredOnyx = configuredOnyx;
    }

    public static Onyx getConfiguredOnyx() {
        return configuredOnyx;
    }

    public void setOnyxResult(OnyxResult onyxResult) { MainApplication.onyxResult = onyxResult; }

    public static OnyxResult getOnyxResult() { return onyxResult; }

    public void setOnyxError(OnyxError onyxError) { MainApplication.onyxError = onyxError; }

    public static OnyxError getOnyxError() { return onyxError; }
}
