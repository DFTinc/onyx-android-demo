package com.dft.onyxandroiddemo.matching;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.dft.onyx.FingerprintTemplate;
import com.dft.onyxandroiddemo.MainApplication;
import com.dft.onyxandroiddemo.R;
import com.dft.onyxcamera.config.OnyxResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

public class EnrollUtil {
    private static final String TAG = "EnrollUtil";
    public final static String ENROLL_FILENAME = "enrolled_template.bin";

    public void createEnrollQuestionDialog(final Context context) {
        final OnyxResult onyxResult = MainApplication.getOnyxResult();
        if (!onyxResult.getFingerprintTemplates().isEmpty() && !onyxResult.getProcessedFingerprintImages().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.enroll_title);
            String enrollQuestion = context.getResources().getString(R.string.enroll_question);
            int nfiqScore = 0;
            if (onyxResult.getMetrics() != null & onyxResult.getMetrics().getNfiqMetrics() != null &&
                    onyxResult.getMetrics().getNfiqMetrics().size() != 0) {
                nfiqScore = onyxResult.getMetrics().getNfiqMetrics().get(0).getNfiqScore();
            }
            builder.setMessage(enrollQuestion + "\n\n" +
                    "(NFIQ is " + (nfiqScore != 0 ? nfiqScore : "not configured.") + ")");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EnrollUtil.this.enrollCurrentTemplate(context, onyxResult.getFingerprintTemplates().get(0));
                    dialog.dismiss();
                }
            });
            final FingerprintTemplate enrolledTemplate = getEnrolledTemplateIfExists(context);
            if (enrolledTemplate != null) {
                builder.setNegativeButton("No, perform match", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        VerifyTask verifyTask = new VerifyTask(context);
                        verifyTask.execute(new VerifyPayload(enrolledTemplate, onyxResult.getProcessedFingerprintImages().get(0)));
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
        } catch (IOException e) {
            Timber.e(e);
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
            } catch (IOException | ClassNotFoundException e) {
                Timber.e(e);
            }
        }
        return fingerprintTemplate;
    }
}
