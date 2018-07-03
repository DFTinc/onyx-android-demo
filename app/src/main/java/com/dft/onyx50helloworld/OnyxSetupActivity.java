package com.dft.onyx50helloworld;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

import static com.dft.onyx50helloworld.ValuesUtil.*;

public class OnyxSetupActivity extends AppCompatActivity implements ProviderInstaller.ProviderInstallListener {
    private static final int ONYX_REQUEST_CODE = 1337;
    MainApplication application = new MainApplication();
    private Activity activity;
    private Button startOnyxButton;
    private ImageView rawImageView;
    private ImageView processedImageView;
    private ImageView enhancedImageView;
    private TextView livenessResultTextView;
    private TextView nfiqScoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProviderInstaller.installIfNeededAsync(this, this); // This is needed in order for SSL to work on Android 5.1 devices and lower
        setupUI();
        setupCallbacks();
    }

    private void setupCallbacks() {
        application.setSuccessCallback(new OnyxConfiguration.SuccessCallback() {
            @Override
            public void onSuccess(OnyxResult onyxResult) {
                application.setOnyxResult(onyxResult);
            }
        });

        application.setErrorCallback(new OnyxConfiguration.ErrorCallback() {
            @Override
            public void onError(OnyxError onyxError) {
                Log.e("OnyxError", onyxError.getErrorMessage());
                application.setOnyxError(onyxError);
            }
        });

        application.setOnyxCallback(new OnyxConfiguration.OnyxCallback() {
            @Override
            public void onConfigured(Onyx configuredOnyx) {
                application.setConfiguredOnyx(configuredOnyx);
                startOnyxButton.setEnabled(true);
            }
        });
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
            .setShowLoadingSpinner(getShowLoadingSpinner(this))
            .setUseOnyxLive(getUseOnyxLive(this))
            .setUseFlash(getUseFlash(this))
            .setShouldSegment(getShouldSegment(this))
            .setImageRotation(getImageRotation(this))
            .setReticleOrientation(getReticleOrientation(this))
            .setCropSize(getCropSizeWidth(this), getCropSizeHeight(this))
            .setCropFactor(getCropFactor(this))
            .setReticleScale(getReticleScale(this))
            .setLayoutPreference(getLayoutPreference(this))
            .setSuccessCallback(application.getSuccessCallback())
            .setErrorCallback(application.getErrorCallback())
            .setOnyxCallback(application.getOnyxCallback());
        // Reticle Angle overrides Reticle Orientation so have to set this separately
        if (getReticleAngle(this) != null) {
            onyxConfigurationBuilder.setReticleAngle(getReticleAngle(this));
        }
        // Finally, build the OnyxConfiguration
        onyxConfigurationBuilder.buildOnyxConfiguration();
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
        if (requestCode == ONYX_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                displayResults(application.getOnyxResult());
            }
        }
    }

    private void displayResults(OnyxResult onyxResult) {
        rawImageView.setImageDrawable(null);
        processedImageView.setImageDrawable(null);
        enhancedImageView.setImageDrawable(null);
        if (onyxResult.getRawFingerprintImage() != null) {
            rawImageView.setImageBitmap(onyxResult.getRawFingerprintImage());
        }
        if (onyxResult.getProcessedFingerprintImage() != null) {
            processedImageView.setImageBitmap(onyxResult.getProcessedFingerprintImage());
        }
        if (onyxResult.getEnhancedFingerprintImage() != null) {
            enhancedImageView.setImageBitmap(onyxResult.getEnhancedFingerprintImage());
        }
        if (onyxResult.getMetrics() != null) {
            livenessResultTextView.setText(Double.toString(onyxResult.getMetrics().getLivenessConfidence()));
            nfiqScoreTextView.setText(Integer.toString(onyxResult.getMetrics().getNfiqMetrics().getNfiqScore()));
        }
    }

    private void setupUI() {
        activity = this;
        setContentView(R.layout.activity_main);
        rawImageView = findViewById(R.id.rawImageView);
        processedImageView = findViewById(R.id.processedImageView);
        enhancedImageView = findViewById(R.id.enhancedImageView);
        livenessResultTextView = findViewById(R.id.livenessResult);
        nfiqScoreTextView = findViewById(R.id.nfiqScore);
        startOnyxButton = findViewById(R.id.start_onyx);
        startOnyxButton.setEnabled(false);
        startOnyxButton.bringToFront();
        startOnyxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
}
