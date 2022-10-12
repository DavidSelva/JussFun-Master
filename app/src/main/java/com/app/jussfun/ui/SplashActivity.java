package com.app.jussfun.ui;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.helper.AppWebSocket;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.model.AddDeviceRequest;
import com.app.jussfun.model.AppDefaultResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.DeviceTokenPref;
import com.app.jussfun.utils.Logging;
import com.app.jussfun.utils.SharedPref;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends BaseFragmentActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private ApiInterface apiInterface;
    DBHelper dbHelper;
    ImageView splashImage;
    private ExecutorService executor;
    private AppUtils appUtils;
    private boolean isFromFeed = false;
    private String feedId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_splash);
        SharedPref.putBoolean(SharedPref.IS_APP_OPENED, false);
        appUtils = new AppUtils(this);
        StorageUtils storageUtils = new StorageUtils(this);
        storageUtils.deleteCacheDir();
        executor = Executors.newSingleThreadExecutor();
        appUtils.hideKeyboard(this);
        initView();
//        printHashKey();
    }

    private void initView() {
        //   splashImage = findViewById(R.id.splashImage);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        AppUtils.callerList = new ArrayList<>();
        dbHelper = DBHelper.getInstance(this);
        //   splashImage.setImageDrawable(getDrawable(R.mipmap.ic_splash));
    }

    private void checkUser() {
        SharedPref.putBoolean(SharedPref.IS_APP_OPENED, true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!VideoCallActivity.isInCall)
                            checkUserIsLoggedIn();
                    }
                }, 2000);
            }
        });
    }

    private void checkUserIsLoggedIn() {
       /* if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!checkOverLayPermission()) {
                requestCameraPermission();
            }
        }*/
        if (SharedPref.getBoolean("isLogged", false)) {
            if (!AppUtils.callerList.contains(GetSet.getUserId())) {
                AppUtils.callerList.add(GetSet.getUserId());
            }
            dbHelper.updateChatsOnline();
            dbHelper.updateChatsTyping();
            GetSet.setLogged(true);
            GetSet.setUserId(SharedPref.getString(SharedPref.USER_ID, null));
            GetSet.setLoginId(SharedPref.getString(SharedPref.LOGIN_ID, null));
            GetSet.setAuthToken(SharedPref.getString(SharedPref.AUTH_TOKEN, null));
            GetSet.setName(SharedPref.getString(SharedPref.NAME, null));
            GetSet.setUserImage(SharedPref.getString(SharedPref.USER_IMAGE, null));
            GetSet.setAge(SharedPref.getString(SharedPref.AGE, null));
            GetSet.setDob(SharedPref.getString(SharedPref.DOB, null));
            GetSet.setGender(SharedPref.getString(SharedPref.GENDER, null));
            GetSet.setLocation(SharedPref.getString(SharedPref.LOCATION, null));
            GetSet.setGems(SharedPref.getLong(SharedPref.GEMS, 0));
            GetSet.setGifts(SharedPref.getLong(SharedPref.GIFTS, 0));
            GetSet.setVideos(SharedPref.getLong(SharedPref.VIDEOS, 0));
            GetSet.setFollowersCount(SharedPref.getString(SharedPref.FOLLOWERS_COUNT, null));
            GetSet.setFollowingCount(SharedPref.getString(SharedPref.FOLLOWINGS_COUNT, null));
            GetSet.setInterestsCount(SharedPref.getInt(SharedPref.INTEREST_COUNT, 0));
            GetSet.setFriendsCount(SharedPref.getInt(SharedPref.FRIENDS_COUNT, 0));
            GetSet.setUnlocksLeft(SharedPref.getInt(SharedPref.UNLOCKS_LEFT, 0));
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            if (isFromFeed) {
                intent.putExtra(Constants.TAG_FEED_ID, feedId);
            }
            startActivity(intent);
            AppWebSocket.mInstance = null;
            AppWebSocket.getInstance(SplashActivity.this);
        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        SplashActivity.this.finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkOverLayPermission() {
        //Android M Or Over
        return (!Settings.canDrawOverlays(this));
    }

    private void requestCameraPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, Constants.OVERLAY_REQUEST_CODE);
    }

    private void checkIsFromLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        Log.d(TAG, "onSuccess: " + deepLink);
                        if (deepLink != null) {
                            if (deepLink.getQueryParameter("referal_code") != null) {
                                String referalCode = deepLink.getQueryParameter("referal_code");
                                SharedPref.putString(SharedPref.REFERAL_CODE, referalCode);
                            } else if (deepLink.getQueryParameter("feed_id") != null) {
                                isFromFeed = true;
                                feedId = deepLink.getQueryParameter("feed_id");
                            } else {
                                SharedPref.putString(SharedPref.REFERAL_CODE, "");
                            }
                        } else {
                            SharedPref.putString(SharedPref.REFERAL_CODE, "");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Logging.e(TAG, "onSuccess: " + e.getMessage());
            }
        });
    }

    private void checkForUpdate() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetAppDefaultTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getAppDefaultData() {
        if (NetworkReceiver.isConnected()) {
            Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID);
            call.enqueue(new Callback<AppDefaultResponse>() {
                @Override
                public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                    if (response.isSuccessful()) {
                        AppDefaultResponse defaultData = response.body();
                        if (("" + defaultData.getStatus()).equals(Constants.TAG_TRUE)) {
                            AdminData.resetData();
                            AdminData.freeGems = defaultData.getFreeGems();
                            AdminData.giftList = defaultData.getGifts();
                            AdminData.giftsDetails = defaultData.getGiftsDetails();
                            AdminData.primeDetails = defaultData.getPrimeDetails();
                            AdminData.reportList = defaultData.getReports();
                            /*Add first item as Select all location filter*/
                            AdminData.locationList = new ArrayList<>();
                            AdminData.locationList.add(getString(R.string.select_all));
                            AdminData.locationList.addAll(defaultData.getLocations());
                            AdminData.membershipList = defaultData.getMembershipPackages();
                            AdminData.filterGems = defaultData.getFilterGems();
                            AdminData.filterOptions = defaultData.getFilterOptions();
                            AdminData.inviteCredits = defaultData.getInviteCredits();
                            AdminData.showAds = defaultData.getShowAds();
                            AdminData.googleAdsId = defaultData.getGoogleAdsClient();
                            AdminData.showVideoAd = defaultData.getVideoAds();
                            AdminData.welcomeMessage = defaultData.getWelcomeMessage();
                            AdminData.contactEmail = defaultData.getContactEmail();
                            AdminData.showMoneyConversion = defaultData.getShowMoneyConversion();
                            AdminData.videoCallsGems = defaultData.getVideoCalls();
                            if (AdminData.membershipList != null && AdminData.membershipList.size() > 0) {
                                SharedPref.putString(SharedPref.DEFAULT_SUBS_SKU, AdminData.membershipList.get(0).getSubsTitle());
                            } else {
                                SharedPref.putString(SharedPref.DEFAULT_SUBS_SKU, Constants.DEFAULT_SUBS_SKU);
                            }
                            AdminData.videoAdsClient = defaultData.getVideoAdsClient();
                            AdminData.videoAdsDuration = defaultData.getVideoAdsDuration();
                            AdminData.gemConversion = defaultData.getGemConversion();
                            AdminData.giftConversion = defaultData.getGiftConversion();
                            AdminData.feedTitle = defaultData.getFeedTitle();
                            AdminData.feedDescription = defaultData.getFeedDescription();
                            checkIsFromLink();
                            try {
                                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                Double versionCode = (double) pInfo.versionCode;
                                if (defaultData.getAndroidVersionCode() != null && defaultData.getAndroidVersionCode() > versionCode) {
                                    openUpdateDialog(defaultData.isAndroidForceUpdate());
                                } else {
                                    checkUser();
                                }
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                                checkUserIsLoggedIn();
                            }

                        }
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                    App.makeToast(getString(R.string.something_went_wrong));
                    call.cancel();
                }
            });
        }
    }

    private void addDeviceID() {
        final String token = DeviceTokenPref.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        AddDeviceRequest request = new AddDeviceRequest();
        request.setUserId(GetSet.getUserId());
        request.setDeviceToken(token);
        request.setDeviceType(Constants.TAG_DEVICE_TYPE);
        request.setDeviceId(deviceId);
        request.setDeviceModel(AppUtils.getDeviceName());

        Call<Map<String, String>> call3 = apiInterface.pushSignIn(request);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {

            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void openUpdateDialog(boolean forceUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        String title = getString(R.string.app_update);
        String message = getString(R.string.app_name) + " " + getString(R.string.app_update_description);

        builder.setTitle(title);
        //Setting message manually and performing action on button click
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                        checkUserIsLoggedIn();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        alert.show();
        // and disable the button to start with
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(forceUpdate ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    public void printHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Logging.d(TAG, "printHashKey: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            Logging.e(TAG, "printHashKey" + e.getMessage());
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetAppDefaultTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            getAppDefaultData();
            if (SharedPref.getBoolean("isLogged", false)) {
                addDeviceID();
            }
            return null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.DEVICE_LOCK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                checkForUpdate();
            } else {
                App.makeToast(getString(R.string.unable_to_verify));
                finish();
            }
        } else if (requestCode == Constants.REQUEST_APP_UPDATE_IMMEDIATE || requestCode == Constants.REQUEST_APP_UPDATE_FLEXIBLE) {
            if (resultCode != RESULT_OK) {
                Logging.d(TAG, "Update flow failed! Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
                if (requestCode == Constants.REQUEST_APP_UPDATE_FLEXIBLE && resultCode == RESULT_CANCELED) {
                    checkUser();
                } else {
                    if (!VideoCallActivity.isInCall)
                        checkUserIsLoggedIn();
                }
            } else {
                if (!VideoCallActivity.isInCall)
                    checkUserIsLoggedIn();
            }
        } else {
            if (!VideoCallActivity.isInCall)
                checkUserIsLoggedIn();

        }
    }
}
