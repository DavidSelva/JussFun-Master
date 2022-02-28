package com.app.jussfun.helper.callback;

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.base.App;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.Constants;

import org.json.JSONArray;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class ResponseJsonClass {

    private static final String TAG = ResponseJsonClass.class.getSimpleName();
    Context mContext;
    public onSocialLoginCallback socialLoginCallback;
    public onLikePostCallback likeCallback;
    public onAddCommentCallback addCommentCallback;
    public onLikeCommentCallback onLikecommentCallback;
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

        public abstract void onDeleteCommentStatus(String status, int position);

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
        this.onLikecommentCallback = onlikecommentCallback;
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

    // Add comment
    public void addComment(String postId, String comments, String type) {

        String parentId = "";               /// pass empty as parentId because add comment
        Call<Map<String, String>> call = apiService.addComment(GetSet.getUserId(), postId, comments, parentId, type);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, retrofit2.Response<Map<String, String>> response) {


                if (response.isSuccessful()) {
                    addCommentCallback.onAddCommentStatus(response.body().get(Constants.TAG_STATUS), response.body(), "");
                } else {
                    App.makeToast("Something went wrong");
                }

            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e("onFailure", "->" + t.toString());
                App.makeToast("Something went wrong");

            }
        });

    }

    // Add Reply
    public void addReply(String postId, String comments, String parentId, int parentPosition, int childPosition, String type) {

        Call<Map<String, String>> call = apiService.addReply(GetSet.getUserId(), postId, comments, parentId, type);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, retrofit2.Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    addCommentCallback.onAddReplyStatus(response.body().get(Constants.TAG_STATUS), response.body(), parentPosition, childPosition);
                } else {
                    App.makeToast("Something went wrong");
                }

            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e("onFailure", "->" + t.toString());
                App.makeToast("Something went wrong");

            }
        });

    }

    // Like Comment
    public void likeComment(int position, String commentId, int likeCount, RecyclerView.ViewHolder holder) {

        int count[] = {1};
        count[0] = likeCount + 1;
        Call<Map<String, String>> call = apiService.likeComment(GetSet.getUserId(), commentId);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, retrofit2.Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    onLikecommentCallback.onLikeCommentStatus(response.body().get(Constants.TAG_STATUS), position, count[0], holder);
                } else {
                    App.makeToast("Something went wrong");
                }

            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e("onFailure", "->" + t.toString());
                App.makeToast("Something went wrong");

            }
        });

    }

    // Like Reply
    public void likeReply(int position, String replyId, String commentId, int likeCount, RecyclerView.ViewHolder holder) {

        Call<Map<String, String>> call = apiService.likeReplyComment(GetSet.getUserId(), replyId, commentId);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, retrofit2.Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    onLikecommentCallback.onLikeCommentStatus(response.body().get(Constants.TAG_STATUS), position, likeCount, holder);
                } else {
                    App.makeToast("Something went wrong");
                }

            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e("onFailure", "->" + t.toString());
                App.makeToast("Something went wrong");

            }
        });

    }

    public void deleteComment(int position, int childPosition, String postId, String commentId, String type) {
        Call<Map<String, String>> call = apiService.deleteComment(GetSet.getUserId(), postId, commentId);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, retrofit2.Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    if (type.equalsIgnoreCase("reply")) {
                        addCommentCallback.onDeleteReplyStatus(response.body().get(Constants.TAG_STATUS), "" + 0, position, childPosition);
                    } else {
                        addCommentCallback.onDeleteCommentStatus(response.body().get(Constants.TAG_STATUS), position);
                    }
                } else {
                    App.makeToast("Something went wrong");
                }

            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e("onFailure", "->" + t.toString());
                App.makeToast("Something went wrong");

            }
        });
    }

}
