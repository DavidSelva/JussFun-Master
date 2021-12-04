package com.app.jussfun.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();

    private FileUtil() {

    }

    public static File getDiskCacheDir(Context context) {
        File cacheDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cacheDir = context.getExternalCacheDir();//sdcard/Android/data/${application package}/cache
        } else {
            cacheDir = context.getCacheDir();//data/data/${application package}/cache
        }
        return cacheDir;
    }


    public static void updateLastModified(File file) throws IOException {
        if (file.exists()) {
            boolean isModified = file.setLastModified(System.currentTimeMillis()); //某些设备上setLastModified()会失效
            if (!isModified) {
                //ugly modify
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                long length = raf.length();
                raf.setLength(length + 1);
                raf.setLength(length);
                raf.close();
            }
        }
    }

    public static void cleanDir(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] listFile = file.listFiles();
            for (int i = 0; i < listFile.length; i++) {
                cleanDir(listFile[i]);
                listFile[i].delete();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean saveMediaToGallery(@NonNull Context context,
                                             @NonNull File inFile) {
        return true;
    }

    public static boolean copyFile(@NonNull FileInputStream inputStream,
                                    @NonNull FileOutputStream outputStream) {

        try (BufferedInputStream is = new BufferedInputStream(inputStream); BufferedOutputStream os = new BufferedOutputStream(outputStream)) {
            byte[] buff = new byte[1024];
            int read;
            while ((read = is.read(buff)) != -1) {
                os.write(buff, 0, read);
            }
            os.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean copyFile(@NonNull InputStream inputStream,
                                   @NonNull FileOutputStream outputStream) {

        try (BufferedInputStream is = new BufferedInputStream(inputStream); BufferedOutputStream os = new BufferedOutputStream(outputStream)) {
            byte[] buff = new byte[1024];
            int read;
            while ((read = is.read(buff)) != -1) {
                os.write(buff, 0, read);
            }
            os.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;        // min pixels
    private static final int DEFAULT_MIN_HEIGHT_QUALITY = 400;        // min pixels

    private static int minWidthQuality = DEFAULT_MIN_WIDTH_QUALITY;
    private static int minHeightQuality = DEFAULT_MIN_HEIGHT_QUALITY;

    /**
     * Loads a bitmap and avoids using too much memory loading big images (e.g.: 2560*1920)
     */
    public static Bitmap decodeBitmap(Context context, Uri theUri) {
        Bitmap outputBitmap = null;
        AssetFileDescriptor fileDescriptor = null;

        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");

            // Get size of bitmap file
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, boundsOptions);

            // Get desired sample size. Note that these must be powers-of-two.
            int[] sampleSizes = new int[]{8, 4, 2, 1};
            int selectedSampleSize = 1; // 1 by default (original image)

            for (int sampleSize : sampleSizes) {
                selectedSampleSize = sampleSize;
                int targetWidth = boundsOptions.outWidth / sampleSize;
                int targetHeight = boundsOptions.outHeight / sampleSize;
                if (targetWidth >= minWidthQuality && targetHeight >= minHeightQuality) {
                    break;
                }
            }

            // Decode bitmap at desired size
            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = selectedSampleSize;
            outputBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, decodeOptions);
            if (outputBitmap != null) {
                Log.i(TAG, "Loaded image with sample size " + decodeOptions.inSampleSize + "\t\t"
                        + "Bitmap width: " + outputBitmap.getWidth()
                        + "\theight: " + outputBitmap.getHeight());
            }
            fileDescriptor.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputBitmap;
    }

    public static File createImageFile(@NonNull Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
//        currentPhotoFile = image;
        return image;
    }

}
