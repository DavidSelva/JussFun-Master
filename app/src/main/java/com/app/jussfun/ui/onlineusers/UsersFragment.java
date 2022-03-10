package com.app.jussfun.ui.onlineusers;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.app.jussfun.R;
import com.app.jussfun.databinding.FragmentUsersBinding;
import com.app.jussfun.utils.Constants;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment {

    private static final String TAG = UsersFragment.class.getSimpleName();
    private Context mContext;
    FragmentUsersBinding binding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UsersFragment() {
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
    public static UsersFragment newInstance(String param1, String param2) {
        UsersFragment fragment = new UsersFragment();
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
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mContext == null) mContext = getActivity();
        binding.toolBarLay.btnBack.setVisibility(View.INVISIBLE);
        binding.toolBarLay.txtTitle.setText("Meet people");
        initViewPager();
    }

    private void initViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getActivity(), "");
        binding.viewPager.setOffscreenPageLimit(1);
        binding.viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(getString(R.string.online));
                    } else {
                        tab.setText("Offline");
                    }
                    /*View tabItemLayout = getLayoutInflater().inflate(R.layout.tab_liked_users, null);
                    ImageView ivLike = tabItemLayout.findViewById(R.id.ivLike);
                    TextView txtLike = tabItemLayout.findViewById(R.id.txtLike);
                    txtLike.setTextSize(TypedValue.COMPLEX_UNIT_SP, mContext.getResources().getDimension(R.dimen.text_small));
                    ivLike.setVisibility(View.GONE);
                    txtLike.setMaxLines(1);
                    if (position == 0) {
                        ivLike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.like_act));
                        txtLike.setText(getString(R.string.online));
                        tab.setCustomView(tabItemLayout);
                    } else {
                        ivLike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.star_act));
                        txtLike.setText("Offline");
                        tab.setCustomView(tabItemLayout);
                    }*/
                }
        ).attach();
    }

    public static class ViewPagerAdapter extends FragmentStateAdapter {
        private static final int TAB_SIZE = 2;
        private String feedId = null;

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String feedId) {
            super(fragmentActivity);
            this.feedId = feedId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            String type;
            if (position == 0) {
                type = Constants.TAG_ONLINE;
            } else {
                type = Constants.TAG_TODAY_USER;
            }
            return OnlineUsersFragment.newInstance("", type);
        }

        @Override
        public int getItemCount() {
            return TAB_SIZE;
        }
    }
}