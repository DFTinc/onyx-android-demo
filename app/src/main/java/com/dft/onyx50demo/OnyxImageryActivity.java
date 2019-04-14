package com.dft.onyx50demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.dft.onyxcamera.config.OnyxResult;

import java.util.ArrayList;

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
        rawImage1.setImageDrawable(null);
        processedImage1.setImageDrawable(null);
        rawImage2.setImageDrawable(null);
        processedImage2.setImageDrawable(null);
        rawImage3.setImageDrawable(null);
        processedImage3.setImageDrawable(null);
        rawImage4.setImageDrawable(null);
        processedImage4.setImageDrawable(null);

        OnyxResult onyxResult = MainApplication.getOnyxResult();
        if(onyxResult == null) {
            return;
        }

        ArrayList<Bitmap> rawImages = onyxResult.getRawFingerprintImages();
        ArrayList<Bitmap> processedImages = onyxResult.getProcessedFingerprintImages();
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

        FileUtil fileUtil = new FileUtil();
        fileUtil.checkExternalMedia(this);
        if (!onyxResult.getWsqData().isEmpty() && fileUtil.getWriteExternalStoragePermission(this)) {
            for (int i = 0; i < onyxResult.getWsqData().size(); i++) {
                fileUtil.writeToSDFile(this, onyxResult.getWsqData().get(i), "wsq" + i);
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
