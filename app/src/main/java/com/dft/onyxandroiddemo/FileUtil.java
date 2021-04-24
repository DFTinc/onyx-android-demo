package com.dft.onyxandroiddemo;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
                Log.v(TAG, "Permission is granted");
                hasPermission = true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                hasPermission = false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
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
    }

    /**
     * Method to write WSQ byte[] to file on SD card. Note that you must add a
     * WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     * a FileNotFound Exception because you won't have write permission.
     */
    public void writeWSQImage(Activity a, byte[] wsqBytes, String fileName) {
        try {
            File externalFile = new File(a.getExternalFilesDir("wsqs"), fileName + ".wsq");
            FileOutputStream out = new FileOutputStream(externalFile);
            out.write(wsqBytes);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to write PNG byte[] to file on SD card. Note that you must add a
     * WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     * a FileNotFound Exception because you won't have write permission.
     */
    public void writePNGImage(Activity a, Bitmap bitmap, String fileName) {
        try {
            FileOutputStream out = a.openFileOutput(fileName + ".png", Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to add image or byte[] to the camera gallery
     */
    @Nullable
    public Uri saveImage(@NonNull final Context context, final Bitmap bitmap,
                           final byte[] wsqBytes,
                           @NonNull final String displayName) throws IOException {
        final String relativeLocation = Environment.DIRECTORY_DCIM;

        final ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        if (wsqBytes != null) {
            String newDisplayName = displayName + ".wsq";
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, newDisplayName);
        }
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);

        final ContentResolver resolver = context.getContentResolver();

        OutputStream stream = null;
        Uri uri = null;

        try {
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, contentValues);

            if (uri == null) {
                throw new IOException("Failed to create new MediaStore record.");
            }

            stream = resolver.openOutputStream(uri);

            if (stream == null) {
                throw new IOException("Failed to get output stream.");
            }
            if (bitmap != null) {
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                    throw new IOException("Failed to save bitmap.");
                }
            }
            if (wsqBytes != null) {
                stream.write(wsqBytes);
            }
        } catch (IOException e) {
            if (uri != null) {
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(uri, null, null);
            }

            throw e;
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return uri;
    }
}
