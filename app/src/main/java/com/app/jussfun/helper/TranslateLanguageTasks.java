package com.app.jussfun.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.app.jussfun.utils.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.Context.MODE_PRIVATE;

public abstract class TranslateLanguageTasks extends AsyncTask<String,String,String> {

    private static final String TAG = TranslateLanguageTasks.class.getSimpleName();
    private Context mContext;
    private SharedPreferences pref;
    String messagesData;
    boolean from;
    boolean instant;

    public TranslateLanguageTasks(Context mContext, String messageData,boolean from) {
        this.messagesData = messageData;
        this.mContext = mContext;
        this.from=from;
        Log.i(TAG, "TranslateLanguageTasks: "+from);
        pref = mContext.getSharedPreferences("SavedPref", MODE_PRIVATE);
    }


    @Override
    protected String doInBackground(String... data) {
        String translatedMsg = getTranslatedMessage(messagesData);
        return translatedMsg;
    }

    @Override
    protected abstract void onPostExecute(String messagesData);

    private String getTranslatedMessage(String message) {
        if (from == true){
            Log.i(TAG, "TranslateLanguageTasksgetTranslatedMessage: "+from);
            String url = "https://translation.googleapis.com/language/translate/v2?target=" +
                    SharedPref.getString(SharedPref.CHAT_LANGUAGE, SharedPref.DEFAULT_CHAT_LANGUAGE) +
                    "&key=" + "AIzaSyCnwt_jj_Pc9UPtQ0mLRAZT6rxXQ3WGFIA" + "&q=" + message;
            Log.i(TAG, "TranslateLanguageTasksgetTranslatedMessages: " + url);
            BufferedReader reader = null;
            try {
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept-Charset", "UTF-8");

                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                String translatedMsg = parseTranslatedMsg(sb.toString());
                return translatedMsg;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            Log.i(TAG, "TranslateLanguageTasksgetTranslatedMessageelse: "+from);
        }

        return message;
    }

    private static String parseTranslatedMsg(String translatedMsg) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(translatedMsg);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            JSONArray translationArray = dataObject.getJSONArray("translations");
            if (translationArray.length() > 0) {
                JSONObject translatedText = translationArray.getJSONObject(0);
                translatedMsg = translatedText.optString("translatedText");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return translatedMsg;
    }
}
