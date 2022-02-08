package com.app.jussfun.helper;

import android.view.View;

import com.app.jussfun.model.Feeds;

public interface OnMenuClickListener {
    void onMenuClicked(View view, Feeds resultsItem, int adapterPosition);

    void onUserClicked(View view, Feeds resultsItem, int adapterPosition);
}
