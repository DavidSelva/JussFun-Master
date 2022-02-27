package com.app.jussfun.helper.callback;

import android.view.View;

import com.app.jussfun.model.Feeds;
import com.app.jussfun.ui.feed.MediaListViewHolder;

public interface OnMenuClickListener {

    void onMenuClicked(View view, Feeds resultsItem, int adapterPosition);

    void onUserClicked(View view, Feeds resultsItem, int adapterPosition);

    void onCommentClicked(View view, Feeds resultsItem, int adapterPosition);

    void onShareClicked(View view, Feeds resultsItem, int adapterPosition);

    void onDeleteClicked(View view, Feeds resultsItem, int adapterPosition);

    void onFollowClicked(MediaListViewHolder mediaListViewHolder, Feeds resultsItem, int adapterPosition, String followStatus);
}
