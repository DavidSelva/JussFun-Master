package com.app.jussfun.helper.callback;

import com.app.jussfun.model.CommentsModel;

public interface FeedListener {
    void setParentReplyDetails(int parentPosition, int childPosition, String parentId, String userName);

    void navigateToProfile(CommentsModel.Result result);
}
