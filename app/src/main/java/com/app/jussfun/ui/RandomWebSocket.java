package com.app.jussfun.ui;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.app.jussfun.db.DBHelper;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.Logging;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class RandomWebSocket {
    private final String TAG = this.getClass().getSimpleName();
    private static final int CLOSE_TIMEOUT = 1000;
    public static RandomWebSocket mInstance;
    private static Context mContext;
    private Handler handler = new Handler(Looper.getMainLooper());
    static String wsServerUrl;
    private static WebSocketChannelEvents events;
    public DBHelper dbHelper;
    private WebSocketClient webSocketClient;

    public RandomWebSocket(Context context) throws URISyntaxException {
        mContext = context;
        dbHelper = DBHelper.getInstance(context);
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (events != null) {
                            events.onWebSocketMessage(message);
                        }
                    }
                });
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.e(TAG, "WebSocket connection closed. Code: " + code + ". Reason: " + reason + ". State: "
                        + remote);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (events != null) {
                            events.onWebSocketClose();
                        }
                    }
                });
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "WebSocket Exception: " + ex.getMessage());
            }
        };
        connect();
    }

    private void connect() {
        Log.i(TAG, "connect: " + wsServerUrl);
        webSocketClient.connect();
    }

    public static synchronized RandomWebSocket getInstance(Context context) {
        wsServerUrl = Constants.RANDOM_CALL_SOCKET_URL + GetSet.getUserId();
        if (mInstance == null) {
            try {
                mInstance = new RandomWebSocket(context);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return mInstance;
    }

    public void send(String message) {
        if (webSocketClient != null && webSocketClient.isOpen())
            webSocketClient.send(message);
    }

    public void disconnect(boolean waitForComplete) {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    public static void setCallEvents(WebSocketChannelEvents channelEvents) {
        events = channelEvents;
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

    // Helper method for debugging purposes. Ensures that WebSocket method is
    // called on a looper thread.
    private void checkIfCalledOnValidThread() {
        if (Thread.currentThread() != handler.getLooper().getThread()) {
            throw new IllegalStateException("WebSocket method is not called on valid thread");
        }
    }
}
