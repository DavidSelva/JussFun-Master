package com.app.jussfun.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.app.jussfun.R;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.ThemeHelper;
import com.app.jussfun.model.TermsResponse;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TermsActivity extends BaseFragmentActivity {

    private static final String TAG = TermsActivity.class.getSimpleName();
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.parentLay)
    RelativeLayout parentLay;
    ApiInterface apiInterface;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        btnBack.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);


        if(LocaleManager.isRTL()){
            btnBack.setRotation(180);
        }else{
            btnBack.setRotation(0);
        }


        initWebView();
        if (getIntent().hasExtra(Constants.TAG_TITLE)) {
            setTitle(getIntent().getStringExtra(Constants.TAG_TITLE));
            loadLink(getIntent().getStringExtra(Constants.TAG_DESCRIPTION));
        } else {
            getTerms();
        }

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

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

    private void initWebView() {
        WebSettings webSetting = webView.getSettings();
        webSetting.setBuiltInZoomControls(true);
        webView.setWebViewClient(new WebViewClient());
        webSetting.setJavaScriptEnabled(true);
        webView.clearCache(true);
    }

    private void getTerms() {
        Call<TermsResponse> call = apiInterface.getTerms();
        call.enqueue(new Callback<TermsResponse>() {
            @Override
            public void onResponse(Call<TermsResponse> call, Response<TermsResponse> response) {
                TermsResponse termsResponse = response.body();
                if (termsResponse.getStatus().equals(Constants.TAG_TRUE) && termsResponse.getTerms() != null) {
                    setTitle(termsResponse.getTerms().getHelpTitle());
                    loadLink(termsResponse.getTerms().getHelpDescrip());
                }
            }

            @Override
            public void onFailure(Call<TermsResponse> call, Throwable t) {

            }
        });
    }

    public void setTitle(String title) {
        txtTitle.setVisibility(View.VISIBLE);
        txtTitle.setText(title);
        txtTitle.setTextColor(getResources().getColor(R.color.colorBlack));
    }

    public void loadLink(String helpDescrip) {
        try {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            String themePref = sharedPreferences.getString("themePref", ThemeHelper.DEFAULT_MODE);
            if (themePref.equals(ThemeHelper.LIGHT_MODE)) {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
                }
            } else if (themePref.equals(ThemeHelper.DARK_MODE)) {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                }
            } else {
                int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                Log.d(TAG, "initWebView: " + currentNightMode);
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                            WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                        } else {
                            WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                            WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
                        }
                        break;
                }
                webView.getSettings().setJavaScriptEnabled(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        webView.loadData(helpDescrip, "text/html", "UTF-8");
    }

    public class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // TODO Auto-generated method stub
            proceedUrl(view, url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }

    }

    private void proceedUrl(WebView view, String url) {
        if (url.startsWith("mailto:")) {
            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
        } else if (url.startsWith("tel:")) {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
        } else {
            view.loadUrl(url);
        }
    }

    @OnClick(R.id.btnBack)
    public void onViewClicked() {
        finish();
    }
}
