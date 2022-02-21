package com.app.jussfun.helper;

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;

import org.json.JSONArray;

public class ResponseJsonClass {

    private static final String TAG = ResponseJsonClass.class.getSimpleName();
    Context mContext;
    public onSocialLoginCallback socialLoginCallback;
    public onLikePostCallback likeCallback;
    public onAddCommentCallback addCommentCallback;
    public onLikeCommentCallback onlikecommentCallback;
    public onHashTagCallback onhashtagCallback;
    public SaveSearchCallback saveSearchCallback;
    public SearchHistorycallback searchHistorycallback;
    public ServiceCallback serviceCallback;
    public FollowCallback followCallback;
    private StorageUtils storageManager;
    ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

    public ResponseJsonClass(Context context) {
        this.mContext = context;
    }

    public interface onLikePostCallback {
        public abstract void onPostLikeStatus(String status, int position, String likeCount, RecyclerView.ViewHolder mediaListViewHolder);
    }

    public interface onLikeCommentCallback {
        public abstract void onLikeCommentStatus(String status, int position, int likeCount, RecyclerView.ViewHolder holder);
    }

    public interface onAddCommentCallback {
        public abstract void onAddCommentStatus(String status, Object object, String cmtCount);

        public abstract void onDeleteCommentStatus(String status, String cmtCount, int position);

        public abstract void onDeleteReplyStatus(String status, String cmtCount, int position, int childPosition);

        public abstract void onAddReplyStatus(String status, Object object, int parentPosition, int childPosition);
    }

    public interface onHashTagCallback {
        public abstract void onStatus(String status, Object list1, Object list2);
    }

    public interface onSocialLoginCallback {
        public abstract void onStatus(String status, String userid, String username, String userimage, String full_name, String first_time_logged, String token);
    }

    public interface SaveSearchCallback {
        public abstract void saveSearchStatus(String status);
    }

    public interface SearchHistorycallback {
//        public abstract void setSearchHistoryResult(List<SearchResponse.ResultsItem> list, String searchType);
    }

    public interface ServiceCallback {
        public abstract void Reportcallback(String way);
    }

    public interface FollowCallback {
        public abstract void Followcallback(int position, String followerid, String result, String followersCount, String mutualStatus, RecyclerView.ViewHolder viewHolder);
    }

    public void setSocialLoginCallback(onSocialLoginCallback socialLoginCallback) {
        this.socialLoginCallback = socialLoginCallback;
    }

    public void setCallback(onLikePostCallback likeCallback) {
        this.likeCallback = likeCallback;
    }

    public void setLikeCommentCallback(onLikeCommentCallback onlikecommentCallback) {
        this.onlikecommentCallback = onlikecommentCallback;
    }

    public void setAddCommentCallback(onAddCommentCallback addCommentCallback) {
        this.addCommentCallback = addCommentCallback;
    }

    public void setHashTagCallback(onHashTagCallback onhashtagCallback) {
        this.onhashtagCallback = onhashtagCallback;
    }

    public void setSaveSearchCallback(SaveSearchCallback saveSearchCallback) {
        this.saveSearchCallback = saveSearchCallback;
    }

    public void setSearchHistorycallback(SearchHistorycallback searchHistorycallback) {
        this.searchHistorycallback = searchHistorycallback;
    }

    public void setServiceCallback(ServiceCallback serviceCallback) {
        this.serviceCallback = serviceCallback;
    }

    public void setFollowCallback(FollowCallback followCallback) {
        this.followCallback = followCallback;
    }

    public String getResult(JSONArray jsonArray, int pos, String paramName) {

        String res = "";

        try {

            res = jsonArray.getJSONObject(pos).has(paramName) ? jsonArray.getJSONObject(pos).getString(paramName).toString() : "";

        } catch (Exception e) {
            Log.e("ex", "-" + e.toString());
        }

        return res;

    }

}
