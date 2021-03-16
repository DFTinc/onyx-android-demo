package com.dft.onyxandroiddemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dft.onyx.NfiqMetrics;
import com.dft.onyxcamera.config.OnyxResult;
import com.dft.onyxcamera.util.UploadMatchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.parseInt;

public class OnyxImageryActivity extends Activity {
    private static final String TAG = OnyxImageryActivity.class.getName();

    private Activity activity;

    @Override
    public void onResume() {
        super.onResume();
        setContentView(R.layout.activity_imagery);
        activity = this;
        ImageView rawImage1 = findViewById(R.id.rawImage1);
        ImageView processedImage1 = findViewById(R.id.processedImage1);
        ImageView rawImage2 = findViewById(R.id.rawImage2);
        ImageView processedImage2 = findViewById(R.id.processedImage2);
        ImageView rawImage3 = findViewById(R.id.rawImage3);
        ImageView processedImage3 = findViewById(R.id.processedImage3);
        ImageView rawImage4 = findViewById(R.id.rawImage4);
        ImageView processedImage4 = findViewById(R.id.processedImage4);
        TextView livenessTextView = findViewById(R.id.livenessText);
        TextView nfiqTextView1 = findViewById(R.id.nfiqText1);
        TextView nfiqTextView2 = findViewById(R.id.nfiqText2);
        TextView nfiqTextView3 = findViewById(R.id.nfiqText3);
        TextView nfiqTextView4 = findViewById(R.id.nfiqText4);
        rawImage1.setImageDrawable(null);
        processedImage1.setImageDrawable(null);
        rawImage2.setImageDrawable(null);
        processedImage2.setImageDrawable(null);
        rawImage3.setImageDrawable(null);
        processedImage3.setImageDrawable(null);
        rawImage4.setImageDrawable(null);
        processedImage4.setImageDrawable(null);

        OnyxResult onyxResult = MainApplication.getOnyxResult();
        if (onyxResult == null) {
            return;
        }

        ArrayList<Bitmap> rawImages = onyxResult.getRawFingerprintImages();
        ArrayList<Bitmap> processedImages = onyxResult.getProcessedFingerprintImages();
        ArrayList<TextView> nfiqTextViews = new ArrayList<>(Arrays.asList(
                nfiqTextView1,
                nfiqTextView2,
                nfiqTextView3,
                nfiqTextView4
        ));
        ArrayList<ImageView> rawImageViews = new ArrayList<>(Arrays.asList(
                rawImage1,
                rawImage2,
                rawImage3,
                rawImage4
        ));
        ArrayList<ImageView> processedImageViews = new ArrayList<>(Arrays.asList(
                processedImage1,
                processedImage2,
                processedImage3,
                processedImage4
        ));
        livenessTextView.setText(String.format("Liveness: %.2f", onyxResult.getMetrics().getLivenessConfidence()));
        if (onyxResult.getMetrics() != null && onyxResult.getMetrics().getNfiqMetrics() != null) {
            List<NfiqMetrics> nfiqMetricsList = onyxResult.getMetrics().getNfiqMetrics();
            for (int i = 0; i < nfiqMetricsList.size(); i++) {
                nfiqTextViews.get(i).setText(nfiqMetricsList.get(i) == null ? "" : "NFIQ: " + String.valueOf(nfiqMetricsList.get(i).getNfiqScore()));
            }
        }
        if (rawImages != null && processedImages != null) {
            for (int i = 0; i < rawImages.size(); i++) {
                if (rawImages.get(i) != null) {
                    rawImageViews.get(i).setImageBitmap(rawImages.get(i));
                }

                if (processedImages.get(i) != null) {
                    processedImageViews.get(i).setImageBitmap(processedImages.get(i));
                }
            }
        }

        if (onyxResult.getMetrics() != null && onyxResult.getMetrics().getTransactionId() != null) {
            String transactionId = onyxResult.getMetrics().getTransactionId();
            UploadMatchResult uploadMatchResult = new UploadMatchResult(this,
                    transactionId,
                    false,
                    "INES");
            uploadMatchResult.uploadMatchResult();
        }

        FileUtil fileUtil = new FileUtil();
        if (onyxResult.getProcessedFingerprintImages() != null && !onyxResult.getProcessedFingerprintImages().isEmpty()) {
            for (int i = 0; i < onyxResult.getProcessedFingerprintImages().size(); i++) {
                fileUtil.writePNGImage(this, onyxResult.getProcessedFingerprintImages().get(i), "finger" + i);
            }
        }

        Button finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainApplication.setOnyxResult(null);
                startActivity(new Intent(activity, OnyxSetupActivity.class));
            }
        });
    }

}
