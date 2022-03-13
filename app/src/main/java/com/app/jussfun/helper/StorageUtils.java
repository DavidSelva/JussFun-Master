package com.app.jussfun.helper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.app.jussfun.BuildConfig;
import com.app.jussfun.R;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.FileUtil;
import com.app.jussfun.utils.Logging;
import com.app.jussfun.utils.apachecommons.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StorageUtils {
    public static final String IMG_SENT_PATH = "Images/Sent/";
    public static final String IMG_PROFILE_PATH = "Images/Profile/";
    public static final String IMG_HOME_PATH = "Images/";
    public static final String IMG_BANNER_PATH = "Images/Banner/";
    public static final String IMG_THUMBNAIL_PATH = "Images/.thumbnails/";
    public static final String VIDEO_CACHE_DIR = "video-cache";
    public static final String TAG_CACHE = "cache";
    private static final String TAG = StorageUtils.class.getSimpleName();
    private static StorageUtils mInstance;
    private final Context mContext;
    private final String appDirectory;

    public StorageUtils(Context context) {
        this.mContext = context;
        appDirectory = "/" + context.getString(R.string.app_name);
    }

    public static StorageUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StorageUtils(context);
        }
        return mInstance;
    }

    public Uri saveToSDCard(Bitmap bitmap, String from, String fileName) {
        fileName = getFileName(fileName);

        String path = getPath(from);

        ContentResolver resolver = mContext.getContentResolver();
        ContentValues contentValues = new ContentValues();
        Uri outUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.TITLE, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + path);
            contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
            contentValues.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);

           /* if (Objects.equals(from, Constants.TAG_SENT) && !containsNoMedia(Environment.DIRECTORY_PICTURES + path)) {
                makeNoMedia(Environment.DIRECTORY_PICTURES + path);
            }*/

            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            outUri = resolver.insert(collection, contentValues);

            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                InputStream inputStream = new ByteArrayInputStream(stream.toByteArray());

                ParcelFileDescriptor pfd = resolver.openFileDescriptor(outUri, "w");
                boolean saved = FileUtil.copyFile(inputStream, new FileOutputStream(pfd.getFileDescriptor()));
                Log.d(TAG, "saveToSDCard: " + outUri.getPath());
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                Log.d(TAG, "saveToSDCard: " + e.toString());
            }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                contentValues.clear();
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(outUri, contentValues, null, null);
            }
            return outUri;
        } else {
            File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            File folder = new File(sdcard.getAbsoluteFile(), path);
            if (from.equals(Constants.TAG_SENT)) {
                if (!folder.exists()) folder.mkdirs();
                File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                try {
                    if (!noMediaFile.exists()) {
                        noMediaFile.createNewFile();
                    }
                } catch (IOException e) {
                    //   Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                folder.mkdirs();
            }

            File file = new File(folder.getAbsoluteFile(), fileName);
            if (file.exists())
                file.delete();

            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                refreshGallery(file);
                return createImageUri(from, file.getAbsolutePath());
            } catch (Exception e) {
                //     Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public Uri getImageUri(String from, String fileName) {
        fileName = getFileName(fileName);
        Uri imageUri = null;
        String path = getPath(from);
        ContentResolver resolver = mContext.getContentResolver();
        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final String selection;
        final String[] selectionArgs;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " +
                    MediaStore.MediaColumns.RELATIVE_PATH + "=?";
            selectionArgs = new String[]{fileName, Environment.DIRECTORY_PICTURES + path};
        } else {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + path;
            selection = MediaStore.MediaColumns.DATA + "=?";
            selectionArgs = new String[]{path + fileName};
        }
        Log.d(TAG, "getImageUri: " + selectionArgs);
        try (Cursor c = resolver.query(collection, null, selection, selectionArgs, null)) {
            if (c.moveToFirst()) {
                imageUri = ContentUris.withAppendedId(collection, c.getLong(c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)));
                //  Timber.d("Check file: exists %s %s", fileName, imageUri);
            } else {
                //   Timber.d("Check file: not found %s", (Object) selectionArgs);
            }
        } catch (Exception e) {
            //  Timber.e(e);
        }
        return imageUri;
    }

    public Uri createImageUri(String from, String filePath) {
        Uri imageUri = getImageUri(from, getFileName(filePath));
        if (imageUri == null) {
            // create a new uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            } else {
                ContentResolver resolver = mContext.getContentResolver();
                ContentValues values = new ContentValues();

                final String fileName = getFileName(filePath);
                final String path = getPath(from);
                Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                values.put(MediaStore.MediaColumns.TITLE, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
                values.put(MediaStore.MediaColumns.DATA, filePath);

                imageUri = resolver.insert(collection, values);
            }
        }
        return imageUri;
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1);
        }
        return imgSplit;
    }

    public void refreshGallery(File file) {

        try {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(file);
            scanIntent.setData(contentUri);
            mContext.sendBroadcast(scanIntent);
            MediaScannerConnection.scanFile(mContext, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Logging.i(TAG, "Finished scanning " + file.getAbsolutePath());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String saveThumbNail(Bitmap bitmap, String filename) {
        String stored = "";

        String path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Sent";
        File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File folder = new File(sdcard.getAbsoluteFile(), path);
        folder.mkdirs();

        File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
        /* if (!nomediaFile.exists()) {
             nomediaFile.createNewFile();
         }*/

        File file = new File(folder.getAbsoluteFile(), filename);
        if (file.exists())
            return file.getPath();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "saveThumbNail: " + file.getAbsolutePath());
        return file.getPath();
    }

    public boolean saveToGallery(Bitmap bitmap, @NonNull String name, String from) throws IOException {
        Log.d(TAG, "onActivityResult: " + bitmap);
        boolean saved;
        OutputStream fos;
        File image = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = mContext.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.TITLE, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES +
                    getPath(from));
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
            Log.d(TAG, "saveToGallery: " + fos.toString());
        } else {
            String imagesDir;
            if (from != null && from.equals("QRCode")) {
                imagesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).toString() + getPath(from);
            } else {
                imagesDir = Environment.getExternalStorageDirectory().toString() + File.separator
                        + getChildDir("");
            }
            File file = new File(imagesDir);
            boolean isDir = false;
            if (!file.exists()) {
                isDir = file.mkdirs();
            }
            image = new File(imagesDir, name);
            if (!image.exists()) {
                image.createNewFile();
            }
            fos = new FileOutputStream(image);
        }
        saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        if (saved && image != null) {
            refreshGallery(image);
        }
        Log.d(TAG, "saveToGallery: " + saved);
        return saved;
    }

    public boolean checkIfImageExists(String from, String imageName) {
       /* if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q)
            return getImage(from, imageName) != null;
         else*/
        return getImageUri(from, imageName) != null;
    }

    public boolean checkIfImageExistsBelowAndroid11(String from, String imageName) {
        return getImage(from, imageName) != null;
    }

    public File getImage(String from, String imageName) {
        File mediaImage = null;
        File root;
        try {

            root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            //    File myDir = new File(root);
            if (!root.exists())
                return null;

            String path = "";
            switch (from) {
                case Constants.TAG_SENT:
                case Constants.TAG_THUMBNAIL:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Sent/";
                    break;
                case "profile":
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Profile/";
                    break;

                case Constants.TAG_AUDIO_SENT:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                    break;

                case Constants.TAG_AUDIO_RECEIVE:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                    break;

                default:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/";
                    break;
            }
            String st = root.getPath() + path + FilenameUtils.getName(imageName);
            if (!st.contains(".jpg"))
                mediaImage = new File(root.getPath() + path + FilenameUtils.getName(imageName) + ".jpg");
            else
                mediaImage = new File(root.getPath() + path + FilenameUtils.getName(imageName));
            Log.d(TAG, "getImagePath: " + mediaImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public File getRootDir(Context context) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalFilesDirs(context, null)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getFilesDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
//        Log.i(TAG, "getAppDataDir: " + mDataDir);
        return new File(mDataDir.getAbsolutePath());
    }

    public String getChildDir(String from) {
        String path;
        switch (from) {
            case "sent":
                path = appDirectory + IMG_SENT_PATH;
                break;
            case "profile":
                path = appDirectory + IMG_PROFILE_PATH;
                break;
            case "banner":
                path = appDirectory + IMG_BANNER_PATH;
                break;
            default:
                path = appDirectory + IMG_HOME_PATH;
                break;
        }
        return path;
    }

    public File getTempFile(Context context, String fileName) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        Logging.i(TAG, "getTempFile: " + mDataDir);
        return new File(mDataDir.getPath() + File.separator + fileName);
    }

    public File getCacheDir(Context context) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        Logging.i(TAG, "getCacheDir: " + mDataDir);
        return mDataDir;
    }

    public File saveToCacheDir(Bitmap bitmap, String filename) {
        boolean stored = false;
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(mContext)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
            Log.i(TAG, "saveToCacheDir: " + mDataDir);
        } else {
            mDataDir = mContext.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
            Log.i(TAG, "saveToCacheDire: " + mDataDir);
        }

        File file = new File(mDataDir.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;
        Log.i(TAG, "saveToCacheDirec: " + file);

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stored ? file : null;
    }

    public void deleteCacheDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            for (File externalCacheDir : ContextCompat.getExternalCacheDirs(mContext)) {
                if (externalCacheDir.listFiles() != null) {
                    for (File file : externalCacheDir.listFiles()) {
                        if (!file.getName().equals(VIDEO_CACHE_DIR)) {
                            file.delete();
                        }
                    }
                }
            }
        } else {
            File mDataDir = null;
            mDataDir = mContext.getCacheDir();
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    if (!file.getName().equals(VIDEO_CACHE_DIR)) {
                        file.delete();
                    }
                }
        }
    }

    public void clearVideoCache() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            for (File externalCacheDir : ContextCompat.getExternalCacheDirs(mContext)) {
                if (externalCacheDir.listFiles() != null) {
                    for (File file : externalCacheDir.listFiles()) {
                        if (file.getName().equals(VIDEO_CACHE_DIR) && file.isDirectory()) {
                            for (File listFile : file.listFiles()) {
                                listFile.delete();
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            File mDataDir = null;
            mDataDir = mContext.getCacheDir();
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    if (file.getName().equals(VIDEO_CACHE_DIR) && file.isDirectory()) {
                        for (File listFile : file.listFiles()) {
                            listFile.delete();
                        }
                        break;
                    }
                }
        }
    }

    public File getFile(String from, String fileName) {
        File file = null;
        try {
            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String path = getPath(from);
            file = new File(myDir.getPath() + path + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public String getPath(String from) {
        String path = "";
        switch (from) {
            case Constants.TAG_SENT:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Sent/";
                break;
            case "profile":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Profile/";
                break;
            case Constants.TAG_THUMBNAIL:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/.thumbnails/";
                break;
            case Constants.TAG_AUDIO_SENT:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                break;
            case Constants.TAG_AUDIO_RECEIVE:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                break;
            default:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/";
                break;
        }


        return path;
    }

    public Bitmap getBitMapFromUri(Context mContext, Uri imageUri) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.getContentResolver(), imageUri));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public Uri getUriFromFile(Context mContext, File file) {
        Uri uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", file);
        return uri;
    }

    public File createDir(String dirType) {
        getCacheDir(mContext);
        return null;
    }

    public Bitmap getThumbnailFromVideo(File mSrcFile) {
        Bitmap thumb = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                thumb = ThumbnailUtils.createVideoThumbnail(mSrcFile,
                        getBitmapSize(mContext), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            thumb = ThumbnailUtils.createVideoThumbnail(mSrcFile.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
        }
        return thumb;
    }

    public Size getBitmapSize(Context mContext) {
        return new Size(150, 150);
    }

    public Bitmap getFrameAtTime(File mSrcFile, int time) {
        Bitmap thumb = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mSrcFile.getAbsolutePath());
            thumb = retriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } else {
            thumb = ThumbnailUtils.createVideoThumbnail(mSrcFile.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
        }
        Log.d(TAG, "getFrameAtTime: " + mSrcFile + ", " + time);
        Log.d(TAG, "getFrameAtTime: " + (thumb == null));
        return thumb;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public String getMimeTypeOfUri(Context context, Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public String getExtension(Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(mContext.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(getUriFromFile(new File(uri.getPath())).toString());

        }
        Log.d(TAG, "getExtension: " + extension);
        return extension;
    }

    public Uri getUriFromFile(File dir) {
        if (dir != null && dir.exists()) {
            return FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", dir);
        } else {
            return null;
        }
    }

    public File saveToCacheDir(File srcFile, String filename) {
        boolean stored = false;
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(mContext)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = mContext.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }

        File file = new File(mDataDir.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;

        try {
            FileOutputStream out = new FileOutputStream(file);
            InputStream in = new FileInputStream(srcFile);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.flush();
            out.close();
            stored = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "saveToCacheDir: " + (file));
        return stored ? file : null;
    }

    public File saveUriToFile(Context mContext, Uri uri, File destDir, String fileName) {
        if (!destDir.exists()) destDir.mkdirs();
        // Create file path inside app's data dir
        File file = new File(destDir, fileName);
        try {
            InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
            if (inputStream == null)
                return null;
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0)
                outputStream.write(buf, 0, len);
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Log.d(TAG, "saveUriToFile: " + file.getAbsolutePath());
        return file;
    }

}
