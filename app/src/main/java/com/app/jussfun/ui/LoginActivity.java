package com.app.jussfun.ui;


import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.helper.AppWebSocket;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.AddDeviceRequest;
import com.app.jussfun.model.AdminMessageResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.SignInRequest;
import com.app.jussfun.model.SignInResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.DeviceTokenPref;
import com.app.jussfun.utils.Logging;
import com.app.jussfun.utils.SharedPref;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseFragmentActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;
    @BindView(R.id.btnMobile)
    Button btnMobile;
    @BindView(R.id.btnFacebook)
    Button btnFacebook;
    @BindView(R.id.txtTerms)
    TextView txtTerms;
    @BindView(R.id.parentLay)
    FrameLayout parentLay;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.iconAppLogo)
    ImageView iconAppLogo;
    @BindView(R.id.btnTerms)
    CheckBox btnTerms;
    @BindView(R.id.bottomLay)
    LinearLayout bottomLay;
    private CallbackManager callbackManager;
    private ApiInterface apiInterface;
    String fbImage, fbName, fbEmail;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        dbHelper = DBHelper.getInstance(this);

        if (AppUtils.hasNavigationBar()) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(50, 0, 50, AppUtils.getNavigationBarHeight(this) + 30);
            params.gravity = Gravity.BOTTOM | Gravity.CENTER;
            bottomLay.setPadding(0, 0, 0, 10);
            bottomLay.setLayoutParams(params);
        }

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        txtTerms.setText(Html.fromHtml(getString(R.string.terms_and_conditions_des)));
    }

    @OnClick({R.id.btnMobile, R.id.btnFacebook, R.id.txtTerms})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnMobile:
                if (btnTerms.isChecked()) {
                    App.preventMultipleClick(btnMobile);
//                    verifyMobileNumber();
                    signIn("9360810959", Constants.TAG_PHONENUMBER,"","", "");
                } else {
                    App.makeToast(getString(R.string.accept_our_policy));
                }
                break;
            case R.id.btnFacebook:
                if (btnTerms.isChecked()) {
                    App.preventMultipleClick(btnFacebook);
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
                    loginWithFacebook();
                } else {
                    App.makeToast(getString(R.string.accept_our_policy));
                }
                break;
            case R.id.txtTerms:
                App.preventMultipleClick(txtTerms);
                Intent intent = new Intent(getApplicationContext(), TermsActivity.class);
                intent.putExtra(Constants.TAG_FROM, "privacy");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
        }
    }

    private void verifyMobileNumber() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build());
        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.LoginAppTheme)
                        .build(),
                RC_SIGN_IN);
    }

    private void loginWithFacebook() {
        LoginManager.getInstance().logOut();
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject profile, GraphResponse response) {
                        try {
                            fbImage = "https://graph.facebook.com/" + profile.getString("id") + "/picture?type=large";
                            fbName = profile.getString("name");

                            if (profile.has("email"))
                                fbEmail = profile.getString("email");
                            else
                                fbEmail = "";
                            signIn(profile.optString(Constants.TAG_ID), Constants.TAG_FACEBOOK, fbEmail, fbName, fbImage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link, email, first_name, last_name");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Logging.i(TAG, "onCancel: ");
            }

            @Override
            public void onError(FacebookException error) {
                Logging.e(TAG, "onError: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getPhoneNumber() != null) {
                    signIn(user.getPhoneNumber(), Constants.TAG_PHONENUMBER, null, null, null);
                }
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void signIn(String loginId, String type, String email, String name, String image) {
        if (NetworkReceiver.isConnected()) {
            showLoading();
            SignInRequest request = new SignInRequest();
            request.setLoginId(loginId);
            request.setType(type);
            Call<SignInResponse> call = apiInterface.callSignIn(request);
            call.enqueue(new Callback<SignInResponse>() {
                @Override
                public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                    if (response.isSuccessful()) {
                        SignInResponse signin = response.body();
                        if (signin.getStatus().equals(Constants.TAG_TRUE)) {
                            GetSet.setLogged(true);
                            SharedPref.putBoolean(SharedPref.IS_LOGGED, true);
                            SharedPref.putString(SharedPref.LOGIN_ID, loginId);
                            SharedPref.putString(SharedPref.LOGIN_TYPE, type);
                            if (type.equalsIgnoreCase(Constants.TAG_FACEBOOK)) {
                                SharedPref.putString(SharedPref.FACEBOOK_NAME, name);
                                SharedPref.putString(SharedPref.FACEBOOK_EMAIL, email);
                                SharedPref.putString(SharedPref.FACEBOOK_IMAGE, image);
                            }
                            GetSet.setLoginId(loginId);
                            GetSet.setLoginType(type);
                            if (!signin.isAccountExists()) {
                                hideLoading();
                                goToMain();
                            } else {
                                //                            App.makeToast(signin.getLocation());
                                GetSet.setUserId(signin.getUserId());
                                GetSet.setLoginId(signin.getLoginId());
                                GetSet.setName(signin.getName());
                                GetSet.setDob(signin.getDob());
                                GetSet.setAge(signin.getAge());
                                GetSet.setGender(signin.getGender());
                                GetSet.setLocation(signin.getLocation());
                                GetSet.setAuthToken(signin.getAuthToken());
                                GetSet.setUserImage(signin.getUserImage());
                                GetSet.setFollowersCount("0");
                                GetSet.setFollowingCount("0");
                                GetSet.setInterestsCount(0);
                                GetSet.setFriendsCount(0);
                                GetSet.setUnlocksLeft(0);
                                GetSet.setGems(0L);
                                GetSet.setGifts(0L);
                                GetSet.setPremiumMember(Constants.TAG_TRUE);
                                GetSet.setPremiumExpiry("");
                                GetSet.setPrivacyAge(signin.getPrivacyAge());
                                GetSet.setPrivacyContactMe(signin.getPrivacyContactMe());
                                GetSet.setFollowNotification(signin.getFollowNotification());
                                GetSet.setChatNotification(signin.getChatNotification());
                                GetSet.setShowNotification(signin.getShowNotification());
                                GetSet.setInterestNotification(signin.getInterestNotification());
                                AppUtils.callerList.clear();
                                AppUtils.callerList.add(GetSet.getUserId());

                                SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                                SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());
                                SharedPref.putString(SharedPref.NAME, GetSet.getName());
                                SharedPref.putString(SharedPref.DOB, GetSet.getDob());
                                SharedPref.putString(SharedPref.AGE, GetSet.getAge());
                                SharedPref.putString(SharedPref.GENDER, GetSet.getGender());
                                SharedPref.putString(SharedPref.LOCATION, GetSet.getLocation());
                                SharedPref.putString(SharedPref.AUTH_TOKEN, GetSet.getAuthToken());
                                SharedPref.putString(SharedPref.USER_IMAGE, GetSet.getUserImage());
                                SharedPref.putString(SharedPref.FOLLOWERS_COUNT, GetSet.getFollowersCount());
                                SharedPref.putString(SharedPref.FOLLOWINGS_COUNT, GetSet.getFollowingCount());
                                SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                                SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());
                                SharedPref.putInt(SharedPref.UNLOCKS_LEFT, GetSet.getUnlocksLeft());
                                SharedPref.putLong(SharedPref.GEMS, GetSet.getGems());
                                SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                                SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                                SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                                SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                                SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());

                                addDeviceID();
                            }
                        } else if (signin.getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                            App.makeToast(getString(R.string.contact_admin_desc));
                            hideLoading();
                        } else {
                            App.makeToast(getString(R.string.something_went_wrong));
                            hideLoading();
                        }
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                        hideLoading();
                    }

                }

                @Override
                public void onFailure(Call<SignInResponse> call, Throwable t) {
                    hideLoading();
                    call.cancel();
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void goToMain() {
        if (GetSet.getUserId() != null) {
            AppWebSocket.mInstance = null;
            AppWebSocket.getInstance(this);
        }
        hideLoading();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private void showLoading() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
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
                String msgAt = "" + (System.currentTimeMillis() / 1000);
                String createdAt = AppUtils.getCurrentUTCTime(getApplicationContext());

                AdminMessageResponse.MessageData messageData = new AdminMessageResponse().new MessageData();
                messageData.setMsgFrom(Constants.TAG_ADMIN);
                messageData.setMsgId(GetSet.getUserId() + (System.currentTimeMillis() / 1000));
                messageData.setMsgType(Constants.TAG_TEXT);
                messageData.setMsgData("" + AdminData.welcomeMessage);
                messageData.setMsgAt(msgAt);
                messageData.setCreateaAt(createdAt);
                dbHelper.addAdminMessage(messageData);

                messageData.setMsgAt(createdAt);
                messageData.setCreateaAt(msgAt);
                int unseenCount = dbHelper.getUnseenMessagesCount(GetSet.getUserId());
                unseenCount = unseenCount + 1;
                dbHelper.addAdminRecentMessage(messageData, unseenCount);
                goToMain();
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        AppUtils.showSnack(this, parentLay, isConnected);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }
}
