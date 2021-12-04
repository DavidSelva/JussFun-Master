package com.app.jussfun.external;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

public class MediaPlayerUtils {

    public static MediaPlayer mediaPlayer;
    private static Listener listener;
    private static Handler mHandler;

    /**
     * Get database instance
     *
     * @return database handler instance
     */
    public static void getInstance() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        if (mHandler == null) {
            mHandler = new Handler();
        }
    }

    /**
     * Release mediaPlayer
     */
    public static void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static void pauseMediaPlayer() {
        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    public static void playMediaPlayer(int progress) {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.seekTo(progress);
            mHandler.postDelayed(mRunnable, 100);
        }
    }

    public static void applySeekBarValue(int selectedValue) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(selectedValue);
            mHandler.postDelayed(mRunnable, 100);
        }
    }

    /**
     * Start mediaPlayer
     *
     * @param seekBar
     * @param context
     * @param audioUrl sd card media file
     * @param progress
     * @throws IOException exception
     */
    public static void startAndPlayMediaPlayer(Context context, Uri audioUrl, final Listener listener, int progress) throws IOException {
        MediaPlayerUtils.listener = listener;
        getInstance();
        if (isPlaying()) {
            pauseMediaPlayer();
        }
        releaseMediaPlayer();
        getInstance();
        mediaPlayer.setDataSource(context, audioUrl);
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        mHandler.postDelayed(mRunnable, 100);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                playMediaPlayer(progress);
            }
        });
    }

    public static boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public static int getTotalDuration() {
        return mediaPlayer.getDuration();
    }

    public static int getCurrentDuration() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    private static MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            MediaPlayerUtils.releaseMediaPlayer();
            listener.onAudioComplete();
        }
    };

    private static Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (isPlaying()) {
                    mHandler.postDelayed(mRunnable, 100);
                    listener.onAudioUpdate(mediaPlayer.getCurrentPosition());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public interface Listener {
        void onAudioComplete();

        void onAudioUpdate(int currentPosition);
    }

    public static void stopMediaPlayer() {
        if (mediaPlayer != null) {
            Log.i("TAG", "stopMediaPlayer: "+"stopMediaPlayer");
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer = null;
        }
    }

}