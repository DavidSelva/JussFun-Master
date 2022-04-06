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
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.databinding.FragmentFeedsMainBinding;
import com.app.jussfun.databinding.ItemMainMenuBinding;
import com.app.jussfun.external.toro.core.widget.Container;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A list of content that contains a {@link Container} as one of its child. We gonna use a
 * {@link PagerSnapHelper} to mimic a Pager-inside-RecyclerView. Other contents will be
 * normal text to preserve the performance and also to not make user confused.
 *
 * @author eneim (7/1/17).
 */


@SuppressWarnings("unused")
public class FeedsFragment extends Fragment {

    private static final String TAG = FeedsFragment.class.getSimpleName();
    private Context mContext;

    private FragmentFeedsMainBinding binding;
    private MenuAdapter adapter;
    private List<String> menuList = new ArrayList<>();
    private int displayWidth, displayHeight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle bundle) {
        // Inflate the layout for this fragment
        binding = FragmentFeedsMainBinding.inflate(inflater, container, false);
        displayWidth = AppUtils.getDisplayWidth(getActivity());
        displayHeight = AppUtils.getDisplayHeight(getActivity());
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View itemView, @Nullable Bundle bundle) {
        super.onViewCreated(itemView, bundle);
        if (mContext == null) mContext = getActivity();
        initMenu();
    }

    private void initMenu() {
        String[] menuArray = getResources().getStringArray(R.array.menu_array);
        menuList = Arrays.asList(menuArray);
        binding.rvMenu.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildLayoutPosition(view);
                outRect.bottom = (int) mContext.getResources().getDimension(R.dimen.size_10);
                outRect.top = (int) mContext.getResources().getDimension(R.dimen.size_10);
                //add left margin only to the first column
                if (position % 2 == 0) {
                    outRect.left = (int) mContext.getResources().getDimension(R.dimen.size_10);
                    outRect.right = (int) mContext.getResources().getDimension(R.dimen.size_5);
                } else {
                    outRect.left = (int) mContext.getResources().getDimension(R.dimen.size_5);
                    outRect.right = (int) mContext.getResources().getDimension(R.dimen.size_10);
                }
            }
        });
        adapter = new MenuAdapter(mContext, menuList, displayWidth, displayHeight);
        binding.rvMenu.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private static class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context mContext;
        private List<String> menuList = new ArrayList<>();
        public int displayWidth, displayHeight;

        public MenuAdapter(Context mContext, List<String> menuList, int displayWidth, int displayHeight) {
            this.mContext = mContext;
            this.menuList = menuList;
            this.displayHeight = displayHeight;
            this.displayWidth = displayWidth;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            @NonNull ItemMainMenuBinding adapterBinding = ItemMainMenuBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new MyViewHolder(adapterBinding, displayWidth, displayHeight);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            MyViewHolder holder = (MyViewHolder) viewHolder;
            holder.binding.txtTitle.setText(menuList.get(viewHolder.getAdapterPosition()));
            holder.binding.itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (menuList.get(holder.getAdapterPosition()).equals(mContext.getString(R.string.photos))) {
                        Intent intent = new Intent(mContext, FeedsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra(Constants.TAG_TYPE, mContext.getString(R.string.photos));
                        mContext.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return menuList.size();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        ItemMainMenuBinding binding;

        public MyViewHolder(@NonNull ItemMainMenuBinding binding, int displayWidth, int displayHeight) {
            super(binding.getRoot());
            this.binding = binding;
            ViewGroup.LayoutParams params = binding.itemLay.getLayoutParams();
            params.height = (int) (displayWidth * 0.5);
            binding.itemLay.setLayoutParams(params);
        }
    }
}
