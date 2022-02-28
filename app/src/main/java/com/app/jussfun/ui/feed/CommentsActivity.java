package com.app.jussfun.ui.feed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.base.BaseActivity;
import com.app.jussfun.external.LinkEllipseTextView;
import com.app.jussfun.external.ProgressWheel;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.callback.FeedListener;
import com.app.jussfun.helper.callback.ResponseJsonClass;
import com.app.jussfun.model.CommentsModel;
import com.app.jussfun.model.Feeds;
import com.app.jussfun.model.FeedsModel;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.ui.MainActivity;
import com.app.jussfun.ui.MyProfileActivity;
import com.app.jussfun.ui.OthersProfileActivity;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsActivity extends BaseActivity implements View.OnClickListener, ResponseJsonClass.onAddCommentCallback, ResponseJsonClass.onHashTagCallback, FeedListener {

    private static final String TAG = CommentsActivity.class.getSimpleName();
    private NestedScrollView nestedScrollView;
    ImageView addcomment, closereply, userImage, iconNoData, btnBack;
    MultiAutoCompleteTextView commentEdit;
    //    SuggestAdapter suggestAdapter;
    TextView replyToUsername, txtNoData, txtTitle;
    ConstraintLayout descriptionLay;
    LinkEllipseTextView txtDescription;
    RelativeLayout reply_postlayout, commentLay, nullLay;
    LinearLayout parentLay, bottomlayout;
    ProgressWheel pgsBar;
    private RecyclerView mRecyclerView;

    private ArrayList<CommentsModel.Result> commentList = new ArrayList<>();
    private CommentsAdapter commentAdapter;
    /// for pagination
    private boolean isLoading, isScroll = false;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private int offsetCnt = 0, limitCnt = 40;                 // for webservice param

    private boolean isReply = false;
    int parentPosition, childPosition;                   // variabls for Reply comment
    String parentId = "", userName = "";                     // variabls for Reply comment
    AppUtils utils;                                         // For call showsoftkeyboard method

    String postId = "", postOwnerId = "", type = Constants.TAG_POST, postOwnerName, postOwnerImage, postDescription;
    private int commentEnabled = 0;
    int homeParentPosition;
    private Context mContext;

    ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

    /// For MultilAutocomplete watch text

    TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();

            Validation(text);                     // validation to comment Edit text

            if (text.lastIndexOf("@") > text.lastIndexOf(" ")) {
                String tag = text.substring(text.lastIndexOf("@"), text.length());
                // search for @mention...
                Log.v(TAG, "@tag=" + tag);
//                if (!tag.replace("@", "").equalsIgnoreCase(""))
//                    responseJsonClass.getUserData(tag.replace("@", ""));
            }

            if (text.lastIndexOf("#") > text.lastIndexOf(" ")) {
                String tag = text.substring(text.lastIndexOf("#"), text.length());
                // search for #hashtag...
                Log.v("#tag", "#tag=" + tag);
//                if (!tag.replace("#", "").equalsIgnoreCase(""))
//                    responseJsonClass.getHashdata(tag.replace("#", ""));
            }
        }
    };
    private ResponseJsonClass responseJsonClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_comments);
// Inflate the layout for this fragment
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        isScroll = false;
        offsetCnt = 0;
        /// ResponseJsonClass -> for get response from server
        responseJsonClass = new ResponseJsonClass(this);
        /// purpose for add comment service
        responseJsonClass.setAddCommentCallback(this);
        /// purpose for hashTag service
//        responseJsonClass.setHashTagCallback(this);
        utils = new AppUtils(this);
        // get postid from home post page
        if (getIntent() != null) {
            if (getIntent().hasExtra(Constants.TAG_FROM) && getIntent().getStringExtra(Constants.TAG_FROM).equals(Constants.NOTIFICATION)) {
                postId = getIntent().getStringExtra(Constants.TAG_FEED_ID);
            } else if (getIntent().hasExtra(Constants.NOTIFICATION) && getIntent().getStringExtra(Constants.NOTIFICATION).equals(Constants.TAG_COMMENT_FEEDS)) {
                postId = getIntent().getStringExtra(Constants.TAG_FEED_ID);
            } else {
                postOwnerId = getIntent().getStringExtra(Constants.TAG_USER_ID);
                postId = getIntent().getStringExtra(Constants.TAG_FEED_ID);
                homeParentPosition = getIntent().getIntExtra("parentposition", -1);
                type = getIntent().getStringExtra("type");
                postOwnerName = getIntent().getStringExtra(Constants.TAG_USER_NAME);
                postOwnerImage = getIntent().getStringExtra(Constants.TAG_USER_IMAGE);
                postDescription = getIntent().getStringExtra(Constants.TAG_DESCRIPTION);
                commentEnabled = getIntent().getIntExtra("commentEnabled", 0);
            }
        }

        Typeface typeface;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = mContext.getResources().getFont(R.font.font_light);
        } else {
            typeface = ResourcesCompat.getFont(mContext, R.font.font_light);
        }

        nestedScrollView = findViewById(R.id.nestedScrollView);
        addcomment = findViewById(R.id.addcomment);
        commentEdit = findViewById(R.id.commentEdit);
        replyToUsername = (TextView) findViewById(R.id.replyToUsername);
        closereply = (ImageView) findViewById(R.id.closereply);
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        descriptionLay = findViewById(R.id.descriptionLay);
        userImage = findViewById(R.id.userImage);
        txtDescription = findViewById(R.id.txtDescription);
        reply_postlayout = (RelativeLayout) findViewById(R.id.reply_postlayout);
        parentLay = (LinearLayout) findViewById(R.id.childLay);
        bottomlayout = (LinearLayout) findViewById(R.id.bottomlayout);
        nullLay = findViewById(R.id.nullLay);
        pgsBar = (ProgressWheel) findViewById(R.id.pBar);
        txtNoData = (TextView) findViewById(R.id.nullText);
        iconNoData = findViewById(R.id.nullImage);
        commentLay = (RelativeLayout) findViewById(R.id.commentlay);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycleView);
        txtTitle.setText(mContext.getString(R.string.comments));
        iconNoData.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.message_grey));
        txtNoData.setText(getResources().getString(R.string.befirst));

        /*SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);*/
        Validation("");
        commentEdit.setTypeface(typeface);
        pgsBar.setVisibility(View.GONE);

        searchTags();

        // get comment post data from service
        if (getIntent().hasExtra(Constants.TAG_FROM) && getIntent().getStringExtra(Constants.TAG_FROM).equals(Constants.NOTIFICATION)) {
            getFeedInfo(postId);
        } else if (getIntent().hasExtra(Constants.NOTIFICATION) && getIntent().getStringExtra(Constants.NOTIFICATION).equals(Constants.TAG_SCOPE)) {
            getFeedInfo(postId);
        } else {
            setAdapter();
            if (NetworkReceiver.isConnected())
                LoadComments(offsetCnt, limitCnt);
            else
                pgsBar.setVisibility(View.GONE);
        }

        // for page scrolling listener
        ScrollMethod();


        addcomment.setOnClickListener(this);
        closereply.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        commentEdit.setOnClickListener(this);

        commentEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    commentEdit.setFocusableInTouchMode(false);
                    utils.hideKeyboard(CommentsActivity.this);
                }
            }
        });

        commentEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    commentEdit.setBackgroundResource(R.drawable.send_msg_greyborder);
                }
                return false;
            }
        });
    }

    private void setAdapter() {
        commentList.clear();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        commentAdapter = new CommentsAdapter(this, commentList, postOwnerId, postId, this, responseJsonClass);
        mRecyclerView.setAdapter(commentAdapter);
        nestedScrollView.setVisibility(View.GONE);
    }

    private void getFeedInfo(String feedId) {
        if (NetworkReceiver.isConnected()) {
//            showLoading();
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            if (feedId != null) {
                requestMap.put(Constants.TAG_FEED_ID, feedId);
                requestMap.put(Constants.TAG_FOLLOWER_ID, "");
            }
            requestMap.put(Constants.TAG_LIMIT, "" + 0);
            requestMap.put(Constants.TAG_OFFSET, "" + (offsetCnt * limitCnt));

            Call<FeedsModel> homeApiCall = apiService.getHomeFeeds(requestMap);
            homeApiCall.enqueue(new Callback<FeedsModel>() {
                @Override
                public void onResponse(Call<FeedsModel> call, Response<FeedsModel> response) {
                    if (response.isSuccessful() && response.body().getResult().size() > 0) {
                        Feeds feed = response.body().getResult().get(0);
                        postOwnerId = feed.getUserId();
                        postId = feed.getFeedId();
                        postOwnerName = feed.getUserName();
                        postOwnerImage = feed.getUserImage();
                        postDescription = feed.getDescription();
                        commentEnabled = feed.getCommentStatus();
                        setAdapter();
                        LoadComments(offsetCnt, limitCnt);
                    }
                }

                @Override
                public void onFailure(Call<FeedsModel> call, Throwable t) {
                    Log.e(TAG, "loadHomeFeeds: " + t.getMessage());
                }
            });
        }
    }

    private void setDescription() {
        if (!TextUtils.isEmpty(postDescription)) {
            descriptionLay.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(Constants.IMAGE_URL + postOwnerImage)
                    .placeholder(R.drawable.avatar)
                    .into(userImage);
            txtDescription.setIsLinkable(true);
//            txtDescription.setText(Utils.stripHtml("@" + postOwnerName + " " + postDescription));
            txtDescription.setText(postDescription);
            MovementMethod movementMethod = txtDescription.getMovementMethod();
            if ((movementMethod == null) || !(movementMethod instanceof LinkMovementMethod)) {
                if (txtDescription.getLinksClickable()) {
                    txtDescription.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            Log.i(TAG, "setDescription: " + postDescription);
/*
            txtDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigateToProfile("otherprofile", "user", postOwnerId);
                }
            });
*/
            /*txtDescription.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {
                @Override
                public void onTextLinkClick(View textView, String clickedString) {
                    if (clickedString.trim().contains("@")) {
                        String user_param = clickedString.replace("@", "");

                        if (GetSet.getUserName().equalsIgnoreCase(user_param))
                            navigateToUserProfile("profile", "user", user_param, "user_name");
                        else
                            navigateToUserProfile("otherprofile", "user", user_param, "user_name");

                    } else if (clickedString.contains("#")) {
                        String user_param = clickedString.replace("#", "");
                        navigateToHashProfile(user_param, "tag");
                    }
                }
            });*/
        } else {
            descriptionLay.setVisibility(View.GONE);
        }
    }

    public void ScrollMethod() {

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (dy > 0 && !isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    recyclerView.post(new Runnable() {
                        public void run() {
                            commentList.add(null);
                            commentAdapter.notifyItemInserted(commentList.size() - 1);
                        }
                    });

                    offsetCnt++;
                    int tempcnt = offsetCnt * limitCnt;
                    Log.e("offsetCnt", "-" + tempcnt);
                    isScroll = true;

                    if (NetworkReceiver.isConnected())
                        LoadComments(tempcnt, limitCnt);

                    isLoading = true;

                }
            }
        });

    }

    public void LoadComments(int offsetCnt, int limitcnt) {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
        requestMap.put(Constants.TAG_FEED_ID, postId);
        requestMap.put(Constants.TAG_OFFSET, "" + offsetCnt);
        requestMap.put(Constants.TAG_LIMIT, "" + limitcnt);
        requestMap.put(Constants.TAG_TYPE, "" + type);
        Call<CommentsModel> call = apiService.getComments(requestMap);

        call.enqueue(new Callback<CommentsModel>() {
            @Override
            public void onResponse(Call<CommentsModel> call, Response<CommentsModel> response) {
                pgsBar.setVisibility(View.GONE);
                nestedScrollView.setVisibility(View.VISIBLE);
                //Remove bottom loading item
                if (isScroll && commentList.size() > 0) {
                    commentList.remove(commentList.size() - 1);
                    commentAdapter.notifyItemRemoved(commentList.size());
                }

                if (response.isSuccessful()) {
                    if (response.body().getStatus().equalsIgnoreCase("true")) {
                        List<CommentsModel.Result> resultList = response.body().getResults();
                        commentList.addAll(resultList);

                        if (resultList.size() > 0) {
                            isLoading = false;
                            commentAdapter.notifyDataSetChanged();
                        } else
                            isLoading = true;

                        //if comments is empty , show message
                        if (commentList.size() == 0) {
                            nullLay.setVisibility(View.GONE);
//                            commentEdit.setHint(mContext.getString(R.string.befirst));
                        } else {
                            commentEdit.setHint(mContext.getString(R.string.addcomment));
                            nullLay.setVisibility(View.GONE);
                        }
                    } else if (response.body().getMessage() != null && response.body().getMessage().contains("Constants.ADMIN_DISABLED")) {
//                        App.makeToast(mContext.getString(R.string.admin_blocked_desc));
                        call.cancel();
                    } else {
                        isLoading = true;
                        //if comments is empty , show message
                        if (commentList.size() == 0) {
                            commentEdit.setHint(mContext.getString(R.string.befirst));
                        } else {
                            nullLay.setVisibility(View.GONE);
                            commentEdit.setHint(mContext.getString(R.string.addcomment));
                        }
                    }

                    if (commentEnabled == 1) {
                        commentLay.setVisibility(View.VISIBLE);
                    } else {
                        commentLay.setVisibility(View.GONE);
                    }
                } else {
                    makeToast(getString(R.string.something_went_wrong));
                }
                if (offsetCnt == 0)
                    setDescription();
            }

            @Override
            public void onFailure(Call<CommentsModel> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                pgsBar.setVisibility(View.GONE);
                makeToast(getString(R.string.something_went_wrong));
                if (offsetCnt == 0)
                    setDescription();
            }
        });

    }

    @Override
    public void onAddCommentStatus(String status, Object object, String cmtCount) {   // position no need for comment list
        if (status.equalsIgnoreCase("true")) {
//            commentList.set(commentList.size() - 1, (CommentsModel.Result) object);
            commentAdapter.notifyItemChanged(commentList.size() - 1);
        }

    }


    @Override
    public void onDeleteCommentStatus(String status, int cmtParentPosition) {

        if (status.equalsIgnoreCase("true")) {

            commentList.remove(cmtParentPosition);
            commentAdapter.notifyItemRemoved(cmtParentPosition);
            commentAdapter.notifyItemRangeChanged(cmtParentPosition, commentList.size());
            if (commentList.size() == 0) {
                commentEdit.setHint(mContext.getString(R.string.befirst));
            } else {
                commentEdit.setHint(mContext.getString(R.string.addcomment));
            }
        }

    }


    @Override
    public void onDeleteReplyStatus(String status, String cmtCount, int cmtParentPosition, int childPosition) {

        if (status.equalsIgnoreCase("true")) {

            commentList.get(cmtParentPosition).getReply().remove(childPosition);
            commentAdapter.notifyItemRemoved(cmtParentPosition);
            commentAdapter.notifyItemRangeChanged(cmtParentPosition, commentList.size());

        }

    }


    @Override
    public void onAddReplyStatus(String status, Object object, int parentposition, int childPosition) {        // position needed for reply list
        if (status.equalsIgnoreCase("true")) {
            List<CommentsModel.Reply> tempList = commentList.get(parentposition).getReply();
//            commentList.get(parentposition).getReply().set(tempList.size() - 1, (CommentsModel.Reply) object);
            commentAdapter.notifyItemChanged(parentposition);
        }

    }


    public void Addcomment() {

        isReply = false;
        nullLay.setVisibility(View.GONE);
        /// add comment to first item  into recyclerview
        CommentsModel.Result resultsItem = new CommentsModel().new Result();
        resultsItem.setUserName(GetSet.getUserName());
        resultsItem.setUserId(GetSet.getUserId());
        resultsItem.setUserImage(GetSet.getUserImage());
        resultsItem.setCommentId("");
        resultsItem.setComments("" + commentEdit.getText().toString().trim());
        resultsItem.setLiked("");
        resultsItem.setLikeCount(0);
        commentList.add(resultsItem);
        commentAdapter.notifyItemInserted(commentList.size() - 1);
        commentEdit.setHint(mContext.getString(R.string.addcomment));

        responseJsonClass.addComment(postId, commentEdit.getText().toString().trim(), Constants.TAG_POST);
        commentEdit.setText(""); // after add comment Edittext shoulbe clear .
        AppUtils.hideKeyboard(this);  // add add comment keyboard shouldbe close
        mRecyclerView.smoothScrollToPosition(commentList.size() - 1);
    }

    @Override
    public void setParentReplyDetails(int parentPosition, int childPosition, String parentId, String userName) {
        isReply = true;
        this.parentPosition = parentPosition;
        this.childPosition = childPosition;
        this.parentId = parentId;
        this.userName = userName;

        reply_postlayout.setVisibility(View.VISIBLE);
        replyToUsername.setText(getString(R.string.replying));
        commentEdit.setSelection(commentEdit.getText().length());
        openKeyboard();
    }

    @Override
    public void navigateToProfile(String userId) {
        if (userId.equals(GetSet.getUserId())) {
            Intent profile = new Intent(mContext, MyProfileActivity.class);
            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            profile.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
            profile.putExtra(Constants.TAG_FROM, "");
            startActivity(profile);
        } else {
            Intent profile = new Intent(mContext, OthersProfileActivity.class);
            profile.putExtra(Constants.TAG_PARTNER_ID, userId);
//            profile.putExtra(Constants.TAG_PARTNER_NAME, resultItem.getUserName());
//            profile.putExtra(Constants.TAG_PARTNER_IMAGE, resultItem.getUserImage());
            profile.putExtra(Constants.TAG_FROM, Constants.TAG_MESSAGE);
            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(profile);
        }
    }

    @Override
    public void deleteComment(int parentPos, int position, String postId, String replyId, String type) {
        responseJsonClass.deleteComment(parentPos, position, postId, replyId, Constants.TAG_REPLY);
    }


    public void setChildReplyDetails(int parentPosition, int childPosition, String parentId, String userName) {

        isReply = true;

        childPosition++;                     /// reply to reply comments . so increase the child position
        this.parentPosition = parentPosition;
        this.childPosition = childPosition;
        this.parentId = parentId;
        this.userName = userName;

        reply_postlayout.setVisibility(View.VISIBLE);
        replyToUsername.setText(mContext.getString(R.string.replying));
        commentEdit.setText("");
        commentEdit.setSelection(commentEdit.getText().length());

        openKeyboard();

    }


    public void AddReply() {

        reply_postlayout.setVisibility(View.GONE);

        /// add comment to first item  into recyclerview

        CommentsModel.Reply reply = new CommentsModel().new Reply();
        CommentsModel.User user = new CommentsModel().new User();
        user.setUserId(GetSet.getUserId());
        user.setUserName(GetSet.getUserName());
        user.setUserImage(GetSet.getUserImage());
        reply.setUser(user);
        reply.setId("");
        reply.setComments("" + commentEdit.getText().toString().trim());
        reply.setLiked(0);
        reply.setReplyLikeCount(0);

        if (commentList.get(parentPosition).getReply() == null) {
            commentList.get(parentPosition).setReply(new ArrayList<>());
        }
        commentList.get(parentPosition).getReply().add(reply);
        commentList.get(parentPosition).setReplyVisible(true);
        commentAdapter.notifyDataSetChanged();
        responseJsonClass.addReply(postId, commentEdit.getText().toString(), commentList.get(parentPosition).getCommentId(), parentPosition, childPosition, Constants.TAG_REPLY);

        commentEdit.setText("");              // after add comment Edittext shoulbe clear .

        AppUtils.hideKeyboard(this);  // after add reply keyboard shouldbe close

        isReply = false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.addcomment:
                if (!TextUtils.isEmpty(commentEdit.getText())) {
                    if (!isReply)
                        Addcomment();
                    else
                        AddReply();
                }
                break;
            case R.id.closereply:
                commentEdit.setText("");
                reply_postlayout.setVisibility(View.GONE);
                isReply = false;
                break;
            case R.id.commentEdit:
                openKeyboard();
                break;

        }
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    public void setupUI(Context context, View view) {
        final AppCompatActivity act = (AppCompatActivity) context;
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    commentEdit.setBackgroundResource(R.drawable.send_msg_greyborder);
                    AppUtils.hideKeyboard(act);
                    return false;
                }

            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(act, innerView);
            }
        }
    }


    /// when click reply open keyboard

    public void openKeyboard() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        // you may want to play with the offset parameter
        if (!isReply)
            parentPosition = -1;

        layoutManager.scrollToPositionWithOffset(parentPosition, 0);

        commentEdit.setFocusableInTouchMode(true);
        commentEdit.post(() -> {
            commentEdit.requestFocus();
//            App.showSoftKeyboard(commentEdit);
        });

    }


   /* @Override
    public void onBackPressed() {
        // It's expensive, if running turn it off.
        utils.hideSoftKeyboard(commentEdit);
        super.onBackPressed();
    }*/


    ///  Hashtag part

    public void searchTags() {

        InputFilter filterArray = new InputFilter.LengthFilter(mContext.getResources().getInteger(R.integer.comments_max_length));
        commentEdit.setFilters(new InputFilter[]{filterArray});

        List<String> uNames = new ArrayList<String>();
        final ArrayAdapter<String> TopicName = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, uNames);
        commentEdit.setAdapter(TopicName);
        //  editText.setThreshold(2);
//        commentEdit.setTokenizer(new Utils.TagTokenizer());
        commentEdit.addTextChangedListener(textWatcher);

    }


    @Override
    public void onStatus(String status, Object object1, Object object2) {

        /*if (((ArrayList<SearchResponse.ResultsItem>) object1).size() > 0) {
            suggestAdapter = new SuggestAdapter(getActivity(), R.layout.tag_item, (List<String>) object2, (ArrayList<SearchResponse.ResultsItem>) object1);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (commentEdit != null) {
                        commentEdit.setAdapter(suggestAdapter);
                        commentEdit.showDropDown();
                    }
                }
            });
        }*/

    }


    /**
     * Adapter for suggestion list
     **/
    /*public class SuggestAdapter extends ArrayAdapter<String> {
        Context context;
        ArrayList<SearchResponse.ResultsItem> resultList = new ArrayList<>();
        List<String> nameList = new ArrayList<>();

        public SuggestAdapter(Context context, int ResourceId, List<String> nameList, ArrayList<SearchResponse.ResultsItem> resultList) {
            super(context, ResourceId, nameList);
            this.context = context;
            this.resultList = resultList;
            this.nameList = nameList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.tag_item, parent, false);//layout
            } else {
                view = convertView;
                view.forceLayout();
            }

            ImageView userImage = (ImageView) view.findViewById(R.id.image);
            TextView userName = (TextView) view.findViewById(R.id.username);
            TextView postCntTxt = (TextView) view.findViewById(R.id.postcount);

            SearchResponse.ResultsItem resultsItem = resultList.get(position);

            if (resultsItem.getTagType().equals("hash")) {
                userImage.setVisibility(View.GONE);
                userName.setText(nameList.get(position));

                if (resultsItem.getPostCount().equalsIgnoreCase("1"))
                    postCntTxt.setText(resultsItem.getPostCount() + " " + mContext.getString(R.string.public_post));
                else
                    postCntTxt.setText(resultsItem.getPostCount() + " " + getString(R.string.public_posts));

            } else {
                userImage.setVisibility(View.VISIBLE);
                userName.setText(resultsItem.getUserName());
                String image = resultsItem.getUserImage();
                if (image != null && !image.equals("")) {
                    Glide.with(context)
                            .load(image)
                            .placeholder(R.drawable.avatar)
                            .into(userImage);
                }
            }

            return view;
        }
    }*/




   /* // when user click home button , need to close keyboard
    @Override
    protected void onPause() {
        super.onPause();
        utils.hideSoftKeyboard(commentEdit);
    }
*/
    public void makeToast(String msg) {
        Toast.makeText(mContext, "" + msg, Toast.LENGTH_LONG).show();
    }

    public boolean Validation(String text) {
        boolean check = true;
        String cmtStr = text.trim();
        if (cmtStr.isEmpty()) {
            addcomment.setImageAlpha(0x3F);
            addcomment.setEnabled(false);
            check = false;
        } else {
            addcomment.setImageAlpha(0xFF);
            addcomment.setEnabled(true);
        }

        return check;
    }


    @Override
    public void onNetworkChanged(boolean isConnected) {

    }
}