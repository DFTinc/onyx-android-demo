package com.dft.onyxandroiddemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dft.onyx.NfiqMetrics;
import com.dft.onyxandroiddemo.matching.EnrollUtil;
import com.dft.onyxcamera.config.OnyxResult;
import com.dft.onyxcamera.util.UploadMatchResult;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class OnyxImageryActivity extends Activity {

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

        final OnyxResult onyxResult = MainApplication.getOnyxResult();
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
        if (onyxResult.getMetrics().getLivenessConfidence() != 0.00) {
            livenessTextView.setText(String.format("Liveness confidence: %.2f", onyxResult.getMetrics().getLivenessConfidence()));
        }
        if (onyxResult.getMetrics() != null && onyxResult.getMetrics().getNfiqMetrics() != null) {
            List<NfiqMetrics> nfiqMetricsList = onyxResult.getMetrics().getNfiqMetrics();
            for (int i = 0; i < nfiqMetricsList.size(); i++) {
                nfiqTextViews.get(i).setText(nfiqMetricsList.get(i) == null ? "" : "NFIQ: " + String.valueOf(nfiqMetricsList.get(i).getNfiqScore()));
            }
        }
        if (rawImages != null) {
            for (int i = 0; i < rawImages.size(); i++) {
                if (rawImages.get(i) != null) {
                    rawImageViews.get(i).setImageBitmap(rawImages.get(i));
                }
            }
        }
        if (processedImages != null) {
            for (int i = 0; i < processedImages.size(); i++) {
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

        final FileUtil fileUtil = new FileUtil();
        Button saveImagesButton = findViewById(R.id.saveImages);
        saveImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentTimeMillis = String.valueOf(System.currentTimeMillis());
                saveImages(onyxResult, fileUtil, currentTimeMillis);
            }
        });

        Button emailButton = findViewById(R.id.emailResults);
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentTimeMillis = String.valueOf(System.currentTimeMillis());
                ArrayList<Uri> uriList = (ArrayList<Uri>) saveImages(onyxResult, fileUtil, currentTimeMillis);

                Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
                SimpleDateFormat DateFor = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

                i.setType("plain/text");
                i.putExtra(Intent.EXTRA_EMAIL, new String[] {""});
                i.putExtra(Intent.EXTRA_SUBJECT, "Onyx results: " + DateFor.format(new Date()));
                i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);

                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(activity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainApplication.setOnyxResult(null);
                startActivity(new Intent(activity, OnyxSetupActivity.class));
            }
        });

        new EnrollUtil().createEnrollQuestionDialog(this);
    }

    private List<Uri> saveImages(OnyxResult onyxResult, FileUtil fileUtil, String currentTimeMillis) {
        ArrayList<Uri> uriList = new ArrayList<>();
        try {
            if (onyxResult.getRawFingerprintImages() != null && !onyxResult
                    .getRawFingerprintImages().isEmpty()) {
                for (int i = 0; i < onyxResult.getRawFingerprintImages().size(); i++) {
                    uriList.add(fileUtil.saveImage(activity, onyxResult.getRawFingerprintImages()
                                    .get(i),
                            null,
                            "raw" + i + "_" + currentTimeMillis));
                }
            }
            if (onyxResult.getProcessedFingerprintImages() != null && !onyxResult
                    .getProcessedFingerprintImages().isEmpty()) {
                for (int i = 0; i < onyxResult.getProcessedFingerprintImages().size(); i++) {
                    uriList.add(fileUtil.saveImage(activity, onyxResult.getProcessedFingerprintImages()
                                    .get(i),
                            null,
                            "processed" + i + "_" + currentTimeMillis));
                }
            }
            if (onyxResult.getWsqData() != null && !onyxResult.getWsqData().isEmpty()) {
                for (int i = 0; i < onyxResult.getWsqData().size(); i++) {
                    uriList.add(fileUtil.saveImage(activity,
                            null,
                            onyxResult.getWsqData().get(i),
                            "wsq" + i + "_" + currentTimeMillis));
                }
            }
        } catch (IOException e) {
            Timber.e("Exception saving imagery.");
            e.printStackTrace();
        }
        return uriList;
    }

}
