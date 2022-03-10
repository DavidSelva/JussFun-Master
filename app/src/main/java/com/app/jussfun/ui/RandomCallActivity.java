package com.app.jussfun.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.jussfun.R;
import com.app.jussfun.apprtc.util.AppRTCUtils;
import com.app.jussfun.base.App;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.helper.AppWebSocket;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.callback.FollowUpdatedListener;
import com.app.jussfun.helper.callback.OnOkCancelClickListener;
import com.app.jussfun.helper.callback.ProfileUpdatedListener;
import com.app.jussfun.model.AppDefaultResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.Gift;
import com.app.jussfun.model.OnlineUsers;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.Logging;
import com.app.jussfun.utils.SharedPref;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RandomCallActivity extends BaseFragmentActivity implements RandomWebSocket.WebSocketChannelEvents {

    private static final String TAG = RandomCallActivity.class.getSimpleName();
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.searchLay)
    RelativeLayout searchLay;
    @BindView(R.id.parentLay)
    FrameLayout parentLay;
    public static String partnerId = "", partnerName = "";
    @BindView(R.id.txtUserAway)
    TextView txtUserAway;
    @BindView(R.id.txtSearching)
    TextView txtSearching;
    @BindView(R.id.rippleBackground)
    RippleBackground rippleBackground;
    private SharedPreferences sharedPref;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String callType = null;
    public RandomCallFragment callFragment;
    public OthersProfileFragment profileFragment;
    RandomPagerAdapter adapter;
    private ApiInterface apiInterface;
    private String roomId = null;
    private boolean isLoading = false;
    private boolean hasPartner = false;
    private boolean filterResult = false;
    private boolean callRandomJoin = true;
    private int searchCount = 0, MAX_RETRY = 50;
    private long WAITING_TIME = 0;
    private PowerManager.WakeLock wakeLock;
    private static final String CPU_WAKELOCK = "MyApp::MyWakelockTag";
    Dialog dialog;
    public static boolean isInCall = false;
    DialogCreditGems alertDialog;
    RandomWebSocket webSocket;
    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    private int revealX;
    private int revealY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_random_chat);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        callType = getIntent().getStringExtra(Constants.TAG_CALL_TYPE);
        if (callType == null) callType = Constants.TAG_VIDEO;
        Constants.isInRandomCall = true;
        searchCount = 0;

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                getIntent().hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                getIntent().hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {


            revealX = getIntent().getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = getIntent().getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);
            ViewTreeObserver viewTreeObserver = parentLay.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        parentLay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            parentLay.setVisibility(View.VISIBLE);
        }

        showLoading();
        // Get setting keys.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefVideoBitrateType = getString(R.string.pref_maxvideobitrate_key);
        keyprefVideoBitrateValue = getString(R.string.pref_maxvideobitratevalue_key);
        keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        if (!checkPermissions()) {
            finish();
            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
//            App.makeToast(getString(R.string.camera_microphone_permission_error));
            return;
        }
        initPowerOptions();
        getAppDefaults();
    }

    private boolean checkPermissions() {
        boolean isPermissionGranted = true;
        for (String permission : AppRTCUtils.MANDATORY_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                isPermissionGranted = false;
                break;
            }
        }
        return isPermissionGranted;
    }

    private void initView(JSONObject profileObject) {
        viewPager.setOffscreenPageLimit(2);
        adapter = new RandomPagerAdapter(getSupportFragmentManager(), profileObject);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initPowerOptions() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, CPU_WAKELOCK);
        wakeLock.acquire();
    }

    public void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
    }

    @Override
    public void onWebSocketConnected() {
        Log.i(TAG, "onWebSocketConnected: ");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timer.cancel();
                        if (!isInCall) {
                            changeLiveStatus(Constants.INSEARCH);
                            AppUtils.setCurrentStatus(Constants.INSEARCH);
                            findPartner();
                        } else {
                            changeLiveStatus(AppUtils.getCurrentStatus());
                        }
                    }
                });
            }
        }, 500);
    }

    @Override
    public void onWebSocketMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            Logging.i(TAG, "onWebSocketMessage: " + jsonObject);
            String type = jsonObject.optString(Constants.TAG_TYPE);
            if (jsonObject.has(Constants.TAG_PARTNER_ID) && !GetSet.getUserId().equals(jsonObject.getString(Constants.TAG_PARTNER_ID)))
                partnerId = jsonObject.optString(Constants.TAG_PARTNER_ID);
            switch (type) {
                case Constants.TAG_JOIN_BACK:
                    roomId = jsonObject.optString(Constants.TAG_RANDOM_KEY);
                    if (!hasPartner) {
                        // Check if already connected users try to join
                        if (!AppUtils.callerList.contains(jsonObject.optString(Constants.TAG_PARTNER_ID))) {
                            hasPartner = true;
                            isInCall = true;
                            changeLiveStatus(Constants.INRANDOMCALL);
                            AppUtils.setCurrentStatus(Constants.INRANDOMCALL);
                            if (jsonObject.has(Constants.TAG_FILTER_SEARCH_RESULT)) {
                                filterResult = jsonObject.optBoolean(Constants.TAG_FILTER_SEARCH_RESULT);
                            }
                            partnerId = jsonObject.optString(Constants.TAG_PARTNER_ID);
                            if (callFragment != null) {
                                Bundle bundle = new Bundle();
                                bundle = connectToRoom(roomId, false, false, false, 0, bundle);
                                setProfile(jsonObject, bundle);
                                callFragment.setContext(RandomCallActivity.this);
                                callFragment.connectWithOthers(bundle);
                                refreshProfile(partnerId);
                            } else {
                                partnerName = jsonObject.optString(Constants.TAG_NAME);
                                initView(jsonObject);
                            }
                        } else {
                            hasPartner = false;
                            if (searchCount > MAX_RETRY) {
                                showAlertDialog(getString(R.string.filter_error_message));
                            } else {
                                leaveRoom(roomId);
                                changeLiveStatus(Constants.INSEARCH);
                                AppUtils.setCurrentStatus(Constants.INSEARCH);
                                callRandomChat();
                                callRandomJoin = false;
                                findPartner();
                            }
                        }
                    }
                    break;
                case Constants.TAG_SEARCH_BACK:
                    if (!hasPartner) {
                        Log.d(TAG, "TAG_SEARCH_BACK: " + searchCount);
                        searchCount++;
                        if (GetSet.isFilterApplied() && (GetSet.getPremiumMember().equals(Constants.TAG_TRUE) ?
                                GetSet.getGems() > Long.parseLong(AdminData.filterGems.getSubFilterPrice()) :
                                GetSet.getGems() > Long.parseLong(AdminData.filterGems.getUnSubFilterPrice()))) {
                            if (searchCount > MAX_RETRY) {
                                showAlertDialog(getString(R.string.filter_error_message));
                            } else {
                                showLoading();
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!AppUtils.getCurrentStatus().equals(Constants.INSEARCH)) {
                                            changeLiveStatus(Constants.INSEARCH);
                                            AppUtils.setCurrentStatus(Constants.INSEARCH);
                                        }
                                        findPartner();
//                                        }
                                    }
                                }, WAITING_TIME);
                            }
                        } else if (GetSet.isFilterApplied()) {
                            AppUtils.resetFilter();
                            showAlertDialog(getString(R.string.not_enough_gems));
                        } else {
                            if (searchCount > MAX_RETRY) {
                                showAlertDialog(getString(R.string.no_user_message));
                            } else {
                                showLoading();
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!AppUtils.getCurrentStatus().equals(Constants.INSEARCH)) {
                                            changeLiveStatus(Constants.INSEARCH);
                                            AppUtils.setCurrentStatus(Constants.INSEARCH);
                                        }
                                        findPartner();
                                    }
                                }, WAITING_TIME);
                            }
                        }
                    }
                    break;
                case Constants.TAG_LEAVE_PARTNER:
                    showLoading();
                    App.makeToast(getString(R.string.partner_left_chat));
                    removeViews();
                    searchAnotherPartner();
                    break;
                case Constants.TAG_RECEIVE_STICKER:
                    if (callFragment != null && jsonObject.get(Constants.TAG_PARTNER_ID).equals(GetSet.getUserId())) {
                        final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_sticker_receive);
                        callFragment.displaySticker(jsonObject.getString(Constants.TAG_STICKER_ID), myAnim, true);
                    }
                    break;

                case Constants.TAG_RECEIVE_GIFT:
                    if (callFragment != null && jsonObject.get(Constants.TAG_PARTNER_ID).equals(GetSet.getUserId())) {
                        Gift gift = new Gift();
                        GetSet.setGifts(Long.valueOf(jsonObject.getString(Constants.TAG_TOTAL_GIFTS)));
                        SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                        gift.setPartnerId(jsonObject.getString(Constants.TAG_PARTNER_ID));
                        gift.setUserId(jsonObject.getString(Constants.TAG_USER_ID));
                        gift.setGiftIcon(jsonObject.getString(Constants.TAG_GIFT_ICON));
                        gift.setGiftTitle(jsonObject.getString(Constants.TAG_GIFT_TITLE));
                        String desc = getString(R.string.receive_gift_desc) + "\n" + "\"" + gift.getGiftTitle() + "\" " + getString(R.string.gift);
                        callFragment.displayReceiveGift(gift, desc);
                    }
                    break;
                case Constants.TAG_SEND_GIFT_SUCCEED:
                    GetSet.setGems(Long.valueOf(jsonObject.getString(Constants.TAG_TOTAL_GEMS)));
                    SharedPref.putLong(SharedPref.GEMS, GetSet.getGems());
                    break;
                case Constants.TAG_OUT_OF_GEMS:
                    App.makeToast(getString(R.string.not_enough_gems));
                    break;
                case Constants.TAG_SEND_GIFT_FAILED:
                    App.makeToast(getString(R.string.something_went_wrong_try_again));
                    break;
                case Constants.TAG_FILTER_CHARGED:
                    if (jsonObject.getString(Constants.TAG_USER_ID).equals(GetSet.getUserId())) {
                        GetSet.setGems(Long.valueOf(jsonObject.getString(Constants.TAG_TOTAL_GEMS)));
                        SharedPref.putLong(SharedPref.GEMS, GetSet.getGems());
                    }
                    break;
                case Constants.TAG_USER_BLOCKED:
                    removeDeviceID(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
                    break;
                case Constants.TAG_LISTEN_USER:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (jsonObject.getString(Constants.TAG_MSG_TYPE).equals(Constants.ONLINE)) {
                                    txtUserAway.setVisibility(View.GONE);
                                } else if (jsonObject.getString(Constants.TAG_MSG_TYPE).equals(Constants.OFFLINE)) {
                                    txtUserAway.setVisibility(View.VISIBLE);
                                    txtUserAway.setText(partnerName + " " + getString(R.string.away_from_chat));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                case Constants.TAG_VIDEO_DISABLED:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (jsonObject.getString(Constants.TAG_IS_BLURRED).equals(Constants.TAG_TRUE)) {
                                    if (callFragment != null) {
                                        callFragment.disableVideo(true);
                                    }
                                } else {
                                    if (callFragment != null) {
                                        callFragment.disableVideo(false);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void removeViews() {
        if (callFragment != null) {
            callFragment.hideControls();
            callFragment.removeFragments();
            callFragment.disconnect();
        }
    }


    @Override
    public void onWebSocketClose() {
        RandomWebSocket.mInstance = null;
        RandomWebSocket.getInstance(RandomCallActivity.this);
    }

    @Override
    public void onWebSocketError(String description) {
        Logging.e(TAG, "onWebSocketError: " + description);
    }

    public void searchAnotherPartner() {
        isInCall = false;
        partnerId = null;
        partnerName = null;
        hasPartner = false;
        leaveRoom(roomId);
        AppUtils.setCurrentStatus(Constants.INSEARCH);
        changeLiveStatus(Constants.INSEARCH);
        findPartner();
    }

    public void changeLiveStatus(String status) {
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_STATUS, status);
            json.put(Constants.TAG_TYPE, Constants.TAG_LIVESTATUS);
            json.put(Constants.TAG_FILTER_APPLIED, GetSet.isFilterApplied() ? "1" : "0");
            Logging.i(TAG, "changeLiveStatus: " + json);
            if (webSocket != null) {
                webSocket.send(json.toString());
            }
        } catch (JSONException e) {
            Logging.e(TAG, "changeLiveStatus: " + e.getMessage());
        }
    }

    public void leaveRoom(String roomId) {
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_ROOM_ID, roomId);
            json.put(Constants.TAG_TYPE, Constants.TAG_LEAVE_ROOM);
            Logging.i(TAG, "TAG_LEAVE_ROOM: " + json);
            if (webSocket != null) {
                webSocket.send(json.toString());
            }
        } catch (JSONException e) {
            Logging.e(TAG, "TAG_LEAVE_ROOM: " + e.getMessage());
        }
    }

    private void findPartner() {
        if (!hasPartner) {
            if ((searchCount == 0 || (searchCount % 5 == 0)) && callRandomJoin) {
                callRandomChat();
            }
            callRandomJoin = true;
            txtUserAway.setVisibility(View.GONE);
            try {
                JSONObject json = new JSONObject();
                json.put(Constants.TAG_USER_ID, GetSet.getUserId());
                json.put(Constants.TAG_PLATFORM, Constants.TAG_ANDROID);
                json.put(Constants.TAG_TYPE, Constants.TAG_FIND_PARTNER);
                json.put(Constants.TAG_CALL_TYPE, callType);
                JSONArray partnersList = new JSONArray(AppUtils.callerList);
                json.put(Constants.TAG_PARTNER_LIST, partnersList);
                json.put(Constants.TAG_FILTER_APPLIED, GetSet.isFilterApplied() ? "1" : "0");
                if (GetSet.isFilterApplied()) {
                    json.put(Constants.TAG_GENDER, GetSet.getFilterGender().toLowerCase());
                    json.put(Constants.TAG_MIN_AGE, GetSet.getFilterMinAge());
                    json.put(Constants.TAG_MAX_AGE, GetSet.getFilterMaxAge());
                    JSONArray locationList;
                    if (GetSet.isLocationApplied()) {
                        locationList = new JSONArray(AppUtils.filterLocation);
                    } else {
                        AppUtils.filterLocation = new ArrayList<>();
                        AppUtils.filterLocation.addAll(AdminData.locationList);
                        AppUtils.filterLocation.add(Constants.TAG_GLOBAL);
                        AppUtils.filterLocation.remove(getString(R.string.select_all));
                        locationList = new JSONArray(AppUtils.filterLocation);
                    }
                    json.put(Constants.TAG_LOCATION, locationList);
                }
                Logging.i(TAG, "findPartner: " + json);
                webSocket.send(json.toString());
            } catch (JSONException e) {
                Logging.e(TAG, "findPartner: " + e.getMessage());
            }
        }
    }

    private void callRandomChat() {
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_TYPE, "_joinRandomChat");
            if (webSocket != null) {
                Logging.i(TAG, "_joinRandomChat: " + json);
                webSocket.send(json.toString());
            }
        } catch (JSONException e) {
            Logging.e(TAG, "_joinRandomChat: " + e.getMessage());
        }
    }

    private void sendBusyRequest(String partnerId) {
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_PARTNER_ID, partnerId);
            json.put(Constants.TAG_TYPE, Constants.TAG_COMMITED);
            Logging.i(TAG, "c: " + json);
            webSocket.send(json.toString());
        } catch (JSONException e) {
            Logging.e(TAG, "sendBusyRequest: " + e.getMessage());
        }
    }

    private void joinRoom(String partnerId) {
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_PARTNER_ID, partnerId);
            json.put(Constants.TAG_PLATFORM, Constants.TAG_ANDROID);
            json.put(Constants.TAG_TYPE, Constants.TAG_JOIN_ROOM);
            Logging.i(TAG, "joinRoom: " + json);
            webSocket.send(json.toString());
        } catch (JSONException e) {
            Logging.e(TAG, "joinRoom: " + e.getMessage());
        }
    }

    public void breakPartner(String partnerId) {
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_PARTNER_ID, partnerId);
            if (!TextUtils.isEmpty(roomId)) {
                json.put(Constants.TAG_ROOM_ID, roomId);
            }
            json.put(Constants.TAG_PARTNER_ID, partnerId);
            json.put(Constants.TAG_TYPE, Constants.TAG_BREAKUP_PARTNER);
            Logging.i(TAG, "breakPartner: " + json);
            webSocket.send(json.toString());
        } catch (JSONException e) {
            Logging.e(TAG, "breakPartner: " + e.getMessage());
        }
    }

    public void sendSticker(String sticker, String partnerId) {
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_PARTNER_ID, partnerId);
            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_STICKER);
            json.put(Constants.TAG_STICKER_ID, sticker);
            Logging.i(TAG, "sendSticker: " + json);
            webSocket.send(json.toString());
        } catch (JSONException e) {
            Logging.e(TAG, "sendSticker: " + e.getMessage());
        }
    }

    public void sendGift(Gift gift, String partnerId) {
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_PARTNER_ID, partnerId);
            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_GIFT);
            json.put(Constants.TAG_GIFT_TITLE, gift.getGiftTitle());
            json.put(Constants.TAG_GIFT_ICON, gift.getGiftIcon());
            if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                json.put(Constants.TAG_GEMS_COUNT, gift.getGiftGemsPrime());
            } else {
                json.put(Constants.TAG_GEMS_COUNT, gift.getGiftGems());
            }
            json.put(Constants.TAG_GEMS_EARNINGS, gift.getGiftGems());
            Logging.i(TAG, "sendGift: " + json);
            webSocket.send(json.toString());
        } catch (JSONException e) {
            Logging.e(TAG, "sendGift: " + e.getMessage());
        }
    }

    public void updateHistory(String partnerId) {
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_PARTNER_ID, partnerId);
            json.put(Constants.TAG_TYPE, Constants.TAG_ON_CHAT);
            Logging.i(TAG, "updateHistory: " + json);
            webSocket.send(json.toString());
        } catch (JSONException e) {
            Logging.e(TAG, "updateHistory: " + e.getMessage());
        }
    }

    public void updateFilterCharge() {
        if (GetSet.isFilterApplied() && filterResult) {
            try {
                JSONObject json = new JSONObject();
                json.put(Constants.TAG_USER_ID, GetSet.getUserId());
                json.put(Constants.TAG_TYPE, Constants.TAG_FILTER_UPDATED);
                json.put(Constants.TAG_FILTER_SEARCH_RESULT, "" + filterResult);
                Logging.i(TAG, "updateFilterResult: " + json);
                webSocket.send(json.toString());
                filterResult = false;
                App.makeToast(getString(R.string.filter_applied_description));
            } catch (JSONException e) {
                Logging.e(TAG, "updateFilterResult: " + e.getMessage());
            }
        } else if (GetSet.isFilterApplied()) {
            App.makeToast(getString(R.string.filter_not_applied_description));
        }
    }

    public void onBlurVideoCall(boolean isBlurred) {
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_TYPE, Constants.TAG_DISABLE_VIDEO);
            json.put(Constants.TAG_IS_BLURRED, "" + isBlurred);
            json.put(Constants.TAG_PARTNER_ID, "" + partnerId);
            Logging.i(TAG, "onBlurVideoCall: " + json);
            webSocket.send(json.toString());
        } catch (JSONException e) {
            Logging.e(TAG, "onBlurVideoCall: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 1) {
            viewPager.setCurrentItem(0);
        } else {
            if (searchLay.getVisibility() == View.VISIBLE) {
                closeConfirmDialog(getString(R.string.stop_finding_partner));
            } else {
                closeConfirmDialog(getString(R.string.are_you_sure_want_to_exit));
            }
        }
    }

    private void setProfile(JSONObject jsonObject, Bundle bundle) {
        partnerName = jsonObject.optString(Constants.TAG_NAME);
        bundle.putString(Constants.TAG_USER_IMAGE, jsonObject.optString(Constants.TAG_USER_IMAGE));
        bundle.putString(Constants.TAG_NAME, jsonObject.optString(Constants.TAG_NAME));
        bundle.putString(Constants.TAG_AGE, jsonObject.optString(Constants.TAG_AGE));
        bundle.putString(Constants.TAG_DOB, jsonObject.optString(Constants.TAG_DOB));
        bundle.putString(Constants.TAG_GENDER, jsonObject.optString(Constants.TAG_GENDER));
        bundle.putString(Constants.TAG_PREMIUM_MEBER, jsonObject.optString(Constants.TAG_PREMIUM_MEBER, Constants.TAG_FALSE));
        bundle.putString(Constants.TAG_LOCATION, jsonObject.optString(Constants.TAG_LOCATION));
        bundle.putString(Constants.TAG_FOLLOW, jsonObject.optString(Constants.TAG_FOLLOW, Constants.TAG_FALSE));
        bundle.putString(Constants.TAG_FOLLOWERS, bundle.getString(Constants.TAG_FOLLOWERS));
        bundle.putString(Constants.TAG_FOLLOWINGS, bundle.getString(Constants.TAG_FOLLOWINGS));
        bundle.putString(Constants.TAG_PRIVACY_AGE, jsonObject.optString(Constants.TAG_PRIVACY_AGE, Constants.TAG_FALSE));
        bundle.putString(Constants.TAG_PRIVACY_CONTACT_ME, jsonObject.optString(Constants.TAG_PRIVACY_CONTACT_ME, Constants.TAG_FALSE));
        bundle.putString(Constants.TAG_PLATFORM, jsonObject.optString(Constants.TAG_PLATFORM, ""));
        bundle.putBoolean(Constants.TAG_INTEREST_ON_YOU, jsonObject.optBoolean(Constants.TAG_INTEREST_ON_YOU, false));
        bundle.putBoolean(Constants.TAG_INTERESTED_BY_ME, jsonObject.optBoolean(Constants.TAG_INTERESTED_BY_ME, false));
        bundle.putBoolean(Constants.TAG_FRIEND, jsonObject.optBoolean(Constants.TAG_FRIEND, false));
        bundle.putBoolean(Constants.TAG_DECLINED, jsonObject.optBoolean(Constants.TAG_DECLINED, false));
    }


    public void refreshProfile(String partnerId) {
        if (profileFragment != null) {
            profileFragment.updateFragment(partnerId);
        }
    }

    public void setRoomId(String roomId) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            RandomWebSocket.setCallEvents(null);
            releaseWakeLock();
        } catch (Exception e) {
            Logging.e(TAG, "onDestroy: " + e.getMessage());
        }
        Constants.isInRandomCall = false;
        partnerId = null;
        partnerName = null;
        callFragment = null;
        unregisterNetworkReceiver();
        changeLiveStatus(Constants.ONLINE);
        AppUtils.setCurrentStatus(Constants.ONLINE);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        if (!isConnected) {
            if (callFragment != null) {
                callFragment.onCallHangUp();
                partnerId = null;
            } else {
                unRevealActivity();
            }
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void resetSearchCount() {
        searchCount = 0;
        callRandomJoin = true;
    }

    @OnClick(R.id.btnExit)
    public void onViewClicked() {
        if (callFragment != null) {
            callFragment.onCallHangUp();
            partnerId = null;
        } else {
            if (NetworkReceiver.isConnected() && partnerId != null && isInCall) {
                breakPartner(partnerId);
            }
            partnerId = null;
            unRevealActivity();
        }
    }

    public class RandomPagerAdapter extends FragmentStatePagerAdapter implements ProfileUpdatedListener, FollowUpdatedListener {

        private static final int NUM_PAGES = 2;
        private final JSONObject profileObject;

        public RandomPagerAdapter(FragmentManager fm, JSONObject profileObject) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.profileObject = profileObject;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (callFragment != null && callFragment.isAdded()) {
                    callFragment.onDetach();
                    callFragment = null;
                }
                callFragment = new RandomCallFragment();
                roomId = profileObject.optString(Constants.TAG_RANDOM_KEY);
                Bundle bundle = new Bundle();
                bundle = connectToRoom(roomId, false, false, false, 0, bundle);
                setProfile(profileObject, bundle);
                callFragment.setContext(RandomCallActivity.this);
                callFragment.setArguments(bundle);
                return callFragment;
            } else {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TAG_PARTNER_ID, partnerId);
                setProfile(profileObject, bundle);
                profileFragment = new OthersProfileFragment();
                profileFragment.setFollowUpdatedListener(this);
                profileFragment.setArguments(bundle);
                return profileFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public void onFollowUpdated(ProfileResponse profileResponse, int holderPosition) {
            if (callFragment != null) {
                callFragment.changeFollowStatus(profileResponse);
            }
        }

        @Override
        public void onFollowUpdated(OnlineUsers.AccountModel profileResponse, int holderPosition) {

        }

        @Override
        public void onProfileClicked(String userId) {

        }

        @Override
        public void onProfileUpdated(String partnerId) {
            refreshProfile(partnerId);
        }

        @Override
        public void moveToProfile() {

        }

    }

    @SuppressWarnings("StringSplitter")
    public Bundle connectToRoom(String roomId, boolean commandLineRun, boolean loopback,
                                boolean useValuesFromIntent, int runTimeMs, Bundle bundle) {

        // roomId is random for loopback.
        if (loopback) {
            roomId = Integer.toString((new Random()).nextInt(100000000));
        }

        String roomUrl = Constants.APPRTC_URL;

        // Video call enabled flag.
        boolean videoCallEnabled = callType.equals(Constants.TAG_VIDEO);

        // Use screencapture option.
        boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
                AppRTCUtils.EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default);

        // Use Camera2 option.
        boolean useCamera2 = sharedPrefGetBoolean(R.string.pref_camera2_key, AppRTCUtils.EXTRA_CAMERA2,
                R.string.pref_camera2_default);

        // Get default codecs.
        String videoCodec = sharedPrefGetString(R.string.pref_videocodec_key,
                AppRTCUtils.EXTRA_VIDEOCODEC, R.string.pref_videocodec_default, useValuesFromIntent);
        String audioCodec = sharedPrefGetString(R.string.pref_audiocodec_key,
                AppRTCUtils.EXTRA_AUDIOCODEC, R.string.pref_audiocodec_default, useValuesFromIntent);

        // Check HW codec flag.
        boolean hwCodec = sharedPrefGetBoolean(R.string.pref_hwcodec_key,
                AppRTCUtils.EXTRA_HWCODEC_ENABLED, R.string.pref_hwcodec_default);

        // Check Capture to texture.
        boolean captureToTexture = sharedPrefGetBoolean(R.string.pref_capturetotexture_key,
                AppRTCUtils.EXTRA_CAPTURETOTEXTURE_ENABLED, R.string.pref_capturetotexture_default);

        // Check FlexFEC.
        boolean flexfecEnabled = sharedPrefGetBoolean(R.string.pref_flexfec_key,
                AppRTCUtils.EXTRA_FLEXFEC_ENABLED, R.string.pref_flexfec_default);

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key,
                AppRTCUtils.EXTRA_NOAUDIOPROCESSING_ENABLED, R.string.pref_noaudioprocessing_default);

        boolean aecDump = sharedPrefGetBoolean(R.string.pref_aecdump_key,
                AppRTCUtils.EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default);

        boolean saveInputAudioToFile =
                sharedPrefGetBoolean(R.string.pref_enable_save_input_audio_to_file_key,
                        AppRTCUtils.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED,
                        R.string.pref_enable_save_input_audio_to_file_default);

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPrefGetBoolean(R.string.pref_opensles_key,
                AppRTCUtils.EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default);

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key,
                AppRTCUtils.EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default);

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key,
                AppRTCUtils.EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default);

        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key,
                AppRTCUtils.EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default);

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = sharedPrefGetBoolean(
                R.string.pref_disable_webrtc_agc_and_hpf_key, AppRTCUtils.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF,
                R.string.pref_disable_webrtc_agc_and_hpf_key);

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        if (useValuesFromIntent) {
            videoWidth = getIntent().getIntExtra(AppRTCUtils.EXTRA_VIDEO_WIDTH, 0);
            videoHeight = getIntent().getIntExtra(AppRTCUtils.EXTRA_VIDEO_HEIGHT, 0);
        }
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution =
                    sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
        if (useValuesFromIntent) {
            cameraFps = getIntent().getIntExtra(AppRTCUtils.EXTRA_VIDEO_FPS, 0);
        }
        if (cameraFps == 0) {
            String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
            String[] fpsValues = fps.split("[ x]+");
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
                    Log.e(TAG, "Wrong camera fps setting: " + fps);
                }
            }
        }

        // Check capture quality slider flag.
        boolean captureQualitySlider = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key,
                AppRTCUtils.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
                R.string.pref_capturequalityslider_default);

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        if (useValuesFromIntent) {
            videoStartBitrate = getIntent().getIntExtra(AppRTCUtils.EXTRA_VIDEO_BITRATE, 0);
        }
        if (videoStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_maxvideobitrate_default);
            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default));
                videoStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        int audioStartBitrate = 0;
        if (useValuesFromIntent) {
            audioStartBitrate = getIntent().getIntExtra(AppRTCUtils.EXTRA_AUDIO_BITRATE, 0);
        }
        if (audioStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
            String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default));
                audioStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        // Check statistics display option.
        boolean displayHud = sharedPrefGetBoolean(R.string.pref_displayhud_key,
                AppRTCUtils.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default);

        boolean tracing = sharedPrefGetBoolean(R.string.pref_tracing_key, AppRTCUtils.EXTRA_TRACING,
                R.string.pref_tracing_default);

        // Check Enable RtcEventLog.
        boolean rtcEventLogEnabled = sharedPrefGetBoolean(R.string.pref_enable_rtceventlog_key,
                AppRTCUtils.EXTRA_ENABLE_RTCEVENTLOG, R.string.pref_enable_rtceventlog_default);

        boolean useLegacyAudioDevice = sharedPrefGetBoolean(R.string.pref_use_legacy_audio_device_key,
                AppRTCUtils.EXTRA_USE_LEGACY_AUDIO_DEVICE, R.string.pref_use_legacy_audio_device_default);

        // Get datachannel options
        boolean dataChannelEnabled = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key,
                AppRTCUtils.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default);
        boolean ordered = sharedPrefGetBoolean(R.string.pref_ordered_key, AppRTCUtils.EXTRA_ORDERED,
                R.string.pref_ordered_default);
        boolean negotiated = sharedPrefGetBoolean(R.string.pref_negotiated_key,
                AppRTCUtils.EXTRA_NEGOTIATED, R.string.pref_negotiated_default);
        int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key,
                AppRTCUtils.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default,
                useValuesFromIntent);
        int maxRetr =
                sharedPrefGetInteger(R.string.pref_max_retransmits_key, AppRTCUtils.EXTRA_MAX_RETRANSMITS,
                        R.string.pref_max_retransmits_default, useValuesFromIntent);
        int id = sharedPrefGetInteger(R.string.pref_data_id_key, AppRTCUtils.EXTRA_ID,
                R.string.pref_data_id_default, useValuesFromIntent);
        String protocol = sharedPrefGetString(R.string.pref_data_protocol_key,
                AppRTCUtils.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, useValuesFromIntent);

        // Start AppRTCMobile activity.
        Log.d(TAG, "Connecting to room " + roomId + " at URL " + roomUrl);
        bundle.putString(Constants.TAG_PARTNER_ID, partnerId);
        bundle.putString(AppRTCUtils.EXTRA_ROOM_URI, roomUrl);
        bundle.putString(AppRTCUtils.EXTRA_ROOMID, roomId);
        bundle.putBoolean(AppRTCUtils.EXTRA_LOOPBACK, loopback);
        bundle.putBoolean(AppRTCUtils.EXTRA_VIDEO_CALL, videoCallEnabled);
        bundle.putBoolean(AppRTCUtils.EXTRA_SCREENCAPTURE, useScreencapture);
        bundle.putBoolean(AppRTCUtils.EXTRA_CAMERA2, useCamera2);
        bundle.putInt(AppRTCUtils.EXTRA_VIDEO_WIDTH, videoWidth);
        bundle.putInt(AppRTCUtils.EXTRA_VIDEO_HEIGHT, videoHeight);
        bundle.putInt(AppRTCUtils.EXTRA_VIDEO_FPS, cameraFps);
        bundle.putBoolean(AppRTCUtils.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
        bundle.putInt(AppRTCUtils.EXTRA_VIDEO_BITRATE, videoStartBitrate);
        bundle.putString(AppRTCUtils.EXTRA_VIDEOCODEC, videoCodec);
        bundle.putBoolean(AppRTCUtils.EXTRA_HWCODEC_ENABLED, hwCodec);
        bundle.putBoolean(AppRTCUtils.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
        bundle.putBoolean(AppRTCUtils.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
        bundle.putBoolean(AppRTCUtils.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
        bundle.putBoolean(AppRTCUtils.EXTRA_AECDUMP_ENABLED, aecDump);
        bundle.putBoolean(AppRTCUtils.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, saveInputAudioToFile);
        bundle.putBoolean(AppRTCUtils.EXTRA_OPENSLES_ENABLED, useOpenSLES);
        bundle.putBoolean(AppRTCUtils.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
        bundle.putBoolean(AppRTCUtils.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
        bundle.putBoolean(AppRTCUtils.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
        bundle.putBoolean(AppRTCUtils.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
        bundle.putInt(AppRTCUtils.EXTRA_AUDIO_BITRATE, audioStartBitrate);
        bundle.putString(AppRTCUtils.EXTRA_AUDIOCODEC, audioCodec);
        bundle.putBoolean(AppRTCUtils.EXTRA_DISPLAY_HUD, displayHud);
        bundle.putBoolean(AppRTCUtils.EXTRA_TRACING, tracing);
        bundle.putBoolean(AppRTCUtils.EXTRA_ENABLE_RTCEVENTLOG, rtcEventLogEnabled);
        bundle.putBoolean(AppRTCUtils.EXTRA_CMDLINE, commandLineRun);
        bundle.putInt(AppRTCUtils.EXTRA_RUNTIME, runTimeMs);
        bundle.putBoolean(AppRTCUtils.EXTRA_USE_LEGACY_AUDIO_DEVICE, useLegacyAudioDevice);

        bundle.putBoolean(AppRTCUtils.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

        if (dataChannelEnabled) {
            bundle.putBoolean(AppRTCUtils.EXTRA_ORDERED, ordered);
            bundle.putInt(AppRTCUtils.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
            bundle.putInt(AppRTCUtils.EXTRA_MAX_RETRANSMITS, maxRetr);
            bundle.putString(AppRTCUtils.EXTRA_PROTOCOL, protocol);
            bundle.putBoolean(AppRTCUtils.EXTRA_NEGOTIATED, negotiated);
            bundle.putInt(AppRTCUtils.EXTRA_ID, id);
        }

        if (useValuesFromIntent) {
            if (getIntent().hasExtra(AppRTCUtils.EXTRA_VIDEO_FILE_AS_CAMERA)) {
                String videoFileAsCamera =
                        getIntent().getStringExtra(AppRTCUtils.EXTRA_VIDEO_FILE_AS_CAMERA);
                bundle.putString(AppRTCUtils.EXTRA_VIDEO_FILE_AS_CAMERA, videoFileAsCamera);
            }

            if (getIntent().hasExtra(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE)) {
                String saveRemoteVideoToFile =
                        getIntent().getStringExtra(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
                bundle.putString(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE, saveRemoteVideoToFile);
            }

            if (getIntent().hasExtra(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH)) {
                int videoOutWidth =
                        getIntent().getIntExtra(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
                bundle.putInt(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, videoOutWidth);
            }

            if (getIntent().hasExtra(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT)) {
                int videoOutHeight =
                        getIntent().getIntExtra(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
                bundle.putInt(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, videoOutHeight);
            }
        }

        return bundle;
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private String sharedPrefGetString(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultValue = getString(defaultId);
        if (useFromIntent) {
            String value = getIntent().getStringExtra(intentName);
            if (value != null) {
                return value;
            }
            return defaultValue;
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getString(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private boolean sharedPrefGetBoolean(
            int attributeId, String intentName, int defaultId) {
        boolean defaultValue = Boolean.parseBoolean(getString(defaultId));
        String attributeName = getString(attributeId);
        return sharedPref.getBoolean(attributeName, defaultValue);
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private int sharedPrefGetInteger(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultString = getString(defaultId);
        int defaultValue = Integer.parseInt(defaultString);
        if (useFromIntent) {
            return getIntent().getIntExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            String value = sharedPref.getString(attributeName, defaultString);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
                return defaultValue;
            }
        }
    }

    public void showLoading() {
        isLoading = true;
        rippleBackground.startRippleAnimation();
        if (searchLay.getVisibility() != View.VISIBLE) {
            searchLay.setVisibility(View.VISIBLE);
            String[] searchArray = getResources().getStringArray(R.array.search_list);
            Random random = new Random();
            int i = random.nextInt(searchArray.length);
            txtSearching.setText(searchArray[i]);
        }

    }

    public void hideLoading() {
        rippleBackground.stopRippleAnimation();
        isLoading = false;
        searchLay.setVisibility(View.GONE);
    }

    private void closeConfirmDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RandomCallActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if (callFragment != null) {
                    callFragment.onCallHangUp();
                    partnerId = null;
                } else {
                    if (NetworkReceiver.isConnected() && partnerId != null && isInCall) {
                        breakPartner(partnerId);
                    }
                    partnerId = null;
                    unRevealActivity();
                }
                overridePendingTransition(0, 0);
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

    private void showAlertDialog(String message) {
        if (callFragment != null) {
            callFragment.disconnect();
            callFragment.resetRoom();
        }
        if (NetworkReceiver.isConnected() && partnerId != null && isInCall) {
            breakPartner(partnerId);
        }
        AppUtils.setCurrentStatus(Constants.ONLINE);
        changeLiveStatus(Constants.ONLINE);
        partnerId = null;
        if (alertDialog != null && alertDialog.isAdded()) {
            return;
        }
        alertDialog = new DialogCreditGems();
        alertDialog.setCallBack(new OnOkCancelClickListener() {
            @Override
            public void onOkClicked(Object o) {
                alertDialog.dismissAllowingStateLoss();
                unRevealActivity();
            }

            @Override
            public void onCancelClicked(Object o) {
                alertDialog.dismissAllowingStateLoss();
                unRevealActivity();
            }
        });
        alertDialog.setContext(RandomCallActivity.this);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(message);
        alertDialog.show(getSupportFragmentManager(), TAG);
    }

    public void checkSlowNetwork() {
        ConnectivityManager
                cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo network = cm.getActiveNetworkInfo();
        if (network != null) {
            int netSubType = network.getSubtype();
            if (netSubType == TelephonyManager.NETWORK_TYPE_GPRS ||
                    netSubType == TelephonyManager.NETWORK_TYPE_EDGE ||
                    netSubType == TelephonyManager.NETWORK_TYPE_1xRTT) {
                //user is in slow network
                App.makeToast(getString(R.string.slow_network_connection));
            }
        }
    }

    private void getAppDefaults() {
        Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID);
        call.enqueue(new Callback<AppDefaultResponse>() {
            @Override
            public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                    AppDefaultResponse defaultData = response.body();
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
                    AdminData.membershipList.addAll(defaultData.getMembershipPackages());
                    AdminData.filterGems = defaultData.getFilterGems();
                    AdminData.filterOptions = defaultData.getFilterOptions();
                    AdminData.contactEmail = defaultData.getContactEmail();
                    AdminData.welcomeMessage = defaultData.getWelcomeMessage();
                    AdminData.inviteCredits = defaultData.getInviteCredits();
                    AdminData.showAds = defaultData.getShowAds();
                    AdminData.showVideoAd = defaultData.getVideoAds();
                    AdminData.googleAdsId = defaultData.getGoogleAdsClient();
                    AdminData.showMoneyConversion = defaultData.getShowMoneyConversion();
                    AdminData.videoAdsClient = defaultData.getVideoAdsClient();
                    AdminData.videoAdsDuration = defaultData.getVideoAdsDuration();
                    AdminData.videoCallsGems = defaultData.getVideoCalls() != null ? defaultData.getVideoCalls() : 0;
                    AdminData.gemConversion = defaultData.getGemConversion();
                    AdminData.giftConversion = defaultData.getGiftConversion();
                }
                initSocket();
            }

            @Override
            public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                call.cancel();
                initSocket();
            }
        });
    }

    private void initSocket() {
        RandomWebSocket.mInstance = null;
        webSocket = RandomWebSocket.getInstance(RandomCallActivity.this);
        RandomWebSocket.setCallEvents(RandomCallActivity.this);
    }

    private void removeDeviceID(String deviceId) {
        Call<HashMap<String, String>> call = apiInterface.pushSignOut(deviceId);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                overridePendingTransition(0, 0);
                DBHelper.getInstance(RandomCallActivity.this).clearDB();
                GetSet.reset();
                SharedPref.clearAll();
                AppWebSocket.getInstance(RandomCallActivity.this).disconnect();
                AppWebSocket.setCallEvents(null);
                RandomWebSocket.getInstance(RandomCallActivity.this).disconnect(true);
                App.makeToast(getString(R.string.account_deactivated_by_admin));
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class GetAppDefaultTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            getAppDefaults();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    protected void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(parentLay.getWidth(), parentLay.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(parentLay, x, y, 0, finalRadius);
            circularReveal.setDuration(600);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            parentLay.setVisibility(View.VISIBLE);
            circularReveal.start();
        }
    }

    protected void unRevealActivity() {
        if (revealX != 0) {
            float finalRadius = (float) (Math.max(parentLay.getWidth(), parentLay.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                    parentLay, revealX, revealY, finalRadius, 0);

            circularReveal.setDuration(300);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    parentLay.setVisibility(View.INVISIBLE);
                    finish();
                }
            });
            circularReveal.start();
        } else {
            finish();
        }
    }
}
