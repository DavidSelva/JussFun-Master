
package com.app.jussfun.model;

import android.database.Observable;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class ChatResponse extends Observable {

    @SerializedName("chat_id")
    private String chatId;
    @SerializedName("receiver_id")
    private String receiverId;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("user_image")
    private String userImage;
    @SerializedName("user_name")
    private String userName;
    @SerializedName("chat_type")
    private String chatType;
    @SerializedName("chat_time")
    private String chatTime;
    @SerializedName("message")
    private String message;
    @SerializedName("message_id")
    private String messageId;
    @SerializedName("message_type")
    private String messageType;
    @SerializedName("message_end")
    private String messageEnd;

    /*For Db purpose*/
    private String sender_id;
    private String deliveryStatus;
    private String progress;
    private String thumbnail;
    private int unreadCount;
    private boolean blockStatus;
    private String type;
    private String createdAt;
    private String receivedTime;
    private boolean onlineStatus;
    private String typingStatus;

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getChatTime() {
        return chatTime;
    }

    public void setChatTime(String chatTime) {
        this.chatTime = chatTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getMessageEnd() {
        return messageEnd;
    }

    public void setMessageEnd(String messageEnd) {
        this.messageEnd = messageEnd;
    }

    public boolean getBlockStatus() {
        return blockStatus;
    }

    public void setBlockStatus(boolean blockStatus) {
        this.blockStatus = blockStatus;
    }

    public boolean isBlockStatus() {
        return blockStatus;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(String receivedTime) {
        this.receivedTime = receivedTime;
    }

    public boolean getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingStatus() {
        return typingStatus;
    }

    public void setTypingStatus(String typingStatus) {
        this.typingStatus = typingStatus;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        boolean result = false;
        if (object == null || object.getClass() != getClass()) {
            result = false;
        } else {
            ChatResponse method = (ChatResponse) object;
            if ((this.messageId != null && this.deliveryStatus != null && this.typingStatus != null) &&
                    this.messageId.equals(method.getMessageId()) &&
                    this.deliveryStatus.equals(method.getDeliveryStatus()) &&
                    this.typingStatus.equals(method.getTypingStatus()) &&
                    this.onlineStatus == method.getOnlineStatus()) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 7 * hash + this.userId.hashCode();
        return hash;
    }
}
