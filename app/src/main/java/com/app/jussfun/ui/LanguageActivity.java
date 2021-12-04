package com.app.jussfun.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.model.LanguageData;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.SharedPref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LanguageActivity extends BaseFragmentActivity {


    private static final String TAG = LanguageActivity.class.getSimpleName();
    RecyclerView recyclerView;
    TextView txtTitle;
    ImageView btnBack;
    private String[] resourceArray;
    List<String> langAry = new ArrayList<>();
    LanguageAdapter languageAdapter;

    private String from;
    ApiInterface apiInterface;
    List<LanguageData> languageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        btnBack = (ImageView) findViewById(R.id.btnBack);


        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        }


        if (from != null && from.equals(Constants.TAG_CHAT)) {
            LanguageData data = new LanguageData();
            String lang = "None";
            String lan = "";
            data.language = lang;
            data.languageCode = lan;
            languageList.add(data);
            Log.i(TAG, "onCreatelanga: " + data.language + ":" + data.languageCode);
        } else {
            resourceArray = getResources().getStringArray(R.array.language_array);
            List<String> tempList = Arrays.asList(resourceArray);
            for (String list : tempList) {
                String lang[] = list.split("-");
                String langTitle = lang[0];
                String langCode = lang[1];
                LanguageData data = new LanguageData();
                data.language = langTitle;
                data.languageCode = langCode;
                languageList.add(data);
            }

        }


        txtTitle.setText(R.string.language);
      /*  resourceArray = getResources().getStringArray(R.array.language_array);
        langAry = Arrays.asList(resourceArray);*/

        final LinearLayoutManager layoutManager = new LinearLayoutManager(LanguageActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        languageAdapter = new LanguageAdapter(this, languageList);
        recyclerView.setAdapter(languageAdapter);

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        LocaleManager.getLanguageName(this);


    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    public class LanguageAdapter extends RecyclerView.Adapter {
        List<LanguageData> languageList = new ArrayList<>();
        Context context;
        private RecyclerView.ViewHolder viewHolder;

        public LanguageAdapter(Context context, List<LanguageData> languageList) {
            this.languageList = languageList;
            this.context = context;

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_language, parent, false);
            viewHolder = new MyViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final LanguageData data = languageList.get(position);

            ((MyViewHolder) holder).langTitle.setText(data.language);

            if (from != null && from.equals(Constants.TAG_CHAT)) {
                Log.i(TAG, "langonBindViewHolder: " + from);
                if (data.languageCode.equals(SharedPref.getString(SharedPref.CHAT_LANGUAGE, SharedPref.DEFAULT_CHAT_LANGUAGE))) {
                    ((MyViewHolder) holder).tick.setVisibility(View.VISIBLE);
                } else {
                    ((MyViewHolder) holder).tick.setVisibility(View.GONE);
                }
            } else {
                Log.i(TAG, "langonBindViewHolders: " + from);
                if (data.languageCode.equals(LocaleManager.getLanguageCode(context))) {
                    ((MyViewHolder) holder).tick.setVisibility(View.VISIBLE);
                } else {
                    ((MyViewHolder) holder).tick.setVisibility(View.GONE);
                }
            }


            ((MyViewHolder) holder).mainLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (from != null && from.equals(Constants.TAG_CHAT)) {
                        Log.i(TAG, "langonBindViewHoldersma: " + from);
                        setTranslateLanguage(data.languageCode);
                    } else {
                        Log.i(TAG, "langonBindViewHoldersmai: " + data.language + "Code" + data.languageCode);
                        setNewLocale(context, data.languageCode, data.language);
                    }


                }
            });

        }

        private void setTranslateLanguage(String languageCode) {
            languageAdapter.notifyDataSetChanged();
            SharedPref.putString(SharedPref.CHAT_LANGUAGE, languageCode);
            finish();
        }

        private void setNewLocale(Context context, String languageCode, String languageName) {
            languageAdapter.notifyDataSetChanged();
            SharedPref.putString(Constants.TAG_LANGUAGE_CODE, languageCode);
            LocaleManager.setNewLocale(context, languageCode, languageName);
            Log.d(TAG, "setNewLocale: " + languageCode + "LangName" + languageName);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        @Override
        public int getItemCount() {
            return languageList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tick)
            ImageView tick;
            @BindView(R.id.mainLay)
            RelativeLayout mainLay;
            @BindView(R.id.helpTitle)
            TextView langTitle;


            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);

            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResultlanguag: " + requestCode);
        if (requestCode == Constants.LANGUAGE_REQUEST_CODE) {
            Log.i(TAG, "onActivityResultlangua: " + requestCode);
        }
    }
}
