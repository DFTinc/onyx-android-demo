package com.dft.onyx50demo.matching;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;

import com.dft.onyx.FingerprintTemplate;
import com.dft.onyx50demo.MainApplication;
import com.dft.onyx50demo.R;
import com.dft.onyxcamera.config.OnyxResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import static android.content.Context.MODE_PRIVATE;

public class EnrollUtil {
    private static final String TAG = "EnrollUtil";
    public final static String ENROLL_FILENAME = "enrolled_template.bin";

    public void createEnrollQuestionDialog(final Context context) {
        final OnyxResult onyxResult = MainApplication.getOnyxResult();
        if (onyxResult.getFingerprintTemplate() != null && onyxResult.getProcessedFingerprintImage() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.enroll_title);
            String enrollQuestion = context.getResources().getString(R.string.enroll_question);
            builder.setMessage(enrollQuestion + "\n\n" +
                    "(NFIQ is " + onyxResult.getMetrics().getNfiqMetrics().getNfiqScore() + ")");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EnrollUtil.this.enrollCurrentTemplate(context, onyxResult.getFingerprintTemplate());
                    dialog.dismiss();
                }
            });
            final FingerprintTemplate enrolledTemplate = getEnrolledTemplateIfExists(context);
            if (enrolledTemplate != null) {
                builder.setNegativeButton("No, perform match", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (enrolledTemplate != null) {
                            VerifyTask verifyTask = new VerifyTask(context);
                            verifyTask.execute(new VerifyPayload(enrolledTemplate, onyxResult.getProcessedFingerprintImage()));
                        }
                    }
                });
            }

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void enrollCurrentTemplate(Context context, FingerprintTemplate fingerprintTemplate) {
        try {
            FileOutputStream enrollStream = context.openFileOutput(ENROLL_FILENAME, MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(enrollStream);
            oos.writeObject(fingerprintTemplate);
            oos.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * This method gets the fingerprint template if it exists.
     */
    public FingerprintTemplate getEnrolledTemplateIfExists(Context context) {
        FingerprintTemplate fingerprintTemplate = null;
        File enrolledFile = context.getFileStreamPath(ENROLL_FILENAME);
        if (enrolledFile.exists()) {
            try {
                FileInputStream enrollStream = context.openFileInput(ENROLL_FILENAME);
                ObjectInputStream ois = new ObjectInputStream(enrollStream);
                fingerprintTemplate = (FingerprintTemplate) ois.readObject();
            } catch (FileNotFoundException e) {
                Log.e(TAG, e.getMessage());
            } catch (StreamCorruptedException e) {
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (ClassNotFoundException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return fingerprintTemplate;
    }
}
