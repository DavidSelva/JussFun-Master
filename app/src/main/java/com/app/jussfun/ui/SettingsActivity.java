package com.app.jussfun.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.databinding.ActivitySettingsBinding;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.helper.AppWebSocket;
import com.app.jussfun.helper.BannerAdUtils;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.rating.AppRate;
import com.app.jussfun.helper.rating.OnClickButtonListener;
import com.app.jussfun.helper.rating.StoreType;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.SharedPref;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends BaseFragmentActivity implements View.OnClickListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private ActivitySettingsBinding binding;
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView();

        // addon for interstitial advertisements
//        App.loadAds(this);
        Log.i(TAG, "onCreate: " + getString(R.string.general_settings));


    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

        binding.toolBarLay.txtTitle.setText(getString(R.string.settings));

        if (LocaleManager.isRTL()) {
            binding.toolBarLay.btnBack.setRotation(180);
        } else {
            binding.toolBarLay.btnBack.setRotation(0);
        }
        binding.privacyLay.setVisibility(View.GONE);
        loadAd();
        binding.toolBarLay.btnBack.setOnClickListener(this);
        binding.settingsLay.setOnClickListener(this);
        binding.privacyLay.setOnClickListener(this);
        binding.inviteLay.setOnClickListener(this);
        binding.helpLay.setOnClickListener(this);
        binding.logoutLay.setOnClickListener(this);
        binding.languageLay.setOnClickListener(this);
        binding.qrGenLay.setOnClickListener(this);
        binding.blockLay.setOnClickListener(this);
        binding.ratingLay.setOnClickListener(this);
    }

    private void loadAd() {
        BannerAdUtils.getInstance(this).loadAd(TAG, binding.adView);
    }


    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.settingsLay:
                Intent settings = new Intent(getApplicationContext(), GeneralSettingsActivity.class);
                settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(settings);
                break;
            case R.id.privacyLay:
                Intent privacy = new Intent(getApplicationContext(), TermsActivity.class);
                privacy.putExtra(Constants.TAG_FROM, "privacy");
                privacy.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(privacy);
                break;
            case R.id.inviteLay:
                Intent invite = new Intent(getApplicationContext(), EarnGemsActivity.class);
                invite.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(invite);
                break;
            case R.id.helpLay:
                Intent help = new Intent(getApplicationContext(), HelpActivity.class);
                help.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(help);
                break;
            case R.id.qrGenLay: {
                Intent intent = new Intent(getApplicationContext(), QRCodeGenerateActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
            break;
            case R.id.blockLay: {
                Intent intent = new Intent(getApplicationContext(), BlockedUsersActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
            break;
            case R.id.languageLay:
                Intent Language = new Intent(getApplicationContext(), LanguageActivity.class);
                Language.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(Language);
                break;
            case R.id.logoutLay:
                openLogoutDialog();
                break;
            case R.id.ratingLay:
                App.preventMultipleClick(binding.ratingLay);
                AppRate.with(SettingsActivity.this)
                        .setStoreType(StoreType.GOOGLEPLAY) //default is Google, other option is Amazon
                        .setInstallDays(3) // default 10, 0 means install day.
                        .setLaunchTimes(10) // default 10 times.
                        .setRemindInterval(2) // default 1 day.
                        .setShowLaterButton(false) // default true.
                        .setDebug(false) // default false.
                        .setCancelable(false) // default false.
                        .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                            @Override
                            public void onClickButton(int which) {

                            }
                        })
                        .setTitle(R.string.rate_dialog_title)
                        .setMessage(R.string.rate_dialog_message)
                        .setTextLater(R.string.rate_dialog_cancel)
                        .setTextNever(R.string.rate_dialog_no)
                        .setTextRateNow(R.string.rate_dialog_ok)
                        .monitor();

                AppRate.showRateDialogIfMeetsConditions(this);
                break;
        }
    }

    private void openLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this, R.style.ThemeAlertDialog);
        builder.setMessage(getString(R.string.really_want_logout));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                removeDeviceID(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_light);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_light);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);

    }

    private void removeDeviceID(String deviceId) {
        Call<HashMap<String, String>> call = apiInterface.pushSignOut(deviceId);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                overridePendingTransition(0, 0);
                GetSet.reset();
                SharedPref.clearAll();
                DBHelper.getInstance(SettingsActivity.this).clearDB();
                AppWebSocket.getInstance(SettingsActivity.this).disconnect();
                Intent logout = new Intent(getApplicationContext(), LoginActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logout);
                finish();
                App.makeToast(getString(R.string.logged_out_successfully));
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }
}
