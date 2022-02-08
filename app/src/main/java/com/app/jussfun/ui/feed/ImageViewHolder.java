/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.jussfun.R;
import com.app.jussfun.base.BaseViewHolder;
import com.app.jussfun.external.ProgressWheel;
import com.app.jussfun.model.Feeds;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;


/**
 * @author eneim (7/1/17).
 */

public class ImageViewHolder extends BaseViewHolder {

    private static final String TAG = ImageViewHolder.class.getSimpleName();
    public static final int LAYOUT_RES = R.layout.home_imageholder;

    public ImageView imageView;
    private TextView removalTextView;
    ProgressWheel pgsBar;
    Context context;
    public RelativeLayout childLay;
    //    LottieAnimationView likeAnimation;
    ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
    int height, width;  //for tag image size

    public ImageViewHolder(View itemView) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
        childLay = (RelativeLayout) itemView.findViewById(R.id.childLay);
        pgsBar = (ProgressWheel) itemView.findViewById(R.id.pBar);
        context = itemView.getContext();
    }

    public interface clickListener {
        public void click(String frag, String userid, String wayType);
    }

    public clickListener listener;

    public void setListener(clickListener listener) {
        this.listener = listener;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void bind(int position, final Object object, String postId) {
        List<Feeds> result = (List<Feeds>) object;
        Glide.with(context)
                .load(result.get(position).getImageUrl())
                .placeholder(R.drawable.place_holder_loading)
                .transition(DrawableTransitionOptions.withCrossFade(1000))
                .into(imageView);

    }

}
