package com.app.jussfun.ui.feed.likes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.databinding.ItemLikedUsersBinding;
import com.app.jussfun.helper.callback.FollowUpdatedListener;
import com.app.jussfun.model.ProfileResponse;
import com.bumptech.glide.Glide;

import java.util.List;

public class LikedUsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<ProfileResponse> usersList;
    private FollowUpdatedListener listener;

    public LikedUsersAdapter(Context context, List<ProfileResponse> usersList, FollowUpdatedListener listener) {
        mContext = context;
        this.usersList = usersList;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_liked_users, parent, false);
        @NonNull ItemLikedUsersBinding binding = ItemLikedUsersBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof ItemViewHolder) {
            ProfileResponse user = usersList.get(viewHolder.getAdapterPosition());
            ItemViewHolder holder = (ItemViewHolder) viewHolder;
            holder.binding.txtUserName.setText(user.getUserName());
            Glide.with(mContext)
                    .load(user.getUserImage())
                    .placeholder(R.drawable.avatar)
                    .thumbnail(0.5f)
                    .into(holder.binding.ivUser);
            if (user.getInterestedByMe()) {
                holder.binding.btnFollow.setText(mContext.getString(R.string.unfollow));
                holder.binding.btnFollow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_solid_white));
                holder.binding.btnFollow.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryText));
            } else {
                holder.binding.btnFollow.setText(mContext.getString(R.string.follow));
                holder.binding.btnFollow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_corner_primary));
                holder.binding.btnFollow.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            }

            holder.binding.btnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.preventMultipleClick(v);
                    listener.onFollowUpdated(user, holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public void onFollowUpdated(ProfileResponse othersProfile, int holderPosition) {
        usersList.get(holderPosition).setInterestedByMe(othersProfile.getInterestedByMe());
        usersList.get(holderPosition).setFriend(othersProfile.getIsFriend());
        notifyItemChanged(holderPosition);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private ItemLikedUsersBinding binding;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public ItemViewHolder(ItemLikedUsersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
