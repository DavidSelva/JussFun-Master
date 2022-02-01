package com.app.jussfun.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.app.jussfun.R;
import com.app.jussfun.apprtc.util.AppRTCUtils;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.model.AddDeviceRequest;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.base.App;
import com.app.jussfun.ui.ChatActivity;
import com.app.jussfun.ui.VideoCallActivity;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.DeviceTokenPref;
import com.app.jussfun.utils.Logging;
import com.app.jussfun.utils.SharedPref;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    ApiInterface apiInterface;
    DBHelper dbHelper;
    Context mContext;
    AppUtils appUtils;

    @Override
    public void onNewToken(String deviceToken) {
        super.onNewToken(deviceToken);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Logging.i(TAG, "onNewToken: " + deviceToken);
        DeviceTokenPref.getInstance(this).saveDeviceToken(deviceToken);
        if (SharedPref.getBoolean(SharedPref.IS_LOGGED, false)) {
            GetSet.setLogged(true);
            addDeviceId();
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Logging.i(TAG, "onMessageReceived getData: " + remoteMessage.getData());
        Logging.i(TAG, "onMessageReceived getNotification: " + remoteMessage.getNotification());
        Logging.i(TAG, "onMessageReceived isOnAppForegrounded: " + App.isOnAppForegrounded());
        dbHelper = DBHelper.getInstance(this);
        mContext = this;
        appUtils = new AppUtils(this);
        Map<String, String> map = remoteMessage.getData();
        String scope = map.get(Constants.TAG_SCOPE);
        if (scope != null) {
            if (scope.equals(Constants.TAG_TEXT_CHAT)) {
                setChatNotification(map);
            } else if (scope.equals(Constants.TAG_VIDEO_CALL)) {
                onCalReceived(map, scope);
            } else if (scope.equals(Constants.TAG_ENDED)) {
                onCalReceived(map, scope);
            } else if (scope.equals(Constants.TAG_FOLLOW)) {
                if (GetSet.getFollowNotification().equals(Constants.TAG_TRUE))
                    setFollowNotification(map);
            } else if (scope.equals(Constants.TAG_ADMIN)) {
                setAdminNotification(map);
            } else if (scope.equals(Constants.TAG_INTEREST)) {
                if (GetSet.getInterestNotification())
                    setInterestNotification(map);
            } else if (scope.equals(Constants.TAG_MATCH)) {
                if (GetSet.getInterestNotification())
                    setInterestNotification(map);
            }
        }
    }

    private void onCalReceived(Map<String, String> map, String scope) {
        String userId = map.get(Constants.TAG_USER_ID);
        String userName = map.get(Constants.TAG_USER_NAME);
        String userImage = map.get(Constants.TAG_USER_IMAGE);
        String receiverId = map.get(Constants.TAG_RECEIVER_ID);
        String chatType = map.get(Constants.TAG_CHAT_TYPE);
        String utcTime = map.get(Constants.TAG_CREATED_AT);
        String callType = map.get(Constants.TAG_CALL_TYPE);
        String roomId = map.get(Constants.TAG_ROOM_ID);
        String platform = map.get(Constants.TAG_PLATFORM);

        Log.i(TAG, "onCalRece: " + scope);
        long diffInMs = System.currentTimeMillis() - AppUtils.getTimeFromUTC(mContext, utcTime);
        long diffSeconds = diffInMs / 1000;

        if (diffSeconds < 30 && scope.equalsIgnoreCase(Constants.TAG_VIDEO_CALL)) {
            TelephonyManager telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            int isPhoneCallOn = telephony.getCallState();
            Log.i(TAG, "onCalRecei: " + isPhoneCallOn);
            Log.i(TAG, "onCalReceiv: " + Constants.isInRandomCall + Constants.isInStream + VideoCallActivity.isInCall);
            if (!Constants.isInRandomCall && !Constants.isInStream && !VideoCallActivity.isInCall && isPhoneCallOn == 0) {
                VideoCallActivity.isInCall = true;
                AppRTCUtils appRTCUtils = new AppRTCUtils(mContext);
                Intent intent = appRTCUtils.connectToRoom(userId, Constants.TAG_RECEIVE, chatType);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.TAG_ROOM_ID, roomId);
                intent.putExtra(Constants.TAG_PLATFORM, platform);
                intent.putExtra(Constants.TAG_USER_ID, userId);
                intent.putExtra(Constants.TAG_USER_NAME, userName);
                intent.putExtra(Constants.TAG_USER_IMAGE, userImage);
                intent.putExtra(Constants.TAG_RECEIVER_ID, receiverId);
                mContext.startActivity(intent);
            }
        } else if (scope.equalsIgnoreCase(Constants.TAG_ENDED)) {
            Log.i(TAG, "onCalReceive: " + scope);
            if (App.getCurrentActivity() instanceof VideoCallActivity) {
                Log.i(TAG, "onCalReceived: " + scope);
                VideoCallActivity activity = (VideoCallActivity) App.getCurrentActivity();
                if (activity != null) {
                    if (activity.isSender) {
                        if (activity.receiverId.equals(userId)) {
                            activity.finishAndRemoveTask();
                        }
                    } else {
                        if (activity.userId.equals(userId)) {
                            activity.finishAndRemoveTask();
                        }
                    }
                }

            } else {
                Log.i(TAG, "onCalReceivedfirebaseonCalReceivedelse: " + scope);
                if (VideoCallActivity.activity != null)
                    VideoCallActivity.activity.finishAndRemoveTask();
            }
        }
    }

    private void setChatNotification(Map<String, String> data) {
        Intent intent = new Intent();
        if (!Constants.isInRandomCall && !Constants.isInVideoCall) {
            intent = new Intent(getApplicationContext(), com.app.jussfun.ui.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.NOTIFICATION, Constants.TAG_PROFILE);
            intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
            intent.putExtra(Constants.TAG_PARTNER_ID, data.get(Constants.TAG_USER_ID));
            intent.putExtra(Constants.TAG_PARTNER_NAME, data.get(Constants.TAG_USER_NAME));
            intent.putExtra(Constants.TAG_PARTNER_IMAGE, data.get(Constants.TAG_USER_IMAGE));
        }

        String messageType = "";
        messageType = data.get(Constants.TAG_MSG_TYPE);
        if (GetSet.getChatNotification().equals(Constants.TAG_TRUE)) {
            if (App.getCurrentActivity() instanceof ChatActivity) {
                ChatActivity activity = (ChatActivity) App.getCurrentActivity();
                if (activity.getPartnerId() != null && !activity.getPartnerId().equals(data.get(Constants.TAG_USER_ID))) {
                    if (messageType.equals(Constants.TAG_TEXT)) {
                        sendNotification(1, data.get(Constants.TAG_USER_NAME), AppUtils.decryptMessage(data.get(Constants.TAG_MESSAGE)), intent);
                    } else if (messageType.equals(Constants.TAG_MISSED)) {

                    } else if (messageType.equals(Constants.TAG_IMAGE)) {
                        sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.image), intent);
                    }
                    // for addon gif

                    else if (messageType.equals(Constants.TAG_GIF)) {
                        sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.gif), intent);
                    } else if (messageType.equals(Constants.TAG_AUDIO)) {
                        sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.audio), intent);
                    }
                }
            } else {
                if (messageType.equals(Constants.TAG_TEXT)) {
                    sendNotification(1, data.get(Constants.TAG_USER_NAME), AppUtils.decryptMessage(data.get(Constants.TAG_MESSAGE)), intent);
                } else if (messageType.equals(Constants.TAG_MISSED)) {

                } else if (messageType.equals(Constants.TAG_IMAGE)) {
                    sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.image), intent);
                }
                // for addon gif
                else if (messageType.equals(Constants.TAG_GIF)) {
                    sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.gif), intent);
                } else if (messageType.equals(Constants.TAG_AUDIO)) {
                    sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.audio), intent);
                }
            }
        }
    }

    private void setFollowNotification(Map<String, String> data) {
        Intent intent = new Intent();
        if (!Constants.isInRandomCall && !Constants.isInVideoCall) {
            intent = new Intent(getApplicationContext(), com.app.jussfun.ui.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
            intent.putExtra(Constants.NOTIFICATION, Constants.TAG_FOLLOW);
            intent.putExtra(Constants.TAG_PARTNER_ID, data.get(Constants.TAG_USER_ID));
        }

        String appName = getString(R.string.app_name);
        if (GetSet.getFollowNotification().equals(Constants.TAG_TRUE)) {
            sendNotification(2, appName, data.get(Constants.TAG_MESSAGE), intent);
        }
    }

    private void setAdminNotification(Map<String, String> data) {
        Intent intent = new Intent();
        if (!Constants.isInRandomCall && !Constants.isInVideoCall) {
            intent = new Intent(getApplicationContext(), com.app.jussfun.ui.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
            intent.putExtra(Constants.NOTIFICATION, Constants.TAG_ADMIN);
            intent.putExtra(Constants.TAG_FROM, Constants.TAG_ADMIN);
        }

        String appName = getString(R.string.app_name);
        sendNotification(0, appName, data.get(Constants.TAG_MESSAGE), intent);
    }

    private void setInterestNotification(Map<String, String> data) {
        Intent intent = new Intent();
        if (!Constants.isInRandomCall && !Constants.isInVideoCall) {
            intent = new Intent(getApplicationContext(), com.app.jussfun.ui.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
            intent.putExtra(Constants.NOTIFICATION, data.get(Constants.TAG_SCOPE));
        }
        String appName = getString(R.string.app_name);
        sendNotification(0, appName, data.get(Constants.TAG_MESSAGE), intent);
    }

    private void sendNotification(int notifyId, String title, String message, Intent intent) {
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        long when = System.currentTimeMillis();
        String CHANNEL_ID = getString(R.string.notification_channel_id);
        CharSequence channelName = getString(R.string.app_name);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, uniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

        mBuilder.setChannelId(CHANNEL_ID)
                .setContentText(message)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setAutoCancel(true);
        mNotifyManager.notify(getString(R.string.app_name), notifyId, mBuilder.build());
    }

    private void addDeviceId() {
        final String token = DeviceTokenPref.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

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
}
