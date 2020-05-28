package com.dft.onyx50demo;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.dft.onyx.core;
import com.dft.onyxcamera.config.Onyx;
import com.dft.onyxcamera.config.OnyxError;
import com.dft.onyxcamera.config.OnyxResult;

import org.opencv.android.OpenCVLoader;

/**
 * Main application to hold Onyx objects
 */

public class MainApplication extends Application {
    private static final String TAG = "MainApplication";
    private static Onyx configuredOnyx;
    private static OnyxResult onyxResult;
    private static OnyxError onyxError;
    private static Activity activityForRunningOnyx;
    private static String speaker;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Unable to load OpenCV!");
        } else {
            Log.i(TAG, "OpenCV loaded successfully");
            core.initOnyx();
        }
    }

    public static void setActivityForRunningOnyx(OnyxActivity activityForRunningOnyx) {
        MainApplication.activityForRunningOnyx = activityForRunningOnyx;
    }

    public static Activity getActivityForRunningOnyx() {
        return activityForRunningOnyx;
    }

    public static void setSpeaker(String speaker) {
        MainApplication.speaker = speaker;
    }

    public static String getSpeaker() {
        return speaker;
    }

    public void setConfiguredOnyx(Onyx configuredOnyx) {
        MainApplication.configuredOnyx = configuredOnyx;
    }

    public static Onyx getConfiguredOnyx() {
        return configuredOnyx;
    }

    public static void setOnyxResult(OnyxResult onyxResult) { MainApplication.onyxResult = onyxResult; }

    public static OnyxResult getOnyxResult() { return onyxResult; }

    public void setOnyxError(OnyxError onyxError) { MainApplication.onyxError = onyxError; }

    public static OnyxError getOnyxError() { return onyxError; }
}
