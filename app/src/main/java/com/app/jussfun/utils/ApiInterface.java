package com.app.jussfun.utils;


import com.app.jussfun.model.AddDeviceRequest;
import com.app.jussfun.model.AdminMessageResponse;
import com.app.jussfun.model.AppDefaultResponse;
import com.app.jussfun.model.CommonResponse;
import com.app.jussfun.model.ConvertGiftRequest;
import com.app.jussfun.model.ConvertGiftResponse;
import com.app.jussfun.model.FollowRequest;
import com.app.jussfun.model.FollowResponse;
import com.app.jussfun.model.FollowersResponse;
import com.app.jussfun.model.GemsPurchaseRequest;
import com.app.jussfun.model.GemsPurchaseResponse;
import com.app.jussfun.model.GemsStoreResponse;
import com.app.jussfun.model.HelpResponse;
import com.app.jussfun.model.ProfileRequest;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.model.RecentHistoryResponse;
import com.app.jussfun.model.ReferFriendResponse;
import com.app.jussfun.model.RenewalRequest;
import com.app.jussfun.model.ReportRequest;
import com.app.jussfun.model.ReportResponse;
import com.app.jussfun.model.SearchUserResponse;
import com.app.jussfun.model.SignInRequest;
import com.app.jussfun.model.SignInResponse;
import com.app.jussfun.model.SubscriptionRequest;
import com.app.jussfun.model.SubscriptionResponse;
import com.app.jussfun.model.TermsResponse;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiInterface {

    @GET("activities/appdefaults/{platform}")
    Call<AppDefaultResponse> getAppDefaultData(@Path("platform") String platform);

    @Multipart
    @POST("accounts/upmychat")
    Call<Map<String, String>> uploadAudio(@Part MultipartBody.Part image, @Part("user_id") RequestBody user_id);

    @POST("giftconversions/gifttomoneyconversion")
    Call<ConvertGiftResponse> convertToMoney(@Body ConvertGiftRequest request);

    @POST("accounts/signin")
    Call<SignInResponse> callSignIn(@Body SignInRequest request);

    @POST("accounts/signup")
    Call<SignInResponse> callSignUp(@Body SignInRequest request);

    @Multipart
    @POST("accounts/uploadprofile")
    Call<Map<String, String>> uploadProfileImage(@Part MultipartBody.Part image, @Part("user_id") RequestBody user_id);

    @Multipart
    @POST("accounts/upmychat")
    Call<Map<String, String>> uploadChatImage(@Part MultipartBody.Part image, @Part("user_id") RequestBody user_id);

    @POST("devices/register")
    Call<Map<String, String>> pushSignIn(@Body AddDeviceRequest request);

    @DELETE("devices/unregister/{device_id}")
    Call<HashMap<String, String>> pushSignOut(@Path("device_id") String deviceId);

    @POST("accounts/profile")
    Call<ProfileResponse> getProfile(@Body ProfileRequest request);

    @GET("activities/followerslist/{user_id}/{offset}/{limit}")
    Call<FollowersResponse> getFollowers(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @GET("activities/followingslist/{user_id}/{offset}/{limit}")
    Call<FollowersResponse> getFollowings(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @POST("activities/follow")
    Call<FollowResponse> followUser(@Body FollowRequest followRequest);

    @GET("activities/interestlist/{user_id}/{offset}/{limit}")
    Call<FollowersResponse> getInterestList(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @GET("activities/friendslist/{user_id}/{offset}/{limit}")
    Call<FollowersResponse> getFriendsList(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @GET("activities/blockeduserslist/{user_id}/{offset}/{limit}")
    Call<SearchUserResponse> getBlockedList(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @FormUrlEncoded
    @POST("activities/blockuser")
    Call<HashMap<String, String>> blockUser(@FieldMap HashMap<String, String> requestMap);

    @FormUrlEncoded
    @POST("activities/unlockinterest")
    Call<HashMap<String, String>> unlockUser(@FieldMap HashMap<String, String> requestMap);

    @FormUrlEncoded
    @POST("accounts/searchuser")
    Call<SearchUserResponse> searchByUserName(@FieldMap HashMap<String, String> requestMap);

    @GET("helps/loginterms")
    Call<TermsResponse> getTerms();

    @GET("helps/allterms")
    Call<HelpResponse> getHelps();

    @GET("accounts/isActive/{user_id}")
    Call<HashMap<String, String>> isActive(@Path("user_id") String userId);

    @GET("chats/recentvideochats/{user_id}/{offset}/{limit}")
    Call<RecentHistoryResponse> getRecentHistory(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @POST("activities/reportuser")
    Call<ReportResponse> reportUser(@Body ReportRequest request);

    @Multipart
    @POST("activities/uploadreport")
    Call<CommonResponse> reportScreenUpload(@Part MultipartBody.Part image, @Part("report_id") RequestBody reportId);

    @Multipart
    @POST("accounts/upmyvideo")
    Call<CommonResponse> chatScreenUpload(@Part MultipartBody.Part image, @Part("user_id") RequestBody userId, @Part("partner_id") RequestBody partnerId);

    @GET("activities/gemstore/{user_id}/{platform}/{offset}/{limit}")
    Call<GemsStoreResponse> getGemsList(@Path("user_id") String userId, @Path("platform") String platform, @Path("offset") int offset, @Path("limit") int limit);

    @POST("payments/subscription")
    Call<SubscriptionResponse> paySubscription(@Body SubscriptionRequest request);

    @POST("payments/purchasegems")
    Call<GemsPurchaseResponse> buyGems(@Body GemsPurchaseRequest request);

    @POST("activities/gifttogems")
    Call<ConvertGiftResponse> convertGifts(@Body ConvertGiftRequest request);

    @GET("accounts/invitecredits/{referal_code}")
    Call<ReferFriendResponse> updateReferal(@Path("referal_code") String referalCode);

    @GET("accounts/rewardvideo/{user_id}")
    Call<Map<String, String>> updateVideoGems(@Path("user_id") String userId);

    @GET("activities/adminmessages/{user_id}/{platform}/{update_from}/{timestamp}")
    Call<AdminMessageResponse> getAdminMessages(@Path("user_id") String userId, @Path("platform") String platform, @Path("update_from") String updateFrom, @Path("timestamp") long currentTimeMillis);

    @GET("chats/clearvideochats/{user_id}")
    Call<HashMap<String, String>> clearRecentHistory(@Path("user_id") String userId);

    @GET("accounts/chargecalls/{user_id}")
    Call<HashMap<String, String>> chargeCalls(@Path("user_id") String userId);

    @POST("payments/renewalsubscription")
    Call<HashMap<String, String>> verifyPayment(@Body RenewalRequest renewalRequest);


    @FormUrlEncoded
    @POST("accounts/nearbyusers")
    Call<FollowersResponse> getNearByUsers(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("activities/interest")
    Call<Map<String, String>> interestOnUser(@FieldMap Map<String, String> params);

    @GET("accounts/chargefilters/{user_id}")
    Call<HashMap<String, String>> chargeSwipeFilters(@Path("user_id") String userId);

}
