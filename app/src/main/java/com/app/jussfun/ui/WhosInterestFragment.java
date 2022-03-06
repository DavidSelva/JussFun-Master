package com.app.jussfun.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.FollowersResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WhosInterestFragment extends Fragment {

    private static String TAG = WhosInterestFragment.class.getSimpleName();
    ApiInterface apiInterface;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.txtError)
    TextView txtError;
    private Context context;
    int currentPage = 0, limit = 20;
    private List<FollowersResponse.FollowersList> followersList = new ArrayList<>();
    private FollowersAdapter adapter;
    private int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 10;
    private GridLayoutManager mLayoutManager;
    String partnerId;
    private boolean isLoading = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_followers, container, false);
        ButterKnife.bind(this, rootView);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        partnerId = getArguments().getString(Constants.TAG_PARTNER_ID);
        initView();
        return rootView;
    }

    private void initView() {
        adapter = new FollowersAdapter(getActivity(), followersList);
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isPositionFooter(position) ? mLayoutManager.getSpanCount() : 1;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 0;
                getFollowersList(currentPage);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if (dy > 0) {//check for scroll down
                    if (isLoading) {
                        if (totalItemCount > previousTotal) {
                            isLoading = false;
                            previousTotal = totalItemCount;
                            currentPage++;
                        }
                    }

                    if (!isLoading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        getFollowersList(currentPage);
                        isLoading = true;
                    }
                }
            }
        });

        /*To load first ten items*/
        swipeRefresh(true);
        getFollowersList(currentPage = 0);
    }

    private void getFollowersList(int offset) {
        if (NetworkReceiver.isConnected()) {
            txtError.setVisibility(View.GONE);
            if (!swipeRefreshLayout.isRefreshing()) {
                adapter.showLoading(true);
            }
            Call<FollowersResponse> call;

            call = apiInterface.getInterestList(partnerId, offset * 20, limit);

            call.enqueue(new Callback<FollowersResponse>() {
                @Override
                public void onResponse(Call<FollowersResponse> call, Response<FollowersResponse> response) {
                    if (swipeRefreshLayout.isRefreshing()) {
                        followersList.clear();
                    }
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            txtError.setVisibility(View.GONE);
                            followersList.addAll(response.body().getFollowersList());
                        }
                    }
                    txtError.setVisibility(followersList.size() == 0 ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(followersList.size() > 0 ? View.VISIBLE : View.GONE);
                    txtError.setText(getString(R.string.no_one_interested_on_you_yet));

                    if (swipeRefreshLayout.isRefreshing()) {
                        adapter.showLoading(false);
                        swipeRefresh(false);
                        adapter.notifyDataSetChanged();
                        isLoading = true;
                    } else {
                        adapter.showLoading(false);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<FollowersResponse> call, Throwable t) {
                    call.cancel();
                    if (currentPage != 0)
                        currentPage--;
                }
            });
        } else {
            if (currentPage != 0)
                currentPage--;
            if (!swipeRefreshLayout.isRefreshing()) {
                adapter.showLoading(false);
            } else {
                if (followersList.size() == 0) {
                    txtError.setVisibility(View.VISIBLE);
                    txtError.setText(getString(R.string.no_internet_connection));
                }
                swipeRefresh(false);
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FollowersActivity.hasInterestChange) {
            FollowersActivity.hasInterestChange = false;
            swipeRefresh(true);
            getFollowersList(currentPage = 0);
        }
    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
    }


    public class FollowersAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;

        private List<FollowersResponse.FollowersList> itemList = new ArrayList<>();
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public FollowersAdapter(Context context, List<FollowersResponse.FollowersList> followersList) {
            this.context = context;
            this.itemList = followersList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_followers, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final FollowersResponse.FollowersList follower = itemList.get(position);

                ((MyViewHolder) holder).txtName.setText(follower.getName());
                ((MyViewHolder) holder).txtAge.setVisibility(follower.getPrivacyAge().equals(Constants.TAG_TRUE) ? View.GONE : View.VISIBLE);
                ((MyViewHolder) holder).txtAge.setText(", " + follower.getAge());
                if (follower.getGender().equals(Constants.TAG_MALE)) {
                    ((MyViewHolder) holder).genderImage.setImageDrawable(context.getDrawable(R.drawable.men));
                } else {
                    ((MyViewHolder) holder).genderImage.setImageDrawable(context.getDrawable(R.drawable.women));
                }

                boolean isPrimeUser = GetSet.getPremiumMember().equals(Constants.TAG_TRUE);
//                if (isPrimeUser || follower.getProfileUnlocked()) {
                    ((MyViewHolder) holder).userLay.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).lockedLay.setVisibility(View.GONE);
                    ((MyViewHolder) holder).premiumImage.setVisibility(follower.getPremiumMember().equals(Constants.TAG_TRUE) ?
                            View.VISIBLE : View.GONE);
                    Glide.with(context)
                            .load(Constants.IMAGE_URL + follower.getUserImage())
                            .apply(new RequestOptions().error(R.drawable.avatar).placeholder(R.drawable.avatar)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(((MyViewHolder) holder).userImage);
                /*} else {
                    ((MyViewHolder) holder).userLay.setVisibility(View.GONE);
                    ((MyViewHolder) holder).lockedLay.setVisibility(View.VISIBLE);

                    Glide.with(context)
                            .load(Constants.IMAGE_URL + follower.getUserImage())
                            .apply(new RequestOptions().error(R.drawable.avatar).placeholder(R.drawable.avatar)
                                    .transform(new BlurTransformation(5, 8))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(((MyViewHolder) holder).userImage);
                }*/


            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = itemList.size();
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

        public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.userImage)
            ImageView userImage;
            @BindView(R.id.premiumImage)
            ImageView premiumImage;
            @BindView(R.id.txtName)
            TextView txtName;
            @BindView(R.id.txtAge)
            TextView txtAge;
            @BindView(R.id.genderImage)
            ImageView genderImage;
            @BindView(R.id.userLay)
            LinearLayout userLay;
            @BindView(R.id.itemLay)
            FrameLayout itemLay;
            @BindView(R.id.iconLock)
            LottieAnimationView iconLock;
            @BindView(R.id.lockedLay)
            FrameLayout lockedLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                txtName.setMaxWidth((int) (displayMetrics.widthPixels * 0.20));
            }

            @OnClick(R.id.itemLay)
            public void onViewClicked() {
                FollowersResponse.FollowersList item = itemList.get(getAdapterPosition());
                Intent intent = new Intent(getActivity(), OthersProfileActivity.class);
                intent.putExtra(Constants.TAG_PARTNER_ID, item.getUserId());
                intent.putExtra(Constants.TAG_PARTNER_NAME, item.getName());
                intent.putExtra(Constants.TAG_AGE, "" + item.getAge());
                intent.putExtra(Constants.TAG_PARTNER_IMAGE, item.getUserImage());
                intent.putExtra(Constants.TAG_GENDER, item.getGender());
                intent.putExtra(Constants.TAG_BLOCKED_BY_ME, "");
                intent.putExtra(Constants.TAG_LOCATION, item.getLocation());
                intent.putExtra(Constants.TAG_PRIVACY_AGE, item.getPrivacyAge());
                intent.putExtra(Constants.TAG_PRIVACY_CONTACT_ME, item.getPrivacyContactMe());
                intent.putExtra(Constants.TAG_FOLLOWERS, "-");
                intent.putExtra(Constants.TAG_FOLLOWINGS, "-");
                intent.putExtra(Constants.TAG_PREMIUM_MEBER, item.getPremiumMember());
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_INTEREST);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                /*if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {

                } else {
                    if (itemList.get(getAdapterPosition()).getProfileUnlocked()) {
                        FollowersResponse.FollowersList item = itemList.get(getAdapterPosition());
                        Intent intent = new Intent(getActivity(), OthersProfileActivity.class);
                        intent.putExtra(Constants.TAG_PARTNER_ID, item.getUserId());
                        intent.putExtra(Constants.TAG_PARTNER_NAME, item.getName());
                        intent.putExtra(Constants.TAG_AGE, "" + item.getAge());
                        intent.putExtra(Constants.TAG_PARTNER_IMAGE, item.getUserImage());
                        intent.putExtra(Constants.TAG_GENDER, item.getGender());
                        intent.putExtra(Constants.TAG_BLOCKED_BY_ME, "");
                        intent.putExtra(Constants.TAG_LOCATION, item.getLocation());
                        intent.putExtra(Constants.TAG_PRIVACY_AGE, item.getPrivacyAge());
                        intent.putExtra(Constants.TAG_PRIVACY_CONTACT_ME, item.getPrivacyContactMe());
                        intent.putExtra(Constants.TAG_FOLLOWERS, "-");
                        intent.putExtra(Constants.TAG_FOLLOWINGS, "-");
                        intent.putExtra(Constants.TAG_PREMIUM_MEBER, item.getPremiumMember());
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_INTEREST);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    } else if (GetSet.getUnlocksLeft() > 0) {
                        unlockUser(itemList.get(getAdapterPosition()).getUserId(), getAdapterPosition());
                    } else {
                        goToPrime();
                    }
                }*/
            }

            private void unlockUser(String partnerId, final int selectedPosition) {
                if (NetworkReceiver.isConnected()) {
                    HashMap<String, String> requestMap = new HashMap<>();
                    requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
                    requestMap.put(Constants.TAG_INTEREST_USER_ID, partnerId);
                    Call<HashMap<String, String>> call = apiInterface.unlockUser(requestMap);
                    call.enqueue(new Callback<HashMap<String, String>>() {
                        @Override
                        public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                            if (response.isSuccessful()) {
                                if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                                    followersList.get(selectedPosition).setProfileUnlocked(true);
                                    iconLock.addAnimatorListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            adapter.notifyItemChanged(selectedPosition);
                                        }
                                    });
                                    iconLock.playAnimation();
                                }
                                if (response.body().containsKey(Constants.TAG_UNLOCKS_LEFT)) {
                                    int leftCount = Integer.parseInt(response.body().get(Constants.TAG_UNLOCKS_LEFT));
                                    GetSet.setUnlocksLeft(leftCount);
                                    if (leftCount > 0) {
                                        App.makeToast(String.format(getString(R.string.remaining_users_unlock), "" + leftCount));
                                    } else {
                                        App.makeToast(getString(R.string.upgrade_to_premium_to_unlock));
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                            call.cancel();
                        }
                    });
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

    private void goToPrime() {
        Intent prime = new Intent(context, PrimeActivity.class);
        prime.putExtra("OnCLick", "ClickHere");
        prime.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(prime, Constants.PRIME_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PRIME_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            swipeRefresh(true);
            getFollowersList(currentPage = 0);
        }
    }

}



