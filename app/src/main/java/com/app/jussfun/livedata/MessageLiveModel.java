package com.app.jussfun.livedata;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.jussfun.R;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.model.ChatResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.base.App;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessageLiveModel extends AndroidViewModel {

    private static final String TAG = MessageLiveModel.class.getSimpleName();
    private DBHelper dbHelper;
    public static MutableLiveData<List<ChatResponse>> msgLiveData;
    public MutableLiveData<List<ChatResponse>> recentLiveData;

    public MessageLiveModel(@NonNull Application application) {
        super(application);
        dbHelper = DBHelper.getInstance(application);
        msgLiveData = new MutableLiveData<List<ChatResponse>>();
        recentLiveData = new MutableLiveData<List<ChatResponse>>();
    }

    public static MutableLiveData<List<ChatResponse>> getMsgLiveData() {
        return msgLiveData;
    }

    public MutableLiveData<List<ChatResponse>> getRecentLiveData() {
        return recentLiveData;
    }

    public LiveData<List<ChatResponse>> getMessages(Context context, String chatId, int limit, int offset) {
        List<ChatResponse> tempList = msgLiveData.getValue();
        if (tempList == null) {
            tempList = new ArrayList<>();
        }
        tempList.addAll(getMessagesByDate(context, dbHelper.getMessages(chatId, limit, offset)));
        msgLiveData.setValue(tempList);
        return msgLiveData;
    }

    public LiveData<List<ChatResponse>> getAdminMessages(Context context, String chatId, int limit, int offset) {
        List<ChatResponse> tempList = msgLiveData.getValue();
        if (tempList == null) {
            tempList = new ArrayList<>();
        }
        tempList.addAll(getMessagesByDate(context, dbHelper.getAdminMessages(chatId, limit, offset)));
        msgLiveData.setValue(tempList);
        return msgLiveData;
    }

    public void addMessage(Context context, JSONObject json) {
        /*Add Date for first time*/
        if (msgLiveData.getValue() != null) {
            if (msgLiveData.getValue().size() == 0) {
                ChatResponse response = new ChatResponse();
                response.setReceiverId(json.optString(Constants.TAG_RECEIVER_ID));
                response.setUserId(GetSet.getUserId());
                response.setMessageType(Constants.TAG_DATE);
                response.setChatTime(AppUtils.getChatDate(context, json.optString(Constants.TAG_CHAT_TIME)));
                msgLiveData.getValue().add(response);
            }
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setChatId(json.optString(Constants.TAG_CHAT_ID));
            chatResponse.setUserId(json.optString(Constants.TAG_USER_ID));
            chatResponse.setReceiverId(json.optString(Constants.TAG_RECEIVER_ID));
            chatResponse.setMessageType(json.optString(Constants.TAG_MSG_TYPE));
            chatResponse.setMessageEnd(json.optString(Constants.TAG_MESSAGE_END));
            chatResponse.setMessage(json.optString(Constants.TAG_MESSAGE));
            chatResponse.setMessageId(json.optString(Constants.TAG_MSG_ID));
            chatResponse.setChatTime(json.optString(Constants.TAG_CHAT_TIME));
            chatResponse.setReceivedTime(AppUtils.getCurrentUTCTime(App.getInstance()));
            chatResponse.setChatType(json.optString(Constants.TAG_CHAT_TYPE));
            chatResponse.setDeliveryStatus(json.optString(Constants.TAG_DELIVERY_STATUS));
            chatResponse.setThumbnail(json.optString(Constants.TAG_THUMBNAIL));
            chatResponse.setProgress(json.optString(Constants.TAG_PROGRESS));
            dbHelper.addMessage(chatResponse);
            msgLiveData.getValue().add(0, chatResponse);
            msgLiveData.setValue(msgLiveData.getValue());
        }
    }


    private List<ChatResponse> getMessagesByType(Context context, List<ChatResponse> chatResponses) {
        for (ChatResponse chatResponse : chatResponses) {
            if (chatResponse.getMessageType().equals(Constants.TAG_IMAGE)) {
                chatResponse.setMessage(context.getString(R.string.image));
            }
        }
        return chatResponses;
    }

    public LiveData<List<ChatResponse>> getRecentChats(int limit, int offset) {

        List<ChatResponse> tempList = recentLiveData.getValue();
        if (tempList == null || offset == 0) {
            tempList = new ArrayList<>();
        }
        tempList.addAll(dbHelper.getRecentMessages(limit, offset));
        recentLiveData.setValue(tempList);

//        recentLiveData.setValue(dbHelper.getAllRecentMessages());
        return recentLiveData;
    }

    public LiveData<List<ChatResponse>> getRecents() {
        List<ChatResponse> tempList = new ArrayList<>();
        tempList.addAll(dbHelper.getAllRecentMessages());
        recentLiveData.setValue(tempList);
        return recentLiveData;
    }

    public List<ChatResponse> getChats() {
        return recentLiveData.getValue();
    }

    public void addRecentChat(JSONObject jsonObject) {
        try {
            int unseenCount = dbHelper.getUnseenMessagesCount(jsonObject.optString(Constants.TAG_CHAT_ID));
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setUserId(jsonObject.optString(Constants.TAG_USER_ID));
            chatResponse.setReceiverId(jsonObject.optString(Constants.TAG_RECEIVER_ID));
            chatResponse.setChatId(jsonObject.optString(Constants.TAG_CHAT_ID));
            chatResponse.setUserName(jsonObject.optString(Constants.TAG_USER_NAME));
            chatResponse.setUserImage(jsonObject.optString(Constants.TAG_USER_IMAGE));
            chatResponse.setChatType(jsonObject.optString(Constants.TAG_CHAT_TYPE));
            chatResponse.setUnreadCount(unseenCount);
            chatResponse.setMessageId(jsonObject.optString(Constants.TAG_MSG_ID));
            chatResponse.setChatTime(jsonObject.optString(Constants.TAG_CHAT_TIME));
            chatResponse.setMessageType(Constants.TAG_MSG_TYPE);
            dbHelper.addRecentMessage(chatResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<ChatResponse> getMessagesByDate(Context context, List<ChatResponse> tmpList) {
        List<ChatResponse> msgList = new ArrayList<>();
        if (tmpList.size() == 1) {
            msgList.add(tmpList.get(0));
            ChatResponse response = new ChatResponse();
            response.setUserId(GetSet.getUserId());
            response.setMessageType(Constants.TAG_DATE);
            response.setChatTime(AppUtils.getChatDate(App.getInstance(), tmpList.get(0).getChatTime()));
            msgList.add(response);

        } else {
            for (int i = 0; i < tmpList.size(); i++) {
                String date1 = AppUtils.getDateFromUTC(context, tmpList.get(i).getChatTime());
                if (i + 1 < tmpList.size()) {
                    String date2 = AppUtils.getDateFromUTC(context, tmpList.get(i + 1).getChatTime());
                    boolean sameDay = date1.trim().equalsIgnoreCase(date2.trim());

                    if (sameDay) {
                        msgList.add(tmpList.get(i));
                    } else {
                        msgList.add(tmpList.get(i));
                        ChatResponse response = new ChatResponse();
                        response.setUserId(GetSet.getUserId());
                        response.setMessageType(Constants.TAG_DATE);
                        response.setChatTime(AppUtils.getChatDate(context, tmpList.get(i).getChatTime()));
                        msgList.add(response);
                    }
                } else {
                    msgList.add(tmpList.get(i));
                }
                if (i == tmpList.size() - 1) {
                    ChatResponse response = new ChatResponse();
                    response.setUserId(GetSet.getUserId());
                    response.setMessageType(Constants.TAG_DATE);
                    response.setChatTime(AppUtils.getChatDate(context, tmpList.get(i).getChatTime()));
                    msgList.add(response);
                }
            }
        }

        return msgList;
    }

    public void changeSentStatus() {
        List<ChatResponse> temp = new ArrayList<>();
        if (msgLiveData.getValue() != null) {
            for (ChatResponse chatResponse : msgLiveData.getValue()) {
                if (chatResponse.getMessageEnd() != null && chatResponse.getMessageEnd().equals(Constants.TAG_SEND))
                    chatResponse.setDeliveryStatus(Constants.TAG_READ);
                temp.add(chatResponse);
            }
            msgLiveData.setValue(temp);
        }
    }

    public void changeLiveStatus(JSONArray usersArray) {
        dbHelper.updateChatsOnline();
        if (recentLiveData.getValue() != null) {
            List<ChatResponse> tempList = recentLiveData.getValue();
            for (ChatResponse chatResponse : tempList) {
                for (int j = 0; j < usersArray.length(); j++) {
                    try {
                        JSONObject userObject = usersArray.getJSONObject(j);
                        if (userObject.getString(Constants.TAG_RECEIVER_ID).equals(chatResponse.getUserId())) {
                            chatResponse.setOnlineStatus(Boolean.parseBoolean(userObject.getString(Constants.TAG_ONLINE_STATUS)));
                            dbHelper.updateChat(chatResponse.getChatId(), Constants.TAG_ONLINE_STATUS, "" + chatResponse.getOnlineStatus());
                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            recentLiveData.setValue(tempList);
        }
    }

    public void changeTypingStatus(JSONObject jsonObject) throws JSONException {
//        dbHelper.updateChatsTyping();
        int i = 0;
        if (recentLiveData.getValue() != null) {
            for (ChatResponse chatResponse : recentLiveData.getValue()) {
                if (jsonObject.getString(Constants.TAG_USER_ID).equals(chatResponse.getUserId())) {
                    chatResponse.setTypingStatus(jsonObject.getString(Constants.TAG_TYPING_STATUS));
                    dbHelper.updateChat(chatResponse.getChatId(), Constants.TAG_TYPING_STATUS, "" + chatResponse.getTypingStatus());
                    break;
                }
            }
            recentLiveData.setValue(recentLiveData.getValue());
        }
    }

    public void updateImageUpload(JSONObject jsonObject) {
        dbHelper.updateMessage(jsonObject.optString(Constants.TAG_MSG_ID), Constants.TAG_PROGRESS, Constants.TAG_COMPLETED);
        if (msgLiveData.getValue() != null) {
            msgLiveData.getValue().get(0).setProgress(Constants.TAG_COMPLETED);
        }
    }

    public void addAdminMessage(ChatResponse chatResponse) {
        if (msgLiveData.getValue() != null) {
            msgLiveData.getValue().add(0, chatResponse);
            msgLiveData.setValue(msgLiveData.getValue());
        }
    }
}