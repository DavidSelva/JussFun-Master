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

import android.view.View;

import com.app.jussfun.R;
import com.app.jussfun.base.BaseViewHolder;
import com.app.jussfun.external.ProgressWheel;


/**
 * @author eneim (7/1/17).
 */

public class LoadingViewHolder extends BaseViewHolder {

    public static final int LAYOUT_RES = R.layout.layout_loading_item;

    ProgressWheel progressBar;

    public LoadingViewHolder(View itemView) {
        super(itemView);
        progressBar = (ProgressWheel) itemView.findViewById(R.id.pBar);
    }

    @Override
    public void bind(int position, Object object, String way) {

    }
}
