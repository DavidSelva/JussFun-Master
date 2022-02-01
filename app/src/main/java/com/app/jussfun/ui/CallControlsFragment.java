/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.app.jussfun.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.app.jussfun.base.App;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.app.jussfun.R;
import com.app.jussfun.apprtc.util.AppRTCUtils;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.Gift;
import com.app.jussfun.model.Report;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;

/**
 * Fragment for call control.
 */
public class CallControlsFragment extends Fragment {

    private static final String TAG = CallControlsFragment.class.getSimpleName();

    @BindView(R.id.controlsLay)
    LinearLayout controlsLay;
    @BindView(R.id.muteImage)
    ImageView muteImage;
    @BindView(R.id.blurLay)
    ImageView blurLay;
    @BindView(R.id.cameraLay)
    ImageView cameraLay;
    @BindView(R.id.giftsLay)
    ImageView giftsLay;
    @BindView(R.id.reportLay)
    ImageView reportLay;
    @BindView(R.id.exitLay)
    ImageView exitLay;
    private Context context;
    public OnCallEvents callEvents;
    private BottomSheetDialog giftDialog;
    private BottomSheetDialog reportDialog;
    private RecyclerView giftsView, stickersView, reportsView;
    private Button btnGifts, btnStickers;
    private LinearLayout sendLay;
    CircleIndicator pagerIndicator;
    private TextView txtAttachmentName, txtSend, txtReport;
    StickersAdapter stickersAdapter;
    GiftAdapter giftAdapter;
    ReportAdapter reportAdapter;
    ViewPager viewPager;
    private LinearLayoutManager stickerLayoutManager;
    private boolean isMicEnabled = true, isBlurred = false, isVideoCall = true;
    private int giftPosition = 0;
    private RecyclerView recyclerView;
    private int ITEM_LIMIT = 8;
    List<String> tempSmileyList = new ArrayList<>();
    List<Gift> tempGiftList = new ArrayList<>();
    int displayHeight, displayWidth;
    Bundle bundle;
    private RandomCallFragment randomCallFragment = null;

    public void setCallEvents(RandomCallFragment fragment) {
        callEvents = fragment;
        randomCallFragment = fragment;
    }

    @Override
    public void onStart() {
        super.onStart();

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        displayWidth = displayMetrics.widthPixels;
        displayHeight = displayMetrics.heightPixels;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View controlView = inflater.inflate(R.layout.fragment_call_controls, container, false);
        ButterKnife.bind(this, controlView);
        bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(Constants.TAG_FROM)) {
                if (bundle.getString(Constants.TAG_FROM).equals(Constants.TAG_RECEIVE)) {
                    if (bundle.getString(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_AUDIO)) {
                        controlsLay.setVisibility(View.GONE);
                        cameraLay.setVisibility(View.GONE);
                    }
                    controlsLay.setVisibility(View.GONE);
                } else {
                    if (bundle.getString(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_AUDIO)) {
                        cameraLay.setVisibility(View.GONE);
                    }
                    exitLay.setVisibility(View.INVISIBLE);
                    reportLay.setVisibility(View.GONE);
                    giftsLay.setVisibility(View.GONE);
                }
            }
            if (randomCallFragment == null) {
                blurLay.setVisibility(View.GONE);
            }
            isVideoCall = bundle.getBoolean(AppRTCUtils.EXTRA_VIDEO_CALL, true);
            if (!isVideoCall) {
                setAudioCallUI();
            }
        }
        return controlView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void hideDialogs() {
        if (giftDialog != null && giftDialog.isShowing()) {
            giftDialog.dismiss();
        }
        if (reportDialog != null && reportDialog.isShowing()) {
            reportDialog.dismiss();
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCallUI() {
        controlsLay.setVisibility(View.VISIBLE);
        reportLay.setVisibility(View.GONE);
        exitLay.setVisibility(View.INVISIBLE);
        giftsLay.setVisibility(View.GONE);
    }

    public void setAudioCallUI() {
        controlsLay.setVisibility(View.VISIBLE);
        reportLay.setVisibility(View.VISIBLE);
        exitLay.setVisibility(View.VISIBLE);
        giftsLay.setVisibility(View.VISIBLE);
        cameraLay.setVisibility(View.INVISIBLE);
        blurLay.setVisibility(View.INVISIBLE);
    }

    public void setControlEvent(VideoCallActivity activity) {
        this.callEvents = activity;
    }

    /**
     * Call control interface for container activity.
     */
    public interface OnCallEvents {

        void onCallHangUp();

        void onCameraSwitch();

        boolean onToggleMic(boolean isMute);

        void onStickerSend(String sticker);

        void onGiftSend(Gift gift);

        void onReportSend(String report);

        void onBlurClicked(boolean isBlured);

    }

    @OnClick({R.id.cameraLay, R.id.muteImage, R.id.giftsLay, R.id.reportLay, R.id.exitLay, R.id.blurLay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cameraLay:
                callEvents.onCameraSwitch();
                break;
            case R.id.muteImage:
                if (!isMicEnabled) {
                    isMicEnabled = true;
                    muteImage.setImageDrawable(getResources().getDrawable(R.drawable.microphone_white));
                } else {
                    isMicEnabled = false;
                    muteImage.setImageDrawable(getResources().getDrawable(R.drawable.microphone_with_shape));
                }
                callEvents.onToggleMic(isMicEnabled);
                break;
            case R.id.giftsLay:
                App.preventMultipleClick(giftsLay);
                openGiftDialog();
                break;
            case R.id.reportLay:
                App.preventMultipleClick(reportLay);
                openReportDialog();
                break;
            case R.id.exitLay:
                App.preventMultipleClick(exitLay);
                closeConfirmDialog();
                break;
            case R.id.blurLay:
                if (randomCallFragment != null) {
                    if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                        setBlurUI();
                        callEvents.onBlurClicked(isBlurred);
                    } else {
                        App.makeToast(getString(R.string.upgrade_to_premium_to_access));
                    }
                } else {
                    setBlurUI();
                    callEvents.onBlurClicked(isBlurred);
                }
                break;
        }
    }

    private void setBlurUI() {
        if (!isBlurred) {
            isBlurred = true;
            blurLay.setSelected(true);
        } else {
            isBlurred = false;
            blurLay.setSelected(false);
        }
    }

    private void openGiftDialog() {
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_stickers, null);
        giftDialog = new BottomSheetDialog(getActivity(), R.style.Bottom_StickerDialog); // Style here
        giftDialog.setContentView(sheetView);
        giftDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });

        viewPager = sheetView.findViewById(R.id.viewPager);
        giftsView = sheetView.findViewById(R.id.giftsView);
        stickersView = sheetView.findViewById(R.id.stickersView);
        btnGifts = sheetView.findViewById(R.id.btnGifts);
        btnStickers = sheetView.findViewById(R.id.btnStickers);
        sendLay = sheetView.findViewById(R.id.sendLay);
        txtAttachmentName = sheetView.findViewById(R.id.txtAttachmentName);
        txtSend = sheetView.findViewById(R.id.txtSend);
        pagerIndicator = sheetView.findViewById(R.id.pagerIndicator);

        if (isVideoCall) {
            btnStickers.setVisibility(View.VISIBLE);
            btnGifts.setVisibility(View.VISIBLE);
            btnStickers.setTextColor(getResources().getColor(R.color.colorWhiteText));
            sendLay.setVisibility(View.GONE);
            giftsView.setVisibility(View.INVISIBLE);
            stickersView.setVisibility(View.VISIBLE);
            setViewPager(Constants.TYPE_STICKERS);
        } else {
            btnStickers.setVisibility(View.GONE);
            btnGifts.setTextColor(getResources().getColor(R.color.colorWhiteText));
            sendLay.setVisibility(View.GONE);
            giftsView.setVisibility(View.VISIBLE);
            stickersView.setVisibility(View.INVISIBLE);
            setViewPager(Constants.TYPE_GIFTS);
        }

        btnStickers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setViewPager(Constants.TYPE_STICKERS);
                btnGifts.setTextColor(getResources().getColor(R.color.colorSecondaryText));
                btnStickers.setTextColor(getResources().getColor(R.color.colorWhiteText));
                sendLay.setVisibility(View.GONE);
                giftsView.setVisibility(View.INVISIBLE);
                stickersView.setVisibility(View.VISIBLE);
//                pagerIndicator.attachToRecyclerView(stickersView);
            }
        });

        btnGifts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setViewPager(Constants.TYPE_GIFTS);
                btnGifts.setTextColor(getResources().getColor(R.color.colorWhiteText));
                btnStickers.setTextColor(getResources().getColor(R.color.colorSecondaryText));
                sendLay.setVisibility(View.GONE);
                giftsView.setVisibility(View.VISIBLE);
                stickersView.setVisibility(View.INVISIBLE);
            }
        });

        giftDialog.show();
    }

    private void setViewPager(String type) {
        int count;
        if (type.equals(Constants.TYPE_STICKERS)) {
            if (AppUtils.smileyList.size() <= ITEM_LIMIT) {
                count = 1;
            } else {
                count = AppUtils.smileyList.size() % ITEM_LIMIT == 0 ? AppUtils.smileyList.size() / ITEM_LIMIT : (AppUtils.smileyList.size() / ITEM_LIMIT) + 1;
            }
        } else {
            if (AdminData.giftList.size() <= ITEM_LIMIT) {
                count = 1;
            } else {
                count = AdminData.giftList.size() % ITEM_LIMIT == 0 ? AdminData.giftList.size() / ITEM_LIMIT : (AdminData.giftList.size() / ITEM_LIMIT) + 1;
            }
        }

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(context, count, type);
        viewPager.setAdapter(pagerAdapter);
        pagerIndicator.setViewPager(viewPager);
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadItems(type);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void loadItems(String type) {
        /*Set Array list by 8*/
        int start = viewPager.getCurrentItem() * ITEM_LIMIT;
        int end = start + (ITEM_LIMIT - 1);
        /* Set last index */
        if (type.equals(Constants.TYPE_STICKERS)) {
            if (end > AppUtils.smileyList.size()) {
                end = AppUtils.smileyList.size() - 1;
            } else if (AppUtils.smileyList.size() <= end) {
                end = AppUtils.smileyList.size() - 1;
            }
            loadStickers(start, end);
        } else if (type.equals(Constants.TYPE_GIFTS)) {
            if (end > AdminData.giftList.size()) {
                end = AdminData.giftList.size() - 1;
            } else if (AdminData.giftList.size() <= end) {
                end = AdminData.giftList.size() - 1;
            }
            loadGifts(start, end);
        }
    }


    public class ViewPagerAdapter extends PagerAdapter {
        private final Context context;
        private int count = 0;
        private String type = "";

        public ViewPagerAdapter(Context context, int count, String type) {
            this.context = context;
            this.type = type;
            this.count = count;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.layout_stickers, container, false);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            loadItems(type);
            container.addView(itemView, 0);
            return itemView;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup view, int position, @NonNull Object object) {
            view.removeView((View) object);
        }
    }

    private void loadStickers(int start, int end) {
        tempSmileyList = new ArrayList<>();

        for (int i = start; i <= end; i++) {
            tempSmileyList.add(AppUtils.smileyList.get(i));
        }
        stickersAdapter = new StickersAdapter(context, tempSmileyList);
        stickerLayoutManager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(stickerLayoutManager);
        recyclerView.setAdapter(stickersAdapter);
        stickersAdapter.notifyDataSetChanged();
    }

    private void loadGifts(int start, int end) {
        tempGiftList = new ArrayList<>();
        /*tempGiftList used only for display, For send AdminData.giftList used*/
        for (int i = start; i <= end; i++) {
            tempGiftList.add(AdminData.giftList.get(i));
        }
        giftAdapter = new GiftAdapter(context, tempGiftList);
        stickerLayoutManager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(stickerLayoutManager);
        recyclerView.setAdapter(giftAdapter);
        giftAdapter.notifyDataSetChanged();


        txtSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Fetch selected position from AdminData.giftList*/
                int position = (viewPager.getCurrentItem() * ITEM_LIMIT) + giftPosition;
                callEvents.onGiftSend(AdminData.giftList.get(position));
                sendLay.setVisibility(View.GONE);
                txtAttachmentName.setText("");
            }
        });

    }

    public void dismissGiftDialog() {
        if (giftDialog != null && giftDialog.isShowing()) {
            giftDialog.cancel();
        }
    }

    private void openReportDialog() {
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_report, null);
        reportDialog = new BottomSheetDialog(context, R.style.Bottom_FilterDialog); // Style here
        reportDialog.setContentView(sheetView);
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) sheetView.getParent());
        int maxHeight = (displayHeight * 80) / 100;
        mBehavior.setPeekHeight(maxHeight);
        sheetView.requestLayout();
        reportsView = sheetView.findViewById(R.id.reportView);
        reportDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });

        loadReports();
    }

    private void loadReports() {
        reportAdapter = new ReportAdapter(context, AdminData.reportList);
        reportsView.setLayoutManager(new LinearLayoutManager(context));
        reportsView.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        reportDialog.show();
    }

    private void closeConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(getString(R.string.really_want_exit));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callEvents.onCallHangUp();
                dialog.cancel();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_light);
        } else {
            typeface = ResourcesCompat.getFont(context, R.font.font_light);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);
    }

    public class StickersAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;
        @BindView(R.id.itemLay)
        LinearLayout itemLay;
        private List<String> stickersList = new ArrayList<>();
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public StickersAdapter(Context context, List<String> smileyList) {
            this.context = context;
            this.stickersList = smileyList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_stickers, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                ((MyViewHolder) holder).stickerImage.setAnimation(stickersList.get(position) + ".json");
                ((MyViewHolder) holder).stickerImage.setImageAssetsFolder(AppUtils.SMILEY_PATH + stickersList.get(position));
                ((MyViewHolder) holder).stickerImage.setRepeatCount(LottieDrawable.INFINITE);
            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(View.VISIBLE);
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
            int itemCount = stickersList.size();
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

        public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.stickerImage)
            LottieAnimationView stickerImage;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }


            @OnClick(R.id.itemLay)
            public void onViewClicked() {
                App.preventMultipleClick(itemView);
                if (callEvents != null) {
                    callEvents.onStickerSend(tempSmileyList.get(getAdapterPosition()));
                }
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.progressBar)
            ProgressBar progressBar;

            public FooterViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
            }
        }
    }

    public class GiftAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;
        private List<Gift> giftList = new ArrayList<>();
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public GiftAdapter(Context context, List<Gift> giftList) {
            this.context = context;
            this.giftList = giftList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_gifts, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final Gift gift = giftList.get(position);
                ((MyViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).giftImage.setVisibility(View.INVISIBLE);
                Glide.with(context)
                        .load(Constants.GIFT_IMAGE_URL + gift.getGiftIcon())
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                ((MyViewHolder) holder).progressBar.setVisibility(View.GONE);
                                ((MyViewHolder) holder).giftImage.setImageDrawable(context.getDrawable(R.drawable.ic_gift_primary));
                                ((MyViewHolder) holder).giftImage.setVisibility(View.VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                ((MyViewHolder) holder).progressBar.setVisibility(View.GONE);
                                ((MyViewHolder) holder).giftImage.setImageDrawable(resource);
                                ((MyViewHolder) holder).giftImage.setVisibility(View.VISIBLE);
                                return false;
                            }
                        }).apply(new RequestOptions().error(R.drawable.ic_gift_primary))
                        .into(((MyViewHolder) holder).giftImage);
                ((MyViewHolder) holder).txtGiftPrice.setText((GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) ? "" + gift.getGiftGemsPrime() : "" + gift.getGiftGems());
            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(View.VISIBLE);
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
            int itemCount = giftList.size();
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

        public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.giftImage)
            ImageView giftImage;
            @BindView(R.id.txtGiftPrice)
            TextView txtGiftPrice;
            @BindView(R.id.itemLay)
            RelativeLayout itemLay;
            @BindView(R.id.progressBar)
            ProgressBar progressBar;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                ViewGroup.LayoutParams params = itemLay.getLayoutParams();
                // Changes the height and width to the specified *pixels*
                params.width = displayWidth / 4;
                itemLay.setLayoutParams(params);
            }


            @OnClick(R.id.itemLay)
            public void onViewClicked() {
                if ((GetSet.getPremiumMember().equals(Constants.TAG_TRUE) &&
                        GetSet.getGems() >= giftList.get(getAdapterPosition()).getGiftGemsPrime()) ||
                        (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE) &&
                                GetSet.getGems() >= giftList.get(getAdapterPosition()).getGiftGems())) {
                    giftPosition = getAdapterPosition();
                    sendLay.setVisibility(View.VISIBLE);
                    txtSend.setVisibility(View.VISIBLE);
                    txtAttachmentName.setText(giftList.get(getAdapterPosition()).getGiftTitle());
                } else {
                    App.makeToast(getString(R.string.not_enough_gems));
                }
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.progressBar)
            ProgressBar progressBar;

            public FooterViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
            }
        }
    }

    public class ReportAdapter extends RecyclerView.Adapter {
        private final Context context;
        private List<Report> reportList = new ArrayList<>();
        private RecyclerView.ViewHolder viewHolder;

        public ReportAdapter(Context context, List<Report> reportList) {
            this.context = context;
            this.reportList = reportList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_report, parent, false);
            viewHolder = new MyViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MyViewHolder) holder).txtReport.setText(reportList.get(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return reportList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txtReport)
            TextView txtReport;
            @BindView(R.id.itemReportLay)
            LinearLayout itemReportLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            @OnClick(R.id.itemReportLay)
            public void onViewClicked() {
                App.preventMultipleClick(itemReportLay);
                if (callEvents != null) {
                    reportDialog.dismiss();
                    callEvents.onReportSend(reportList.get(getAdapterPosition()).getTitle());
                }
            }
        }
    }
}
