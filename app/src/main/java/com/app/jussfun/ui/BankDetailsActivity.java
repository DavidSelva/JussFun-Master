package com.app.jussfun.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.databinding.ActivityBankDetailsBinding;
import com.app.jussfun.helper.BannerAdUtils;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.ProfileRequest;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BankDetailsActivity extends BaseFragmentActivity {

    private static final String TAG = BankDetailsActivity.class.getSimpleName();

    private ApiInterface apiInterface;
    private Context mContext;
    ActivityBankDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBankDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        mContext = this;
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

        binding.toolBarLay.txtTitle.setText(getString(R.string.bank_details));
//        binding.toolBarLay.btnSettings.setText(getString(R.string.settings));
        loadAd();

        if (LocaleManager.isRTL()) {
            binding.toolBarLay.btnBack.setRotation(180);
        } else {
            binding.toolBarLay.btnBack.setRotation(0);
        }

        binding.toolBarLay.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.toolBarLay.btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBankDetails();
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(binding.edtAccountNo.getText())) {
                    binding.edtAccountNo.setError(mContext.getString(R.string.enter_bank_account));
                } else if (TextUtils.isEmpty(binding.edtConfirmAccountNo.getText())) {
                    binding.edtConfirmAccountNo.setError(mContext.getString(R.string.confirm_bank_account));
                } else if (TextUtils.isEmpty(binding.edtIfsc.getText())) {
                    binding.edtIfsc.setError(mContext.getString(R.string.enter_ifsc));
                } else if (TextUtils.isEmpty(binding.edtAccountName.getText())) {
                    binding.edtAccountName.setError(mContext.getString(R.string.enter_account_name));
                } else if (!binding.edtAccountNo.getText().toString().equals(binding.edtConfirmAccountNo.getText().toString())) {
                    binding.edtConfirmAccountNo.setError(getString(R.string.bank_account_mismatched));
                } else {
                    binding.btnSave.setEnabled(false);
                    saveBankDetails();
                }
            }
        });
        getBankDetails();
    }

    private void getBankDetails() {
        if (NetworkReceiver.isConnected()) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());
            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        binding.edtAccountNo.setText("" + profile.getBankAccNo());
                        binding.edtConfirmAccountNo.setText("" + profile.getBankAccNo());
                        binding.edtIfsc.setText("" + profile.getBankIfscCode());
                        binding.edtAccountName.setText("" + profile.getBankAccName());
                        if (!TextUtils.isEmpty(binding.edtAccountNo.getText())) {
                            binding.toolBarLay.btnSettings.setVisibility(View.VISIBLE);
                            binding.toolBarLay.btnSettings.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_delete_black));
                        }
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void saveBankDetails() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_ACCOUNT_NUMBER, binding.edtAccountNo.getText().toString());
            requestMap.put(Constants.TAG_IFSC_CODE, binding.edtIfsc.getText().toString());
            requestMap.put(Constants.TAG_ACCOUNT_NAME, binding.edtAccountName.getText().toString());

            Call<ProfileResponse> call = apiInterface.saveBankDetails(requestMap);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
//                    hideLoading();
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        App.makeToast(getString(R.string.bank_details_updated));
                        AppUtils.showAlertSnack(mContext, binding.parentLay, mContext.getString(R.string.bank_details_updated));
                        finish();
                    } else {
                        binding.btnSave.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    binding.btnSave.setEnabled(true);
                    t.printStackTrace();
                    call.cancel();
                }
            });
        }
    }

    private void deleteBankDetails() {
        if (NetworkReceiver.isConnected()) {
            Call<ProfileResponse> call = apiInterface.deleteBankDetails(GetSet.getUserId());
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
//                    hideLoading();
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        App.makeToast(getString(R.string.bank_details_deleted));
                        finish();
                    } else {
                        binding.btnSave.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    binding.btnSave.setEnabled(true);
                    t.printStackTrace();
                    call.cancel();
                }
            });
        }
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            BannerAdUtils.getInstance(this).loadAd(TAG, binding.adView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }
}