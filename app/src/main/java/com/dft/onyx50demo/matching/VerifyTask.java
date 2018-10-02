package com.dft.onyx50demo.matching;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dft.onyx.core;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class VerifyTask extends AsyncTask<VerifyPayload, Void, Float> {
    private Exception mException = null;
    private Context mContext = null;

    public VerifyTask(Context context) {
        mContext = context;
    }

    @Override
    protected Float doInBackground(VerifyPayload... payloads) {
        try {
            Mat probe = new Mat();
            Utils.bitmapToMat(payloads[0].getProbe(), probe);

            Imgproc.cvtColor(probe, probe, Imgproc.COLOR_RGB2GRAY);
            return core.pyramidVerify(payloads[0].getReference(), probe);
        } catch (Exception e) {
            mException = e;
        }
        return -1.0f;
    }

    @Override
    protected void onPostExecute(Float matchScore) {
        if (mException != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(mException.getMessage())
                    .setCancelable(false)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setTitle("Verification error");

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Toast.makeText(mContext, createMatchString(matchScore), Toast.LENGTH_SHORT).show();
        }
    }

    private String createMatchString(double score) {
        String matchString;
        if (score < 0.1) {
            matchString = new String("Failed");
        } else {
            matchString = new String("Match");
        }
        matchString += " (Score = " + String.format("%.2f", score) + ")";

        return matchString;
    }

}
