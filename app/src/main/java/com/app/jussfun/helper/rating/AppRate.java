package com.app.jussfun.helper.rating;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.app.jussfun.R;

import java.util.Date;

import static com.app.jussfun.helper.rating.PreferenceHelper.getInstallDate;
import static com.app.jussfun.helper.rating.PreferenceHelper.getIsAgreeShowDialog;
import static com.app.jussfun.helper.rating.PreferenceHelper.getLaunchTimes;
import static com.app.jussfun.helper.rating.PreferenceHelper.getRemindInterval;
import static com.app.jussfun.helper.rating.PreferenceHelper.isFirstLaunch;
import static com.app.jussfun.helper.rating.PreferenceHelper.setInstallDate;

public final class AppRate {

    private static AppRate singleton;

    private final Context context;

    private final DialogOptions options = new DialogOptions();

    private int installDate = 10;

    private int launchTimes = 10;

    private int remindInterval = 1;

    private boolean isDebug = false;

    private AppRate(Context context) {
        this.context = context.getApplicationContext();
    }

    public static AppRate with(Context context) {
        if (singleton == null) {
            synchronized (AppRate.class) {
                if (singleton == null) {
                    singleton = new AppRate(context);
                }
            }
        }
        return singleton;
    }

    public static void showRateDialogIfMeetsConditions(Activity activity) {
        singleton.showRateDialog(activity);
    }

    private static boolean isOverDate(long targetDate, int threshold) {
        return new Date().getTime() - targetDate >= threshold * 24 * 60 * 60 * 1000;
    }

    public AppRate setLaunchTimes(int launchTimes) {
        this.launchTimes = launchTimes;
        return this;
    }

    public AppRate setInstallDays(int installDate) {
        this.installDate = installDate;
        return this;
    }

    public AppRate setRemindInterval(int remindInterval) {
        this.remindInterval = remindInterval;
        return this;
    }

    public AppRate setShowLaterButton(boolean isShowNeutralButton) {
        options.setShowNeutralButton(isShowNeutralButton);
        return this;
    }

    public AppRate setShowNeverButton(boolean isShowNeverButton) {
        options.setShowNegativeButton(isShowNeverButton);
        return this;
    }

    public AppRate setShowTitle(boolean isShowTitle) {
        options.setShowTitle(isShowTitle);
        return this;
    }

    public AppRate clearAgreeShowDialog() {
        PreferenceHelper.setAgreeShowDialog(context, true);
        return this;
    }

    public AppRate clearSettingsParam() {
        PreferenceHelper.setAgreeShowDialog(context, true);
        PreferenceHelper.clearSharedPreferences(context);
        return this;
    }

    public AppRate setAgreeShowDialog(boolean clear) {
        PreferenceHelper.setAgreeShowDialog(context, clear);
        return this;
    }

    public AppRate setView(View view) {
        options.setView(view);
        return this;
    }

    public AppRate setOnClickButtonListener(OnClickButtonListener listener) {
        options.setListener(listener);
        return this;
    }

    public AppRate setTitle(int resourceId) {
        options.setTitleResId(resourceId);
        return this;
    }

    public AppRate setTitle(String title) {
        options.setTitleText(title);
        return this;
    }

    public AppRate setMessage(int resourceId) {
        options.setMessageResId(resourceId);
        return this;
    }

    public AppRate setMessage(String message) {
        options.setMessageText(message);
        return this;
    }

    public AppRate setTextRateNow(int resourceId) {
        options.setTextPositiveResId(resourceId);
        return this;
    }

    public AppRate setTextRateNow(String positiveText) {
        options.setPositiveText(positiveText);
        return this;
    }

    public AppRate setTextLater(int resourceId) {
        options.setTextNeutralResId(resourceId);
        return this;
    }

    public AppRate setTextLater(String neutralText) {
        options.setNeutralText(neutralText);
        return this;
    }

    public AppRate setTextNever(int resourceId) {
        options.setTextNegativeResId(resourceId);
        return this;
    }

    public AppRate setTextNever(String negativeText) {
        options.setNegativeText(negativeText);
        return this;
    }

    public AppRate setCancelable(boolean cancelable) {
        options.setCancelable(cancelable);
        return this;
    }

    public AppRate setStoreType(StoreType appstore) {
        options.setStoreType(appstore);
        return this;
    }

    public void monitor() {
        if (isFirstLaunch(context)) {
            setInstallDate(context);
        }
        PreferenceHelper.setLaunchTimes(context, getLaunchTimes(context) + 1);
    }

    public void showRateDialog(Activity activity) {
        if (!activity.isFinishing()) {
            Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(null);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setContentView(R.layout.dialog_rating);
            dialog.setCanceledOnTouchOutside(true);
            final OnClickButtonListener listener = options.getListener();
            TextView txtTitle = dialog.findViewById(R.id.txtTitle);
            TextView txtMessage = dialog.findViewById(R.id.txtMessage);
            Button btnNotNow = dialog.findViewById(R.id.btnNotNow);
            RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);

            txtTitle.setText(String.format(activity.getString(R.string.rate_dialog_title),
                    activity.getResources().getString(R.string.app_name)));

            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                    /*final Intent intentToAppstore = options.getStoreType() == StoreType.GOOGLEPLAY ?
                            createIntentForGooglePlay(activity) : createIntentForAmazonAppstore(activity);
                    activity.startActivity(intentToAppstore);*/
                    Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    // To count with Play market backstack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        context.startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        context.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
                    }
                    if (listener != null) listener.onClickButton(0);
                    dialog.dismiss();
                }
            });

            btnNotNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            /*
            Typeface lightFont = null, regularFont = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                lightFont = activity.getResources().getFont(R.font.font_light);
                regularFont = activity.getResources().getFont(R.font.font_regular);
            } else {
                lightFont = ResourcesCompat.getFont(activity, R.font.font_light);
                regularFont = ResourcesCompat.getFont(activity, R.font.font_regular);
            }

            SpannableString s = new SpannableString(options.getTitleText(context));
            s.setSpan(regularFont, 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new RelativeSizeSpan(1f), 0, s.length(), 0);
            dialog.setTitle(s);

            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setTypeface(lightFont);
            textView.setTextSize(AppUtils.dpToPx(context, 6));

            Button btn1 = dialog.findViewById(android.R.id.button1);
            btn1.setTypeface(regularFont);
            btn1.setAllCaps(false);
            btn1.setTextSize(AppUtils.dpToPx(context, 6));

            Button btn2 = dialog.findViewById(android.R.id.button2);
            btn2.setTypeface(regularFont);
            btn2.setAllCaps(false);
            btn2.setTextSize(AppUtils.dpToPx(context, 6));*/
        }
    }

    public boolean shouldShowRateDialog() {
        return getIsAgreeShowDialog(context) &&
                isOverLaunchTimes() &&
                isOverInstallDate() &&
                isOverRemindDate();
    }

    private boolean isOverLaunchTimes() {
        return getLaunchTimes(context) >= launchTimes;
    }

    private boolean isOverInstallDate() {
        return isOverDate(getInstallDate(context), installDate);
    }

    private boolean isOverRemindDate() {
        return isOverDate(getRemindInterval(context), remindInterval);
    }

    public boolean isDebug() {
        return isDebug;
    }

    public AppRate setDebug(boolean isDebug) {
        this.isDebug = isDebug;
        return this;
    }

}