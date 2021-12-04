package com.app.jussfun.model;

public class GetSet {
    private static boolean isLogged = false;
    private static String userId = null;
    private static String name = null;
    private static String userName = null;
    private static String authToken = null;
    private static Long gems = null;
    private static String Password = null;
    private static String userImage = null;
    private static String dob = null;
    private static String age = null;
    private static String loginId = null;
    private static String gender = null;
    private static String loginType = null;
    private static Long gifts = 0L;
    private static Long videos = 0L;
    private static String followersCount = null;
    private static String followingCount = null;
    private static int friendsCount = 0;
    private static int interestsCount = 0;
    private static int unlocksLeft = 0;
    private static String location = null;
    private static String premiumMember = null;
    private static String premiumExpiry = null;
    private static boolean filterApplied;
    private static boolean locationApplied;
    private static String filterLocation = null;
    private static String filterMinAge = null;
    private static String filterMaxAge = null;
    private static String filterGender = null;
    private static String filterGems = null;
    private static String cameraGems = null;
    private static String privacyAge = null;
    private static String privacyContactMe = null;
    private static String showNotification = null;
    private static String chatNotification = null;
    private static String followNotification = null;
    private static boolean interestNotification;
    private static String giftEarnings = null;
    private static String referalLink = null;
    private static String createdAt = null;
    private static boolean oncePurchased;

    private static String paypal_id = null;
    private static String giftCoversionEarnings = null;
    private static String giftConversionValue = null;

    public static String getGiftConversionValue() {
        return giftConversionValue;
    }

    public static void setGiftConversionValue(String giftConversionValue) {
        GetSet.giftConversionValue = giftConversionValue;
    }

    public static String getGiftCoversionEarnings() {
        return giftCoversionEarnings;
    }

    public static void setGiftCoversionEarnings(String giftCoversionEarnings) {
        GetSet.giftCoversionEarnings = giftCoversionEarnings;
    }


    public static String getPaypal_id() {
        return paypal_id;
    }

    public static void setPaypal_id(String paypal_id) {
        GetSet.paypal_id = paypal_id;
    }

    public static boolean isLogged() {
        return isLogged;
    }

    public static void setLogged(boolean isLogged) {
        GetSet.isLogged = isLogged;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        GetSet.userId = userId;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        GetSet.name = name;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        GetSet.userName = userName;
    }

    public static Long getGems() {
        return gems;
    }

    public static void setGems(Long gems) {
        GetSet.gems = gems;
    }

    public static String getPassword() {
        return Password;
    }

    public static void setPassword(String password) {
        Password = password;
    }

    public static String getUserImage() {
        return userImage;
    }

    public static void setUserImage(String userImage) {
        GetSet.userImage = userImage;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void setAuthToken(String authToken) {
        GetSet.authToken = authToken;
    }

    public static String getDob() {
        return dob;
    }

    public static void setDob(String dob) {
        GetSet.dob = dob;
    }

    public static String getAge() {
        return age;
    }

    public static void setAge(String age) {
        GetSet.age = age;
    }

    public static void setLoginId(String loginId) {
        GetSet.loginId = loginId;
    }

    public static String getLoginId() {
        return loginId;
    }

    public static String getGender() {
        return gender;
    }

    public static void setGender(String gender) {
        GetSet.gender = gender;
    }

    public static String getLoginType() {
        return loginType;
    }

    public static void setLoginType(String loginType) {
        GetSet.loginType = loginType;
    }

    public static boolean isIsLogged() {
        return isLogged;
    }

    public static void setIsLogged(boolean isLogged) {
        GetSet.isLogged = isLogged;
    }

    public static Long getGifts() {
        return gifts;
    }

    public static void setGifts(Long gifts) {
        GetSet.gifts = gifts;
    }


    public static Long getVideos() {
        return videos;
    }

    public static void setVideos(Long videos) {
        GetSet.videos = videos;
    }

    public static String getFollowersCount() {
        return followersCount;
    }

    public static void setFollowersCount(String followersCount) {
        GetSet.followersCount = followersCount;
    }

    public static String getFollowingCount() {
        return followingCount;
    }

    public static void setFollowingCount(String followingCount) {
        GetSet.followingCount = followingCount;
    }

    public static String getLocation() {
        return location;
    }

    public static void setLocation(String location) {
        GetSet.location = location;
    }

    public static String getPremiumMember() {
        return premiumMember;
    }

    public static void setPremiumMember(String premiumMember) {
        GetSet.premiumMember = premiumMember;
    }

    public static String getPremiumExpiry() {
        return premiumExpiry;
    }

    public static void setPremiumExpiry(String premiumExpiry) {
        GetSet.premiumExpiry = premiumExpiry;
    }

    public static boolean isFilterApplied() {
        return filterApplied;
    }

    public static void setFilterApplied(boolean filterApplied) {
        GetSet.filterApplied = filterApplied;
    }

    public static String getFilterLocation() {
        return filterLocation;
    }

    public static void setFilterLocation(String filterLocation) {
        GetSet.filterLocation = filterLocation;
    }

    public static String getFilterMinAge() {
        return filterMinAge;
    }

    public static void setFilterMinAge(String filterMinAge) {
        GetSet.filterMinAge = filterMinAge;
    }

    public static String getFilterMaxAge() {
        return filterMaxAge;
    }

    public static void setFilterMaxAge(String filterMaxAge) {
        GetSet.filterMaxAge = filterMaxAge;
    }

    public static String getFilterGender() {
        return filterGender;
    }

    public static void setFilterGender(String filterGender) {
        GetSet.filterGender = filterGender;
    }

    public static String getPrivacyAge() {
        return privacyAge;
    }

    public static void setPrivacyAge(String privacyAge) {
        GetSet.privacyAge = privacyAge;
    }

    public static String getPrivacyContactMe() {
        return privacyContactMe;
    }

    public static void setPrivacyContactMe(String privacyContactMe) {
        GetSet.privacyContactMe = privacyContactMe;
    }

    public static String getReferalLink() {
        return referalLink;
    }

    public static void setReferalLink(String referalLink) {
        GetSet.referalLink = referalLink;
    }

    public static String getFilterGems() {
        return filterGems;
    }

    public static void setFilterGems(String filterGems) {
        GetSet.filterGems = filterGems;
    }

    public static String getCameraGems() {
        return cameraGems;
    }

    public static void setCameraGems(String cameraGems) {
        GetSet.cameraGems = cameraGems;
    }

    public static boolean isOncePurchased() {
        return oncePurchased;
    }

    public static void setOncePurchased(boolean oncePurchased) {
        GetSet.oncePurchased = oncePurchased;
    }

    public static boolean isLocationApplied() {
        return locationApplied;
    }

    public static void setLocationApplied(boolean locationApplied) {
        GetSet.locationApplied = locationApplied;
    }

    public static String getShowNotification() {
        return showNotification;
    }

    public static void setShowNotification(String showNotification) {
        GetSet.showNotification = showNotification;
    }

    public static String getChatNotification() {
        return chatNotification;
    }

    public static void setChatNotification(String chatNotification) {
        GetSet.chatNotification = chatNotification;
    }

    public static String getFollowNotification() {
        return followNotification;
    }

    public static void setFollowNotification(String followNotification) {
        GetSet.followNotification = followNotification;
    }

    public static String getGiftEarnings() {
        return giftEarnings;
    }

    public static void setGiftEarnings(String giftEarnings) {
        GetSet.giftEarnings = giftEarnings;
    }

    public static String getCreatedAt() {
        return createdAt;
    }

    public static void setCreatedAt(String createdAt) {
        GetSet.createdAt = createdAt;
    }

    public static int getFriendsCount() {
        return friendsCount;
    }

    public static void setFriendsCount(int friendsCount) {
        GetSet.friendsCount = friendsCount;
    }

    public static int getInterestsCount() {
        return interestsCount;
    }

    public static void setInterestsCount(int interestsCount) {
        GetSet.interestsCount = interestsCount;
    }

    public static boolean getInterestNotification() {
        return interestNotification;
    }

    public static void setInterestNotification(boolean interestNotification) {
        GetSet.interestNotification = interestNotification;
    }

    public static int getUnlocksLeft() {
        return unlocksLeft;
    }

    public static void setUnlocksLeft(int unlocksLeft) {
        GetSet.unlocksLeft = unlocksLeft;
    }

    public static void reset() {
        GetSet.setLogged(false);
        GetSet.setAuthToken(null);
        GetSet.setPassword(null);
        GetSet.setUserId(null);
        GetSet.setName(null);
        GetSet.setUserName(null);
        GetSet.setUserImage(null);
        GetSet.setAge(null);
        GetSet.setGems(0L);
        GetSet.setGifts(0L);
        GetSet.setVideos(0L);
        GetSet.setFollowingCount(null);
        GetSet.setFollowersCount(null);
        GetSet.setFriendsCount(0);
        GetSet.setInterestsCount(0);
        GetSet.setUnlocksLeft(0);
        GetSet.setLocation(null);
        GetSet.setLoginType(null);
        GetSet.setPrivacyAge(null);
        GetSet.setPrivacyContactMe(null);
        GetSet.setShowNotification(null);
        GetSet.setChatNotification(null);
        GetSet.setFollowNotification(null);
        GetSet.setFilterApplied(false);
        GetSet.setLocationApplied(false);
        GetSet.setFilterGender(null);
        GetSet.setFilterMinAge(null);
        GetSet.setFilterMaxAge(null);
        GetSet.setFilterLocation(null);
        GetSet.setPremiumMember(null);
        GetSet.setPremiumExpiry(null);
        GetSet.setCameraGems(null);
        GetSet.setFilterGems(null);
        GetSet.setOncePurchased(false);
        GetSet.setGiftEarnings(null);
        GetSet.setCreatedAt(null);

        GetSet.setPaypal_id(null);
        GetSet.setGiftCoversionEarnings(null);
        GetSet.setGiftConversionValue(null);

        GetSet.setInterestNotification(false);
    }
}
