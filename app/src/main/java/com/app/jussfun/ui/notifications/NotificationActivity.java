package com.app.jussfun.ui.notifications;

import android.content.Context;
import android.os.Bundle;

import com.app.jussfun.databinding.ActivityNotificationBinding;
import com.app.jussfun.ui.BaseFragmentActivity;
import com.app.jussfun.ui.feed.PlayerActivity;
import com.app.jussfun.utils.AppUtils;

public class NotificationActivity extends BaseFragmentActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();
    private Context mContext;
    private ActivityNotificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void initValues() {
        mContext = this;
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, binding.parentLay, isConnected);
    }
}