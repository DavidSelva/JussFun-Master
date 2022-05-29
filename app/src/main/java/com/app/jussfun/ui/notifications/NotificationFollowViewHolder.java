package com.app.jussfun.ui.notifications;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hitasoft.app.pixapop.R;
import com.hitasoft.app.pixapop.helper.LinkEllipseTextView;

public class NotificationFollowViewHolder extends RecyclerView.ViewHolder {

    public TextView time;
    Button btnFollow;
    FrameLayout followLay;
    ProgressBar followLoad;
    LinkEllipseTextView description;
    ImageView userImg;
    Context context;

    public NotificationFollowViewHolder(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        time = (TextView) itemView.findViewById(R.id.time);
        description = (LinkEllipseTextView) itemView.findViewById(R.id.description);
        followLoad = itemView.findViewById(R.id.follow_load);
        followLay = itemView.findViewById(R.id.followLay);
        userImg = (ImageView) itemView.findViewById(R.id.userImage);
        btnFollow = itemView.findViewById(R.id.btnFollow);

    }

}


