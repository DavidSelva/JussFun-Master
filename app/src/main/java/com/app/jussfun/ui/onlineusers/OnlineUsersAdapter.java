package com.app.jussfun.ui.onlineusers;

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
import com.app.jussfun.databinding.ItemLoadingBinding;
import com.app.jussfun.helper.callback.FollowUpdatedListener;
import com.app.jussfun.model.OnlineUsers;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;

import java.util.List;

public class OnlineUsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
            OnlineUsers.AccountModel user = itemList.get(viewHolder.getAdapterPosition());
            ItemViewHolder holder = (ItemViewHolder) viewHolder;
            holder.binding.txtUserName.setText(user.getAcctName());
            Glide.with(mContext)
                    .load(user.getAcctPhoto())
                    .placeholder(R.drawable.avatar)
                    .thumbnail(0.5f)
                    .into(holder.binding.ivUser);
            if (user.isAcctInterestAlert()) {
                holder.binding.btnFollow.setText(mContext.getString(R.string.unfollow));
                holder.binding.btnFollow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_solid_white));
                holder.binding.btnFollow.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryText));
            } else {
                holder.binding.btnFollow.setText(mContext.getString(R.string.follow));
                holder.binding.btnFollow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_corner_primary));
                holder.binding.btnFollow.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            }

            holder.binding.btnFollow.setVisibility(View.GONE);
            if (type.equals(Constants.ONLINE)) {
                holder.binding.iconOnline.setVisibility(View.VISIBLE);
            } else {
                holder.binding.iconOnline.setVisibility(View.GONE);
            }

            holder.binding.btnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.preventMultipleClick(v);
                    listener.onFollowUpdated(user, holder.getAdapterPosition());
                }
            });

            holder.binding.imageLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.preventMultipleClick(v);
                    holder.binding.txtUserName.performClick();
                }
            });
            holder.binding.txtUserName.setOnClickListener(new View.OnClickListener() {
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

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        private ItemLoadingBinding binding;

        public FooterViewHolder(View parent) {
            super(parent);
        }

        public FooterViewHolder(ItemLoadingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
