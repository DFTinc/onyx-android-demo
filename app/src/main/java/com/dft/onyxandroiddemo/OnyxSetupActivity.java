package com.dft.onyxandroiddemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dft.onyxcamera.config.Onyx;
import com.dft.onyxcamera.config.OnyxConfiguration;
import com.dft.onyxcamera.config.OnyxConfigurationBuilder;
import com.dft.onyxcamera.config.OnyxError;
import com.dft.onyxcamera.config.OnyxResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;

import timber.log.Timber;

import static com.dft.onyxandroiddemo.ValuesUtil.getReturnFingerprintTemplate;
import static com.dft.onyxandroiddemo.ValuesUtil.getReturnProcessedImage;
import static com.dft.onyxandroiddemo.ValuesUtil.getReturnRawImage;
import static com.dft.onyxandroiddemo.ValuesUtil.getReturnWSQ;
import static com.dft.onyxandroiddemo.ValuesUtil.getUseOnyxLive;

public class OnyxSetupActivity extends Activity implements ProviderInstaller.ProviderInstallListener {
    private static final String TAG = OnyxSetupActivity.class.getName();
    private static final int ONYX_REQUEST_CODE = 1337;
    MainApplication application = new MainApplication();
    private Activity activity;
    private AlertDialog alertDialog;

    private OnyxConfiguration.SuccessCallback successCallback;
    private OnyxConfiguration.ErrorCallback errorCallback;
    private OnyxConfiguration.OnyxCallback onyxCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProviderInstaller.installIfNeededAsync(this, this); // This is needed in order for SSL to work on Android 5.1 devices and lower
        new FileUtil().getWriteExternalStoragePermission(this); // This is for file writing permission on SDK >= 23
        setupUI();
        setupCallbacks();
    }

    private void setupCallbacks() {
        successCallback = new OnyxConfiguration.SuccessCallback() {
            @Override
            public void onSuccess(OnyxResult onyxResult) {
                application.setOnyxResult(onyxResult);
                finishActivityForRunningOnyx();
            }
        };

        errorCallback = new OnyxConfiguration.ErrorCallback() {
            @Override
            public void onError(OnyxError onyxError) {
                Log.e("OnyxError", onyxError.getErrorMessage());
                application.setOnyxError(onyxError);
                showAlertDialog(onyxError);
                finishActivityForRunningOnyx();
            }
        };

        onyxCallback = new OnyxConfiguration.OnyxCallback() {
            @Override
            public void onConfigured(Onyx configuredOnyx) {
                application.setConfiguredOnyx(configuredOnyx);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivityForResult(new Intent(activity, OnyxActivity.class), ONYX_REQUEST_CODE);
                    }
                });
            }
        };
    }

    private void finishActivityForRunningOnyx() {
        if (MainApplication.getActivityForRunningOnyx() != null) {
            MainApplication.getActivityForRunningOnyx().finish();
        }
    }

    private void setupOnyx(final Activity activity) {
        // Create an OnyxConfigurationBuilder and configure it with desired options
        OnyxConfigurationBuilder onyxConfigurationBuilder = new OnyxConfigurationBuilder()
                .setActivity(activity)
                .setLicenseKey(getResources().getString(R.string.onyx_license))
                .setReturnRawImage(getReturnRawImage(this))
                .setReturnProcessedImage(getReturnProcessedImage(this))
                .setReturnWSQ(getReturnWSQ(this))
                .setReturnFingerprintTemplate(getReturnFingerprintTemplate(this))
                .setUseOnyxLive(getUseOnyxLive(this))
                .setComputeNfiqMetrics(true)
                .setSuccessCallback(successCallback)
                .setErrorCallback(errorCallback)
                .setOnyxCallback(onyxCallback);

        // Finally, build the OnyxConfiguration
        onyxConfigurationBuilder.buildOnyxConfiguration();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MainApplication.getOnyxResult() != null) {
            displayResults(MainApplication.getOnyxResult());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
            // Adding a fragment via GoogleApiAvailability.showErrorDialogFragment
            // before the instance state is restored throws an error. So instead,
            // set a flag here, which will cause the fragment to delay until
            // onPostResume.
            mRetryProviderInstall = true;
        }
    }

    private void displayResults(OnyxResult onyxResult) {
        startActivity(new Intent(this, OnyxImageryActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Timber.v("Permission: " + permissions[0] + " was " + grantResults[0]);
        }
    }

    private void setupUI() {
        activity = this;
        setContentView(R.layout.activity_main);
        Button captureButton = findViewById(R.id.captureButton);
        captureButton.bringToFront();
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainApplication.setOnyxResult(null);
                setupOnyx(activity);
            }
        });
    }

    /**
     * This displays an AlertDialog upon receiving an OnyxError, please handle appropriately for
     * your application
     *
     * @param onyxError
     */
    private void showAlertDialog(OnyxError onyxError) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setTitle("Onyx Error");
        alertDialogBuilder.setMessage(onyxError.getErrorMessage());
        alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                alertDialog.dismiss();
            }
        });

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

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
        Timber.i("Provider is up-to-date, app can make secure network calls.");
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
        Timber.i("ProviderInstaller not available, device cannot make secure network calls.");
    }
}
