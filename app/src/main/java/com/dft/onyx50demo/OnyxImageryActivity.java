package com.dft.onyx50demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dft.onyx.MatVector;
import com.dft.onyx.core;
import com.dft.onyxcamera.config.OnyxResult;
import com.google.android.gms.common.api.internal.IStatusCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class OnyxImageryActivity extends Activity {
    private static final String TAG = OnyxImageryActivity.class.getName();

    private Activity activity;
    private OnyxResult onyxResult;
    private ArrayList<Bitmap> processedImages;



    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_imagery);
        activity = this;
        onyxResult = MainApplication.getOnyxResult();
        if (onyxResult == null) {
            return;
        }
        ImageView rawImage1 = findViewById(R.id.rawImage1);
        ImageView processedImage1 = findViewById(R.id.processedImage1);
        ImageView rawImage2 = findViewById(R.id.rawImage2);
        ImageView processedImage2 = findViewById(R.id.processedImage2);
        ImageView rawImage3 = findViewById(R.id.rawImage3);
        ImageView processedImage3 = findViewById(R.id.processedImage3);
        ImageView rawImage4 = findViewById(R.id.rawImage4);
        ImageView processedImage4 = findViewById(R.id.processedImage4);
        rawImage1.setImageDrawable(null);
        processedImage1.setImageDrawable(null);
        rawImage2.setImageDrawable(null);
        processedImage2.setImageDrawable(null);
        rawImage3.setImageDrawable(null);
        processedImage3.setImageDrawable(null);
        rawImage4.setImageDrawable(null);
        processedImage4.setImageDrawable(null);

        ArrayList<Bitmap> rawImages = onyxResult.getRawFingerprintImages();
        processedImages = onyxResult.getProcessedFingerprintImages();
        if (rawImages != null) {
            for (int i = 0; i < rawImages.size(); i++) {
                switch (i) {
                    case 0:
                        if (rawImages.get(i) != null) {
                            rawImage1.setImageBitmap(rawImages.get(i));
                        }
                        if (processedImages.get(i) != null) {
                            processedImage1.setImageBitmap(processedImages.get(i));
                        }
                        break;
                    case 1:
                        if (rawImages.get(i) != null) {
                            rawImage2.setImageBitmap(rawImages.get(i));
                        }
                        if (processedImages.get(i) != null) {
                            processedImage2.setImageBitmap(processedImages.get(i));
                        }
                        break;
                    case 2:
                        if (rawImages.get(i) != null) {
                            rawImage3.setImageBitmap(rawImages.get(i));
                        }
                        if (processedImages.get(i) != null) {
                            processedImage3.setImageBitmap(processedImages.get(i));
                        }
                        break;
                    case 3:
                        if (rawImages.get(i) != null) {
                            rawImage4.setImageBitmap(rawImages.get(i));
                        }
                        if (processedImages.get(i) != null) {
                            processedImage4.setImageBitmap(processedImages.get(i));
                        }
                        break;
                }
            }
        }

        FileUtil fileUtil = new FileUtil();
        fileUtil.checkExternalMedia(this);
        if (onyxResult.getWsqData() != null && !onyxResult.getWsqData().isEmpty() && fileUtil.getWriteExternalStoragePermission(this)) {
            for (int i = 0; i < onyxResult.getWsqData().size(); i++) {
                fileUtil.writeToSDFile(this, onyxResult.getWsqData().get(i), "wsq" + i);
            }
        }

        Button finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainApplication.setOnyxResult(null);
                startActivity(new Intent(activity, OnyxSetupActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        if (onyxResult.getMetrics().getFocusQuality() > 1) {
            startService(new Intent(this, VerifyService.class));
        } else {
            Timber.e("Focus quality was less than 1.");
        }
    }

}
