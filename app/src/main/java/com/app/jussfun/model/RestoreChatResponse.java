
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RestoreChatResponse {

    @SerializedName("message_data")
    private List<MessageData> messageData;
    @SerializedName("status")
    private String status;

    public List<MessageData> getMessageData() {
        return messageData;
    }

    public void setMessageData(List<MessageData> messageData) {
        this.messageData = messageData;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public class MessageData {

        @SerializedName("block_status")
        private boolean blockStatus;
        @SerializedName("chat_id")
        private String chatId;
        @SerializedName("chat_time")
        private String chatTime;
        @SerializedName("chat_type")
        private String chatType;
        @SerializedName("message")
        private String message;
        @SerializedName("message_type")
        private String messageType;
        @SerializedName("message_end")
        private String messageEnd;
        @SerializedName("receiver_id")
        private String receiverId;
        @SerializedName("user_id")
        private String userId;
        @SerializedName("user_name")
        private String userName;
        @SerializedName("userdetails")
        private List<UserDetail> userDetails;

        public boolean getBlockStatus() {
            return blockStatus;
        }

        public void setBlockStatus(boolean blockStatus) {
            this.blockStatus = blockStatus;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }

        public String getChatTime() {
            return chatTime;
        }

        public void setChatTime(String chatTime) {
            this.chatTime = chatTime;
        }

        public String getChatType() {
            return chatType;
        }

        public void setChatType(String chatType) {
            this.chatType = chatType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public String getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(String receiverId) {
            this.receiverId = receiverId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getMessageEnd() {
            return messageEnd;
        }

        public void setMessageEnd(String messageEnd) {
            this.messageEnd = messageEnd;
        }

        public List<UserDetail> getUserDetails() {
            return userDetails;
        }

        public void setUserDetails(List<UserDetail> userDetails) {
            this.userDetails = userDetails;
        }

    }

}
