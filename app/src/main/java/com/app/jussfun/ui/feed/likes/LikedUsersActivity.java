package com.app.jussfun.ui.feed.likes;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.app.jussfun.R;
import com.app.jussfun.databinding.ActivityLikedUsersBinding;
import com.app.jussfun.utils.Constants;
import com.google.android.material.tabs.TabLayoutMediator;

public class LikedUsersActivity extends AppCompatActivity {

    private static final String TAG = LikedUsersActivity.class.getSimpleName();
    private Context mContext;
    ActivityLikedUsersBinding binding;
    private String feedId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLikedUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initValues();
        initView();
    }

    private void initValues() {
        mContext = this;
        feedId = getIntent().getStringExtra(Constants.TAG_FEED_ID);
    }

    private void initView() {
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initViewPager();
    }

    private void initViewPager() {
        LikedViewPagerAdapter viewPagerAdapter = new
                LikedViewPagerAdapter(this, feedId);
        binding.viewPager.setOffscreenPageLimit(1);
        binding.viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(getString(R.string.likes));
                    } else if (position == 1) {
                        tab.setText(getString(R.string.super_like));
                    } else if (position == 2) {
                        tab.setText(getString(R.string.heart));
                    } else if (position == 3) {
                        tab.setText(getString(R.string.star));
                    }
                }
        ).attach();
    }
}