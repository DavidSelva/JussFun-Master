package com.app.jussfun.ui;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.base.BaseActivity;
import com.app.jussfun.databinding.ActivityFullScreenImageViewBinding;
import com.app.jussfun.external.CustomTypefaceSpan;
import com.app.jussfun.helper.AppWebSocket;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.Logging;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FullScreenImageViewActivity extends BaseActivity {

    private static final String TAG = FullScreenImageViewActivity.class.getSimpleName();
    private ActivityFullScreenImageViewBinding binding;
    private PopupMenu popupMenu;
    private String feedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullScreenImageViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        feedId = getIntent().getStringExtra(Constants.TAG_FEED_ID);
        binding.toolBarLay.btnBack.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite));
        binding.toolBarLay.btnMenu.setVisibility(View.VISIBLE);
        binding.toolBarLay.btnMenu.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite));
        loadImage(getIntent().getStringExtra(Constants.TAG_IMAGE));
        binding.toolBarLay.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.toolBarLay.btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenu(v);
            }
        });
    }

    private void loadImage(String image) {
        Log.d(TAG, "loadImage: "+ image);
        Glide.with(this)
                .load(image)
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        binding.ivFullScreen.setImageDrawable(resource);
                        return false;
                    }
                })
                .into((binding.ivFullScreen));
    }

    private void openMenu(View view) {
        popupMenu = new PopupMenu(this, view, R.style.PopupMenuBackground);
        popupMenu.getMenuInflater().inflate(R.menu.feed_menu, popupMenu.getMenu());
        popupMenu.setGravity(Gravity.START);
        popupMenu.getMenu().getItem(0).setVisible(false);
        popupMenu.getMenu().getItem(2).setVisible(false);
        Typeface typeface;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_light);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_light);
        }
        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            MenuItem menuItem = popupMenu.getMenu().getItem(i);
            SpannableString mNewTitle = new SpannableString(menuItem.getTitle());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mNewTitle.setSpan(new TypefaceSpan(typeface), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                mNewTitle.setSpan(new CustomTypefaceSpan("", typeface), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            menuItem.setTitle(mNewTitle);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menuReport) {
                    AppUtils appUtils = new AppUtils(FullScreenImageViewActivity.this);
                    appUtils.openReportDialog(feedId, FullScreenImageViewActivity.this);
                }
                return true;
            }
        });
        popupMenu.show();
    }

    public void onReportSend(String feedId, String title) {
        App.makeToast(getString(R.string.reported_successfully));
        if (NetworkReceiver.isConnected()) {
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<Map<String, String>> call = apiInterface.reportFeed(GetSet.getUserId(), feedId, title);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> data = response.body();
                        if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            App.makeToast(getString(R.string.reported_successfully));
                        }
                    }
                }


                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Log.e(TAG, "onReportSend: " + t.getMessage());
                    call.cancel();
                }
            });
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }
}