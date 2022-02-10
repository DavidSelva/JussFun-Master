package com.app.jussfun.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.app.jussfun.R;
import com.app.jussfun.base.BaseActivity;
import com.app.jussfun.external.TouchImageView;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullScreenImageViewActivity extends BaseActivity {

    private static final String TAG = FullScreenImageViewActivity.class.getSimpleName();
    @BindView(R.id.ivFullScreen)
    ImageView ivFullScreen;
    @BindView(R.id.btnBack)
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image_view);
        ButterKnife.bind(this);
        btnBack.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite));
        loadImage(getIntent().getStringExtra(Constants.TAG_IMAGE));
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                        ivFullScreen.setImageDrawable(resource);
                        return false;
                    }
                })
                .into((ivFullScreen));
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }
}