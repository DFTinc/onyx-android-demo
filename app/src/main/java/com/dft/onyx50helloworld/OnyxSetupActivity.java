package com.dft.onyx50helloworld;

import android.app.Activity;
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
import com.dft.onyxcamera.config.remoteconfig.RemoteConfigSharedPrefs;

import static com.dft.onyx50helloworld.ValuesUtil.*;

public class OnyxSetupActivity extends AppCompatActivity {
    private static final int ONYX_REQUEST_CODE = 1337;
    MainApplication application = new MainApplication();
    private Activity activity;
    private Button startOnyxButton;
    private ImageView rawImageView;
    private ImageView processedImageView;
    private ImageView enhancedImageView;
    private TextView livenessResultTextView;
    private TextView nfiqScoreTextView;
    private TextView focusScoreTextView;
    private TextView mlpScoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            focusScoreTextView.setText(Double.toString(Math.round(onyxResult.getMetrics().getFocusQuality() * 100.0) / 100.0));
            mlpScoreTextView.setText(Double.toString(Math.round(onyxResult.getMetrics().getNfiqMetrics().getMlpScore() * 100.0) / 100.0));
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
        focusScoreTextView = findViewById(R.id.focusScore);
        mlpScoreTextView = findViewById(R.id.mlpScore);
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
}
