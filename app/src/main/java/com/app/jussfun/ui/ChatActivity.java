package com.app.jussfun.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.base.App;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.app.jussfun.BuildConfig;
import com.app.jussfun.R;
import com.app.jussfun.apprtc.util.AppRTCUtils;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.external.CustomTypefaceSpan;
import com.app.jussfun.external.EndlessRecyclerOnScrollListener;
import com.app.jussfun.external.ImagePicker;
import com.app.jussfun.external.MediaPlayerUtils;
import com.app.jussfun.external.ProgressWheel;
import com.app.jussfun.helper.AppWebSocket;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.livedata.MessageLiveModel;
import com.app.jussfun.model.AdminMessageResponse;
import com.app.jussfun.model.ChatResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.FileUtil;
import com.app.jussfun.utils.Logging;
import com.app.jussfun.utils.SharedPref;
import com.makeramen.roundedimageview.RoundedImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

/*
import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.GiphyCoreUI;
import com.giphy.sdk.ui.themes.GridType;
import com.giphy.sdk.ui.themes.LightTheme;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.gson.Gson;
import com.google.mlkit.nl.smartreply.SmartReply;
import com.google.mlkit.nl.smartreply.SmartReplyGenerator;
import com.google.mlkit.nl.smartreply.SmartReplySuggestion;
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult;
import com.google.mlkit.nl.smartreply.TextMessage;*/


public class ChatActivity extends BaseFragmentActivity implements AppWebSocket.WebSocketChannelEvents, AdapterView.OnItemClickListener
        /*,GiphyDialogFragment.GifSelectionListener , ReplyChipAdapter.ClickListener, MediaPlayerUtils.Listener*/ {
    private static final int REQUEST_CODE_PICK_IMAGE = 100;

    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    public String partnerId = "";
    public String chatId;
    ApiInterface apiInterface;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnVideoCall)
    ImageView btnVideoCall;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btnGallery)
    ImageView btnGallery;
    @BindView(R.id.btnCamera)
    ImageView btnCamera;
    @BindView(R.id.attachLay)
    LinearLayout attachLay;
    @BindView(R.id.btnAdd)
    ImageView btnAdd;
    @BindView(R.id.edtMessage)
    EditText edtMessage;
    @BindView(R.id.btnSend)
    ImageView btnSend;
    @BindView(R.id.edtLay)
    RelativeLayout edtLay;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.btnMenu)
    ImageView btnMenu;
    @BindView(R.id.txtTyping)
    TextView txtTyping;
    @BindView(R.id.btnContactUs)
    Button btnContactUs;
    @BindView(R.id.profileLayout)
    LinearLayout profileLayout;
    @BindView(R.id.parentLay)
    RelativeLayout parentLay;
    @BindView(R.id.edtMsgLay)
    RelativeLayout edtMsgLay;
    @BindView(R.id.bottomLay)
    RelativeLayout bottomLay;

    int currentPage = 0, limit = 20;
    LinearLayoutManager mLayoutManager;
    StorageUtils storageUtils;
    Handler handler = new Handler();
    Runnable runnable;
    Handler onlineHandler = new Handler();
    int delay = 5000; //milliseconds
    PopupWindow popupWindow;
    boolean btnSendShow = false;
    boolean from = false;
    String instantMessage = "";
    Runnable onlineRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Constants.TAG_TYPE, Constants.TAG_PROFILE_LIVE);
                jsonObject.put(Constants.TAG_RECEIVER_ID, partnerId);
                Logging.i(TAG, "checkOnline: " + jsonObject);
                AppWebSocket.getInstance(ChatActivity.this).send(jsonObject.toString());
            } catch (JSONException e) {
                Logging.e(TAG, "checkOnline: " + e.getMessage());
                e.printStackTrace();
            }
            onlineHandler.postDelayed(onlineRunnable, delay);
        }
    };
    private File currentPhotoFile;
    private Uri mCurrentPhotoUri;
    private TextView txtAudioTime;
    private String partnerImage = "";
    private String partnerName = "";
    private DBHelper dbHelper;
    private MessageLiveModel liveModel;
    private ChatAdapter adapter;
    private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;
    private String blockStatus = Constants.TAG_FALSE;
    private PopupMenu popupMenu;
    private boolean isAdminChat = false;
    private List<ChatResponse> chatList = new ArrayList<>();
    // addon for voice messages
    private Context mContext;

    private SlidrInterface sliderInterface;
    private AppUtils appUtils;
    private ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {
        @Override
        public void onInserted(int position, int count) {
            // custom observer methods
            adapter.notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            // custom observer methods
            adapter.notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            // custom observer methods
            adapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            // custom observer methods
            adapter.notifyItemRangeChanged(position, count);
        }
    };

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);

        mContext = this;
//        loadAd();

        from = false;
        instantMessage = "notInstant";


        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            isAdminChat = getIntent().getStringExtra(Constants.TAG_FROM).equals(Constants.TAG_ADMIN);
        }

        partnerName = getIntent().getStringExtra(Constants.TAG_PARTNER_NAME);
        dbHelper = DBHelper.getInstance(this);
        storageUtils = new StorageUtils(this);

        liveModel = new ViewModelProvider(ChatActivity.this).get(MessageLiveModel.class);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (!isAdminChat) {
            if (notificationManager != null) {
                notificationManager.cancel(1);
            }
            partnerId = getIntent().getStringExtra(Constants.TAG_PARTNER_ID);
            partnerImage = getIntent().getStringExtra(Constants.TAG_PARTNER_IMAGE);
            chatId = GetSet.getUserId() + partnerId;
            blockStatus = dbHelper.getBlockStatus(chatId);
            dbHelper.updateRecent(chatId, Constants.TAG_UNREAD_COUNT, "0");
            if (getIntent().hasExtra(Constants.TAG_BLOCKED_BY_ME)) {
                blockStatus = getIntent().getStringExtra(Constants.TAG_BLOCKED_BY_ME);
            }
        } else {
            if (notificationManager != null) {
                notificationManager.cancel(0);
            }
            chatId = GetSet.getUserId();
            dbHelper.updateRecent(chatId, Constants.TAG_UNREAD_COUNT, "0");
        }

        initView();


        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }


    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        sliderInterface = Slidr.attach(this, config);

        Slidr.attach(this, config);

        txtTitle.setText(partnerName);
        adapter = new ChatAdapter(this);
        mLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        attachLay.setVisibility(View.VISIBLE);

        if (isAdminChat) {
            btnContactUs.setVisibility(View.VISIBLE);
            txtSubTitle.setVisibility(View.GONE);
            txtTyping.setVisibility(View.GONE);
            edtMessage.setVisibility(View.GONE);
            edtLay.setVisibility(View.GONE);
            btnVideoCall.setVisibility(View.GONE);
            btnMenu.setVisibility(View.GONE);

            // addon for smart reply
//            mSmartRepliesRecyler.setVisibility(View.GONE);
            // addon for chat translate
//            btnLanguage.setVisibility(View.INVISIBLE);

            // addon for audio call
//            btnAudioCall.setVisibility(View.INVISIBLE);

            getMessageList(currentPage = 0);
            getAdminMessages();
        } else {
            if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                txtSubTitle.setVisibility(View.GONE);
            } else {
                txtSubTitle.setVisibility(View.VISIBLE);
            }
            profileLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent profile = new Intent(getApplicationContext(), OthersProfileActivity.class);
                    profile.putExtra(Constants.TAG_PARTNER_ID, partnerId);
                    profile.putExtra(Constants.TAG_PARTNER_NAME, partnerName);
                    profile.putExtra(Constants.TAG_PARTNER_IMAGE, partnerImage);
                    profile.putExtra(Constants.TAG_FROM, Constants.TAG_MESSAGE);
                    profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(profile);
                }
            });
            getMessageList(currentPage = 0);
        }

        edtMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(edtMessage, InputMethodManager.SHOW_FORCED);
//                hideAttachment();
            }
        });

        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                if (charSequence.length() > 0)
                    setTyping(true);
                // addon for voice message
               /* if (charSequence.length() > 0) {
                    btnRecord.setVisibility(View.GONE);
                    setTyping(true);
                } else {
                    btnRecord.setVisibility(View.VISIBLE);
                }*/
            }

            @Override
            public void afterTextChanged(Editable editable) {
                runnable = new Runnable() {
                    public void run() {
                        setTyping(false);
                    }
                };
                handler.postDelayed(runnable, 1000);

                if (editable.length() > 0 && !btnSendShow) {
                    btnSendShow = true;
                    slideOutAnim();
                } else if (editable.length() == 0) {
                    btnSendShow = false;
                    slideInAnim();
                }
            }
        });

        endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                getMessageList(currentPage = page);
            }
        };
        endlessRecyclerOnScrollListener.resetState();
        recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);
        recyclerView.scrollToPosition(0);

        // addon for voice message
//        setVoiceRecorder();

        if (!isAdminChat) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dbHelper.updateMessageReadStatus(chatId, GetSet.getUserId());
                    sendReadStatus();
                }
            }, 2000);
        }
    }

    private void slideOutAnim() {
        btnSend.setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()) {
            btnSend.setRotation(180);
            TranslateAnimation animate = new TranslateAnimation(
                    -50,
                    0,
                    0,
                    0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            btnSend.startAnimation(animate);
        } else {
            btnSend.setRotation(0);
            btnSend.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    -50,
                    0,
                    0,
                    0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            btnSend.startAnimation(animate);
        }

    }

    private void slideInAnim() {
        TranslateAnimation animate = new TranslateAnimation(
                0,
                -50,
                0,
                0);
        animate.setDuration(200);
        animate.setFillAfter(false);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                btnSend.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        btnSend.startAnimation(animate);
    }

    private void getMessageList(int offset) {
        if (isAdminChat) {
            liveModel.getAdminMessages(this, chatId, limit, offset * limit)
                    .observe(this, new Observer<List<ChatResponse>>() {
                        @Override
                        public void onChanged(List<ChatResponse> chatResponses) {
                            adapter.setData(chatResponses);
                        }
                    });

        } else {
            liveModel.getMessages(this, chatId, limit, offset * limit).observe(this, new Observer<List<ChatResponse>>() {
                @Override
                public void onChanged(List<ChatResponse> chatResponses) {
                    adapter.setData(chatResponses);

                    // addon for smart reply
                    /*if (chatResponses.size() > 0){
                        for (ChatResponse messagesData : chatResponses){
                            if (messagesData.getMessageType().equals(Constants.TAG_TEXT)){
                                if (messagesData.getUserId()!=null && !messagesData.getUserId().equals(chatId)){
                                    generateReplies(messagesData);
                                }
                                break;
                            }else {
                                mSmartRepliesRecyler.setVisibility(View.GONE);
                            }


                        }
                    }*/


                    // addon for chat translate
                   /* if (chatResponses.size() > 0){
                        for (ChatResponse messagesData : chatResponses){
                            if (messagesData.getMessageEnd().equals(Constants.TAG_RECEIVED) && messagesData.getMessageType().equals(Constants.TAG_TEXT)) {
                                if (messagesData.getUserId()!=null && !messagesData.getUserId().equals(chatId)){
                                    if (SharedPref.getString(SharedPref.CHAT_LANGUAGE,SharedPref.DEFAULT_CHAT_LANGUAGE).equals("")){
                                        messagesData.setMessage(messagesData.getMessage());
                                    }else {
                                        String message=messagesData.getMessage();

                                        new TranslateLanguageTasks(ChatActivity.this,message,from){

                                            @Override
                                            protected void onPostExecute(String translatedMsg) {
                                                messagesData.setMessage(translatedMsg);
                                                adapter.notifyItemChanged(0);
                                                recyclerView.smoothScrollToPosition(0);
//                                            adapter.notifyDataSetChanged();

                                            }
                                        }.execute();
                                    }

                                }
                            }
                            break;
                        }
                    }*/

//                    adapter.notifyItemChanged(0);
                }
            });
        }
    }

    private void getAdminMessages() {
        if (NetworkReceiver.isConnected()) {
            long lastTime = 0L;
            ChatResponse lastMessage;
            lastMessage = dbHelper.getLastAdminMessage();
            if (lastMessage != null && lastMessage.getCreatedAt() != null) {
                lastTime = Long.parseLong(lastMessage.getCreatedAt());
            }
            Call<AdminMessageResponse> call = apiInterface.getAdminMessages(GetSet.getUserId(), Constants.TAG_ANDROID, GetSet.getCreatedAt(), lastTime);
            call.enqueue(new Callback<AdminMessageResponse>() {
                @Override
                public void onResponse(Call<AdminMessageResponse> call, Response<AdminMessageResponse> response) {
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        for (AdminMessageResponse.MessageData messageData : response.body().getMessageData()) {
                            dbHelper.addAdminMessage(messageData);
                            dbHelper.addAdminRecentMessage(messageData, 0);
                            ChatResponse chatResponse = new ChatResponse();
                            chatResponse.setChatId(GetSet.getUserId());
                            chatResponse.setReceiverId(GetSet.getUserId());
                            chatResponse.setMessageType(messageData.getMsgType());
                            chatResponse.setMessageEnd(Constants.TAG_RECEIVE);
                            chatResponse.setMessage(messageData.getMsgData());
                            chatResponse.setMessageId(messageData.getMsgId());
                            chatResponse.setChatTime(messageData.getCreateaAt());
                            chatResponse.setCreatedAt(messageData.getMsgAt());
                            chatResponse.setChatType(Constants.TAG_ADMIN);
                            liveModel.addAdminMessage(chatResponse);
                        }
                    }
                }

                @Override
                public void onFailure(Call<AdminMessageResponse> call, Throwable t) {

                }
            });
        } else {
            getMessageList(currentPage = 0);
        }
    }

    private void setTyping(boolean isTyping) {
        if (blockStatus != null && !blockStatus.equals(Constants.TAG_TRUE)) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Constants.TAG_TYPE, Constants.TAG_USER_TYPING);
                jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                jsonObject.put(Constants.TAG_RECEIVER_ID, partnerId);
                jsonObject.put(Constants.TAG_TYPING_STATUS, isTyping ? Constants.TAG_TYPING : Constants.TAG_UNTYPING);
                Logging.i(TAG, "setTyping: " + jsonObject);
                AppWebSocket.getInstance(this).send(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendReadStatus() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_TYPE, Constants.TAG_UPDATE_READ);
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_RECEIVER_ID, partnerId);
            Log.i(TAG, "sendReadStatus: " + jsonObject.toString());
            AppWebSocket.getInstance(this).send(jsonObject.toString());
        } catch (JSONException e) {
            Log.i(TAG, "sendReadStatus: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
        if (isConnected) {
            AppWebSocket.getInstance(this).setWebSocketClient(null);
            AppWebSocket.setNullInstance();
            AppWebSocket.getInstance(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        AppWebSocket.setCallEvents(this);
        checkOnline();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        EventBus.getDefault().unregister(this);
    }

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String userId) {
        if (partnerId.equals(userId)) {
            getProfile(partnerId);
        }
    }*/

    @Override
    protected void onPause() {
        try {
            AppWebSocket.setCallEvents(null);
        } catch (Exception e) {
            Logging.e(TAG, "onDestroy: " + e.getMessage());
        }
        onlineHandler.removeCallbacks(onlineRunnable);
        AppUtils.hideKeyboard(ChatActivity.this);

//        stopMedia();
        // addon for voice message
//        stopAudioViewHolderChat();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetworkReceiver();
    }

    private void checkOnline() {
        if (blockStatus != null && !blockStatus.equals(Constants.TAG_TRUE)) {
            onlineHandler.post(onlineRunnable);
        }
    }

    @OnClick({R.id.btnBack, R.id.btnMenu, R.id.btnVideoCall, R.id.btnGallery, R.id.btnCamera,
            R.id.btnAdd, R.id.btnSend, R.id.btnContactUs, R.id.btnLanguage, R.id.btnGif, R.id.btnAudioCall})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                hideKeyboard(ChatActivity.this);
                onBackPressed();
                break;
            case R.id.btnVideoCall:
                if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else {
                    if (GetSet.getGems() >= AdminData.videoCallsGems) {
                        if (NetworkReceiver.isConnected()) {
                            AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                            Intent callIntent = appRTCUtils.connectToRoom(partnerId, Constants.TAG_SEND, Constants.TAG_VIDEO);
                            callIntent.putExtra(Constants.TAG_USER_NAME, partnerName);
                            callIntent.putExtra(Constants.TAG_USER_IMAGE, partnerImage);
                            callIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                            callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(callIntent);
                        } else {
                            App.makeToast(getString(R.string.no_internet_connection));
                        }
                    } else {
                        App.makeToast(getString(R.string.not_enough_gems));
                    }
                }
                break;

            // addon for audio call
           /* case R.id.btnAudioCall:
                if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else {
                    if (NetworkReceiver.isConnected()) {
                        AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                        Intent callIntent = appRTCUtils.connectToRoom(partnerId, Constants.TAG_SEND, Constants.TAG_AUDIO);
                        callIntent.putExtra(Constants.TAG_USER_NAME, partnerName);
                        callIntent.putExtra(Constants.TAG_USER_IMAGE, partnerImage);
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(callIntent);
                    } else {
                        App.makeToast(getString(R.string.no_internet_connection));
                    }

                }
                break;*/

            case R.id.btnMenu:
                openMenu(view);
                break;
            case R.id.btnGallery:
            case R.id.btnCamera:
            /*    if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, Constants.STORAGE_REQUEST_CODE);
                } else {
                   if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                        ImagePicker.pickImage(this, "Select your image:");
                    } else {
                        pickImage();
                   }
                }*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA}, 100);
                    } else {
                        pickImage();
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                    } else {
                        ImagePicker.pickImage(this, "Select your image:");
                    }
                }
                break;
               /* if (ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA},
                            Constants.STORAGE_REQUEST_CODE);
                } else if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else {
                    App.preventMultipleClick(btnCamera);
                    pickImage();

                    //  ImagePicker.pickImage(ChatActivity.this);
                }*/
                /*case R.id.btnAdd:
                if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else if (ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA},
                            Constants.STORAGE_REQUEST_CODE);
                } else {
                    ImagePicker.pickImage(ChatActivity.this);
                }
                break;*/

            case R.id.btnAdd:
                if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else {
                    btnAdd.setVisibility(View.GONE);
                    AppUtils.hideKeyboard(this);
                    showAttachment();

                }
                break;

            // addon for emoji and gifs
           /* case R.id.btnGif:
                giphyDialogFragment = GiphyDialogFragment.Companion.newInstance(settings);
                giphyDialogFragment.show(getSupportFragmentManager(), "giphy_dialog");
                giphyDialogFragment.setGifSelectionListener(this);
                break;*/

            case R.id.btnSend:
                if (TextUtils.isEmpty("" + edtMessage.getText().toString().trim())) {
                    edtMessage.setError(getString(R.string.enter_your_message));
                } else if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else if (!NetworkReceiver.isConnected()) {
                    App.makeToast(getString(R.string.no_internet_connection));
                } else {
//                    sendChat(Constants.TAG_TEXT, "", "", "");
                    String textsend = edtMessage.getText().toString();
                    sendChat(Constants.TAG_TEXT, "", "", "", textsend);
                    edtMessage.setText("");
                }
                break;
            case R.id.btnContactUs:
                if (NetworkReceiver.isConnected()) {
                    sendEmail();
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void sendEmail() {
        try {

            String reportContetnt = "\n\n" + "DEVICE OS VERSION CODE: " + Build.VERSION.SDK_INT + "\n" +
                    "DEVICE VERSION CODE NAME: " + Build.VERSION.CODENAME + "\n" +
                    "DEVICE NAME: " + AppUtils.getDeviceName() + "\n" +
                    "VERSION CODE: " + BuildConfig.VERSION_CODE + "\n" +
                    "VERSION NAME: " + BuildConfig.VERSION_NAME + "\n" +
                    "PACKAGE NAME: " + BuildConfig.APPLICATION_ID + "\n" +
                    "BUILD TYPE: " + BuildConfig.BUILD_TYPE;

            final Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{"" + AdminData.contactEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            emailIntent.putExtra(Intent.EXTRA_TEXT, reportContetnt);
            try {
                //start email intent
                startActivity(Intent.createChooser(emailIntent, "Email"));
            } catch (Exception e) {
                //if any thing goes wrong for example no email client application or any exception
                //get and show exception message
                App.makeToast(e.getMessage());
            }
        } catch (Exception e) {
            Logging.e(TAG, "sendEmail: " + e.getMessage());
        }
    }

    private void openMenu(View view) {
        popupMenu = new PopupMenu(ChatActivity.this, btnMenu, R.style.PopupMenuBackground);
        popupMenu.getMenuInflater().inflate(R.menu.chat_menu, popupMenu.getMenu());
        popupMenu.setGravity(Gravity.START);
        if (blockStatus.equals(Constants.TAG_TRUE))
            popupMenu.getMenu().getItem(1).setTitle(getString(R.string.unblock));
        else popupMenu.getMenu().getItem(1).setTitle(getString(R.string.block));

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
                if (item.getTitle().toString().equals(getString(R.string.clear_chat))) {
                    openClearChatDialog();
                } else if (item.getTitle().toString().equals(getString(R.string.block))) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put(Constants.TAG_TYPE, Constants.TAG_BLOCK_USER);
                        json.put(Constants.TAG_RECEIVER_ID, partnerId);
                        json.put(Constants.TAG_BLOCKED, true);
                        Logging.i(TAG, "blockUser: " + json);
                        AppWebSocket.getInstance(ChatActivity.this).send(json.toString());
                        if (dbHelper.isChatIdExists(chatId)) {
                            dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_TRUE);
                            dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
                            dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
                        } else {
                            dbHelper.insertBlockStatus(chatId, partnerId, partnerName, partnerImage, Constants.TAG_TRUE);
                            if (dbHelper.isChatIdExists(chatId)) {
                                dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
                                dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
                            }
                        }
                        EventBus.getDefault().postSticky(partnerId);
                        blockStatus = Constants.TAG_TRUE;
                        onlineHandler.removeCallbacks(onlineRunnable);
                        txtSubTitle.setVisibility(View.GONE);
                        txtTyping.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    popupMenu.dismiss();
                    App.makeToast(getString(R.string.user_blocked_successfully));
                } else if (item.getTitle().toString().equals(getString(R.string.unblock))) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put(Constants.TAG_TYPE, Constants.TAG_BLOCK_USER);
                        json.put(Constants.TAG_RECEIVER_ID, partnerId);
                        json.put(Constants.TAG_BLOCKED, false);
                        Logging.i(TAG, "blockUser: " + json);
                        AppWebSocket.getInstance(ChatActivity.this).send(json.toString());
                        if (dbHelper.isChatIdExists(chatId)) {
                            dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_FALSE);
                        } else {
                            dbHelper.insertBlockStatus(chatId, partnerId, partnerName, partnerImage, Constants.TAG_FALSE);
                        }
                        EventBus.getDefault().postSticky(partnerId);
                        blockStatus = Constants.TAG_FALSE;
                        txtSubTitle.setVisibility(View.VISIBLE);
                        checkOnline();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    popupMenu.dismiss();
                    App.makeToast(getString(R.string.unblocked_successfully));
                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    private void openClearChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setMessage(getString(R.string.really_want_clear));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                hideKeyboard(ChatActivity.this);
                dbHelper.deleteMessages(chatId);
                if (MessageLiveModel.msgLiveData != null) {
                    MessageLiveModel.msgLiveData.setValue(new ArrayList<>());
                }
                AppUtils.hideKeyboard(ChatActivity.this);
                popupMenu.dismiss();
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
            typeface = ResourcesCompat.getFont(this, R.font.font_light);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 234) {
            try {
                final File file = new File(ImagePicker.getImageFilePath(this, requestCode, resultCode, data));
                String fileName = getString(R.string.app_name) + System.currentTimeMillis() + Constants.IMAGE_EXTENSION;
                Bitmap bitmap = ImagePicker.getImageFromResult(getApplicationContext(), requestCode, resultCode, data);
                //   Uri thumbnail = storageUtils.saveToSDCard(bitmap, Constants.TAG_SENT,file.getName());
                //   storageUtils.saveImage(bitmap,file.getName(), Constants.TAG_SENT);
                String thumbnails = storageUtils.saveThumbNail(bitmap, fileName);

                Log.d(TAG, "onResponseUploadImage: " + thumbnails);
                Log.d(TAG, "onResponseUploadImage: " + fileName);

                JSONObject jsonObject = updateDB(Constants.TAG_IMAGE, fileName, fileName, thumbnails, Constants.TAG_SENDING);
                Uri myUri = Uri.parse(thumbnails);
                InputStream imageStream = getContentResolver().openInputStream(Uri.fromFile(file));
                uploadChatImage(storageUtils.getBytes(imageStream), fileName, thumbnails, jsonObject);

           /*     @SuppressLint("StaticFieldLeak")


                ImageCompression imageCompression = new ImageCompression(ChatActivity.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        try {
                            Uri myUri = Uri.parse(imagePath);

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                };
                Log.d(TAG, "onActivityResult: "+file.getAbsolutePath());
                imageCompression.execute(file.getAbsolutePath());*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                //          Log.d(TAG, "onActivityResult: "+data.getData());
                try {
                    boolean isCamera = (data == null
                            || data.getData() == null);
                    String fileName = getString(R.string.app_name) + System.currentTimeMillis() + Constants.IMAGE_EXTENSION;
                    Uri file = null;
                    if (isCamera) {     /** CAMERA **/
                        Log.d(TAG, "onActivityResult: " + mCurrentPhotoUri.getPath());
                        file = storageUtils.saveToSDCard(FileUtil.decodeBitmap(this, mCurrentPhotoUri), Constants.TAG_SENT, fileName);
                        deleteFileForUri(mCurrentPhotoUri);
                        mCurrentPhotoUri = null;
                        //      Timber.d("Picked: %s fromCamera: %s", imageUri, true);
                    } else {            /** ALBUM **/
                        String mimeType = getContentResolver().getType(data.getData());
                        if (mimeType != null && mimeType.startsWith("image")) {
                            file = storageUtils.saveToSDCard(FileUtil.decodeBitmap(this, data.getData()), Constants.TAG_SENT, fileName);
                            //   Timber.d("Picked: %s", imageUri);
                        } else {
                            //    Toasty.info(this, "Videos are not supported yet").show();
                            return;
                        }
                    }

                    if (file != null) {
                        String thumbnail = storageUtils.saveThumbNail(FileUtil.decodeBitmap(this, file), fileName);
                        try {
                            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(file, "r");
                            /*byte[] imageBytes = ByteStreams.toByteArray(new FileInputStream(pfd.getFileDescriptor()));
                            JSONObject jsonObject = updateDB(Constants.TAG_IMAGE, fileName, fileName, thumbnail, Constants.TAG_SENDING);
                            uploadChatImage(imageBytes, fileName, thumbnail, jsonObject);*/
                        } catch (Exception e) {
                            //   Timber.e(e);
                            //   Toasty.error(this, R.string.something_went_wrong).show();
                        }
                        /**
                         * The image is working fine without a compression.
                         */

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            // for chat translation addon
      /*  if (requestCode == Constants.LANGUAGE_REQUEST_CODE) {
            adapter.notifyDataSetChanged();
        }*/
        }

        if (requestCode == Constants.LANGUAGE_REQUEST_CODE) {
           /* Intent refresh = new Intent(ChatActivity.this, ChatActivity.class);
            startActivity(refresh);
            this.finish();*/
//            startActivity(getIntent());
//            startActivityForResult(getIntent(),Constants.LANGUAGE_REQUEST_CODE);
            adapter.notifyDataSetChanged();

        }
    }

    private void deleteFileForUri(Uri uri) {
        if (uri != null) {
            ContentResolver contentResolver = getContentResolver();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (contentResolver.delete(uri, null, null) > 0) {
                    // Timber.i("Save to gallery: deleted %s", uri.getPath());
                } else {
                    //   Timber.i("Save to gallery: failed deleting %s", uri.getPath());
                }
            } else {
                /* For Android 11 (R) and above, use this snippet */
                        /*PendingIntent trashPendingIntent = MediaStore.createTrashRequest(contentResolver, Collections.singleton(mTempSavedFileUri), true);
                        try {
                            startIntentSenderForResult(trashPendingIntent.getIntentSender(), 120, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            Timber.e("Save to gallery: failed deleting %s", mTempSavedFileUri.getPath());
                        }*/
            }
        }
    }

    private void uploadChatImage(byte[] imageBytes, String fileName, String thumbnail, JSONObject jsonObject) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_CHAT_IMAGE, "image.jpg", requestFile);
        RequestBody userId = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<Map<String, String>> call3 = apiInterface.uploadChatImage(body, userId);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Log.d(TAG, "onResponseUploadImage: " + response.body().toString());
                Map<String, String> data = response.body();
                if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                    Log.d(TAG, "onResponse: " + data.get(Constants.TAG_USER_NAME));


                    storageUtils.getImage(Constants.TAG_THUMBNAIL, fileName);

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                        storeImage(data.get(Constants.TAG_USER_IMAGE), fileName, jsonObject, data);
                    else {
                        liveModel.updateImageUpload(jsonObject);
                        sendImageChat(jsonObject, data.get(Constants.TAG_USER_IMAGE), data.get(Constants.TAG_USER_IMAGE));

                    }


                }
            }


            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.d(TAG, "onResponseUploadImage: " + t.toString());

                call.cancel();
            }
        });
    }

    private void storeImage(String s1, String s, JSONObject jsonObject, Map<String, String> data) {
        Log.d(TAG, "storeImage: " + s1 + "S" + s);

        String storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + getString(R.string.app_name) + "/" + getString(R.string.app_name) + "Images/Sent/" + s;
        File photo = new File(storagePath);

        String fileName = photo.getName();

        Log.d(TAG, "storeImage: " + fileName);
        File newFile = new File(photo.getParent(), s1 + ".jpg");

        photo.renameTo(newFile);
        Log.d(TAG, "storeImage: " + photo.renameTo(newFile));
        liveModel.updateImageUpload(jsonObject);
        sendImageChat(jsonObject, data.get(Constants.TAG_USER_IMAGE), data.get(Constants.TAG_USER_IMAGE));

    }

    private void sendChat(String type, String chatImage, String filePath, String thumbnail, String txt) {
        try {
            long unixStamp = System.currentTimeMillis() / 1000L;
            String utcTime = AppUtils.getCurrentUTCTime(this);
            String messageId = GetSet.getUserId() + unixStamp;
            String msg = "";
            switch (type) {
                case Constants.TAG_TEXT:
//                    msg = edtMessage.getText().toString();
                    msg = txt;
                    break;
                case Constants.TAG_IMAGE:
                    msg = chatImage;
                    break;

                case Constants.TAG_GIF:
                    msg = chatImage;
                    break;

                case Constants.TAG_AUDIO:
                    msg = getFileName(filePath);
                    break;
                case Constants.TAG_VIDEO:
                    msg = getString(R.string.video);
                    break;
            }

            JSONObject json = new JSONObject();
            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            json.put(Constants.TAG_RECEIVER_ID, partnerId);
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_USER_NAME, GetSet.getName());
            json.put(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            json.put(Constants.TAG_CHAT_ID, chatId);
            json.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            json.put(Constants.TAG_MSG_TYPE, type);
            json.put(Constants.TAG_MESSAGE_END, Constants.TAG_SEND);
            json.put(Constants.TAG_MESSAGE, AppUtils.encryptMessage(msg));
            json.put(Constants.TAG_MSG_ID, messageId);
            json.put(Constants.TAG_CHAT_TIME, utcTime);
            Logging.i(TAG, "sendChat: " + json);
            AppWebSocket.getInstance(this).send(json.toString());

            /*Add receiver info in DB*/
            json.put(Constants.TAG_USER_ID, partnerId);
            json.put(Constants.TAG_USER_NAME, partnerName);
            json.put(Constants.TAG_USER_IMAGE, partnerImage);
            /*Add original message to DB*/
            json.put(Constants.TAG_MESSAGE, msg);
            json.put(Constants.TAG_THUMBNAIL, thumbnail);
            json.put(Constants.TAG_DELIVERY_STATUS, Constants.TAG_SEND);
            json.put(Constants.TAG_PROGRESS, "");
            liveModel.addMessage(getApplicationContext(), json);
            liveModel.addRecentChat(json);
            recyclerView.smoothScrollToPosition(0);
            edtMessage.setText("");
        } catch (JSONException e) {
            Logging.e(TAG, "sendChat: " + e.getMessage());
        }
    }

    private void sendImageChat(JSONObject jsonObject, String chatImage, String fileName) {
        try {
            String utcTime = jsonObject.getString(Constants.TAG_CHAT_TIME);
            String messageId = jsonObject.getString(Constants.TAG_MSG_ID);
            String msg = "";
            String type = jsonObject.getString(Constants.TAG_MSG_TYPE);
            switch (type) {
                case Constants.TAG_TEXT:
                    msg = edtMessage.getText().toString();
                    break;
                case Constants.TAG_IMAGE:
                    msg = chatImage;
                    break;

                case Constants.TAG_GIF:
                    msg = chatImage;
                    break;

                case Constants.TAG_AUDIO:
                    msg = fileName;
                    break;
                case Constants.TAG_VIDEO:
                    msg = getString(R.string.video);
                    break;
            }

            JSONObject json = new JSONObject();
            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            json.put(Constants.TAG_RECEIVER_ID, partnerId);
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_USER_NAME, GetSet.getName());
            json.put(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            json.put(Constants.TAG_CHAT_ID, chatId);
            json.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            json.put(Constants.TAG_MSG_ID, messageId);
            json.put(Constants.TAG_MSG_TYPE, type);
            json.put(Constants.TAG_MESSAGE, AppUtils.encryptMessage(msg));
            json.put(Constants.TAG_CHAT_TIME, utcTime);
            Logging.i(TAG, "sendImageChat: " + json);
            AppWebSocket.getInstance(this).send(json.toString());
            dbHelper.updateMessage(messageId, Constants.TAG_PROGRESS, Constants.TAG_COMPLETED);
            dbHelper.updateMessage(messageId, Constants.TAG_MESSAGE, chatImage);
            recyclerView.smoothScrollToPosition(0);
        } catch (JSONException e) {
            Logging.e(TAG, "sendImageChat: " + e.getMessage());
        }
    }

    private JSONObject updateDB(String type, String chatImage, String filePath,
                                String thumbnail, String progress) {
        JSONObject json = new JSONObject();
        try {
            long unixStamp = System.currentTimeMillis() / 1000L;
            String utcTime = AppUtils.getCurrentUTCTime(this);
            String messageId = GetSet.getUserId() + unixStamp;
            String msg = "";
            switch (type) {
                case Constants.TAG_TEXT:
                    msg = edtMessage.getText().toString();
                    break;
                case Constants.TAG_IMAGE:
                    msg = chatImage;
                    break;

                case Constants.TAG_GIF:
                    msg = chatImage;
                    break;

                case Constants.TAG_AUDIO:
                    msg = getFileName(filePath);
                    break;
                case Constants.TAG_VIDEO:
                    msg = getString(R.string.video);
                    break;
            }

            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            json.put(Constants.TAG_RECEIVER_ID, partnerId);
            json.put(Constants.TAG_CHAT_ID, chatId);
            json.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            json.put(Constants.TAG_MSG_ID, messageId);
            json.put(Constants.TAG_MSG_TYPE, type);
            json.put(Constants.TAG_MESSAGE_END, Constants.TAG_SEND);
            json.put(Constants.TAG_CHAT_TIME, utcTime);
            json.put(Constants.TAG_USER_ID, partnerId);
            json.put(Constants.TAG_USER_NAME, partnerName);
            json.put(Constants.TAG_USER_IMAGE, partnerImage);
            /*Add original message to DB*/
            json.put(Constants.TAG_MESSAGE, msg);
            json.put(Constants.TAG_THUMBNAIL, thumbnail);
            json.put(Constants.TAG_DELIVERY_STATUS, Constants.TAG_UNREAD);
            json.put(Constants.TAG_PROGRESS, progress);
            liveModel.addMessage(getApplicationContext(), json);
            liveModel.addRecentChat(json);
            recyclerView.smoothScrollToPosition(0);
        } catch (JSONException e) {
            Logging.e(TAG, "updateDB: " + e.getMessage());
        }
        return json;
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1);
        }
        return imgSplit;
    }

    private void showAttachment() {
        // Prepare the View for the animation
        attachLay.setVisibility(View.VISIBLE);
        // Start the animation
        attachLay.animate()
                .translationX(0)
                .alpha(1.0f)
                .setDuration(100)
                .setListener(null);
    }

    private void hideAttachment() {
        attachLay.animate()
                .translationX(-100)
                .setDuration(100)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        attachLay.setVisibility(View.GONE);

                        btnAdd.setVisibility(View.VISIBLE);
                    }
                });
    }

    public void showLoading() {
        /*Disable touch options*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        /*Enable touch options*/
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onWebSocketConnected() {

    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(message);
                    Log.i(TAG, "chatactivityr: " + message);
                    String type = jsonObject.getString(Constants.TAG_TYPE);
                    if (!isAdminChat) {
                        switch (type) {
                            case Constants.TAG_RECEIVE_CHAT:
                                recyclerView.smoothScrollToPosition(0);
                                sendReadStatus();
                                break;
                            case Constants.TAG_lISTEN_TYPING:
                                if (jsonObject.getString(Constants.TAG_RECEIVER_ID).equals(GetSet.getUserId())) {
                                    if (blockStatus.equals(Constants.TAG_FALSE)) {
                                        if (jsonObject.getString(Constants.TAG_TYPING_STATUS).equals(Constants.TAG_TYPING)) {
                                            txtSubTitle.setVisibility(View.GONE);
                                            txtTyping.setVisibility(View.VISIBLE);
                                        } else {
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    txtTyping.setVisibility(View.GONE);
                                                    txtSubTitle.setVisibility(View.VISIBLE);
                                                }
                                            }, 1000);
                                        }
                                    } else {
                                        txtTyping.setVisibility(View.GONE);
                                    }
                                }
                                break;
                            case Constants.TAG_PROFILE_STATUS:
                                if (jsonObject.optString(Constants.TAG_ONLINE_STATUS).equals(Constants.TAG_TRUE)) {

                                    from = true;
                                    instantMessage = "instant";
                                    Log.i(TAG, "chatactranslatedMsgrun: " + from);

                                    if (txtTyping.getVisibility() != View.VISIBLE) {
                                        txtSubTitle.setVisibility(View.VISIBLE);
                                        txtSubTitle.setText(getString(R.string.online));
                                    } else {
                                        txtSubTitle.setVisibility(View.GONE);
                                    }
                                } else if (jsonObject.getString(Constants.TAG_ONLINE_STATUS).equals(Constants.TAG_FALSE)) {
                                    txtSubTitle.setVisibility(View.GONE);
                                    Log.i(TAG, "chatactivityrunru: " + AppUtils.getRecentDate(getApplicationContext(),
                                            AppUtils.getTimeFromUTC(getApplicationContext(), jsonObject.optString(Constants.TAG_ONLINE_STATUS))).toLowerCase());

                                } else {
                                    if (txtTyping.getVisibility() != View.VISIBLE) {
                                        txtSubTitle.setVisibility(View.VISIBLE);
                                        txtSubTitle.setText(getString(R.string.last_seen_at) + " " + AppUtils.getRecentDate(getApplicationContext(),
                                                AppUtils.getTimeFromUTC(getApplicationContext(), jsonObject.optString(Constants.TAG_ONLINE_STATUS))).toLowerCase());
                                        Log.i(TAG, "chatactivityrunrun: " + AppUtils.getRecentDate(getApplicationContext(),
                                                AppUtils.getTimeFromUTC(getApplicationContext(), jsonObject.optString(Constants.TAG_ONLINE_STATUS))).toLowerCase());
                                    }
                                }
                                break;
                            case Constants.TAG_RECEIVE_READ_STATUS:
                                liveModel.changeSentStatus();
                                break;
                            case Constants.TAG_OFFLINE_READ_STATUS:
                                liveModel.changeSentStatus();
                                break;
                        }
                    }
                } catch (
                        JSONException e) {
                    Log.e(TAG, "onWebSocketMessage: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onWebSocketClose() {

    }

    @Override
    public void onWebSocketError(String description) {

    }

    private void pickImage() {

        View contentView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_pick_options, findViewById(R.id.parentLay), false);
        BottomSheetDialog pickerOptionsSheet = new BottomSheetDialog(this, R.style.SimpleBottomDialog);
        pickerOptionsSheet.setCanceledOnTouchOutside(true);
        pickerOptionsSheet.setContentView(contentView);
        //    pickerOptionsSheet.setDismissWithAnimation(true);

        View layoutCamera = contentView.findViewById(R.id.container_camera_option);
        View layoutGallery = contentView.findViewById(R.id.container_gallery_option);

        layoutCamera.setOnClickListener(v -> {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (captureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    //     Timber.e(ex);
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCurrentPhotoUri = FileProvider.getUriForFile(this,
                            getString(R.string.file_provider_authority),
                            photoFile);
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri);
                    startActivityForResult(captureIntent, REQUEST_CODE_PICK_IMAGE);
                }
            }
            pickerOptionsSheet.dismiss();
        });
        layoutGallery.setOnClickListener(v -> {
            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Intent pickIntent = new Intent(Intent.ACTION_PICK, collection);
            pickIntent.setType("image/jpeg");

            Intent chooserIntent = Intent.createChooser(pickIntent, "Select a picture");

            if (chooserIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
            }
            pickerOptionsSheet.dismiss();
        });

        pickerOptionsSheet.show();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoFile = image;
        return image;
    }


    // addon for emoji and gifs
   /* @Override
    public void onDismissed() {
        hideAttachment();

    }

    @Override
    public void onGifSelected(@NotNull Media media) {
        hideAttachment();

        JSONObject jsonObject = updateDB(Constants.TAG_GIF, "" + media.getImages().getFixedHeightDownsampled().getGifUrl(), "", "", Constants.TAG_SENDING);
        sendFileChat(jsonObject, jsonObject.optString(Constants.TAG_MESSAGE));

    }*/

    // addon for smart reply
  /*  @Override
    public void onChipClick(@NonNull String chipText) {

        Log.i(TAG, "onChipClick: "+chipText);
        if (blockStatus!=null &&blockStatus.equals(Constants.TAG_TRUE)){
            App.makeToast(getString(R.string.unblock_description));
        }else {
            sendChat(Constants.TAG_TEXT, "", "", "",chipText);
        }


    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.STORAGE_REQUEST_CODE:
                if (checkPermissions(permissions)) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                        ImagePicker.pickImage(this, "Select your image:");
                    } else {
                        pickImage();
                    }
                    //   ImagePicker.pickImage(this, "Select your image:");
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(permissions, Constants.STORAGE_REQUEST_CODE);
                        } else {
                            App.makeToast(getString(R.string.camera_storage_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivity(i);
                        }
                    }
                }
                break;
            case Constants.DOWNLOAD_REQUEST_CODE:
                if (!checkPermissions(permissions)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(permissions, Constants.DOWNLOAD_REQUEST_CODE);
                        } else {
                            App.makeToast(getString(R.string.storage_permission_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivity(i);
                        }
                    }
                }
                break;
        }

        if (requestCode == 111) {
            int permissionAudio = ContextCompat.checkSelfPermission(ChatActivity.this,
                    RECORD_AUDIO);
            int permissionStorage = ContextCompat.checkSelfPermission(ChatActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            // addon for smart reply
//            setVoiceRecorder();
        }
    }

    // addon for voice message
   /* @Override
    public void onAudioComplete() {
        state = recyclerView.getLayoutManager().onSaveInstanceState();
        // Main position of RecyclerView when loaded again
        if (state != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(state);
        }
        if (currentMessageId != null) {
            ChatResponse tempMsgData = dbHelper.getSingleMessage(currentMessageId);
            int tempPosition = -1;

            int chatPosition = 0;
            for (ChatResponse chatResponse : chatList){
                if (chatResponse.getMessageId().equals(currentMessageId)){
                    tempPosition =chatPosition;
                    break;
                }
                chatPosition ++;
            }
            *//*if (chatList.contains(tempMsgData)) {
                tempPosition = chatList.indexOf(tempMsgData);
            }*//*
            if (tempPosition != -1) {
                RecyclerView.ViewHolder tempViewHolder = recyclerView.findViewHolderForAdapterPosition(tempPosition);
                if (tempViewHolder instanceof ChatAdapter.SentAudioHolder) {
                    ChatAdapter.SentAudioHolder holder = (ChatAdapter.SentAudioHolder) tempViewHolder;
                    holder.songSeekbar.setProgress(0);
                    File file = storageUtils.getFile(Constants.TAG_AUDIO_SENT, tempMsgData.getMessage());
                    if (file != null) {
                        Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                        holder.txtDuration.setVisibility(View.VISIBLE);
                        holder.txtDuration.setText(milliSecondsToTimer(storageUtils.getMediaDuration(mContext, voiceURI)));
                    } else {
                        holder.txtDuration.setVisibility(View.INVISIBLE);
                    }
                    holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pause_icon_white));
                } else if (tempViewHolder instanceof ChatAdapter.ReceiveAudioHolder) {
                    ChatAdapter.ReceiveAudioHolder holder = ( ChatAdapter.ReceiveAudioHolder) tempViewHolder;
                    holder.songSeekbar.setProgress(0);
                    File file = storageUtils.getFile(Constants.TAG_AUDIO_RECEIVE, tempMsgData.getMessage());
                    if (file != null) {
                        Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                        holder.txtDuration.setVisibility(View.VISIBLE);
                        holder.txtDuration.setText(milliSecondsToTimer(storageUtils.getMediaDuration(mContext, voiceURI)));
                    } else {
                        holder.txtDuration.setVisibility(View.INVISIBLE);
                    }
                    holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pause_icon_white));
                }
            }
        }
        AppUtils.resumeExternalAudioChat(mContext);
    }*/

    // addon for voice message
/*
    @Override
    public void onAudioUpdate(int currentPosition) {
   if (currentMessageId != null) {
            ChatResponse tempMsgData = dbHelper.getSingleMessage(currentMessageId);
            int tempPosition = -1;
            int chatPosition = 0;
            for (ChatResponse chatResponse : chatList){
                if (chatResponse.getMessageId().equals(currentMessageId)){
                    tempPosition =chatPosition;
                    break;
                }
                chatPosition ++;
            }
            */
/*if (chatList.contains(tempMsgData)) {
                tempPosition = chatList.indexOf(tempMsgData);
            }*//*


            if (tempPosition != -1) {
                RecyclerView.ViewHolder tempViewHolder = recyclerView.findViewHolderForAdapterPosition(tempPosition);
                if (tempViewHolder instanceof ChatAdapter.SentAudioHolder) {
                    ChatAdapter.SentAudioHolder holder = (ChatAdapter.SentAudioHolder) tempViewHolder;
                    holder.songSeekbar.setProgress(currentPosition);
                    holder.txtDuration.setText(milliSecondsToTimer(currentPosition));
                    holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.play_icon_white));
                } else if (tempViewHolder instanceof ChatAdapter.ReceiveAudioHolder) {
                    ChatAdapter.ReceiveAudioHolder holder = (ChatAdapter.ReceiveAudioHolder) tempViewHolder;
                    holder.songSeekbar.setProgress(currentPosition);
                    holder.txtDuration.setText(milliSecondsToTimer(currentPosition));
                    holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.play_icon_white));
                }
            }
        }
    }
*/

    public boolean checkPermissions(String[] permissions) {
        boolean permissionGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
                break;
            }
        }
        return permissionGranted;
    }


    // addon for voice message
/*
    public void stopAudioViewHolder() {
        MediaPlayerUtils.pauseMediaPlayer();
        if (currentMessageId != null) {
            ChatResponse tempMsgData = dbHelper.getSingleMessage(currentMessageId);
            int tempPosition = -1;
            int chatPosition = 0;
            for (ChatResponse chatResponse : chatList){
                if (chatResponse.getMessageId().equals(currentMessageId)){
                    tempPosition =chatPosition;
                    break;
                }
                chatPosition ++;
            }
           */
/* if (chatList.contains(tempMsgData)) {
                tempPosition = chatList.indexOf(tempMsgData);
            }*//*

            if (tempPosition != -1) {
                adapter.notifyItemChanged(tempPosition);
                */
/*RecyclerView.ViewHolder tempViewHolder = recyclerView.findViewHolderForAdapterPosition(tempPosition);
                if (tempViewHolder instanceof MessageListAdapter.SentVoiceHolder) {
                    MessageListAdapter.SentVoiceHolder holder = (MessageListAdapter.SentVoiceHolder) tempViewHolder;
                    holder.seekbar.setProgress(0);
                    holder.icon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pause_icon_white));
                    File file = storageManager.getFile(ApplicationClass.decryptMessage(tempMsgData.attachment), StorageManager.TAG_AUDIO_SENT);
                    if (file != null) {
                        Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                        holder.duration.setVisibility(View.VISIBLE);
                        holder.duration.setText(milliSecondsToTimer(getMediaDuration(mContext, voiceURI)));
                    } else {
                        holder.duration.setVisibility(View.INVISIBLE);
                    }
                } else if (tempViewHolder instanceof MessageListAdapter.ReceiveVoiceHolder) {
                    MessageListAdapter.ReceiveVoiceHolder holder = (MessageListAdapter.ReceiveVoiceHolder) tempViewHolder;
                    holder.seekbar.setProgress(0);
                    holder.icon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pause_icon_white));
                    File file = storageManager.getFile(ApplicationClass.decryptMessage(tempMsgData.attachment), StorageManager.TAG_AUDIO);
                    if (file != null) {
                        Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                        holder.duration.setVisibility(View.VISIBLE);
                        holder.duration.setText(milliSecondsToTimer(getMediaDuration(mContext, voiceURI)));
                    } else {
                        holder.duration.setVisibility(View.INVISIBLE);
                    }
                }*//*

            }
        }
    }
*/

/*
 public void stopAudioViewHolderChat() {
     MediaPlayerUtils.pauseMediaPlayer();
     if (currentMessageId != null) {
         ChatResponse tempMsgData = dbHelper.getSingleMessage(currentMessageId);
         int tempPosition = -1;
         int chatPosition = 0;
         for (ChatResponse chatResponse : chatList) {
             if (chatResponse.getMessageId().equals(currentMessageId)) {
                 tempPosition = chatPosition;
                 break;
             }
             chatPosition++;
         }
           */
/* if (chatList.contains(tempMsgData)) {
                tempPosition = chatList.indexOf(tempMsgData);
            }*//*

         if (tempPosition != -1) {
             RecyclerView.ViewHolder tempViewHolder = recyclerView.findViewHolderForAdapterPosition(tempPosition);
             if (tempViewHolder instanceof ChatAdapter.SentAudioHolder) {
                 ChatAdapter.SentAudioHolder holder = (ChatAdapter.SentAudioHolder) tempViewHolder;
                 holder.songSeekbar.setProgress(0);
                 holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pause_icon_white));
                 File file = storageUtils.getFile(Constants.TAG_AUDIO_SENT, tempMsgData.getMessage());
                 if (file != null) {
                     Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                     holder.txtDuration.setVisibility(View.VISIBLE);
                     holder.txtDuration.setText(milliSecondsToTimer(storageUtils.getMediaDuration(mContext, voiceURI)));
                 } else {
                     holder.txtDuration.setVisibility(View.INVISIBLE);
                 }
             } else if (tempViewHolder instanceof ChatAdapter.ReceiveAudioHolder) {
                 ChatAdapter.ReceiveAudioHolder holder = (ChatAdapter.ReceiveAudioHolder) tempViewHolder;
                 holder.songSeekbar.setProgress(0);
                 holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pause_icon_white));
                 File file = storageUtils.getFile(Constants.TAG_AUDIO_RECEIVE, tempMsgData.getMessage());
                 if (file != null) {
                     Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                     holder.txtDuration.setVisibility(View.VISIBLE);
                     holder.txtDuration.setText(milliSecondsToTimer(storageUtils.getMediaDuration(mContext, voiceURI)));
                 } else {
                     holder.txtDuration.setVisibility(View.INVISIBLE);
                 }
             }
         }
     }
 }
*/

    // addon for smart reply
/*
    private void generateReplies(ChatResponse lastMessage) {
        // If the last message in the chat thread is not sent by the "other" user, don't generate
        // smart replies.
        if (lastMessage.getMessageType().equals(Constants.TAG_TEXT) && lastMessage.getMessageEnd().equals(Constants.TAG_RECEIVED) && !lastMessage.getUserId().equals(chatId)) {
            mSmartRepliesRecyler.setVisibility(View.VISIBLE);
            List<TextMessage> chatHistory = new ArrayList<>();
            chatHistory.add(TextMessage.createForRemoteUser(lastMessage.getMessage(),
                    System.currentTimeMillis(), GetSet.getUserId()));

            smartReply.suggestReplies(chatHistory)
                    .addOnSuccessListener(smartReplySuggestionResult -> {
                        if (smartReplySuggestionResult.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                            // The conversation's language isn't supported, so
                            // the result doesn't contain any suggestions.
                            Log.e(TAG, "generateReplies: " + SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE);
                        } else if (smartReplySuggestionResult.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                            // Task completed successfully
                            // ...
                            List<SmartReplySuggestion> suggestion = smartReplySuggestionResult.getSuggestions();
                            Log.i(TAG, "generateRepliess: " + new Gson().toJson(suggestion));
                            mChipAdapter.setSuggestions(suggestion);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "generateRepliesfa: " + e.getMessage());
                }
            });
        } else {
            mSmartRepliesRecyler.setVisibility(View.GONE);
        }
    }
*/

    public String getPartnerId() {
        return partnerId;
    }

    private void sendFileChat(JSONObject jsonObject, String file) {
        try {
            String utcTime = jsonObject.getString(Constants.TAG_CHAT_TIME);
            String messageId = jsonObject.getString(Constants.TAG_MSG_ID);
            String msg = "";
            String type = jsonObject.getString(Constants.TAG_MSG_TYPE);
            switch (type) {
                case Constants.TAG_TEXT:
                    msg = edtMessage.getText().toString();
                    break;
                case Constants.TAG_IMAGE:
                    msg = file;
                    break;
                case Constants.TAG_GIF:
                    msg = file;
                    break;
                case Constants.TAG_AUDIO:
                    msg = file;
                    break;
                case Constants.TAG_VIDEO:
                    msg = getString(R.string.video);
                    break;
            }

            JSONObject json = new JSONObject();
            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            json.put(Constants.TAG_RECEIVER_ID, partnerId);
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_USER_NAME, GetSet.getUserName());
            json.put(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            json.put(Constants.TAG_CHAT_ID, chatId);
            json.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            json.put(Constants.TAG_MSG_ID, messageId);
            json.put(Constants.TAG_MSG_TYPE, type);
            json.put(Constants.TAG_MESSAGE, AppUtils.encryptMessage(msg));
            json.put(Constants.TAG_CHAT_TIME, utcTime);
            Logging.i(TAG, "sendFileChat: " + json);
            AppWebSocket.getInstance(this).send(json.toString());
            dbHelper.updateMessage(messageId, Constants.TAG_PROGRESS, Constants.TAG_COMPLETED);
            recyclerView.smoothScrollToPosition(0);
        } catch (JSONException e) {
            Logging.e(TAG, "sendFileChat: " + e.getMessage());
        }
    }

    private void uploadAudio(byte[] audioBytes, String fileName, JSONObject jsonObject) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/*"), audioBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_CHAT_IMAGE, "audio.mp3", requestFile);
        RequestBody userId = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<Map<String, String>> call3 = apiInterface.uploadAudio(body, userId);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Map<String, String> data = response.body();
                    if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        liveModel.updateImageUpload(jsonObject);
                        sendFileChat(jsonObject, data.get(Constants.TAG_USER_IMAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    public void resumeExternalAudio(Context context) {
        if (AppUtils.isExternalPlay) {
            AppUtils.isExternalPlay = false;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "play");
            context.sendBroadcast(i);
        }
    }

    public void pauseExternalAudio(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            AppUtils.isExternalPlay = true;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            context.sendBroadcast(i);
        }
    }

    private void stopAudio() {
        if (MediaPlayerUtils.isPlaying()) {
            MediaPlayerUtils.mediaPlayer.stop();
            MediaPlayerUtils.mediaPlayer.reset();
        }
    }

    public interface DownloadListener {
        void onDownloading();

        void onDownloaded(Bitmap bitmap);

        void onAudioDownloaded(String filePath);
    }

    public class ChatAdapter extends RecyclerView.Adapter {
        public static final int VIEW_TYPE_SENT_TEXT = 0;
        public static final int VIEW_TYPE_RECEIVE_TEXT = 1;
        public static final int VIEW_TYPE_SENT_IMAGE = 2;
        public static final int VIEW_TYPE_RECEIVE_IMAGE = 3;
        public static final int VIEW_TYPE_FOOTER = 4;
        public static final int VIEW_TYPE_CALL = 8;
        public static final int VIEW_TYPE_DATE = 9;

        public static final int VIEW_TYPE_SENT_AUDIO = 10;
        public static final int VIEW_TYPE_RECEIVE_AUDIO = 11;


        private final Context context;
        RequestOptions requestOptions;
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;


        public ChatAdapter(Context context) {
            this.context = context;
            requestOptions = new RequestOptions().error(R.drawable.rounded_square_semi_transparent)
                    .dontAnimate().diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new BlurTransformation(25, 3));
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_RECEIVE_TEXT) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_text_bubble_receive, parent, false);
                viewHolder = new ReceiveMessageHolder(itemView);
            } else if (viewType == VIEW_TYPE_RECEIVE_IMAGE) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_image_bubble_receive, parent, false);
                viewHolder = new ReceiveImageHolder(itemView);
            } else if (viewType == VIEW_TYPE_SENT_TEXT) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_text_bubble_sent, parent, false);
                viewHolder = new SentMessageHolder(itemView);
            } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_image_bubble_sent, parent, false);
                viewHolder = new SentImageHolder(itemView);
            } else if (viewType == VIEW_TYPE_DATE) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_date_layout, parent, false);
                viewHolder = new DateHolder(itemView);
            } else if (viewType == VIEW_TYPE_CALL) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_call_layout, parent, false);
                viewHolder = new CallHolder(itemView);
            }

            // addon for voice message
           /* else if (viewType == VIEW_TYPE_RECEIVE_AUDIO) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_voice_receive, parent, false);
                viewHolder = new ReceiveAudioHolder(itemView);
            } else if (viewType == VIEW_TYPE_SENT_AUDIO) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_voice_sent, parent, false);
                viewHolder = new SentAudioHolder(itemView);
            }*/

            else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ReceiveMessageHolder) {
                ChatResponse chat = chatList.get(position);
                ((ReceiveMessageHolder) holder).bind(chat);
            } else if (holder instanceof ReceiveImageHolder) {
                ChatResponse chat = chatList.get(position);
                ((ReceiveImageHolder) holder).bind(chat);
            } else if (holder instanceof SentMessageHolder) {
                ChatResponse chat = chatList.get(position);
                ((SentMessageHolder) holder).bind(chat);
            } else if (holder instanceof SentImageHolder) {
                ((SentImageHolder) holder).bind(chatList.get(position));
            } else if (holder instanceof DateHolder) {
                ChatResponse chat = chatList.get(position);
                ((DateHolder) holder).bind(chat);
            } else if (holder instanceof CallHolder) {
                ChatResponse chat = chatList.get(position);
                ((CallHolder) holder).bind(chat);
            }
            // addon for voice message
          /*  else if (holder instanceof SentAudioHolder) {
                ((SentAudioHolder) holder).bind(chatList.get(position));
            } else if (holder instanceof ReceiveAudioHolder) {
                ((ReceiveAudioHolder) holder).bind(chatList.get(position));
            }*/

            else if (holder instanceof ChatFragment.ChatAdapter.FooterViewHolder) {
                ChatFragment.ChatAdapter.FooterViewHolder footerHolder = (ChatFragment.ChatAdapter.FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemViewType(int position) {
            ChatResponse chat = chatList.get(position);
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            else if (chat.getMessageType().equals(Constants.TAG_DATE)) {
                return VIEW_TYPE_DATE;
            } else if (chat.getMessageType().equals(Constants.TAG_MISSED)) {
                return VIEW_TYPE_CALL;
            } else if (chat.getChatType().equals(Constants.TAG_ADMIN) || chat.getMessageEnd().equals(Constants.TAG_RECEIVED)) {
                switch (chat.getMessageType()) {
                    case Constants.TAG_TEXT:
                        return VIEW_TYPE_RECEIVE_TEXT;
                    case Constants.TAG_IMAGE:
                    case Constants.TAG_GIF:
                        return VIEW_TYPE_RECEIVE_IMAGE;

                    // addon for voice message
                    /*case Constants.TAG_AUDIO:
                        return VIEW_TYPE_RECEIVE_AUDIO;*/

                    default:
                        return VIEW_TYPE_RECEIVE_TEXT;
                }
            } else {
                switch (chat.getMessageType()) {
                    case Constants.TAG_TEXT:
                        return VIEW_TYPE_SENT_TEXT;
                    case Constants.TAG_IMAGE:
                    case Constants.TAG_GIF:
                        return VIEW_TYPE_SENT_IMAGE;
                    // addon for voice message
                    /*case Constants.TAG_AUDIO:
                        return VIEW_TYPE_SENT_AUDIO;*/
                    default:
                        return VIEW_TYPE_SENT_TEXT;
                }
            }
        }

        // addon for voice message
/*
        private void startPlayer(Uri voiceURI, int progress, Context context) {
            try {
                AppUtils.pauseExternalAudioChat(mContext);
                MediaPlayerUtils.startAndPlayMediaPlayer(context, voiceURI, ChatActivity.this, progress);
            } catch (IOException e) {
                Log.e(TAG, "startPlayer: " + e.getMessage());
                e.printStackTrace();
            }
        }
*/

/*
        private String milliSecondsToTimers(long milliseconds) {
            String finalTimerString = "";
            String secondsString = "";

            // Convert total duration into time
            int hours = (int) (milliseconds / (1000 * 60 * 60));
            int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
            int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
            // Add hours if there
            if (hours > 0) {
                finalTimerString = hours + ":";
            }

            // Prepending 0 to seconds if it is one digit
            if (seconds < 10) {
                secondsString = "0" + seconds;
            } else {
                secondsString = "" + seconds;
            }

            finalTimerString = finalTimerString + minutes + ":" + secondsString;

            // return timer string
            return finalTimerString;
        }
*/

        @Override
        public int getItemCount() {
            int itemCount = chatList.size();
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

        public void setData(List<ChatResponse> newData) {
            chatList = newData;
            adapter.notifyDataSetChanged();
        }

        /*public void setData(List<ChatResponse> newData) {
            if (chatList == null) {
                chatList = newData;
                notifyItemRangeInserted(0, newData.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return chatList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return newData.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        if ((chatList.get(oldItemPosition).getMessageType().equals(Constants.TAG_DATE) &&
                                newData.get(newItemPosition).getMessageType().equals(Constants.TAG_DATE))) {
                            return true;
                        } else
                            return (chatList.get(oldItemPosition).getMessageId() != null && newData.get(newItemPosition).getMessageId() != null) &&
                                    (chatList.get(oldItemPosition).getMessageId().equals(newData.get(newItemPosition).getMessageId()));
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        if ((chatList.get(oldItemPosition).getMessageType().equals(Constants.TAG_DATE) &&
                                newData.get(newItemPosition).getMessageType().equals(Constants.TAG_DATE))) {
                            return true;
                        } else {
                            return Objects.equals(chatList.get(oldItemPosition).getDeliveryStatus(), newData.get(newItemPosition).getDeliveryStatus());
                        }
                    }
                });
                chatList.clear();
                chatList.addAll(newData);
                result.dispatchUpdatesTo(adapter);
            }
        }*/

        public class DateHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txtMsgTime)
            TextView txtMsgTime;

            public DateHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void bind(ChatResponse chat) {
                txtMsgTime.setText(chat.getChatTime());
            }
        }

        public class CallHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txtMsg)
            TextView txtMsg;

            public CallHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void bind(ChatResponse chat) {
                txtMsg.setText(context.getString(R.string.missed_call_at) + " " +
                        AppUtils.getChatTime(context, chat.getChatTime()));
            }
        }

        public class SentMessageHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txtMessage)
            TextView txtMessage;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            int start = 0;

            public SentMessageHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bind(ChatResponse chat) {
                Spanned spannedText;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    spannedText = Html.fromHtml(chat.getMessage(), Html.FROM_HTML_MODE_LEGACY);
                } else {
                    spannedText = Html.fromHtml(chat.getMessage());
                }
                txtMessage.setText(spannedText);
                txtMessage.setMovementMethod(LinkMovementMethod.getInstance());

                txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));
                userImage.setVisibility(View.GONE);
                ChatResponse chatResponse = dbHelper.getLastReadMessage(chatId);
                if (chatResponse.getMessageId() != null && chatResponse.getMessageId().equals(chat.getMessageId())) {
                    if (chat.getDeliveryStatus().equals(Constants.TAG_READ)) {
                        userImage.setVisibility(View.VISIBLE);
                        Log.d(TAG, "bindPartner: " + Constants.IMAGE_URL + partnerImage);
                        Glide.with(context)
                                .load(Constants.IMAGE_URL + partnerImage)
                                .apply(new RequestOptions().placeholder(R.drawable.avatar).error(R.drawable.avatar))
                                .into(userImage);
                    }
                }
            }

            private ChatResponse getLastSentMessage(String s) {
                for (ChatResponse chatResponse : chatList) {
                    if (chatResponse.getMessageEnd().equals(Constants.TAG_SEND)) {
                        return chatResponse;
                    }
                }
                return null;
            }
        }

        public class SentImageHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.chatImage)
            RoundedImageView chatImage;
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            @BindView(R.id.btnUpload)
            ImageView btnUpload;
            @BindView(R.id.uploadProgress)
            ProgressWheel uploadProgress;
            @BindView(R.id.uploadLay)
            RelativeLayout uploadLay;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;
            @BindView(R.id.itemLay)
            RelativeLayout itemLay;

            @BindView(R.id.gifImage)
            ImageView gifImage;


            public SentImageHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public String getStringFromFile(String filePath) throws Exception {
                File fl = new File(filePath);
                FileInputStream fin = new FileInputStream(fl);
                String ret = convertStreamToString(fin);
                //Make sure you close all streams.
                fin.close();
                return ret;
            }

            public String convertStreamToString(InputStream is) throws Exception {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                return sb.toString();
            }

            public void bind(ChatResponse chat) {
                txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));
                Log.d(TAG, "bindChat: " + chat.getMessage());
                if (chat.getMessageType().equals(Constants.TAG_IMAGE)) {
                    Log.d(TAG, "bindChat: " + chat.getMessage());
                    gifImage.setVisibility(View.GONE);
                    Log.d(TAG, "bindChatImage: " + getAdapterPosition() + ", " + chat.getMessage());

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        if (storageUtils.checkIfImageExistsBelowAndroid11(Constants.TAG_THUMBNAIL, chat.getMessage())) {
                            uploadLay.setVisibility(View.GONE);
                            uploadProgress.stopSpinning();
                            Log.d(TAG, "bindChatMainSS: " + storageUtils.getImage(Constants.TAG_THUMBNAIL, chat.getMessage()));
                            Glide
                                    .with(context)
                                    .load(storageUtils.getImage(Constants.TAG_THUMBNAIL, chat.getMessage()))
                                    .into(chatImage);
                            if (chat.getProgress().equals(Constants.TAG_COMPLETED)) {
                                uploadProgress.stopSpinning();
                                uploadProgress.stopSpinning();
                                uploadLay.setVisibility(View.GONE);
                            } else {
                                btnUpload.setVisibility(View.VISIBLE);
                                uploadProgress.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d(TAG, "bindChatElseSS: " + Constants.CHAT_IMAGE_URL + chat.getMessage());
                            Glide
                                    .with(context)
                                    .load(Constants.CHAT_IMAGE_URL + chat.getMessage())
                                    .into(chatImage);
                            uploadLay.setVisibility(View.GONE);

                        }
                    } else {
                        if (storageUtils.checkIfImageExists(Constants.TAG_SENT, chat.getMessage())) {
                            uploadLay.setVisibility(View.GONE);
                            uploadProgress.stopSpinning();
                            Log.d(TAG, "bindChatMain: " + storageUtils.getImageUri(Constants.TAG_SENT, chat.getMessage()));
                            Glide
                                    .with(context)
                                    // .load(FileProvider.getUriForFile(context,BuildConfig.APPLICATION_ID+".provider",storageUtils.getImage(Constants.TAG_SENT, chat.getMessage())))
                                    .load(storageUtils.getImageUri(Constants.TAG_SENT, chat.getMessage()))
                                    .into(chatImage);


                            if (chat.getProgress().equals(Constants.TAG_COMPLETED)) {
                                uploadProgress.stopSpinning();
                                uploadProgress.stopSpinning();
                                uploadLay.setVisibility(View.GONE);
                            } else {
                                btnUpload.setVisibility(View.VISIBLE);
                                uploadProgress.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d(TAG, "bindChatElse: " + Constants.CHAT_IMAGE_URL + chat.getMessage());
                            Glide
                                    .with(context)
                                    .load(Constants.CHAT_IMAGE_URL + chat.getMessage())
                                    .into(chatImage);
                            uploadLay.setVisibility(View.GONE);

                        }
                    }

                    userImage.setVisibility(View.GONE);
                    ChatResponse chatResponse = dbHelper.getLastReadMessage(chatId);
                    if (chatResponse.getMessageId() != null && chatResponse.getMessageId().equals(chat.getMessageId())) {
                        if (chat.getDeliveryStatus().equals(Constants.TAG_READ)) {
                            userImage.setVisibility(View.VISIBLE);
                            Glide.with(context)
                                    .load(Constants.IMAGE_URL + partnerImage)
                                    .apply(new RequestOptions().placeholder(R.drawable.avatar).error(R.drawable.avatar))
                                    .into(userImage);
                        }
                    }

                    btnUpload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (NetworkReceiver.isConnected()) {
                                if (storageUtils.checkIfImageExists(Constants.TAG_SENT, chat.getMessage())) {
                                    File uploadedFile = storageUtils.getImage(Constants.TAG_SENT, chat.getMessage());
                                    String sentPath = uploadedFile.getAbsolutePath();
                                    BufferedInputStream buf;
                                    try {
                                        int size = (int) uploadedFile.length();
                                        byte[] bytes = new byte[size];
                                        buf = new BufferedInputStream(new FileInputStream(uploadedFile));
                                        buf.read(bytes, 0, bytes.length);
                                        buf.close();
                                        uploadProgress.spin();
                                        uploadChatImage(bytes, sentPath, "", new JSONObject());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                App.makeToast(getString(R.string.no_internet_connection));
                            }
                        }
                    });

                    chatImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                if (storageUtils.checkIfImageExists(Constants.TAG_SENT, chat.getMessage())) {
                                    Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                                    imageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    imageIntent.putExtra(Constants.TAG_USER_IMAGE, chat.getMessage());
                                    imageIntent.putExtra(Constants.TAG_FROM, Constants.TAG_SENT);
                                    imageIntent.putExtra("DeletePic", "No");
                                    startActivity(imageIntent);
                                } else {
                                    Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                                    imageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    imageIntent.putExtra(Constants.TAG_USER_IMAGE, chat.getMessage());
                                    imageIntent.putExtra(Constants.TAG_FROM, Constants.TAG_SENT);
                                    imageIntent.putExtra("DeletePic", "yes");
                                    startActivity(imageIntent);
                                }
                            } else {
                                if (storageUtils.checkIfImageExistsBelowAndroid11(Constants.TAG_SENT, chat.getMessage())) {
                                    Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                                    imageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    imageIntent.putExtra(Constants.TAG_USER_IMAGE, chat.getMessage());
                                    imageIntent.putExtra(Constants.TAG_FROM, Constants.TAG_SENT);
                                    imageIntent.putExtra("DeletePic", "No");
                                    startActivity(imageIntent);
                                } else {
                                    Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                                    imageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    imageIntent.putExtra(Constants.TAG_USER_IMAGE, chat.getMessage());
                                    imageIntent.putExtra(Constants.TAG_FROM, Constants.TAG_SENT);
                                    imageIntent.putExtra("DeletePic", "yes");
                                    startActivity(imageIntent);
                                }
                            }
                        }
                    });

                } else {
                    btnUpload.setVisibility(View.GONE);
                    txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));
                    gifImage.setVisibility(View.VISIBLE);
                    chatImage.setVisibility(View.GONE);
                    Glide
                            .with(context)
                            .asGif()
                            .load(chat.getMessage())
                            .into(gifImage);


                    gifImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                            imageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            imageIntent.putExtra(Constants.TAG_USER_IMAGE, chat.getMessage());
                            imageIntent.putExtra(Constants.TAG_FROM, Constants.TAG_GIF);
                            startActivity(imageIntent);
                        }
                    });
                }

            }
        }

        public class ReceiveMessageHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            @BindView(R.id.txtMessage)
            TextView txtMessage;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;

            @BindView(R.id.translatebtn)
            TextView translatebtn;

            public ReceiveMessageHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bind(ChatResponse chat) {
                txtMessage.setText(chat.getMessage());
                txtMessage.setMovementMethod(LinkMovementMethod.getInstance());
                txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));
                if (isAdminChat) {
                    userImage.setVisibility(View.GONE);

                    translatebtn.setVisibility(View.GONE);
                } else {
                    Glide.with(context)
                            .load(Constants.IMAGE_URL + partnerImage)
                            .apply(new RequestOptions().placeholder(R.drawable.avatar).error(R.drawable.avatar))
                            .into(userImage);
                    userImage.setVisibility(View.VISIBLE);

                    if (SharedPref.getString(SharedPref.CHAT_LANGUAGE, SharedPref.DEFAULT_CHAT_LANGUAGE).equals("")) {
                        translatebtn.setVisibility(View.GONE);
                    } else {
                        translatebtn.setVisibility(View.VISIBLE);
                    }
                }
/*
                translatebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        new TranslateLanguageTasks(ChatActivity.this,chat.getMessage(),true){

                            @Override
                            protected void onPostExecute(String translatedMsg) {
                                chat.setMessage(translatedMsg);
                                notifyItemChanged(getAdapterPosition());

                            }
                        }.execute();

                    }
                });
*/
            }
        }
/*
        public class SentAudioHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.btnUpload)
            ImageView btnUpload;
            @BindView(R.id.song_seekbar)
            SeekBar songSeekbar;
            @BindView(R.id.txtFileName)
            TextView txtFileName;
            @BindView(R.id.txtDuration)
            TextView txtDuration;
            @BindView(R.id.uploadProgress)
            ProgressWheel uploadProgress;
            @BindView(R.id.btnPlay)
            ImageView btnPlay;
            @BindView(R.id.contentLay)
            RelativeLayout contentLay;
            @BindView(R.id.uploadLay)
            FrameLayout uploadLay;
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            @BindView(R.id.itemLay)
            RelativeLayout itemLay;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;

            public SentAudioHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bind(ChatResponse chat) {
                try {


                    txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));
                    if (storageUtils.checkIfFileExists(Constants.TAG_AUDIO_SENT, chat.getMessage())) {
                        uploadLay.setVisibility(View.GONE);
                        uploadProgress.stopSpinning();
                        if (chat.getProgress().equals(Constants.TAG_COMPLETED)) {
                            uploadProgress.stopSpinning();
                            uploadLay.setVisibility(View.GONE);
                        } else {
                            btnUpload.setVisibility(View.VISIBLE);
                            uploadProgress.setVisibility(View.VISIBLE);
                        }
                    }
                    userImage.setVisibility(View.GONE);
                    ChatResponse chatResponse = dbHelper.getLastReadMessage(chatId);
                    if (chatResponse.getMessageId() != null && chatResponse.getMessageId().equals(chat.getMessageId())) {
                        if (chat.getDeliveryStatus().equals(Constants.TAG_READ)) {
                            userImage.setVisibility(View.VISIBLE);
                            Glide.with(context)
                                    .load(Constants.IMAGE_URL + partnerImage)
                                    .apply(new RequestOptions().placeholder(R.drawable.avatar).error(R.drawable.avatar))
                                    .into(userImage);
                        }
                    }

                    if (currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                        if (MediaPlayerUtils.isPlaying()) {
                            btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.play_icon_white));
                            seekBar.setMax(MediaPlayerUtils.getTotalDuration());
                            seekBar.setProgress(MediaPlayerUtils.getCurrentDuration());
                        } else {
                            btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.pause_icon_white));
                            File file = storageUtils.getFile(Constants.TAG_AUDIO_SENT, chat.getMessage());
                            if (file != null) {
                                Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                                long totalDuration = storageUtils.getMediaDuration(mContext, voiceURI);
                                txtDuration.setText(milliSecondsToTimers(totalDuration));
                            }
                        }
                        songSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                    btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_icon_white));
                                    MediaPlayerUtils.applySeekBarValue(progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                if (MediaPlayerUtils.isPlaying() && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                    btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon_white));
                                }
                            }
                        });
                    } else {
                        btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.pause_icon_white));
                        songSeekbar.setProgress(0);
                        File file = storageUtils.getFile(Constants.TAG_AUDIO_SENT, chat.getMessage());
                        if (file != null) {
                            Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                            long totalDuration = storageUtils.getMediaDuration(context, voiceURI);
                            songSeekbar.setMax((int) totalDuration);
                            txtDuration.setText(milliSecondsToTimers(totalDuration));
                            txtDuration.setVisibility(View.VISIBLE);
                            songSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                    if (fromUser && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                        btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_icon_white));
                                        MediaPlayerUtils.applySeekBarValue(progress);
                                    }
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                    if (MediaPlayerUtils.isPlaying() && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                        btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon_white));
                                    }
                                }
                            });
                        } else {
                            songSeekbar.setMax(0);
                            txtDuration.setVisibility(View.GONE);
                        }
                    }




                    songSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_icon_white));
                                MediaPlayerUtils.applySeekBarValue(progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            if (MediaPlayerUtils.isPlaying() && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon_white));
                            }
                        }
                    });



                        btnPlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (chatVoicePressed == true) {
                                    File file = storageUtils.getFile(Constants.TAG_AUDIO_SENT, chat.getMessage());
                                    if (file != null) {
                                        txtDuration.setVisibility(View.VISIBLE);
                                        if (currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                            if (MediaPlayerUtils.isPlaying()) {
                                                btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_icon_white));
                                                MediaPlayerUtils.pauseMediaPlayer();
                                            } else {
                                                Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                                                songSeekbar.setMax(storageUtils.getMediaDuration(context, voiceURI));
                                                btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon_white));
                                                startPlayer(voiceURI, songSeekbar.getProgress(), context);
                                            }
                                        } else {
                                            stopAudioViewHolder();
                                            currentMessageId = chat.getMessageId();
                                            Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                                            songSeekbar.setMax(storageUtils.getMediaDuration(context, voiceURI));
                                            btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon_white));
                                            startPlayer(voiceURI, songSeekbar.getProgress(), context);
                                        }
                                    } else {
                                        Toast.makeText(context, getString(R.string.no), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
*/



/*
        public class ReceiveAudioHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            @BindView(R.id.btnDownload)
            ImageView btnDownload;
            @BindView(R.id.song_seekbar)
            SeekBar songSeekbar;
            @BindView(R.id.txtFileName)
            TextView txtFileName;
            @BindView(R.id.txtDuration)
            TextView txtDuration;
            @BindView(R.id.downloadProgress)
            ProgressWheel downloadProgress;
            @BindView(R.id.btnPlay)
            ImageView btnPlay;
            @BindView(R.id.contentLay)
            RelativeLayout contentLay;
            @BindView(R.id.downloadLay)
            FrameLayout downloadLay;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;
            @BindView(R.id.itemLay)
            RelativeLayout itemLay;

            public ReceiveAudioHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bind(ChatResponse chat) {
                try {

                    Glide.with(context)
                            .load(Constants.IMAGE_URL + partnerImage)
                            .apply(new RequestOptions().placeholder(R.drawable.avatar).error(R.drawable.avatar))
                            .into(userImage);
                    userImage.setVisibility(View.VISIBLE);
                    txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));


                    btnDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                    WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE},
                                        Constants.DOWNLOAD_REQUEST_CODE);
                            } else {
                                if (NetworkReceiver.isConnected()) {
                                    downloadProgress.spin();
                                    btnDownload.setVisibility(View.GONE);
                                    DownloadAudio downloadImage = new DownloadAudio(chat.getMessage(), new DownloadListener() {
                                        @Override
                                        public void onDownloading() {

                                        }

                                        @Override
                                        public void onDownloaded(Bitmap bitmap) {

                                        }

                                        @Override
                                        public void onAudioDownloaded(String filePath) {
                                            downloadProgress.stopSpinning();
                                            downloadProgress.setVisibility(View.GONE);
                                            txtDuration.setVisibility(View.VISIBLE);
                                            txtDuration.setText("0:00");
                                            btnDownload.setVisibility(View.GONE);
                                            btnPlay.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    downloadImage.execute();
                                } else {
                                    App.makeToast(getString(R.string.no_internet_connection));
                                }
                            }
                        }
                    });


                    if (currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                        if (MediaPlayerUtils.isPlaying()) {
                            btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.play_icon_white));
                            songSeekbar.setMax(MediaPlayerUtils.getTotalDuration());
                            songSeekbar.setProgress(MediaPlayerUtils.getCurrentDuration());
                        } else {
                            btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.pause_icon_white));
                            File file = storageUtils.getFile(Constants.TAG_AUDIO_RECEIVE, chat.getMessage());
                            if (file != null) {
                                Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                                long totalDuration = storageUtils.getMediaDuration(mContext, voiceURI);
                                txtDuration.setText(milliSecondsToTimer(totalDuration));
                            }
                        }
                        songSeekbar.setEnabled(true);
                        songSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                    btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_icon_white));
                                    MediaPlayerUtils.applySeekBarValue(progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                if (MediaPlayerUtils.isPlaying() && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                    btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon_white));
                                }
                            }
                        });
                    } else {
                        btnPlay.setImageResource(R.drawable.pause_icon_white);
//                    songSeekbar.getProgressDrawable().setColorFilter(ContextCompat.getColor(context, R.color.colorWhiteText), PorterDuff.Mode.MULTIPLY);
                        File file = storageUtils.getFile(Constants.TAG_AUDIO_RECEIVE, chat.getMessage());
                        btnPlay.setImageResource(R.drawable.pause_icon_white);
//                    songSeekbar.getProgressDrawable().setColorFilter(ContextCompat.getColor(context, R.color.colorWhiteText), PorterDuff.Mode.MULTIPLY);
                        if (storageUtils.checkIfFileExists(Constants.TAG_AUDIO_RECEIVE, chat.getMessage())) {
                            downloadProgress.setVisibility(View.GONE);
                            btnDownload.setVisibility(View.GONE);
                            btnPlay.setVisibility(View.VISIBLE);
                            downloadProgress.stopSpinning();
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(file.getAbsolutePath());
                            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            txtDuration.setVisibility(View.VISIBLE);
                            if (durationStr != null) {
                                txtDuration.setText(AppUtils.formateMilliSeccond(Long.parseLong(durationStr)));
                            } else {
                                txtDuration.setText("0:00");
                            }
                            int duration = Integer.parseInt(durationStr);

                            songSeekbar.setProgress(0);
                            songSeekbar.setMax((int) duration);
                            //  txtDuration.setText("0:00");
                            mmr.release();

                            btnPlay.setEnabled(true);
                            songSeekbar.setEnabled(true);
                            songSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    if (fromUser && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                        btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_icon_white));
                                        MediaPlayerUtils.applySeekBarValue(progress);
                                    }
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                    if (MediaPlayerUtils.isPlaying() && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                        btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon_white));
                                    }
                                }
                            });

                        } else {
                            downloadProgress.setVisibility(View.VISIBLE);
                            downloadProgress.stopSpinning();
                            btnDownload.setVisibility(View.VISIBLE);
                            btnPlay.setVisibility(View.GONE);
                            txtDuration.setVisibility(View.GONE);
                            btnPlay.setVisibility(View.GONE);
                        }

                        songSeekbar.setProgress(0);
                    }

                    btnPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (chatVoicePressed == true) {
                                File file = storageUtils.getFile(Constants.TAG_AUDIO_RECEIVE, chat.getMessage());
                                if (file != null) {
                                    txtDuration.setVisibility(View.VISIBLE);
                                    if (currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                        if (MediaPlayerUtils.isPlaying()) {
                                            btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_icon_white));
                                            MediaPlayerUtils.pauseMediaPlayer();
                                        } else {
                                            Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                                            songSeekbar.setMax(storageUtils.getMediaDuration(context, voiceURI));
                                            btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon_white));
                                            startPlayer(voiceURI, songSeekbar.getProgress(), context);
                                        }
                                    } else {
                                        stopAudioViewHolder();
                                        currentMessageId = chat.getMessageId();
                                        Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                                        songSeekbar.setMax(storageUtils.getMediaDuration(context, voiceURI));
                                        btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon_white));
                                        startPlayer(voiceURI, songSeekbar.getProgress(), context);
                                    }
                                } else {
                                    Toast.makeText(context, getString(R.string.no), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
*/


        @SuppressLint("StaticFieldLeak")
        private class DownloadAudio extends AsyncTask<String, Integer, String> {
            String filePath, fileurl;
            String fileName;
            int fileLength;
            DownloadListener downloadListener;

            public DownloadAudio(String fileurl, DownloadListener downloadListener) {
                this.fileurl = fileurl;
                this.downloadListener = downloadListener;
            }

            protected String doInBackground(String... urls) {
                String imageURL = Constants.CHAT_IMAGE_URL + fileurl;
                Log.i(TAG, "doInBackground: " + imageURL);
                fileName = getFileName(imageURL);
                storageUtils.ifFolderExists(Constants.TAG_AUDIO_RECEIVE);
                filePath = storageUtils.getFile(Constants.TAG_AUDIO_RECEIVE, fileName).getAbsolutePath();
                try {
                    BufferedInputStream in = new BufferedInputStream(new URL(imageURL).openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                    byte dataBuffer[] = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }

                } catch (Exception e) {
                    Log.e("Error Message", e.getMessage());
                    e.printStackTrace();
                }
                return filePath;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                Log.e(TAG, "onProgressUpdate: " + values[0]);
            }

            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                downloadListener.onAudioDownloaded(result);
            }
        }

        public class ReceiveImageHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            @BindView(R.id.chatImage)
            RoundedImageView chatImage;
            @BindView(R.id.btnDownload)
            ImageView btnDownload;
            @BindView(R.id.downloadProgress)
            ProgressWheel downloadProgress;
            @BindView(R.id.downloadLay)
            FrameLayout downloadLay;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;

            @BindView(R.id.gifImage)
            ImageView gifImage;

            public ReceiveImageHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bind(ChatResponse chat) {
                if (isAdminChat) {
                    userImage.setVisibility(View.GONE);
                } else {
                    Glide.with(context)
                            .load(Constants.IMAGE_URL + partnerImage)
                            .apply(new RequestOptions().placeholder(R.drawable.avatar).error(R.drawable.avatar))
                            .into(userImage);
                    userImage.setVisibility(View.VISIBLE);

                }

                downloadLay.setVisibility(View.VISIBLE);
                downloadLay.setVisibility(View.VISIBLE);
                downloadProgress.setVisibility(View.VISIBLE);
                chatImage.setVisibility(View.VISIBLE);
                if (chat.getMessageType().equals(Constants.TAG_IMAGE)) {
                    gifImage.setVisibility(View.GONE);
                    chatImage.setVisibility(View.VISIBLE);

                    txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));

                    Log.d(TAG, "bindImageHolder: " + storageUtils.checkIfImageExists(Constants.TAG_RECEIVED, chat.getMessage() + Constants.IMAGE_EXTENSION));

                    if (storageUtils.checkIfImageExists(Constants.TAG_RECEIVED, chat.getMessage() + Constants.IMAGE_EXTENSION)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Uri file = storageUtils.getImageUri(Constants.TAG_RECEIVED, chat.getMessage() + Constants.IMAGE_EXTENSION);
                            Glide
                                    .with(context)
                                    .load(file)
                                    .into(chatImage);
                        } else {
                            File file = storageUtils.getImage(Constants.TAG_RECEIVED, chat.getMessage() + Constants.IMAGE_EXTENSION);
                            Glide
                                    .with(context)
                                    .load(file)
                                    .into(chatImage);
                        }
                        downloadLay.setVisibility(View.GONE);
                        btnDownload.setVisibility(View.GONE);
                        downloadProgress.setVisibility(View.GONE);

                    } else {
                        downloadLay.setVisibility(View.VISIBLE);
                        btnDownload.setVisibility(View.VISIBLE);
                        downloadProgress.setVisibility(View.VISIBLE);
                        // pass the request as a a parameter to thediskcache thumbnail request
                        Glide
                                .with(context)
                                .asBitmap()
                                .load(Constants.CHAT_IMAGE_URL + chat.getMessage())
                                .thumbnail(0.1f)
                                .apply(requestOptions)
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//                                    storageUtils.saveToSDCard(resource, Constants.TAG_THUMBNAIL, chat.getAttachment() + Constants.IMAGE_EXTENSION);
                                        chatImage.setImageBitmap(resource);
                                        return false;
                                    }
                                }).into(chatImage);
                    }

                    btnDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, Constants.DOWNLOAD_REQUEST_CODE);
                            } else {
                                if (NetworkReceiver.isConnected()) {
                                    downloadProgress.spin();
                                    btnDownload.setVisibility(View.GONE);
                             /*   DownloadImage downloadImage = new DownloadImage(chat.getMessage(), new DownloadListener() {
                                    @Override
                                    public void onDownloading() {

                                    }

                                    @Override
                                    public void onDownloaded(Bitmap bitmap) {
                                        Glide.with(context)
                                                .load(bitmap)
                                                .into(chatImage);
                                        downloadProgress.stopSpinning();
                                        downloadLay.setVisibility(View.GONE);
                                    }
                                    @Override
                                    public void onAudioDownloaded(String filePath) {

                                    }
                                });
                                downloadImage.execute();*/
                                    ImageDownload imageDownloader = new ImageDownload(ChatActivity.this) {
                                        @Override
                                        protected void onPostExecute(Bitmap imgBitmap) {
                                            if (imgBitmap == null) {
//                                            Toast.makeText(mContext, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                            } else {
                                                try {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                        Uri imageUri = storageUtils.getImageUri(Constants.TAG_RECEIVED, chat.getMessage() + Constants.IMAGE_EXTENSION);
                                                        if (imageUri != null) {
                                                            Glide.with(context).load(imageUri).thumbnail(0.5f)
                                                                    .into(chatImage);
                                                            downloadProgress.stopSpinning();
                                                            downloadLay.setVisibility(View.GONE);

                                                        } //                                                    Toast.makeText(mContext, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();

                                                    } else {
                                                        File imageFile = storageUtils.getImage(Constants.TAG_RECEIVED, chat.getMessage() + Constants.IMAGE_EXTENSION);
                                                        if (imageFile != null && imageFile.exists()) {
                                                            Glide.with(context).load(imageFile).thumbnail(0.5f)
                                                                    .into(chatImage);
                                                            downloadProgress.stopSpinning();
                                                            downloadLay.setVisibility(View.GONE);

                                                        } //                                                    Toast.makeText(mContext, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();


                                                    }
                                                } catch (NullPointerException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                        @Override
                                        protected void onProgressUpdate(String... progress) {

                                        }
                                    };
                                    imageDownloader.execute(Constants.CHAT_IMAGE_URL + chat.getMessage(), Constants.TAG_IMAGE);
                                    Log.d(TAG, "onClickImage: " + Constants.CHAT_IMAGE_URL + chat.getMessage());
                                } else {
                                    App.makeToast(getString(R.string.no_internet_connection));
                                }
                            }
                        }
                    });

                    chatImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (storageUtils.checkIfImageExists(Constants.TAG_RECEIVED, chat.getMessage() + Constants.IMAGE_EXTENSION)) {
                                Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                                imageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                Log.d(TAG, "onClickImage: " + chat.getMessage() + Constants.IMAGE_EXTENSION);
                                imageIntent.putExtra(Constants.TAG_USER_IMAGE, chat.getMessage() + Constants.IMAGE_EXTENSION);
                                imageIntent.putExtra(Constants.TAG_FROM, Constants.TAG_RECEIVED);
                                startActivity(imageIntent);
                            } else {
                                btnDownload.performClick();
                            }
                        }
                    });


                } else if (chat.getMessageType().equals(Constants.TAG_GIF)) {
                    chatImage.setVisibility(View.GONE);
                    gifImage.setVisibility(View.VISIBLE);

                    txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));
                    Glide
                            .with(context)
                            .asGif()
                            .load(chat.getMessage())
                            .into(gifImage);
                   /* downloadProgress.stopSpinning();
                    downloadLay.setVisibility(View.GONE);*/

                    gifImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                            imageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            imageIntent.putExtra(Constants.TAG_USER_IMAGE, chat.getMessage());
                            imageIntent.putExtra(Constants.TAG_FROM, Constants.TAG_GIF);
                            startActivity(imageIntent);
                        }
                    });

                } else {

                    chatImage.setVisibility(View.GONE);
                    gifImage.setVisibility(View.GONE);
                    downloadLay.setVisibility(View.GONE);
                    downloadProgress.setVisibility(View.GONE);
                    btnDownload.setVisibility(View.GONE);

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

    class PostDiffCallback extends DiffUtil.Callback {

        private final List<ChatResponse> oldPosts, newPosts;
        private ChatResponse temp;

        public PostDiffCallback(List<ChatResponse> oldPosts, List<ChatResponse> newPosts) {
            this.oldPosts = oldPosts;
            this.newPosts = newPosts;
        }

        @Override
        public int getOldListSize() {
            return oldPosts.size();
        }

        @Override
        public int getNewListSize() {
            return newPosts.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            if ((oldPosts.get(oldItemPosition).getMessageType().equals(Constants.TAG_DATE) &&
                    newPosts.get(newItemPosition).getMessageType().equals(Constants.TAG_DATE))) {
                return true;
            } else {
                return oldPosts.get(oldItemPosition).getMessageId().equals(newPosts.get(newItemPosition).getMessageId());
            }
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            if ((oldPosts.get(oldItemPosition).getMessageType().equals(Constants.TAG_DATE) &&
                    newPosts.get(newItemPosition).getMessageType().equals(Constants.TAG_DATE))) {
                return true;
            } else {
                ChatResponse newProduct = newPosts.get(newItemPosition);
                ChatResponse oldProduct = oldPosts.get(oldItemPosition);
                return newProduct.getMessageId().equals(oldProduct.getMessageId()) &&
                        newProduct.getDeliveryStatus().equals(oldProduct.getDeliveryStatus());


            }
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            newPosts.get(newItemPosition).setDeliveryStatus(Constants.TAG_READ);
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadImage extends AsyncTask<String, Integer, Bitmap> {
        String imagePath;
        int orientation = 0;
        String fileName;
        int fileLength;
        DownloadListener downloadListener;
        private Bitmap rotatedBitmap;

        public DownloadImage(String imagePath, DownloadListener downloadListener) {
            this.imagePath = imagePath;
            this.downloadListener = downloadListener;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = Constants.CHAT_IMAGE_URL + imagePath;
            Bitmap bitmap = null;
            fileName = getFileName(imageURL);
            rotatedBitmap = null;
            try {
                try {
                    InputStream inputStream = new URL(imageURL).openStream();
                    /*ExifInterface exif = null;     //Since API Level 5
                    try {
                        exif = new ExifInterface(inputStream);
                        orientation = exif.getRotationDegrees();
                        Logging.e(TAG, " orientation " + orientation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap == null) {
                        return null;
                    } else {
//                        rotatedBitmap = rotateImage(bitmap, orientation);
                        rotatedBitmap = bitmap;
                        storageUtils.saveToSDCard(rotatedBitmap, Constants.TAG_RECEIVED, fileName + Constants.IMAGE_EXTENSION);
                    }

                } catch (Exception e) {
                    Logging.e("Error Message", e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return rotatedBitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.e(TAG, "onProgressUpdate: " + values[0]);
        }

        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            downloadListener.onDownloaded(result);
        }
    }

/*
    private void startVoice() {
        btnAdd.setVisibility(View.GONE);
        if (visible) {
            TransitionManager.beginDelayedTransition(bottomLay);
            visible = !visible;
        }
  */
/*      if(visible){
            androidx.transition.TransitionManager.beginDelayedTransition(bottomLay);
            visible = !visible;
            attachLay.setVisibility(visible ? View.VISIBLE : View.GONE);
            btnAdd.setVisibility(View.GONE);
        }*//*

        stopMedia();
        recordView.setVisibility(View.VISIBLE);
        edtMsgLay.setVisibility(View.GONE);
        btnAdd.setVisibility(View.GONE);

        if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(ChatActivity.this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, 111);

        } else if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(ChatActivity.this, findViewById(R.id.parentLay), false);
        } else {
            String fileName = (getString(R.string.app_name) + "_" + System.currentTimeMillis() + Constants.AUDIO_EXTENSION).replaceAll(" ", "");
            recordVoicePath = storageUtils.getPath(Constants.TAG_AUDIO_SENT) + "/" + fileName;
            MediaRecorderReady();
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (Exception e) {
                Log.e(TAG, "startVoice: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
*/

/*
    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(recordVoicePath);
    }
*/


/*
    private String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }
*/


/*
    void stopMedia() {
        if (player != null) {
            if (player.isPlaying() && playingPosition != -1) {
                View itemView;
                itemView = mLayoutManager.findViewByPosition(playingPosition);
                ImageView play = itemView.findViewById(R.id.btnPlay);
                TextView time = itemView.findViewById(R.id.txtDuration);
                SeekBar seek = itemView.findViewById(R.id.song_seekbar);
                time.setText(milliSecondsToTimer(player.getDuration()));
                seek.setProgress(0);
                seek = null;
                play.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_play_white));
                player.pause();
            }
            player.stop();
            player.reset();
            playingPosition = -1;
        }
        resumeExternalAudio(this);
    }
*/


    // addon for voice message
/*
    private void setVoiceRecorder() {

        recordView.setCounterTimeColor(Color.parseColor("#a3a3a3"));
        recordView.setSmallMicColor(ContextCompat.getColor(ChatActivity.this, R.color.colorAccent));
        recordView.setSlideToCancelTextColor(Color.parseColor("#a3a3a3"));
        recordView.setLessThanSecondAllowed(false);
        recordView.setSlideToCancelText(getString(R.string.slide_to_cancel));
        recordView.setCustomSounds(0, 0, 0);

        btnRecord.setOnRecordClickListener(new OnRecordClickListener() {
            @Override
            public void onClick(View v) {





            }
        });

        btnRecord.setRecordView(recordView);

        */
/*if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(ChatActivity.this, findViewById(R.id.parentLay), false);
        } else if (blockStatus == null || blockStatus.equals(Constants.TAG_FALSE)) {
            setRecordListener();
        }*//*

        setRecordListener();
    }
*/


/*
    private void setRecordListener() {
        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                edtMsgLay.setVisibility(View.VISIBLE);
                // edtMessage.setHint("");
                attachLay.setVisibility(View.GONE);
                btnAdd.setVisibility(View.GONE);
                setToolBarClick(false);
                stopAudioViewHolderChat();
                if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(ChatActivity.this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    recordView.setOnRecordListener(null);
                    btnAdd.setVisibility(View.VISIBLE);
                    // edtMsgLay.setVisibility(View.VISIBLE);
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, 111);
                } else {
                    if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                        App.makeToast(getString(R.string.unblock_description));
                    } else {
                        sliderInterface.lock();
                        startVoice();
                    }
                }
            }

            @Override
            public void onCancel() {
                edtMessage.requestFocus();
                sliderInterface.unlock();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AppUtils.hideKeyboard(ChatActivity.this);
                        edtMessage.requestFocus();
                        edtMsgLay.setVisibility(View.VISIBLE);
                        btnAdd.setVisibility(View.VISIBLE);
                        recordView.setVisibility(View.GONE);
                        setToolBarClick(true);
                    }
                }, 1000);

            }

            @Override
            public void onFinish(long recordTime) {
                sliderInterface.unlock();
                if (recordTime > 1000) {
                    edtMessage.requestFocus();
                    if (!NetworkReceiver.isConnected()) {
                        AppUtils.showSnack(ChatActivity.this, findViewById(R.id.parentLay), false);
                    } else if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                        Log.i(TAG, "onFinish: ");
                        if (null != mediaRecorder) {
                            try {
                                mediaRecorder.stop();
                            } catch (RuntimeException ignored) {
                            }
                        }
                        edtMsgLay.setVisibility(View.VISIBLE);
                        btnAdd.setVisibility(View.GONE);
                        recordView.setVisibility(View.GONE);
                    }else {
                        if (null != mediaRecorder) {
                            try {
                                mediaRecorder.stop();
                            } catch (RuntimeException ignored) {
                            }
                        }

                        edtMsgLay.setVisibility(View.VISIBLE);
                        btnAdd.setVisibility(View.VISIBLE);
                        recordView.setVisibility(View.GONE);
                        setToolBarClick(true);
                        String fileName = getFileName(recordVoicePath);
                        JSONObject jsonObject = updateDB(Constants.TAG_AUDIO, fileName, recordVoicePath, "", Constants.TAG_SENDING);
                        uploadAudio(storageUtils.getFileBytes(recordVoicePath), fileName, jsonObject);
                    }
                } else {
                    stopMedia();
                    edtMsgLay.setVisibility(View.VISIBLE);
                    btnAdd.setVisibility(View.VISIBLE);
                    recordView.setVisibility(View.GONE);
                    setToolBarClick(true);
                    AppUtils.hideKeyboard(ChatActivity.this);
                    App.makeToast(getString(R.string.audio_length_error));
                }


            }

            @Override
            public void onLessThanSecond() {
                stopMedia();
                edtMsgLay.setVisibility(View.VISIBLE);
                btnAdd.setVisibility(View.VISIBLE);
                recordView.setVisibility(View.GONE);
                setToolBarClick(true);
                AppUtils.hideKeyboard(ChatActivity.this);
                App.makeToast(getString(R.string.audio_length_error));
            }
        });
    }
*/

    /*
        private void setToolBarClick(boolean value){
            Log.i(TAG, "setToolBarClick: "+value);
            btnAudioCall.setEnabled(value);
            btnVideoCall.setEnabled(value);
            btnLanguage.setEnabled(value);
            profileLayout.setEnabled(value);
            recyclerView.setClickable(value);
            recyclerView.setEnabled(value);
            framebg.setEnabled(value);
            framebg.setClickable(value);
            parentLay.setClickable(value);
            parentLay.setEnabled(value);
            chatVoicePressed = value;

        }
    */
    private class ImageDownload extends AsyncTask<String, String, Bitmap> {
        int orientation = 0;
        private Context context;
        private String TAG = "ImageDownloader";
        private Bitmap rotatedBitmap;

        public ImageDownload(Context context) {
            this.context = context;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            if (strings.length == 0 || strings[0] == null) {
                return null;
            }


            InputStream inputStream = getInputStream(strings[0]);
            ExifInterface exif = null;     //Since API Level 5
            try {
                if (inputStream != null) {
                    exif = new ExifInterface(inputStream);
                    orientation = exif.getRotationDegrees();
                }
                Log.e(TAG, " orientation " + orientation);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //load image directly
            Bitmap image = downloadImage(strings[0]);

            Bitmap rotatedBitmap = null;
            if (image == null) {
                return null;
            } else {
                try {
                    final StorageUtils imageStorage = StorageUtils.getInstance(context);
                    String filename = getFileName(strings[0]);
                    rotatedBitmap = rotateImage(image, orientation);
                    Log.d(TAG, "doInBackground: " + rotatedBitmap);
                    imageStorage.saveToSDCard(rotatedBitmap, Constants.TAG_RECEIVED, filename + Constants.IMAGE_EXTENSION);

//                    imageStorage.saveToSDCard(rotatedBitmap, strings[1], filename);
                    return rotatedBitmap;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Log.e(TAG, "doInBackground: " + e.getMessage());
                }
            }
            return null;
        }

        private Bitmap getRotatedBitmap(Bitmap image) {
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(image, 90);

                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(image, 180);

                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(image, 270);

                default:
                    return image;
            }
        }


        public boolean setOrientation(Uri fileUri, int orientation, Context context) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.ORIENTATION, orientation);
            int rowsUpdated = context.getContentResolver().update(fileUri, values, null, null);
            return rowsUpdated > 0;
        }

        Bitmap rotateImage(Bitmap source, float angle) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                    matrix, true);
        }

        public Bitmap getBitmapFromURL(String src) {
            try (InputStream is = new URL(src).openStream()) {
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                return bitmap;
            } catch (Exception ignored) {

            }
            return null;
        }

        public Bitmap downloadImage(String src) {
            Log.d(TAG, "downloadImage: " + src);
            try {
                Bitmap bitmap = Glide
                        .with(context)
                        .asBitmap()
                        .load(src)
                        .submit()
                        .get();
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        public InputStream getInputStream(String src) {
            InputStream inputStream = null;
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                inputStream = connection.getInputStream();
                return inputStream;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String getFileName(String url) {
            String imgSplit = url;
            int endIndex = imgSplit.lastIndexOf("/");
            if (endIndex != -1) {
                imgSplit = imgSplit.substring(endIndex + 1, imgSplit.length());
            }
            return imgSplit;
        }

    }
}
