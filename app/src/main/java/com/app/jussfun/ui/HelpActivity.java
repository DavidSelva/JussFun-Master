package com.app.jussfun.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.databinding.ActivityHelpBinding;
import com.app.jussfun.helper.BannerAdUtils;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.HelpResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HelpActivity extends BaseFragmentActivity {

    private static final String TAG = HelpActivity.class.getSimpleName();
    private ActivityHelpBinding binding;
    ApiInterface apiInterface;
    HelpAdapter adapter;
    private List<HelpResponse.HelpList> helpList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView();
    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        binding.toolBarLay.txtTitle.setText(R.string.help);
        adapter = new HelpAdapter(getApplicationContext(), helpList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        loadAd();

        if (LocaleManager.isRTL()) {
            binding.toolBarLay.btnBack.setRotation(180);
        } else {
            binding.toolBarLay.btnBack.setRotation(0);
        }
        binding.toolBarLay.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        helpList.clear();
        HelpResponse.HelpList help = new HelpResponse().new HelpList();
        help.setHelpTitle(getString(R.string.privacy));
        helpList.add(help);
        help = new HelpResponse().new HelpList();
        help.setHelpTitle(getString(R.string.terms_and_conditions));
        helpList.add(help);
        adapter.notifyDataSetChanged();
//        getHelpList();
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            BannerAdUtils.getInstance(this).loadAd(TAG, binding.adView);
        }
    }


    private void getHelpList() {
        if (NetworkReceiver.isConnected()) {
            Call<HelpResponse> call = apiInterface.getHelps();
            call.enqueue(new Callback<HelpResponse>() {
                @Override
                public void onResponse(Call<HelpResponse> call, Response<HelpResponse> response) {
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        helpList.clear();
                        helpList.addAll(response.body().getHelpList());
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<HelpResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

    public class HelpAdapter extends RecyclerView.Adapter {
        private final Context context;
        private List<HelpResponse.HelpList> helpList = new ArrayList<>();
        private RecyclerView.ViewHolder viewHolder;

        public HelpAdapter(Context context, List<HelpResponse.HelpList> helpList) {
            this.context = context;
            this.helpList = helpList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_help, parent, false);
            viewHolder = new MyViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final HelpResponse.HelpList help = helpList.get(position);
            ((MyViewHolder) holder).txtHelpTitle.setText(help.getHelpTitle());
            ((MyViewHolder) holder).txtHelpTitle.setTextColor(getResources().getColor(R.color.colorPrimaryText));
            ((MyViewHolder) holder).divider.setBackgroundColor(getResources().getColor(R.color.colorDivider));
        }

        @Override
        public int getItemCount() {
            return helpList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView iconHelp;
            TextView txtHelpTitle;
            RelativeLayout itemLay;
            View divider;

            public MyViewHolder(View view) {
                super(view);
                iconHelp = view.findViewById(R.id.iconHelp);
                txtHelpTitle = view.findViewById(R.id.txtHelpTitle);
                itemLay = view.findViewById(R.id.itemLay);
                divider = view.findViewById(R.id.view);

                itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, TermsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        if (helpList.get(getAdapterPosition()).getHelpTitle().equals(context.getString(R.string.terms_and_conditions))) {
                            intent.putExtra(Constants.TAG_FROM, "terms");
                        } else {
                            intent.putExtra(Constants.TAG_FROM, "privacy");

                        }
                        startActivity(intent);
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }
}
