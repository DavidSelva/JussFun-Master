package com.app.jussfun.utils;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.FINGERPRINT_SERVICE;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.app.jussfun.BuildConfig;
import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.external.CryptLib;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.Feeds;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.Report;
import com.app.jussfun.ui.HomeFragment;
import com.app.jussfun.ui.MainActivity;
import com.app.jussfun.ui.feed.FeedsActivity;
import com.app.jussfun.ui.feed.MediaListViewHolder;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.crypto.NoSuchPaddingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppUtils {

    private static final String TAG = AppUtils.class.getSimpleName();
    private static Snackbar snackbar;
    public static final String SMILEY_PATH = "images/lottie/smileys/";
    public static List<String> callerList = new ArrayList<>();
    public static List<String> filterLocation = new ArrayList<>();
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    public static final String UTC_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static boolean isExternalPlay = false;
    private static String currentStatus = Constants.ONLINE;
    int displayHeight, displayWidth;

    public static List<String> smileyList = new ArrayList<String>() {{
        add("smile");
        add("angry");
        add("cry");
        add("lol");
        add("tired");
        add("wow");
        /*add("smile");
        add("angry");
        add("cry");
        add("lol");
        add("tired");
        add("wow");
        add("smile");
        add("angry");
        add("cry");
        add("lol");
        add("tired");
        add("wow");
        add("smile");
        add("angry");
        add("cry");
        add("lol");
        add("tired");
        add("wow");
        add("smile");
        add("angry");
        add("cry");
        add("lol");
        add("tired");
        add("wow");*/
    }};
    private final Context context;

    public AppUtils(Context context) {
        this.context = context;
    }

    public static int calculateAge(Date date) {
        Calendar birth = Calendar.getInstance();
        birth.setTime(date);
        Calendar today = Calendar.getInstance();

        int yearDifference = today.get(Calendar.YEAR)
                - birth.get(Calendar.YEAR);

        if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH)) {
            yearDifference--;
        } else {
            if (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH)
                    && today.get(Calendar.DAY_OF_MONTH) < birth
                    .get(Calendar.DAY_OF_MONTH)) {
                yearDifference--;
            }

        }

        return yearDifference;
    }

    /**
     * function for avoiding emoji typing in keyboard
     **/
    public static InputFilter EMOJI_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {

                int type = Character.getType(source.charAt(index));

                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL || Character.isSpaceChar(source.charAt(index))) {
                    return "";
                }
            }
            return null;
        }
    };

    /**
     * function for avoiding special characters typing in keyboard
     **/
    public static InputFilter SPECIAL_CHARACTERS_FILTER = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    /**
     * To convert the given dp value to pixel
     **/
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int getDisplayWidth(AppCompatActivity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        return width;
    }

    public static int getDisplayHeight(AppCompatActivity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        return height;
    }

    public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
        int buffSize = 1024;
        byte[] buff = new byte[buffSize];
        int len = 0;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }
        return byteBuff.toByteArray();
    }

    /**
     * Returns the consumer friendly device name
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        Logging.i(TAG, "getDeviceName: " + manufacturer + " " + model);
        return manufacturer + " " + model;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void hideStatusBar(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = activity.getActionBar();
        actionBar.hide();
    }

    public static int getNavigationBarHeight(Context context) {
        if (context != null) {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }

    /**
     * Returns {@code null} if this couldn't be determined.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @SuppressLint("PrivateApi")
    public static Boolean hasNavigationBar() {
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            IBinder serviceBinder = (IBinder) serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
            Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
            Object windowManagerService = stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
            Method hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar");
            return (boolean) hasNavigationBar.invoke(windowManagerService);
        } catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Log.w("YOUR_TAG_HERE", "Couldn't determine whether the device has a navigation bar", e);
            return false;
        }
    }

    public static String getPrimeTitle(Context context) {
        if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            return context.getString(R.string.premium_member);
        } else {
            return context.getString(R.string.get_prime_benefits);
        }
    }

    public static String getPrimeContent(Context context) {
        if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            return context.getString(R.string.valid_until) + " " + getPremiumDateFromUTC(context, GetSet.getPremiumExpiry());
        } else {
            String price = SharedPref.getString(SharedPref.IN_APP_PRICE, Constants.DEFAULT_SUBSCRIPTION);
            String validity = SharedPref.getString(SharedPref.IN_APP_VALIDITY, Constants.DEFAULT_VALIDITY);
            switch (validity) {
                case "P1W":
                    validity = context.getString(R.string.one_week_subs);
                    break;
                case "P1M":
                    validity = context.getString(R.string.one_month_subs);
                    break;
                case "P3M":
                    validity = context.getString(R.string.three_month_subs);
                    break;
                case "P6M":
                    validity = context.getString(R.string.six_month_subs);
                    break;
                case "P1Y":
                    validity = context.getString(R.string.one_year_subs);
                    break;
            }
            return context.getString(R.string.subscription_price, price, validity);
        }
    }

    public static String getPackageTitle(String validity, Context context) {
        switch (validity) {
            case "P1W":
                validity = context.getString(R.string.one_week);
                break;
            case "P1M":
                validity = context.getString(R.string.one_month);
                break;
            case "P3M":
                validity = context.getString(R.string.three_months);
                break;
            case "P6M":
                validity = context.getString(R.string.six_months);
                break;
            case "P1Y":
                validity = context.getString(R.string.one_year);
                break;
        }
        return validity;
    }

    public static String getGemsCount(Context context, Long gems) {
        if (gems != null && gems > 999) {
            return (gems / 1000) + context.getString(R.string.k);
        } else {
            return "" + gems;
        }
     /*   if (gems < 1000) return "" + gems;
        int exp = (int) (Math.log(gems) / Math.log(1000));
        return String.format("%.1f %c",
                gems / Math.pow(1000, exp),
                "kMGTPE".charAt(exp-1));*/
    }


    private static String getPremiumDateFromUTC(Context context, String premiumExpiry) {
        DateFormat utcFormat = new SimpleDateFormat(UTC_DATE_PATTERN, LocaleManager.getLocale(context.getResources()));
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", LocaleManager.getLocale(context.getResources()));
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date premiumDate = new Date();
        try {
            premiumDate = utcFormat.parse(premiumExpiry);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormat.format(premiumDate);
    }

    public static String getCurrentUTCTime(Context context) {
        DateFormat utcFormat = new SimpleDateFormat(UTC_DATE_PATTERN, Locale.ENGLISH);
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        return utcFormat.format(myDate);
    }

    public static String getUTCFromTimeStamp(Context context, long timeStamp) {
        DateFormat utcFormat = new SimpleDateFormat(UTC_DATE_PATTERN, LocaleManager.getLocale(context.getResources()));
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        myDate.setTime(timeStamp);
        return utcFormat.format(myDate);
    }

    public static Long getTimeFromUTC(Context context, String date) {
        DateFormat utcFormat = new SimpleDateFormat(UTC_DATE_PATTERN, Locale.ENGLISH);
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        try {
            myDate = utcFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        Log.i(TAG, "getTimeFromUTC: " + myDate.getTime());
        return myDate.getTime();
    }

    public static String getDateFromUTC(Context context, String date) {
        DateFormat utcFormat = new SimpleDateFormat(UTC_DATE_PATTERN, LocaleManager.getLocale(context.getResources()));
        DateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd", LocaleManager.getLocale(context.getResources()));
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        String newDate = "";
        try {
            myDate = utcFormat.parse(date);
            newDate = newFormat.format(myDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        Log.i(TAG, "getDateFromUTC: " + newDate);
        return newDate;
    }

    public static String getRecentDate(Context context, long smsTimeInMillis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeZone(TimeZone.getDefault());
        smsTime.setTimeInMillis(smsTimeInMillis);

        Calendar now = Calendar.getInstance();
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm", Locale.ENGLISH);
            SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
            String recent = timeFormat.format(new Date(smsTime.getTimeInMillis())) + " " + hourFormat.format(new Date(smsTime.getTimeInMillis()));
            return context.getString(R.string.today) + ", " + recent;
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
            SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
            Date date = new Date(smsTime.getTimeInMillis());
            String recent = timeFormat.format(date) + " " + hourFormat.format(date);
            return context.getString(R.string.yesterday) + ", " + recent;
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", LocaleManager.getLocale(context.getResources()));
            SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
            SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
            Date date = new Date(smsTime.getTimeInMillis());
            String recent = monthFormat.format(date) + " " + dayFormat.format(date) + ", " + timeFormat.format(date) + " " + hourFormat.format(date);
            return recent;
        } else {
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", LocaleManager.getLocale(context.getResources()));
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd yyyy", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
            SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
            Date date = new Date(smsTime.getTimeInMillis());
            String recent = monthFormat.format(date) + " " + dayFormat.format(date) + ", " + timeFormat.format(date) +
                    " " + hourFormat.format(date);
            return recent;
        }
    }

    public static String getRecentChatTime(Context context, String utcTime) {
        Date inputDate = new Date();
        if (utcTime != null && !utcTime.isEmpty()) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UTC_DATE_PATTERN);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                inputDate = simpleDateFormat.parse(utcTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeZone(TimeZone.getDefault());
        smsTime.setTime(inputDate);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "MMMM d, hh:mm aa";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return "" + android.text.format.DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return context.getString(R.string.yesterday);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return android.text.format.DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return android.text.format.DateFormat.format("MMMM/dd/yyyy", smsTime).toString();
        }
    }

    public static String getChatTime(Context context, String date) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeZone(TimeZone.getDefault());
        smsTime.setTimeInMillis(getTimeFromUTC(context, date));
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm", Locale.ENGLISH);
        SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
        String time = timeFormat.format(new Date(smsTime.getTimeInMillis())) + " " + hourFormat.format(new Date(smsTime.getTimeInMillis()));
        return time;
    }

    public static String getChatDate(Context context, String date) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeZone(TimeZone.getDefault());
        smsTime.setTimeInMillis(getTimeFromUTC(context, date));
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", LocaleManager.getLocale(context.getResources()));
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd yyyy", Locale.ENGLISH);
        return monthFormat.format(new Date(smsTime.getTimeInMillis())) + " " + dayFormat.format(new Date(smsTime.getTimeInMillis()));
    }

    public static String getTimeAgo(Context context, String utcTime) {

        Date inputDate = new Date();
        if (utcTime != null && !utcTime.isEmpty()) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UTC_DATE_PATTERN);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                inputDate = simpleDateFormat.parse(utcTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Calendar smsTime = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        smsTime.setTimeZone(TimeZone.getDefault());
        smsTime.setTime(inputDate);

        long now = System.currentTimeMillis();
        if (calendar.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("a", LocaleManager.getLocale(context.getResources()));
            String timesAgo = dateFormat.format(smsTime.getTimeInMillis()) + " " + timeFormat.format(smsTime.getTimeInMillis());
            return timesAgo;
        } else if (calendar.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return context.getString(R.string.yesterday);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            return dateFormat.format(smsTime.getTimeInMillis());
        }
    }

    public static String formatWord(String word) {
        if (word != null && !word.isEmpty()) {
            word = String.valueOf(word.charAt(0)).toUpperCase() + word.subSequence(1, word.length());
            return word;
        }
        return "";
    }

    public static void setCurrentStatus(String currentStatus) {
        AppUtils.currentStatus = currentStatus;
    }

    public static String getCurrentStatus() {
        return currentStatus;
    }

    public static String stripHtml(String htmlString) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return "" + Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return "" + Html.fromHtml(htmlString);
        }
    }

    public void openReportDialog(String feedId, AppCompatActivity activity) {
        BottomSheetDialog reportDialog;
        View sheetView = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_report, null);
        reportDialog = new BottomSheetDialog(activity, R.style.Bottom_FilterDialog); // Style here
        reportDialog.setContentView(sheetView);
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) sheetView.getParent());
//        int maxHeight = (getDisplayHeight(activity) * 80) / 100;
//        mBehavior.setPeekHeight(maxHeight);
        sheetView.requestLayout();
        RecyclerView reportsView = sheetView.findViewById(R.id.reportView);
        reportDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });
        ReportAdapter reportAdapter = new ReportAdapter(activity, AdminData.reportList, feedId, reportDialog);
        reportsView.setLayoutManager(new LinearLayoutManager(activity));
        reportsView.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        reportDialog.show();
    }

    public class ReportAdapter extends RecyclerView.Adapter {
        private final AppCompatActivity activity;
        private List<Report> reportList = new ArrayList<>();
        private RecyclerView.ViewHolder viewHolder;
        BottomSheetDialog reportDialog;
        String feedId;

        public ReportAdapter(AppCompatActivity activity, List<Report> reportList, String feedId, BottomSheetDialog reportDialog) {
            this.activity = activity;
            this.reportList = reportList;
            this.reportDialog = reportDialog;
            this.feedId = feedId;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_report, parent, false);
            viewHolder = new ReportAdapter.MyViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ReportAdapter.MyViewHolder) holder).txtReport.setText(reportList.get(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return reportList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txtReport)
            TextView txtReport;
            @BindView(R.id.itemReportLay)
            LinearLayout itemReportLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            @OnClick(R.id.itemReportLay)
            public void onViewClicked() {
                App.preventMultipleClick(itemReportLay);
                if (activity instanceof FeedsActivity) {
                    FeedsActivity feedsActivity = (FeedsActivity) activity;
                    reportDialog.dismiss();
                    feedsActivity.onReportSend(feedId, reportList.get(getAdapterPosition()).getTitle());
                } else {
                    MainActivity feedsActivity = (MainActivity) activity;
                    reportDialog.dismiss();
                    feedsActivity.onReportSend(feedId, reportList.get(getAdapterPosition()).getTitle());
                }
            }
        }
    }

    public static void followUnfollow(Context mContext, MediaListViewHolder mediaListViewHolder, Feeds resultsItem, String followStatus) {
        if (NetworkReceiver.isConnected()) {
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_INTEREST_USER_ID, resultsItem.getUserId());
            requestMap.put(Constants.TAG_INTERESTED, "" + (followStatus.equals(mContext.getString(R.string.follow)) ? 1 : 0));
            Call<Map<String, String>> call = apiInterface.interestOnUser(requestMap);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            resultsItem.setFriend(Boolean.parseBoolean(responseMap.get(Constants.TAG_FRIEND)));
                            if (followStatus.equals(mContext.getString(R.string.follow))) {
                                mediaListViewHolder.txtFollow.setText(mContext.getString(R.string.unfollow));
                                resultsItem.setInterestedByMe(true);
                            } else {
                                resultsItem.setInterestedByMe(false);
                                mediaListViewHolder.txtFollow.setText(mContext.getString(R.string.follow));
                            }
                        }

                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    public void showKeyboard(EditText editText, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.showSoftInput(editText, 0);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void resetFilter() {
        HomeFragment.filterApplied = false;
        GetSet.setFilterApplied(false);
        GetSet.setLocationApplied(false);
    }

    // Showing network status in Snackbar
    public static void showSnack(final Context context, View view, boolean isConnected) {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
        snackbar = Snackbar
                .make(view, context.getString(R.string.no_internet_connection), Snackbar.LENGTH_INDEFINITE)
                .setAction(context.getString(R.string.settings).toUpperCase(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        context.startActivity(intent);
                    }
                })
                .setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);

        if (isConnected) {
            snackbar.dismiss();
            snackbar = null;
        } else {
            if (!snackbar.isShown()) {
                snackbar.show();
            }
        }
    }

    public static void dismissSnack() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
            snackbar = null;
        }
    }

    public Task<ShortDynamicLink> getReferralDynamicLink() {

        String url = Constants.SITE_URL + "/" + GetSet.getReferalLink();
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(url))
                .setDomainUriPrefix("https://" + context.getString(R.string.dynamic_link_filter) + "/")
                .setIosParameters(new DynamicLink.IosParameters.Builder(context.getString(R.string.ios_bundle_id))
                        .setAppStoreId(context.getString(R.string.ios_app_store_id))
                        .setCustomScheme(context.getString(R.string.ios_bundle_id))
                        .build())
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(BuildConfig.APPLICATION_ID)
                        .build())
                .buildShortDynamicLink();

        return shortLinkTask;
    }

    public Task<ShortDynamicLink> getFeedDynamicLink(String feedId) {

        String url = Constants.SITE_URL + "?feed_id=" + feedId;
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(url))
                .setDomainUriPrefix("https://" + context.getString(R.string.dynamic_link_filter) + "/")
                .setIosParameters(new DynamicLink.IosParameters.Builder(context.getString(R.string.ios_bundle_id))
                        .setAppStoreId(context.getString(R.string.ios_app_store_id))
                        .setCustomScheme(context.getString(R.string.ios_bundle_id))
                        .build())
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(BuildConfig.APPLICATION_ID)
                        .build())
                .buildShortDynamicLink();

        return shortLinkTask;
    }

    public static String encryptMessage(String message) {
        String encryptedMsg = "";
        try {
            CryptLib cryptLib = new CryptLib();
            encryptedMsg = cryptLib.encryptPlainTextWithRandomIV(message, Constants.MESSAGE_CRYPT_KEY);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedMsg;
    }

    public static String decryptMessage(String message) {
        try {
            CryptLib cryptLib = new CryptLib();
            message = cryptLib.decryptCipherTextWithRandomIV(message, Constants.MESSAGE_CRYPT_KEY);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    public static String twoDigitString(long number) {
        if (number == 0) {
            return "00";
        } else if (number / 10 == 0) {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    public static void startAlphaAnimation(View v, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(ALPHA_ANIMATIONS_DURATION);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    public void setPrimeBanner(LottieAnimationView primeLottie,
                               LottieAnimationView renewalLottie, RelativeLayout primeBg, RelativeLayout renewalBg) {
        primeLottie.setVisibility(View.VISIBLE);
        renewalLottie.setVisibility(View.VISIBLE);
        renewalLottie.setVisibility(View.VISIBLE);
        primeBg.setBackground(null);
        renewalBg.setBackground(null);
    }

    public Drawable getBanner() {
        return context.getDrawable(R.drawable.side_nav_bar);
    }

    public Drawable getChatBG() {
        return context.getDrawable(R.drawable.side_nav_bar);
    }

    public String getFormattedDuration(long currentMilliSeconds) {
        String duration;
        if (TimeUnit.MILLISECONDS.toHours(currentMilliSeconds) != 0)
            duration = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(currentMilliSeconds),
                    TimeUnit.MILLISECONDS.toMinutes(currentMilliSeconds) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(currentMilliSeconds) % TimeUnit.MINUTES.toSeconds(1));
        else
            duration = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(currentMilliSeconds) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(currentMilliSeconds) % TimeUnit.MINUTES.toSeconds(1));
        return duration;
    }

    public void pauseExternalAudio() {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            isExternalPlay = true;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            context.sendBroadcast(i);
        }
    }

    public void resumeExternalAudio() {
        if (isExternalPlay) {
            isExternalPlay = false;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "play");
            context.sendBroadcast(i);
        }
    }

    public static void resumeExternalAudioChat(Context context) {
        if (Constants.isExternalPlay) {
            Constants.isExternalPlay = false;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "play");
            context.sendBroadcast(i);
        }
    }

    public static void pauseExternalAudioChat(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            Constants.isExternalPlay = true;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            context.sendBroadcast(i);
        }
    }

    public boolean checkIsDeviceEnabled(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (km.isKeyguardSecure() && BiometricManager.from(context).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                return true;
            } else return km.isKeyguardSecure();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if we're running on Android 6.0 (M) or higher
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);
            if (fingerprintManager != null) {
                if (fingerprintManager.isHardwareDetected()) {
                    if (fingerprintManager.hasEnrolledFingerprints()) {
                        return true;
                    } else
                        return km.isKeyguardSecure();
                } else {
                    return km.isKeyguardSecure();
                }
            } else return km.isKeyguardSecure();
        } else return km.isKeyguardSecure();
    }

    private Animation getLikeZoomInAnim() {
        return AnimationUtils.loadAnimation(context, R.anim.anim_zoom_in);
    }

    private Animation getLikeZoomOutAnim() {
        return AnimationUtils.loadAnimation(context, R.anim.anim_zoom_out);
    }

    public void onInterestClicked(View view) {
        view.startAnimation(getLikeZoomInAnim());
        view.startAnimation(getLikeZoomOutAnim());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.clearAnimation();
            }
        }, 200);
    }

    public void onUnInterestClicked(View view) {
        view.startAnimation(getLikeZoomInAnim());
        view.startAnimation(getLikeZoomOutAnim());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.clearAnimation();
            }
        }, 200);
    }

    public static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    public String isAppRunning() {
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
// The first in the list of RunningTasks is always the foreground task.
        ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
        String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
        PackageManager pm = context.getPackageManager();
        PackageInfo foregroundAppPackageInfo = null;
        try {
            foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
            String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();
            return foregroundTaskAppName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formateMilliSeccond(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        //      return  String.format("%02d Min, %02d Sec",
        //                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
        //                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
        //                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));

        // return timer string
        return finalTimerString;
    }

    public String getCommentsDuration(long currentMilliSeconds) {
        String duration;
        duration = String.format(Locale.ENGLISH, "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(currentMilliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(currentMilliSeconds) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(currentMilliSeconds) % TimeUnit.MINUTES.toSeconds(1));
        return duration;
    }


    public Task<ShortDynamicLink> getStreamShareLink(String shareLink) {

        String url = Constants.APP_SHARE_URL + "/" + shareLink;
        Log.i(TAG, "getStreamShareLink: " + url);
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(url))
                .setDomainUriPrefix("https://" + context.getString(R.string.dynamic_link_filter) + "/")
                .setIosParameters(new DynamicLink.IosParameters.Builder(context.getString(R.string.ios_bundle_id))
                        .setAppStoreId(context.getString(R.string.ios_app_store_id))
                        .setCustomScheme(context.getString(R.string.ios_bundle_id))
                        .build())
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(BuildConfig.APPLICATION_ID)
                        .build())
                .buildShortDynamicLink();

        return shortLinkTask;
    }
}
