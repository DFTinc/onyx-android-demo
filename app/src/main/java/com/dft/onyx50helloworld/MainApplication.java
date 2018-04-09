package com.dft.onyx50helloworld;

import android.app.Application;

import com.dft.onyxcamera.config.Onyx;
import com.dft.onyxcamera.config.OnyxConfiguration;
import com.dft.onyxcamera.config.OnyxError;
import com.dft.onyxcamera.config.OnyxResult;

/**
 * Main application to hold Onyx objects
 */

public class MainApplication extends Application {

    private static OnyxConfiguration.SuccessCallback successCallback;
    private static OnyxConfiguration.ErrorCallback errorCallback;
    private static OnyxConfiguration.OnyxCallback onyxCallback;
    private static Onyx configuredOnyx;
    private static OnyxResult onyxResult;
    private static OnyxError onyxError;

    public void setSuccessCallback(OnyxConfiguration.SuccessCallback successCallback) {
        this.successCallback = successCallback;
    }

    public static OnyxConfiguration.SuccessCallback getSuccessCallback() {
        return successCallback;
    }

    public void setErrorCallback(OnyxConfiguration.ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
    }

    public static OnyxConfiguration.ErrorCallback getErrorCallback() {
        return errorCallback;
    }

    public void setOnyxCallback(OnyxConfiguration.OnyxCallback onyxCallback) {
        this.onyxCallback = onyxCallback;
    }

    public OnyxConfiguration.OnyxCallback getOnyxCallback() {
        return onyxCallback;
    }

    public void setConfiguredOnyx(Onyx configuredOnyx) {
        this.configuredOnyx = configuredOnyx;
    }

    public static Onyx getConfiguredOnyx() {
        return configuredOnyx;
    }

    public void setOnyxResult(OnyxResult onyxResult) { this.onyxResult = onyxResult; }

    public static OnyxResult getOnyxResult() { return onyxResult; }

    public void setOnyxError(OnyxError onyxError) { this.onyxError = onyxError; }

    public static OnyxError getOnyxError() { return onyxError; }
}