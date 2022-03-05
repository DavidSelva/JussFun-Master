package com.app.jussfun.ui.feed.likes;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
                    View tabItemLayout = getLayoutInflater().inflate(R.layout.tab_liked_users, null);
                    ImageView ivLike = tabItemLayout.findViewById(R.id.ivLike);
                    TextView txtLike = tabItemLayout.findViewById(R.id.txtLike);
                    txtLike.setMaxLines(2);
                    if (position == 0) {
                        ivLike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.like_act));
                        txtLike.setText(getString(R.string.likes));
                        tab.setCustomView(tabItemLayout);
                    } else if (position == 1) {
                        ivLike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.super_like_act));
                        txtLike.setText(mContext.getString(R.string._super) + "\n" + getString(R.string.like));
                        tab.setCustomView(tabItemLayout);
                    } else if (position == 2) {
                        ivLike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.heart_act));
                        txtLike.setText(getString(R.string.heart));
                        tab.setCustomView(tabItemLayout);
                    } else if (position == 3) {
                        ivLike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.star_act));
                        txtLike.setText(getString(R.string.star));
                        tab.setCustomView(tabItemLayout);
                    }
                }
        ).attach();
    }
}