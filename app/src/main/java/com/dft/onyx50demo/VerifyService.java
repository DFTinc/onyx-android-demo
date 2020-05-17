package com.dft.onyx50demo;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.View;

import com.dft.onyx.MatVector;
import com.dft.onyx.core;
import com.dft.onyxcamera.config.OnyxResult;

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

public class VerifyService extends IntentService implements IdentifyFingerprintCallback {
    private Context mContext;
    private ArrayList<Bitmap> processedImages;
    private OnyxResult onyxResult;
    private static IdentifyFingerprintCallback identifyFingerprintCallback;
    private final static String VERIFICATION_URL =
            "https://afis.diamondfortress.com/api/v1/onyx/wsq/identify";

    private double[] imageScales = new double[]{0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.1, 1.2, 1.3};

    private double currentImageScale;
    private int wsqScaleCounter;
    private boolean success;
    private float score;

    final String API_KEY = "api_key";
    final String WSQ_IMAGE = "wsqImage";
    final String SUCCESS = "success";
    final String FINGERPRINT_ID = "fingerprintId";
    final String SCORE = "score";
    final String MESSAGE = "message";

    private OkHttpClient client = new OkHttpClient.Builder()
            .build();

    public VerifyService() {
        super("VerifyService");
    }

    private void doProgressiveImagePyramid(Bitmap processedBitmap, double[] imageScales) {
        byte[] scaledWSQ;
        if (imageScales[0] == 1.0) {
            // Submit at 1.0 scale first
            currentImageScale = 1.0;
            scaledWSQ = onyxResult.getWsqData().get(0);
            doIdentification(scaledWSQ);
        } else {
            Mat mat = new Mat();
            Utils.bitmapToMat(processedBitmap, mat);
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY); // ensure image is grayscale
            MatVector vector = core.pyramidImage(mat, imageScales);
            scaledWSQ = core.matToWsq(vector.get(0));
            mat.release();
            // Send scaledWSQ to server for matching
            doIdentification(scaledWSQ);
        }
    }

    @Override
    public void onIdentifyFingerprint(boolean fingerprintIdentified, float score) {
        // Evaluate result and continue or break loop based on match result
        if (fingerprintIdentified) {
            success = true;
            // TODO: If it gets a hit you can attempt scales of +0.05 and -0.05 to see if they improve
            Timber.i("Successfully matched.");
        }

        if (!success && wsqScaleCounter != imageScales.length) {
            double[] imageScale = new double[]{imageScales[wsqScaleCounter]};
            currentImageScale = imageScales[wsqScaleCounter];
            wsqScaleCounter++;
            doProgressiveImagePyramid(processedImages.get(0), imageScale);
        } else if (wsqScaleCounter == imageScales.length) {
            Timber.i("Did not get a match.");
        }
    }

    private void doIdentification(byte[] wsq) {
        try {
            String base64EncodedWSQ = Base64.encodeToString(wsq, 0).trim();
            RequestBody formBody = new FormBody.Builder()
                    .add(API_KEY, "e25c23f1fbef3e54c9553ae185e20e45")
                    .add(WSQ_IMAGE, base64EncodedWSQ)
                    .build();

            Request request = new Request.Builder()
                    .url(VERIFICATION_URL)
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                assert response.body() != null;
                String data = response.body().source().readUtf8();

                if (null != data) {
                    JSONObject jObject = new JSONObject(data);
                    // Get the data object from the status object
                    boolean success = jObject.getBoolean(SUCCESS);
                    if (success) {
                        score = jObject.getInt(SCORE);
                    }

                }
            }
            Timber.i("Scale: " + currentImageScale + ". Success: " + success);
            identifyFingerprintCallback.onIdentifyFingerprint(success, score);
        } catch (IOException e) {
            e.printStackTrace();
            Timber.e("IOException while doing JSONAsyncTask");
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e("JSONException while doing JSONAsyncTask");
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("Exception executing client request.");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mContext = this;
        identifyFingerprintCallback = this;
        currentImageScale = 1.0;
        wsqScaleCounter = 0;
        success = false;
        score = 0.0f;
        onyxResult = MainApplication.getOnyxResult();
        if (onyxResult == null) {
            return;
        }
        processedImages = onyxResult.getProcessedFingerprintImages();

        // Do the progressive image pyramiding
        if (processedImages.get(0) != null) {
            currentImageScale = 1.0;
            doProgressiveImagePyramid(processedImages.get(0), new double[]{1.0});
        }
    }
}
