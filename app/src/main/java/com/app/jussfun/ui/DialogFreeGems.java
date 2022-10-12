package com.app.jussfun.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.jussfun.R;
import com.app.jussfun.databinding.DialogFreeGemsBinding;
import com.app.jussfun.helper.callback.OnOkClickListener;
import com.app.jussfun.utils.AdminData;

public class DialogFreeGems extends DialogFragment {

    private Context context;
    private OnOkClickListener callBack;
    private DialogFreeGemsBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
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
        binding = DialogFreeGemsBinding.inflate(inflater, container, false);
        View itemView = binding.getRoot();
        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String firstString = String.format(context.getString(R.string.congratulations_you_got), "" + AdminData.freeGems);
        StringBuilder builder = new StringBuilder();
        builder.append(firstString);
        builder.append("\n");
        builder.append("1. Post a photo or Video");
        builder.append("\n");
        builder.append("2. Watch Video Ads");
        builder.append("\n");
        builder.append("3. Invite Friends");
//        SpannableStringBuilder str = new SpannableStringBuilder(firstString + noOfGems + lastString);
//        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), firstString.length(), firstString.length() + noOfGems.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtDescription.setText(builder);
        binding.btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callBack != null) {
                    callBack.onOkClicked("");
                }
            }
        });
    }

    public void setContext(Context context) {
        if (this.context == null) {
            this.context = context;
        }
    }

    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
