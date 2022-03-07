package com.app.jussfun.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.databinding.ActivityContactUsBinding;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactUsActivity extends AppCompatActivity {

    private static final String TAG = BankDetailsActivity.class.getSimpleName();

    private ApiInterface apiInterface;
    private Context mContext;
    ActivityContactUsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactUsBinding.inflate(getLayoutInflater());
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

        binding.toolBarLay.txtTitle.setText(getString(R.string.contact_us));

        binding.toolBarLay.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(binding.edtMessage.getText().toString())) {
                    App.makeToast(mContext.getString(R.string.enter_queries));
                } else {
                    binding.btnSave.setEnabled(false);
                    sendEmail(binding.edtMessage.getText().toString());
                }
            }
        });
    }

    private void sendEmail(String message) {
        if (NetworkReceiver.isConnected()) {
            Call<HashMap<String, String>> call = apiInterface.sendEmail(GetSet.getUserId(), message);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    App.makeToast(getString(R.string.send_successfully));
                    finish();
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    binding.btnSave.setEnabled(true);
                    t.printStackTrace();
                    call.cancel();
                }
            });
        }
    }
}