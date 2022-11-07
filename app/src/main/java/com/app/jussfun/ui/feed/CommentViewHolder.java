package com.app.jussfun.ui.feed;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.base.BaseViewHolder;
import com.app.jussfun.external.LinkEllipseTextView;
import com.app.jussfun.helper.callback.FeedListener;
import com.app.jussfun.helper.callback.ResponseJsonClass;
import com.app.jussfun.model.CommentsModel;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.AppUtils;
import com.bumptech.glide.Glide;
import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.List;

public class CommentViewHolder extends BaseViewHolder {

    private static final String TAG = CommentViewHolder.class.getSimpleName();
    public TextView txtUserName;
    public TextView view_reply, cmt_time, like_cnt, reply, txtLoading;
    LinkEllipseTextView commentTxt;
    ImageView userImg, likeClick;
    RelativeLayout viewreply_layout;
    LinearLayout likeLay, leftSwipe, rightSwipe;
    SwipeLayout swipeLayout;
    public RecyclerView recyclerView;
    Context context;
    String PostOwnerId = "";

    public CommentViewHolder(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
        txtUserName = (TextView) itemView.findViewById(R.id.username);
        view_reply = (TextView) itemView.findViewById(R.id.view_reply);
        commentTxt = (LinkEllipseTextView) itemView.findViewById(R.id.commentTxt);
        cmt_time = (TextView) itemView.findViewById(R.id.cmt_time);
        like_cnt = (TextView) itemView.findViewById(R.id.like_cnt);
        reply = (TextView) itemView.findViewById(R.id.reply);
        txtLoading = (TextView) itemView.findViewById(R.id.loading);
        likeClick = (ImageView) itemView.findViewById(R.id.likeClick);
        userImg = (ImageView) itemView.findViewById(R.id.userImage);
        likeLay = (LinearLayout) itemView.findViewById(R.id.likeLay);
//        leftSwipe = (LinearLayout) itemView.findViewById(R.id.leftSwipe);
        rightSwipe = (LinearLayout) itemView.findViewById(R.id.rightSwipe);
        viewreply_layout = (RelativeLayout) itemView.findViewById(R.id.viewreply_layout);
        recyclerView = (RecyclerView) itemView.findViewById(R.id.recyclerview);
    }

    public FeedListener listener;

    public void setFeedListener(FeedListener listener) {
        this.listener = listener;
    }


    @Override
    public void bind(int position, Object object, String PostOwnerId) {

        this.PostOwnerId = PostOwnerId;

        ArrayList<CommentsModel.Result> list = (ArrayList<CommentsModel.Result>) object;
        txtLoading.setVisibility(View.GONE);
        reply.setVisibility(View.VISIBLE);

        commentTxt.setVisibility(View.VISIBLE);
        txtUserName.setText(list.get(position).getUserName());
        commentTxt.setText(AppUtils.stripHtml(list.get(position).getComments()).trim());
        cmt_time.setText(list.get(position).getCommentTime());
        reply.setText(context.getResources().getString(R.string.reply));

        commentTxt.setIsLinkable(true);

        MovementMethod m = commentTxt.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (commentTxt.getLinksClickable()) {
                commentTxt.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }


        commentTxt.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {
            @Override
            public void onTextLinkClick(View textView, String clickedString) {

            }
        });


        if (list.get(position).getLiked().equalsIgnoreCase("true"))
            likeClick.setSelected(true);
        else
            likeClick.setSelected(false);

        if (list.get(position).getLikeCount() != null) {
            if (list.get(position).getLikeCount() == 0)
                like_cnt.setVisibility(View.GONE);
            else {

                like_cnt.setVisibility(View.VISIBLE);

                if (list.get(position).getLikeCount() == 1)
                    like_cnt.setText(list.get(position).getLikeCount() + " " + context.getString(R.string.like));
                else
                    like_cnt.setText("" + list.get(position).getLikeCount() + " " + context.getString(R.string.likes));
            }
        }

        Glide.with(context)
                .load(list.get(position).getUserImage())
                .centerCrop().placeholder(R.drawable.avatar)
                .into(userImg);

        /*if (!list.get(position).isReplyVisible()) {
            recyclerView.setVisibility(View.GONE);
            view_reply.setText(context.getResources().getString(R.string.viewreply));
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            view_reply.setText(context.getResources().getString(R.string.hidereply));
        }*/

        List<CommentsModel.Reply> replyList = list.get(position).getReply();

        if (replyList != null && replyList.size() > 0) {
            viewreply_layout.setVisibility(View.VISIBLE);
        } else {
            viewreply_layout.setVisibility(View.GONE);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        ReplyAdapter replyAdapter = new ReplyAdapter(context, replyList, position, list.get(position).getCommentId());
        recyclerView.setAdapter(replyAdapter);
        replyAdapter.notifyDataSetChanged();
    }


    class ReplyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ResponseJsonClass.onLikeCommentCallback, ReplyViewHolder.clickListener {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        private int parentPos;
        Context context;
        List<CommentsModel.Reply> replylist;
        ResponseJsonClass responseJsonClass;
        String parentId;

        public ReplyAdapter(Context context, List<CommentsModel.Reply> replylist, int parentPos, String parentId) {
            this.context = context;
            this.replylist = replylist;
            this.parentPos = parentPos;
            this.parentId = parentId;
            responseJsonClass = new ResponseJsonClass(context);
            responseJsonClass.setLikeCommentCallback(ReplyAdapter.this);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == VIEW_TYPE_ITEM) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reply, parent, false);
                return new ReplyViewHolder(view);

            } else if (viewType == VIEW_TYPE_LOADING) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loading_item, parent, false);
                return new LoadingViewHolder(view);

            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int adapterPosition) {
            int position = holder.getAdapterPosition();
            if (holder instanceof ReplyViewHolder) {
                CommentsModel.Reply replypojo = replylist.get(position);
                final ReplyViewHolder replyViewHolder = (ReplyViewHolder) holder;
                replyViewHolder.bind(position, replypojo, "");
                replyViewHolder.setListener(this);

                replyViewHolder.replyClick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.setChildReplyDetails(parentPos, position, parentId, replylist.get(position).getUser().getUserName());
                    }
                });

                replyViewHolder.likeLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int lcount = 0;
                        if (replylist.get(position).getLiked() == 1) {

                            replyViewHolder.likeClick.setSelected(false);
                            replylist.get(position).setLiked(0);

                            if (replylist.get(position).getReplyLikeCount() > 0) {
                                lcount = replylist.get(position).getReplyLikeCount();
                                lcount--;
                            }
                            replylist.get(position).setReplyLikeCount(lcount);

                            if (lcount == 0)
                                replyViewHolder.like_cnt.setVisibility(View.GONE);
                            else {

                                replyViewHolder.like_cnt.setVisibility(View.VISIBLE);

                                if (lcount == 1)
                                    replyViewHolder.like_cnt.setText(lcount + " " + context.getString(R.string.like));
                                else
                                    replyViewHolder.like_cnt.setText("" + lcount + " " + context.getString(R.string.likes));

                            }

                        } else {
                            replyViewHolder.likeClick.setSelected(true);
                            replylist.get(position).setLiked(1);

                            lcount = replylist.get(position).getReplyLikeCount();


                            lcount++;

                            replylist.get(position).setReplyLikeCount(lcount);

                            if (lcount == 0)
                                replyViewHolder.like_cnt.setVisibility(View.GONE);
                            else {

                                replyViewHolder.like_cnt.setVisibility(View.VISIBLE);

                                if (lcount == 1)
                                    replyViewHolder.like_cnt.setText(lcount + " " + context.getString(R.string.like));
                                else
                                    replyViewHolder.like_cnt.setText("" + lcount + " " + context.getString(R.string.likes));
                            }

                        }
                        replyViewHolder.likeLay.setEnabled(false);


                        responseJsonClass.likeReply(position, replylist.get(position).getId(), parentId, lcount, replyViewHolder);

                    }
                });

                if (replylist.get(position).getUser().getUserId().equalsIgnoreCase(GetSet.getUserId()) || PostOwnerId.equalsIgnoreCase(GetSet.getUserId()))
                    replyViewHolder.swipeLayout.setRightSwipeEnabled(true);
                else
                    replyViewHolder.swipeLayout.setRightSwipeEnabled(false);


                replyViewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

                // Drag From Left
                //replyViewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, replyViewHolder.swipeLayout.findViewById(R.id.leftSwipe));

                // Drag From Right
                replyViewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, replyViewHolder.swipeLayout.findViewById(R.id.rightSwipe));

                // Handling different events when swiping
                replyViewHolder.rightSwipe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.deleteComment(parentPos, position, replylist.get(position).getId(), parentId, "reply");
                    }
                });

                replyViewHolder.userImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*if (GetSet.getUserId().equalsIgnoreCase(replylist.get(position).getUserId()))
                            // Utils.getInstance().GlobalUpdate("profile", "user", ""+cmtList.get(position).getUserid());
                            CommentsAdapter.fragment.navigateToProfile("profile", "user", replylist.get(position).getUserId());
                        else
                            //Utils.getInstance().GlobalUpdate("otherprofile", "user", ""+cmtList.get(position).getUserid());
                            CommentAdapter.fragment.navigateToProfile("otherprofile", "user", replylist.get(position).getUserId());*/

                    }
                });

                replyViewHolder.username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*if (GetSet.getUserId().equalsIgnoreCase(replylist.get(position).getUserId()))
                            // Utils.getInstance().GlobalUpdate("profile", "user", ""+cmtList.get(position).getUserid());
                            CommentsAdapter.navigateToProfile("profile", "user", replylist.get(position).getUserId());
                        else
                            //Utils.getInstance().GlobalUpdate("otherprofile", "user", ""+cmtList.get(position).getUserid());
                            CommentsAdapter.fragment.navigateToProfile("otherprofile", "user", replylist.get(position).getUserId());*/

                    }
                });

            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            }
        }

        @Override
        public int getItemCount() {
            return replylist == null ? 0 : replylist.size();
        }


        @Override
        public int getItemViewType(int position) {
            return replylist.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        // from Replyview Holder
        @Override
        public void clickListen(String fragment, String way, String userid) {
            listener.navigateToProfile(userid);
        }

        @Override
        public void onLikeCommentStatus(String status, int position, int likeCount, RecyclerView.ViewHolder holder) {

            if (status.equalsIgnoreCase("true")) {
                replylist.get(position).setLiked(1);                        // For when scroll update data
                replylist.get(position).setReplyLikeCount(likeCount);

                ReplyViewHolder replyViewHolder = (ReplyViewHolder) holder;

                replyViewHolder.likeLay.setEnabled(true);

                if (status.equalsIgnoreCase("true"))
                    replyViewHolder.likeClick.setSelected(true);
                else
                    replyViewHolder.likeClick.setSelected(false);


                if (likeCount == 0)
                    replyViewHolder.like_cnt.setVisibility(View.GONE);
                else {
                    replyViewHolder.like_cnt.setVisibility(View.VISIBLE);
                    if (likeCount == 1)
                        replyViewHolder.like_cnt.setText(likeCount + " " + context.getString(R.string.like));
                    else
                        replyViewHolder.like_cnt.setText("" + likeCount + " " + context.getString(R.string.likes));
                }

            } else {
                ReplyViewHolder replyViewHolder = (ReplyViewHolder) holder;

                replyViewHolder.likeLay.setEnabled(true);
            }
        }

    }

}
