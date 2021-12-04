package com.app.jussfun.ui;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.app.jussfun.R;
import com.app.jussfun.apprtc.AppRTCAudioManager;
import com.app.jussfun.apprtc.AppRTCClient;
import com.app.jussfun.apprtc.CpuMonitor;
import com.app.jussfun.apprtc.DirectRTCClient;
import com.app.jussfun.apprtc.PeerConnectionClient;
import com.app.jussfun.apprtc.UnhandledExceptionHandler;
import com.app.jussfun.apprtc.WebSocketRTCClient;
import com.app.jussfun.apprtc.util.AppRTCUtils;
import com.app.jussfun.external.shimmer.ShimmerFrameLayout;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.ProfileUpdatedListener;
import com.app.jussfun.model.CommonResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.Gift;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.model.ReportRequest;
import com.app.jussfun.model.ReportResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.NetworkMonitorAutoDetect;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jp.wasabeef.blurry.Blurry;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.app.jussfun.ui.RandomCallActivity.isInCall;

public class RandomCallFragment extends Fragment implements AppRTCClient.SignalingEvents,
        PeerConnectionClient.PeerConnectionEvents, CallControlsFragment.OnCallEvents, ProfileUpdatedListener {
    private static final String TAG = RandomCallFragment.class.getSimpleName();
    private Bundle bundle;
    private String partnerId, roomId;
    private ApiInterface apiInterface;
    private static boolean commandLineRun;
    private int initialY = 0;
    private RandomCallActivity activity;
    private boolean isVideoCall, isCallConnected = false;
    NetworkMonitorAutoDetect autoDetect;
    private String platform;
    private boolean isFrontCamera = true, isBlurred = false, isFindPartnerCalled = false;
    private Bitmap blurredBitMap = null;
    Handler handler = new Handler(Looper.getMainLooper());

    private EglRenderer.FrameListener captureFrameListener = new EglRenderer.FrameListener() {
        @Override
        public void onFrame(Bitmap bitmap) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentRenderer.removeFrameListener(captureFrameListener);
                }
            });

            if (bitmap != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos);
                byte[] bitmapData = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData);
                new ImageUploadTask(bs).execute();
            }
        }
    };
    private EglRenderer.FrameListener pipRendererFrameListener = new EglRenderer.FrameListener() {
        @Override
        public void onFrame(Bitmap bitmap) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentRenderer.removeFrameListener(pipRendererFrameListener);
                }
            });
            if (bitmap != null) {
                blurredBitMap = bitmap;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pipBlurView.setImageBitmap(blurredBitMap);
                        pipBlurView.setAlpha(0.98f);
                        Blurry.with(activity).sampling(5).radius(20).from(blurredBitMap).into(pipBlurView);
                        fullScreenBlurView.setVisibility(View.GONE);
                        pipBlurView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    };
    private EglRenderer.FrameListener fullRendererFrameListener = new EglRenderer.FrameListener() {
        @Override
        public void onFrame(Bitmap bitmap) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentRenderer.removeFrameListener(fullRendererFrameListener);
                }
            });
            if (bitmap != null) {
                blurredBitMap = bitmap;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fullScreenBlurView.setImageBitmap(blurredBitMap);
                        fullScreenBlurView.setAlpha(0.98f);
                        Blurry.with(activity).sampling(5).radius(20).from(blurredBitMap).into(fullScreenBlurView);
                        fullScreenBlurView.setVisibility(View.VISIBLE);
                        pipBlurView.setVisibility(View.GONE);
                    }
                });

            }

        }
    };

    private final long captureTimeDelay = 30000;
    Runnable captureRunnable = new Runnable() {
        @Override
        public void run() {
            if (isCallConnected) {
                if (currentRenderer != null) {
                    currentRenderer.addFrameListener(captureFrameListener, 1);
                }
            }
            handler.postDelayed(captureRunnable, captureTimeDelay);
        }
    };

    private final long connectionWaitingTime = 10000;
    Runnable myRunnable = new Runnable() {
        public void run() {
            // do something
            if (!isCallConnected) {
                if (activity != null) {
                    disconnect();
                    activity.searchAnotherPartner();
                }
                handler.removeCallbacks(myRunnable);
            }
        }
    };

    private static class ProxyVideoSink implements VideoSink {

        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }

    }

    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    @Nullable
    private PeerConnectionClient peerConnectionClient;
    @Nullable
    private AppRTCClient appRtcClient;
    @Nullable
    private AppRTCClient.SignalingParameters signalingParameters;
    @Nullable
    private AppRTCAudioManager audioManager;
    @Nullable
    private SurfaceViewRenderer pipRenderer;
    @Nullable
    private SurfaceViewRenderer fullscreenRenderer;
    @Nullable
    private VideoFileRenderer videoFileRenderer;
    private List<VideoSink> remoteSinks = new ArrayList<>();
    private boolean activityRunning;
    private AppRTCClient.RoomConnectionParameters roomConnectionParameters;
    @Nullable
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private boolean connected;
    private boolean isError;
    private boolean callControlFragmentVisible = true;
    private long callStartedTimeMs;
    private boolean screencaptureEnabled;
    private static Intent mediaProjectionPermissionResultData;
    private static int mediaProjectionPermissionResultCode;
    // True if local view is in the fullscreen renderer.
    private boolean isSwappedFeeds = false;

    private ShimmerFrameLayout shimmerLayout;
    // Controls
    UserBottomFragment userFragment = null;
    CallControlsFragment controlsFragment = null;

    private LottieAnimationView stickerImageBig;
    private ImageView giftImageBig, giftReceiveImage, audioBgImage;
    private NestedScrollView scrollView;
    private LinearLayout bottomLay;
    private RelativeLayout giftDisplayLay, giftReceiveLay;
    private FrameLayout callLay, audioLay;
    private TextView txtGiftName, txtGiftReceive;
    private int displayHeight, displayWidth;
    private static final float OFFSET = 3f;
    private CpuMonitor cpuMonitor;
    @Nullable
    private SurfaceViewRenderer currentRenderer;
    private ImageView fullScreenBlurView, pipBlurView, appLogo;
    private AppUtils appUtils;

    public void removeFragments() {
        if (userFragment != null && controlsFragment != null) {
            if (userFragment.isAdded() && controlsFragment.isAdded()) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                ft.remove(userFragment);
                ft.remove(controlsFragment);
                ft.commitAllowingStateLoss();
                userFragment = null;
                controlsFragment = null;
            }
        }
    }


    void hideControls() {
        if (controlsFragment != null) {
            controlsFragment.hideDialogs();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(activity));
        bundle = getArguments();
        partnerId = bundle.getString(Constants.TAG_PARTNER_ID);
        isVideoCall = bundle.getBoolean(AppRTCUtils.EXTRA_VIDEO_CALL);
        roomId = bundle.getString(AppRTCUtils.EXTRA_ROOMID);
        platform = bundle.getString(Constants.TAG_PLATFORM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_random_call, container, false);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(getActivity());


        initView(rootView);
        if (roomId != null)
            initCall();
        addControlsUI();

        return rootView;
    }

    private void initView(ViewGroup rootView) {
        // Create UI controls.
        pipRenderer = rootView.findViewById(R.id.pip_video_view);
        fullscreenRenderer = rootView.findViewById(R.id.fullscreen_video_view);
        scrollView = rootView.findViewById(R.id.scrollView);
        callLay = rootView.findViewById(R.id.callLay);
        bottomLay = rootView.findViewById(R.id.bottomLay);
        stickerImageBig = rootView.findViewById(R.id.stickerImageBig);
        giftDisplayLay = rootView.findViewById(R.id.giftDisplayLay);
        giftReceiveLay = rootView.findViewById(R.id.giftReceiveLay);
        giftImageBig = rootView.findViewById(R.id.giftImageBig);
        giftReceiveImage = rootView.findViewById(R.id.giftReceiveImage);
        audioBgImage = rootView.findViewById(R.id.audioBgImage);
        shimmerLayout = rootView.findViewById(R.id.shimmerLayout);
        txtGiftName = rootView.findViewById(R.id.txtGiftName);
        txtGiftReceive = rootView.findViewById(R.id.txtGiftReceive);
        fullScreenBlurView = rootView.findViewById(R.id.fullscreenBlurView);
        pipBlurView = rootView.findViewById(R.id.pipBlurView);
        appLogo = rootView.findViewById(R.id.appLogo);
        audioLay = rootView.findViewById(R.id.audioLay);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        if (activity != null) {
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowMetrics windowMetrics = wm.getCurrentWindowMetrics();
                Insets insets = windowMetrics.getWindowInsets()
                        .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
                displayHeight = windowMetrics.getBounds().height() - insets.bottom - insets.top;
                displayWidth = windowMetrics.getBounds().width() - insets.left - insets.right;
            } else {
                wm.getDefaultDisplay().getMetrics(displayMetrics);
                displayHeight = displayMetrics.heightPixels;
                displayWidth = displayMetrics.widthPixels;
            }
        }

        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearParams.width = displayWidth;
        linearParams.height = displayHeight - AppUtils.getNavigationBarHeight(activity);
        callLay.setLayoutParams(linearParams);

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        frameParams.width = displayWidth;
        frameParams.height = displayHeight - AppUtils.getNavigationBarHeight(getContext());
        audioLay.setLayoutParams(frameParams);
//        audioBgImage.setImageDrawable(appUtils.getChatBG(true));

        linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearParams.width = displayWidth;
        linearParams.height = (int) (displayHeight * 0.5);
        bottomLay.setLayoutParams(linearParams);

        FrameLayout.LayoutParams giftParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        giftParams.bottomMargin = (int) (displayHeight * 0.2);
        giftParams.gravity = Gravity.BOTTOM | Gravity.START;
        giftDisplayLay.setLayoutParams(giftParams);

        FrameLayout.LayoutParams giftReceiveParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        giftReceiveParams.bottomMargin = (int) (displayHeight * 0.4);
        giftReceiveParams.gravity = Gravity.BOTTOM | Gravity.START;
        giftReceiveLay.setLayoutParams(giftReceiveParams);

        audioLay.setVisibility(!isVideoCall ? View.VISIBLE : View.GONE);
        appLogo.setVisibility(!isVideoCall ? View.VISIBLE : View.GONE);
        shimmerLayout.setVisibility(!isVideoCall ? View.GONE : View.VISIBLE);

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    // perceive a touch action.

                } else if (event.getActionMasked() == MotionEvent.ACTION_UP ||
                        event.getActionMasked() == MotionEvent.ACTION_SCROLL ||
                        event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    checkIfScrollStopped();
                }

                return false;
            }
        });

        showLoading();
    }

    private void showLoading() {
        removeFragments();
        if (activity != null) {
            activity.showLoading();
        }
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, 0);
            }
        }, 300);
    }

    private void hideLoading() {
        if (activity != null) {
            activity.hideLoading();
        }
    }

    public void connectWithOthers(Bundle bundle) {
        this.bundle = bundle;
        partnerId = bundle.getString(Constants.TAG_PARTNER_ID);
        isVideoCall = bundle.getBoolean(AppRTCUtils.EXTRA_VIDEO_CALL);
        platform = bundle.getString(Constants.TAG_PLATFORM);
        ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        pipRenderer = rootView.findViewById(R.id.pip_video_view);
        fullscreenRenderer = rootView.findViewById(R.id.fullscreen_video_view);
        isCallConnected = false;
        initCall();
        addControlsUI();
    }

    void changeFollowStatus(ProfileResponse profile) {
        if (userFragment != null) {
            userFragment.setFollowStatus(profile);
        }
    }

    @Override
    public void onProfileUpdated(String partnerId) {
        if (activity != null) {
            activity.refreshProfile(partnerId);
        }
    }

    @Override
    public void moveToProfile() {
        if (activity != null) {
            activity.viewPager.setCurrentItem(1);
        }
    }

    public void setContext(RandomCallActivity activity) {
        this.activity = activity;
    }

    private void initCall() {
        connected = false;
        signalingParameters = null;
        remoteSinks = new ArrayList<>();
        userFragment = new UserBottomFragment();
        controlsFragment = new CallControlsFragment();
        controlsFragment.setCallEvents(this);
        // Show/hide call control fragment on view click.
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCallControlFragmentVisibility();
            }
        };

        // Swap feeds on pip view click.
        pipRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentRenderer = currentRenderer == fullscreenRenderer ? pipRenderer : fullscreenRenderer;
                setSwappedFeeds(!isSwappedFeeds);
                if (!isFrontCamera) {
                    fullscreenRenderer.setMirror(false);
                    pipRenderer.setMirror(false);
                } else {
                    setMirror(isSwappedFeeds);
                }
                setSwappedBlurFeeds(isBlurred);
            }
        });

        fullscreenRenderer.setOnClickListener(listener);
        shimmerLayout.setOnClickListener(listener);
        remoteSinks.add(remoteProxyRenderer);

        final EglBase eglBase = EglBase.create();

        // Create video renderers.
        pipRenderer.init(eglBase.getEglBaseContext(), null);
        pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT);
        String saveRemoteVideoToFile = bundle.getString(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);

        // When saveRemoteVideoToFile is set we save the video from the remote to a file.
        if (saveRemoteVideoToFile != null) {
            int videoOutWidth = bundle.getInt(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
            int videoOutHeight = bundle.getInt(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
            try {
                videoFileRenderer = new VideoFileRenderer(
                        saveRemoteVideoToFile, videoOutWidth, videoOutHeight, eglBase.getEglBaseContext());
                remoteSinks.add(videoFileRenderer);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to open video file for output: " + saveRemoteVideoToFile, e);
            }
        }
        fullscreenRenderer.init(eglBase.getEglBaseContext(), null);
        fullscreenRenderer.setScalingType(ScalingType.SCALE_ASPECT_FILL);

        pipRenderer.setZOrderMediaOverlay(true);
        pipRenderer.setEnableHardwareScaler(true /* enabled */);
        fullscreenRenderer.setEnableHardwareScaler(false /* enabled */);
        // Start with local feed in fullscreen and swap it to the pip when the call is connected.
        setSwappedFeeds(true /* isSwappedFeeds */);

        Uri roomUri = Uri.parse(bundle.getString(AppRTCUtils.EXTRA_ROOM_URI));
        if (roomUri == null) {
            logAndToast(getString(R.string.missing_url));
            activity.setResult(RESULT_CANCELED);
            activity.finish();
            return;
        }

        // Get Intent parameters.
        roomId = bundle.getString(AppRTCUtils.EXTRA_ROOMID);
        Log.d(TAG, "Room ID: " + roomId);
        if (roomId == null || roomId.length() == 0) {
            logAndToast(getString(R.string.missing_url));
            Log.e(TAG, "Incorrect room ID in intent!");
            activity.setResult(RESULT_CANCELED);
            activity.finish();
            return;
        }

        boolean loopback = bundle.getBoolean(AppRTCUtils.EXTRA_LOOPBACK, false);
        boolean tracing = bundle.getBoolean(AppRTCUtils.EXTRA_TRACING, false);

        int videoWidth = bundle.getInt(AppRTCUtils.EXTRA_VIDEO_WIDTH, 0);
        int videoHeight = bundle.getInt(AppRTCUtils.EXTRA_VIDEO_HEIGHT, 0);

        screencaptureEnabled = bundle.getBoolean(AppRTCUtils.EXTRA_SCREENCAPTURE, false);
        // If capturing format is not specified for screencapture, use screen resolution.
        if (screencaptureEnabled && videoWidth == 0 && videoHeight == 0) {
            DisplayMetrics displayMetrics = getDisplayMetrics();
            videoWidth = displayMetrics.widthPixels;
            videoHeight = displayMetrics.heightPixels;
        }
        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;
        if (bundle.getBoolean(AppRTCUtils.EXTRA_DATA_CHANNEL_ENABLED, false)) {
            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(bundle.getBoolean(AppRTCUtils.EXTRA_ORDERED, true),
                    bundle.getInt(AppRTCUtils.EXTRA_MAX_RETRANSMITS_MS, -1),
                    bundle.getInt(AppRTCUtils.EXTRA_MAX_RETRANSMITS, -1), bundle.getString(AppRTCUtils.EXTRA_PROTOCOL),
                    bundle.getBoolean(AppRTCUtils.EXTRA_NEGOTIATED, false), bundle.getInt(AppRTCUtils.EXTRA_ID, -1));
        }
        peerConnectionParameters =
                new PeerConnectionClient.PeerConnectionParameters(bundle.getBoolean(AppRTCUtils.EXTRA_VIDEO_CALL, true), loopback,
                        tracing, videoWidth, videoHeight, bundle.getInt(AppRTCUtils.EXTRA_VIDEO_FPS, 0),
                        bundle.getInt(AppRTCUtils.EXTRA_VIDEO_BITRATE, 0), bundle.getString(AppRTCUtils.EXTRA_VIDEOCODEC),
                        bundle.getBoolean(AppRTCUtils.EXTRA_HWCODEC_ENABLED, true),
                        bundle.getBoolean(AppRTCUtils.EXTRA_FLEXFEC_ENABLED, false),
                        bundle.getInt(AppRTCUtils.EXTRA_AUDIO_BITRATE, 0), bundle.getString(AppRTCUtils.EXTRA_AUDIOCODEC),
                        bundle.getBoolean(AppRTCUtils.EXTRA_NOAUDIOPROCESSING_ENABLED, false),
                        bundle.getBoolean(AppRTCUtils.EXTRA_AECDUMP_ENABLED, false),
                        bundle.getBoolean(AppRTCUtils.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, true),
                        bundle.getBoolean(AppRTCUtils.EXTRA_OPENSLES_ENABLED, false),
                        bundle.getBoolean(AppRTCUtils.EXTRA_DISABLE_BUILT_IN_AEC, false),
                        bundle.getBoolean(AppRTCUtils.EXTRA_DISABLE_BUILT_IN_AGC, false),
                        bundle.getBoolean(AppRTCUtils.EXTRA_DISABLE_BUILT_IN_NS, false),
                        bundle.getBoolean(AppRTCUtils.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, false),
                        bundle.getBoolean(AppRTCUtils.EXTRA_ENABLE_RTCEVENTLOG, false), dataChannelParameters);
        commandLineRun = bundle.getBoolean(AppRTCUtils.EXTRA_CMDLINE, false);
        int runTimeMs = bundle.getInt(AppRTCUtils.EXTRA_RUNTIME, 0);

        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
        // standard WebSocketRTCClient.
        if (loopback || !DirectRTCClient.IP_PATTERN.matcher(roomId).matches()) {
            appRtcClient = new WebSocketRTCClient(RandomCallFragment.this);
        } else {
            Log.i(TAG, "Using DirectRTCClient because room name looks like an IP.");
            appRtcClient = new DirectRTCClient(this);
        }
        // Create connection parameters.
        String urlParameters = bundle.getString(AppRTCUtils.EXTRA_URLPARAMETERS);
        roomConnectionParameters =
                new AppRTCClient.RoomConnectionParameters(roomUri.toString(), roomId, loopback, urlParameters);

        // For command line execution run connection for <runTimeMs> and exit.
        if (commandLineRun && runTimeMs > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            }, runTimeMs);
        }
//        checkAvailableCodec(eglBase);
        // Create peer connection client.
        peerConnectionClient = new PeerConnectionClient(
                activity, eglBase, peerConnectionParameters, this);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        if (loopback) {
            options.networkIgnoreMask = 0;
        }
        handler.post(() -> {
            peerConnectionClient.createPeerConnectionFactory(options);
        });

        if (screencaptureEnabled) {
            startScreenCapture();
        } else {
            startCall();
        }
    }

    private void checkAvailableCodec(final EglBase eglBase) {
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(activity != null ? activity : getActivity())
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        VideoEncoderFactory videoEncoderFactory =
                new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), true, true);

        for (int i = 0; i < videoEncoderFactory.getSupportedCodecs().length; i++) {
            Log.d(TAG, "Supported codecs: " + videoEncoderFactory.getSupportedCodecs()[i].name);
        }
    }

    private void setMirror(boolean isSwappedFeeds) {
        if (isSwappedFeeds) {
            if (fullscreenRenderer != null)
                fullscreenRenderer.setMirror(true);
            if (pipRenderer != null)
                pipRenderer.setMirror(false);
        } else {
            if (fullscreenRenderer != null)
                fullscreenRenderer.setMirror(false);
            if (pipRenderer != null)
                pipRenderer.setMirror(true);
        }
    }

    private void addControlsUI() {
        Bundle controlBundle = new Bundle();
        controlBundle.putString(AppRTCUtils.EXTRA_ROOM_URI, bundle.getString(AppRTCUtils.EXTRA_ROOM_URI));
        controlBundle.putBoolean(AppRTCUtils.EXTRA_VIDEO_CALL, bundle.getBoolean(AppRTCUtils.EXTRA_VIDEO_CALL));
        controlBundle.putBoolean(AppRTCUtils.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
                bundle.getBoolean(AppRTCUtils.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED));
        controlsFragment.setArguments(controlBundle);

        // Activate call and HUD fragments and start the call.

        Bundle userBundle = new Bundle();
        userBundle.putString(Constants.TAG_PARTNER_ID, bundle.getString(Constants.TAG_PARTNER_ID));
        userBundle.putString(Constants.TAG_NAME, bundle.getString(Constants.TAG_NAME));
        userBundle.putString(Constants.TAG_USER_IMAGE, bundle.getString(Constants.TAG_USER_IMAGE));
        userBundle.putString(Constants.TAG_AGE, bundle.getString(Constants.TAG_AGE));
        userBundle.putString(Constants.TAG_DOB, bundle.getString(Constants.TAG_DOB));
        userBundle.putString(Constants.TAG_GENDER, bundle.getString(Constants.TAG_GENDER));
        userBundle.putString(Constants.TAG_PREMIUM_MEBER, bundle.getString(Constants.TAG_PREMIUM_MEBER, Constants.TAG_FALSE));
        userBundle.putString(Constants.TAG_LOCATION, bundle.getString(Constants.TAG_LOCATION));
        userBundle.putString(Constants.TAG_FOLLOWERS, bundle.getString(Constants.TAG_FOLLOWERS));
        userBundle.putString(Constants.TAG_FOLLOWINGS, bundle.getString(Constants.TAG_FOLLOWINGS));
        userBundle.putString(Constants.TAG_FOLLOW, bundle.getString(Constants.TAG_FOLLOW, Constants.TAG_FALSE));
        userBundle.putString(Constants.TAG_PRIVACY_AGE, bundle.getString(Constants.TAG_PRIVACY_AGE, Constants.TAG_FALSE));
        userBundle.putString(Constants.TAG_PRIVACY_CONTACT_ME, bundle.getString(Constants.TAG_PRIVACY_CONTACT_ME, Constants.TAG_FALSE));
        userBundle.putBoolean(Constants.TAG_INTEREST_ON_YOU, bundle.getBoolean(Constants.TAG_INTEREST_ON_YOU, false));
        userBundle.putBoolean(Constants.TAG_INTERESTED_BY_ME, bundle.getBoolean(Constants.TAG_INTERESTED_BY_ME, false));
        userBundle.putBoolean(Constants.TAG_FRIEND, bundle.getBoolean(Constants.TAG_FRIEND, false));
        userBundle.putBoolean(Constants.TAG_DECLINED, bundle.getBoolean(Constants.TAG_DECLINED, false));
        userBundle.putString(Constants.TAG_CALL_TYPE, isVideoCall ? Constants.TAG_VIDEO : Constants.TAG_AUDIO);
        userBundle.putString(Constants.TAG_FROM, isVideoCall ? Constants.TAG_VIDEO : Constants.TAG_AUDIO);
        userFragment.setArguments(userBundle);
        userFragment.setProfileUpdatedListner(RandomCallFragment.this);

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.add(R.id.call_controls_container, controlsFragment);
        ft.commitAllowingStateLoss();
    }

    private void checkIfScrollStopped() {
        initialY = scrollView.getScrollY();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                int updatedY = scrollView.getScrollY();
                if (updatedY == initialY) {
                    //we've stopped
                    if (scrollView.getScrollY() >= displayHeight / OFFSET) {
                        scrollView.scrollTo(0, displayHeight);
                        showLoading();
                        activity.breakPartner(partnerId);
                        searchPartner();
                    } else {
                        scrollView.scrollTo(0, 0);
                    }
                } else {
                    initialY = updatedY;
                    checkIfScrollStopped();
                }
            }
        }, 50);
    }

    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        return flags;
    }

    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) activity.getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), AppRTCUtils.CAPTURE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != AppRTCUtils.CAPTURE_PERMISSION_REQUEST_CODE)
            return;
        mediaProjectionPermissionResultCode = resultCode;
        mediaProjectionPermissionResultData = data;
        startCall();
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(activity) && bundle.getBoolean(AppRTCUtils.EXTRA_CAMERA2, true);
    }

    private boolean captureToTexture() {
        return bundle.getBoolean(AppRTCUtils.EXTRA_CAPTURETOTEXTURE_ENABLED, false);
    }

    private @Nullable
    VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    @TargetApi(21)
    private @Nullable
    VideoCapturer createScreenCapturer() {
        if (mediaProjectionPermissionResultCode != RESULT_OK) {
            reportError("User didn'timer give permission to capture the screen.");
            return null;
        }
        return new ScreenCapturerAndroid(
                mediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                reportError("User revoked permission to capture the screen.");
            }
        });
    }

    // Activity interfaces
    @Override
    public void onStop() {
        super.onStop();
        activityRunning = false;
        // Don'timer stop the video when using screencapture to allow user to show other apps to the remote
        // end.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.stopVideoSource();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        activityRunning = true;
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.startVideoSource();
        }
    }

    @Override
    public void onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null);
        disconnect();
        activityRunning = false;
        changeLiveStatus(Constants.INSEARCH);
        super.onDestroy();
    }

    private void changeLiveStatus(String liveStatus) {
        if (activity != null) {
            activity.changeLiveStatus(liveStatus);
        }
        AppUtils.setCurrentStatus(liveStatus);
    }

    // CallFragment.OnCallEvents interface implementation.
    @Override
    public void onBlurClicked(boolean isBlurred) {
        this.isBlurred = isBlurred;
        if (activity != null) {
            activity.onBlurVideoCall(isBlurred);
        }
    }

    // CallFragment.OnCallEvents interface implementation.
    @Override
    public void onCallHangUp() {
        disconnect();
        if (activity != null) {
            if (NetworkReceiver.isConnected() && partnerId != null && isInCall) {
                activity.breakPartner(partnerId);
            }
            activity.unRevealActivity();
//            activity.finish();
        }
    }

    public void resetRoom() {
        partnerId = null;
        roomId = null;
    }

    @Override
    public void onCameraSwitch() {
        if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            switchCamera();
        } else {
            if (("" + AdminData.filterOptions.getbCamera().getNonPrime()).equals("1")) {
                switchCamera();
            } else {
                App.makeToast(getString(R.string.upgrade_to_premium_to_access));
            }
        }
    }

    private void switchCamera() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
            isFrontCamera = !isFrontCamera;
            if (!isFrontCamera) {
                fullscreenRenderer.setMirror(false);
                pipRenderer.setMirror(false);
            } else {
                setMirror(false);
            }
        }
    }

    @Override
    public boolean onToggleMic(boolean isMicEnabled) {
        if (peerConnectionClient != null) {
            peerConnectionClient.setAudioEnabled(isMicEnabled);
        }
        return isMicEnabled;
    }

    @Override
    public void onStickerSend(String sticker) {
        if (activity != null) {
            activity.sendSticker(sticker, partnerId);
            final Animation myAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_sticker_send);
            displaySticker(sticker, myAnim, false);
        }

    }

    @Override
    public void onGiftSend(Gift gift) {
        if (activity != null) {
            activity.sendGift(gift, partnerId);
            String desc = getString(R.string.send_gift_desc) + "\n" + "\"" + gift.getGiftTitle() + "\" " + getString(R.string.gift);
            displayGift(gift, desc);
        }
    }

    @Override
    public void onReportSend(String reportMessage) {
        ReportRequest request = new ReportRequest();
        request.setReport(reportMessage);
        request.setReportBy(GetSet.getUserId());
        request.setUserId(partnerId);
        request.setReportType(isVideoCall ? Constants.TAG_VIDEO : Constants.TAG_AUDIO);
        Call<ReportResponse> call = apiInterface.reportUser(request);
        call.enqueue(new Callback<ReportResponse>() {
            @Override
            public void onResponse(Call<ReportResponse> call, Response<ReportResponse> response) {
                ReportResponse reportResponse = response.body();
                if (reportResponse.getStatus().equals(Constants.TAG_TRUE)) {
//                    activity.showLoading();
                    connected = false;
                    EglRenderer.FrameListener currentFrameListener = null;
                    if (reportResponse.getMessage().equals(Constants.TAG_REPORT_SUCCESS)) {
                        App.makeToast(getString(R.string.reported_successfully));
//                        frameListener.reportId = reportResponse.getReportId();
                        if (isVideoCall) {
                            if (currentRenderer != null) {
                                EglRenderer.FrameListener finalCurrentFrameListener = currentFrameListener;
                                currentFrameListener = new EglRenderer.FrameListener() {
                                    @Override
                                    public void onFrame(Bitmap bitmap) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (fullscreenRenderer != null && finalCurrentFrameListener != null) {
                                                    currentRenderer.removeFrameListener(finalCurrentFrameListener);
                                                }
                                            }
                                        });
                                        takeReportScreenShot(reportResponse.getReportId(), bitmap);
                                    }
                                };
                                currentRenderer.addFrameListener(currentFrameListener, 1);
                            }
                        } else {
                            closeChat();
                        }
                    } else {
                        App.makeToast(reportResponse.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ReportResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void takeReportScreenShot(String reportId, Bitmap bitmap) {
        if (bitmap != null) {
            Log.i(TAG, "takeScreenShot: ");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90 /*ignored for PNG*/, bos);
            byte[] bitmapData = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData);
            uploadReportImage(reportId, bs);
            closeChat();
        } else {
            closeChat();
        }
    }

    public void closeChat() {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    searchPartner();
                }
            });
        }
    }

    private void uploadReportImage(String reportId, ByteArrayInputStream bs) {
        try {
            com.app.jussfun.utils.Logging.i(TAG, "uploadReportImage: " + reportId);
            RequestBody requestFile = RequestBody.create(AppUtils.getBytes(bs), MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_REPORT_IMAGE, "image.jpg", requestFile);
            RequestBody reportBody = RequestBody.create(reportId, MediaType.parse("multipart/form-data"));
            Call<CommonResponse> call3 = apiInterface.reportScreenUpload(body, reportBody);
            call3.enqueue(new Callback<CommonResponse>() {
                @Override
                public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {

                }

                @Override
                public void onFailure(Call<CommonResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadScreenShot(ByteArrayInputStream bs) {
        try {
            RequestBody requestFile = RequestBody.create(AppUtils.getBytes(bs), MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_VIDEO_CHAT_IMAGE, "image.jpg", requestFile);
            RequestBody reportBody = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
            RequestBody partnerReqBody = RequestBody.create(partnerId, MediaType.parse("multipart/form-data"));
            Call<CommonResponse> call3 = apiInterface.chatScreenUpload(body, reportBody, partnerReqBody);
            call3.enqueue(new Callback<CommonResponse>() {
                @Override
                public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                    if (response.body().getStatus().equals(Constants.TAG_REJECTED)) {
                        closeChat();
                    }
                }

                @Override
                public void onFailure(Call<CommonResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displaySticker(String sticker, Animation myAnim, boolean showDialog) {
        if (controlsFragment != null && !showDialog) {
            controlsFragment.dismissGiftDialog();
        }
        if (stickerImageBig.isAnimating()) {
            stickerImageBig.pauseAnimation();
        }
        stickerImageBig.setVisibility(View.VISIBLE);

        stickerImageBig.startAnimation(myAnim);
        stickerImageBig.setAnimation(sticker + ".json");
        stickerImageBig.setImageAssetsFolder(AppUtils.SMILEY_PATH + sticker);
        stickerImageBig.setRepeatCount(1);
        stickerImageBig.playAnimation();
        stickerImageBig.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                AppUtils.startAlphaAnimation(stickerImageBig, View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    public void displayGift(Gift gift, String message) {

        if (controlsFragment != null) {
            controlsFragment.dismissGiftDialog();
        }
        txtGiftName.setText(message);

        if (!LocaleManager.isRTL()) {
            txtGiftName.setBackground(activity.getDrawable(R.drawable.gift_send_bg));
            Animation slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_slide_right_in);
            Animation slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_slide_left_out);
            Glide.with(activity)
                    .load(Constants.GIFT_IMAGE_URL + gift.getGiftIcon())
                    .apply(new RequestOptions().error(R.drawable.gift).placeholder(R.drawable.gift))
                    .into(giftImageBig);
            giftDisplayLay.setVisibility(View.VISIBLE);
            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            slideOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    giftDisplayLay.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            giftDisplayLay.startAnimation(slideOut);
                        }
                    }, 3000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            giftDisplayLay.startAnimation(slideIn);
        } else {
            txtGiftName.setBackground(activity.getDrawable(R.drawable.gift_send_rtl_bg));
            giftImageBig.setTranslationX(AppUtils.dpToPx(activity, 20));
            Glide.with(activity)
                    .load(Constants.GIFT_IMAGE_URL + gift.getGiftIcon())
                    .apply(new RequestOptions().error(R.drawable.gift).placeholder(R.drawable.gift))
                    .into(giftImageBig);
            giftDisplayLay.setVisibility(View.VISIBLE);

            Animation slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_rtl_right_in);
            Animation slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_rtl_right_out);

            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            slideOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    giftDisplayLay.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            giftDisplayLay.startAnimation(slideOut);
                        }
                    }, 3000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            giftDisplayLay.startAnimation(slideIn);
        }
    }

    public void displayReceiveGift(Gift gift, String message) {
        txtGiftReceive.setText(message);
        if (!LocaleManager.isRTL()) {
            txtGiftReceive.setBackground(activity.getDrawable(R.drawable.gift_send_bg));
            Animation slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_slide_right_in);
            Animation slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_slide_left_out);
            Glide.with(activity)
                    .load(Constants.GIFT_IMAGE_URL + gift.getGiftIcon())
                    .apply(new RequestOptions().error(R.drawable.gift).placeholder(R.drawable.gift))
                    .into(giftReceiveImage);
            giftReceiveLay.setVisibility(View.VISIBLE);
            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            slideOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    giftReceiveLay.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            giftReceiveLay.startAnimation(slideOut);
                        }
                    }, 3000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            giftReceiveLay.startAnimation(slideIn);
        } else {
            giftReceiveImage.setTranslationX(AppUtils.dpToPx(activity, 20));
            Glide.with(activity)
                    .load(Constants.GIFT_IMAGE_URL + gift.getGiftIcon())
                    .apply(new RequestOptions().error(R.drawable.gift).placeholder(R.drawable.gift))
                    .into(giftReceiveImage);
            giftReceiveLay.setVisibility(View.VISIBLE);
            txtGiftReceive.setBackground(activity.getDrawable(R.drawable.gift_send_rtl_bg));
            Animation slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_rtl_right_in);
            Animation slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_rtl_right_out);
            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            slideOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    giftReceiveLay.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            giftReceiveLay.startAnimation(slideOut);
                        }
                    }, 3000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            giftReceiveLay.startAnimation(slideIn);
        }
    }

    public void disableVideo(boolean isBlurred) {
        this.isBlurred = isBlurred;
        if (isBlurred) {
            if (remoteProxyRenderer.target != null && remoteProxyRenderer.target == fullscreenRenderer) {
                fullscreenRenderer.addFrameListener(fullRendererFrameListener, 1f);
            } else if (remoteProxyRenderer.target != null && remoteProxyRenderer.target == pipRenderer) {
                pipRenderer.addFrameListener(pipRendererFrameListener, 1f);
            }
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pipBlurView.setVisibility(View.GONE);
                            fullScreenBlurView.setVisibility(View.GONE);
                        }
                    });
                }
            });
        }
    }

    public void setSwappedBlurFeeds(boolean isBlurred) {
        this.isBlurred = isBlurred;
        if (isBlurred) {
            if (remoteProxyRenderer.target == fullscreenRenderer) {
                if (blurredBitMap != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fullScreenBlurView.setVisibility(View.VISIBLE);
                            pipBlurView.setVisibility(View.GONE);
                            fullScreenBlurView.setImageBitmap(blurredBitMap);
                        }
                    });
                }
            } else if (remoteProxyRenderer.target == pipRenderer) {
                if (blurredBitMap != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fullScreenBlurView.setVisibility(View.GONE);
                            pipBlurView.setVisibility(View.VISIBLE);
                            pipBlurView.setImageBitmap(blurredBitMap);
                        }
                    });
                }
            }
        } else {
            fullScreenBlurView.setVisibility(View.GONE);
            pipBlurView.setVisibility(View.GONE);
        }
    }

    // Helper functions.
    private void toggleCallControlFragmentVisibility() {
        if (!connected || !controlsFragment.isAdded()) {
            return;
        }
        if (shimmerLayout.getVisibility() == View.VISIBLE) {
            shimmerLayout.setVisibility(View.GONE);
            shimmerLayout.stopShimmer();
        }
        // Show/hide call control fragment
        callControlFragmentVisible = !callControlFragmentVisible;
        FragmentTransaction callTransaction = getChildFragmentManager().beginTransaction();
        FragmentTransaction userTransaction = getChildFragmentManager().beginTransaction();

        if (LocaleManager.isRTL()) {
            callTransaction.setCustomAnimations(R.anim.anim_slide_right_in, R.anim.anim_slide_left_out);
        } else {
            callTransaction.setCustomAnimations(R.anim.anim_slide_left_in, R.anim.anim_slide_right_out);
        }
        userTransaction.setCustomAnimations(R.anim.anim_slide_up, R.anim.anim_slide_down);
        if (callControlFragmentVisible) {
            callTransaction.show(controlsFragment);
            userTransaction.show(userFragment);
        } else {
            callTransaction.hide(controlsFragment);
            userTransaction.hide(userFragment);
        }
        callTransaction.commitAllowingStateLoss();
        userTransaction.commitAllowingStateLoss();
    }

    private void startCall() {
        if (appRtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }
        callStartedTimeMs = System.currentTimeMillis();

        // Start room connection.
        logAndToast(getString(R.string.connecting_to, roomConnectionParameters.roomUrl));
        appRtcClient.connectToRoom(roomConnectionParameters);

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(activity);
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio
// devices has changed.
            @Override
            public void onAudioDeviceChanged(
                    AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
            }
        });
    }

    // This method is called when the audio manager reports audio device change,
// e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    public void disconnect() {
        isCallConnected = false;
        handler.removeCallbacks(captureRunnable);
        connected = false;
        roomId = null;
        activityRunning = false;
        remoteProxyRenderer.setTarget(null);
        localProxyVideoSink.setTarget(null);
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        if (pipRenderer != null) {
            pipRenderer.release();
            pipRenderer = null;
        }
        if (videoFileRenderer != null) {
            videoFileRenderer.release();
            videoFileRenderer = null;
        }
        if (fullscreenRenderer != null) {
            fullscreenRenderer.release();
            fullscreenRenderer = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        if (commandLineRun || !activityRunning) {
            Log.e(TAG, "Critical error: " + errorMessage);
//            App.makeToast(getString(R.string.partner_left_chat));
            searchPartner();
        } else {
            if (errorMessage.contains(AppRTCUtils.ROOM_FULL_MESSAGE) ||
                    errorMessage.contains(AppRTCUtils.PEERCONNECTION_ERROR_MESSAGE) ||
                    errorMessage.contains(AppRTCUtils.ROOM_IO_ERROR) ||
                    errorMessage.contains(AppRTCUtils.GAE_POST_ERROR) ||
                    errorMessage.contains(AppRTCUtils.ROOM_PARSING_ERROR)) {
//                App.makeToast(getString(R.string.partner_left_chat));
                searchPartner();
            } else {
                searchPartner();
            }
        }
    }

    // Log |msg| and Toast about it.
    private void logAndToast(String msg) {
        com.app.jussfun.utils.Logging.i(TAG, "logAndToast:" + msg);
        /*if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
        logToast.show();*/
    }

    private void reportError(final String description) {
        Logging.e(TAG, "reportError: " + description);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    isError = true;
                    disconnectWithErrorMessage(description);
                }
            }
        });
    }

    private @Nullable
    VideoCapturer createVideoCapturer() {
        final VideoCapturer videoCapturer;
        String videoFileAsCamera = bundle.getString(AppRTCUtils.EXTRA_VIDEO_FILE_AS_CAMERA);
        if (videoFileAsCamera != null) {
            try {
                videoCapturer = new FileVideoCapturer(videoFileAsCamera);
            } catch (IOException e) {
                reportError("Failed to open video file for emulated camera");
                return null;
            }
        } else if (screencaptureEnabled) {
            return createScreenCapturer();
        } else if (useCamera2()) {
            if (!captureToTexture()) {
                reportError(getString(R.string.camera2_texture_only_error));
                return null;
            }

            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(activity));
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            reportError("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyVideoSink.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
        /*if (currentRenderer != null) {
            if (currentRenderer == fullscreenRenderer) {
                localProxyVideoSink.setTarget(fullscreenRenderer);
                remoteProxyRenderer.setTarget(pipRenderer);
            } else {
                localProxyVideoSink.setTarget(pipRenderer);
                remoteProxyRenderer.setTarget(fullscreenRenderer);
            }
        } else {
            localProxyVideoSink.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
            remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
        }*/
    }

    private void updateVideoViews(final boolean remoteVisible) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pipRenderer != null) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) pipRenderer.getLayoutParams();
                    ViewGroup.MarginLayoutParams tempParams = (ViewGroup.MarginLayoutParams) pipRenderer.getLayoutParams();
                    if (remoteVisible && params != null) {
                        params.width = getDisplayMetrics().widthPixels * 30 / 100;
                        params.height = getDisplayMetrics().heightPixels * 18 / 100;
                        tempParams.width = (getDisplayMetrics().widthPixels * 30 / 100) - 10;
                        tempParams.height = (getDisplayMetrics().heightPixels * 20 / 100) - 10;
                        pipRenderer.setLayoutParams(params);
                        pipBlurView.setLayoutParams(tempParams);
                    }
                }
            }

        });

    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
// All callbacks are invoked from websocket signaling looper thread and
// are routed to UI thread.
    private void onConnectedToRoomInternal(final AppRTCClient.SignalingParameters params) {
        handler.postDelayed(myRunnable, connectionWaitingTime);
        if (activity != null)
            activity.setRoomId("" + roomId);
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        signalingParameters = params;
        logAndToast("Creating peer connection, delay=" + delta + "ms");
        VideoCapturer videoCapturer = null;
        if (peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createVideoCapturer();
        }
        peerConnectionClient.createPeerConnection(
                localProxyVideoSink, remoteSinks, videoCapturer, signalingParameters);

        if (signalingParameters.initiator) {
            logAndToast("Creating OFFER...");
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                logAndToast("Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
    }

    @Override
    public void onConnectedToRoom(final AppRTCClient.SignalingParameters params) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onConnectedToRoomInternal(params);
                }
            });
        }
    }

    @Override
    public void onRemoteDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
                peerConnectionClient.setRemoteDescription(sdp);
                if (!signalingParameters.initiator) {
                    logAndToast("Creating ANSWER...");
                    // Create answer. Answer SDP will be sent to offering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createAnswer();
                }
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.removeRemoteIceCandidates(candidates);
            }
        });
    }

    @Override
    public void onChannelClose() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("Remote end hung up; dropping PeerConnection");
                disconnect();
            }
        });
    }

    @Override
    public void onChannelError(final String description) {
        reportError(description);
    }


    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
// Send local peer connection SDP and ICE candidates to remote party.
// All callbacks are invoked from peer connection client looper thread and
// are routed to UI thread.
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms");
                    if (signalingParameters.initiator) {
                        appRtcClient.sendOfferSdp(sdp);
                    } else {
                        appRtcClient.sendAnswerSdp(sdp);
                    }
                }
                if (peerConnectionParameters.videoMaxBitrate > 0) {
                    Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                    peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                }
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidate(candidate);
                }
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidateRemovals(candidates);
                }
            }
        });
    }

    @Override
    public void onIceConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onIceDisconnected() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onIceClosed() {

    }

    @Override
    public void onConnected() {
        if (isInCall) {
            final long delta = System.currentTimeMillis() - callStartedTimeMs;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connected = true;
                    updateVideoViews(true);
                    changeLiveStatus(Constants.INRANDOMCALL);
                    FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                    ft.add(R.id.user_fragment_container, userFragment);
                    ft.commitAllowingStateLoss();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (shimmerLayout.getVisibility() == View.VISIBLE) {
                                shimmerLayout.setVisibility(View.GONE);
                                shimmerLayout.stopShimmer();
                            }
                        }
                    }, 4000);
                    callConnected();
                }
            });
        }
    }

    // Should be called from UI thread
    private void callConnected() {
        scrollView.scrollTo(0, 0);
        isCallConnected = true;
        handler.removeCallbacks(myRunnable);
        changeLiveStatus(Constants.INRANDOMCALL);
        AppUtils.callerList.add(partnerId);
        hideLoading();
        if (activity != null) {
            activity.updateFilterCharge();
            activity.resetSearchCount();
            activity.updateHistory(partnerId);
            activity.checkSlowNetwork();
        }

        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        com.app.jussfun.utils.Logging.i(TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, AppRTCUtils.STAT_CALLBACK_PERIOD);
        currentRenderer = pipRenderer;
        setSwappedFeeds(false /* isSwappedFeeds */);
        setMirror(false);
        if (isVideoCall) {
            if (AdminData.hasImageModerate) {
                handler.postDelayed(captureRunnable, captureTimeDelay);
            }
        } else {
            handler.removeCallbacks(captureRunnable);
        }
    }

    @Override
    public void onDisconnected() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (platform != null && platform.equals(Constants.TAG_ANDROID))
                    searchPartner();
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError && connected) {

                }
            }
        });
    }

    @Override
    public void onPeerConnectionError(final String description) {
        reportError(description);
    }

    @Override
    public void onClosed() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (platform != null && platform.equals("ios")) {
                    searchPartner();
                }
            }
        });
    }

    private void searchPartner() {
        showLoading();
        connected = false;
        disconnect();
        removeFragments();
        if (activity != null) {
            activity.searchAnotherPartner();
            resetRoom();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ImageUploadTask extends AsyncTask<Void, Void, Void> {
        ByteArrayInputStream bs;

        public ImageUploadTask(ByteArrayInputStream bs) {
            this.bs = bs;

        }

        @Override
        protected Void doInBackground(Void... voids) {
            uploadScreenShot(bs);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
