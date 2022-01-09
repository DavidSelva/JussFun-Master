package com.app.jussfun.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.app.jussfun.R;
import com.app.jussfun.external.CrystalRangeSeekbar;
import com.app.jussfun.external.OnRangeSeekbarChangeListener;
import com.app.jussfun.external.OnRangeSeekbarFinalValueListener;
import com.app.jussfun.external.cardstackview.CardStackLayoutManager;
import com.app.jussfun.external.cardstackview.CardStackListener;
import com.app.jussfun.external.cardstackview.CardStackView;
import com.app.jussfun.external.cardstackview.Direction;
import com.app.jussfun.external.cardstackview.RewindAnimationSetting;
import com.app.jussfun.external.cardstackview.StackFrom;
import com.app.jussfun.external.cardstackview.SwipeAnimationSetting;
import com.app.jussfun.external.cardstackview.SwipeableMethod;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.OnOkClickListener;
import com.app.jussfun.model.FollowersResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.SharedPref;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NearByUsersFragment extends Fragment implements CardStackListener {

    private static String TAG = NearByUsersFragment.class.getSimpleName();
    @BindView(R.id.card_stack_view)
    CardStackView cardStackView;
    @BindView(R.id.btnInterest)
    ImageView btnInterest;
    @BindView(R.id.btnUnInterest)
    ImageView btnUnInterest;
    @BindView(R.id.parentLay)
    RelativeLayout parentLay;
    @BindView(R.id.bottomLay)
    RelativeLayout bottomLay;
    @BindView(R.id.loadingImage)
    LottieAnimationView loadingImage;
    @BindView(R.id.btnRetry)
    Button btnRetry;
    @BindView(R.id.noUserLay)
    LinearLayout noUserLay;
    private Context context;
    private ApiInterface apiInterface;
    private AppUtils appUtils;
    public static List<FollowersResponse.FollowersList> nearByUsersList = new ArrayList<>();
    private CardStackLayoutManager cardManager;
    public static CardStackAdapter nearByAdapter;
    public static boolean isFragmentChanged = false;
    private Dialog dialog;
    private boolean isFilterApplied = false, isAllLocationSelected = false, isLocationFilter = false;
    private ImageView btnApply, btnCancel;
    private RadioButton btnGirls, btnGuys, btnBoth;
    private TextView txtAge, txtLocation, labelLocation;
    private int limit = Constants.MAX_LIMIT, offset = 0, lastUserPosition;
    private ArrayList<String> filterLocations = new ArrayList<>();
    private ArrayList<String> tempLocations = new ArrayList<>();
    private RelativeLayout locationLay;
    private CrystalRangeSeekbar seekBar;
    private LinearLayout filterLay;
    private ImageView btnLocation;

    public NearByUsersFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_near_by_users, container, false);
        ButterKnife.bind(this, rootView);
        appUtils = new AppUtils(context);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        cardManager = new CardStackLayoutManager(context, this);
        nearByUsersList = new ArrayList<>();

        initView(rootView);
        offset = 0;
        getNearByUsersList();
        return rootView;
    }

    private void initView(ViewGroup rootView) {
        nearByAdapter = new CardStackAdapter(context, nearByUsersList);
//        cardManager.setStackFrom(StackFrom.None);
        cardManager.setVisibleCount(2);
        cardManager.setTranslationInterval(4.0f);
        cardManager.setStackFrom(StackFrom.Bottom);
        cardManager.setScaleInterval(0.98f);
        cardManager.setSwipeThreshold(0.3f);
        cardManager.setMaxDegree(90f);
        cardManager.setDirections(Direction.HORIZONTAL);
        cardManager.setCanScrollHorizontal(true);
        cardManager.setCanScrollVertical(true);
        cardManager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);
        cardManager.setOverlayInterpolator(new LinearInterpolator());
        initFilterDialog(getActivity(), 0, 0);
        bottomLay.setVisibility(View.GONE);

        setSwipeDirection(Direction.Left);
        RewindAnimationSetting rewindSetting = new RewindAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setInterpolator(new LinearInterpolator())
                .build();
        cardManager.setRewindAnimationSetting(rewindSetting);

        cardStackView.setLayoutManager(cardManager);
        cardStackView.setAdapter(nearByAdapter);
        nearByAdapter.notifyDataSetChanged();
    }

    private void setSwipeDirection(Direction direction) {
        SwipeAnimationSetting swipeSettings = new SwipeAnimationSetting.Builder()
                .setDirection(direction)
                .setInterpolator(new AccelerateInterpolator())
                .build();
        cardManager.setSwipeAnimationSetting(swipeSettings);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NearByUsersFragment.isFragmentChanged) {
            NearByUsersFragment.isFragmentChanged = false;
            offset = 0;
            getNearByUsersList();
        }
    }

    private void getNearByUsersList() {
        if (NetworkReceiver.isConnected()) {
            noUserLay.setVisibility(View.GONE);
            loadingImage.setVisibility(View.VISIBLE);
            loadingImage.playAnimation();
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_LIMIT, "" + limit);
            requestMap.put(Constants.TAG_OFFSET, "" + (offset * limit));
            if (btnBoth.isChecked()) {
                requestMap.put(Constants.TAG_GENDER, Constants.TAG_BOTH);
            } else if (btnGuys.isChecked()) {
                requestMap.put(Constants.TAG_GENDER, Constants.TAG_MALE);
            } else {
                requestMap.put(Constants.TAG_GENDER, Constants.TAG_FEMALE);
            }
            requestMap.put(Constants.TAG_MIN_AGE, "" + seekBar.getSelectedMinValue());
            requestMap.put(Constants.TAG_MAX_AGE, "" + seekBar.getSelectedMaxValue());
            if (isLocationFilter) {
                JSONArray locationList = new JSONArray(filterLocations);
                requestMap.put(Constants.TAG_LOCATION, "" + locationList);
            } else {
                if (GetSet.getLocation() != null) {
                    List<String> locationList = new ArrayList<>();
                    locationList.add(GetSet.getLocation());
                    JSONArray locationArray = new JSONArray(locationList);
                    requestMap.put(Constants.TAG_LOCATION, "" + locationArray);
                }
            }

            Log.i(TAG, "getNearByUsersList: " + new Gson().toJson(requestMap));
            Call<FollowersResponse> call = apiInterface.getNearByUsers(requestMap);
            call.enqueue(new Callback<FollowersResponse>() {
                @Override
                public void onResponse(Call<FollowersResponse> call, Response<FollowersResponse> response) {
                    if (response.isSuccessful()) {
                        if (isFilterApplied || offset == 0)
                            nearByUsersList.clear();
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            if (response.body().getFollowersList().size() > 0 && isFilterApplied) {
                                isFilterApplied = false;
                                chargeSwipeFilters();
                            }
                            nearByUsersList.addAll(response.body().getFollowersList());
                        }
                        nearByAdapter.notifyDataSetChanged();
                    }
                    loadingImage.setVisibility(View.GONE);
                    loadingImage.pauseAnimation();
                    if (nearByUsersList.size() > 0) {
                        bottomLay.setVisibility(View.VISIBLE);
                        noUserLay.setVisibility(View.GONE);
                    } else {
                        bottomLay.setVisibility(View.GONE);
                        noUserLay.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<FollowersResponse> call, Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                }
            });
        }
    }

    private void chargeSwipeFilters() {
        Call<HashMap<String, String>> call = apiInterface.chargeSwipeFilters(GetSet.getUserId());
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                if (response.isSuccessful()) {
                    if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        HashMap<String, String> responseMap = response.body();
                        GetSet.setGems(Long.valueOf(responseMap.get(Constants.TAG_AVAILABLE_GEMS)));
                        SharedPref.putLong(SharedPref.GEMS, GetSet.getGems());
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {

    }

    @Override
    public void onCardSwiped(Direction direction) {
        if (direction == Direction.Left) {
            btnUnInterestClicked();
            if (nearByUsersList.size() > 0) {
                interestOnUser(nearByUsersList.get(lastUserPosition), 0);
            }
        } else if (direction == Direction.Right) {
            btnInterestClicked();
            if (nearByUsersList.size() > 0) {
//                showMatchDialog(nearByUsersList.get(lastUserPosition));
                interestOnUser(nearByUsersList.get(lastUserPosition), 1);
            }
        }
    }

    @Override
    public void onCardRewound() {

    }

    @Override
    public void onCardCanceled() {

    }

    @Override
    public void onCardAppeared(View view, int position) {

    }

    @Override
    public void onCardDisappeared(View view, int position) {
        lastUserPosition = position;
        if (position == (nearByUsersList.size() - 1)) {
            bottomLay.setVisibility(View.INVISIBLE);
            offset++;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    nearByUsersList.clear();
                    getNearByUsersList();
                }
            }, 200);
        }
    }

    private void goToPrime() {
        Intent prime = new Intent(context, PrimeActivity.class);
        prime.putExtra("OnCLick","ClickHere");
        prime.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(prime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void btnInterestClicked() {
        appUtils.onInterestClicked(btnInterest);
    }

    private void btnUnInterestClicked() {
        appUtils.onUnInterestClicked(btnUnInterest);
    }

    public void setBottomMargin(int bottomNavHeight) {
        if (bottomNavHeight != 0 && parentLay != null) {
            parentLay.setPadding(0, 0, 0, bottomNavHeight);
        }
    }

    public void updateNearByUsers() {
        nearByUsersList.clear();
        offset = 0;
        getNearByUsersList();
    }

    private void initFilterDialog(Context context, int x, int y) {
        // x -->  X-Cordinate
        // y -->  Y-Cordinate
        dialog = new Dialog(context, R.style.CustomDialogAnimation);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_filter_random);
        dialog.setCanceledOnTouchOutside(true);

        filterLay = dialog.findViewById(R.id.filterLay);
        btnApply = dialog.findViewById(R.id.btnApply);
        btnCancel = dialog.findViewById(R.id.btnCancel);
        btnGirls = dialog.findViewById(R.id.btnGirls);
        btnGuys = dialog.findViewById(R.id.btnGuys);
        btnBoth = dialog.findViewById(R.id.btnBoth);
        txtAge = dialog.findViewById(R.id.txtAge);
        txtLocation = dialog.findViewById(R.id.txtLocation);
        btnLocation = dialog.findViewById(R.id.btnLocation);
        labelLocation = dialog.findViewById(R.id.labelLocation);
        seekBar = dialog.findViewById(R.id.seekBar);
        locationLay = dialog.findViewById(R.id.locationLay);

        btnGirls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked)
                    compoundButton.setTextColor(context.getResources().getColor(R.color.colorWhiteText));
                else
                    compoundButton.setTextColor(context.getResources().getColor(R.color.colorBlack));
            }
        });

        btnGuys.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked)
                    compoundButton.setTextColor(context.getResources().getColor(R.color.colorWhiteText));
                else
                    compoundButton.setTextColor(context.getResources().getColor(R.color.colorBlack));
            }
        });

        btnBoth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked)
                    compoundButton.setTextColor(context.getResources().getColor(R.color.colorWhiteText));
                else
                    compoundButton.setTextColor(context.getResources().getColor(R.color.colorBlack));
            }
        });

        if (GetSet.getLocation() != null) {
            txtLocation.setText(GetSet.getLocation());
            tempLocations.add(GetSet.getLocation());
        }
        if (LocaleManager.isRTL()) {
            btnLocation.setRotation(180);
        } else {
            btnLocation.setRotation(0);
        }

        initSeekBar();

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                isAllLocationSelected = txtLocation.getText().toString().equalsIgnoreCase(getString(R.string.world_wide));
                filterLocations = new ArrayList<>();
                offset = 0;
                for (String location : tempLocations) {
                    filterLocations.add(location.trim());
                }
                isFilterApplied = true;
                getNearByUsersList();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                offset = 0;
                resetFilters();
                dialog.dismiss();
                isFilterApplied = false;
                getNearByUsersList();
            }
        });

        locationLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent locationIntent = new Intent(getActivity(), LocationFilterActivity.class);
                locationIntent.putExtra(Constants.TAG_FROM, Constants.TAG_NEARBY);
                locationIntent.putExtra(Constants.TAG_LOCATION_SELECTED, isAllLocationSelected);
                if (isAllLocationSelected) {
                    locationIntent.putStringArrayListExtra(Constants.TAG_FILTER_LOCATION, (ArrayList<String>) AdminData.locationList);
                } else {
                    locationIntent.putStringArrayListExtra(Constants.TAG_FILTER_LOCATION, tempLocations);
                }
                startActivityForResult(locationIntent, Constants.LOCATION_FILTER_REQUEST_CODE);
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        lp.copyFrom(window.getAttributes());
        //This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        lp.x = x;
        lp.y = y;
        window.setAttributes(lp);
    }

    private void initSeekBar() {
        if (seekBar != null) {
            seekBar.setMinValue(Constants.MIN_AGE);
            seekBar.setMaxValue(Constants.MAX_AGE);
            seekBar.setMinStartValue(Constants.MIN_AGE);
            seekBar.setMaxStartValue(Constants.MAX_AGE);
            seekBar.apply();

            seekBar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
                @Override
                public void finalValue(Number minValue, Number maxValue) {
                    if (NetworkReceiver.isConnected()) {
                        applyAge(minValue, maxValue);
                    } else {
                        App.makeToast(getString(R.string.no_internet_connection));
                        resetSeekBar();
                    }
                }
            });

            seekBar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
                @Override
                public void valueChanged(Number minValue, Number maxValue) {
                    txtAge.setText(minValue + " - " + maxValue);
                }
            });
        }
    }

    public void showFilterDialog(Context context) {
        if (dialog != null && !dialog.isShowing())
            dialog.show();
    }

    void applyAge(Number minValue, Number maxValue) {
        txtAge.setText(minValue + " - " + maxValue);
    }

    private void resetSeekBar() {
        seekBar.setMinValue(Constants.MIN_AGE);
        seekBar.setMaxValue(Constants.MAX_AGE);
        seekBar.setMinStartValue(Constants.MIN_AGE);
        seekBar.setMaxStartValue(Constants.MAX_AGE);
        seekBar.apply();
        txtAge.setText(Constants.MIN_AGE + " - " + Constants.MAX_AGE);
    }

    private void resetFilters() {
        isFilterApplied = false;
        isLocationFilter = false;
        isAllLocationSelected = true;
        filterLocations = new ArrayList<>();
        tempLocations = (ArrayList<String>) AdminData.locationList;
        txtLocation.setText(GetSet.getLocation());
        btnBoth.setChecked(true);
        btnGirls.setChecked(false);
        btnGuys.setChecked(false);
        resetSeekBar();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.LOCATION_FILTER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            tempLocations = data.getStringArrayListExtra(Constants.TAG_FILTER_LOCATION);
            isAllLocationSelected = data.getBooleanExtra(Constants.TAG_LOCATION_SELECTED, false);
            if (tempLocations == null || tempLocations.size() == 0) {
                tempLocations = new ArrayList<>();
                tempLocations.add(GetSet.getLocation());
            }
            txtLocation.setText(TextUtils.join(",", tempLocations));
            isLocationFilter = true;
        }
    }

    @OnClick({R.id.btnSearch, R.id.btnFilter, R.id.btnInterest, R.id.btnUnInterest, R.id.btnRetry})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnSearch: {
                Intent intent = new Intent(context, FindPeoplesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
            break;
            case R.id.btnInterest:
                setSwipeDirection(Direction.Right);
                cardStackView.swipe();
                break;
            case R.id.btnUnInterest:
                setSwipeDirection(Direction.Left);
                cardStackView.swipe();
                break;
            case R.id.btnFilter:
                /*if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    showFilterDialog(getActivity());
                } else {
                    goToPrime();
                }*/
                showFilterDialog(getActivity());
                break;
            case R.id.btnRetry:
                offset = 0;
                getNearByUsersList();
                break;
        }
    }

    private void interestOnUser(FollowersResponse.FollowersList partnerData, int isInterested) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_INTEREST_USER_ID, partnerData.getUserId());
            requestMap.put(Constants.TAG_INTERESTED, "" + isInterested);
            Call<Map<String, String>> call = apiInterface.interestOnUser(requestMap);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (responseMap.get(Constants.TAG_FRIEND).equals(Constants.TAG_TRUE)) {
                                showMatchDialog(partnerData);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void showMatchDialog(FollowersResponse.FollowersList followersList) {
        DialogMatch dialogMatch = new DialogMatch();
        dialogMatch.setContext(context);
        dialogMatch.setPartnerData(followersList);
        dialogMatch.setCallBack(new OnOkClickListener() {
            @Override
            public void onOkClicked(Object object) {
                boolean isChat = (boolean) object;
                if (isChat) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra(Constants.TAG_PARTNER_ID, followersList.getUserId());
                    intent.putExtra(Constants.TAG_PARTNER_NAME, followersList.getName());
                    intent.putExtra(Constants.TAG_PARTNER_IMAGE, followersList.getUserImage());
                    startActivity(intent);
                    dialogMatch.dismissAllowingStateLoss();
                } else {
                    dialogMatch.dismissAllowingStateLoss();
                }
            }
        });
        dialogMatch.show(getChildFragmentManager(), TAG);
    }

    public class CardStackAdapter extends RecyclerView.Adapter<CardStackAdapter.MyViewHolder> {

        private List<FollowersResponse.FollowersList> nearByUsersList = new ArrayList<>();
        private Context context;
        private MyViewHolder viewHolder;

        public CardStackAdapter(Context context, List<FollowersResponse.FollowersList> nearByUsersList) {
            this.context = context;
            this.nearByUsersList = nearByUsersList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_near_by_users, parent, false);
            viewHolder = new MyViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final FollowersResponse.FollowersList follower = nearByUsersList.get(position);
            if (follower.getPremiumMember().equals(Constants.TAG_TRUE)) {
                ((MyViewHolder) holder).premiumImage.setVisibility(View.VISIBLE);
            } else {
                ((MyViewHolder) holder).premiumImage.setVisibility(View.GONE);
            }

            if (follower.getPrivacyAge().equals(Constants.TAG_TRUE)) {
                holder.txtName.setText(follower.getName());
            } else {
                holder.txtName.setText(follower.getName() + ", " + follower.getAge());
            }

            ((MyViewHolder) holder).txtLocation.setText(AppUtils.formatWord(follower.getLocation()));

            Glide.with(context)
                    .load(Constants.IMAGE_URL + follower.getUserImage())
                    .apply(new RequestOptions().error(R.drawable.avatar).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(((MyViewHolder) holder).userImage);
            if (follower.getGender().equals(Constants.TAG_MALE)) {
                ((MyViewHolder) holder).genderImage.setImageDrawable(context.getDrawable(R.drawable.men));
            } else {
                ((MyViewHolder) holder).genderImage.setImageDrawable(context.getDrawable(R.drawable.women));
            }
            holder.itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String profileResponse = new Gson().toJson(follower);
                    ProfileResponse profile = new Gson().fromJson(profileResponse, ProfileResponse.class);
                    Intent intent = new Intent(context, OthersProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_NEARBY);
                    intent.putExtra(Constants.TAG_PROFILE_DATA, profile);
                    intent.putExtra(Constants.TAG_POSITION, 0);
                    intent.putExtra(Constants.TAG_PARTNER_ID, follower.getUserId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return nearByUsersList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.userImage)
            RoundedImageView userImage;
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
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            }
        }
    }
}


