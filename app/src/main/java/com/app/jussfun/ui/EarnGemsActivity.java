package com.app.jussfun.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.helper.BannerAdUtils;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.SharedPref;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EarnGemsActivity extends BaseFragmentActivity {

    private static final String TAG = EarnGemsActivity.class.getSimpleName();
    ApiInterface apiInterface;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.txtGemsDesc)
    TextView txtGemsDesc;
    @BindView(R.id.btnSettings)
    ImageView btnCopy;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btnWatchVideo)
    Button btnWatchVideo;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.lottieImage)
    ImageView lottieImage;
    @BindView(R.id.btnRefer)
    Button btnRefer;
    @BindView(R.id.txtVideoTime)
    TextView txtVideoTime;
    private LinearLayout.LayoutParams linearParams;
    private CountDownTimer timer;
    private AppUtils appUtils;
    private RewardedAd mRewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlack));
        }
        setContentView(R.layout.activity_earn_gems);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        initView();
    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);
        btnCopy.setVisibility(View.VISIBLE);
        btnCopy.setImageDrawable(getResources().getDrawable(R.drawable.ic_copy));
        int padding = AppUtils.dpToPx(this, 12);
        btnCopy.setPadding(padding, padding, padding, padding);
        txtGemsDesc.setText(String.format(getString(R.string.free_gems_content), getString(R.string.app_name)));

        linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearParams.height = (int) (getDisplayHeight() * 0.35);
        lottieImage.setLayoutParams(linearParams);

        if (SystemClock.elapsedRealtime() > SharedPref.getLong(SharedPref.VIDEO_END_TIME, 0)) {
            btnWatchVideo.setEnabled(true);
            btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
            txtVideoTime.setText("");
            txtVideoTime.setVisibility(View.INVISIBLE);
        } else {
            txtVideoTime.setVisibility(View.VISIBLE);
            long endTime = SharedPref.getLong(SharedPref.VIDEO_END_TIME, 0);
            timer = new CountDownTimer(endTime - SystemClock.elapsedRealtime(), 1000) {
                @Override
                public void onTick(long l) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long hours = (((endTime - SystemClock.elapsedRealtime()) / 1000) / 60) / 60;
                            long minutes = ((endTime - SystemClock.elapsedRealtime()) / 1000) / 60;
                            long seconds = ((endTime - SystemClock.elapsedRealtime()) / 1000) % 60;
                            String videoTime = getString(R.string.video_time) + " " + AppUtils.twoDigitString(hours) +
                                    getString(R.string.h).toLowerCase() + " " + AppUtils.twoDigitString(minutes) + getString(R.string.m).toLowerCase() +
                                    " " + AppUtils.twoDigitString(seconds) + getString(R.string.s).toLowerCase();
                            txtVideoTime.setText(videoTime);
                            btnWatchVideo.setEnabled(false);
                            btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorGrey));
                        }
                    });
                }

                @Override
                public void onFinish() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timer.cancel();
                            txtVideoTime.setText("");
                            txtVideoTime.setVisibility(View.INVISIBLE);
                            btnWatchVideo.setEnabled(true);
                            btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                        }
                    });
                }
            };
            timer.start();
        }

        btnCopy.setImageDrawable(getDrawable(R.drawable.copy));
        txtTitle.setText(getString(R.string.earn_gems));
        if (!AdminData.showVideoAd.equals("1")) {
            btnWatchVideo.setVisibility(View.INVISIBLE);
        }
        loadAd();
        initVideoAd();

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }


    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            BannerAdUtils.getInstance(this).loadAd(TAG, adView);
        }
    }

    private void initVideoAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, getString(R.string.video_ad_id),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "Ad was loaded.");
                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad was shown.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.d(TAG, "Ad failed to show.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad was dismissed.");
                                mRewardedAd = null;
                            }
                        });
                    }
                });
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

    @OnClick({R.id.btnBack, R.id.btnSettings, R.id.btnWatchVideo, R.id.btnRefer})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnSettings:
                App.preventMultipleClick(btnCopy);
                copyReferCode();
                break;
            case R.id.btnWatchVideo:
                btnWatchVideo.setEnabled(false);
                openVideo();
                break;
            case R.id.btnRefer:
                App.preventMultipleClick(btnRefer);
                openShare();
                break;
        }
    }

    private void loadRewardedVideoAd() {
        if (mRewardedAd != null) {
            mRewardedAd.show(this, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d(TAG, "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    Log.d(TAG, "onUserEarnedReward: "+ rewardAmount);
                    Log.d(TAG, "onUserEarnedReward: "+ rewardType);
                    onRewarded(rewardItem);
                }
            });
        } else {
            Log.e(TAG, "The rewarded ad wasn't ready yet.");
            btnWatchVideo.setEnabled(true);
        }
    }

    private void openShare() {
        Task<ShortDynamicLink> shortLinkTask = appUtils.getReferralDynamicLink();

        shortLinkTask.addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
            @Override
            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                if (task.isSuccessful()) {
                    // Short link created
                    Uri shortLink = task.getResult().getShortLink();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    String msg = String.format(getString(R.string.invite_description), getString(R.string.app_name), shortLink);
                    intent.putExtra(Intent.EXTRA_TEXT, msg);
                    startActivity(Intent.createChooser(intent, getString(R.string.share_link)));
                } else {
                    // Error
                    // ...
                    Log.e(TAG, "onComplete: ");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
            }
        });
    }

    private void copyReferCode() {

        Task<ShortDynamicLink> shortLinkTask = appUtils.getReferralDynamicLink();

        shortLinkTask.addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
            @Override
            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                if (task.isSuccessful()) {
                    // Short link created
                    // Short link created
                    Uri shortLink = task.getResult().getShortLink();
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    String msg = String.format(getString(R.string.invite_description), getString(R.string.app_name), shortLink);
                    ClipData clip = ClipData.newPlainText("label", "" + msg);
                    clipboard.setPrimaryClip(clip);
                    App.makeToast(getString(R.string.referral_code_copied));
                } else {
                    // Error
                    // ...
                    Log.e(TAG, "onComplete: ");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
            }
        });

    }

    private void openVideo() {
        loadRewardedVideoAd();
    }

    public void onRewarded(RewardItem rewardItem) {
        updateRewards(rewardItem.getAmount());
        SharedPref.putLong(SharedPref.VIDEO_END_TIME, (SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(AdminData.videoAdsDuration)));
        Long endTime = SharedPref.getLong(SharedPref.VIDEO_END_TIME, 0);
        btnWatchVideo.setEnabled(false);
        btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorGrey));
        txtVideoTime.setVisibility(View.VISIBLE);
        timer = new CountDownTimer(TimeUnit.MINUTES.toMillis(AdminData.videoAdsDuration), 1000) {
            @Override
            public void onTick(long l) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnWatchVideo.setEnabled(false);
                        long hours = (((endTime - SystemClock.elapsedRealtime()) / 1000) / 60) / 60;
                        long minutes = ((endTime - SystemClock.elapsedRealtime()) / 1000) / 60;
                        long seconds = ((endTime - SystemClock.elapsedRealtime()) / 1000) % 60;
                        String videoTime = getString(R.string.video_time) + " " + AppUtils.twoDigitString(hours) + " " +
                                getString(R.string.h).toLowerCase() + " " + AppUtils.twoDigitString(minutes) + getString(R.string.m).toLowerCase() +
                                " " + AppUtils.twoDigitString(seconds) + getString(R.string.s).toLowerCase();
                        txtVideoTime.setText(videoTime);
                    }
                });
            }

            @Override
            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timer.cancel();
                        txtVideoTime.setVisibility(View.INVISIBLE);
                        btnWatchVideo.setEnabled(true);
                        btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                    }
                });
            }
        };
        timer.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        btnWatchVideo.setEnabled(true);
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateRewards(int amount) {
        Call<Map<String, String>> call = apiInterface.updateVideoGems(GetSet.getUserId());
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> map = response.body();
                if (map.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                    GetSet.setGems(Long.valueOf(map.get(Constants.TAG_TOTAL_GEMS)));
                    SharedPref.putLong(SharedPref.GEMS, GetSet.getGems());
                    App.makeToast(getString(R.string.gems_added_to_your_account));
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }
}
