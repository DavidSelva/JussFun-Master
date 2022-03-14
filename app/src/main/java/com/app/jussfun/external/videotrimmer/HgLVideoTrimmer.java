/*
 * MIT License
 *
 * Copyright (c) 2016 Knowledge, education for life.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.app.jussfun.external.videotrimmer;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.app.jussfun.R;
import com.app.jussfun.external.videotrimmer.interfaces.OnHgLVideoListener;
import com.app.jussfun.external.videotrimmer.interfaces.OnProgressVideoListener;
import com.app.jussfun.external.videotrimmer.interfaces.OnRangeSeekBarListener;
import com.app.jussfun.external.videotrimmer.interfaces.OnTrimVideoListener;
import com.app.jussfun.external.videotrimmer.utils.BackgroundExecutor;
import com.app.jussfun.external.videotrimmer.utils.TrimVideoUtils;
import com.app.jussfun.external.videotrimmer.utils.UiThreadExecutor;
import com.app.jussfun.external.videotrimmer.view.ProgressBarView;
import com.app.jussfun.external.videotrimmer.view.RangeSeekBarView;
import com.app.jussfun.external.videotrimmer.view.Thumb;
import com.app.jussfun.external.videotrimmer.view.TimeLineView;
import com.app.jussfun.helper.StorageUtils;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;

public class HgLVideoTrimmer extends FrameLayout implements View.OnClickListener, VideoRendererEventListener, PlaybackPreparer {

    private static final String TAG = HgLVideoTrimmer.class.getSimpleName();
    private static final int MIN_TIME_FRAME = 1000;
    private static final int SHOW_PROGRESS = 2;

    private SeekBar mHolderTopView;
    private RangeSeekBarView mRangeSeekBarView;
    private RelativeLayout mLinearVideo;
    private View mTimeInfoContainer;
    private TextView txtVideoSize;
    private TextView mTextTimeFrame;
    private TextView txtDuration;
    private TimeLineView mTimeLineView;

    private LinearLayout topLay;
    private RelativeLayout closeLay;
    private ImageView btnPlay;

    private ProgressBarView mVideoProgressIndicator;
    private Uri videoUri;
    private String mFinalPath;

    private int mMaxDuration;
    private List<OnProgressVideoListener> mListeners;

    private OnTrimVideoListener mOnTrimVideoListener;
    private OnHgLVideoListener mOnHgLVideoListener;

    private int mDuration = 0;
    private long mTimeVideo = 0;
    private int mStartPosition = 0;
    private int mEndPosition = 0;
    private float startTime = 0;
    private float endTime = 0;

    private long mOriginSizeFile;
    private boolean mResetSeekBar = true;
    private final MessageHandler mMessageHandler = new MessageHandler(this);
    private SharedPreferences pref;
    private SimpleExoPlayer player;
    SimpleExoPlayerView playerView;
    private StorageUtils storageUtils;
    private Context mContext;
    private boolean playReady;
    public static String MEDIA_ACTION = "media_action";
    private static DefaultBandwidthMeter BANDWIDTH_METER;
    private static String USER_AGENT;
    Handler handler;
    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    DefaultLoadControl loadControl;
    MediaController mediaController;
    private boolean needNewPlayer = player == null;
    private boolean isPlayerInitialized = false;
    private boolean shouldAutoPlay = false, durationSet = false;
    private long resumePosition;
    private int resumeWindow;
    private boolean needRetrySource;
    private DefaultTrackSelector trackSelector;
    private float videoDuration;
    private long estimatedDuration, trimStartTime, trimEndTime;
    private long originalSize, estimatedSize;

    public HgLVideoTrimmer(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.mContext = context;
    }

    public HgLVideoTrimmer(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.view_time_line, this, true);
        pref = context.getSharedPreferences("SavedPref", MODE_PRIVATE);
        storageUtils = StorageUtils.getInstance(context);
        mHolderTopView = ((SeekBar) findViewById(R.id.handlerTop));
        mVideoProgressIndicator = ((ProgressBarView) findViewById(R.id.timeVideoView));
        mRangeSeekBarView = ((RangeSeekBarView) findViewById(R.id.timeLineBar));
        mLinearVideo = ((RelativeLayout) findViewById(R.id.layout_surface_view));
        playerView = findViewById(R.id.playerView);
        mTimeInfoContainer = findViewById(R.id.timeText);
        txtVideoSize = ((TextView) findViewById(R.id.txtVideoSize));
        mTextTimeFrame = ((TextView) findViewById(R.id.textTimeSelection));
        txtDuration = ((TextView) findViewById(R.id.txtDuration));
        mTimeLineView = ((TimeLineView) findViewById(R.id.timeLineView));
        topLay = findViewById(R.id.layout);

        closeLay = findViewById(R.id.close_layout);
        btnPlay = findViewById(R.id.icon_video_play);
        USER_AGENT = Util.getUserAgent(mContext, mContext.getString(R.string.app_name));
        BANDWIDTH_METER = new DefaultBandwidthMeter.Builder(mContext).build();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        initMargins();
        setUpListeners();
        setUpMargins();
    }

    private void initExoPlayer() {
        if (needNewPlayer || needRetrySource) {
            // Prepare the player with the source.
            DefaultExtractorsFactory extractorsFactory =
                    new DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true);
            DataSource.Factory dataSourceFactory =
                    new DefaultDataSourceFactory(mContext, USER_AGENT);
            ProgressiveMediaSource.Factory factory = new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory);
            MediaSource mediaSource = factory.createMediaSource(videoUri);
            isPlayerInitialized = true;
            playerView.setControllerVisibilityListener(null);
            playerView.setControllerAutoShow(false);
            playerView.requestFocus();
            playerView.setKeepContentOnPlayerReset(true);

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
            setPlayerListener();
            playerView.setPlayer(player);
            playerView.setPlaybackPreparer(this);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                player.seekTo(resumeWindow, resumePosition);
            }
            player.setPlayWhenReady(shouldAutoPlay);
            player.getPlaybackState();
            player.prepare(mediaSource, !haveResumePosition, false);
            needRetrySource = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onVideoPrepared(playerView.getWidth(), playerView.getHeight());
                }
            }, 1000);
        }
    }


    private void setPlayerListener() {
        player.addListener(new Player.EventListener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.d(TAG, "onPlayerStateChanged: " + playWhenReady + " , " + playbackState);

                if (playWhenReady && playbackState == Player.STATE_IDLE) {

                } else if (playbackState == Player.STATE_READY) {
//                    handler.removeCallbacks(runnable);
                    if (playWhenReady) {
                        if (!durationSet) {
                            durationSet = true;
                            videoDuration = player.getDuration();
                        }
                        btnPlay.setVisibility(View.GONE);
                    } else {
//                        mTimeLineView.removeCallbacks(updateProgressRunnable);
                        btnPlay.setVisibility(View.VISIBLE);
                    }
                } else if (playbackState == Player.STATE_BUFFERING) {

                } else if (playbackState == Player.STATE_ENDED) {
                    onVideoCompleted();
                }
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                Log.i(TAG, "onPositionDiscontinuity: " + reason);
                if (needRetrySource) {
                    // This will only occur if the user has performed a seek whilst in the error state. Update the
                    // resume position so that if the user then retries, playback will resume from the position to
                    // which they seeked.

                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                if (mOnTrimVideoListener != null)
                    mOnTrimVideoListener.onError(null);
            }
        });
    }

    private void setUpListeners() {
        mTimeLineView.setOnClickListener(null);
        mListeners = new ArrayList<>();
        mListeners.add(new OnProgressVideoListener() {
            @Override
            public void updateProgress(int time, int max, float scale) {
                updateVideoProgress(time);
            }
        });
        mListeners.add(mVideoProgressIndicator);

        findViewById(R.id.close_layout)
                .setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onCancelClicked();
                            }
                        }
                );

        final GestureDetector gestureDetector = new
                GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        onClickVideoPlayPause();
                        return true;
                    }
                }
        );

        findViewById(R.id.icon_video_play)
                .setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, @NonNull MotionEvent event) {
                        gestureDetector.onTouchEvent(event);
                        return true;
                    }
                });

        playerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, @NonNull MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        mRangeSeekBarView.addOnRangeSeekBarListener(mVideoProgressIndicator);
        mRangeSeekBarView.addOnRangeSeekBarListener(new OnRangeSeekBarListener() {
            @Override
            public void onCreate(RangeSeekBarView rangeSeekBarView, int index, float value) {

            }

            @Override
            public void onSeek(RangeSeekBarView rangeSeekBarView, int index, float value) {
                onSeekThumbs(index, value);
            }

            @Override
            public void onSeekStart(RangeSeekBarView rangeSeekBarView, int index, float value) {

            }

            @Override
            public void onSeekStop(RangeSeekBarView rangeSeekBarView, int index, float value) {
//                Log.i(TAG, "onSeekStop: " + value + ", " + index);
                if (index == 0) {
                    startTime = (value * mDuration) / 100;
                } else {
                    endTime = (value * mDuration) / 100;
                }
                mTimeVideo = (long) Math.round(endTime - startTime);
                setTimeVideo(Math.round(mTimeVideo));
                onStopSeekThumbs(index, value);
            }
        });

        mHolderTopView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onPlayerIndicatorSeekChanged(progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                onPlayerIndicatorSeekStart();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "onStopTrackingTouch: " + seekBar.getProgress());
                onPlayerIndicatorSeekStop(seekBar);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setUpMargins() {
        int marge = mRangeSeekBarView.getThumbs().get(0).getWidthBitmap();
        int widthSeek = mHolderTopView.getThumb().getMinimumWidth() / 2;

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHolderTopView.getLayoutParams();
        lp.setMargins(marge - widthSeek, 0, marge - widthSeek, 0);
        mHolderTopView.setLayoutParams(lp);

        lp = (RelativeLayout.LayoutParams) mTimeLineView.getLayoutParams();
        lp.setMargins(marge, 0, marge, 0);
        mTimeLineView.setLayoutParams(lp);

        lp = (RelativeLayout.LayoutParams) mVideoProgressIndicator.getLayoutParams();
        lp.setMargins(marge, 0, marge, 0);
        mVideoProgressIndicator.setLayoutParams(lp);
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD_MR1)
    public void onSaveClicked() {
        if (mStartPosition == 0 && mEndPosition >= mDuration) {
            if (videoUri != null && videoUri.getPath() != null) {
                //notify that video trimming started
                if (mOnTrimVideoListener != null)
                    mOnTrimVideoListener.onTrimStarted();
                final File destDir = storageUtils.createCacheDir(mContext);
                String mDestFileName = getContext().getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".mp4";
                File savedFile = storageUtils.saveUriToFile(mContext, videoUri, destDir, mDestFileName);
                Bitmap thumb = storageUtils.getThumbnailFromVideo(savedFile);

                /*Uri uri = FileProvider.getUriForFile(mContext,
                        BuildConfig.APPLICATION_ID + ".provider", mSrcFile);*/
                Uri uri = Uri.parse(savedFile.getAbsolutePath());
//                File tempSrc = storageUtils.saveToCacheDir(context, uri, mDestDir, mDestFileName);
                //notify that video trimming started
                if (mOnTrimVideoListener != null) {
                    mOnTrimVideoListener.getResult(uri, thumb, mTimeVideo);
                }
            }
        } else {
            pausePlayer();

            int delta = mEndPosition - mStartPosition;
            if (delta < 1000) {
                if (mOnTrimVideoListener != null)
                    mOnTrimVideoListener.onError(getContext().getString(R.string.cannot_trim_less_than_one_second));
            } else {
                if (videoUri != null && videoUri.getPath() != null) {
                    String mDestFileName = getContext().getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".mp4";
                    File mDestDir = storageUtils.createCacheDir(mContext);
                    File savedFile = storageUtils.saveUriToFile(mContext, videoUri, mDestDir, mDestFileName);
                    final File mSrcFile = savedFile;
                    mDestFileName = getContext().getString(R.string.app_name) + "_" + System.currentTimeMillis() / 1000 + ".mp4";
                    File mDestFile = new File(mDestDir, mDestFileName);
                    Bitmap thumb = storageUtils.getFrameAtTime(mSrcFile, mStartPosition);
                    //notify that video trimming started
                    if (mOnTrimVideoListener != null)
                        mOnTrimVideoListener.onTrimStarted();
                    Log.i(TAG, "onSaveClicked: " + mStartPosition + ", " + mEndPosition);
                    BackgroundExecutor.execute(
                            new BackgroundExecutor.Task("", 0L, "") {
                                @Override
                                public void execute() {
                                    try {
                                        TrimVideoUtils videoUtils = new TrimVideoUtils(getContext());
                                        videoUtils.startTrim(mSrcFile, mDestFile, mStartPosition, mEndPosition, mOnTrimVideoListener, thumb, mTimeVideo);
                                    } catch (final Throwable e) {
                                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                                    }
                                }
                            }
                    );
                }
            }
        }
    }

    private void onClickVideoPlayPause() {
        if (player.isPlaying()) {
            mMessageHandler.removeMessages(SHOW_PROGRESS);
            pausePlayer();
        } else {
            btnPlay.setVisibility(View.GONE);
            if (mResetSeekBar) {
                mResetSeekBar = false;
                player.seekTo(mStartPosition);
            }
            setProgressBarPosition((int) player.getCurrentPosition());
//            if (!mMessageHandler.hasMessageID(SHOW_PROGRESS)) {
            Log.d("SHOW_PROGRESS", "send ---->");
            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS);
            mMessageHandler.setMessageID(SHOW_PROGRESS);
//            }
            startPlayer();
        }
    }

    private void onCancelClicked() {
        releasePlayer();
        if (mOnTrimVideoListener != null) {
            mOnTrimVideoListener.cancelAction();
        }
    }

    private void onPlayerIndicatorSeekChanged(int progress, boolean fromUser) {

        int duration = progress;
        if (fromUser) {
            if (duration < mStartPosition) {
                setProgressBarPosition(mStartPosition);
                duration = mStartPosition;
            } else if (duration > mEndPosition) {
                setProgressBarPosition(mEndPosition);
                duration = mEndPosition;
            }
            setTimeVideo(duration);
        }
    }

    private void onPlayerIndicatorSeekStart() {
//        if (mMessageHandler.hasMessageID(SHOW_PROGRESS)) {
        mMessageHandler.removeMessages();
        Log.d("SHOW_PROGRESS", "remove: 3");
//        }
        pausePlayer();
        notifyProgressUpdate(false);
    }

    private void onPlayerIndicatorSeekStop(@NonNull SeekBar seekBar) {
//        if (mMessageHandler.hasMessageID(SHOW_PROGRESS)) {
        mMessageHandler.removeMessages();
        Log.d("SHOW_PROGRESS", "remove: 4");
//        }
        pausePlayer();
        int duration = seekBar.getProgress();
        player.seekTo(duration);
        setTimeVideo(duration);
        notifyProgressUpdate(false);
    }

    private void onVideoPrepared(int videoWidth, int videoHeight) {

        // Adjust the size of the video
        // so it fits on the screen
        float videoProportion = (float) videoWidth / (float) videoHeight;
        int screenWidth = mLinearVideo.getWidth();
        int screenHeight = mLinearVideo.getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;
        ViewGroup.LayoutParams lp = playerView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }
        playerView.setLayoutParams(lp);

        btnPlay.setVisibility(View.VISIBLE);

        mDuration = Math.round(player.getDuration());
        Log.i(TAG, "onVideoPrepared: " + mDuration);
        mHolderTopView.setMax(mDuration);
        setSeekBarPosition();

        setTimeFrames();
        setTimeVideo(mMaxDuration);

        if (mOnHgLVideoListener != null) {
            mOnHgLVideoListener.onVideoPrepared();
        }
    }

    private void setSeekBarPosition() {

        if (mDuration >= mMaxDuration) {
            mStartPosition = 0;
            mEndPosition = mMaxDuration;

            /*mStartPosition = 0;
            mEndPosition = mMaxDuration;*/
            mRangeSeekBarView.setThumbValue(0, (mStartPosition * 100) / mDuration);
            mRangeSeekBarView.setThumbValue(1, (mEndPosition * 100) / mDuration);

        } else {
            mStartPosition = 0;
            mEndPosition = mDuration;
        }

        setProgressBarPosition(mStartPosition);
        player.seekTo(mStartPosition);

        mTimeVideo = mDuration;
        mRangeSeekBarView.initMaxWidth();
    }

    private void setTimeFrames() {
        String seconds = getContext().getString(R.string.short_seconds);
        mTextTimeFrame.setText(String.format("%s %s - %s %s", TrimVideoUtils.stringForTime(mStartPosition), seconds, TrimVideoUtils.stringForTime(mEndPosition), seconds));
    }

    private void setTimeVideo(int position) {
        txtDuration.setText(TrimVideoUtils.stringForTime(position));
    }

    private void onSeekThumbs(int index, float value) {
        switch (index) {
            case Thumb.LEFT: {
                mStartPosition = (int) ((mDuration * value) / 100L);
                player.seekTo(mStartPosition);
                break;
            }
            case Thumb.RIGHT: {
                mEndPosition = (int) ((mDuration * value) / 100L);
                break;
            }
        }
        setProgressBarPosition(mStartPosition);

        setTimeFrames();
        mTimeVideo = mEndPosition - mStartPosition;
        setTimeVideo((int) mTimeVideo);
    }

    private void onStopSeekThumbs(int index, float value) {
//        if (mMessageHandler.hasMessageID(SHOW_PROGRESS)) {
        mMessageHandler.removeMessages();
        Log.d("SHOW_PROGRESS", "remove: 1");
//        }
        pausePlayer();
        /*switch (index) {
            case Thumb.LEFT: {
                mStartPosition = Math.round((mDuration * value) / 100L);
                mVideoView.seekTo(mStartPosition);
                break;
            }
            case Thumb.RIGHT: {
                mEndPosition = Math.round((mDuration * value) / 100L);
                break;
            }
        }
        setProgressBarPosition(mStartPosition);

        setTimeFrames();
        mTimeVideo = mEndPosition - mStartPosition;
        mMessageHandler.removeMessages(SHOW_PROGRESS);
        mVideoView.pause();
        btnPlay.setVisibility(View.VISIBLE);*/
    }

    private void onVideoCompleted() {
        pausePlayer();
        player.seekTo(mStartPosition);
        Log.i(TAG, "onVideoCompleted: ");
        mMessageHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    private void notifyProgressUpdate(boolean all) {
        if (mDuration == 0) return;

        int position = (int) player.getCurrentPosition();
        if (all) {
            for (OnProgressVideoListener item : mListeners) {
                item.updateProgress(position, mDuration, ((position * 100) / mDuration));
            }
        } else {
            mListeners.get(1).updateProgress(position, mDuration, ((position * 100) / mDuration));
        }
    }

    private void updateVideoProgress(int time) {
        if (player == null) {
            return;
        }

        if (time >= mEndPosition) {
            Log.d(TAG, "updateVideoProgress: found it larger than mEndPosition");
//            if (mMessageHandler.hasMessageID(SHOW_PROGRESS)) {
            mMessageHandler.removeMessages();
            Log.d("SHOW_PROGRESS", "remove: 5");
//            }
            mResetSeekBar = true;
            onVideoCompleted();
            return;
        }

        if (mHolderTopView != null) {
            // use long to avoid overflow
            setProgressBarPosition(time);
        }
        setTimeVideo(time);
    }

    private void setProgressBarPosition(int position) {
        if (mDuration > 0) {
            mHolderTopView.setProgress(position);
        }
    }

    /**
     * Set video information visibility.
     * For now this is for debugging
     *
     * @param visible whether or not the videoInformation will be visible
     */
    public void setVideoInformationVisibility(boolean visible) {
        mTimeInfoContainer.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * Listener for events such as trimming operation success and cancel
     *
     * @param onTrimVideoListener interface for events
     */
    @SuppressWarnings("unused")
    public void setOnTrimVideoListener(OnTrimVideoListener onTrimVideoListener) {
        mOnTrimVideoListener = onTrimVideoListener;
    }

    /**
     * Listener for some {@link VideoView} events
     *
     * @param onHgLVideoListener interface for events
     */
    @SuppressWarnings("unused")
    public void setOnHgLVideoListener(OnHgLVideoListener onHgLVideoListener) {
        mOnHgLVideoListener = onHgLVideoListener;
    }

    /**
     * Sets the path where the trimmed video will be saved
     * Ex: /storage/emulated/0/MyAppFolder/
     *
     * @param finalPath the full path
     */
    @SuppressWarnings("unused")
    public void setDestinationPath(final String finalPath) {
        mFinalPath = finalPath;
        Log.d(TAG, "Setting custom path " + mFinalPath);
    }

    /**
     * Cancel all current operations
     */
    public void destroy() {
        BackgroundExecutor.cancelAll("", true);
        UiThreadExecutor.cancelAll("");
    }

    /**
     * Set the maximum duration of the trimmed video.
     * The trimmer interface wont allow the user to set duration longer than maxDuration
     *
     * @param maxDuration the maximum duration of the trimmed video in seconds
     */
    @SuppressWarnings("unused")
    public void setMaxDuration(int maxDuration) {
        if (maxDuration <= 30) {
            mMaxDuration = maxDuration * 1000;
        } else {
            mMaxDuration = 30 * 1000;
        }
    }

    /**
     * Sets the uri of the video to be trimmer
     *
     * @param videoURI Uri of the video
     */
    @SuppressWarnings("unused")
    public void setVideoURI(final Uri videoURI) {
        videoUri = videoURI;

        if (mOriginSizeFile == 0) {
            File file = new File(videoUri.getPath());

            mOriginSizeFile = file.length();
            long fileSizeInKB = mOriginSizeFile / 1024;

            if (fileSizeInKB > 1000) {
                long fileSizeInMB = fileSizeInKB / 1024;
                txtVideoSize.setText(String.format("%s %s", fileSizeInMB, getContext().getString(R.string.megabyte)));
            } else {
                txtVideoSize.setText(String.format("%s %s", fileSizeInKB, getContext().getString(R.string.kilobyte)));
            }
        }

        initExoPlayer();
        mTimeLineView.setVideo(videoUri);
    }

    @Override
    public void onClick(View view) {

    }

    public void stopPlayer() {
        player.stop(true);
    }

    @Override
    public void preparePlayback() {

    }

    private static class MessageHandler extends Handler {

        private int messageID = -1;

        boolean hasMessageID(int id) {
            return messageID == id;
        }

        void removeMessages() {
            super.removeMessages(getMessageID());
            messageID = -1;
        }

        void setMessageID(int messageID) {
            this.messageID = messageID;
        }

        private int getMessageID() {
            return messageID;
        }

        @NonNull
        private final WeakReference<HgLVideoTrimmer> mView;

        MessageHandler(HgLVideoTrimmer view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            HgLVideoTrimmer view = mView.get();
            if (view == null || view.player == null) {
                return;
            }
            view.notifyProgressUpdate(true);
            if (view.player.isPlaying()) {
                sendEmptyMessageDelayed(0, 10);
            }
        }

    }

    public void releasePlayer() {
        pausePlayer();
        if (Util.SDK_INT <= 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            if (player != null) {
                player.release();
                player = null;
            }
        }
    }

    private void pausePlayer() {
        btnPlay.setVisibility(VISIBLE);
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    private void startPlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    private void initMargins() {
        int topMargin = pref.getInt("status_height", 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = topMargin + 5;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        topLay.setLayoutParams(layoutParams);
    }
}
