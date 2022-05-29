package com.app.jussfun.ui.notifications;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.external.LinkEllipseTextView;


public class NotificationCommentHolder extends RecyclerView.ViewHolder {

    public TextView time;

    LinkEllipseTextView description;
    ImageView userImg, post_image;
    RecyclerView recyclerView;
    Context context;

    public NotificationCommentHolder(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        time = (TextView) itemView.findViewById(R.id.time);
        description = (LinkEllipseTextView) itemView.findViewById(R.id.description);
        userImg = (ImageView) itemView.findViewById(R.id.userImage);
        post_image = (ImageView) itemView.findViewById(R.id.post_image);
        recyclerView = (RecyclerView) itemView.findViewById(R.id.card_recycler_view);

    }
}


