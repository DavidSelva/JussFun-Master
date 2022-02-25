package com.app.jussfun.ui.feed;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.base.BaseViewHolder;
import com.app.jussfun.external.LinkEllipseTextView;
import com.app.jussfun.model.CommentsModel;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;
import com.daimajia.swipe.SwipeLayout;

public class ReplyViewHolder extends BaseViewHolder {

    TextView username;
    TextView cmt_time, replyClick, like_cnt, loading;
    LinkEllipseTextView commentTxt;
    ImageView userImg, likeClick;
    LinearLayout likeLay, leftSwipe, rightSwipe;
    public RecyclerView recyclerView;
    SwipeLayout swipeLayout;
    Context context;

    public ReplyViewHolder(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
        username = (TextView) itemView.findViewById(R.id.username);
        userImg = (ImageView) itemView.findViewById(R.id.userImage);
        commentTxt = (LinkEllipseTextView) itemView.findViewById(R.id.commentTxt);
        cmt_time = (TextView) itemView.findViewById(R.id.cmt_time);
        replyClick = (TextView) itemView.findViewById(R.id.reply);
        likeClick = itemView.findViewById(R.id.likeClick);
        like_cnt = (TextView) itemView.findViewById(R.id.like_cnt);
        loading = (TextView) itemView.findViewById(R.id.loading);
        likeLay = (LinearLayout) itemView.findViewById(R.id.likeLay);
//        leftSwipe = (LinearLayout) itemView.findViewById(R.id.leftSwipe);
        rightSwipe = (LinearLayout) itemView.findViewById(R.id.rightSwipe);
        recyclerView = (RecyclerView) itemView.findViewById(R.id.recyclerview);
    }

    public interface clickListener {
        public void clickListen(String fragment, String way, String userid);
    }

    public clickListener listener;

    public void setListener(clickListener listener) {
        this.listener = listener;
    }


    @Override
    public void bind(int position, Object object, String way) {

        CommentsModel.Reply replyPojo = (CommentsModel.Reply) object;

        commentTxt.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);

        commentTxt.setIsLinkable(true);

        username.setText(replyPojo.getUser().getUserName());
        commentTxt.setText(AppUtils.stripHtml(replyPojo.getComments()).trim());
        cmt_time.setText(AppUtils.getTimeAgo(context, replyPojo.getReplyCreatedAt()));

        if (replyPojo.getLiked() == 1)
            likeClick.setSelected(true);
        else
            likeClick.setSelected(false);


        if (replyPojo.getReplyLikeCount() == 0)
            like_cnt.setVisibility(View.GONE);
        else {

            like_cnt.setVisibility(View.VISIBLE);
            if (replyPojo.getReplyLikeCount() == 1)
                like_cnt.setText(replyPojo.getReplyLikeCount() + " " + context.getString(R.string.like));
            else
                like_cnt.setText("" + replyPojo.getReplyLikeCount() + " " + context.getString(R.string.likes));
        }

        Glide.with(context)
                .load(Constants.IMAGE_URL + replyPojo.getUser().getUserImage())
                .centerCrop().placeholder(R.drawable.avatar)
                .into(userImg);


        MovementMethod m = commentTxt.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (commentTxt.getLinksClickable()) {
                commentTxt.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }


        commentTxt.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {
            @Override
            public void onTextLinkClick(View textView, String clickedString) {
                if (clickedString.contains("@")) {

                    String user_param = clickedString.replace("@", "");
                    if (GetSet.getUserName().equalsIgnoreCase(user_param))
                        //  Utils.getInstance().GlobalUpdate("profile", "user", ""+user_param);
                        listener.clickListen("profile", "user", user_param);
                    else
                        // Utils.getInstance().GlobalUpdate("otherprofile", "user", ""+user_param);
                        listener.clickListen("otherprofile", "user", user_param);

                } else if (clickedString.contains("#")) {
                    String user_param = clickedString.replace("#", "");
                    /// Utils.getInstance().GlobalUpdate("HashProfile", "tag", ""+user_param);
                    listener.clickListen("HashProfile", "tag", user_param);
                }

            }
        });


    }

}