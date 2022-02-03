package com.app.jussfun.helper;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.app.jussfun.R;


public class PermissionsUtils {

    private static final String TAG = PermissionsUtils.class.getSimpleName();
    public static final int CALL_PERMISSION_REQUEST_CODE = 101;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 106;
    public static final int CONTACT_PERMISSION_REQUEST_CODE = 110;
    public static final int RECORD_PERMISSION_REQUEST_CODE = 111;
    public static final int CAMERA_RECORD_PERMISSION_REQUEST_CODE = 112;

    public static final String[] HOMEPAGE_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public static final String[] HOMEPAGE_V11_PERMISSION = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public static final String[] HOMEPAGE_V10_PERMISSION = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public static final String[] CAMERA_STORAGE_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    public static final String[] LOCATION_PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static final String[] CALL_PERMISSION = {
            Manifest.permission.CALL_PHONE
    };

    public static final String[] GETLOCATION_PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static final String[] CAMERA_PERMISSION = {
            Manifest.permission.CAMERA
    };


    public static final String[] READ_WRITE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] READ_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    public static final String[] WRITE_STORAGE_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] READ_STORAGE_PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    //
    public static boolean checkPermissionsArray(String[] permissions, Context mContext) {
        for (int i = 0; i < permissions.length; i++) {
            String check = permissions[i];
            if (!checkPermissions(check, mContext)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check a single permission is it has been verified
     *
     * @param permission
     * @return
     */
    public static boolean checkPermissions(String permission, Context mContext) {
        Log.d(TAG, "checkPermissions: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(mContext, permission);
        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            return false;
        } else {
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
        }
    }

    public static boolean checkStoragePermission(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            boolean granted = false;
            for (String permission : PermissionsUtils.READ_WRITE_PERMISSIONS) {
                int permissionRequest = ActivityCompat.checkSelfPermission(mContext, permission);
                if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                } else {
                    granted = true;
                }
            }
            return granted;
        }
    }


    public static void openPermissionDialog(Context mContext, OnOkCancelClickListener callback, String description) {
        new AlertDialog.Builder(mContext)
                .setMessage(description)
                .setPositiveButton(mContext.getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onOkClicked("");
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onCancelClicked("");
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }
}
