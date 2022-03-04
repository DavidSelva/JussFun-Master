package com.app.jussfun.ui.feed.likes;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.app.jussfun.utils.Constants;

public class LikedViewPagerAdapter extends FragmentStateAdapter {
    private static final int TAB_SIZE = 4;
    private String feedId = null;

    public LikedViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String feedId) {
        super(fragmentActivity);
        this.feedId = feedId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String type;
        if (position == 0) {
            type = Constants.TAG_LIKE;
        } else if (position == 1) {
            type = Constants.TAG_SUPER_LIKE;
        } else if (position == 2) {
            type = Constants.TAG_HEART;
        } else {
            type = Constants.TAG_STAR;
        }
        return LikedUsersFragment.newInstance(feedId, type);
    }

    @Override
    public int getItemCount() {
        return TAB_SIZE;
    }
}
