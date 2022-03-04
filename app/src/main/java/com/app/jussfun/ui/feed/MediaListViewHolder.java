/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.app.jussfun.ui.feed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.app.jussfun.R;
import com.app.jussfun.base.BaseViewHolder;
import com.app.jussfun.external.CustomLinkReadMoreTextView;
import com.app.jussfun.external.toro.core.PlayerSelector;
import com.app.jussfun.external.toro.core.ToroPlayer;
import com.app.jussfun.external.toro.core.ToroUtil;
import com.app.jussfun.external.toro.core.media.PlaybackInfo;
import com.app.jussfun.external.toro.core.widget.Container;
import com.app.jussfun.model.Feeds;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.ui.FullScreenImageViewActivity;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This {@link RecyclerView.ViewHolder} will contain a {@link Container}.
 *
 * @author eneim (7/1/17).
 */

@SuppressWarnings("unused")
public //
class MediaListViewHolder extends BaseViewHolder implements ToroPlayer {

    public static final int LAYOUT_RES = R.layout.home_post_container;
    private static final String TAG = MediaListViewHolder.class.getSimpleName();
    private String way = "";
    public Context mContext;
    private final SnapHelper snapHelper = new PagerSnapHelper();

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.container)
    Container container;
    @BindView(R.id.userImage)
    public ImageView userImg;
    @BindView(R.id.txtUserName)
    public TextView txtUserName;
    @BindView(R.id.btnLike)
    ImageView btnLike;
    @BindView(R.id.btnHeart)
    ImageView btnHeart;
    @BindView(R.id.btnStar)
    ImageView btnStar;
    @BindView(R.id.btnSuperLike)
    ImageView btnSuperLike;
    @BindView(R.id.txtLikeCount)
    public TextView txtLikeCount;
    @BindView(R.id.txtSuperLikeCount)
    public TextView txtSuperLikeCount;
    @BindView(R.id.txtHeartCount)
    public TextView txtHeartCount;
    @BindView(R.id.txtStarCount)
    public TextView txtStarCount;
    @BindView(R.id.txtViewUsers)
    public TextView txtViewUsers;
    @BindView(R.id.btnShare)
    public LinearLayout btnShare;
    @BindView(R.id.btnMore)
    public LinearLayout btnMore;
    @BindView(R.id.txtPostTime)
    TextView txtPostTime;
    @BindView(R.id.descriptionLay)
    public LinearLayout descriptionLay;
    @BindView(R.id.txtDescription)
    public CustomLinkReadMoreTextView txtDescription;
    @BindView(R.id.readMoreTextView)
    public CustomLinkReadMoreTextView readMoreTextView;
    @BindView(R.id.commentLay)
    public LinearLayout commentLay;
    @BindView(R.id.btnComment)
    public ImageView btnComment;
    @BindView(R.id.txtCommentCount)
    public TextView txtCommentCount;
    @BindView(R.id.txtComment)
    public TextView txtComment;
    @BindView(R.id.followLay)
    public LinearLayout followLay;
    @BindView(R.id.txtFollow)
    public TextView txtFollow;

    public List<Feeds> feedsList = new ArrayList<>();
    public clickListener listener;
    private int initPosition = -1;


    public MediaListViewHolder(View itemView) {
        super(itemView);
        this.mContext = itemView.getContext();
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(int position, Object item, String way) {
        this.way = way;
        Feeds resultsItem = (Feeds) item;
        Glide.with(mContext)
                .load(Constants.IMAGE_URL + resultsItem.getUserImage())
                .centerCrop()
                .placeholder(R.drawable.avatar)
                .into(userImg);

        txtUserName.setText(resultsItem.getUserName());
        txtPostTime.setText("" + resultsItem.getFeedTime());
        if (resultsItem.getUserId().equals(GetSet.getUserId())) {
            followLay.setVisibility(View.GONE);
        } else {
            followLay.setVisibility(View.VISIBLE);
        }
        if (resultsItem.isFriend() || resultsItem.isInterestedByMe()) {
            txtFollow.setText(mContext.getString(R.string.unfollow));
        } else {
            txtFollow.setText(mContext.getString(R.string.follow));
        }

        txtDescription.setIsLinkable(true);
        if (TextUtils.isEmpty(resultsItem.getDescription().trim())) {
            descriptionLay.setVisibility(View.GONE);
        } else {
            descriptionLay.setVisibility(View.VISIBLE);
            txtDescription.setText(resultsItem.getDescription().trim());
        }
        txtDescription.post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (resultsItem != null) {
                            if (resultsItem.getDescription() != null) {
                                if (txtDescription != null && (resultsItem.getDescription()).length() > 100) {
                                    addReadMore(resultsItem.getDescription(), txtDescription);
                                }
                            }
                        }
                    }
                });
        MovementMethod m = txtDescription.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (txtDescription.getLinksClickable()) {
                txtDescription.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
            /*MovementMethod movementMethod = holder.txtComments.getMovementMethod();
            if ((movementMethod == null) || !(movementMethod instanceof LinkMovementMethod)) {
                if (holder.txtComments.getLinksClickable()) {
                    holder.txtComments.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }*/

        btnLike.setSelected(resultsItem.getLike() == 1);
        btnSuperLike.setSelected(resultsItem.getSuperLike() == 1);
        btnStar.setSelected(resultsItem.getStar() == 1);
        btnHeart.setSelected(resultsItem.getHeart() == 1);

        txtLikeCount.setText(!TextUtils.isEmpty("" + resultsItem.getLikeCount()) ? "" + resultsItem.getLikeCount() : "" + 0);
        txtSuperLikeCount.setText(!TextUtils.isEmpty("" + resultsItem.getSuperLikeCount()) ? "" + resultsItem.getSuperLikeCount() : "" + 0);
        txtHeartCount.setText(!TextUtils.isEmpty("" + resultsItem.getHeartCount()) ? "" + resultsItem.getHeartCount() : "" + 0);
        txtStarCount.setText(!TextUtils.isEmpty("" + resultsItem.getStarCount()) ? "" + resultsItem.getStarCount() : "" + 0);
        if (resultsItem.getCommentStatus() == 0) {
            txtCommentCount.setText("");
            commentLay.setVisibility(View.GONE);
        } else {
            txtCommentCount.setText("" + resultsItem.getCommentsCount());
            commentLay.setVisibility(View.VISIBLE);
        }

        initAdapter(resultsItem);

    }

    public void setListener(clickListener listener) {
        this.listener = listener;
    }

    private void initAdapter(Feeds resultsItem) {
        feedsList = new ArrayList<Feeds>();
        feedsList.add(resultsItem);
        Adapter adapter = new Adapter(feedsList, listener);
        container.setAdapter(adapter);
    }

    private void addReadMore(final String text, final AppCompatTextView textView) {
        SpannableString ss = new SpannableString(text.substring(0, 100) + " " + mContext.getString(R.string.read_more));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                addReadLess(text, textView);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ds.setColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                } else {
                    ds.setColor(mContext.getResources().getColor(R.color.colorAccent));
                }
            }
        };
        ss.setSpan(clickableSpan, ss.length() - 10, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void addReadLess(final String text, final AppCompatTextView textView) {
        SpannableString ss = new SpannableString(text + " " + mContext.getString(R.string.read_less));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                addReadMore(text, textView);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ds.setColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                } else {
                    ds.setColor(mContext.getResources().getColor(R.color.colorAccent));
                }
            }
        };
        ss.setSpan(clickableSpan, ss.length() - 10, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void onDetached() {
        snapHelper.attachToRecyclerView(null);
    }

    public void onAttached() {
        snapHelper.attachToRecyclerView(container);
    }

    // ToroPlayer implementation
    @NonNull
    @Override
    public View getPlayerView() {
        return container;
    }

    @NonNull
    @Override
    public PlaybackInfo getCurrentPlaybackInfo() {
        SparseArray<PlaybackInfo> actualInfos = container.getLatestPlaybackInfos();
        ExtraPlaybackInfo resultInfo = new ExtraPlaybackInfo(actualInfos);

        List<ToroPlayer> activePlayers = container.filterBy(Container.Filter.PLAYING);
        if (activePlayers.size() >= 1) {
            resultInfo.setResumeWindow(activePlayers.get(0).getPlayerOrder());
        }

        return resultInfo;
    }

    @Override
    public void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo) {
        this.initPosition = -1;
        if (playbackInfo instanceof ExtraPlaybackInfo) {
            //noinspection unchecked
            SparseArray<PlaybackInfo> cache = ((ExtraPlaybackInfo) playbackInfo).actualInfo;
            if (cache != null && cache.size() > 0) {
                for (int i = 0; i < cache.size(); i++) {
                    int key = cache.keyAt(i);
                    PlaybackInfo info = cache.get(key);
                    if (info != null) this.container.savePlaybackInfo(key, info);
                }
            }
            this.initPosition = playbackInfo.getResumeWindow();
        }
        this.container.setPlayerSelector(PlayerSelector.NONE);
    }

    @Override
    public void play() {
        if (initPosition >= 0) this.container.scrollToPosition(initPosition);
        initPosition = -1;
        this.container.setPlayerSelector(PlayerSelector.DEFAULT);
    }

    @Override
    public void pause() {
        this.container.setPlayerSelector(PlayerSelector.NONE);
    }

    @Override
    public boolean isPlaying() {
        return this.container.filterBy(Container.Filter.PLAYING).size() > 0;
    }

    @Override
    public void release() {
        // release here
        List<ToroPlayer> managed = this.container.filterBy(Container.Filter.MANAGING);
        for (ToroPlayer player : managed) {
            if (player.isPlaying()) {
                PlaybackInfo info = player.getCurrentPlaybackInfo();
                //noinspection ConstantConditions
                if (info != null) this.container.savePlaybackInfo(player.getPlayerOrder(), info);
                player.pause();
            }
            player.release();
        }
        this.container.setPlayerSelector(PlayerSelector.NONE);
    }

    @Override
    public boolean wantsToPlay() {
        return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.55;
    }

    @Override
    public int getPlayerOrder() {
        return getAdapterPosition();
    }

    public interface clickListener {
        public void click(String frag, String userid, String wayType);
    }

    //// Recycler paging adapter
    static class Adapter extends RecyclerView.Adapter<BaseViewHolder> implements ImageViewHolder.clickListener {

        private final int VIEW_TYPE_IMAGE_ITEM = 0;
        private final int VIEW_TYPE_VIDEO_ITEM = 1;
        MotionEvent simulationEvent;
        // for gesture detector
        PlayerViewHolder tempViewHolder;
        private LayoutInflater inflater;
        private List<Feeds> childAdapterlist;
        private Context context;
        private clickListener listener;

        Adapter(List<Feeds> childlist, clickListener listener) {
            this.childAdapterlist = childlist;
            this.listener = listener;
        }

        @NonNull
        @Override
        public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (inflater == null || inflater.getContext() != parent.getContext()) {
                inflater = LayoutInflater.from(parent.getContext());
            }

            context = parent.getContext();

            final View view;
            final BaseViewHolder viewHolder;
            if (viewType == VIEW_TYPE_VIDEO_ITEM) {
                view = inflater.inflate(PlayerViewHolder.LAYOUT_RES, parent, false);
                viewHolder = new PlayerViewHolder(view);
            } else {
                view = inflater.inflate(ImageViewHolder.LAYOUT_RES, parent, false);
                viewHolder = new ImageViewHolder(view);
            }

            Log.d(TAG, "onCreateViewHolder: " + viewType);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final BaseViewHolder holder, final int position) {

            if (holder instanceof PlayerViewHolder) {

                final PlayerViewHolder simplePlayerViewHolder = (PlayerViewHolder) holder;

                ArrayList<BaseViewHolder> holders = new ArrayList<>();
                holders.add(simplePlayerViewHolder);

                holder.bind(holder.getAdapterPosition(), childAdapterlist, "");

//                simplePlayerViewHolder.durationTxt.setText(childAdapterlist.get(position).getDuration());

                simplePlayerViewHolder.img_vol.setSelected(Constants.isMute);

                simplePlayerViewHolder.soundLay.setOnClickListener(new DoubleClickListener() {

                    @Override
                    public void onSingleClick(View v) {
                    }

                    @Override
                    public void onDoubleClick(View v) {

                    }
                });

            } else {
                final ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
                imageViewHolder.bind(holder.getAdapterPosition(), childAdapterlist, "");
                imageViewHolder.setListener(Adapter.this);
                imageViewHolder.imageView.setOnClickListener(new DoubleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        Intent intent = new Intent(context, FullScreenImageViewActivity.class);
                        intent.putExtra(Constants.TAG_IMAGE, childAdapterlist.get(holder.getAdapterPosition()).getImageUrl());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onDoubleClick(View v) {

                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            return childAdapterlist.get(position).getImageUrl().contains(".mp4") ? VIEW_TYPE_VIDEO_ITEM : VIEW_TYPE_IMAGE_ITEM;

        }

        public void setHolder(PlayerViewHolder tempViewHolder) {
            this.tempViewHolder = tempViewHolder;
        }

        @Override
        public int getItemCount() {
            return childAdapterlist.size();
        }


        @Override
        public void click(String frag, String userid, String wayType) {
            listener.click(frag, userid, wayType);
        }
    }

    public static abstract class DoubleClickListener implements View.OnClickListener {

        private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds
        long lastClickTime = 0;
        private Timer timer = null;  //at class level;
        private int DELAY = 300;

        @Override
        public void onClick(View v) {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                processDoubleClickEvent(v);
            } else {
                processSingleClickEvent(v);
            }
            lastClickTime = clickTime;
        }


        public void processSingleClickEvent(final View v) {

            final Handler handler = new Handler();
            final Runnable mRunnable = new Runnable() {
                public void run() {
                    onSingleClick(v); //Do what ever u want on single click

                }
            };

            TimerTask timertask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(mRunnable);
                }
            };
            timer = new Timer();
            timer.schedule(timertask, DELAY);

        }

        public void processDoubleClickEvent(View v) {
            if (timer != null) {
                timer.cancel(); //Cancels Running Tasks or Waiting Tasks.
                timer.purge();  //Frees Memory by erasing cancelled Tasks.
            }
            onDoubleClick(v);//Do what ever u want on Double Click
        }

        public abstract void onSingleClick(View v);

        public abstract void onDoubleClick(View v);
    }

    class LinePagerIndicatorDecoration extends RecyclerView.ItemDecoration {

        Context context;


        private int dotscount;
        private ImageView[] dots;


        public LinePagerIndicatorDecoration(Context context) {
            this.context = context;

        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);

            // find active page (which should be highlighted)
            LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
            int activePosition = layoutManager.findFirstVisibleItemPosition();
            if (activePosition == RecyclerView.NO_POSITION) {
                return;
            }
            activePosition++;
            parent.getAdapter().getItemCount();
        }

    }

}
