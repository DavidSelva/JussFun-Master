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
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.app.jussfun.R;
import com.app.jussfun.base.BaseViewHolder;
import com.app.jussfun.external.toro.core.PlayerSelector;
import com.app.jussfun.external.toro.core.ToroPlayer;
import com.app.jussfun.external.toro.core.ToroUtil;
import com.app.jussfun.external.toro.core.media.PlaybackInfo;
import com.app.jussfun.external.toro.core.widget.Container;
import com.app.jussfun.model.Feeds;
import com.app.jussfun.ui.FullScreenImageViewActivity;
import com.app.jussfun.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    private final SnapHelper snapHelper = new PagerSnapHelper();
    public Context context;
    public ImageView userImg, btnLike, btnHeart, btnStar, btnShare, btnSuperLike;
    public FrameLayout btnMore;
    public TextView txtUserName, txtPostTime;
    public List<Feeds> feedsList = new ArrayList<>();
    public clickListener listener;
    @SuppressWarnings("WeakerAccess")
    Container container;
    View itemVw;
    private int initPosition = -1;


    public MediaListViewHolder(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        container = (Container) itemView.findViewById(R.id.container);
        userImg = itemView.findViewById(R.id.userImage);

        btnLike = itemView.findViewById(R.id.btnLike);
        btnHeart = itemView.findViewById(R.id.btnHeart);
        btnStar = itemView.findViewById(R.id.btnStar);
        btnSuperLike = itemView.findViewById(R.id.btnSuperLike);
        btnMore = itemView.findViewById(R.id.btnMore);
        txtUserName = (TextView) itemView.findViewById(R.id.txtUserName);
        txtPostTime = (TextView) itemView.findViewById(R.id.txtPostTime);
        itemVw = itemView;

    }

    public void setListener(clickListener listener) {
        this.listener = listener;
    }

    // Called by Adapter
    public void bind(int position, Object item, String way) {
        this.way = way;
        Feeds resultsItem = (Feeds) item;
        feedsList = new ArrayList<Feeds>();
        feedsList.add(resultsItem);
        Adapter adapter = new Adapter(feedsList, listener, itemVw);
        container.setAdapter(adapter);
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
        private View itemVw;

        Adapter(List<Feeds> childlist, clickListener listener, View itemVw) {
            this.childAdapterlist = childlist;
            this.listener = listener;
            this.itemVw = itemVw;
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

                holder.bind(position, childAdapterlist, "");

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
                imageViewHolder.bind(position, childAdapterlist, "");
                imageViewHolder.setListener(Adapter.this);
                imageViewHolder.imageView.setOnClickListener(new DoubleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        Intent intent = new Intent(context, FullScreenImageViewActivity.class);
                        intent.putExtra(Constants.TAG_IMAGE, childAdapterlist.get(position).getImageUrl());
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
