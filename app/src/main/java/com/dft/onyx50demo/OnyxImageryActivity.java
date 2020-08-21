package com.dft.onyx50demo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dft.onyxcamera.config.OnyxResult;

import org.opencv.core.Mat;

import java.util.ArrayList;

import timber.log.Timber;

public class OnyxImageryActivity extends Activity {
    private static final String TAG = OnyxImageryActivity.class.getName();

    private Activity activity;
    private OnyxResult onyxResult;
    private ArrayList<Bitmap> processedImages;

    private static double imageScale;
    private static boolean success;
    private static String outcome;

    private static ProgressBar spinner;

    private String speaker;

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_imagery);
        activity = this;
        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);
        onyxResult = MainApplication.getOnyxResult();
        if (onyxResult == null) {
            return;
        }
        speaker = MainApplication.getSpeaker();
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
                Intent mStartActivity = new Intent(activity, SpeakerActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(activity, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
            }
        });

        if (onyxResult.getMetrics().getFocusQuality() > 1) {
            Timber.e("Focus quality is: " + onyxResult.getMetrics().getFocusQuality());
            IdentifyResultReceiver identifyResultReceiver = new IdentifyResultReceiver(new Handler());
            Intent startIntent =new Intent(this, VerifyService.class);
            startIntent.putExtra("receiver", identifyResultReceiver);
            startIntent.putExtra("speaker", speaker);
            startService(startIntent);
        } else {
            Timber.e("Focus quality was less than 1.");
            Toast.makeText(getApplicationContext(), "Focus quality was less than 1, please try again.",
                    Toast.LENGTH_LONG).show();
            new FocusQualityTooLowDialogFragment().show(activity.getFragmentManager(), TAG);
        }
    }

    private class IdentifyResultReceiver extends ResultReceiver {

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public IdentifyResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            success = resultData.getBoolean("matchResult");
            switch (resultCode) {
                case VerifyService.IDENTIFY_ERROR:
                    Toast.makeText(getApplicationContext(), "Error in Identification",
                            Toast.LENGTH_SHORT).show();
                    break;

                case VerifyService.IDENTIFY_SUCCESS:
                    imageScale = resultData.getDouble("imageScale");
                    outcome = resultData.getString("outcome");
                    new IdentifyFingerprintDialogFragment().show(activity.getFragmentManager(), TAG);
                    break;
                case VerifyService.IDENTIFY_FAILURE:
                    new IdentifyFingerprintDialogFragment().show(activity.getFragmentManager(), TAG);
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

    public static class IdentifyFingerprintDialogFragment extends DialogFragment {
        public static final String TAG = "FingerprintCaptureFailureDialogFragment";
        Activity mActivity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mActivity = getActivity();
            spinner.setVisibility(View.GONE);

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            String message;
            if (success) {
                message = "Verification of fingerprint was a success.  Outcome was " + outcome + ". Image scale was " + imageScale + ".";
            } else {
                message = "Verification of fingerprint was not a success.";
            }
            builder.setTitle(getResources().getString(R.string.identify_fingerprint_result))
                    .setMessage(message)
                    .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            mActivity.finish();
                        }
                    });
            return builder.create();
        }
    }

    public static class FocusQualityTooLowDialogFragment extends DialogFragment {
        public static final String TAG = "FocusQualityDialogFragment";
        Activity mActivity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mActivity = getActivity();
            spinner.setVisibility(View.GONE);

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            String message = "Focus quality was < 1.0.";
            builder.setTitle("Focus quality was too low.")
                    .setMessage(message)
                    .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            mActivity.finish();
                        }
                    });
            return builder.create();
        }
    }

}
