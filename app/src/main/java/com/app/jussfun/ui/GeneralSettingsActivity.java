package com.app.jussfun.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.helper.BannerAdUtils;
import com.app.jussfun.helper.AppWebSocket;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.ProfileRequest;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.SharedPref;
import com.google.android.gms.ads.AdView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeneralSettingsActivity extends BaseFragmentActivity implements AppWebSocket.WebSocketChannelEvents {

    private static final String TAG = GeneralSettingsActivity.class.getSimpleName();
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btnPrivateChat)
    Switch btnPrivateChat;
    @BindView(R.id.privateChatLay)
    RelativeLayout privateChatLay;
    @BindView(R.id.btnDnD)
    Switch btnDnD;
    @BindView(R.id.dndLay)
    RelativeLayout dndLay;
    @BindView(R.id.btnFollow)
    Switch btnFollow;
    @BindView(R.id.followLay)
    RelativeLayout followLay;
    @BindView(R.id.txtFollow)
    TextView txtFollow;
    @BindView(R.id.btnHideMyAge)
    Switch btnHideMyAge;
    @BindView(R.id.ageLay)
    RelativeLayout ageLay;
    @BindView(R.id.btnNoOneContact)
    Switch btnNoOneContact;
    @BindView(R.id.contactLay)
    RelativeLayout contactLay;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.btnSecurity)
    Switch btnSecurity;
    @BindView(R.id.securityLay)
    RelativeLayout securityLay;
    private ApiInterface apiInterface;
    private AppUtils appUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_settings);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        initView();
    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);
        txtTitle.setText(getString(R.string.general_settings));
        setSettings();
        loadAd();

        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }

        btnSecurity.setChecked(SharedPref.getBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false));
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            BannerAdUtils.getInstance(this).loadAd(TAG, adView);
        }
    }


    private void setSettings() {
        if (GetSet.getPremiumMember().equals(Constants.TAG_FALSE)) {
            btnHideMyAge.setClickable(false);
            btnNoOneContact.setClickable(false);
        } else {
            btnHideMyAge.setClickable(true);
            btnNoOneContact.setClickable(true);
        }
        btnPrivateChat.setChecked(GetSet.getChatNotification().equals(Constants.TAG_TRUE));
        btnDnD.setChecked(GetSet.getShowNotification().equals(Constants.TAG_TRUE));

        txtFollow.setText(getString(R.string.notify_if_anyone_interest));
        btnFollow.setChecked(GetSet.getInterestNotification());

        btnHideMyAge.setChecked(GetSet.getPrivacyAge().equals(Constants.TAG_TRUE));
        btnNoOneContact.setChecked(GetSet.getPrivacyContactMe().equals(Constants.TAG_TRUE));
    }

    private void updateSettings(ProfileRequest request) {
        if (NetworkReceiver.isConnected()) {
//            showLoading();
            if (GetSet.getUserId() != null) {
                request.setUserId(GetSet.getUserId());
                request.setProfileId(GetSet.getUserId());
                Call<ProfileResponse> call = apiInterface.getProfile(request);
                call.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        ProfileResponse profile = response.body();
                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                            GetSet.setPrivacyAge(profile.getPrivacyAge());
                            GetSet.setPrivacyContactMe(profile.getPrivacyContactMe());
                            GetSet.setShowNotification(profile.getShowNotification());
                            GetSet.setFollowNotification(profile.getFollowNotification());
                            GetSet.setChatNotification(profile.getChatNotification());
                            GetSet.setInterestNotification(profile.getInterestNotification());

                            SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                            SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                            SharedPref.putString(SharedPref.SHOW_NOTIFICATION, GetSet.getShowNotification());
                            SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                            SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                            SharedPref.putBoolean(SharedPref.INTEREST_NOTIFICATION, GetSet.getInterestNotification());
                            setSettings();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
//                        hideLoading();
                        call.cancel();
                    }
                });
            }
        } else {
//            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    public void showLoading() {
        /*Disable touch options*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        /*Enable touch options*/
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onWebSocketConnected() {

    }

    @Override
    public void onWebSocketMessage(String message) {

    }

    @Override
    public void onWebSocketClose() {

    }

    @Override
    public void onWebSocketError(String description) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }

    @OnClick({R.id.btnBack, R.id.btnPrivateChat, R.id.privateChatLay, R.id.btnDnD, R.id.dndLay, R.id.btnFollow, R.id.followLay,
            R.id.btnHideMyAge, R.id.ageLay, R.id.btnNoOneContact, R.id.contactLay, R.id.adView, R.id.btnSecurity, R.id.securityLay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnPrivateChat:
                if (NetworkReceiver.isConnected())
                    updatePrivateChat();
                break;
            case R.id.privateChatLay:
                if (NetworkReceiver.isConnected()) {
                    btnPrivateChat.setChecked(!btnPrivateChat.isChecked());
                    updatePrivateChat();
                }
                break;
            case R.id.btnDnD:
                if (NetworkReceiver.isConnected()) {
                    if (btnDnD.isChecked()) {
                        btnPrivateChat.setChecked(false);
                        btnFollow.setChecked(false);
                    }
                    updateDND();
                }
                break;
            case R.id.dndLay:
                if (NetworkReceiver.isConnected()) {
                    btnDnD.setChecked(!btnDnD.isChecked());
                    if (btnDnD.isChecked()) {
                        btnPrivateChat.setChecked(false);
                        btnFollow.setChecked(false);
                    }
                    updateDND();
                }
                break;
            case R.id.btnFollow:
                if (NetworkReceiver.isConnected()) {
                    updateFollow();
                }
                break;
            case R.id.followLay:
                if (NetworkReceiver.isConnected()) {
                    btnFollow.setChecked(!btnFollow.isChecked());
                    updateFollow();
                }
                break;
            case R.id.btnHideMyAge:
                if (NetworkReceiver.isConnected()) {
                    if (checkPremium()) {
                        updatePrivacyAge();
                    }
                }
                break;
            case R.id.ageLay:
                if (NetworkReceiver.isConnected()) {
                    if (checkPremium()) {
                        btnHideMyAge.setChecked(!btnHideMyAge.isChecked());
                        updatePrivacyAge();
                    }
                }
                break;
            case R.id.btnSecurity:
                updateDeviceSecurity();
                break;
            case R.id.securityLay:
                updateDeviceSecurity();
                break;
            case R.id.btnNoOneContact:
                if (NetworkReceiver.isConnected()) {
                    if (checkPremium()) {
                        updatePrivacyContact();
                    }
                }
                break;
            case R.id.contactLay:
                if (NetworkReceiver.isConnected()) {
                    if (checkPremium()) {
                        btnNoOneContact.setChecked(!btnNoOneContact.isChecked());
                        updatePrivacyContact();
                    }
                }
                break;
            case R.id.adView:
                break;
        }
    }

    private boolean checkPremium() {
        if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            return true;
        } else {
            Intent prime = new Intent(getApplicationContext(), PrimeActivity.class);
            prime.putExtra("OnCLick", "ClickHere");
            startActivityForResult(prime, Constants.PRIME_REQUEST_CODE);
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PRIME_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setSettings();
            loadAd();
        }
    }

    private void updatePrivateChat() {
        ProfileRequest request = new ProfileRequest();
        request.setChatNotification("" + btnPrivateChat.isChecked());
        updateSettings(request);
    }

    private void updateDND() {
        ProfileRequest request = new ProfileRequest();
        request.setShowNotification("" + btnDnD.isChecked());
        if (btnDnD.isChecked()) {
            request.setChatNotification("" + btnPrivateChat.isChecked());
            request.setFollowNotification("" + btnFollow.isChecked());
        }
        updateSettings(request);
    }

    private void updateFollow() {
        ProfileRequest request = new ProfileRequest();
        request.setInterestNotification("" + btnFollow.isChecked());
    }

    private void updatePrivacyAge() {
        ProfileRequest request = new ProfileRequest();
        request.setPrivacyAge("" + btnHideMyAge.isChecked());
        updateSettings(request);
    }

    private void updatePrivacyContact() {
        ProfileRequest request = new ProfileRequest();
        request.setPrivacyContactMe("" + btnNoOneContact.isChecked());
        updateSettings(request);
    }

    private void updateDeviceSecurity() {
        if (!SharedPref.getBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false)) {
            if (appUtils.checkIsDeviceEnabled(this)) {
                btnSecurity.setChecked(true);
                SharedPref.putBoolean(SharedPref.IS_FINGERPRINT_LOCKED, true);
            } else {
                btnSecurity.setChecked(false);
                App.makeToast(getString(R.string.enable_device_lock_first));
            }
        } else {
            SharedPref.putBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false);
            btnSecurity.setChecked(false);
        }
    }
}
