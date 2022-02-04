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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.base.BaseViewHolder;
import com.app.jussfun.model.Feeds;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * @author eneim (7/1/17).
 */

public class FeedsAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private static String TAG = FeedsAdapter.class.getSimpleName();
    private final int VIEW_TYPE_STORY = 0;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_LOADING = 2;
    private LayoutInflater inflater;

    ArrayList<Feeds> parentList = new ArrayList<>();
    FragmentActivity activity;
    Context mContext;


    public FeedsAdapter(ArrayList<Feeds> parentList, Context context, FragmentActivity activity) {
        this.parentList = parentList;
        this.mContext = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null || inflater.getContext() != parent.getContext()) {
            inflater = LayoutInflater.from(parent.getContext());
        }

        final View view;
        final BaseViewHolder viewHolder;
        if (viewType == VIEW_TYPE_ITEM) {
            view = inflater.inflate(MediaListViewHolder.LAYOUT_RES, parent, false);
            viewHolder = new MediaListViewHolder(view);
            return viewHolder;
        } else if (viewType == VIEW_TYPE_LOADING) {
            view = inflater.inflate(LoadingViewHolder.LAYOUT_RES, parent, false);
            viewHolder = new LoadingViewHolder(view);
            return viewHolder;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int position) {
        if (viewHolder instanceof MediaListViewHolder) {
            Feeds resultsItem = parentList.get(position);
            final MediaListViewHolder holder = (MediaListViewHolder) viewHolder;
//            holder.setListener(activity);
            holder.bind(position, resultsItem, "");

            Glide.with(mContext)
                    .load(resultsItem.getUserImage())
                    .centerCrop()
                    .placeholder(R.drawable.avatar)
                    .into(holder.userImg);
            holder.btnMore.setVisibility(View.VISIBLE);

            holder.txtUserName.setText(resultsItem.getUserName());

            holder.btnLike.setSelected(resultsItem.getLike() == 1);
            holder.txtPostTime.setText("" + resultsItem.getFeedTime());

            holder.btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.preventMultipleClick(v);
                }
            });

            holder.btnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            holder.userImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            holder.txtUserName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            holder.btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

        } else if (viewHolder instanceof LoadingViewHolder) {
            viewHolder.bind(position, "", "");    /// parameter no need . just for loading
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (parentList.size() > 0 && parentList.get(position) == null)
            return VIEW_TYPE_LOADING;
        else if (position == 0) {
            return VIEW_TYPE_STORY;
        } else
            return VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return parentList.size();
        /*int itemCount = parentList == null ? 0 : (HomeListFragment.storyList.size() > 0 ? parentList.size() + 1 : parentList.size());
        Log.d(TAG, "getItemCount: " + itemCount);
        Log.d(TAG, "getItemCount: " + HomeListFragment.storyList.size());
        Log.d(TAG, "getItemCount: " + parentList.size());
        return itemCount;*/
    }


    public void addLoadingView() {
        new Handler().post(new Runnable() {
            public void run() {
                // There is no need to use notifyDataSetChanged()
                parentList.add(null);
                notifyItemInserted(parentList.size() - 1);
            }
        });
    }

    public void removeLoadingView() {
        //Remove loading item
        if (parentList.size() != 0 && parentList.get(parentList.size() - 1) == null) {
            // There is no need to use notifyDataSetChanged()
            parentList.remove(parentList.size() - 1);
            notifyItemRemoved(parentList.size());
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof MediaListViewHolder) {
            ((MediaListViewHolder) holder).onDetached();
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull BaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof MediaListViewHolder) {
            ((MediaListViewHolder) holder).onAttached();
        }
    }

    public static abstract class DoubleClickListener implements View.OnClickListener {

        private Timer timer = null;  //at class level;
        private int DELAY = 300;

        private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

        long lastClickTime = 0;

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

}