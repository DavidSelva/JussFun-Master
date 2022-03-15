/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.app.jussfun.ui.feed;

import android.content.Context;
import android.graphics.Outline;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.app.jussfun.R;
import com.app.jussfun.base.BaseViewHolder;
import com.app.jussfun.external.ProgressWheel;
import com.app.jussfun.external.toro.core.ToroPlayer;
import com.app.jussfun.external.toro.core.ToroUtil;
import com.app.jussfun.external.toro.core.media.PlaybackInfo;
import com.app.jussfun.external.toro.core.widget.Container;
import com.app.jussfun.external.toro.exoplayer.ExoPlayerViewHelper;
import com.app.jussfun.external.toro.exoplayer.Playable;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.Feeds;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;


/**
 * @author eneim (7/1/17).
 */

@SuppressWarnings("WeakerAccess") //
public class PlayerViewHolder extends BaseViewHolder implements ToroPlayer {

    private static final String TAG = PlayerViewHolder.class.getSimpleName();
    public static final int LAYOUT_RES = R.layout.holder_exoplayer_item;
    public static boolean isBuffer = false;
    public PlayerView playerView;
    public ImageView img_vol, thumbnail;
    public TextView durationTxt;
    public ProgressWheel pgsBar;
    public FrameLayout clickLay;
    ExoPlayerViewHelper helper;
    Uri mediaUri;
    DefaultControlDispatcher dispatcher;
    private String way = "";
    private Context mContext;

    private Playable.EventListener listener = new Playable.DefaultEventListener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            super.onPlayerStateChanged(playWhenReady, playbackState);
            Log.d(TAG, "onPlayerStateChanged: " + playWhenReady + ", " + playbackState);
            if (playbackState == Player.STATE_READY) {
//                Log.e(TAG, "onPlayerStateChanged: STATE_READY");
                isBuffer = false;
                thumbnail.setVisibility(View.GONE);
                pgsBar.setVisibility(View.GONE);
                img_vol.setVisibility(View.VISIBLE);
                if (Constants.isMute) {
                    setMute(true);
                    img_vol.setSelected(false);
                } else {
                    setMute(false);
                    img_vol.setSelected(true);
                }
            } else if (playbackState == Player.STATE_BUFFERING) {
                //thumbnail.setVisibility(View.VISIBLE);
                isBuffer = true;
//                Log.e(TAG, "onPlayerStateChanged: STATE_BUFFERING");
                pgsBar.setVisibility(View.VISIBLE);
                img_vol.setVisibility(View.GONE);
                //  durationTxt.setVisibility(View.GONE);
            } else if (playbackState == Player.STATE_ENDED) {
//                Log.e(TAG, "onPlayerStateChanged: STATE_ENDED");
            } else if (playbackState == Player.STATE_IDLE) {
//                Log.e(TAG, "onPlayerStateChanged: STATE_IDLE");
                thumbnail.setVisibility(View.VISIBLE);
            } else {
                isBuffer = false;
                thumbnail.setVisibility(View.GONE);
                pgsBar.setVisibility(View.GONE);
                img_vol.setVisibility(View.VISIBLE);
                //  durationTxt.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            super.onPlayerError(error);
            Log.e(TAG, "onPlayerError: " + error.getMessage());
            try {
                String errorString = null;
                Log.i(TAG, "onPlayerError: " + error.type);
                if (error.type == ExoPlaybackException.TYPE_RENDERER) {
                    Exception cause = error.getRendererException();
                    if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                        // Special case for decoder initialization failures.
                        MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                                (MediaCodecRenderer.DecoderInitializationException) cause;
                        if (decoderInitializationException.diagnosticInfo == null) {
                            if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                                errorString = mContext.getString(R.string.error_querying_decoders);
                            } else if (decoderInitializationException.secureDecoderRequired) {
                                errorString = mContext.getString(R.string.error_no_secure_decoder,
                                        decoderInitializationException.mimeType);
                            } else {
                                errorString = mContext.getString(R.string.error_no_decoder,
                                        decoderInitializationException.mimeType);
                            }
                        } else {
                            errorString = mContext.getString(R.string.error_instantiating_decoder,
                                    decoderInitializationException.diagnosticInfo);
                        }
                    }
                } else if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                    if (!NetworkReceiver.isConnected()) {
                        Toast.makeText(mContext, mContext.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "onPlayerError: " + error.getUnexpectedException().getMessage());
                }
                if (errorString != null) {
                    Toast.makeText(mContext, errorString, Toast.LENGTH_SHORT).show();
                }

                play();
                /*if (isBehindLiveWindow(error)) {
                }*/
            } catch (Exception exception) {
                Log.e(TAG, "onPlayerError: " + exception.getMessage());
            }
        }
    };

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    public PlayerViewHolder(View itemView) {
        super(itemView);
        clickLay = itemView.findViewById(R.id.clickLay);
        playerView = (PlayerView) itemView.findViewById(R.id.player);
        img_vol = (ImageView) itemView.findViewById(R.id.img_vol);
        thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
        durationTxt = (TextView) itemView.findViewById(R.id.durationTxt);
        pgsBar = (ProgressWheel) itemView.findViewById(R.id.pBar);
        dispatcher = new DefaultControlDispatcher();
        mContext = itemView.getContext();

        playerView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 2);
            }
        });

        playerView.setClipToOutline(true);
    }

    @Override
    public void bind(int position, Object object, String way) {
        this.way = way;
        List<Feeds> postMediaItem = (List<Feeds>) object;
        Glide.with(itemView.getContext())
                .load(postMediaItem.get(position).getImageUrl())
                .placeholder(R.drawable.bg_local_video_view)
                .error(R.drawable.bg_local_video_view)
                .thumbnail(0.5f)
                .into(thumbnail);
        mediaUri = Uri.parse(postMediaItem.get(position).getImageUrl());
    }

    public void setMute(boolean isMute) {
        if (helper != null) {
            if (isMute) helper.setVolume(0);
            else helper.setVolume(0.75f);
        }
    }

    @NonNull
    @Override
    public View getPlayerView() {
        return playerView;
    }

    @NonNull
    @Override
    public PlaybackInfo getCurrentPlaybackInfo() {
        return helper != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
    }

    @Override
    public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
        if (helper == null) {

            helper = new ExoPlayerViewHelper(this, mediaUri);
            helper.addEventListener(listener);
        }
        helper.initialize(container, playbackInfo);
    }


    @Override
    public void play() {
        if (helper != null) helper.play();

    }

    @Override
    public void pause() {
        if (helper != null) helper.pause();
    }

    @Override
    public boolean isPlaying() {
        return helper != null && helper.isPlaying();
    }

    @Override
    public void release() {
        if (helper != null) {
            thumbnail.setVisibility(View.VISIBLE);
            helper.release();
            helper = null;
        }
    }

    @Override
    public boolean wantsToPlay() {
        return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.85;
    }

    @Override
    public int getPlayerOrder() {
        return getAdapterPosition();
    }

    @Override
    public String toString() {
        return "ExoPlayer{" + hashCode() + " " + getAdapterPosition() + "}";
    }


}
