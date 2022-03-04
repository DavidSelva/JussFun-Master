package com.app.jussfun.ui.feed.likes;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.databinding.FragmentLikedUsersBinding;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.callback.FollowUpdatedListener;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.LikedUsersModel;
import com.app.jussfun.model.ProfileResponse;
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
 * Use the {@link LikedUsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LikedUsersFragment extends Fragment implements FollowUpdatedListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Context mContext;
    private FragmentLikedUsersBinding binding;
    private ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
    private List<ProfileResponse> likedUsers = new ArrayList<>();
    private LikedUsersAdapter adapter;

    public LikedUsersFragment() {
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
     * @return A new instance of fragment LikedUsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LikedUsersFragment newInstance(String param1, String param2) {
        LikedUsersFragment fragment = new LikedUsersFragment();
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
        binding = FragmentLikedUsersBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        getUsersList(mParam1, mParam2);
    }

    private void getUsersList(String feedId, String type) {
        if (NetworkReceiver.isConnected()) {
            showLoading();
            Call<LikedUsersModel> call = apiInterface.getLikedUsers(GetSet.getUserId(), feedId, type);
            call.enqueue(new Callback<LikedUsersModel>() {
                @Override
                public void onResponse(Call<LikedUsersModel> call, Response<LikedUsersModel> response) {
                    if (response.isSuccessful()) {
                        LikedUsersModel responseModel = response.body();
                        if (responseModel.getStatus().equals(Constants.TAG_TRUE)) {
                            likedUsers.addAll(responseModel.getUsersList());
                            initAdapter(likedUsers);
                        }
                        if (likedUsers.size() == 0) {
                            binding.noDataLay.nullLay.setVisibility(View.VISIBLE);
                            binding.noDataLay.nullText.setText(mContext.getString(R.string.no_user_found));
                        } else {
                            binding.noDataLay.nullLay.setVisibility(View.GONE);
                        }
                        hideLoading();
                    }
                }

                @Override
                public void onFailure(Call<LikedUsersModel> call, Throwable t) {

                }
            });
        } else {
            hideLoading();
        }
    }

    private void initAdapter(List<ProfileResponse> likedUsers) {
        if (adapter == null) {
            adapter = new LikedUsersAdapter(mContext, likedUsers, this);
            binding.rvLikedUsers.setAdapter(adapter);
        }
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onFollowUpdated(ProfileResponse profileResponse, int holderPosition) {
        followUnFollowUser(profileResponse, holderPosition);
    }

    private void followUnFollowUser(ProfileResponse othersProfile, int holderPosition) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_INTEREST_USER_ID, othersProfile.getUserId());
            requestMap.put(Constants.TAG_INTERESTED, "" + (othersProfile.getInterestedByMe() ? 0 : 1));
            Call<Map<String, String>> call = apiInterface.interestOnUser(requestMap);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (requestMap.get(Constants.TAG_INTERESTED).equals("" + 1)) {
                                othersProfile.setInterestedByMe(true);
                            } else {
                                othersProfile.setInterestedByMe(false);
                            }
                            if (responseMap.get(Constants.TAG_FRIEND).equals(Constants.TAG_TRUE)) {
                                othersProfile.setFriend(true);
                            } else {
                                othersProfile.setFriend(false);
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