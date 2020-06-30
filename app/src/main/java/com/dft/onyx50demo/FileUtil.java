package com.dft.onyx50demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

//import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    private static final String TAG = "FileUtil";

    /**
     * Method to check if external storage has write permission
     */
    public boolean getWriteExternalStoragePermission(Activity activity) {
        boolean hasPermission;
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                hasPermission = true;
            } else {
                Log.v(TAG,"Permission is revoked");
//                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                hasPermission = false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            hasPermission = true;
        }
        return hasPermission;
    }

    /**
     * Method to check whether external media available and writable. This is adapted from
     * http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
     */
    public void checkExternalMedia(Activity a) {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        Toast.makeText(a, "External Media: readable="
                + mExternalStorageAvailable + " writable=" + mExternalStorageWriteable,
                Toast.LENGTH_LONG).show();
    }

    /**
     * Method to write WSQ byte[] to file on SD card. Note that you must add a
     * WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     * a FileNotFound Exception because you won't have write permission.
     */
    public void writeToSDFile(Activity a, byte[] wsqBytes, String fileName) {
        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal
        File root = android.os.Environment.getExternalStorageDirectory();
        Toast.makeText(a, "External file system root: " + root, Toast.LENGTH_LONG).show();

        File dir = new File(root.getAbsolutePath() + "/wsq");
        dir.mkdirs();
        File file = new File(dir, fileName + ".wsq");

        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(wsqBytes);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(a, "File written to " + file, Toast.LENGTH_LONG).show();
    }
}
