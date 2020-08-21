package com.dft.onyx50demo;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.dft.onyx.MatVector;
import com.dft.onyx.core;
import com.dft.onyxcamera.config.OnyxResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class VerifyService extends IntentService implements IdentifyFingerprintCallback {
    private ResultReceiver receiver;
    public static final int IDENTIFY_SUCCESS = 1;
    public static final int IDENTIFY_FAILURE = 2;
    public static final int IDENTIFY_ERROR = 3;
    private ArrayList<Bitmap> processedImages;
    private OnyxResult onyxResult;
    private static IdentifyFingerprintCallback identifyFingerprintCallback;
    private final static String VERIFICATION_URL =
            "https://afis.diamondfortress.com/api/v1/onyx/wsq/identify";

    private final static String START_SESSION_URL =
            "https://cloud-dev.koalavsp.com/kivoxWeb/rest/finger/start-session";
    private final static String VERIFY_MF_URL = "https://cloud-dev.koalavsp.com/kivoxWeb/rest/finger/verify-mf";

    private double[] imageScales = new double[]{0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.1, 1.2, 1.3};

    private double currentImageScale;
    private int wsqScaleCounter;
    private boolean success;
    private String outcome;
    private String speaker;

    final String ENTERPRISE_ID = "enterpriseId";
    final String CONFIGURATION = "configuration";
    final String SESSION = "session";
    final String SPEAKER = "speaker";
    final String RIGHT_INDEX = "right-index";
    final String STATUS = "status";
    final String OUTCOME = "outcome";


    private OkHttpClient client = new OkHttpClient.Builder()
            .build();

    public VerifyService() {
        super("VerifyService");
    }

    private void doProgressiveImagePyramid(Bitmap processedBitmap, double[] imageScales) {
        speaker = MainApplication.getSpeaker();
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
    public void onIdentifyFingerprint(boolean fingerprintIdentified, String outcome) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("matchResult", success);
        // Evaluate result and continue or break loop based on match result
        if (fingerprintIdentified) {
            success = true;
            // TODO: If it gets a hit you can attempt scales of +0.05 and -0.05 to see if they improve
            String message = "Successfully matched.";
            Timber.i(message);
            bundle.putDouble("imageScale", currentImageScale);
            bundle.putString("outcome", outcome);
            receiver.send(IDENTIFY_SUCCESS, bundle);
        }

        if (!success && wsqScaleCounter != imageScales.length) {
            double[] imageScale = new double[]{imageScales[wsqScaleCounter]};
            currentImageScale = imageScales[wsqScaleCounter];
            wsqScaleCounter++;
            doProgressiveImagePyramid(processedImages.get(0), imageScale);
        } else if (wsqScaleCounter == imageScales.length) {
            String message = "Did not get a match.";
            Timber.i(message);
            receiver.send(IDENTIFY_FAILURE, bundle);
        }
    }

    private void doIdentification(byte[] wsq) {
        try {
            String session;
            RequestBody formBody = new FormBody.Builder()
                    .add(ENTERPRISE_ID, getResources().getString(R.string.koala_enterprise_id))
                    .add(CONFIGURATION, "FINGER_4F")
                    .build();

            Request request = new Request.Builder()
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .url(START_SESSION_URL)
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                assert response.body() != null;
                String data = response.body().source().readUtf8();

                if (null != data) {
                    JSONObject jObject = new JSONObject(data);
                    // Get the data object from the status object
                    String status = jObject.getString(STATUS);
                    if (status.equalsIgnoreCase("SUCCESS")) {
                        session = jObject.getString(SESSION);

                        // Make a new verify request using the session id
                        final MediaType MEDIA_TYPE_OCTET_STREAM = MediaType.parse("application/octet-stream");
                        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart(RIGHT_INDEX, "rightIndex.wsq",
                                        RequestBody.create(MEDIA_TYPE_OCTET_STREAM, wsq))
                                .addFormDataPart(SESSION, session)
                                .addFormDataPart(SPEAKER, speaker)
                                .build();

                        Request verifyRequest = new Request.Builder()
                                .header("Content-Type", "multipart/form-data")
                                .url(VERIFY_MF_URL)
                                .post(requestBody).build();
                        Response verifyResponse = client.newCall(verifyRequest).execute();
                        if (verifyResponse.isSuccessful()) {
                            assert verifyResponse.body() != null;
                            String verifyData = verifyResponse.body().source().readUtf8();

                            if (null != verifyData) {
                                JSONObject jsonObject = new JSONObject(verifyData);
                                String verifyOutcome = jsonObject.getString(OUTCOME);
                                if (verifyOutcome.equalsIgnoreCase("ACCEPTED")) {
                                    success = true;
                                }
                                outcome = jsonObject.getString(OUTCOME);
                            }
                        }
                    }
                }
            }
            Timber.d("Scale: " + currentImageScale + ". Success: " + success);
            identifyFingerprintCallback.onIdentifyFingerprint(success, outcome);
        } catch (IOException e) {
            e.printStackTrace();
            Timber.e("IOException while doing JSONAsyncTask");
            receiver.send(IDENTIFY_ERROR, Bundle.EMPTY);
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e("JSONException while doing JSONAsyncTask");
            receiver.send(IDENTIFY_ERROR, Bundle.EMPTY);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("Exception executing client request.");
            receiver.send(IDENTIFY_ERROR, Bundle.EMPTY);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        receiver = intent.getParcelableExtra("receiver");
        Context mContext = this;
        identifyFingerprintCallback = this;
        currentImageScale = 1.0;
        wsqScaleCounter = 0;
        success = false;
        outcome = "";
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
