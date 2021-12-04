package com.app.jussfun.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.app.jussfun.R;
import com.app.jussfun.livedata.MessageLiveModel;
import com.app.jussfun.model.AdminMessageResponse;
import com.app.jussfun.model.ChatResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.ui.App;
import com.app.jussfun.ui.ChatActivity;
import com.app.jussfun.ui.VideoCallActivity;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.Logging;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = DBHelper.class.getSimpleName();
    // If you change the database schema, you must increment the database version.
    public static final String DATABASE_NAME = "jussfun.db";
    public static final int DATABASE_VERSION = 4;
    public static final String TABLE_MESSAGES = "messages";
    public static final String TABLE_RECENTS = "recents";
    public static final String TABLE_CHATS = "chats";
    public static final String[] ALL_TABLES = new String[]{TABLE_MESSAGES,
            TABLE_RECENTS, TABLE_CHATS};
    private static DBHelper dbInstance;

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public DBHelper(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DBHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (dbInstance == null) {
            dbInstance = new DBHelper(context.getApplicationContext());
        }
        return dbInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES +
                "(" +
                Constants.TAG_CHAT_ID + " TEXT," + Constants.TAG_MSG_ID + " TEXT PRIMARY KEY," + Constants.TAG_USER_ID + " TEXT," + Constants.TAG_CHAT_TYPE + " TEXT," + Constants.TAG_MSG_TYPE + " TEXT," +
                Constants.TAG_MESSAGE_END + " TEXT," + Constants.TAG_MESSAGE + " TEXT," + Constants.TAG_CHAT_TIME + " TEXT," + Constants.TAG_CREATED_AT + " TEXT," +
                Constants.TAG_RECEIVER_ID + " TEXT," + Constants.TAG_DELIVERY_STATUS + " TEXT," + Constants.TAG_THUMBNAIL + " TEXT," + Constants.TAG_PROGRESS + " TEXT," +
                Constants.TAG_RECEIVED_TIME + " TEXT" +
                ")";

        String CREATE_RECENT_TABLE = "CREATE TABLE " + TABLE_RECENTS +
                "(" +
                Constants.TAG_CHAT_ID + " TEXT PRIMARY KEY," + Constants.TAG_RECENT_TYPE + " TEXT," + Constants.TAG_USER_ID + " TEXT," + Constants.TAG_MSG_ID + " TEXT," +
                Constants.TAG_MSG_TYPE + " TEXT," + Constants.TAG_RECEIVER_ID + " TEXT," + Constants.TAG_USER_NAME + " TEXT," + Constants.TAG_USER_IMAGE + " TEXT," +
                Constants.TAG_CHAT_TIME + " TEXT," + Constants.TAG_CREATED_AT + " TEXT," + Constants.TAG_UNREAD_COUNT + " TEXT," + Constants.TAG_READ_STATUS + " TEXT " +
                ")";

        String CREATE_CHATS_TABLE = "CREATE TABLE " + TABLE_CHATS +
                "(" +
                Constants.TAG_CHAT_ID + " TEXT PRIMARY KEY," + Constants.TAG_USER_ID + " TEXT," + Constants.TAG_USER_NAME + " TEXT," +
                Constants.TAG_USER_IMAGE + " TEXT, " + Constants.TAG_BLOCKED_BY_ME + " TEXT, " + Constants.TAG_ONLINE_STATUS + " TEXT, " +
                Constants.TAG_TYPING_STATUS + " TEXT " +
                ")";

        sqLiteDatabase.execSQL(CREATE_MESSAGES_TABLE);
        sqLiteDatabase.execSQL(CREATE_RECENT_TABLE);
        sqLiteDatabase.execSQL(CREATE_CHATS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.e(TAG, "onUpgrade: " + oldVersion + " " + newVersion);
        if (oldVersion < DATABASE_VERSION) {
            dropTables(sqLiteDatabase);
        }
    }

    private void dropTables(SQLiteDatabase sqLiteDatabase) {
        for (String tableName : ALL_TABLES) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '" + tableName + "'");
        }
        onCreate(sqLiteDatabase);
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    public void clearDB() {
        SQLiteDatabase db = getWritableDatabase(); // helper is object extends SQLiteOpenHelper
        for (String tableName : ALL_TABLES) {
            db.delete(tableName, null, null);
        }
    }

    public void saveMessageModel(JSONObject jsonObject) {
        try {
            String chatId = GetSet.getUserId() + jsonObject.optString(Constants.TAG_USER_ID);
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setChatId(chatId);
            chatResponse.setUserId(jsonObject.optString(Constants.TAG_USER_ID));
            chatResponse.setChatType(jsonObject.optString(Constants.TAG_CHAT_TYPE));
            chatResponse.setReceiverId(jsonObject.optString(Constants.TAG_RECEIVER_ID));
            chatResponse.setMessageType(jsonObject.optString(Constants.TAG_MSG_TYPE));
            chatResponse.setMessageEnd(Constants.TAG_RECEIVED);
            chatResponse.setMessage(AppUtils.decryptMessage(jsonObject.optString(Constants.TAG_MESSAGE)));
            chatResponse.setMessageId(jsonObject.optString(Constants.TAG_MSG_ID));
            chatResponse.setChatTime(jsonObject.optString(Constants.TAG_CHAT_TIME));
            chatResponse.setReceivedTime(AppUtils.getCurrentUTCTime(App.getInstance()));
            chatResponse.setThumbnail("");
            chatResponse.setProgress("");

            if (App.getCurrentActivity() instanceof VideoCallActivity) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (App.getCurrentActivity() instanceof ChatActivity) {
                            ChatActivity chatActivity = (ChatActivity) App.getCurrentActivity();
                            if (chatActivity.partnerId.equals(jsonObject.optString(Constants.TAG_USER_ID))) {
                                if (!chatResponse.getMessageType().equals(Constants.TAG_MISSED)) {
                                    chatResponse.setDeliveryStatus(Constants.TAG_READ);
                                }
                                if (MessageLiveModel.getMsgLiveData() != null) {
                                    List<ChatResponse> temp = MessageLiveModel.msgLiveData.getValue();
                                    if (temp == null)
                                        temp = new ArrayList<>();
                                    List<ChatResponse> clonedList = new ArrayList<>(temp);
                                    if (temp.size() == 0) {
                                        ChatResponse response = new ChatResponse();
                                        response.setReceiverId(jsonObject.optString(Constants.TAG_RECEIVER_ID));
                                        response.setUserId(GetSet.getUserId());
                                        response.setMessageType(Constants.TAG_DATE);
                                        response.setChatTime(AppUtils.getChatDate(App.getInstance(), jsonObject.optString(Constants.TAG_CHAT_TIME)));
                                        clonedList.add(response);
                                    }
                                    clonedList.add(0, chatResponse);
                                    Log.i(TAG, "saveMessageModel: " + new Gson().toJson(chatResponse));
                                    MessageLiveModel.getMsgLiveData().setValue(clonedList);
                                }
                            } else chatResponse.setDeliveryStatus(Constants.TAG_UNREAD);

                        }
                    }
                }, 500);
            }
            if (App.getCurrentActivity() instanceof ChatActivity) {
                ChatActivity chatActivity = (ChatActivity) App.getCurrentActivity();
                if (chatActivity.partnerId.equals(jsonObject.optString(Constants.TAG_USER_ID))) {
                    if (!chatResponse.getMessageType().equals(Constants.TAG_MISSED)) {
                        chatResponse.setDeliveryStatus(Constants.TAG_READ);
                    }
                    if (MessageLiveModel.getMsgLiveData() != null) {
                        List<ChatResponse> temp = MessageLiveModel.msgLiveData.getValue();
                        if (temp == null)
                            temp = new ArrayList<>();
                        List<ChatResponse> clonedList = new ArrayList<>(temp);
                        if (temp.size() == 0) {
                            ChatResponse response = new ChatResponse();
                            response.setReceiverId(jsonObject.optString(Constants.TAG_RECEIVER_ID));
                            response.setUserId(GetSet.getUserId());
                            response.setMessageType(Constants.TAG_DATE);
                            response.setChatTime(AppUtils.getChatDate(App.getInstance(), jsonObject.optString(Constants.TAG_CHAT_TIME)));
                            clonedList.add(response);
                        }
                        clonedList.add(0, chatResponse);
                        Log.i(TAG, "saveMessageModel: " + new Gson().toJson(chatResponse));
                        MessageLiveModel.getMsgLiveData().setValue(clonedList);
                    }
                } else chatResponse.setDeliveryStatus(Constants.TAG_UNREAD);

            } else chatResponse.setDeliveryStatus(Constants.TAG_UNREAD);

            addMessage(chatResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMessage(ChatResponse chatResponse) {
        try {
            ContentValues values = new ContentValues();
            values.put(Constants.TAG_USER_ID, chatResponse.getUserId());
            values.put(Constants.TAG_RECEIVER_ID, chatResponse.getReceiverId());
            values.put(Constants.TAG_CHAT_ID, chatResponse.getChatId());
            values.put(Constants.TAG_CHAT_TYPE, chatResponse.getChatType());
            values.put(Constants.TAG_MSG_ID, chatResponse.getMessageId());
            values.put(Constants.TAG_MSG_TYPE, chatResponse.getMessageType());
            values.put(Constants.TAG_MESSAGE_END, chatResponse.getMessageEnd());
            values.put(Constants.TAG_MESSAGE, chatResponse.getMessage());
            values.put(Constants.TAG_CHAT_TIME, chatResponse.getChatTime());
            values.put(Constants.TAG_RECEIVED_TIME, chatResponse.getReceivedTime());
            values.put(Constants.TAG_DELIVERY_STATUS, chatResponse.getDeliveryStatus());
            values.put(Constants.TAG_THUMBNAIL, chatResponse.getThumbnail());
            values.put(Constants.TAG_PROGRESS, "");
            getWritableDatabase().insertWithOnConflict(TABLE_MESSAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addAdminMessage(AdminMessageResponse.MessageData messageData) {
        try {
            ContentValues values = new ContentValues();
            values.put(Constants.TAG_CHAT_TYPE, messageData.getMsgFrom());
            values.put(Constants.TAG_CHAT_ID, GetSet.getUserId());
            values.put(Constants.TAG_MSG_ID, messageData.getMsgId());
            values.put(Constants.TAG_MSG_TYPE, messageData.getMsgType());
            values.put(Constants.TAG_MESSAGE_END, Constants.TAG_RECEIVED);
            values.put(Constants.TAG_MESSAGE, messageData.getMsgData());
            values.put(Constants.TAG_CREATED_AT, messageData.getMsgAt());
            values.put(Constants.TAG_CHAT_TIME, messageData.getCreateaAt());
            getWritableDatabase().insertWithOnConflict(TABLE_MESSAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ChatResponse> getMessages(String chatId, int limit, int offset) {
        SQLiteDatabase db = getReadableDatabase();

        /*String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + Constants.TAG_CHAT_ID + "='" + chatId + "'" +
                " ORDER BY " + Constants.TAG_CHAT_TIME + " DESC " + " LIMIT " + limit + " OFFSET " + offset;
        Cursor cursor = db.rawQuery(selectQuery, null);*/

        String[] projection = {Constants.TAG_CHAT_ID, Constants.TAG_RECEIVER_ID, Constants.TAG_MSG_TYPE, Constants.TAG_CHAT_TYPE,
                Constants.TAG_MESSAGE_END, Constants.TAG_MESSAGE, Constants.TAG_MSG_ID, Constants.TAG_CHAT_TIME, Constants.TAG_DELIVERY_STATUS,
                Constants.TAG_PROGRESS, Constants.TAG_THUMBNAIL};
        String selection = Constants.TAG_CHAT_ID + "=?"; //this must be array
        String[] selectionArgs = new String[]{chatId}; //this must be array
        String orderBy = Constants.TAG_CHAT_TIME + " DESC " + " LIMIT " + limit + " OFFSET " + offset;
        Cursor cursor = db.query(TABLE_MESSAGES, projection, selection, selectionArgs, null, null, orderBy);

        List<ChatResponse> chatList = new ArrayList<>();
        // looping through all rows and adding to list
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                ChatResponse chatResponse = new ChatResponse();
                chatResponse.setChatId(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_ID)));
                chatResponse.setUserId(GetSet.getUserId());
                chatResponse.setReceiverId(cursor.getString(cursor.getColumnIndex(Constants.TAG_RECEIVER_ID)));
                chatResponse.setMessageType(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_TYPE)));
                chatResponse.setChatType(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TYPE)));
                chatResponse.setMessageEnd(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE_END)));
                chatResponse.setMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE)));
                chatResponse.setMessageId(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_ID)));
                chatResponse.setChatTime(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TIME)));
                chatResponse.setDeliveryStatus(cursor.getString(cursor.getColumnIndex(Constants.TAG_DELIVERY_STATUS)));
                chatResponse.setProgress(cursor.getString(cursor.getColumnIndex(Constants.TAG_PROGRESS)));
                chatResponse.setThumbnail(cursor.getString(cursor.getColumnIndex(Constants.TAG_THUMBNAIL)));

                chatList.add(chatResponse);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return chatList;
    }

    public List<ChatResponse> getAllMessages(String chatId) {
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + Constants.TAG_CHAT_ID + "='" + chatId + "'" +
                " ORDER BY " + Constants.TAG_CHAT_TIME + " DESC ";

        List<ChatResponse> chatList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                ChatResponse chatResponse = new ChatResponse();
                chatResponse.setChatId(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_ID)));
                chatResponse.setUserId(GetSet.getUserId());
                chatResponse.setReceiverId(cursor.getString(cursor.getColumnIndex(Constants.TAG_RECEIVER_ID)));
                chatResponse.setMessageType(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_TYPE)));
                chatResponse.setChatType(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TYPE)));
                chatResponse.setMessageEnd(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE_END)));
                chatResponse.setMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE)));
                chatResponse.setMessageId(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_ID)));
                chatResponse.setChatTime(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TIME)));
                chatResponse.setDeliveryStatus(cursor.getString(cursor.getColumnIndex(Constants.TAG_DELIVERY_STATUS)));
                chatResponse.setProgress(cursor.getString(cursor.getColumnIndex(Constants.TAG_PROGRESS)));
                chatResponse.setThumbnail(cursor.getString(cursor.getColumnIndex(Constants.TAG_THUMBNAIL)));

                chatList.add(chatResponse);
                //Log.v("Items", "Id="+cursor.getString(0)+"ItemId="+cursor.getString(1)+" Liked="+cursor.getString(2)+" Report="+cursor.getString(3)+" Share="+cursor.getString(4));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return chatList;
    }

    public List<ChatResponse> getAdminMessages(String chatId, int limit, int offset) {
        SQLiteDatabase db = getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + Constants.TAG_CHAT_ID + "='" + chatId + "'" +
                " ORDER BY " + Constants.TAG_CHAT_TIME + " DESC " + " LIMIT " + limit + " OFFSET " + offset;
        Cursor cursor = db.rawQuery(selectQuery, null);

        /*String[] projection = {Constants.TAG_CHAT_ID, Constants.TAG_RECEIVER_ID, Constants.TAG_MSG_TYPE,Constants.TAG_MESSAGE,
                Constants.TAG_MESSAGE_END, Constants.TAG_MSG_ID, Constants.TAG_CHAT_TIME, Constants.TAG_CREATED_AT,
                Constants.TAG_CHAT_TYPE};
        String selection = Constants.TAG_CHAT_ID + "=?";
        String[] selectionArgs = new String[]{chatId}; //this must be array
        String orderBy = Constants.TAG_CHAT_TIME + " DESC " + " LIMIT " + limit + " OFFSET " + offset;
        Cursor cursor = db.query(TABLE_MESSAGES, projection, selection, selectionArgs, null, null, orderBy);*/

        List<ChatResponse> chatList = new ArrayList<>();
        // looping through all rows and adding to list
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                ChatResponse chatResponse = new ChatResponse();
                chatResponse.setChatId(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_ID)));
                chatResponse.setReceiverId(GetSet.getUserId());
                chatResponse.setMessageType(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_TYPE)));
                chatResponse.setMessageEnd(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE_END)));
                chatResponse.setMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE)));
                chatResponse.setMessageId(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_ID)));
                chatResponse.setChatTime(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TIME)));
                chatResponse.setCreatedAt(cursor.getString(cursor.getColumnIndex(Constants.TAG_CREATED_AT)));
                chatResponse.setChatType(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TYPE)));

                chatList.add(chatResponse);
                //Log.v("Items", "Id="+cursor.getString(0)+"ItemId="+cursor.getString(1)+" Liked="+cursor.getString(2)+" Report="+cursor.getString(3)+" Share="+cursor.getString(4));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return chatList;
    }

    public void updateProgress(String key, ChatResponse chatResponse, String value) {
        if (isUserExist(chatResponse.getMessageId())) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(key, value);
            db.update(TABLE_MESSAGES, values, Constants.TAG_MSG_ID + " =? ",
                    new String[]{chatResponse.getMessageId()});
            if (MessageLiveModel.msgLiveData != null) {
                if (MessageLiveModel.msgLiveData.getValue().contains(chatResponse)) {
                    int index = MessageLiveModel.msgLiveData.getValue().indexOf(chatResponse);
                    chatResponse.setProgress(value);
                    MessageLiveModel.msgLiveData.getValue().get(index).setProgress(value);
                }
            }
        }
    }

    public boolean isUserExist(String messageId) {
        long line = DatabaseUtils.longForQuery(getReadableDatabase(), "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE " + Constants.TAG_MSG_ID + "=?",
                new String[]{messageId});
        return line > 0;
    }

    public void deleteMessages(String chatId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_MESSAGES, Constants.TAG_CHAT_ID + " =? ", new String[]{chatId});
        } catch (Exception e) {
            Logging.e(TAG, "deleteAllChats: " + e.getMessage());
        }
    }

    public int getMessagesCount(String chatId) {
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + Constants.TAG_CHAT_ID + "='" + chatId + "'";
        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public ChatResponse getLastReadMessage(String chatId) {
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + Constants.TAG_CHAT_ID + "='" + chatId + "'" +
                " AND " + Constants.TAG_MESSAGE_END + " ='" + Constants.TAG_SEND + "'" + " AND " + Constants.TAG_DELIVERY_STATUS + " ='" + Constants.TAG_READ + "'" +
                " ORDER BY " + Constants.TAG_CHAT_TIME + " DESC " + " LIMIT " + 1;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ChatResponse chatResponse = new ChatResponse();

        if (cursor.getCount() > 0 && cursor.moveToLast()) {
            chatResponse.setChatId(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_ID)));
            chatResponse.setUserId(GetSet.getUserId());
            chatResponse.setReceiverId(cursor.getString(cursor.getColumnIndex(Constants.TAG_RECEIVER_ID)));
            chatResponse.setMessageType(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_TYPE)));
            chatResponse.setChatType(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TYPE)));
            chatResponse.setMessageEnd(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE_END)));
            chatResponse.setMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE)));
            chatResponse.setMessageId(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_ID)));
            chatResponse.setChatTime(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TIME)));
            chatResponse.setDeliveryStatus(cursor.getString(cursor.getColumnIndex(Constants.TAG_DELIVERY_STATUS)));
            chatResponse.setProgress(cursor.getString(cursor.getColumnIndex(Constants.TAG_PROGRESS)));
            chatResponse.setThumbnail(cursor.getString(cursor.getColumnIndex(Constants.TAG_THUMBNAIL)));
        }
        cursor.close();
        db.close();
        return chatResponse;
    }

    public ChatResponse getLastAdminMessage() {
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + Constants.TAG_CHAT_TYPE + "='" + Constants.TAG_ADMIN + "'" +
                " ORDER BY " + Constants.TAG_CHAT_TIME + " DESC " + " LIMIT " + 1;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ChatResponse chatResponse = new ChatResponse();

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            chatResponse.setChatId(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_ID)));
            chatResponse.setMessageType(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_TYPE)));
            chatResponse.setChatType(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TYPE)));
            chatResponse.setMessageEnd(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE_END)));
            chatResponse.setMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE)));
            chatResponse.setMessageId(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_ID)));
            chatResponse.setChatTime(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TIME)));
            chatResponse.setCreatedAt(cursor.getString(cursor.getColumnIndex(Constants.TAG_CREATED_AT)));
        }
        cursor.close();
        db.close();
        return chatResponse;
    }

    // Insert a recent message into the database
    public void saveRecentModel(JSONObject jsonObject, String onlineStatus) {
        try {
           /* List<ChatResponse> temp = new ArrayList<>();
            if (MessageLiveModel.recentLiveData != null)
                temp = MessageLiveModel.recentLiveData.getValue();
            List<ChatResponse> clonedList;
            if (temp != null) {
                clonedList = new ArrayList<>(temp);
            } else {
                clonedList = new ArrayList<>();
            }*/
            String chatId = GetSet.getUserId() + jsonObject.optString(Constants.TAG_USER_ID);
            int unseenCount = getUnseenMessagesCount(chatId);
            /*if (!(App.getCurrentActivity() instanceof ChatActivity)) {
                ChatActivity activity = (ChatActivity) App.getCurrentActivity();
                if (!("" + activity.partnerId).equals(jsonObject.optString(Constants.TAG_USER_ID))) {
                    unseenCount = unseenCount + 1;
                }
            }*/
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setChatId(chatId);
            chatResponse.setUserId(jsonObject.optString(Constants.TAG_USER_ID));
            chatResponse.setUserName(jsonObject.optString(Constants.TAG_USER_NAME));
            chatResponse.setUserImage(jsonObject.optString(Constants.TAG_USER_IMAGE));
            chatResponse.setReceiverId(jsonObject.optString(Constants.TAG_RECEIVER_ID));
            chatResponse.setChatType(jsonObject.optString(Constants.TAG_CHAT_TYPE));
            chatResponse.setMessageType(jsonObject.optString(Constants.TAG_MSG_TYPE));
            chatResponse.setUnreadCount(unseenCount);
            chatResponse.setMessageId(jsonObject.optString(Constants.TAG_MSG_ID));
            chatResponse.setChatTime(jsonObject.optString(Constants.TAG_CHAT_TIME));
            if (onlineStatus.equals(Constants.ONLINE)) {
                chatResponse.setOnlineStatus(true);
            }
            addRecentMessage(chatResponse);

            /*ChatResponse tempResponse = getRecentByChatId(chatResponse.getChatId(), chatResponse.getUserId());
            for (ChatResponse response : clonedList) {
                if (response.getChatId().equals(tempResponse.getChatId())) {
                    int index = clonedList.indexOf(response);
                    clonedList.set(index, tempResponse);
                    break;
                }
            }

            if (!clonedList.contains(tempResponse)) {
                clonedList.add(tempResponse);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addRecentMessage(ChatResponse chatResponse) {
        try {
            addChats(chatResponse);

            ContentValues values = new ContentValues();
            values.put(Constants.TAG_USER_ID, chatResponse.getUserId());
            values.put(Constants.TAG_RECEIVER_ID, chatResponse.getReceiverId());
            values.put(Constants.TAG_CHAT_ID, chatResponse.getChatId());
            values.put(Constants.TAG_RECENT_TYPE, chatResponse.getChatType());
            values.put(Constants.TAG_MSG_TYPE, chatResponse.getMessageType());
            values.put(Constants.TAG_UNREAD_COUNT, "" + chatResponse.getUnreadCount());
            values.put(Constants.TAG_MSG_ID, chatResponse.getMessageId());
            values.put(Constants.TAG_CHAT_TIME, chatResponse.getChatTime());

            getWritableDatabase().insertWithOnConflict(TABLE_RECENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addAdminRecentMessage(AdminMessageResponse.MessageData messageData, int unseenCount) {
        try {
            String chatId = GetSet.getUserId();
            addAdminChats();
            ContentValues values = new ContentValues();
            values.put(Constants.TAG_CHAT_ID, chatId);
            values.put(Constants.TAG_RECENT_TYPE, messageData.getMsgFrom());
            values.put(Constants.TAG_MSG_TYPE, messageData.getMsgType());
            values.put(Constants.TAG_UNREAD_COUNT, "" + unseenCount);
            values.put(Constants.TAG_MSG_ID, messageData.getMsgId());
            values.put(Constants.TAG_CHAT_TIME, messageData.getMsgAt());
            values.put(Constants.TAG_CREATED_AT, "" + messageData.getCreateaAt());

            getWritableDatabase().insertWithOnConflict(TABLE_RECENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAdminChats() {
        try {
            ContentValues values = new ContentValues();
            values.put(Constants.TAG_CHAT_ID, GetSet.getUserId());
            values.put(Constants.TAG_USER_NAME, App.getInstance().getString(R.string.app_name) + " " + App.getInstance().getString(R.string.team));

            getWritableDatabase().insertWithOnConflict(TABLE_CHATS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteRecent(String chatId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_RECENTS, Constants.TAG_CHAT_ID + " =? ", new String[]{chatId});
        } catch (Exception e) {
            Logging.e(TAG, "deleteAllChats: " + e.getMessage());
        }
    }

    public List<ChatResponse> getRecentMessages(int limit, int offset) {

        String selectQuery = "SELECT * FROM " + TABLE_RECENTS + " LEFT JOIN " + TABLE_MESSAGES + " ON " + TABLE_RECENTS + "." +
                Constants.TAG_MSG_ID + " = " + TABLE_MESSAGES + "." + Constants.TAG_MSG_ID + " INNER JOIN " +
                TABLE_CHATS + " ON " + TABLE_RECENTS + "." + Constants.TAG_CHAT_ID + " = " + TABLE_CHATS + "." +
                Constants.TAG_CHAT_ID + " ORDER BY " + TABLE_RECENTS + "." + Constants.TAG_RECENT_TYPE + " ASC," +
                TABLE_MESSAGES + "." + Constants.TAG_RECEIVED_TIME + " DESC " + " LIMIT " + limit + " OFFSET " + offset;

        List<ChatResponse> chatList = new ArrayList<>();
        SQLiteDatabase db = dbInstance.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                ChatResponse chatResponse = new ChatResponse();
                chatResponse.setChatId(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_ID)));
                chatResponse.setReceiverId(cursor.getString(cursor.getColumnIndex(Constants.TAG_RECEIVER_ID)));
                chatResponse.setUserId(cursor.getString(cursor.getColumnIndex(Constants.TAG_USER_ID)));
                chatResponse.setUserName(cursor.getString(cursor.getColumnIndex(Constants.TAG_USER_NAME)));
                chatResponse.setUserImage(cursor.getString(cursor.getColumnIndex(Constants.TAG_USER_IMAGE)));
                chatResponse.setOnlineStatus(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Constants.TAG_ONLINE_STATUS))));
                chatResponse.setTypingStatus(cursor.getString(cursor.getColumnIndex(Constants.TAG_TYPING_STATUS)));
                if (cursor.getString(cursor.getColumnIndex(Constants.TAG_BLOCKED_BY_ME)) != null)
                    chatResponse.setBlockStatus(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Constants.TAG_BLOCKED_BY_ME))));
                else
                    chatResponse.setBlockStatus(false);

                chatResponse.setMessageType(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_TYPE)));
                chatResponse.setMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE)));
                chatResponse.setMessageId(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_ID)));
                chatResponse.setChatType(cursor.getString(cursor.getColumnIndex(Constants.TAG_RECENT_TYPE)));
                chatResponse.setChatTime(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TIME)));
                chatResponse.setDeliveryStatus(cursor.getString(cursor.getColumnIndex(Constants.TAG_DELIVERY_STATUS)));
                chatResponse.setProgress(cursor.getString(cursor.getColumnIndex(Constants.TAG_PROGRESS)));
                chatList.add(chatResponse);
                //Log.v("Items", "Id="+cursor.getString(0)+"ItemId="+cursor.getString(1)+" Liked="+cursor.getString(2)+" Report="+cursor.getString(3)+" Share="+cursor.getString(4));
            } while (cursor.moveToNext());
            cursor.close();
            db.close();
        }

        return chatList;
    }

    public List<ChatResponse> getAllRecentMessages() {

        String selectQuery = "SELECT * FROM " + TABLE_RECENTS + " LEFT JOIN " + TABLE_MESSAGES + " ON " + TABLE_RECENTS + "." +
                Constants.TAG_MSG_ID + " = " + TABLE_MESSAGES + "." + Constants.TAG_MSG_ID + " INNER JOIN " +
                TABLE_CHATS + " ON " + TABLE_RECENTS + "." + Constants.TAG_CHAT_ID + " = " + TABLE_CHATS + "." +
                Constants.TAG_CHAT_ID + " ORDER BY " + TABLE_RECENTS + "." + Constants.TAG_RECENT_TYPE + " ASC," +
                TABLE_MESSAGES + "." + Constants.TAG_RECEIVED_TIME + " DESC ";

        List<ChatResponse> chatList = new ArrayList<>();
        SQLiteDatabase db = dbInstance.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ChatResponse chatResponse = new ChatResponse();
                chatResponse.setChatId(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_ID)));
                chatResponse.setReceiverId(cursor.getString(cursor.getColumnIndex(Constants.TAG_RECEIVER_ID)));
                chatResponse.setUserId(cursor.getString(cursor.getColumnIndex(Constants.TAG_USER_ID)));
                chatResponse.setUserName(cursor.getString(cursor.getColumnIndex(Constants.TAG_USER_NAME)));
                chatResponse.setUserImage(cursor.getString(cursor.getColumnIndex(Constants.TAG_USER_IMAGE)));
                chatResponse.setOnlineStatus(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Constants.TAG_ONLINE_STATUS))));
                chatResponse.setTypingStatus(cursor.getString(cursor.getColumnIndex(Constants.TAG_TYPING_STATUS)));

                chatResponse.setMessageType(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_TYPE)));
                chatResponse.setMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE)));
                chatResponse.setMessageId(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_ID)));
                chatResponse.setChatType(cursor.getString(cursor.getColumnIndex(Constants.TAG_RECENT_TYPE)));
                chatResponse.setChatTime(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TIME)));
                chatResponse.setDeliveryStatus(cursor.getString(cursor.getColumnIndex(Constants.TAG_DELIVERY_STATUS)));
                chatResponse.setProgress(cursor.getString(cursor.getColumnIndex(Constants.TAG_PROGRESS)));
                chatList.add(chatResponse);
                //Log.v("Items", "Id="+cursor.getString(0)+"ItemId="+cursor.getString(1)+" Liked="+cursor.getString(2)+" Report="+cursor.getString(3)+" Share="+cursor.getString(4));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return chatList;
    }

    private ChatResponse getRecentByChatId(String chatId, String userId) {

        String selectQuery = "SELECT * FROM " + TABLE_RECENTS + " LEFT JOIN " + TABLE_MESSAGES + " ON " +
                TABLE_RECENTS + "." + Constants.TAG_USER_ID + " = '" + userId + "'" +
                " AND " + TABLE_RECENTS + "." + Constants.TAG_CHAT_ID + " = '" + chatId + "'" +
                " INNER JOIN " + TABLE_CHATS + " ON " + TABLE_RECENTS + "." + Constants.TAG_CHAT_ID + " = " + TABLE_CHATS + "." + Constants.TAG_CHAT_ID;

        SQLiteDatabase db = dbInstance.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ChatResponse chatResponse = null;
        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            chatResponse = new ChatResponse();
            chatResponse.setChatId(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_ID)));
            chatResponse.setReceiverId(cursor.getString(cursor.getColumnIndex(Constants.TAG_RECEIVER_ID)));
            chatResponse.setUserId(cursor.getString(cursor.getColumnIndex(Constants.TAG_USER_ID)));
            chatResponse.setUserName(cursor.getString(cursor.getColumnIndex(Constants.TAG_USER_NAME)));
            chatResponse.setUserImage(cursor.getString(cursor.getColumnIndex(Constants.TAG_USER_IMAGE)));
            chatResponse.setOnlineStatus(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Constants.TAG_ONLINE_STATUS))));
            chatResponse.setTypingStatus(cursor.getString(cursor.getColumnIndex(Constants.TAG_TYPING_STATUS)));

            chatResponse.setMessageType(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_TYPE)));
            chatResponse.setChatType(cursor.getString(cursor.getColumnIndex(Constants.TAG_RECENT_TYPE)));
            chatResponse.setMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE)));
            chatResponse.setMessageId(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_ID)));
            chatResponse.setChatTime(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TIME)));
            chatResponse.setDeliveryStatus(cursor.getString(cursor.getColumnIndex(Constants.TAG_DELIVERY_STATUS)));
            chatResponse.setProgress(cursor.getString(cursor.getColumnIndex(Constants.TAG_PROGRESS)));
        }
        cursor.close();
        db.close();

        return chatResponse;
    }

    public int getUnseenMessagesCount(String chatId) {
        String selectQuery = "SELECT " + Constants.TAG_UNREAD_COUNT + " FROM " + TABLE_RECENTS + " WHERE " + Constants.TAG_CHAT_ID + " = '" + chatId + "'";
        Cursor cursor = getWritableDatabase().rawQuery(selectQuery, null);
        int count = cursor.getCount();
        String unreadCount = null;
        if (count > 0) {
            cursor.moveToLast();
            unreadCount = cursor.getString(cursor.getColumnIndex(Constants.TAG_UNREAD_COUNT));
        }
        cursor.close();
        Log.i(TAG, "getUnseenMessagesCount: " + unreadCount);
        return unreadCount == null ? 0 : Integer.parseInt(unreadCount);
    }

    public void addChats(ChatResponse chatResponse) {
        try {
            ContentValues values = new ContentValues();
            values.put(Constants.TAG_CHAT_ID, chatResponse.getChatId());
            values.put(Constants.TAG_USER_ID, chatResponse.getUserId());
            values.put(Constants.TAG_USER_NAME, chatResponse.getUserName());
            values.put(Constants.TAG_USER_IMAGE, chatResponse.getUserImage());
            if (chatResponse.getOnlineStatus()) {
                values.put(Constants.TAG_ONLINE_STATUS, Constants.TAG_TRUE);
            }
            getWritableDatabase().insertWithOnConflict(TABLE_CHATS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChatResponse getAdminChat(String chatId) {
        String selectQuery = "SELECT * FROM " + TABLE_CHATS + " WHERE " + Constants.TAG_CHAT_ID + "='" + chatId + "'";

        SQLiteDatabase db = dbInstance.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ChatResponse chatResponse = null;
        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            chatResponse = new ChatResponse();
            chatResponse.setChatId(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_ID)));
            chatResponse.setUserName(cursor.getString(cursor.getColumnIndex(Constants.TAG_USER_NAME)));
        }
        cursor.close();
        db.close();
        return chatResponse;
    }

    public String getBlockStatus(String chatId) {
        String selectQuery = "SELECT " + Constants.TAG_BLOCKED_BY_ME + " FROM " + TABLE_CHATS + " WHERE " + Constants.TAG_CHAT_ID + "='" + chatId + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String blockStatus = Constants.TAG_FALSE;
        // looping through all rows and adding to list
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            blockStatus = cursor.getString(cursor.getColumnIndex(Constants.TAG_BLOCKED_BY_ME));
        }
        if (blockStatus == null) {
            blockStatus = Constants.TAG_FALSE;
        }
        cursor.close();
        db.close();
        return blockStatus;
    }

    public void updateChatDB(String chatId, String key, String blockStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key, blockStatus);
        db.update(TABLE_CHATS, values, Constants.TAG_CHAT_ID + " =? ",
                new String[]{chatId});
    }

    public void updateMessage(String messageId, String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key, value);
        db.update(TABLE_MESSAGES, values, Constants.TAG_MSG_ID + " =? ",
                new String[]{messageId});

    }

 public void updateImageMessage(String messageId, String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key, value);
        db.update(TABLE_CHATS, values, Constants.TAG_MSG_ID + " =? ",
                new String[]{messageId});

    }

    public void updateRecent(String chatId, String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key, value);
        db.update(TABLE_RECENTS, values, Constants.TAG_CHAT_ID + " =? ",
                new String[]{chatId});
    }

    public void updateChat(String chatId, String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key, value);
        db.update(TABLE_CHATS, values, Constants.TAG_CHAT_ID + " =? ",
                new String[]{chatId});
    }

    public void updateChatsOnline() {
        String selectQuery = "UPDATE " + TABLE_CHATS + " SET " + Constants.TAG_ONLINE_STATUS + " ='" + Constants.OFFLINE + "'";
        getWritableDatabase().execSQL(selectQuery);
    }

    public void updateChatsTyping() {
        String selectQuery = "UPDATE " + TABLE_CHATS + " SET " + Constants.TAG_TYPING_STATUS + " ='" + Constants.TAG_UNTYPING + "'";
        getWritableDatabase().execSQL(selectQuery);
    }

    public void insertBlockStatus(String chatId, String partnerId, String partnerName, String partnerImage, String blockStatus) {
        try {
            ContentValues values = new ContentValues();
            values.put(Constants.TAG_USER_ID, partnerId);
            values.put(Constants.TAG_CHAT_ID, chatId);
            values.put(Constants.TAG_USER_NAME, partnerName);
            values.put(Constants.TAG_USER_IMAGE, partnerImage);
            values.put(Constants.TAG_BLOCKED_BY_ME, blockStatus);

            getWritableDatabase().insertWithOnConflict(TABLE_CHATS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isChatIdExists(String chatId) {
        SQLiteDatabase db = getReadableDatabase();
        long line = DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + TABLE_CHATS + " WHERE " + Constants.TAG_CHAT_ID + "=?",
                new String[]{chatId});
        return line > 0;
    }

    public void updateMessageReadStatus(String chatId, String userId) {
        SQLiteDatabase db = getWritableDatabase();
        boolean exists = isChatIdExistInMsgs(chatId);
        ContentValues values = new ContentValues();
        values.put(Constants.TAG_DELIVERY_STATUS, Constants.TAG_READ);

        if (exists) {
            db.update(TABLE_MESSAGES, values, Constants.TAG_CHAT_ID + " =? AND " + Constants.TAG_DELIVERY_STATUS + " =? AND " + Constants.TAG_RECEIVER_ID + " =? ",
                    new String[]{chatId, Constants.TAG_RECEIVED, userId});
        }
    }

    public boolean isChatIdExistInMsgs(String chatId) {
        long line = DatabaseUtils.longForQuery(getReadableDatabase(), "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE " + Constants.TAG_CHAT_ID + "=?",
                new String[]{chatId});
        return line > 0;
    }

    public void updateReadStatus(String chatId) {
        String selectQuery = "UPDATE " + TABLE_MESSAGES + " SET " + Constants.TAG_DELIVERY_STATUS + " ='" + Constants.TAG_READ + "' WHERE " + Constants.TAG_CHAT_ID + " = '" + chatId + "'" +
                " AND " + Constants.TAG_DELIVERY_STATUS + " != '" + Constants.TAG_READ + "'" + " AND " + Constants.TAG_MESSAGE_END + " = '" + Constants.TAG_SEND + "'";
        getWritableDatabase().execSQL(selectQuery);
    }

    public ChatResponse getSingleMessage(String message_id) {

        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + Constants.TAG_MSG_ID + "='" + message_id + "'";

        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);
        if (cursor != null)
            cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            ChatResponse results = new ChatResponse();
            results.setChatId(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_ID)));
            results.setMessageId(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_ID)));
            results.setUserId(cursor.getString(cursor.getColumnIndex(Constants.TAG_USER_ID)));
            results.setMessageType(cursor.getString(cursor.getColumnIndex(Constants.TAG_MSG_TYPE)));
           /* results.attachment = cursor.getString(cursor.getColumnIndex(Constants.TAG_ATTACHMENT));
            results.lat = ApplicationClass.decryptMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_LAT)));
            results.lon = ApplicationClass.decryptMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_LON)));
            results.contact_name = ApplicationClass.decryptMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_CONTACT_NAME)));
            results.contact_phone_no = ApplicationClass.decryptMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_CONTACT_PHONE_NO)));
            results.contact_country_code = ApplicationClass.decryptMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_CONTACT_COUNTRY_CODE)));
            results.isDelete = cursor.getString(cursor.getColumnIndex(Constants.TAG_IS_DELETE));*/
            results.setChatTime(cursor.getString(cursor.getColumnIndex(Constants.TAG_CHAT_TIME)));
            results.setReceiverId(cursor.getString(cursor.getColumnIndex(Constants.TAG_RECEIVER_ID)));
//            results.setSender_id(cursor.getString(cursor.getColumnIndex(Constants.TAG_SENDER_ID)));
            results.setDeliveryStatus(cursor.getString(cursor.getColumnIndex(Constants.TAG_DELIVERY_STATUS)));
            results.setThumbnail(cursor.getString(cursor.getColumnIndex(Constants.TAG_THUMBNAIL)));
            results.setProgress(cursor.getString(cursor.getColumnIndex(Constants.TAG_PROGRESS)));
//            results.statusData = ApplicationClass.decryptMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_STATUS_DATA)));
//            results.setUserName(cursor.getString(cursor.getColumnIndex(Constants.TAG_USER_NAME)));
            results.setMessage(cursor.getString(cursor.getColumnIndex(Constants.TAG_MESSAGE)));
            cursor.close();
            return results;
        }
        return null;
    }

}
