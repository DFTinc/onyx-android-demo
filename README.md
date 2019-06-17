# onyx-5.0-demo
Demo app for developers that demonstrates the features of Onyx 5.0 SDK

Getting Started
---------------

If you don't already have Android Studio, you can download it <a href="http://developer.android.com/sdk/index.html" target="_blank">here</a>.

Once Android Studio is installed, please contact us to purchase your ONYX license key <a href="http://www.diamondfortress.com/contact" target="_blank">here</a>. <br />
**Note: Make sure you have updated to the latest Android SDK via the SDK Manager.**
Please contact Diamond Fortress Technologies here www.diamondfortress.com to purchase a license key
You should receive a license of the form XXXX-XXXX-XXXX-X-X at your provided e-mail address.
<br />
Next, you can clone our sample repository on the command-line using the following commands:

    > cd <YOUR_DEVELOPMENT_ROOT>
    > git clone https://github.com/DFTinc/onyx-5.0-demo.git

Alternatively, you can clone the project via Android Studio:
<br/>
Select `VCS >> Checkout from Version Control >> GitHub`, and follow the on-screen instructions.

Place your license key into `app/src/main/res/values/strings.xml` shown below:

    ...
    <string name="onyx_license">XXXX-XXXX-XXXX-X-X</string>
    ...

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

Support
-------

- Diamond Fortress Technologies Support Site: <a href="http://support.diamondfortress.com" target="_blank">support.diamondfortress.com</a>
