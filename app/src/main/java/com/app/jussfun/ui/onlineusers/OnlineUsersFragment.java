package com.app.jussfun.ui.onlineusers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.jussfun.R;
import com.app.jussfun.databinding.FragmentOnlineUsersBinding;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.callback.FollowUpdatedListener;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.OnlineUsers;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.ui.MyProfileActivity;
import com.app.jussfun.ui.OthersProfileActivity;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OnlineUsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnlineUsersFragment extends Fragment implements FollowUpdatedListener {

    private static final String TAG = OnlineUsersFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context mContext;
    private FragmentOnlineUsersBinding binding;
    private ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
    private List<OnlineUsers.AccountModel> likedUsers = new ArrayList<>();
    private OnlineUsersAdapter adapter;
    int currentPage = 0, limit = 20;
    private int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 10;
    private boolean isLoading = true;

    public OnlineUsersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OnlineUsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OnlineUsersFragment newInstance(String param1, String param2) {
        OnlineUsersFragment fragment = new OnlineUsersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOnlineUsersBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideLoading();
        initView();
    }

    private void initView() {
        LinearLayoutManager mLayoutManager = (LinearLayoutManager) binding.rvLikedUsers.getLayoutManager();
        binding.rvLikedUsers.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildLayoutPosition(view);
                outRect.right = (int) mContext.getResources().getDimension(R.dimen.activity_horizontal_margin);
                outRect.left = (int) mContext.getResources().getDimension(R.dimen.activity_horizontal_margin);
                outRect.top = (int) mContext.getResources().getDimension(R.dimen.size_15);
                if (position == state.getItemCount() - 1) {
                    outRect.bottom = (int) mContext.getResources().getDimension(R.dimen.size_15);
                } else {
                    outRect.bottom = 0;
                }
            }
        });
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 0;
                getUsersList(currentPage);
            }
        });
        initAdapter(likedUsers);
        binding.rvLikedUsers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = binding.rvLikedUsers.getChildCount();
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
                        getUsersList(currentPage);
                        isLoading = true;
                    }
                }
            }
        });
        swipeRefresh(true);
        getUsersList(currentPage = 0);
    }

    private void swipeRefresh(final boolean refresh) {
        binding.swipeRefreshLayout.setRefreshing(refresh);
    }

    private void getUsersList(int offset) {
        if (NetworkReceiver.isConnected()) {
            if (!binding.swipeRefreshLayout.isRefreshing()) {
                adapter.showLoading(true);
            }
            Call<OnlineUsers> call = apiInterface.getOnlineUsers(GetSet.getUserId(), mParam2, offset * limit, limit);
            call.enqueue(new Callback<OnlineUsers>() {
                @Override
                public void onResponse(Call<OnlineUsers> call, Response<OnlineUsers> response) {
                    if (response.isSuccessful()) {
                        if (binding.swipeRefreshLayout.isRefreshing()) {
                            likedUsers.clear();
                        }
                        OnlineUsers responseModel = response.body();
                        if (responseModel.getStatus().equals(Constants.TAG_TRUE)) {
                            likedUsers.addAll(responseModel.getResult());
                        }
                        if (likedUsers.size() == 0) {
                            binding.noDataLay.nullLay.setVisibility(View.VISIBLE);
                            binding.noDataLay.nullText.setText(mContext.getString(R.string.no_user_found));
                        } else {
                            binding.noDataLay.nullLay.setVisibility(View.GONE);
                        }
                    }
                    if (binding.swipeRefreshLayout.isRefreshing()) {
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
                public void onFailure(Call<OnlineUsers> call, Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                    if (currentPage != 0)
                        currentPage--;
                }
            });
        } else {
            if (currentPage != 0)
                currentPage--;
            if (!binding.swipeRefreshLayout.isRefreshing()) {
                adapter.showLoading(false);
            } else {
                if (likedUsers.size() == 0) {
                    binding.noDataLay.nullLay.setVisibility(View.VISIBLE);
                    binding.noDataLay.nullText.setText(getString(R.string.no_internet_connection));
                }
                swipeRefresh(false);
            }
        }
    }

    private void initAdapter(List<OnlineUsers.AccountModel> likedUsers) {
        if (adapter == null) {
            adapter = new OnlineUsersAdapter(mContext, likedUsers, this, mParam2);
            binding.rvLikedUsers.setAdapter(adapter);
        }
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onFollowUpdated(ProfileResponse profileResponse, int holderPosition) {

    }

    @Override
    public void onFollowUpdated(OnlineUsers.AccountModel profileResponse, int holderPosition) {
        followUnFollowUser(profileResponse, holderPosition);
    }

    @Override
    public void onProfileClicked(String userId) {
        if (userId.equals(GetSet.getUserId())) {
            Intent profile = new Intent(mContext, MyProfileActivity.class);
            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            profile.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
            profile.putExtra(Constants.TAG_FROM, "");
            startActivity(profile);
        } else {
            Intent profile = new Intent(mContext, OthersProfileActivity.class);
            profile.putExtra(Constants.TAG_PARTNER_ID, userId);
            profile.putExtra(Constants.TAG_FROM, Constants.TAG_MESSAGE);
            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(profile);
        }
    }

    private void followUnFollowUser(OnlineUsers.AccountModel othersProfile, int holderPosition) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_INTEREST_USER_ID, othersProfile.getId());
            requestMap.put(Constants.TAG_INTERESTED, "" + (othersProfile.isAcctInterestAlert() ? 0 : 1));
            Call<Map<String, String>> call = apiInterface.interestOnUser(requestMap);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (requestMap.get(Constants.TAG_INTERESTED).equals("" + 1)) {
                                othersProfile.setAcctInterestAlert(true);
                            } else {
                                othersProfile.setAcctInterestAlert(false);
                            }
                            if (responseMap.get(Constants.TAG_FRIEND).equals(Constants.TAG_TRUE)) {
//                                othersProfile.setFriend(true);
                            } else {
//                                othersProfile.setFriend(false);
                            }
                            if (adapter != null) {
                                adapter.onFollowUpdated(othersProfile, holderPosition);
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

    public void showLoading() {
        binding.loadingLay.progressLay.setVisibility(View.VISIBLE);
        /*Disable touch options*/
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        /*Enable touch options*/
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        binding.loadingLay.progressLay.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}