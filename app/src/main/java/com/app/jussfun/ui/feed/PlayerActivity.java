package com.app.jussfun.ui.feed;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.base.BaseActivity;
import com.app.jussfun.databinding.ActivityPlayerBinding;
import com.app.jussfun.external.CustomTypefaceSpan;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.ui.FullScreenImageViewActivity;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Map;
import java.util.Timer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayerActivity extends BaseActivity implements PlaybackPreparer {

    private static final String TAG = PlayerActivity.class.getSimpleName();
    private Context mContext;
    private ActivityPlayerBinding binding;

    private SimpleExoPlayer player;
    private boolean shouldAutoPlay = true, durationSet = false;
    private Uri videoUri;
    private static DefaultBandwidthMeter BANDWIDTH_METER;
    private static String USER_AGENT;
    private boolean USE_BANDWIDTH_METER = false, needRetrySource;
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    private DefaultTrackSelector trackSelector;
    DefaultLoadControl loadControl;
    private long resumePosition;
    private boolean isPlayerInitialized = false;
    private int resumeWindow;
    Timer netWorktimer = new Timer();
    private AppUtils appUtils;
    private Player.EventListener playerListener;
    private PopupMenu popupMenu;
    private String feedId;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    /*private Runnable timerUpdate = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                long currentMilliSeconds = player.getCurrentPosition();
                String duration, commentDuration;
                duration = appUtils.getFormattedDuration(currentMilliSeconds);
                commentDuration = appUtils.getCommentsDuration(currentMilliSeconds);
                if (tempCommentList.size() > 0) {
                    if (timeList.contains(commentDuration.trim())) {
                        int firstIndex = timeList.indexOf(commentDuration.trim());
                        int lastIndex = timeList.lastIndexOf(commentDuration.trim());
                        List<StreamDetails.Comments> subList;
                        if (firstIndex == lastIndex) {
                            subList = Collections.singletonList(tempCommentList.get(firstIndex));
                        } else {
                            subList = tempCommentList.subList(firstIndex, lastIndex);
                        }
                        for (StreamDetails.Comments comments : subList) {
                            if (comments.getTime() != null && comments.getTime().equals(commentDuration)) {
                                setCommentUI(comments);
                            }
                        }
                    }
                }
                txtTime.setText(duration + " / " + totalTime);
                handler.postDelayed(timerUpdate, 1000);
            }
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            // TODO: The system bars are visible. Make any desired
                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    decorView.setSystemUiVisibility(uiOptions);
                                }
                            }, 1000);
                        } else {
                            // TODO: The system bars are NOT visible. Make any desired
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                        }
                    }
                });
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initValues();
        initView();
    }

    private void initValues() {
        feedId = getIntent().getStringExtra(Constants.TAG_FEED_ID);
        mContext = this;
        videoUri = Uri.parse(getIntent().getStringExtra(Constants.TAG_VIDEO));
        appUtils = new AppUtils(this);
        USER_AGENT = Util.getUserAgent(this, getString(R.string.app_name));
        BANDWIDTH_METER = new DefaultBandwidthMeter.Builder(this).build();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        playerListener = new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.i(TAG, "onPlayerStateChanged: " + playbackState);
                Log.i(TAG, "onPlayerStateChanged: " + playWhenReady);
                if (playbackState == Player.STATE_ENDED) {
                /*btnRefresh.setVisibility(VISIBLE);
                btnPlay.setVisibility(GONE);
                handler.removeCallbacks(timerUpdate);*/
                    durationSet = false;
                } else if (playWhenReady && playbackState == Player.STATE_READY) {/*
                btnPlay.setChecked(true);
                handler.post(timerUpdate);
                if (!durationSet) {
                    durationSet = true;
                    long duration = player.getDuration();
                    if (TimeUnit.MILLISECONDS.toHours(player.getDuration()) != 0)
                        totalTime = String.format(Locale.ENGLISH, "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration),
                                TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                                TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
                    else
                        totalTime = String.format(Locale.ENGLISH, "%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                                TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
                    txtBottomDuration.setText(totalTime);
                }
                loadingLay.setVisibility(GONE);
                avBallIndicator.setVisibility(GONE);
            */
                } else if (playbackState == Player.STATE_BUFFERING) {
                /*avBallIndicator.setVisibility(VISIBLE);
                handler.removeCallbacks(timerUpdate);*/
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Log.i(TAG, "onIsPlayingChanged: " + isPlaying);
                if (!isPlaying) {
//                handler.removeCallbacks(timerUpdate);
                }
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                Log.i(TAG, "onPositionDiscontinuity: " + reason);
                if (needRetrySource) {
                    // This will only occur if the user has performed a seek whilst in the error state. Update the
                    // resume position so that if the user then retries, playback will resume from the position to
                    // which they seeked.
                    updateResumePosition();
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException e) {
                try {
                    String errorString = null;
                    Log.i(TAG, "onPlayerError: " + e.type);
                    if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                        Exception cause = e.getRendererException();
                        if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                            // Special case for decoder initialization failures.
                            MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                                    (MediaCodecRenderer.DecoderInitializationException) cause;
                            if (decoderInitializationException.diagnosticInfo == null) {
                                if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                                    errorString = getString(R.string.error_querying_decoders);
                                } else if (decoderInitializationException.secureDecoderRequired) {
                                    errorString = getString(R.string.error_no_secure_decoder,
                                            decoderInitializationException.mimeType);
                                } else {
                                    errorString = getString(R.string.error_no_decoder,
                                            decoderInitializationException.mimeType);
                                }
                            } else {
                                errorString = getString(R.string.error_instantiating_decoder,
                                        decoderInitializationException.diagnosticInfo);
                            }
                        }
                    } else if (e.type == ExoPlaybackException.TYPE_SOURCE) {
                        if (!NetworkReceiver.isConnected()) {
                            App.makeToast(getString(R.string.no_internet_connection));
                        } else {
                            //App.makeToast("Source error");
                            App.makeToast("Please wait, Video progressing");
                        }
                        finish();
                    } else {
                        Log.e(TAG, "onPlayerError: " + e.getUnexpectedException().getMessage());
                    }
                    if (errorString != null) {
                        App.makeToast(errorString);
                    }
                    needRetrySource = true;
                    if (isBehindLiveWindow(e)) {
                        shouldAutoPlay = false;
                        clearResumePosition();
                        play(null);
                    } else {
                        updateResumePosition();
                    }
                } catch (Exception exception) {
                    Log.e(TAG, "onPlayerError: " + exception.getMessage());
                }
            }
        };
    }

    private void initView() {
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.btnMenu.setVisibility(View.VISIBLE);
        binding.btnMenu.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite));
        binding.btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenu(v);
            }
        });

        if (videoUri != null) {
            setUpExoPlayer(videoUri);
        }
    }

    private void setUpExoPlayer(Uri videoUri) {
        boolean needNewPlayer = player == null;
        if (needNewPlayer) {
            isPlayerInitialized = true;
            binding.playerView.setControllerVisibilityListener(null);
            binding.playerView.requestFocus();
            binding.playerView.setKeepContentOnPlayerReset(true);
            if (videoUri.toString().startsWith("file://")) {
                player = new SimpleExoPlayer.Builder(mContext).build();
            } else {
                final int loadControlStartBufferMs = 1500;
                /* Instantiate a DefaultLoadControl.Builder. */
                DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
                builder.setBufferDurationsMs(
                        DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                        loadControlStartBufferMs,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
                /* Build the actual DefaultLoadControl instance */
                loadControl = builder.createDefaultLoadControl();
                TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
                trackSelector = new DefaultTrackSelector(mContext, videoTrackSelectionFactory);
                player = new SimpleExoPlayer.Builder(mContext)
                        .setBandwidthMeter(BANDWIDTH_METER)
                        .setLoadControl(loadControl)
                        .setTrackSelector(trackSelector)
                        .build();
            }
            /* Milliseconds of media data buffered before playback starts or resumes. */
            player.addListener(playerListener);
            player.addVideoListener(new VideoListener() {
                @Override
                public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                    Log.d(TAG, "onVideoSizeChanged: " + width + ", " + height + ", " + unappliedRotationDegrees + ", " + pixelWidthHeightRatio);
                }

                @Override
                public void onSurfaceSizeChanged(int width, int height) {
                    VideoListener.super.onSurfaceSizeChanged(width, height);
                }

                @Override
                public void onRenderedFirstFrame() {
                    VideoListener.super.onRenderedFirstFrame();
                }
            });
            binding.playerView.setPlayer(player);
            binding.playerView.setPlaybackPreparer(this);
            binding.playerView.setControllerAutoShow(true);
            binding.playerView.setUseController(true);
            binding.playerView.setControllerHideOnTouch(true);
            binding.playerView.showController();
            //playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            //player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);

            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        }
        if (needNewPlayer || needRetrySource) {
            // Prepare the player with the source.
            MediaSource mediaSource = buildMediaSource(videoUri, StorageUtils.getMimeType("" + videoUri));
            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                player.seekTo(resumeWindow, resumePosition);
            }
            player.setPlayWhenReady(shouldAutoPlay);
            player.prepare(mediaSource, !haveResumePosition, false);
            needRetrySource = false;
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory(USE_BANDWIDTH_METER);
        Log.i(TAG, "buildMediaSource: " + type);
        switch (type) {
            case C.TYPE_SS: {
                SsMediaSource.Factory ssMediaSourceFactory = new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false));
                return ssMediaSourceFactory.createMediaSource(uri);
            }
            case C.TYPE_DASH: {
                DashMediaSource.Factory dashMediaFactory = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false));
                return dashMediaFactory.createMediaSource(uri);
            }
            case C.TYPE_HLS: {
                HlsDataSourceFactory hlsDataSourceFactory = new DefaultHlsDataSourceFactory(buildDataSourceFactory(USE_BANDWIDTH_METER));
                return new HlsMediaSource.Factory(hlsDataSourceFactory)
                        .setAllowChunklessPreparation(true).createMediaSource(uri);
            }
            case C.TYPE_OTHER: {
                if (!uri.getScheme().equals("rtmp")) {
                    if (uri.toString().startsWith("file://")) {
                        DefaultExtractorsFactory extractorsFactory =
                                new DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true);
                        DataSource.Factory dataSourceFactory =
                                new DefaultDataSourceFactory(this, USER_AGENT);
                        ProgressiveMediaSource.Factory factory = new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory);
                        return factory.createMediaSource(uri);
                    } else {
                        return new ProgressiveMediaSource.Factory(buildDataSourceFactory(USE_BANDWIDTH_METER))
                                .createMediaSource(uri);
                    }
                }
            }
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    public void play(View view) {
        setUpExoPlayer(videoUri);
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(mContext, null, buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(USER_AGENT, bandwidthMeter);
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getContentPosition())
                : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    private void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    private void startPlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    @Override
    public void preparePlayback() {
        Log.i(TAG, "preparePlayback: ");
        player.retry();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appUtils.pauseExternalAudio();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        handler.removeCallbacks(timerUpdate);
        releasePlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        handler.removeCallbacks(timerUpdate);
        releasePlayer();
        appUtils.resumeExternalAudio();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (netWorktimer != null) {
            netWorktimer.cancel();
            netWorktimer = null;
        }
    }

    private void releasePlayer() {
        if (Util.SDK_INT <= 23) {
            if (binding.playerView != null) {
                binding.playerView.onPause();
            }
        }
        if (player != null) {
            shouldAutoPlay = player.getPlayWhenReady();
            updateResumePosition();
            player.release();
            player = null;
            binding.playerView.setPlayer(/* player= */ null);
            player = null;
            trackSelector = null;
        }
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
                    AppUtils appUtils = new AppUtils(PlayerActivity.this);
                    appUtils.openReportDialog(feedId, PlayerActivity.this);
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
        AppUtils.showSnack(this, binding.parentLay, isConnected);
    }
}