package com.app.jussfun.helper.callback;

import com.app.jussfun.model.CommentsModel;

public interface FeedListener {

    void setParentReplyDetails(int parentPosition, int childPosition, String parentId, String userName);

    void setChildReplyDetails(int parentPosition, int childPosition, String parentId, String userName);

    void navigateToProfile(String userId);

    void deleteComment(int parentPos, int position, String postId, String replyId, String type);
}
