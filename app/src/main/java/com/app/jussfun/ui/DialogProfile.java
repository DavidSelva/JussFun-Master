package com.app.jussfun.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;

import com.airbnb.lottie.LottieAnimationView;
import com.app.jussfun.R;
import com.app.jussfun.external.KeyboardUtils;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.OnOkClickListener;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.SignInRequest;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.SharedPref;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogProfile extends DialogFragment {

    private static final String TAG = DialogProfile.class.getSimpleName();
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtDescription)
    TextView txtDescription;
    @BindView(R.id.headerLay)
    RelativeLayout headerLay;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.edtName)
    EditText edtName;
    @BindView(R.id.edtUserName)
    EditText edtUserName;
    @BindView(R.id.txtGender)
    TextView txtGender;
    @BindView(R.id.btnMale)
    RadioButton btnMale;
    @BindView(R.id.btnFemale)
    RadioButton btnFemale;
    @BindView(R.id.txtDob)
    TextView txtDob;
    @BindView(R.id.edtDob)
    EditText edtDob;
    @BindView(R.id.profileLayout)
    LinearLayout profileLayout;
    @BindView(R.id.btnNext)
    Button btnNext;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.edtLocation)
    AppCompatTextView edtLocation;
    @BindView(R.id.btnLocation)
    ImageView btnLocation;
    private OnOkClickListener callBack;
    private Context context;
    private DatePickerDialog mDatePickerDialog;
    private String strAge, strDob, strLocation = null;
    private boolean isKeyboardOpen = false;
    private DisplayMetrics displayMetrics;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorTransparent)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.dialog_profile, container, false);
        ButterKnife.bind(this, itemView);
        displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        edtName.setFilters(new InputFilter[]{AppUtils.SPECIAL_CHARACTERS_FILTER, new InputFilter.LengthFilter(Constants.MAX_LENGTH)});
        edtUserName.setFilters(new InputFilter[]{AppUtils.SPECIAL_CHARACTERS_FILTER, AppUtils.EMOJI_FILTER, new InputFilter.LengthFilter(Constants.MAX_LENGTH)});
        if (SharedPref.getString(SharedPref.FACEBOOK_NAME, null) != null) {
            edtName.setText(SharedPref.getString(SharedPref.FACEBOOK_NAME, null));
        }
        progressBar.setIndeterminate(true);
        if (LocaleManager.isRTL()) {
            btnLocation.setScaleX(-1);
        } else {
            btnLocation.setScaleX(1);
        }
        KeyboardUtils.addKeyboardToggleListener(getActivity(), new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                isKeyboardOpen = isVisible;
            }
        });
        return itemView;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }


    public void setContext(Context context) {
        this.context = context;
    }

    @OnClick({R.id.edtDob, R.id.btnNext, R.id.btnMale, R.id.btnFemale, R.id.edtLocation, R.id.btnLocation})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.edtDob:
                openDateDialog();
                break;
            case R.id.btnNext:
                if (TextUtils.isEmpty(edtName.getText().toString().trim())) {
                    edtName.getText().clear();
                    edtName.setError(context.getString(R.string.enter_name));
                } else if (TextUtils.isEmpty(edtUserName.getText().toString())) {
                    edtName.setError(null);
                    edtUserName.setError(context.getString(R.string.enter_user_name));
                } else if (TextUtils.isEmpty(edtDob.getText().toString())) {
                    edtUserName.setError(null);
                    edtDob.setError(context.getString(R.string.enter_date_of_birth));
                } else if (strLocation == null || TextUtils.isEmpty(strLocation)) {
                    edtDob.setError(null);
                    edtLocation.setError(context.getString(R.string.select_location));
                } else {
                    edtName.setError(null);
                    edtUserName.setError(null);
                    SignInRequest request = new SignInRequest();
                    request.setLoginId(GetSet.getLoginId());
                    request.setType(GetSet.getLoginType());
                    request.setMailId(SharedPref.getString(SharedPref.FACEBOOK_EMAIL, ""));
                    request.setName(edtName.getText().toString());
                    request.setUserName(edtUserName.getText().toString());
                    request.setAge(strAge);
                    request.setDob(strDob);
                    request.setGender(btnMale.isChecked() ? Constants.TAG_MALE : Constants.TAG_FEMALE);
                    request.setLocation(strLocation);
                    callBack.onOkClicked(request);
                }
                break;
            case R.id.btnMale:
                if (isKeyboardOpen) {
//                    App.makeCustomToast(getString(R.string.male));
                } else {
//                    App.makeToast(getString(R.string.male));
                }
                break;
            case R.id.btnFemale:
                if (isKeyboardOpen) {
//                    App.makeCustomToast(getString(R.string.female));
                } else {
//                    App.makeToast(getString(R.string.female));
                }
                break;
            case R.id.btnLocation:
            case R.id.edtLocation:
                Intent location = new Intent(context, LocationFilterActivity.class);
                location.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                location.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                location.putExtra(Constants.TAG_LOCATION, strLocation != null ? strLocation : "");
                startActivityForResult(location, Constants.LOCATION_REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                strLocation = data.getStringExtra(Constants.TAG_LOCATION);
                edtLocation.setText(strLocation);
                edtLocation.setError(null);
            } else {
                strLocation = null;
                edtLocation.setText("");
            }
        }
    }

    private void openDateDialog() {
        Calendar calendar = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR) - Constants.MIN_AGE, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 0);
        long maxDate = cal.getTimeInMillis();
        calendar.setTimeInMillis(maxDate);
        mDatePickerDialog = new DatePickerDialog(context, R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, monthOfYear, dayOfMonth);
                final Date newDate = newCalendar.getTime();

                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                strAge = "" + AppUtils.calculateAge(newDate);
                strDob = displayFormat.format(newDate);
                edtDob.setText(strDob);
                edtDob.setError(null);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.getDatePicker().setMaxDate(maxDate);
        mDatePickerDialog.getDatePicker().getTouchables().get(0).performClick();
        mDatePickerDialog.show();
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        if (getActivity() != null)
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        btnNext.setEnabled(false);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnNext.setEnabled(true);
        if (getActivity() != null)
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
