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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.base.App;
import com.app.jussfun.base.BaseViewHolder;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.OnMenuClickListener;
import com.app.jussfun.model.Feeds;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.SharedPref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * @author eneim (7/1/17).
 */

public class FeedsAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private static String TAG = FeedsAdapter.class.getSimpleName();
    private final int VIEW_TYPE_STORY = 0;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_LOADING = 2;
    private LayoutInflater inflater;
    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);

    ArrayList<Feeds> parentList = new ArrayList<>();
    OnMenuClickListener listener;
    Context mContext;


    public FeedsAdapter(ArrayList<Feeds> parentList, Context context, OnMenuClickListener clickListener) {
        this.parentList = parentList;
        this.mContext = context;
        this.listener = clickListener;
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
//            holder.setListener(activity)
            holder.bind(position, resultsItem, "");

            holder.userImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onUserClicked(holder.userImg, resultsItem, holder.getAdapterPosition());
                }
            });

            holder.txtUserName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onUserClicked(holder.txtUserName, resultsItem, holder.getAdapterPosition());
                }
            });

            holder.btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.preventMultipleClick(v);
                    if (resultsItem.getLike() != 1) {
                        updateFeed(Constants.TAG_LIKE, resultsItem, holder.getAdapterPosition());
                    }
                }
            });

            holder.btnSuperLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    App.preventMultipleClick(view);
                    if (resultsItem.getSuperLike() != 1) {
                        updateFeed(Constants.TAG_SUPER_LIKE, resultsItem, holder.getAdapterPosition());
                    }
                }
            });
            holder.btnStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    App.preventMultipleClick(view);
                    if (resultsItem.getStar() != 1) {
                        updateFeed(Constants.TAG_STAR, resultsItem, holder.getAdapterPosition());
                    }
                }
            });
            holder.btnHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    App.preventMultipleClick(view);
                    if (resultsItem.getHeart() != 1) {
                        updateFeed(Constants.TAG_HEART, resultsItem, holder.getAdapterPosition());
                    }
                }
            });

            holder.btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onShareClicked(holder.btnShare, resultsItem, holder.getAdapterPosition());
                }
            });

            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onShareClicked(holder.btnDelete, resultsItem, holder.getAdapterPosition());
                }
            });

            holder.commentLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, CommentsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    mContext.startActivity(intent);
                }
            });

        } else if (viewHolder instanceof LoadingViewHolder) {
            viewHolder.bind(position, "", "");    /// parameter no need . just for loading
        }
    }

    private void updateFeed(String type, Feeds resultsItem, int adapterPosition) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_FEED_ID, resultsItem.getFeedId());
            requestMap.put(Constants.TAG_TYPE, type);
            Call<Map<String, String>> call = apiInterface.updateFeed(requestMap);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful() && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        if (response.body().get("gems_remaining") != null) {
                            GetSet.setGems(Long.parseLong(response.body().get("gems_remaining")));
                            SharedPref.putLong(SharedPref.GEMS, GetSet.getGems());
                        }
                        switch (type) {
                            case Constants.TAG_LIKE: {
                                parentList.get(adapterPosition).setLike(1);
                                parentList.get(adapterPosition).setLikeCount(parentList.get(adapterPosition).getLikeCount() + 1);
                            }
                            break;
                            case Constants.TAG_SUPER_LIKE: {
                                parentList.get(adapterPosition).setSuperLike(1);
                                parentList.get(adapterPosition).setSuperLikeCount(parentList.get(adapterPosition).getSuperLikeCount() + 1);
                            }
                            break;
                            case Constants.TAG_STAR: {
                                parentList.get(adapterPosition).setStar(1);
                                parentList.get(adapterPosition).setStarCount(parentList.get(adapterPosition).getStarCount() + 1);
                            }
                            break;
                            case Constants.TAG_HEART: {
                                parentList.get(adapterPosition).setHeart(1);
                                parentList.get(adapterPosition).setHeartCount(parentList.get(adapterPosition).getHeartCount() + 1);
                            }
                            break;
                        }
                        notifyItemChanged(adapterPosition);
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {

                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (parentList.size() > 0 && parentList.get(position) == null)
            return VIEW_TYPE_LOADING;
        else
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
