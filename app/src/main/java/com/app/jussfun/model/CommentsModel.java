package com.app.jussfun.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CommentsModel {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("user_name")
    @Expose
    private String userName;
    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("user_image")
    @Expose
    private String userImage;
    @SerializedName("result")
    @Expose
    private List<Result> results = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public class Result {

        @SerializedName("comment_id")
        @Expose
        private String commentId;
        @SerializedName("comments")
        @Expose
        private String comments;
        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("user_id")
        @Expose
        private String userId;
        @SerializedName("user_image")
        @Expose
        private String userImage;
        @SerializedName("comment_time")
        @Expose
        private String commentTime;
        @SerializedName("created_time")
        @Expose
        private String createdTime;
        @SerializedName("liked")
        @Expose
        private String liked;
        @SerializedName("like_count")
        @Expose
        private Integer likeCount;
        @SerializedName("reply_status")
        @Expose
        private String replyStatus;
        @SerializedName("reply")
        @Expose
        private List<Reply> reply = null;
        @SerializedName("isReplyVisible")
        @Expose
        private boolean isReplyVisible;

        public String getCommentId() {
            return commentId;
        }

        public void setCommentId(String commentId) {
            this.commentId = commentId;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getCommentTime() {
            return commentTime;
        }

        public void setCommentTime(String commentTime) {
            this.commentTime = commentTime;
        }

        public String getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(String createdTime) {
            this.createdTime = createdTime;
        }

        public String getLiked() {
            return liked;
        }

        public void setLiked(String liked) {
            this.liked = liked;
        }

        public Integer getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(Integer likeCount) {
            this.likeCount = likeCount;
        }

        public String getReplyStatus() {
            return replyStatus;
        }

        public void setReplyStatus(String replyStatus) {
            this.replyStatus = replyStatus;
        }

        public List<Reply> getReply() {
            return reply;
        }

        public void setReply(List<Reply> reply) {
            this.reply = reply;
        }

        public boolean isReplyVisible() {
            return isReplyVisible;
        }

        public void setReplyVisible(boolean isReplyVisible) {
            this.isReplyVisible = isReplyVisible;
        }
    }

    public class Reply {

        @SerializedName("reply_id")
        @Expose
        private String replyId;
        @SerializedName("comments")
        @Expose
        private String comments;
        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("user_id")
        @Expose
        private String userId;
        @SerializedName("user_image")
        @Expose
        private String userImage;
        @SerializedName("comment_time")
        @Expose
        private String commentTime;
        @SerializedName("created_time")
        @Expose
        private Integer createdTime;
        @SerializedName("liked")
        @Expose
        private String liked;
        @SerializedName("like_count")
        @Expose
        private Integer likeCount;

        public String getReplyId() {
            return replyId;
        }

        public void setReplyId(String replyId) {
            this.replyId = replyId;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getCommentTime() {
            return commentTime;
        }

        public void setCommentTime(String commentTime) {
            this.commentTime = commentTime;
        }

        public Integer getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Integer createdTime) {
            this.createdTime = createdTime;
        }

        public String getLiked() {
            return liked;
        }

        public void setLiked(String liked) {
            this.liked = liked;
        }

        public Integer getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(Integer likeCount) {
            this.likeCount = likeCount;
        }

    }
}
