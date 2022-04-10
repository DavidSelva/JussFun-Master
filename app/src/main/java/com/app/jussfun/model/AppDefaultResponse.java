
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AppDefaultResponse {

    @SerializedName("freegems")
    private Long freeGems;
    @SerializedName("gifts")
    private List<Gift> gifts;
    @SerializedName("gifts_details")
    private GiftsDetails giftsDetails;
    @SerializedName("prime_details")
    private PrimeDetails primeDetails;
    @SerializedName("reports")
    private List<Report> reports;
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("locations")
    private List<String> locations;
    @SerializedName("membership_packages")
    private List<MembershipPackages> membershipPackages;
    @SerializedName("filter_gems")
    private FilterGems filterGems;
    @SerializedName("filter_options")
    private FilterOptions filterOptions;
    @SerializedName("invite_credits")
    private Long inviteCredits;
    @SerializedName("google_ads_client")
    private String googleAdsClient;
    @SerializedName("show_ads")
    private String showAds;
    @SerializedName("video_ads")
    private String videoAds;
    @SerializedName("contact_email")
    private String contactEmail;
    @SerializedName("welcome_message")
    private String welcomeMessage;
    @SerializedName("show_money_conversion")
    private String showMoneyConversion;
    @SerializedName("schedule_video_ads")
    private Long videoAdsDuration;
    @SerializedName("video_ads_client")
    private String videoAdsClient;
    @SerializedName("video_calls")
    private Long videoCalls;
    @SerializedName("gemprime")
    private String gemConversion;
    @SerializedName("giftprime")
    private String giftConversion;
    @SerializedName("feed_title")
    private String feedTitle;
    @SerializedName("feed_description")
    private String feedDescription;

    public Long getFreeGems() {
        return freeGems;
    }

    public void setFreeGems(Long freeGems) {
        this.freeGems = freeGems;
    }

    public List<Gift> getGifts() {
        return gifts;
    }

    public void setGifts(List<Gift> gifts) {
        this.gifts = gifts;
    }

    public GiftsDetails getGiftsDetails() {
        return giftsDetails;
    }

    public void setGiftsDetails(GiftsDetails giftsDetails) {
        this.giftsDetails = giftsDetails;
    }

    public PrimeDetails getPrimeDetails() {
        return primeDetails;
    }

    public void setPrimeDetails(PrimeDetails primeDetails) {
        this.primeDetails = primeDetails;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public List<MembershipPackages> getMembershipPackages() {
        return membershipPackages;
    }

    public void setMembershipPackages(List<MembershipPackages> membershipPackages) {
        this.membershipPackages = membershipPackages;
    }

    public FilterGems getFilterGems() {
        return filterGems;
    }

    public void setFilterGems(FilterGems filterGems) {
        this.filterGems = filterGems;
    }

    public FilterOptions getFilterOptions() {
        return filterOptions;
    }

    public void setFilterOptions(FilterOptions filterOptions) {
        this.filterOptions = filterOptions;
    }

    public Long getInviteCredits() {
        return inviteCredits;
    }

    public void setInviteCredits(Long inviteCredits) {
        this.inviteCredits = inviteCredits;
    }

    public String getGoogleAdsClient() {
        return googleAdsClient;
    }

    public void setGoogleAdsClient(String googleAdsClient) {
        this.googleAdsClient = googleAdsClient;
    }

    public String getShowAds() {
        return showAds;
    }

    public void setShowAds(String showAds) {
        this.showAds = showAds;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public String getVideoAds() {
        return videoAds;
    }

    public void setVideoAds(String videoAds) {
        this.videoAds = videoAds;
    }

    public String getShowMoneyConversion() {
        return showMoneyConversion;
    }

    public void setShowMoneyConversion(String showMoneyConversion) {
        this.showMoneyConversion = showMoneyConversion;
    }

    public String getVideoAdsClient() {
        return videoAdsClient;
    }

    public void setVideoAdsClient(String videoAdsClient) {
        this.videoAdsClient = videoAdsClient;
    }

    public Long getVideoAdsDuration() {
        return videoAdsDuration;
    }

    public void setVideoAdsDuration(Long videoAdsDuration) {
        this.videoAdsDuration = videoAdsDuration;
    }

    public Long getVideoCalls() {
        return videoCalls;
    }

    public void setVideoCalls(Long videoCalls) {
        this.videoCalls = videoCalls;
    }

    public String getGemConversion() {
        return gemConversion;
    }

    public void setGemConversion(String gemConversion) {
        this.gemConversion = gemConversion;
    }

    public String getGiftConversion() {
        return giftConversion;
    }

    public void setGiftConversion(String giftConversion) {
        this.giftConversion = giftConversion;
    }

    public String getFeedTitle() {
        return feedTitle;
    }

    public String getFeedDescription() {
        return feedDescription;
    }
}
