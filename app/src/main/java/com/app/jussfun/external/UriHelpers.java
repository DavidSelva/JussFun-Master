package com.app.jussfun.external;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.RequiresApi;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.app.jussfun.ui.ChatActivity.rotateImage;

/**
 * Utility functions to support Uri conversion and processing.
 */
public final class UriHelpers {
    private static final String TAG = UriHelpers.class.getSimpleName();

    private UriHelpers() {
    }

    /**
     * Get the file path for a uri. This is a convoluted way to get the path for an Uri created using the
     * StorageAccessFramework. This in no way is the official way to do this but there does not seem to be a better
     * way to do this at this point. It is taken from https://github.com/iPaulPro/aFileChooser.
     *
     * @param context The context of the application
     * @param uri     The uri of the saved file
     * @return The file with path pointing to the saved file. It can return null if we can't resolve the uri properly.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static File getFileForUri(final Context context, final Uri uri, boolean isCamera, String from) {
        String path = null;
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    path = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                // DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris
                        .withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                path = getDataColumn(context, contentUri, null, null, isCamera, from);
            } else if (isMediaDocument(uri)) {
                // MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                path = getDataColumn(context, contentUri, selection, selectionArgs, isCamera, from);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // MediaStore (and general)
            path = getDataColumn(context, uri, null, null, isCamera, from);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // File
            path = uri.getPath();
        }

        if (path != null) {
            return new File(path);
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs, boolean isCamera, String from) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isCamera) {
            return getFilePathForN(uri, context, isCamera, from);
        } else {

            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {
                    column
            };
            String filePath = "";

            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                        null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    Bitmap bitmap = null;
                    int maxWidth = 0, maxHeight = 0;

                    filePath = cursor.getString(column_index);
                    try {
                        bitmap = BitmapFactory.decodeFile(filePath);
                        FileOutputStream out = new FileOutputStream(filePath);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        // PNG is a lossless format, the compression factor (100) is ignored
                    } catch (IOException e) {
                        Log.e(TAG, "getDataColumn: " + e.getMessage());
                    }

                    if (bitmap != null) {
                        if (maxWidth != 0 && maxHeight != 0) {
                            Log.i(TAG, "getDataColumn: " + maxHeight + " " + maxWidth);
                            Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, false);
                        }
                        try {
                            ExifInterface ei = new ExifInterface(filePath);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);

                            switch (orientation) {

                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotateImage(bitmap, 90);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotateImage(bitmap, 180);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotateImage(bitmap, 270);
                                    break;

                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    break;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return cursor.getString(column_index);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
        }
    }

    private static String getFilePathForN(Uri uri, Context context, boolean isCamera, String from) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
               move to the first row in the Cursor, get the data,
               and display it.
          */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        int maxWidth = 0, maxHeight = 0;

        File file;
        file = new File(context.getExternalCacheDir(), name);
        Log.i(TAG, "getFilePathForN: " +file.getAbsolutePath());

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        Log.i(TAG, "getFilePathForN: " + bitmap.getWidth() + " " + bitmap.getHeight());
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                /*OutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // bmp
                inputStream.close();
                outputStream.close();*/
                try {
                    ExifInterface ei = new ExifInterface(file.getAbsolutePath());
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    switch (orientation) {

                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotateImage(bitmap, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotateImage(bitmap, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotateImage(bitmap, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            break;
                    }

                    if (maxWidth != 0 && maxHeight != 0) {
                        /*int Height = bitmap.getHeight();
                        int Width = bitmap.getWidth();
                        Matrix matrix = new Matrix();
                        float scaleWidth = ((float) maxWidth) / Width;
                        float scaleHeight = ((float) maxHeight) / Height;
                        matrix.postScale(scaleWidth, scaleHeight);
                        Bitmap.createBitmap(bitmap, 0, 0, Width, Height, matrix, true);*/
                        Log.i(TAG, "getFilePathForN: " + maxWidth + " " + maxHeight);
                        Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, false);
                        if (file.exists()) file.delete();
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception : " + e.getMessage());
        }
        return file.getPath();
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}