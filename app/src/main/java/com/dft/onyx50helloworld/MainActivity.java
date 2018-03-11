package com.dft.onyx50helloworld;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dft.onyxcamera.config.Onyx;
import com.dft.onyxcamera.config.OnyxConfiguration;
import com.dft.onyxcamera.config.OnyxConfigurationBuilder;
import com.dft.onyxcamera.config.OnyxResult;

import static com.dft.onyx50helloworld.ValuesUtil.*;

public class MainActivity extends AppCompatActivity {
    private Activity activity;
    private Onyx onyx = null;
    private Button startOnyxButton;
    private ImageView rawImageView;
    private ImageView processedImageView;
    private ImageView enhancedImageView;
    private TextView livenessResultTextView;

    @Override
    protected void onResume() {
        super.onResume();
        setupUI();
        setupOnyx(this);
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
            .setSuccessCallback(new OnyxConfiguration.SuccessCallback() {
                @Override
                public void onSuccess(OnyxResult onyxResult) {
                    displayResults(onyxResult);
                }
            })
            .setErrorCallback(new OnyxConfiguration.ErrorCallback() {
                @Override
                public void onError(Error error, String errorMessage, Exception exception) {
                    Log.e("MainActivity", errorMessage);
                }
            })
            .setOnyxCallback(new OnyxConfiguration.OnyxCallback() {

                @Override
                public void onConfigured(Onyx configuredOnyx) {
                    MainApplication application = new MainApplication();
                    application.setConfiguredOnyx(configuredOnyx);
//                    onyx = configuredOnyx;
                    startOnyxButton.setEnabled(true);
                }
            });
        // Reticle Angle overrides Reticle Orientation so have to set this separately
        if (getReticleAngle(this) != null) {
            onyxConfigurationBuilder.setReticleAngle(getReticleAngle(this));
        }
        // Finally, build the OnyxConfiguration
        onyxConfigurationBuilder.buildOnyxConfiguration();
    }

    private void startOnyx() {
        onyx.create(this);
        onyx.capture();
    }

    private void displayResults(OnyxResult onyxResult) {
        rawImageView.setImageDrawable(null);
        processedImageView.setImageDrawable(null);
        enhancedImageView.setImageDrawable(null);
        if (onyxResult.getRawFingerprintBitmap() != null) {
            rawImageView.setImageBitmap(onyxResult.getRawFingerprintBitmap());
        }
        if (onyxResult.getProcessedFingerprintBitmap() != null) {
            processedImageView.setImageBitmap(onyxResult.getProcessedFingerprintBitmap());
        }
        if (onyxResult.getEnhancedFingerprintBitmap() != null) {
            enhancedImageView.setImageBitmap(onyxResult.getEnhancedFingerprintBitmap());
        }
        if (onyxResult.getMetrics() != null) {
//            livenessResultTextView.setText(onyxResult.getMetrics().getLiveness());
        }
    }

    private void setupUI() {
        activity = this;
        setContentView(R.layout.activity_main);
        rawImageView = findViewById(R.id.rawImageView);
        processedImageView = findViewById(R.id.processedImageView);
        enhancedImageView = findViewById(R.id.enhancedImageView);
        startOnyxButton = findViewById(R.id.start_onyx);
        startOnyxButton.setEnabled(false);
        startOnyxButton.bringToFront();
        startOnyxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity, OnyxActivity.class));
//                startOnyx();
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
