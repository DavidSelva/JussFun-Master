package com.app.jussfun.utils;

public class Constants {

    public static final String BASE_URL = "jussfun.com"; // site main url
    public static final String SITE_URL = "https://" + BASE_URL; // site main url
    public static final String API_URL = SITE_URL + ":9123/";
    // For Random stream
    public static final String RANDOM_CALL_SOCKET_URL = "wss://" + BASE_URL + ":9124/";
    /*For Text chat*/
    public static final String CHAT_SOCKET_URL = "wss://" + BASE_URL + ":9125/";
    /*Turn URL*/
    public static final String APPRTC_URL = "http://turn.jussfun.com:8080";
    /*Invite Friends*/
    public static final String APP_SHARE_URL = SITE_URL;


    public static final String POP_UP_WINDOW_PERMISSION = "POP UP WINDOW PERMISSION";
    public static final String POP_UP_WINDOW_PERMISSION_ASK_AGAIN = "POP UP DONT ASK AGAIN";
    public static final int OVERLAY_REQUEST_CODE_TECNO = 1;
    public static boolean isCallPopup;

    // for chat translate addon
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String TAG_DEFAULT_LANGUAGE_CODE = LANGUAGE_ENGLISH;

    public static final String TAG_STORY = "story";

    public static final String TAG_GIF = "gif";
    public static final String TAG_CHAT = "chat";

    public static final int LANGUAGE_REQUEST_CODE = 110;

    // for voice message addon
    public static final String TAG_AUDIO_SENT = "audio_sent";
    public static final String AUDIO_EXTENSION = ".mp3";
    public static final String TAG_AUDIO_RECEIVE = "audio_receive";
    public static boolean isExternalPlay = false;

    public static final String TAG_HISTORY = "history";
    public static final int MUTUAL_REQUEST_CODE = 500;
    public static final int HASHTAG_REQUEST_CODE = 501;
    public static final int PUBLISH_REQUEST_CODE = 502;
    public static final String TAG_SHARE = "share";
    public static final String TAG_TRENDING = "trending";
    public static final String TAG_HOME = "home";
    public static final String TAG_PUBLIC = "public";
    public static final String IS_VIDEO_DELETED = "isVideoDeleted";
    public static final String TAG_MUTUAL = "mutual";
    public static final String TAG_PUBLISH = "publish";
    public static final String TAG_INTENT_DATA = "intent_data";
    public static final String TAG_COUNT = "count";

    public static boolean isInVideoCall = false, isInStream = false, isInRandomCall = false;

    public static final String IMAGE_DIRECTORY = "";

    public static final String IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/accounts/";
    public static final String GIFT_IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/gifts/";
    public static final String PRIME_IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/slider/";
    public static final String GEMS_IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/gems/";
    public static final String CHAT_IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/chats/";

    public static final String TAG_AUTHORIZATION = "Authorization";
    public static final String TAG_LANGUAGE_CODE = "language_code";
    public static final String TAG_LANGUAGE = "language";
    public static String DEFAULT_LANGUAGE = "English";
    public static final String DEFAULT_LANGUAGE_CODE = "en";
    public static final String DEFAULT_SUBSCRIPTION = "$ 99";
    public static final String DEFAULT_VALIDITY = "1M";
    public static final String TAG_ID = "id";
    public static final String TAG_USER_ID = "user_id";
    public static final String TAG_USER_NAME = "user_name";
    public static final String TAG_USER_IMAGE = "user_image";
    public static final String TAG_PROFILE_IMAGE = "profile_image";
    public static final String TAG_CHAT_IMAGE = "chat_image";
    public static final String TAG_FEED_IMAGE = "feed_image";
    public static final String TAG_FEED_ID = "feed_id";
    public static final String TAG_FROM = "from";
    public static final String TAG_TRUE = "true";
    public static final String TAG_FALSE = "false";
    public static final String TAG_REJECTED = "rejected";
    public static final String TAG_ACCOUNT_BLOCKED = "accountblocked";
    public static final String TAG_MALE = "male";
    public static final String TAG_FEMALE = "female";
    public static final String TAG_BOTH = "both";
    public static final String TAG_STATUS = "status";
    public static final String TAG_MESSAGE = "message";
    public static final String TAG_PHONENUMBER = "phonenumber";
    public static final String TAG_FACEBOOK = "facebook";
    public static final String TAG_DEVICE_TYPE = "1";
    public static final String TAG_PLATFORM = "platform";
    public static final String TAG_ANDROID = "android";
    public static final String TAG_REPORT_IMAGE = "report_image";
    public static final String TAG_REPORT = "report";
    public static final String TAG_VIDEO_CHAT_IMAGE = "video_chat_image";
    public static final String TAG_REPORT_SUCCESS = "Reported successfully";
    public static final String TAG_FOLLOWER_ID = "follower_id";
    public static final String TAG_LIKE = "like";
    public static final String TAG_SUPER_LIKE = "superlike";
    public static final String TAG_STAR = "star";
    public static final String TAG_HEART = "heart";

    // JSON constant keys
    public static final String TAG_RESULT = "result";
    public static final String TAG_TOKEN = "token";
    public static final String TAG_FULL_NAME = "full_name";
    public static final String TAG_FIRST_LOGIN = "first_time_logged";
    public static final String TAG_FOLLOWERS_COUNT = "followers_count";
    public static final String TAG_FOLLOWING_COUNT = "following_count";
    public static final String TAG_BLOCKED_COUNT = "blocked_count";
    public static final String TAG_NOTIFICATION_COUNT = "notification_count";
    public static final String TAG_FOLLOWING = "following";
    public static final String TAG_PAGE_TYPE = "pagetype";
    public static final String TAG_CONTENT = "content";

    /*Socket Constants*/

    public static final String TAG_APP_NAME = "app_name";

    public static final String TAG_NAME = "name";
    public static final String TAG_AGE = "age";
    public static final String TAG_DOB = "dob";
    public static final String TAG_GENDER = "gender";
    public static final String TAG_PREMIUM_MEBER = "premium_member";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_REFERRAL_LINK = "referal_link";
    public static final String TAG_FOLLOW = "follow";
    public static final String TAG_UNFOLLOW = "unfollow";
    public static final String TAG_FOLLOW_USER = "Follow";
    public static final String TAG_UNFOLLOW_USER = "Unfollow";
    public static final String TAG_FOLLOWERS = "followers";
    public static final String TAG_FOLLOWINGS = "followings";
    public static final String TAG_INTEREST_ON_YOU = "interest_on_you";
    public static final String TAG_INTERESTED_BY_ME = "interested_by_me";
    public static final String TAG_INTEREST_USER_ID = "interest_user_id";
    public static final String TAG_INTERESTED = "interested";
    public static final String TAG_FRIENDS = "friends";
    public static final String TAG_INTEREST = "interest";
    public static final String TAG_MATCH = "match";
    public static final String TAG_PRIVACY_AGE = "privacy_age";
    public static final String TAG_PRIVACY_CONTACT_ME = "privacy_contactme";
    public static final String TAG_TYPE = "type";
    public static final String TAG_LIVE = "live";
    public static final String TAG_USER = "user";
    public static final String TAG_PARTNER_ID = "partner_id";
    public static final String TAG_RANDOM_KEY = "randomKey";
    public static final String TAG_LIVESTATUS = "_liveStatus";
    public static final String TAG_LIVE_STATUS = "live_status";
    public static final String TAG_FIND_PARTNER = "_findPartner";
    public static final String TAG_CONNECT_BACK = "_Connectback";
    public static final String TAG_SEARCH_BACK = "_Searchback";
    public static final String TAG_JOIN_ROOM = "_joinRoom";
    public static final String TAG_JOIN_BACK = "_Joinback";
    public static final String TAG_PARTNER_JOINED = "_partnerJoined";
    public static final String TAG_COMMITED = "_committed";
    public static final String TAG_BREAKUP_PARTNER = "_breakupPartner";
    public static final String TAG_LEAVE_PARTNER = "_leavePartner";
    public static final String TAG_LEAVE_ROOM = "_leaveRoom";
    public static final String TAG_SEND_STICKER = "_sendSticker";
    public static final String TAG_RECEIVE_STICKER = "_receiveSticker";
    public static final String TAG_SEND_GIFT = "_sendGift";
    public static final String TAG_CHECK_GIFT = "_checkGift";
    public static final String TAG_RECEIVE_GIFT = "_receiveGift";
    public static final String TAG_GIFT_STATUS = "_giftStatus";
    public static final String TAG_SEND_GIFT_SUCCEED = "_sendGiftSucceed";
    public static final String TAG_OUT_OF_GEMS = "_outofGems";
    public static final String TAG_SEND_GIFT_FAILED = "_sendGiftFailed";
    public static final String TAG_ON_CHAT = "_onChat";
    public static final String TAG_FILTER_SEARCH_RESULT = "filter_search_result";
    public static final String TAG_FILTER_UPDATED = "_filterUpdated";
    public static final String TAG_FILTER_CHARGED = "_filterCharged";
    public static final String TAG_USER_BLOCKED = "_Userblocked";
    public static final String TAG_RECEIVE_CHAT = "_receiveChat";
    public static final String TAG_SEND_CHAT = "_sendChat";
    public static final String TAG_CREATE_CALL = "_createCall";
    public static final String TAG_CALL_RECEIVED = "_callReceived";
    public static final String TAG_OFFLINE_CHATS = "_offlineChat";
    public static final String TAG_USER_TYPING = "_userTyping";
    public static final String TAG_lISTEN_TYPING = "_listenTyping";
    public static final String TAG_BLOCK_USER = "_blockUser";
    public static final String TAG_BLOCK_USER_ID = "block_user_id";
    public static final String TAG_BLOCK_STATUS = "block_status";
    public static final String _ONLINE_LIST = "_onlineList";
    public static final String TAG_ONLINE_LIST_STATUS = "_onlineListStatus";
    public static final String TAG_PROFILE_LIVE = "_profileLive";
    public static final String TAG_PROFILE_STATUS = "_profileStatus";
    public static final String TAG_NOTIFY_USER = "_userNotify";
    public static final String TAG_LISTEN_USER = "_listenNotify";
    public static final String TAG_CALL_REJECTED = "_callRejected";
    public static final String TAG_DISABLE_VIDEO = "_disableVideo";
    public static final String TAG_VIDEO_DISABLED = "_videoDisabled";

    public static final String TAG_ONLINE = "online";
    public static final String TAG_ONLINE_STATUS = "online_status";
    public static final String TAG_MIN_AGE = "min_age";
    public static final String TAG_MAX_AGE = "max_age";
    public static final String TAG_BLOCKED = "blocked";
    public static final String TAG_BLOCKED_BY_ME = "blocked_by_me";
    public static final String TAG_BLOCKED_ME = "blocked_me";
    public static final String TAG_TYPING_STATUS = "typing_status";
    public static final String TAG_TYPING = "typing";
    public static final String TAG_UNTYPING = "untyping";

    public static final String TAG_PARTNER_LIST = "partner_list";
    public static final Integer MAX_AGE = 99;
    public static final Integer MIN_AGE = 18;
    public static final String OFFLINE = "0";
    public static final String ONLINE = "1";
    public static final String INSEARCH = "2";
    public static final String INRANDOMCALL = "3";
    public static final String TAG_STICKER_ID = "sticker_id";
    public static final String TAG_GIFT_ID = "gift_id";
    public static final String TAG_NO_OF_GEM = "no_of_gem";
    public static final String TAG_TOTAL_GEMS = "total_gems";
    public static final String TAG_TOTAL_GIFTS = "total_gifts";
    public static final String TAG_AVAILABLE_GEMS = "available_gems";
    public static final String TAG_GEMS_COUNT = "gems_count";
    public static final String TAG_GIFT_ICON = "gift_icon";
    public static final String TAG_GIFT_TITLE = "gift_title";
    public static final String TAG_FILTER_LOCATION = "filter_location";
    public static final String TAG_FILTER_APPLIED = "filter_applied";
    public static final String TAG_LOCATION_SELECTED = "location_selected";
    public static final String TAG_FRIEND = "friend";
    public static final String TAG_DECLINED = "declined";
    public static final String TAG_FROM_FOREGROUND = "from_foreground";

    public static final String TAG_PROFILE_DATA = "profile_data";
    public static final String TAG_PROFILE = "profile";
    public static final int CAMERA_REQUEST_CODE = 111;
    public static final int STORAGE_REQUEST_CODE = 112;
    public static final int DOWNLOAD_REQUEST_CODE = 113;
    public static final int STREAM_REQUEST_CODE = 114;
    public static final int OVERLAY_REQUEST_CODE = 115;
    public static final int PRIME_REQUEST_CODE = 105;
    public static final int INTENT_REQUEST_CODE = 100;
    public static final int PROFILE_REQUEST_CODE = 200;
    public static final int LOCATION_REQUEST_CODE = 300;
    public static final int LOCATION_FILTER_REQUEST_CODE = 301;
    public static final int DEVICE_LOCK_REQUEST_CODE = 302;
    public static final int REQUEST_APP_UPDATE_IMMEDIATE = 400;
    public static final int REQUEST_APP_UPDATE_FLEXIBLE = 401;
    public static final int MAX_LENGTH = 30;
    public static final String EDIT = "edit";
    public static final String NOTIFICATION = "notification";
    public static final String TAG_GLOBAL = "global";
    public static final String TAG_COUNTRY = "country";
    public static final int MAX_LIMIT = 20;
    public static final String TAG_BROADCAST = "broadcast";
    public static final String TAG_SEARCH = "search";
    public static final String TAG_BOTTOM_HEIGHT = "bottom_nav_height";
    public static final int NOTIFICATION_ID = 2;
    public static final String TAG_RECENT = "recent";
    public static final String TAG_VISIBILITY = "visibility";
    public static String DEFAULT_SUBS_SKU = "become_prime";
    public static final String TYPE_STICKERS = "stickers";
    public static final String TYPE_GIFTS = "gifts";
    public static final String REPORT_TO_EMAIL = "";
    public static final String TAG_ACTIVITY = "activity";
    public static final String TAG_HELP = "help";
    public static final String TAG_YES = "yes";
    public static final String TAG_NO = "no";

    public static final String TAG_RECEIVER_ID = "receiver_id";
    public static final String TAG_CHAT_ID = "chat_id";
    public static final String TAG_MSG_ID = "msg_id";
    public static final String TAG_USER_CHAT = "user_chat";
    public static final String TAG_DATE = "date";
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_SEND = "send";
    public static final String TAG_SENDING = "sending";
    public static final String TAG_COMPLETED = "completed";
    public static final String TAG_RECEIVE = "receive";
    public static final String TAG_RECEIVED = "received";
    public static final String TAG_SENT = "sent";
    public static final String TAG_LAST_SENT = "last_sent";
    public static final String TAG_LAST_SEEN = "last_seen";
    public static final String TAG_CALL = "call";
    public static final String TAG_CALL_TYPE = "call_type";
    public static final String TAG_ROOM_ID = "room_id";
    public static final String TAG_CREATED_AT = "created_at";
    public static final String MESSAGE_CRYPT_KEY = "crypt@123";
    public static final String TAG_GEMS = "gems";
    public static final String TAG_CASH = "cash";
    public static final String TAG_OTHER_PROFILE = "other_profile";
    public static final String IMAGE_EXTENSION = ".jpg";
    public static final String TAG_SCOPE = "scope";
    public static final String TAG_SENDER_ID = "sender_id";
    public static final String TAG_UPDATE_LIVE = "_updateLive";
    public static final String TAG_RECEIVE_READ_STATUS = "_receiveReadStatus";
    public static final String TAG_OFFLINE_READ_STATUS = "_offlineReadStatus";
    public static final String TAG_UPDATE_READ = "_updateRead";
    public static final String TAG_GEMS_EARNINGS = "gems_earnings";

    public static String TAG_MSG_TYPE = "msg_type";
    public static String TAG_MESSAGE_END = "message_end";
    public static String TAG_CHAT_TYPE = "chat_type";
    public static String TAG_RECENT_TYPE = "recent_type";
    public static String TAG_DELIVERY_STATUS = "delivery_status";
    public static String TAG_UNREAD_COUNT = "unread_count";
    public static String TAG_READ = "read";
    public static String TAG_UNREAD = "unread";
    public static String TAG_READ_STATUS = "read_status";
    public static String TAG_CHAT_TIME = "chat_time";
    public static String TAG_RECEIVED_TIME = "received_time";
    public static String TAG_TIMESTAMP = "timestamp";
    public static final String TAG_ADMIN = "admin";
    public static final String TAG_THUMBNAIL = "thumbnail";
    public static final String TAG_PROGRESS = "progress";
    public static final String TAG_IMAGE = "image";
    public static final String TAG_TEXT = "text";
    public static final String TAG_GIFT = "gift";
    public static final String TAG_STICKER = "sticker";
    public static final String TAG_AUDIO = "audio";
    public static final String TAG_VIDEO = "video";
    public static final String TAG_PARTNER_NAME = "partner_name";
    public static final String TAG_PARTNER_IMAGE = "partner_image";
    public static final String TAG_DESCRIPTION = "description";
    public static final String TAG_CREATED = "created";
    public static final String TAG_ENDED = "ended";
    public static final String TAG_WAITING = "waiting";
    public static final String TAG_MISSED = "missed";
    public static final String CALL_TAG = "RandomCall";
    public static final String TAG_RECORDS = "records";
    public static String TAG_TEXT_CHAT = "txtchat";
    public static String TAG_VIDEO_CALL = "videocall";
    public static String TAG_USERS_LIST = "users_list";
    public static String TAG_SUBSCRIBE = "subscribe";
    public static final String TAG_QR_CODE = "qr_code";
    public static final String TAG_LIMIT = "limit";
    public static final String TAG_OFFSET = "offset";
    public static final String TAG_NEARBY = "nearby";
    public static final String TAG_IS_BLURRED = "is_blurred";
    public static final String TAG_UNLOCKS_LEFT = "unlocks_left";
    public static final String TAG_SEARCH_KEY = "search_key";
    public static final String TAG_POSITION = "position";
    public static final String TAG_TITLE = "title";

    /**
     * Milliseconds used for UI animations
     */
    public static final long ANIMATION_FAST_MILLIS = 50L;
    public static final long ANIMATION_SLOW_MILLIS = 100L;
    public static final long TYPING_DELAY = 300L;

    /**
     * ScreenLock Constants
     */
    public static final String SECRET_MESSAGE = "Very secret message";
    public static final String KEY_NAME_NOT_INVALIDATED = "key_not_invalidated";
    public static final String DEFAULT_KEY_NAME = "default_key";

    public static boolean isMute = true;
}
