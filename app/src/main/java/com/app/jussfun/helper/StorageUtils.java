package com.app.jussfun.helper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.app.jussfun.BuildConfig;
import com.app.jussfun.R;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.FileUtil;
import com.app.jussfun.utils.Logging;
import com.app.jussfun.utils.apachecommons.FilenameUtils;

import java.io.BufferedInputStream;
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
    /*  private static final String TAG = StorageUtils.class.getSimpleName();
      private static StorageUtils mInstance;
      private final Context context;
      private String appDirectory;
      public static final String IMG_SENT_PATH = "Images/Sent/";
      public static final String IMG_PROFILE_PATH = "Images/Profile/";
      public static final String IMG_HOME_PATH = "Images/";
      public static final String IMG_BANNER_PATH = "Images/Banner/";
      public static final String IMG_THUMBNAIL_PATH = "Images/.thumbnails/";

      public StorageUtils(Context context) {
          this.context = context;
          appDirectory = "/" + context.getString(R.string.app_name);
      }

      public static synchronized StorageUtils getInstance(Context context) {
          if (mInstance == null) {
              mInstance = new StorageUtils(context);
          }
          return mInstance;
      }

      private String getFileName(String url) {
          String imgSplit = url;
          int endIndex = imgSplit.lastIndexOf("/");
          if (endIndex != -1) {
              imgSplit = imgSplit.substring(endIndex + 1);
          }
          return imgSplit;
      }

      @RequiresApi(api = Build.VERSION_CODES.Q)
      private boolean containsNoMedia(@NonNull final String dir) {
          ContentResolver resolver = context.getContentResolver();
          final String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " +
                  MediaStore.MediaColumns.TITLE + "=?";
          final String[] selectionArgs = new String[]{dir, MediaStore.MEDIA_IGNORE_FILENAME};
          final Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
          try (Cursor c = resolver.query(collection,
                  null, selection, selectionArgs,     null)) {
              if (c.moveToFirst()) {
                  return true;
              } else {
                  return false;
              }
          } catch (Exception e) {
              //   Timber.e(e);
              return false;
          }
      }


      @RequiresApi(api = Build.VERSION_CODES.Q)
      private void makeNoMedia(@NonNull final String dir) {
          ContentResolver resolver = context.getContentResolver();
          ContentValues contentValues = new ContentValues();

          contentValues.put(MediaStore.MediaColumns.TITLE, MediaStore.MEDIA_IGNORE_FILENAME);
          contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MEDIA_IGNORE_FILENAME);
          contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, dir);
          contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");

          final Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

          final Uri target = resolver.insert(collection, contentValues);

          try (ParcelFileDescriptor pfd = new ParcelFileDescriptor(resolver.openFileDescriptor(target, "w"));
               FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor())) {
              outputStream.flush();
              //   Timber.d("Gallery: .nomedia created at " + dir);
              Log.d(TAG, "makeNoMedia: "+dir);
          } catch (IOException e) {
              // Timber.e(e);
              Log.d(TAG, "makeNoMedia: "+e.toString());

          }
      }

      public Uri saveToSDCard(Bitmap bitmap, String from, String fileName) {
          fileName = getFileName(fileName);

          String path = getPath(from);

          ContentResolver resolver = context.getContentResolver();
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

              if (Objects.equals(from, Constants.TAG_SENT) && !containsNoMedia(Environment.DIRECTORY_PICTURES + path)) {
                  makeNoMedia(Environment.DIRECTORY_PICTURES + path);
              }

              Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

              outUri = resolver.insert(collection, contentValues);

              try {
                  ByteArrayOutputStream stream = new ByteArrayOutputStream();
                  bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                  InputStream inputStream = new ByteArrayInputStream(stream.toByteArray());

                  ParcelFileDescriptor pfd = resolver.openFileDescriptor(outUri, "w");
                  boolean saved = FileUtil.copyFile(inputStream, new FileOutputStream(pfd.getFileDescriptor()));
                  //Timber.i("Save: %s", outUri.getPath());
              } catch (IOException | NullPointerException e) {
                  e.printStackTrace();
                  //   Timber.e("Save: failed %s", e.getMessage());
              }

              if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                  contentValues.clear();
                  contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                  resolver.update(outUri, contentValues, null, null);
              }
              return outUri;
          } else {
              // for API 28 and below (in progress)
             *//* final String directory = Environment.getExternalStorageDirectory().getAbsolutePath();
            File targetDir = new File(directory);

            File folder = new File(targetDir, path);
            if (from.equals(Constants.TAG_SENT)) {
                if (!folder.exists()) {
                    if (folder.mkdirs()) {
                        Timber.i("Directory created: %s", targetDir.getAbsolutePath());
                    }
                    File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                    try {
                        if (!noMediaFile.exists()) {
                            noMediaFile.createNewFile();
                        }
                    } catch (IOException e) {
                        Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                folder.mkdirs();
            }

            File createdMedia = new File(folder.getAbsoluteFile(), fileName);
            if (createdMedia.exists()) createdMedia.delete();*//*

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
                    Logging.e(TAG, "saveToSDCard: " + e.getMessage());
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
                Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public Uri createImageUri(String from, String filePath) {
        Uri imageUri = getImageUri(from, getFileName(filePath));
        if (imageUri == null) {
            // create a new uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            } else {
                ContentResolver resolver = context.getContentResolver();
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

    public Uri getImageUri(String from, String fileName) {
        fileName = getFileName(fileName);
        Uri imageUri = null;
        String path = getPath(from);

        ContentResolver resolver = context.getContentResolver();

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
        try (Cursor c = resolver.query(collection, null, selection, selectionArgs, null)) {
            if (c.moveToFirst()) {
                imageUri = ContentUris.withAppendedId(collection, c.getLong(c.getColumnIndex(MediaStore.MediaColumns._ID)));
                //  Timber.d("Check file: exists %s %s", fileName, imageUri);
            } else {
                //   Timber.d("Check file: not found %s", (Object) selectionArgs);
            }
        } catch (Exception e) {
            //  Timber.e(e);
        }
        return imageUri;
    }

*//*
    public File saveToSDCard(Bitmap bitmap, String from, String fileName) {
        File sdcard = Environment.getExternalStorageDirectory();

        String path = "";
        switch (from) {
            case Constants.TAG_SENT:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent";
                break;
            case Constants.TAG_RECEIVED:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images";
                break;
            case "profile":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile";
                break;
            case Constants.TAG_THUMBNAIL:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                break;
            default:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images";
                break;
        }

        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (from.equals(Constants.TAG_SENT)) {
            if (!folder.exists()) folder.mkdirs();
            File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
            try {
                if (!noMediaFile.exists()) {
                    noMediaFile.createNewFile();
                }
            } catch (IOException e) {
                Logging.e(TAG, "saveToSDCard: " + e.getMessage());
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
        } catch (Exception e) {
            Logging.e(TAG, "saveToSDCard: " + e.getMessage());
            e.printStackTrace();
        }
        return file;
    }
*//*

    public void refreshGallery(File file) {

        try {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(file);
            scanIntent.setData(contentUri);
            context.sendBroadcast(scanIntent);
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
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

        String path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails";
        File sdcard = Environment.getExternalStorageDirectory();
        File folder = new File(sdcard.getAbsoluteFile(), path);
        folder.mkdirs();

        File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
        try {
            if (!nomediaFile.exists()) {
                nomediaFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        return file.getPath();
    }

    public boolean saveToGallery(Bitmap bitmap, @NonNull String name) throws IOException {
        boolean saved;
        OutputStream fos;
        File image = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.TITLE, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES +
                    File.separator + context.getString(R.string.app_name) + File.separator);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).toString() + File.separator + context.getString(R.string.app_name) +
                    File.separator + context.getString(R.string.my_qr_code);
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
        return saved;
    }

    public boolean checkIfImageExists(String from, String imageName) {
        File file = getImage(from, imageName);
        return file != null && file.exists();
    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public File getImage(String from, String imageName) {
        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String path = "";
            switch (from) {
                case Constants.TAG_SENT:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
                    break;
                case "profile":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
                    break;
                case Constants.TAG_THUMBNAIL:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                    break;
                case Constants.TAG_AUDIO_SENT:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                    break;

                case Constants.TAG_AUDIO_RECEIVE:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                    break;
                default:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
                    break;
            }
            mediaImage = new File(myDir.getPath() + path + imageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public File getFileSavePath(String from) {
        return new File(getRootDir(context) + getChildDir(from));
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

    public File saveToAppDir(Bitmap bitmap, String from, String filename) {
        boolean stored = false;
        File sdcard = getRootDir(context);
        String path = getChildDir(from);

        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (!folder.exists())
            folder.mkdir();

        File file = new File(folder.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;

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

    public File saveToCacheDir(Bitmap bitmap, String filename) {
        boolean stored = false;
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

        File file = new File(mDataDir.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;

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
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    file.delete();
                }
        } else {
            mDataDir = context.getCacheDir();
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    file.delete();
                }
        }
    }


    public boolean checkIfFileExists(String from, String imageName) {
        File file = getImage(from, imageName);
        return file != null && file.exists();
    }

    public File getFile(String from, String fileName) {
        File file = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String path = "";
            switch (from) {
                case Constants.TAG_SENT:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
                    break;
                case "profile":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
                    break;
                case Constants.TAG_THUMBNAIL:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                    break;
                case Constants.TAG_AUDIO_SENT:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                    break;
                case Constants.TAG_AUDIO_RECEIVE:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                    break;
                default:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
                    break;
            }
            file = new File(myDir.getPath() + path + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
    public byte[] getFileBytes(String filePath) {
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    public boolean ifFolderExists(String from){
        String path = "";
        switch (from) {
            case Constants.TAG_SENT:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
                break;
            case "profile":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
                break;
            case Constants.TAG_THUMBNAIL:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                break;
            case Constants.TAG_AUDIO_SENT:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                break;
            case Constants.TAG_AUDIO_RECEIVE:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                break;
            default:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
                break;
        }

        File sdcard = Environment.getExternalStorageDirectory();
        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (!folder.exists()) {
            folder.mkdirs();
            return true;
        }
        return true;
    }

    public String getPath(String from) {
        String path = "";
        switch (from) {
            case Constants.TAG_SENT:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
                break;
            case "profile":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
                break;
            case Constants.TAG_THUMBNAIL:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                break;
            case Constants.TAG_AUDIO_SENT:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                break;
            case Constants.TAG_AUDIO_RECEIVE:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                break;
            default:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
                break;
        }

        File sdcard = Environment.getExternalStorageDirectory();
        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
        try {
            if (!noMediaFile.exists()) {
                noMediaFile.createNewFile();
            }
        } catch (IOException e) {
            Logging.e(TAG, "getPath: " + e.getMessage());
            e.printStackTrace();
        }
        return folder.getPath();
    }

    public int getMediaDuration(Context context, Uri fileUri) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        Log.d(TAG, "getMediaDuration: "+fileUri);
        mmr.setDataSource(context, fileUri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        try {
            int duration = Integer.parseInt(durationStr);
            return duration;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }
*/
    private static final String TAG = StorageUtils.class.getSimpleName();
    private static StorageUtils mInstance;
    private final Context context;
    private final String appDirectory;

    public StorageUtils(Context context) {
        this.context = context;
        appDirectory = "/" + context.getString(R.string.app_name);
    }

    public static StorageUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StorageUtils(context);
        }
        return mInstance;
    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void makeNoMedia(@NonNull final String dir) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        contentValues.put(MediaStore.MediaColumns.TITLE, MediaStore.MEDIA_IGNORE_FILENAME);
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MEDIA_IGNORE_FILENAME);
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, dir);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        final Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        final Uri target = resolver.insert(collection, contentValues);

        try (ParcelFileDescriptor pfd = new ParcelFileDescriptor(resolver.openFileDescriptor(target, "w"));
             FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor())) {
            outputStream.flush();
            //   Timber.d("Gallery: .nomedia created at " + dir);
            Log.d(TAG, "makeNoMedia: " + dir);
        } catch (IOException e) {
            // Timber.e(e);
            Log.d(TAG, "makeNoMedia: " + e.toString());

        }
    }

    public Uri saveToSDCard(Bitmap bitmap, String from, String fileName) {
        fileName = getFileName(fileName);

        String path = getPath(from);

        ContentResolver resolver = context.getContentResolver();
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
        ContentResolver resolver = context.getContentResolver();
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
                imageUri = ContentUris.withAppendedId(collection, c.getLong(c.getColumnIndex(MediaStore.MediaColumns._ID)));
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
                ContentResolver resolver = context.getContentResolver();
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
            context.sendBroadcast(scanIntent);
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
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

        String path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent";
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
            ContentResolver resolver = context.getContentResolver();
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
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
                    break;
                case "profile":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
                    break;

                case Constants.TAG_AUDIO_SENT:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                    break;

                case Constants.TAG_AUDIO_RECEIVE:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                    break;

                default:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
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
        Logging.i(TAG, "getTempFile: " + mDataDir);
        return mDataDir;
    }

    public File saveToCacheDir(Bitmap bitmap, String filename) {
        boolean stored = false;
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
            Log.i(TAG, "saveToCacheDir: " + mDataDir);
        } else {
            mDataDir = context.getCacheDir();
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
            for (File externalCacheDir : ContextCompat.getExternalCacheDirs(context)) {
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
            mDataDir = context.getCacheDir();
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
            for (File externalCacheDir : ContextCompat.getExternalCacheDirs(context)) {
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
            mDataDir = context.getCacheDir();
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
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
                break;
            case "profile":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
                break;
            case Constants.TAG_THUMBNAIL:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                break;
            case Constants.TAG_AUDIO_SENT:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                break;
            case Constants.TAG_AUDIO_RECEIVE:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                break;
            default:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
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

    public Uri getUriFromFile(Context mContext, File file) {
        Uri uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", file);
        return uri;
    }
}
