package com.app.jussfun.ui.notifications;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.helper.callback.NotificationClickListener;
import com.app.jussfun.model.NotificationResponse;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class NotificationTabAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements NotificationLikeHolder.clickListener {

    private static String TAG = NotificationTabAdapter.class.getSimpleName();

    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_LIKE = 1;
    private final int VIEW_TYPE_FOLLOW = 2;
    private final int VIEW_TYPE_COMMENT = 3;
    private final int VIEW_TYPE_TAGGED = 4;
    private final int VIEW_TYPE_REPLY = 5;
    private final int VIEW_TYPE_ADMIN = 6;
    private final int VIEW_TYPE_POST = 7;

    Context mContext;
    List<NotificationResponse.ResultItem> notificationList = new ArrayList<>();
    NotificationClickListener clickListener;

    public NotificationTabAdapter(Context context,  List<NotificationResponse.ResultItem> notificationList, NotificationClickListener clickListener) {
        this.mContext = context;
        this.notificationList = notificationList;
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_LIKE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_likeitem, parent, false);
            return new NotificationLikeHolder(view);

        } else if (viewType == VIEW_TYPE_FOLLOW) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_followitem, parent, false);
            return new NotificationFollowViewHolder(view);

        } else if (viewType == VIEW_TYPE_COMMENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_likeitem, parent, false);
            return new NotificationCommentHolder(view);
        } else if (viewType == VIEW_TYPE_REPLY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_likeitem, parent, false);
            return new NotificationReplyHolder(view);
        } else if (viewType == VIEW_TYPE_TAGGED) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_likeitem, parent, false);
            return new NotificationTagHolder(view);

        } else if (viewType == VIEW_TYPE_ADMIN) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_admin, parent, false);
            return new NotificationAdminHolder(view);

        } else if (viewType == VIEW_TYPE_POST) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_likeitem, parent, false);
            return new NotificationPostHolder(view);

        }else if (viewType == VIEW_TYPE_LOADING) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loading_item, parent, false);
            return new LoadingViewHolder(view);

        }
        return null;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof NotificationLikeHolder) {
            final NotificationLikeHolder holder = (NotificationLikeHolder) viewHolder;
            holder.setListener(this);
            holder.bind(position, notificationList, "");

            holder.userImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onNavigateToProfile(notificationList.get(position).getLikedUserId(), "user");
                }
            });

            holder.description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onNavigateToProfile(notificationList.get(position).getLikedUserId(), "user");
                }
            });

            holder.post_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notificationList.get(position).setSeen("" + true);
                    notifyItemChanged(position);
                    if (notificationList.get(position).getPostMedia().isEmpty()) {
                        App.showToast(mContext, mContext.getString(R.string.no_post_found));
                    } else {
                        clickListener.onNavigateToPostDetail(notificationList.get(position).getPostType(), notificationList.get(position));
                    }
                }
            });

            holder.recyclerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onNavigateToPostDetail(notificationList.get(position).getPostType(), notificationList.get(position));
                }
            });

        } else if (viewHolder instanceof NotificationCommentHolder) {
            NotificationCommentHolder holder = (NotificationCommentHolder) viewHolder;
            if (notificationList.get(position).getSeen().equals("" + false)) {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
            } else {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
            }

            holder.description.setIsLinkable(true);
            holder.description.setText(Utils.stripHtml("@" + notificationList.get(position).getCommentedUserName() + " " + mContext.getString(R.string.commented) + ": " +
                    notificationList.get(position).getRecentComment() /*+ " " + notificationList.get(position).getLogTime()*/));
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(notificationList.get(position).getLogTime());

            Log.i(TAG, "commentonBindViewHolder: "+notificationList.get(position).getUserImage());
            Glide.with(mContext)
                    .load(notificationList.get(position).getUserImage())
                    .centerCrop().placeholder(R.drawable.user_default)
                    .into(holder.userImg);

            holder.recyclerView.setVisibility(View.GONE);

            holder.post_image.setVisibility(View.VISIBLE);

            List<HomeResponse.PostMediaItem> childList = notificationList.get(position).getPostMedia();

            Glide.with(mContext)
                    .load((childList != null && childList.size() > 0) ? childList.get(0).getImageUrl() : R.drawable.place_holder_loading)
                    .centerCrop()
                    .placeholder(R.drawable.place_holder_loading)
                    .into(holder.post_image);

            holder.userImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userid = notificationList.get(position).getUserId();
                    clickListener.onNavigateToProfile(userid, "user");
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notificationList.get(position).setSeen("" + true);
                    notifyItemChanged(position);
                    clickListener.onNavigateToPostDetail("", notificationList.get(position));
                }
            });

            MovementMethod m = holder.description.getMovementMethod();
            if ((m == null) || !(m instanceof LinkMovementMethod)) {
                if (holder.description.getLinksClickable()) {
                    holder.description.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            holder.description.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {
                @Override
                public void onTextLinkClick(View textView, String clickedString) {
                    if (clickedString.contains("@")) {
                        String user_param = clickedString.replace("@", "");
                        String username = AppPreferences.getStringFromStore(Preference_Constants.PREF_KEY_USER_NAME);
                        if (username.equalsIgnoreCase(user_param)) {
                            clickListener.onNavigateToProfile("profile", "user", AppPreferences.getStringFromStore(Preference_Constants.PREF_KEY_USER_ID));
                        } else
                            clickListener.onNavigateToProfile("otherprofile", "user", user_param);

                    } else if (clickedString.contains("#")) {
                        String user_param = clickedString.replace("#", "");
                        clickListener.onNavigateToHashProfile(user_param, "tag");
                    }

                }
            });

        } else if (viewHolder instanceof NotificationFollowViewHolder) {
            NotificationFollowViewHolder holder = (NotificationFollowViewHolder) viewHolder;

            if (notificationList.get(position).getSeen().equals("" + false)) {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
            } else {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
            }
            if (notificationList.get(position).getFollowStatus().equals(Constants.TAG_TRUE)) {
                holder.btnFollow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.grey_border));
                holder.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.text_primary_color));
                holder.btnFollow.setText(mContext.getString(R.string.unfollow));
            } else {
                holder.btnFollow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.btn_bg_primary));
                holder.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite));
                holder.btnFollow.setText(mContext.getString(R.string.follow));
            }

            holder.btnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.preventMultipleClick(holder.btnFollow);
                    if (notificationList.get(position).getFollowStatus().equals(Constants.TAG_TRUE)) {
//                        navigateListener.onMessageUser(notificationList.get(position));
                        holder.btnFollow.setVisibility(View.GONE);
                        holder.followLoad.setVisibility(View.VISIBLE);
                        responseJsonClass.FollowUnFollow(position, notificationList.get(position).getFollowerId(), "users", holder);
                    } else {
                        holder.btnFollow.setVisibility(View.GONE);
                        holder.followLoad.setVisibility(View.VISIBLE);
                        responseJsonClass.FollowUnFollow(position, notificationList.get(position).getFollowerId(), "users", holder);
                    }
                }
            });

            holder.description.setIsLinkable(true);
            holder.description.setText(Utils.stripHtml("@" + notificationList.get(position).getUsername() + " " + mContext.getString(R.string.started_following_you)
                    /*+ " " + notificationList.get(position).getLogTime()*/));
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(notificationList.get(position).getLogTime());

            Log.i(TAG, "notificationonBindViewHolder: " + notificationList.get(position).getProfilePicture());
            Glide.with(mContext)
                    .load(notificationList.get(position).getProfilePicture())
                    .centerCrop().placeholder(R.drawable.user_default)
                    .thumbnail(0.7f)
                    .into(holder.userImg);


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notificationList.get(position).setSeen("" + true);
                    notifyItemChanged(position);
                    String userid = notificationList.get(position).getFollowerId();
                    clickListener.onNavigateToProfile(userid, "user");
                }
            });


        } else if (viewHolder instanceof NotificationReplyHolder) {
            NotificationReplyHolder holder = (NotificationReplyHolder) viewHolder;
            if (notificationList.get(position).getSeen().equals("" + false)) {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
            } else {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
            }

            holder.description.setIsLinkable(true);
            holder.description.setText(Utils.stripHtml("@" + notificationList.get(position).getReplyUserName() + " " + mContext.getString(R.string.replied_your_comment) + ": " +
                    notificationList.get(position).getReplyComment() /*+ " " + notificationList.get(position).getLogTime()*/));
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(notificationList.get(position).getLogTime());

            Log.i(TAG, "replyonBindViewHolde: "+notificationList.get(position).getReplyComment());
            Log.i(TAG, "replyonBindViewHolder: "+notificationList.get(position).getUserImage());
            Glide.with(mContext)
                    .load(notificationList.get(position).getUserImage())
                    .centerCrop().placeholder(R.drawable.user_default)
                    .into(holder.userImg);
/*
            Glide.with(mContext)
                    .load(notificationList.get(position).getProfilePicture())
                    .centerCrop().placeholder(R.drawable.user_default)
                    .into(holder.userImg);
*/

            holder.recyclerView.setVisibility(View.GONE);

            holder.post_image.setVisibility(View.VISIBLE);

            List<HomeResponse.PostMediaItem> childList = notificationList.get(position).getPostMedia();
            Glide.with(mContext)
                    .load((childList != null && childList.size() > 0) ? childList.get(0).getImageUrl() : R.drawable.place_holder_loading)
                    .centerCrop()
                    .placeholder(R.drawable.place_holder_loading)
                    .into(holder.post_image);

            holder.userImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userid = notificationList.get(position).getUserId();
                    clickListener.onNavigateToProfile(userid, "user");
                }
            });

            MovementMethod m = holder.description.getMovementMethod();
            if ((m == null) || !(m instanceof LinkMovementMethod)) {
                if (holder.description.getLinksClickable()) {
                    holder.description.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            holder.description.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {
                @Override
                public void onTextLinkClick(View textView, String clickedString) {
                    if (clickedString.contains("@")) {
                        String user_param = clickedString.replace("@", "");
                        String username = AppPreferences.getStringFromStore(Preference_Constants.PREF_KEY_USER_NAME);
                        if (username.equalsIgnoreCase(user_param)) {
                            clickListener.onNavigateToProfile("profile", "user", AppPreferences.getStringFromStore(Preference_Constants.PREF_KEY_USER_ID));
                        } else
                            clickListener.onNavigateToProfile("otherprofile", "user", user_param);

                    } else if (clickedString.contains("#")) {
                        String user_param = clickedString.replace("#", "");
                        clickListener.onNavigateToHashProfile(user_param, "tag");
                    }

                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notificationList.get(position).setSeen("" + true);
                    notifyItemChanged(position);
                    clickListener.onNavigateToPostDetail("", notificationList.get(position));
                }
            });

        } else if (viewHolder instanceof NotificationReplyHolder) {
            NotificationReplyHolder holder = (NotificationReplyHolder) viewHolder;
            if (notificationList.get(position).getSeen().equals("" + false)) {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
            } else {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
            }

            holder.description.setIsLinkable(true);
            holder.description.setText(Utils.stripHtml("@" + notificationList.get(position).getCommentedUserName() + " " + mContext.getString(R.string.commented) + ": " +
                    notificationList.get(position).getRecentComment() /*+ " " + notificationList.get(position).getLogTime()*/));
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(notificationList.get(position).getLogTime());

            Glide.with(mContext)
                    .load(notificationList.get(position).getProfilePicture())
                    .centerCrop().placeholder(R.drawable.user_default)
                    .into(holder.userImg);

            holder.recyclerView.setVisibility(View.GONE);

            holder.post_image.setVisibility(View.VISIBLE);

            List<HomeResponse.PostMediaItem> childList = notificationList.get(position).getPostMedia();
            Glide.with(mContext)
                    .load((childList != null && childList.size() > 0) ? childList.get(0).getImageUrl() : R.drawable.place_holder_loading)
                    .centerCrop()
                    .placeholder(R.drawable.place_holder_loading)
                    .into(holder.post_image);

            holder.userImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userid = notificationList.get(position).getUserId();
                    clickListener.onNavigateToProfile(userid, "user");
                }
            });

            MovementMethod m = holder.description.getMovementMethod();
            if ((m == null) || !(m instanceof LinkMovementMethod)) {
                if (holder.description.getLinksClickable()) {
                    holder.description.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            holder.description.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {
                @Override
                public void onTextLinkClick(View textView, String clickedString) {
                    if (clickedString.contains("@")) {
                        String user_param = clickedString.replace("@", "");
                        String username = AppPreferences.getStringFromStore(Preference_Constants.PREF_KEY_USER_NAME);
                        if (username.equalsIgnoreCase(user_param)) {
                            clickListener.onNavigateToProfile("profile", "user", AppPreferences.getStringFromStore(Preference_Constants.PREF_KEY_USER_ID));
                        } else
                            clickListener.onNavigateToProfile("otherprofile", "user", notificationList.get(position).getUserId());

                    } else if (clickedString.contains("#")) {
                        String user_param = clickedString.replace("#", "");
                        clickListener.onNavigateToHashProfile(user_param, "tag");
                    }

                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notificationList.get(position).setSeen("" + true);
                    notifyItemChanged(position);
                    clickListener.onNavigateToPostDetail("", notificationList.get(position));
                }
            });

        } else if (viewHolder instanceof NotificationPostHolder) {
            NotificationPostHolder holder = (NotificationPostHolder) viewHolder;
            if (notificationList.get(position).getSeen().equals("" + false)) {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
            } else {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
            }

            holder.description.setIsLinkable(true);
            holder.description.setText(Utils.stripHtml("@" + notificationList.get(position).getUserName() + " " + mContext.getString(R.string.mentioned_post) + ": " +
                    notificationList.get(position).getPostDescription() /*+ " " + notificationList.get(position).getLogTime()*/));
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(notificationList.get(position).getLogTime());

            Glide.with(mContext)
                    .load(notificationList.get(position).getProfilePicture())
                    .centerCrop().placeholder(R.drawable.user_default)
                    .into(holder.userImg);

            holder.recyclerView.setVisibility(View.GONE);

            holder.post_image.setVisibility(View.VISIBLE);

            List<HomeResponse.PostMediaItem> childList = notificationList.get(position).getPostMedia();
            Glide.with(mContext)
                    .load((childList != null && childList.size() > 0) ? childList.get(0).getImageUrl() : R.drawable.place_holder_loading)
                    .centerCrop()
                    .placeholder(R.drawable.place_holder_loading)
                    .into(holder.post_image);

            holder.userImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userid = notificationList.get(position).getUserId();
                    clickListener.onNavigateToProfile(userid, "user");
                }
            });

            MovementMethod m = holder.description.getMovementMethod();
            if ((m == null) || !(m instanceof LinkMovementMethod)) {
                if (holder.description.getLinksClickable()) {
                    holder.description.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            holder.description.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {
                @Override
                public void onTextLinkClick(View textView, String clickedString) {
                    if (clickedString.contains("@")) {
                        String user_param = clickedString.replace("@", "");
                        String username = AppPreferences.getStringFromStore(Preference_Constants.PREF_KEY_USER_NAME);
                        if (username.equalsIgnoreCase(user_param)) {
                            clickListener.onNavigateToProfile("profile", "user", AppPreferences.getStringFromStore(Preference_Constants.PREF_KEY_USER_ID));
                        } else
                            clickListener.onNavigateToProfile("otherprofile", "user", notificationList.get(position).getUserId());

                    } else if (clickedString.contains("#")) {
                        String user_param = clickedString.replace("#", "");
                        clickListener.onNavigateToHashProfile(user_param, "tag");
                    }

                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notificationList.get(position).setSeen("" + true);
                    notifyItemChanged(position);
                    clickListener.onNavigateToPostDetail("", notificationList.get(position));
                }
            });

        }
        else if (viewHolder instanceof NotificationTagHolder) {
            NotificationTagHolder holder = (NotificationTagHolder) viewHolder;
            if (notificationList.get(position).getSeen().equals("" + false)) {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
            } else {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
            }

            holder.description.setIsLinkable(true);
//            holder.description.setText(Utils.stripHtml("@" + notificationList.get(position).getUserName() + " " + notificationList.get(position).getMessage()));
            holder.description.setText(Utils.stripHtml("@" + notificationList.get(position).getUserName() + " " + mContext.getResources().getString(R.string.tag_you_post)));
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(notificationList.get(position).getLogTime());
            MovementMethod m = holder.description.getMovementMethod();
            if ((m == null) || !(m instanceof LinkMovementMethod)) {
                if (holder.description.getLinksClickable()) {
                    holder.description.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            holder.description.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {
                @Override
                public void onTextLinkClick(View textView, String clickedString) {
                    if (clickedString.contains("@")) {
                        String user_param = clickedString.replace("@", "");
                        String username = AppPreferences.getStringFromStore(Preference_Constants.PREF_KEY_USER_NAME);
                        if (username.equalsIgnoreCase(user_param)) {
                            clickListener.onNavigateToProfile("profile", "user", AppPreferences.getStringFromStore(Preference_Constants.PREF_KEY_USER_ID));
                        } else
                            clickListener.onNavigateToProfile("otherprofile", "user", notificationList.get(position).getUserId());

                    } else if (clickedString.contains("#")) {
                        String user_param = clickedString.replace("#", "");
                        clickListener.onNavigateToHashProfile(user_param, "tag");
                    }

                }
            });

            Glide.with(mContext)
                    .load(notificationList.get(position).getProfilePicture())
                    .centerCrop().placeholder(R.drawable.user_default)
                    .into(holder.userImg);

            holder.recyclerView.setVisibility(View.GONE);

            holder.post_image.setVisibility(View.VISIBLE);

            List<HomeResponse.PostMediaItem> childList = notificationList.get(position).getPostMedia();

            if (childList != null && childList.size() > 0) {
                Glide.with(mContext)
                        .load(childList.get(0).getImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.place_holder_loading)
                        .into(holder.post_image);
            }

            holder.userImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userid = notificationList.get(position).getUserId();
                    clickListener.onNavigateToProfile(userid, "user");
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notificationList.get(position).setSeen("" + true);
                    notifyItemChanged(position);
                    clickListener.onNavigateToPostDetail("", notificationList.get(position));
                }
            });

        } else if (viewHolder instanceof NotificationAdminHolder) {
            NotificationAdminHolder holder = (NotificationAdminHolder) viewHolder;
            if (notificationList.get(position).getSeen().equals("" + false)) {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_medium));
            } else {
                holder.description.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
                holder.time.setTypeface(ResourcesCompat.getFont(mContext, R.font.font_regular));
            }

            Log.i(TAG, "adminonBindViewHolder: " + notificationList.get(position).getDescriptions());

            holder.description.setText(Utils.stripHtml("@" + notificationList.get(position).getLogType() + " " + notificationList.get(position).getDescriptions()));
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(notificationList.get(position).getLogTime());

            Glide.with(mContext)
                    .load(notificationList.get(position).getAdminImage())
                    .centerCrop().placeholder(R.drawable.user_default)
                    .into(holder.userImg);

            List<HomeResponse.PostMediaItem> childList = notificationList.get(position).getPostMedia();


        } else if (viewHolder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) viewHolder;
        }
    }

    @Override
    public int getItemCount() {
        return notificationList == null ? 0 : notificationList.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (notificationList.get(position) == null) return VIEW_TYPE_LOADING;
        else if (notificationList.get(position).getLogType().equals("like")) return VIEW_TYPE_LIKE;
        else if (notificationList.get(position).getLogType().equals("comment"))
            return VIEW_TYPE_COMMENT;
        else if (notificationList.get(position).getLogType().equals("reply"))
            return VIEW_TYPE_REPLY;
        else if (notificationList.get(position).getLogType().equals("tag"))
            return VIEW_TYPE_TAGGED;
        else if (notificationList.get(position).getLogType().equals("admin"))
            return VIEW_TYPE_ADMIN;
        else if (notificationList.get(position).getLogType().equals("user"))
            return VIEW_TYPE_POST;
        else return VIEW_TYPE_FOLLOW;
    }


    @Override
    public void click(int pos) {
        clickListener.onNavigateToPostDetail(notificationList.get(pos).getPostType(), notificationList.get(pos));
    }
}


