# onyx-android-demo
Demo app for developers that demonstrates the features of Onyx Android SDK

Getting Started
---------------

If you don't already have Android Studio, you can download it <a href="http://developer.android.com/sdk/index.html" target="_blank">here</a>.

Once Android Studio is installed, please contact us to purchase your ONYX license key <a href="http://www.diamondfortress.com/contact" target="_blank">here</a>. <br />
**Note: Make sure you have updated to the latest Android SDK via the SDK Manager.**

You should receive a license of the form XXXX-XXXX-XXXX-X-X at your provided e-mail address.
<br />
Next, you can clone our sample repository on the command-line using the following commands:

    > cd <YOUR_DEVELOPMENT_ROOT>
    > git clone https://github.com/DFTinc/onyx-android-demo.git

Alternatively, you can clone the project via Android Studio:
<br/>
Select `VCS >> Checkout from Version Control >> GitHub`, and follow the on-screen instructions.

Place your license key into `app/src/main/res/values/license.xml` shown below:

    ...
    <string name="license_key_value">XXXX-XXXX-XXXX-X-X</string>
    ...

The license.xml file should be excluded from version control.

There are customizable string resources that can be overriden from the onyx-camera library by adding
the strings to the `app/src/main/res/values/strings.xml` file.
The strings and their values are:

`<string name="too_close_text">Move fingers back</string>`

`<string name="too_far_text">Move fingers closer</string>`

`<string name="capture_text">Hold fingers steady</string>`


Simply replace the strings with the translation that is desired, or use the Android Studio
Translations Editor: https://developer.android.com/studio/write/translations-editor

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Build >> Make Project" in Android Studio.

Now plug in your compatible device, and select Run >> Run 'app'.

Important addendum:  Update your security provider to protect against SSL exploits
Please see the article here: https://developer.android.com/training/articles/security-gms-provider

This is implemented in this project by adding a dependency on 'com.google.android.gms:play-services-base'.

The gist of it is to have an activity that implements ProviderInstaller.ProviderInstallListener.

In your onCreate() method, add a call to:
 ```
 ProviderInstaller.installIfNeededAsync(this, this);
 ```
Then add the following code:
```
    /**
     * The below is for updating the device's security provider to protect against SSL exploits
     * See https://developer.android.com/training/articles/security-gms-provider#java
     */
    private static final int ERROR_DIALOG_REQUEST_CODE = 11111;
    private boolean mRetryProviderInstall;

    /**
     * This method is only called if the provider is successfully updated
     * (or is already up-to-date).
     */
    @Override
    public void onProviderInstalled() {
        Log.i("OnyxSetupActivity","Provider is up-to-date, app can make secure network calls.");
    }

    /**
     * This method is called if updating fails; the error code indicates
     * whether the error is recoverable.
     */
    @Override
    public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        if (availability.isUserResolvableError(errorCode)) {
            // Recoverable error. Show a dialog prompting the user to
            // install/update/enable Google Play services.
            availability.showErrorDialogFragment(
                    this,
                    errorCode,
                    ERROR_DIALOG_REQUEST_CODE,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // The user chose not to take the recovery action
                            onProviderInstallerNotAvailable();
                        }
                    });
        } else {
            // Google Play services is not available.
            onProviderInstallerNotAvailable();
        }
    }

    /**
     * On resume, check to see if we flagged that we need to reinstall the
     * provider.
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mRetryProviderInstall) {
            // We can now safely retry installation.
            ProviderInstaller.installIfNeededAsync(this, this);
        }
        mRetryProviderInstall = false;
    }

    private void onProviderInstallerNotAvailable() {
        // This is reached if the provider cannot be updated for some reason.
        // App should consider all HTTP communication to be vulnerable, and take
        // appropriate action.
        Log.i("OnyxSetupActivity","ProviderInstaller not available, device cannot make secure network calls.");
    }
```
Finally, in your onActivityResult(int requestCode, int resultCode, Intent data), include the following:
```
    if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
        // Adding a fragment via GoogleApiAvailability.showErrorDialogFragment
        // before the instance state is restored throws an error. So instead,
        // set a flag here, which will cause the fragment to delay until
        // onPostResume.
        mRetryProviderInstall = true;
    }
```

For an example, see OnyxSetupActivity.java.

Documentation on the OnyxConfigurationBuilder is in the Java code comments, and listed here
for reference:
```

/**
 * Create this class first to build an instance of OnyxConfiguration.  This builder allows
 * you to specify all of the different options that you would like for Onyx.
 */

@Keep
public class OnyxConfigurationBuilder {
    private OnyxConfiguration onyxConfig = new OnyxConfiguration();

    /**
     * This sets the configActivity in which to run Onyx
     * @param activity (required) the Android configActivity
     */
    public OnyxConfigurationBuilder setActivity(Activity activity) {
        onyxConfig.setConfigActivity(activity);
        return this;
    }

    /**
     * This sets the Onyx license key
     * @param licenseKey (required) the Onyx license key
     */
    public OnyxConfigurationBuilder setLicenseKey(String licenseKey) {
        onyxConfig.setLicenseKey(licenseKey);
        return this;
    }

    /**
     * This sets the OnyxSuccess event handler.
     * @param successCallback (required) the event handler for the SuccessCallback.
     * @see OnyxConfiguration.SuccessCallback
     */
    public OnyxConfigurationBuilder setSuccessCallback(
            OnyxConfiguration.SuccessCallback successCallback) {
        onyxConfig.setSuccessCallback(successCallback);
        return this;
    }

    /**
     * This sets the ErrorCallback event handler.
     * @param errorCallback (required) the event handler for the ErrorCallback.
     * @see OnyxConfiguration.ErrorCallback
     */
    public OnyxConfigurationBuilder setErrorCallback(OnyxConfiguration.ErrorCallback errorCallback) {
        onyxConfig.setErrorCallback(errorCallback);
        return this;
    }

    /**
     * This sets the OnyxCallback event handler.
     * The callback returns the Onyx object used to create Onyx.
     * @param onyxCallback
     */
    public OnyxConfigurationBuilder setOnyxCallback(
            OnyxConfiguration.OnyxCallback onyxCallback) {
        onyxConfig.setOnyxCallback(onyxCallback);
        return this;
    }

    /**
     * This method sets whether or not the capture task will return the raw image in the
     * {@link OnyxResult}
     */
    public OnyxConfigurationBuilder setReturnRawImage(boolean returnRawImage) {
        onyxConfig.setReturnRawImage(returnRawImage);
        return this;
    }

    /**
     * This method sets whether or not the capture task will return the processed image in the
     * {@link OnyxResult}
     */
    public OnyxConfigurationBuilder setReturnProcessedImage(boolean returnProcessedImage) {
        onyxConfig.setReturnProcessedImage(returnProcessedImage);
        return this;
    }

    /**
     * This method sets whether or not the capture task will return the enhanced image in the
     * {@link OnyxResult}
     */
    public OnyxConfigurationBuilder setReturnEnhancedImage(boolean returnEnhancedImage) {
        onyxConfig.setReturnEnhancedImage(returnEnhancedImage);
        return this;
    }

    /**
     * This method sets whether or not the capture task will return a full frame image in the
     * {@link OnyxResult}
     */
    public OnyxConfigurationBuilder setReturnFullFrameImage(boolean returnFullFrameImage) {
        onyxConfig.setReturnFullFrameImage(returnFullFrameImage);
        return this;
    }

    /**
     * This method sets the scaleFactor for the FullFrame image.
     */
    public OnyxConfigurationBuilder setFullFrameScaleFactor(float fullFrameScaleFactor) {
        onyxConfig.setFullFrameScaleFactor(fullFrameScaleFactor);
        return this;
    }

    /**
     * This method sets whether or not the capture task will return the WSQ image in the
     * {@link OnyxResult}
     */
    public OnyxConfigurationBuilder setReturnWSQ(boolean returnWSQ) {
        onyxConfig.setReturnWSQ(returnWSQ);
        return this;
    }

    /**
     * This method sets whether or not the capture task will return the {@link com.dft.onyx.FingerprintTemplate} in the
     * {@link OnyxResult}
     */
    public OnyxConfigurationBuilder setReturnFingerprintTemplate(boolean returnFingerprintTemplate) {
        onyxConfig.setReturnFingerprintTemplate(returnFingerprintTemplate);
        return this;
    }

    /**
     * This method sets whether or not the capture task will return a PGM formatted byte[] in the
     * {@link OnyxResult}
     */
    public OnyxConfigurationBuilder setReturnPGMFormatByteArray(boolean returnPGMFormatByteArray) {
        onyxConfig.setReturnPGMFormatByteArray(returnPGMFormatByteArray);
        return this;
    }

    /**
     * This method sets whether or not the capture task should convert the FingerprintTemplate
     * to an ISO Template
     * @param shouldConvertToISOTemplate true if the FingerprintTemplate should be converted
     */
    public OnyxConfigurationBuilder setShouldConvertToISOTemplate(boolean shouldConvertToISOTemplate) {
        onyxConfig.setShouldConvertToISOTemplate(shouldConvertToISOTemplate);
        return this;
    }

    /**
     * This method sets the imageRotation amount for the image.
     * @param rotation an integer specifying the imageRotation amount (0, 90, 180, or 270 degrees).
     * @note only 90 degree rotations are supported for speed reasons.
     */
    public OnyxConfigurationBuilder setImageRotation(int rotation) {
        onyxConfig.setImageRotation(rotation);
        return this;
    }

    /**
     * This method sets whether or not to set the crop to the whole finger.
     * @param width sets the width of the center crop area.
     * @param height sets the height of the center crop area.
     */
    public OnyxConfigurationBuilder setCropSize(double width, double height) {
        onyxConfig.setCropSize(new Size(width, height));
        return this;
    }

    /**
     * This method sets the crop factor to use when cropping the finger portion from the image.
     * @param cropFactor
     * @return
     */
    public OnyxConfigurationBuilder setCropFactor(double cropFactor) {
        onyxConfig.setCropFactor(cropFactor);
        return this;
    }

    /**
     * This method sets whether or not to show the spinner while waiting for Onyx to load its setup
     * @param showSpinner
     */
    public OnyxConfigurationBuilder setShowLoadingSpinner(boolean showSpinner) {
        onyxConfig.setShowSpinner(showSpinner);
        return this;
    }

    /**
     * This method sets whether or not to use the manual capture instead of the auto-capture.
     * Manual capture is where the operator will tap the screen in order to capture.
     * algorithm for deteching the finger.
     */
    public OnyxConfigurationBuilder setUseManualCapture(boolean useManualCapture) {
        onyxConfig.setManualCapture(useManualCapture);
        return this;
    }

    /**
     * This method sets whether or not to use Onyx liveness detection for real/fake finger detection
     * @param useOnyxLive
     */
    public OnyxConfigurationBuilder setUseOnyxLive(boolean useOnyxLive) {
        onyxConfig.setUseOnyxLive(useOnyxLive);
        return this;
    }

    /**
     * This method sets the flash mode on (true) or off (false)
     * @param useFlash
     */
    public OnyxConfigurationBuilder setUseFlash(boolean useFlash) {
        onyxConfig.setUseFlash(useFlash);
        return this;
    }

    /**
     * This method sets the {@link com.dft.onyxcamera.ui.reticles.Reticle.Orientation}.  This was
     * for use with the single finger capture for changing the orientation for left or right or
     * vertical orientation for thumb captures.
     * @param reticleOrientation
     */
    public OnyxConfigurationBuilder setReticleOrientation(Reticle.Orientation reticleOrientation) {
        onyxConfig.setReticleOrientation(reticleOrientation);
        return this;
    }

    /**
     * This method sets the angle of the reticle
     * @param reticleAngle the degree angle to rotate the reticle
     * positive values rotate clockwise
     */
    public OnyxConfigurationBuilder setReticleAngle(float reticleAngle) {
        onyxConfig.setReticleAngle(reticleAngle);
        onyxConfig.setOverrideReticleOrientation(true);
        return this;
    }

    /**
     * This method sets the preference for the layout of capture UI.  The options are FULL_SCREEN
     * or UPPER_THIRD.  The UPPER_THIRD is a carry-over from previous versions of Onyx and is
     * for backwards compatibility with the older versions.
     * @param layoutPreference
     */
    public OnyxConfigurationBuilder setLayoutPreference(OnyxConfiguration.LayoutPreference layoutPreference) {
        onyxConfig.setLayoutPreference(layoutPreference);
        return this;
    }

    /**
     * This method sets the configuration to use MittOverlayReticleView.  The first argument is
     * to enable the "Mitt" style four finger reticle.  The second argument determines if the
     * layout should be set to the right hand layout.
     */
    public OnyxConfigurationBuilder setUseFourFingerReticle(boolean useFourFingerReticle, boolean useRightHandLayout) {
        onyxConfig.setUseFourFingerReticle(useFourFingerReticle);
        onyxConfig.setUseRightHandLayout(useRightHandLayout);
        return this;
    }

    /**
     * This method thresholds the processed image to black and white pixels (necessary for some matchers)
     */
    public OnyxConfigurationBuilder setThresholdProcessedImage(boolean thresholdProcessedImage) {
        onyxConfig.setThresholdProcessedImage(thresholdProcessedImage);
        return this;
    }

    /**
     * This method configures Onyx to compute NFIQ metrics
     */
    public OnyxConfigurationBuilder setComputeNfiqMetrics(boolean computeNfiqMetrics) {
        onyxConfig.setShouldComputeNfiq(computeNfiqMetrics);
        return this;
    }

    /**
     * This method configures the target pixels per inch
     */
    public OnyxConfigurationBuilder setTargetPixelsPerInch(double pixelsPerInch) {
        onyxConfig.setTargetPixelsPerInch(pixelsPerInch);
        return this;
    }

    /**
     * This method configures Onyx to use the left hand layout
     */
    public OnyxConfigurationBuilder setUseLeftHandLayout(boolean useLeftHandLayout) {
        onyxConfig.setUseLeftHandLayout(useLeftHandLayout);
        return this;
    }

    /**
     * This method configures Onyx to use the right hand layout
     */
    public OnyxConfigurationBuilder setUseRightHandLayout(boolean useRightHandLayout) {
        onyxConfig.setUseRightHandLayout(useRightHandLayout);
        return this;
    }

    /**
     * This method configures Onyx with a unique user id
     */
    public OnyxConfigurationBuilder setUniqueUserID(String uniqueUserID) {
        onyxConfig.setUniqueUserID(uniqueUserID);
        return this;
    }

    /**
     * This method configures Onyx to perform a quality check by matching an existing template
     */
    public OnyxConfigurationBuilder setPerformQualityCheckMatch(boolean isPerformQualityCheckMatch) {
        onyxConfig.setPerformQualityCheckMatch(isPerformQualityCheckMatch);
        return this;
    }

    /**
     * This method configures Onyx to upload capture metrics result
     */
    public OnyxConfigurationBuilder setUploadMetrics(boolean isUploadMetrics) {
        onyxConfig.setUploadMetrics(isUploadMetrics);
        return this;
    }

    /**
     * This method configures Onyx to use a lens focus distance for camera2 API compatible devices.
     * This is used to set a "fixed focus" distance.  The value can vary based on the device
     * capabilities.  If the supplied parameter is not valid for the device, it will attempt a
     * default value based on information reported by the device.  If this does not work, it will
     * fall back to auto-focus for the device.
     */
    public OnyxConfigurationBuilder setLensFocusDistanceCamera2(float lensFocusDistanceCamera2) {
        onyxConfig.setLensFocusDistanceCamera2(lensFocusDistanceCamera2);
        return this;
    }

    /**
     * This method configures Onyx to only return an OnyxResult if the image quality is only
     * of high quality.  It will continue to trigger image captures until it gets
     * high quality images.  The quality check is only done on the first finger to allow for thumb
     * capture currently.
     */
    public OnyxConfigurationBuilder setReturnOnlyHighQualityImages(boolean isReturnOnlyHighQualityImages) {
        onyxConfig.setReturnOnlyHighQualityImages(isReturnOnlyHighQualityImages);
        return this;
    }

    /**
     * This method configures Onyx to return an OnyxError if the image quality is an NFIQ score
     * of 5, indicating that it is not a usable image, and should not be used.
     */
    public OnyxConfigurationBuilder setReturnOnyxErrorOnNFIQScore5(boolean isReturnOnyxErrorOnNFIQScore5) {
        onyxConfig.setReturnOnyxErrorOnNFIQScore5(isReturnOnyxErrorOnNFIQScore5);
        return this;
    }

    /**
     * This method configures Onyx to enable or disable the camera shutter sound if this option
     * is available on the device.  Not all device manufactuers implement this, so it is not
     * guaranteed to work on all devices.  The default is true for this configuration.
     */
    public OnyxConfigurationBuilder setEnableShutterSound(boolean isEnableShutterSound) {
        onyxConfig.setEnableShutterSound(isEnableShutterSound);
        return this;
    }

    /**
     * This method builds the OnyxConfiguration object with the specified parameters, and
     * checks that all configuration setup is complete before returning the {@link Onyx} via
     * {@link com.dft.onyxcamera.config.OnyxConfiguration.OnyxCallback#onConfigured onConfigured}
     */
    public void buildOnyxConfiguration() {
        onyxConfig.setCaptureAnimationCallback(new CaptureAnimationCallbackUtil()
                .createCaptureAnimationCallback(onyxConfig.getConfigActivity()));
        Onyx onyx = new Onyx();
        onyx.doSetup(onyxConfig);
    }
}


```

Support
-------

- Diamond Fortress Technologies Support Site: <a href="http://support.diamondfortress.com" target="_blank">support.diamondfortress.com</a>
