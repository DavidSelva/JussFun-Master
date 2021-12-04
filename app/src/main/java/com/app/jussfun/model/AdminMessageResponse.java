
package com.app.jussfun.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdminMessageResponse {

    @SerializedName("message_data")
    private List<MessageData> messageData;
    @Expose
    private String status;
    @Expose
    private String message;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public class MessageData {

        @SerializedName("msg_at")
        private String msgAt;
        @SerializedName("created_at")
        private String createaAt;
        @SerializedName("msg_data")
        private String msgData;
        @SerializedName("msg_from")
        private String msgFrom;
        @SerializedName("msg_id")
        private String msgId;
        @SerializedName("msg_type")
        private String msgType;

        public String getMsgAt() {
            return msgAt;
        }

        public void setMsgAt(String msgAt) {
            this.msgAt = msgAt;
        }

        public String getMsgData() {
            return msgData;
        }

        public void setMsgData(String msgData) {
            this.msgData = msgData;
        }

        public String getMsgFrom() {
            return msgFrom;
        }

        public void setMsgFrom(String msgFrom) {
            this.msgFrom = msgFrom;
        }

        public String getMsgId() {
            return msgId;
        }

        public void setMsgId(String msgId) {
            this.msgId = msgId;
        }

        public String getMsgType() {
            return msgType;
        }

        public void setMsgType(String msgType) {
            this.msgType = msgType;
        }

        public String getCreateaAt() {
            return createaAt;
        }

        public void setCreateaAt(String createaAt) {
            this.createaAt = createaAt;
        }
    }

}
