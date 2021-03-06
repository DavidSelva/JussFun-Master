package com.app.jussfun.ui.feed;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.helper.callback.FeedListener;
import com.app.jussfun.helper.callback.ResponseJsonClass;
import com.app.jussfun.model.CommentsModel;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.Constants;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;

public class CommentsAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> implements ResponseJsonClass.onLikeCommentCallback, FeedListener {

    private static final String TAG = CommentsAdapter.class.getSimpleName();
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    Context context;
    ArrayList<CommentsModel.Result> cmtList;
    ResponseJsonClass responseJsonClass;
    String PostOwnerid = "", postId;
    private FeedListener feedListener;

    public CommentsAdapter(Context context, ArrayList<CommentsModel.Result> cmtList, String PostOwnerid, String postId, FeedListener feedListener, ResponseJsonClass responseJsonClass) {
        this.context = context;
        this.cmtList = cmtList;
        this.PostOwnerid = PostOwnerid;
        this.postId = postId;
        this.responseJsonClass = responseJsonClass;
        this.responseJsonClass.setLikeCommentCallback(CommentsAdapter.this);
        this.feedListener = feedListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comment_item, parent, false);
            return new CommentViewHolder(view);

        } else if (viewType == VIEW_TYPE_LOADING) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loading_item, parent, false);
            return new LoadingViewHolder(view);

        }
        return null;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int adapterPosition) {

        int position = holder.getAdapterPosition();
        if (holder instanceof CommentViewHolder) {

            CommentsModel.Result commentPojo = cmtList.get(position);
            final CommentViewHolder commentViewHolder = (CommentViewHolder) holder;
            commentViewHolder.bind(position, cmtList, PostOwnerid);
            commentViewHolder.setFeedListener(CommentsAdapter.this);

            if (!commentPojo.isReplyVisible()) {
                commentViewHolder.recyclerView.setVisibility(View.GONE);
                commentViewHolder.view_reply.setText(context.getResources().getString(R.string.viewreply));
            } else {
                commentViewHolder.recyclerView.setVisibility(View.VISIBLE);
                commentViewHolder.view_reply.setText(context.getResources().getString(R.string.hidereply));
            }

            commentViewHolder.view_reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!cmtList.get(position).isReplyVisible()) {
                        cmtList.get(position).setReplyVisible(true);
                        commentViewHolder.recyclerView.setVisibility(View.VISIBLE);
                        commentViewHolder.view_reply.setText(context.getResources().getString(R.string.hidereply));
                    } else {
                        cmtList.get(position).setReplyVisible(false);
                        commentViewHolder.recyclerView.setVisibility(View.GONE);
                        commentViewHolder.view_reply.setText(context.getResources().getString(R.string.viewreply));
                    }

                }
            });


            commentViewHolder.reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    feedListener.setParentReplyDetails(position, 0, cmtList.get(position).getCommentId(), cmtList.get(position).getUserName());
                }
            });


            commentViewHolder.userImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    feedListener.navigateToProfile(cmtList.get(position).getUserId());
                }
            });

            commentViewHolder.txtUserName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    feedListener.navigateToProfile(cmtList.get(position).getUserId());
                }
            });

            commentViewHolder.likeLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (cmtList.get(position).getLiked().equalsIgnoreCase("true")) {
                        commentViewHolder.likeClick.setSelected(false);
                        cmtList.get(position).setLiked("false");

                        int lcount = 0;

                        if (cmtList.get(position).getLikeCount() > 0) {
                            lcount = cmtList.get(position).getLikeCount();
                            lcount--;
                        }
                        cmtList.get(position).setLikeCount(lcount);

                        if (lcount == 0)
                            commentViewHolder.like_cnt.setVisibility(View.GONE);
                        else {

                            commentViewHolder.like_cnt.setVisibility(View.VISIBLE);

                            if (lcount == 1)
                                commentViewHolder.like_cnt.setText(lcount + " " + context.getString(R.string.like));
                            else
                                commentViewHolder.like_cnt.setText("" + lcount + " " + context.getString(R.string.likes));

                        }
                    } else {
                        commentViewHolder.likeClick.setSelected(true);
                        cmtList.get(position).setLiked("true");

                        int lcount = 0;
                        lcount = cmtList.get(position).getLikeCount();
                        lcount++;

                        cmtList.get(position).setLikeCount(lcount);

                        if (lcount == 0)
                            commentViewHolder.like_cnt.setVisibility(View.GONE);
                        else {

                            commentViewHolder.like_cnt.setVisibility(View.VISIBLE);

                            if (lcount == 1)
                                commentViewHolder.like_cnt.setText(lcount + " " + context.getString(R.string.like));
                            else
                                commentViewHolder.like_cnt.setText("" + lcount + " " + context.getString(R.string.likes));
                        }

                    }

                    commentViewHolder.likeLay.setEnabled(false);
                    responseJsonClass.likeComment(position, cmtList.get(position).getCommentId(),cmtList.get(position).getLikeCount(), commentViewHolder);

                }
            });

            if (cmtList.get(position).getUserId().equalsIgnoreCase(GetSet.getUserId()) || PostOwnerid.equalsIgnoreCase(GetSet.getUserId()))
                commentViewHolder.swipeLayout.setRightSwipeEnabled(true);
            else
                commentViewHolder.swipeLayout.setRightSwipeEnabled(false);

            commentViewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

            // Drag From Left
            //  commentViewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, commentViewHolder.swipeLayout.findViewById(R.id.leftSwipe));

            // Drag From Right
            commentViewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, commentViewHolder.swipeLayout.findViewById(R.id.rightSwipe));


            commentViewHolder.rightSwipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    responseJsonClass.deleteComment(position, -1, postId, cmtList.get(position).getCommentId(), Constants.TAG_COMMENT);
                }
            });


        } else if (holder instanceof LoadingViewHolder) {

            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;

        }
    }

    @Override
    public int getItemCount() {
        return cmtList == null ? 0 : cmtList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return cmtList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }


    @Override
    public void onLikeCommentStatus(String status, int position, int likeCount, RecyclerView.ViewHolder holder) {

        if (status.equalsIgnoreCase("true")) {
            Log.e(TAG, "onLikeCommentStatus: " + likeCount);

            CommentViewHolder commentViewHolder = (CommentViewHolder) holder;

            commentViewHolder.likeLay.setEnabled(true);

            cmtList.get(position).setLiked(status);                        // For when scroll update data
            cmtList.get(position).setLikeCount(likeCount);                   // For when scroll update data

            if (status.equalsIgnoreCase("true"))
                commentViewHolder.likeClick.setSelected(true);
            else
                commentViewHolder.likeClick.setSelected(false);

            if (likeCount == 0)
                commentViewHolder.like_cnt.setVisibility(View.GONE);
            else {
                commentViewHolder.like_cnt.setVisibility(View.VISIBLE);
                if (likeCount == 1)
                    commentViewHolder.like_cnt.setText(likeCount + " " + context.getString(R.string.like));
                else
                    commentViewHolder.like_cnt.setText("" + likeCount + " " + context.getString(R.string.likes));
            }
        } else {

            CommentViewHolder commentViewHolder = (CommentViewHolder) holder;
            commentViewHolder.likeLay.setEnabled(true);

        }

    }


    @Override
    public int getSwipeLayoutResourceId(int cartList) {
        return R.id.swipe;
    }

    @Override
    public void setParentReplyDetails(int parentPosition, int childPosition, String parentId, String userName) {

    }

    @Override
    public void setChildReplyDetails(int parentPosition, int childPosition, String parentId, String userName) {
        feedListener.setChildReplyDetails(parentPosition, childPosition, parentId, userName);
    }

    @Override
    public void navigateToProfile(String userId) {
        feedListener.navigateToProfile(userId);
    }

    @Override
    public void deleteComment(int parentPos, int position, String postId, String replyId, String type) {
        feedListener.deleteComment(parentPos, position, postId, replyId, Constants.TAG_REPLY);
    }
}
