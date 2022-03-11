package com.app.jussfun.ui.onlineusers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.databinding.ItemLikedUsersBinding;
import com.app.jussfun.databinding.ItemLoadingBinding;
import com.app.jussfun.helper.callback.FollowUpdatedListener;
import com.app.jussfun.model.OnlineUsers;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class OnlineUsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = OnlineUsersAdapter.class.getSimpleName();
    private Context mContext;
    private List<OnlineUsers.AccountModel> itemList;
    private FollowUpdatedListener listener;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_FOOTER = 1;
    private boolean showLoading = false;
    private String type;

    public OnlineUsersAdapter(Context context, List<OnlineUsers.AccountModel> usersList, FollowUpdatedListener listener, String type) {
        mContext = context;
        this.itemList = usersList;
        this.listener = listener;
        this.type = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            @NonNull ItemLikedUsersBinding binding = ItemLikedUsersBinding.inflate(LayoutInflater.from(mContext), parent, false);
            return new ItemViewHolder(binding);
        } else {
            @NonNull ItemLoadingBinding binding = ItemLoadingBinding.inflate(LayoutInflater.from(mContext), parent, false);
            return new FooterViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof ItemViewHolder) {
            ItemViewHolder holder = (ItemViewHolder) viewHolder;
            OnlineUsers.AccountModel user = itemList.get(holder.getAdapterPosition());
            holder.viewBinding.txtUserName.setText(user.getAcctName());
            Glide.with(mContext)
                    .load(Constants.IMAGE_URL + user.getAcctPhoto())
                    .override(Target.SIZE_ORIGINAL / 2, Target.SIZE_ORIGINAL / 2)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.viewBinding.ivUser.setImageDrawable(resource);
                            return false;
                        }
                    })
                    .placeholder(R.drawable.avatar)
                    .into(holder.viewBinding.ivUser);
            if (user.isAcctInterestAlert()) {
                holder.viewBinding.btnFollow.setText(mContext.getString(R.string.unfollow));
                holder.viewBinding.btnFollow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_solid_white));
                holder.viewBinding.btnFollow.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryText));
            } else {
                holder.viewBinding.btnFollow.setText(mContext.getString(R.string.follow));
                holder.viewBinding.btnFollow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_corner_primary));
                holder.viewBinding.btnFollow.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            }
            holder.viewBinding.btnFollow.setVisibility(View.GONE);

            if (type.equals(Constants.TAG_ONLINE)) {
                holder.viewBinding.iconOnline.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.online));
            } else {
                holder.viewBinding.iconOnline.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.offline));
            }

            holder.viewBinding.btnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.preventMultipleClick(v);
                    listener.onFollowUpdated(user, holder.getAdapterPosition());
                }
            });

            holder.viewBinding.itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: ");
                    App.preventMultipleClick(v);
                    listener.onProfileClicked(user.getId());
                }
            });
            holder.viewBinding.txtUserName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.preventMultipleClick(v);
                    listener.onProfileClicked(user.getId());
                }
            });
        } else if (viewHolder instanceof FooterViewHolder) {
            FooterViewHolder footerHolder = (FooterViewHolder) viewHolder;
            footerHolder.binding.progressBar.setIndeterminate(true);
            footerHolder.binding.progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoading && isPositionFooter(position))
            return VIEW_TYPE_FOOTER;
        return VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        int itemCount = itemList.size();
        if (showLoading)
            itemCount++;
        return itemCount;
    }

    public boolean isPositionFooter(int position) {
        return position == getItemCount() - 1 && showLoading;
    }

    public void showLoading(boolean value) {
        showLoading = value;
    }

    public void onFollowUpdated(OnlineUsers.AccountModel othersProfile, int holderPosition) {
        itemList.get(holderPosition).setAcctInterestAlert(othersProfile.isAcctInterestAlert());
//        usersList.get(holderPosition).setAccFr(othersProfile.getIsFriend());
        notifyItemChanged(holderPosition);
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ItemLikedUsersBinding viewBinding;

        public ItemViewHolder(ItemLikedUsersBinding binding) {
            super(binding.getRoot());
            this.viewBinding = binding;
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        private final ItemLoadingBinding binding;

        public FooterViewHolder(ItemLoadingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
