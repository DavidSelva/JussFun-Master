package com.app.jussfun.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.base.App;
import com.app.jussfun.helper.AdUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdView;
import com.app.jussfun.R;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.model.SearchUserResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindPeoplesActivity extends BaseFragmentActivity {

    private static final String TAG = FindPeoplesActivity.class.getSimpleName();

    long delay = Constants.TYPING_DELAY; // Delay seconds after user stops typing
    long lastTimeEdited = 0;
    Handler handler = new Handler();
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    AppCompatTextView txtTitle;
    @BindView(R.id.edtFindPeople)
    EditText edtFindPeople;
    @BindView(R.id.btnClear)
    ImageView btnClear;
    @BindView(R.id.btnQRCode)
    ImageView btnQRCode;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.parentLay)
    ConstraintLayout parentLay;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.progressLay)
    LinearLayout progressLay;
    @BindView(R.id.nullImage)
    ImageView nullImage;
    @BindView(R.id.nullText)
    AppCompatTextView nullText;
    @BindView(R.id.nullLay)
    RelativeLayout nullLay;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private ApiInterface apiInterface;
    private AppUtils appUtils;
    private ArrayList<ProfileResponse> userList = new ArrayList<>();
    private UserAdapter userAdapter;
    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (lastTimeEdited + delay - 500)) {
                // TODO: do what you need here
                // ............
                // ............
//                searchUsers();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_peoples);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        initView();
    }

    private void initView() {
        txtTitle.setVisibility(View.VISIBLE);
        txtTitle.setText(getString(R.string.find_peoples));
        userAdapter = new UserAdapter(this, userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);

        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.width = getDisplayWidth() / 3;
        params.height = getDisplayHeight() / 5;
        nullImage.setLayoutParams(params);
        nullImage.setVisibility(View.GONE);
        nullText.setText(getString(R.string.my_username) + " " + GetSet.getUserName());
        nullText.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.VISIBLE);

        edtFindPeople.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(input_finish_checker);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                lastTimeEdited = System.currentTimeMillis();
                handler.postDelayed(input_finish_checker, delay);
                if (editable.length() > 0) {
                    btnQRCode.setVisibility(View.GONE);
                    btnQRCode.clearAnimation();
                    if (btnClear.getVisibility() != View.VISIBLE) {
//                        slideOutAnim();
                        startAnim(btnClear);
                    }
                } else if (editable.length() == 0) {
                    btnClear.setVisibility(View.GONE);
                    btnClear.clearAnimation();
                    startAnim(btnQRCode);
//                    slideInAnim();
                }
            }
        });
        appUtils.showKeyboard(edtFindPeople, this);
        edtFindPeople.requestFocus();
        edtFindPeople.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int keyCode, KeyEvent event) {
                if (keyCode == EditorInfo.IME_ACTION_SEARCH) {
                    if (TextUtils.isEmpty(edtFindPeople.getText())) {
                        App.makeToast(getString(R.string.enter_user_name_to_find_the_people));
                    } else {
                        searchUsers();
                    }
                    return true;
                }
                return false;
            }
        });
        loadAd();
    }

    private void searchUsers() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_SEARCH_KEY, "" + edtFindPeople.getText());
            Call<SearchUserResponse> call = apiInterface.searchByUserName(requestMap);
            call.enqueue(new Callback<SearchUserResponse>() {
                @Override
                public void onResponse(Call<SearchUserResponse> call, Response<SearchUserResponse> response) {
                    if (response.isSuccessful()) {
                        userList.clear();
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            userList.addAll(response.body().getUserList());
                        }
                        if (userList.size() == 0) {
                            nullLay.setVisibility(View.VISIBLE);
                        } else {
                            nullLay.setVisibility(View.GONE);
                        }
                        userAdapter.notifyDataSetChanged();
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(Call<SearchUserResponse> call, Throwable t) {
                    t.printStackTrace();
                    call.cancel();
                }
            });

        }
    }

    private void setProfile(ProfileResponse profile) {

    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            AdUtils.getInstance(this).loadAd(TAG, adView);}
    }

    private void startAnim(View view) {
        if (LocaleManager.isRTL()) {
            view.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    -50,
                    0,
                    0,
                    0);
            animate.setDuration(300);
            animate.setFillAfter(true);
            view.startAnimation(animate);
        } else {
            view.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    50,
                    0,
                    0,
                    0);
            animate.setDuration(300);
            animate.setFillAfter(true);
            view.startAnimation(animate);
        }
    }

    private void slideOutAnim() {
        btnClear.setVisibility(View.VISIBLE);
        btnQRCode.clearAnimation();
        TranslateAnimation animate = new TranslateAnimation(
                -50,
                0,
                0,
                0);
        animate.setDuration(300);
        animate.setFillAfter(true);
        btnClear.startAnimation(animate);
    }

    private void slideInAnim() {
        btnQRCode.setVisibility(View.VISIBLE);
        btnClear.clearAnimation();
        TranslateAnimation animate = new TranslateAnimation(
                50,
                0,
                0,
                0);
        animate.setDuration(300);
        animate.setFillAfter(false);
        btnQRCode.startAnimation(animate);
    }

    @OnClick({R.id.btnBack, R.id.btnQRCode, R.id.btnClear})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnClear:
                edtFindPeople.getText().clear();
                break;
            case R.id.btnQRCode: {
                Intent intent = new Intent(getApplicationContext(), QRCodeScannerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
            break;
        }
    }

    public static class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

        private Context context;
        private ArrayList<ProfileResponse> userList = new ArrayList<>();

        UserAdapter(Context context, ArrayList<ProfileResponse> userList) {
            this.context = context;
            this.userList = userList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_search_user, parent, false);
            MyViewHolder viewHolder = new MyViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final ProfileResponse profile = userList.get(position);
            if (profile.getPrivacyAge().equals(Constants.TAG_TRUE)) {
                holder.txtName.setText(profile.getName());
            } else {
                holder.txtName.setText(profile.getName() + ", " + profile.getAge());
            }
            if (profile.getPremiumMember().equals(Constants.TAG_TRUE)) {
                holder.premiumImage.setVisibility(View.VISIBLE);
            } else {
                holder.premiumImage.setVisibility(View.GONE);
            }

            Glide.with(context)
                    .load(Constants.IMAGE_URL + profile.getUserImage())
                    .apply(App.getProfileImageRequest())
                    .into(holder.profileImage);

            Glide.with(context)
                    .load(profile.getGender().equals(Constants.TAG_MALE) ? R.drawable.men : R.drawable.women)
                    .into(holder.genderImage);

            holder.txtLocation.setText(AppUtils.formatWord(profile.getLocation()));

            holder.itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, OthersProfileActivity.class);
                    Log.i(TAG, "findonClick: "+profile.getUserId());
                    Log.i(TAG, "findonClick1: "+profile);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_SEARCH);
                    intent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                    intent.putExtra(Constants.TAG_PROFILE_DATA, profile);
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.profileImage)
            RoundedImageView profileImage;
            @BindView(R.id.premiumImage)
            ImageView premiumImage;
            @BindView(R.id.txtName)
            AppCompatTextView txtName;
            @BindView(R.id.genderImage)
            ImageView genderImage;
            @BindView(R.id.txtLocation)
            AppCompatTextView txtLocation;
            @BindView(R.id.itemLay)
            RelativeLayout itemLay;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
