package com.app.jussfun.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.app.jussfun.R;
import com.app.jussfun.helper.AdUtils;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.OnOkCancelClickListener;
import com.app.jussfun.model.ConvertGiftRequest;
import com.app.jussfun.model.ConvertGiftResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.SharedPref;
import com.google.android.gms.ads.AdView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.lang.String.format;

public class ConvertGiftActivity extends BaseFragmentActivity {

    private static final String TAG = ConvertGiftActivity.class.getSimpleName();
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtGiftsCount)
    TextView txtGiftsCount;
    @BindView(R.id.btnWithdraw)
    Button btnWithdraw;
    ApiInterface apiInterface;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.btnGems)
    RadioButton btnGems;
    @BindView(R.id.btnCash)
    RadioButton btnCash;
    AlertDialog dialog;
    @BindView(R.id.optionLay)
    RadioGroup optionLay;
    DialogCreditGems alertDialog;
    private AppUtils appUtils;

    DialogPayPal dialogPayPal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_convert_gift);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        btnBack.setColorFilter(ContextCompat.getColor(this, R.color.white));
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        // for convert gifts into money addon
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
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = AppUtils.getStatusBarHeight(getApplicationContext());
        toolbar.setLayoutParams(params);
//        optionLay.setVisibility((("" + AdminData.showMoneyConversion).equals("1")) ? View.VISIBLE : View.GONE);
        loadAd();
        getGems();
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            AdUtils.getInstance(this).loadAd(TAG, adView);
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void getGems() {
        Log.d(TAG, "getGemsCount: " + GetSet.getGifts() + "count" + GetSet.getGiftEarnings());
        if (GetSet.getGiftEarnings() != null)
            txtGiftsCount.setText(format(getString(R.string.gift_to_gems), "" + GetSet.getGifts(), GetSet.getGiftEarnings()));
        else
            txtGiftsCount.setText(format(getString(R.string.gift_to_gems), "" + GetSet.getGifts(), "0"));

    }


    @SuppressLint("StringFormatInvalid")
    private void getcash() {
        if (AdminData.conversionGems != null) {
            Double gems = Double.valueOf(AdminData.conversionGems);
            Double cash = Double.valueOf(AdminData.conversionCash);
            Log.i(TAG, "gems: " + gems);
            Log.i(TAG, "cash: " + cash);
            Log.i(TAG, "giftEarning: " + Double.valueOf(GetSet.getGiftEarnings()));
            Double singleGem = cash / gems;
            Log.i(TAG, "singleGem: " + singleGem);
            Double totalMoney = singleGem * Double.valueOf(GetSet.getGiftEarnings());
            Log.i(TAG, "totalMoney: " + totalMoney);
            //txtGiftsCount.setText(""+totalMoney);

            txtGiftsCount.setText(format(getString(R.string.gift_to_cash), " " + GetSet.getGifts(), "" + totalMoney));


        }
    }


    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

    @OnClick({R.id.btnBack, R.id.btnWithdraw, R.id.btnGems, R.id.btnCash})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnGems:
                getGems();
                btnWithdraw.setText(getString(R.string.convert_gems));
                break;
            case R.id.btnCash:
                if (GetSet.getGiftConversionValue() != null) {
                    if (Float.parseFloat(GetSet.getGiftConversionValue()) > 0) {
                        getcash();
                        txtGiftsCount.setText(String.format(getString(R.string.gift_to_cash), "" + GetSet.getGifts(), GetSet.getGiftConversionValue()));
                    } else {
                        txtGiftsCount.setText(String.format(getString(R.string.gift_to_cash_no), "" + GetSet.getGifts()));
                    }
                } else {
                    txtGiftsCount.setText(String.format(getString(R.string.gift_to_cash_no), "" + GetSet.getGifts()));

                }
                btnWithdraw.setText(R.string.convert_money);
                break;
            case R.id.btnWithdraw:
                if (GetSet.getGifts() > 0) {
                    if (btnGems.isChecked()) {
                        App.preventMultipleClick(btnWithdraw);
                        showConfirmDialog();
                    } else {
                        if (btnCash.isChecked()) {
                            if (SharedPref.getString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id()) != null) {
                                if (!TextUtils.isEmpty(GetSet.getGiftConversionValue()) && Float.parseFloat(GetSet.getGiftConversionValue()) > 0) {
                                    showConvertDialog();
                                } else {
                                    App.makeToast(getString(R.string.gift_to_cash_not_enough));
                                }
                            } else {
                                App.makeToast(getString(R.string.update_paypal_id));
                            }
                        }
//                        App.makeToast(getString(R.string.convert_money_error_desc));
                    }
                } else showAlertDialog(getString(R.string.you_dont_have_any_gifts));
                break;
        }
    }

    private void showConvertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConvertGiftActivity.this);
        builder.setMessage(getString(R.string.convert_gifts_to_money_desc));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updatePayments(dialog);
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_light);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_light);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);
    }

    private void updatePayments(DialogInterface dialog) {
        if (NetworkReceiver.isConnected()) {

            String payPalId = SharedPref.getString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id());

            ConvertGiftRequest request = new ConvertGiftRequest();
            request.setGemsRequested(GetSet.getGiftCoversionEarnings());
            request.setUserId(GetSet.getUserId());
            request.setUserName(GetSet.getUserName());
            request.setPayPalId(payPalId);

            Call<ConvertGiftResponse> call = apiInterface.convertToMoney(request);
            call.enqueue(new Callback<ConvertGiftResponse>() {
                @Override
                public void onResponse(Call<ConvertGiftResponse> call, Response<ConvertGiftResponse> response) {
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        Toast.makeText(ConvertGiftActivity.this, getString(R.string.convert_money_success), Toast.LENGTH_SHORT).show();
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        finish();
                    } else {
                        App.makeToast(getString(R.string.not_enough_gems));
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ConvertGiftResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });

        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }


    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConvertGiftActivity.this);
        builder.setMessage(getString(R.string.convert_gifts_to_gems_desc));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                convertGems(dialog);
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_light);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_light);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);
    }

    private void convertGems(DialogInterface dialog) {
        ConvertGiftRequest request = new ConvertGiftRequest();
        request.setUserId(GetSet.getUserId());
        request.setType(btnGems.isChecked() ? Constants.TAG_GEMS : Constants.TAG_CASH);
        Call<ConvertGiftResponse> call = apiInterface.convertGifts(request);
        call.enqueue(new Callback<ConvertGiftResponse>() {
            @Override
            public void onResponse(Call<ConvertGiftResponse> call, Response<ConvertGiftResponse> response) {
                dialog.cancel();
                if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                    GetSet.setGems(response.body().getTotalGems());
                    SharedPref.putLong(SharedPref.GEMS, GetSet.getGems());
                    getGems();
                    showAlertDialog(getString(R.string.gems_purchase_response));
                }
            }

            @Override
            public void onFailure(Call<ConvertGiftResponse> call, Throwable t) {
                dialog.cancel();
            }
        });
    }

    private void showAlertDialog(String message) {
        alertDialog = new DialogCreditGems();
        alertDialog.setContext(ConvertGiftActivity.this);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(message);
        alertDialog.setCallBack(new OnOkCancelClickListener() {
            @Override
            public void onOkClicked(Object o) {
                alertDialog.dismissAllowingStateLoss();
                finish();
            }

            @Override
            public void onCancelClicked(Object o) {
                alertDialog.dismissAllowingStateLoss();
                finish();
            }
        });
        alertDialog.show(getSupportFragmentManager(), TAG);
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
