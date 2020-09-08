package com.dft.onyx50demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dft.onyxcamera.config.Onyx;
import com.dft.onyxcamera.config.OnyxConfiguration;
import com.dft.onyxcamera.config.OnyxConfigurationBuilder;
import com.dft.onyxcamera.config.OnyxError;
import com.dft.onyxcamera.config.OnyxResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxSecurityLevel;
import SecuGen.FDxSDKPro.SGISOTemplateInfo;

import static com.dft.onyx50demo.ValuesUtil.getCropFactor;
import static com.dft.onyx50demo.ValuesUtil.getCropSizeHeight;
import static com.dft.onyx50demo.ValuesUtil.getCropSizeWidth;
import static com.dft.onyx50demo.ValuesUtil.getImageRotation;
import static com.dft.onyx50demo.ValuesUtil.getReticleAngle;
import static com.dft.onyx50demo.ValuesUtil.getReticleOrientation;
import static com.dft.onyx50demo.ValuesUtil.getReturnEnhancedImage;
import static com.dft.onyx50demo.ValuesUtil.getReturnFingerprintTemplate;
import static com.dft.onyx50demo.ValuesUtil.getReturnProcessedImage;
import static com.dft.onyx50demo.ValuesUtil.getReturnRawImage;
import static com.dft.onyx50demo.ValuesUtil.getReturnWSQ;
import static com.dft.onyx50demo.ValuesUtil.getShowLoadingSpinner;
import static com.dft.onyx50demo.ValuesUtil.getTargetPixelsPerInch;
import static com.dft.onyx50demo.ValuesUtil.getThresholdImage;
import static com.dft.onyx50demo.ValuesUtil.getUseFlash;
import static com.dft.onyx50demo.ValuesUtil.getUseManualCapture;
import static com.dft.onyx50demo.ValuesUtil.getUseOnyxLive;
import static com.dft.onyx50demo.ValuesUtil.getUseRightHandLayout;

public class OnyxSetupActivity extends Activity implements ProviderInstaller.ProviderInstallListener {
    private static final String TAG = OnyxSetupActivity.class.getName();
    private static final int ONYX_REQUEST_CODE = 1337;
    MainApplication application = new MainApplication();
    private Activity activity;
    private ImageView fingerprintView;
    private Button startOnyxButton;
    private AlertDialog alertDialog;
    private TextView livenessResultTextView;
    private TextView nfiqScoreTextView;

    private OnyxConfiguration.SuccessCallback successCallback;
    private OnyxConfiguration.ErrorCallback errorCallback;
    private OnyxConfiguration.OnyxCallback onyxCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProviderInstaller.installIfNeededAsync(this, this); // This is needed in order for SSL to work on Android 5.1 devices and lower
        FileUtil fileUtil = new FileUtil();
        fileUtil.getWriteExternalStoragePermission(this); // This is for file writing permission on SDK >= 23
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
                        startOnyxButton.setEnabled(true);
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
                .setReturnEnhancedImage(getReturnEnhancedImage(this))
                .setReturnWSQ(getReturnWSQ(this))
                .setReturnFingerprintTemplate(getReturnFingerprintTemplate(this))
                .setThresholdProcessedImage(getThresholdImage(this))
                .setShowLoadingSpinner(getShowLoadingSpinner(this))
                .setUseOnyxLive(getUseOnyxLive(this))
                .setUseFlash(getUseFlash(this))
                .setImageRotation(getImageRotation(this))
                .setReticleOrientation(getReticleOrientation(this))
                .setCropSize(getCropSizeWidth(this), getCropSizeHeight(this))
                .setCropFactor(getCropFactor(this))
                .setTargetPixelsPerInch(getTargetPixelsPerInch(this))
                .setUseFourFingerReticle(true, false)
                .setUseRightHandLayout(getUseRightHandLayout(this))
                .setLayoutPreference(OnyxConfiguration.LayoutPreference.FULL)
                .setLensFocusDistanceCamera2(10.0f)
                .setSuccessCallback(successCallback)
                .setErrorCallback(errorCallback)
                .setOnyxCallback(onyxCallback);
        // Reticle Angle overrides Reticle Orientation so have to set this separately
        if (getReticleAngle(this) != null) {
            onyxConfigurationBuilder.setReticleAngle(getReticleAngle(this));
        }
        if (getUseManualCapture(this)) {
            onyxConfigurationBuilder.setUseManualCapture(true);
        }
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
        if (onyxResult.getMetrics() != null) {
            livenessResultTextView.setText(Double.toString(onyxResult.getMetrics().getLivenessConfidence()));
//            nfiqScoreTextView.setText(Integer.toString(onyxResult.getMetrics().getNfiqMetrics().getNfiqScore()));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ " was " + grantResults[0]);
        }
    }

    private void setupUI() {
        activity = this;
        setContentView(R.layout.activity_main);
        fingerprintView = new ImageView(this);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addContentView(fingerprintView, layoutParams);
        livenessResultTextView = findViewById(R.id.livenessResult);
        nfiqScoreTextView = findViewById(R.id.nfiqScore);
        startOnyxButton = findViewById(R.id.start_onyx);
        startOnyxButton.setEnabled(false);
        startOnyxButton.bringToFront();
        startOnyxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainApplication.setOnyxResult(null);
                startActivityForResult(new Intent(activity, OnyxActivity.class), ONYX_REQUEST_CODE);
            }
        });
        Button refreshConfigButton = findViewById(R.id.refresh_config);
        refreshConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupOnyx(activity);
                startOnyxButton.setEnabled(false);
            }
        });
    }

    /**
     * This displays an AlertDialog upon receiving an OnyxError, please handle appropriately for
     * your application
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

    private int sgISOMatch(byte[] template1, byte[] template2) {
        JSGFPLib jsgfplib = new JSGFPLib((UsbManager) getSystemService(Context.USB_SERVICE));
        long initError = jsgfplib.Init(SGFDxDeviceName.SG_DEV_AUTO);
        long err;
        boolean[] matched = new boolean[1];
        matched[0] = false;
        // ISO19794-2
        SGISOTemplateInfo sample_info = new SGISOTemplateInfo();
        err = jsgfplib.GetIsoTemplateInfo(template1, sample_info);
        int found_finger = -1;
        for (int i = 0; i < sample_info.TotalSamples; i++) {
            // ISO19794-2
            err = jsgfplib.MatchIsoTemplate(template1,
                    i,
                    template2,
                    0, SGFDxSecurityLevel.SL_NORMAL, matched);
            if (matched[0]) {
                found_finger = sample_info.SampleInfo[i].FingerNumber;
                break;
            }
        }

        Long closeError = jsgfplib.Close();
        return found_finger;
    }

    private File getExternalFile(String filename) {
        final File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), filename);
        return file;
    }
}
