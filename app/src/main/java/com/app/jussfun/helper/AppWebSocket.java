package com.app.jussfun.helper;


import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.app.jussfun.apprtc.util.AppRTCUtils;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.external.RandomString;
import com.app.jussfun.model.ChatResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.ui.App;
import com.app.jussfun.ui.ChatActivity;
import com.app.jussfun.ui.VideoCallActivity;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.Logging;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import de.tavendo.autobahn.WebSocketConnection;

public class AppWebSocket {
    private final String TAG = this.getClass().getSimpleName();
    public static AppWebSocket mInstance;
    private static Context mContext;
    public static WebSocketConnection ws;
    WebSocketClient webSocketClient;
    private Handler handler = new Handler(Looper.getMainLooper());
    private HandlerThread handlerThread;
    static String wsServerUrl;
    private static WebSocketChannelEvents events;
    private final List<String> wsSendQueue = new ArrayList<>();
    private boolean closeEvent;
    private final Object closeEventLock = new Object();
    public DBHelper dbHelper;

    public AppWebSocket(Context context) throws URISyntaxException {
        mContext = context;
        dbHelper = DBHelper.getInstance(context);
//        ws = new WebSocketConnection();
        webSocketClient = new WebSocketClient(new URI(wsServerUrl)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Logging.d(TAG, "WebSocket connection opened to: " + wsServerUrl);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (events != null) {
                            events.onWebSocketConnected();
                        }
                    }
                });
            }

            @Override
            public void onMessage(String message) {
                Logging.d(TAG, "onMessage: " + message);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(message);
                            String type = jsonObject.getString(Constants.TAG_TYPE);
                            switch (type) {
                                case Constants.TAG_RECEIVE_CHAT:
                                    dbHelper.saveMessageModel(jsonObject);
                                    dbHelper.saveRecentModel(jsonObject, Constants.ONLINE);
                                    if (events != null) {
                                        events.onWebSocketMessage(message);
                                    }

                                    String chatId = GetSet.getUserId() + jsonObject.optString(Constants.TAG_USER_ID);
                                    int unseenCount = dbHelper.getUnseenMessagesCount(chatId);
                                    if ((App.getCurrentActivity() instanceof ChatActivity)) {
                                        ChatActivity activity = (ChatActivity) App.getCurrentActivity();
                                        if (!activity.chatId.equals(chatId)) {
                                            unseenCount = unseenCount + 1;
                                        }
                                    } else {
                                        unseenCount = unseenCount + 1;
                                    }
                                    dbHelper.updateRecent(chatId, Constants.TAG_UNREAD_COUNT, "" + unseenCount);
                                    break;
                                case Constants.TAG_CALL_RECEIVED:
                                    onCallReceive(jsonObject);
                                    break;
                                case Constants.TAG_OFFLINE_CHATS:
                                    JSONArray jsonArray = jsonObject.getJSONArray(Constants.TAG_RECORDS);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject data = jsonArray.getJSONObject(i);
                                        if (data.has(Constants.TAG_MSG_TYPE) && data.getString(Constants.TAG_MSG_TYPE).equals(Constants.TAG_MISSED)) {
                                            String chatID = GetSet.getUserId() + data.optString(Constants.TAG_USER_ID);
                                            ChatResponse chatResponse = new ChatResponse();
                                            chatResponse.setChatId(chatID);
                                            chatResponse.setUserId(data.optString(Constants.TAG_USER_ID));
                                            chatResponse.setUserName(jsonObject.optString(Constants.TAG_USER_NAME));
                                            chatResponse.setUserImage(jsonObject.optString(Constants.TAG_USER_IMAGE));
                                            chatResponse.setChatType(data.optString(Constants.TAG_CHAT_TYPE));
                                            chatResponse.setReceiverId(data.optString(Constants.TAG_RECEIVER_ID));
                                            chatResponse.setMessageType(data.optString(Constants.TAG_MSG_TYPE));
                                            chatResponse.setMessageEnd(Constants.TAG_CALL);
                                            chatResponse.setMessage(Constants.TAG_MISSED);
                                            chatResponse.setMessageId(new RandomString().nextString());
                                            chatResponse.setChatTime(data.optString(Constants.TAG_CREATED_AT));
                                            chatResponse.setReceivedTime(AppUtils.getCurrentUTCTime(App.getInstance()));
                                            chatResponse.setThumbnail("");
                                            chatResponse.setProgress("");
                                            dbHelper.addMessage(chatResponse);
                                            dbHelper.saveRecentModel(data, Constants.OFFLINE);

                                            int count = dbHelper.getUnseenMessagesCount(chatID);
                                            if ((App.getCurrentActivity() instanceof ChatActivity)) {
                                                ChatActivity activity = (ChatActivity) App.getCurrentActivity();
                                                if (!activity.chatId.equals(chatID)) {
                                                    count = count + 1;
                                                }
                                            } else {
                                                count = count + 1;
                                            }
                                            dbHelper.updateRecent(chatID, Constants.TAG_UNREAD_COUNT, "" + count);
                                        } else {
                                            dbHelper.saveMessageModel(data);
                                            dbHelper.saveRecentModel(data, Constants.OFFLINE);

                                            String chatID = GetSet.getUserId() + jsonObject.optString(Constants.TAG_USER_ID);
                                            int count = dbHelper.getUnseenMessagesCount(chatID);
                                            if ((App.getCurrentActivity() instanceof ChatActivity)) {
                                                ChatActivity activity = (ChatActivity) App.getCurrentActivity();
                                                if (!activity.chatId.equals(chatID)) {
                                                    count = count + 1;
                                                }
                                            } else {
                                                count = count + 1;
                                            }
                                            dbHelper.updateRecent(chatID, Constants.TAG_UNREAD_COUNT, "" + count);
                                        }

                                        if (events != null) {
                                            events.onWebSocketMessage(data.toString());
                                        }
                                    }
                                    break;
                                case Constants.TAG_RECEIVE_READ_STATUS:
                                    dbHelper.updateReadStatus(jsonObject.getString(Constants.TAG_RECEIVER_ID) + jsonObject.getString(Constants.TAG_USER_ID));
                                    if (events != null) {
                                        events.onWebSocketMessage(jsonObject.toString());
                                    }
                                    break;
                                case Constants.TAG_OFFLINE_READ_STATUS:
                                    JSONArray records = jsonObject.getJSONArray(Constants.TAG_RECORDS);
                                    for (int i = 0; i < records.length(); i++) {
                                        dbHelper.updateReadStatus(records.getJSONObject(i).getString(Constants.TAG_RECEIVER_ID) + records.getJSONObject(i).getString(Constants.TAG_USER_ID));
                                    }
                                    if (events != null) {
                                        events.onWebSocketMessage(jsonObject.toString());
                                    }
                                    break;
                                default:
                                    if (events != null) {
                                        events.onWebSocketMessage(jsonObject.toString());
                                    }
                                    break;
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "onTextMessage: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });

            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d(TAG, "WebSocket connection closed. Code: " + code + ". Reason: " + reason + ". State: "
                        + remote);
                synchronized (closeEventLock) {
                    closeEvent = true;
                    closeEventLock.notify();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (events != null)
                            events.onWebSocketClose();
                    }
                });
            }

            @Override
            public void onError(Exception ex) {
                Log.d(TAG, "WebSocket Exception: " + ex.getMessage());
            }
        };
        webSocketClient.setConnectionLostTimeout(5);
        connect();
    }

    public static synchronized AppWebSocket getInstance(Context context) {
        wsServerUrl = Constants.CHAT_SOCKET_URL + GetSet.getUserId();
        if (mInstance == null) {
            try {
                mInstance = new AppWebSocket(context);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return mInstance;
    }

    public static synchronized void setNullInstance() {
        mInstance = null;
    }

    private void connect() {
       /* checkIfCalledOnValidThread();
        if (state != WebSocketConnectionState.NEW) {
            Log.e(TAG, "WebSocket is already connected.");
            return;
        }
        try {
            ws = new WebSocketConnection();
            wsObserver = new WebSocketObserver();
            closeEvent = false;
            ws.connect(new URI(wsServerUrl), wsObserver);
        } catch (URISyntaxException e) {
            reportError("URI error: " + e.getMessage());
        } catch (WebSocketException e) {
            reportError("WebSocket connection error: " + e.getMessage());
        }*/
        if (webSocketClient != null)
            webSocketClient.connect();
    }

    public void send(String message) {
        if (webSocketClient != null && webSocketClient.isOpen())
            webSocketClient.send(message);
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    public static void setCallEvents(WebSocketChannelEvents events) {
        AppWebSocket.events = events;
    }

    public WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    /**
     * Possible WebSocket connection states.
     */
    public enum WebSocketConnectionState {
        NEW, CONNECTED, REGISTERED, CLOSED, ERROR
    }

    /**
     * Callback interface for messages delivered on WebSocket.
     * All events are dispatched from a looper executor thread.
     */
    public interface WebSocketChannelEvents {

        void onWebSocketConnected();

        void onWebSocketMessage(final String message);

        void onWebSocketClose();

        void onWebSocketError(final String description);
    }

    private void reportError(final String errorMessage) {
        Log.e(TAG, "reportError: " + errorMessage);
        handler.post(new Runnable() {
            @Override
            public void run() {
                events.onWebSocketError(errorMessage);
            }
        });
    }

    // Helper method for debugging purposes. Ensures that WebSocket method is
    // called on a looper thread.
    private void checkIfCalledOnValidThread() {
        if (Thread.currentThread() != handler.getLooper().getThread()) {
            throw new IllegalStateException("WebSocket method is not called on valid thread");
        }
    }

    private void onCallReceive(JSONObject data) {
        String userId = data.optString(Constants.TAG_USER_ID, "");
        String userName = data.optString(Constants.TAG_USER_NAME, "");
        String userImage = data.optString(Constants.TAG_USER_IMAGE, "");
        String receiverId = data.optString(Constants.TAG_RECEIVER_ID, "");
        String chatType = data.optString(Constants.TAG_CHAT_TYPE, "");
        String utcTime = data.optString(Constants.TAG_CREATED_AT, "");
        String callType = data.optString(Constants.TAG_CALL_TYPE, "");
        String roomId = data.optString(Constants.TAG_ROOM_ID, "");
        String platform = data.optString(Constants.TAG_PLATFORM, "");

        if (callType.equalsIgnoreCase(Constants.TAG_CREATED)) {
            TelephonyManager telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            int isPhoneCallOn = telephony.getCallState();
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
            } else if (VideoCallActivity.isInCall && isPhoneCallOn == 0) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TAG_USER_ID, userId);
                    jsonObject.put(Constants.TAG_USER_NAME, userName);
                    jsonObject.put(Constants.TAG_USER_IMAGE, userImage);
                    jsonObject.put(Constants.TAG_RECEIVER_ID, receiverId);
                    jsonObject.put(Constants.TAG_TYPE, Constants.TAG_CREATE_CALL);
                    jsonObject.put(Constants.TAG_CALL_TYPE, Constants.TAG_WAITING);
                    jsonObject.put(Constants.TAG_CREATED_AT, utcTime);
                    jsonObject.put(Constants.TAG_ROOM_ID, roomId);
                    jsonObject.put(Constants.TAG_CHAT_TYPE, Constants.TAG_CALL);
                    jsonObject.put(Constants.TAG_PLATFORM, Constants.TAG_ANDROID);
                    send(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (callType.equalsIgnoreCase(Constants.TAG_ENDED)) {
            if (App.getCurrentActivity() instanceof VideoCallActivity) {
                VideoCallActivity activity = (VideoCallActivity) App.getCurrentActivity();
                if (activity.receiverId.equals(userId)) {
                    activity.finish();
                    VideoCallActivity.isInCall = false;
                }
            }
        } else if (callType.equalsIgnoreCase(Constants.TAG_WAITING)) {
            if (App.getCurrentActivity() instanceof VideoCallActivity) {
                VideoCallActivity activity = (VideoCallActivity) App.getCurrentActivity();
                activity.setWaiting();
            }
        }
    }
}
