/*
package com.app.jussfun.ui.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.base.BaseViewHolder;
import com.app.jussfun.external.LinkEllipseTextView;
import com.app.jussfun.model.NotificationResponse;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class NotificationLikeHolder extends BaseViewHolder {

    public TextView time;

    LinkEllipseTextView description;
    ImageView userImg, post_image;

    RecyclerView recyclerView;
    LikePostListAdapter adapter;

    Context mContext;
    int positionClick = 0;

    public NotificationLikeHolder(View itemView) {
        super(itemView);
        this.mContext = itemView.getContext();
        time = (TextView) itemView.findViewById(R.id.time);
        description = itemView.findViewById(R.id.description);
        userImg = (ImageView) itemView.findViewById(R.id.userImage);
        post_image = (ImageView) itemView.findViewById(R.id.post_image);
        recyclerView = (RecyclerView) itemView.findViewById(R.id.card_recycler_view);

    }

    public interface clickListener {
        public void click(int pos);
    }

    public clickListener listener;

    public void setListener(clickListener listener) {
        this.listener = listener;
    }

    @Override
    public void bind(int position, Object object, String way) {
        positionClick = position;
        ArrayList<NotificationResponse.ResultItem> notificationList = (ArrayList<NotificationResponse.ResultItem>) object;
        if (notificationList.get(position).getSeen().equals("" + false)) {
            description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
            time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
        } else {
            description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
            time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
        }

        description.setIsLinkable(true);
        if (notificationList.get(position).getNotificationType().equalsIgnoreCase("comment")) {
            description.setText(Utils.stripHtml("@" + notificationList.get(position).getLikedUserName() + " " +
                    mContext.getString(R.string.likes_your_comment)) */
/*+ " " +
                    Utils.stripHtml(notificationList.get(position).getLogTime())*//*
);
        }else if (notificationList.get(position).getNotificationType().equalsIgnoreCase("post")){
            description.setText(Utils.stripHtml("@" + notificationList.get(position).getLikedUserName()) + " " +
                    mContext.getString(R.string.liked_your_message) */
/*+ " " +
        }
        else {
            description.setText(Utils.stripHtml("@" + notificationList.get(position).getLikedUserName()) + " " +
                    mContext.getString(R.string.liked_your_message) /*+ " " +
                    Utils.stripHtml(notificationList.get(position).getLogTime())*//*
);
        }
        time.setVisibility(View.VISIBLE);
        time.setText(notificationList.get(position).getLogTime());

        Glide.with(mContext)
                .load(notificationList.get(position).getProfilePicture())
                .centerCrop().placeholder(R.drawable.user_default)
                .into(userImg);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext.getApplicationContext(), 6);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        List<HomeResponse.PostMediaItem> postList = notificationList.get(position).getPostMedia();
        post_image.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        if (postList != null) {
            if (postList.size() == 1) {
                Glide.with(mContext)
                        .load(postList.get(0).getImageUrl())
                        .centerCrop().placeholder(R.drawable.place_holder_loading)
                        .into(post_image);
            } else if (postList.size() == 0) {
                Glide.with(mContext)
                        .load(R.drawable.place_holder_loading)
                        .centerCrop().placeholder(R.drawable.place_holder_loading)
                        .into(post_image);
            } else {
                Glide.with(mContext)
                        .load(postList.get(0).getImageUrl())
                        .centerCrop().placeholder(R.drawable.place_holder_loading)
                        .into(post_image);
                //            recyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            Glide.with(mContext)
                    .load(R.drawable.place_holder_loading)
                    .centerCrop()
                    .into(post_image);
        }

        */
/*adapter = new LikePostListAdapter(context.getApplicationContext(), list.get(position), listener);
        recyclerView.setAdapter(adapter);*//*


    }

    class LikePostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        Context context;
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        private NotificationResponse.ResultItem homeParentpojo;
        private clickListener listener;

        public LikePostListAdapter(Context context, NotificationResponse.ResultItem homeParentpojo, clickListener listener) {
            this.context = context;
            this.homeParentpojo = homeParentpojo;
            this.listener = listener;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            final View view;
            final RecyclerView.ViewHolder rcv;

            if (i == VIEW_TYPE_ITEM) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_likeitem, viewGroup, false);
                rcv = new MediaViewHolder(view);
                return rcv;
            } else if (i == VIEW_TYPE_LOADING) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_loading_item, viewGroup, false);
                rcv = new LoadingViewHolder(view);
                return rcv;
            }

            return null;
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            if (holder instanceof MediaViewHolder) {
                MediaViewHolder viewHolder = (MediaViewHolder) holder;
                Glide.with(context)
                        .load(homeParentpojo.getPostMedia().get(position).getImageUrl())
                        .error(R.drawable.place_holder_loading)
                        .centerCrop()
                        .thumbnail(0.5f)
                        .into(viewHolder.img_android);

                viewHolder.countTxt.setVisibility(View.GONE);

             */
/*   if (homelist.get(i).getList().get(0).getPath().contains("video"))
                    viewHolder.playButton.setVisibility(View.VISIBLE);
                else*//*

                viewHolder.playButton.setVisibility(View.GONE);


                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Utils.getInstance().GlobalUpdate("postdetail", "", "");   // just userid act as position for Post details pag
                        listener.click(positionClick);
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            return homeParentpojo.getPostMedia().get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            return homeParentpojo.getPostMedia() == null ? 0 : homeParentpojo.getPostMedia().size();
        }

        public class MediaViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout parentLay;
            private ImageView img_android, playButton;
            TextView countTxt;

            public MediaViewHolder(View view) {
                super(view);

                img_android = (ImageView) view.findViewById(R.id.img_android);
                playButton = (ImageView) view.findViewById(R.id.playButton);
                countTxt = (TextView) view.findViewById(R.id.countTxt);
                parentLay = (RelativeLayout) view.findViewById(R.id.childLay);
            }
        }


        public class LoadingViewHolder extends RecyclerView.ViewHolder {

            static final int LAYOUT_RES = R.layout.layout_loading_item;

            ProgressWheel progressBar;

            public LoadingViewHolder(View itemView) {
                super(itemView);
                progressBar = (ProgressWheel) itemView.findViewById(R.id.pBar);
            }


        }


    }

}


*/
