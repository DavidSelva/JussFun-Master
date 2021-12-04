/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.app.jussfun.apprtc.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.URLUtil;

import androidx.appcompat.app.AlertDialog;

import com.app.jussfun.R;
import com.app.jussfun.ui.VideoCallActivity;
import com.app.jussfun.utils.Constants;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;

/**
 * AppRTCUtils provides helper functions for managing thread safety.
 */
public final class AppRTCUtils {

    private static final String TAG = AppRTCUtils.class.getSimpleName();
    public static final String ROOM_FULL_MESSAGE = "Room response error:";
    public static final String PEERCONNECTION_ERROR_MESSAGE = "ICE connection failed.";
    public static final String ROOM_IO_ERROR = "Room IO error:";
    public static final String ROOM_PARSING_ERROR = "Room JSON parsing error:";
    public static final String GAE_POST_ERROR = "GAE POST error:";
    public static final String EXTRA_ROOM_URI = "org.appspot.apprtc.ROOM_URI";
    public static final String EXTRA_ROOMID = "org.appspot.apprtc.ROOMID";
    public static final String EXTRA_URLPARAMETERS = "org.appspot.apprtc.URLPARAMETERS";
    public static final String EXTRA_LOOPBACK = "org.appspot.apprtc.LOOPBACK";
    public static final String EXTRA_VIDEO_CALL = "org.appspot.apprtc.VIDEO_CALL";
    public static final String EXTRA_SCREENCAPTURE = "org.appspot.apprtc.SCREENCAPTURE";
    public static final String EXTRA_CAMERA2 = "org.appspot.apprtc.CAMERA2";
    public static final String EXTRA_VIDEO_WIDTH = "org.appspot.apprtc.VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT = "org.appspot.apprtc.VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_FPS = "org.appspot.apprtc.VIDEO_FPS";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
            "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static final String EXTRA_VIDEO_BITRATE = "org.appspot.apprtc.VIDEO_BITRATE";
    public static final String EXTRA_VIDEOCODEC = "org.appspot.apprtc.VIDEOCODEC";
    public static final String EXTRA_HWCODEC_ENABLED = "org.appspot.apprtc.HWCODEC";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = "org.appspot.apprtc.CAPTURETOTEXTURE";
    public static final String EXTRA_FLEXFEC_ENABLED = "org.appspot.apprtc.FLEXFEC";
    public static final String EXTRA_AUDIO_BITRATE = "org.appspot.apprtc.AUDIO_BITRATE";
    public static final String EXTRA_AUDIOCODEC = "org.appspot.apprtc.AUDIOCODEC";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
            "org.appspot.apprtc.NOAUDIOPROCESSING";
    public static final String EXTRA_AECDUMP_ENABLED = "org.appspot.apprtc.AECDUMP";
    public static final String EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED =
            "org.appspot.apprtc.SAVE_INPUT_AUDIO_TO_FILE";
    public static final String EXTRA_OPENSLES_ENABLED = "org.appspot.apprtc.OPENSLES";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC = "org.appspot.apprtc.DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC = "org.appspot.apprtc.DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS = "org.appspot.apprtc.DISABLE_BUILT_IN_NS";
    public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF =
            "org.appspot.apprtc.DISABLE_WEBRTC_GAIN_CONTROL";
    public static final String EXTRA_DISPLAY_HUD = "org.appspot.apprtc.DISPLAY_HUD";
    public static final String EXTRA_TRACING = "org.appspot.apprtc.TRACING";
    public static final String EXTRA_CMDLINE = "org.appspot.apprtc.CMDLINE";
    public static final String EXTRA_RUNTIME = "org.appspot.apprtc.RUNTIME";
    public static final String EXTRA_VIDEO_FILE_AS_CAMERA = "org.appspot.apprtc.VIDEO_FILE_AS_CAMERA";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE =
            "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH =
            "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT =
            "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
    public static final String EXTRA_USE_VALUES_FROM_INTENT =
            "org.appspot.apprtc.USE_VALUES_FROM_INTENT";
    public static final String EXTRA_DATA_CHANNEL_ENABLED = "org.appspot.apprtc.DATA_CHANNEL_ENABLED";
    public static final String EXTRA_ORDERED = "org.appspot.apprtc.ORDERED";
    public static final String EXTRA_MAX_RETRANSMITS_MS = "org.appspot.apprtc.MAX_RETRANSMITS_MS";
    public static final String EXTRA_MAX_RETRANSMITS = "org.appspot.apprtc.MAX_RETRANSMITS";
    public static final String EXTRA_PROTOCOL = "org.appspot.apprtc.PROTOCOL";
    public static final String EXTRA_NEGOTIATED = "org.appspot.apprtc.NEGOTIATED";
    public static final String EXTRA_ID = "org.appspot.apprtc.ID";
    public static final String EXTRA_ENABLE_RTCEVENTLOG = "org.appspot.apprtc.ENABLE_RTCEVENTLOG";
    public static final String EXTRA_USE_LEGACY_AUDIO_DEVICE =
            "org.appspot.apprtc.USE_LEGACY_AUDIO_DEVICE";

    public static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
    public static final int CALL_PERMISSIONS_REQUEST_CODE = 100;
    // List of mandatory application permissions.
    public static final String[] MANDATORY_PERMISSIONS = {MODIFY_AUDIO_SETTINGS,
            RECORD_AUDIO, INTERNET, CAMERA, WAKE_LOCK};

    // Peer connection statistics callback period in ms.
    public static final int STAT_CALLBACK_PERIOD = 1000;

    private Context context;
    private SharedPreferences sharedPref;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String keyprefRoomServerUrl;
    private String keyprefRoom;
    private String keyprefRoomList;
    public static final int CONNECTION_REQUEST = 1;

    public AppRTCUtils(Context context) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        keyprefResolution = context.getString(R.string.pref_resolution_key);
        keyprefFps = context.getString(R.string.pref_fps_key);
        keyprefVideoBitrateType = context.getString(R.string.pref_maxvideobitrate_key);
        keyprefVideoBitrateValue = context.getString(R.string.pref_maxvideobitratevalue_key);
        keyprefAudioBitrateType = context.getString(R.string.pref_startaudiobitrate_key);
        keyprefAudioBitrateValue = context.getString(R.string.pref_startaudiobitratevalue_key);
        keyprefRoomServerUrl = context.getString(R.string.pref_room_server_url_key);
        keyprefRoom = context.getString(R.string.pref_room_key);
        keyprefRoomList = context.getString(R.string.pref_room_list_key);
        this.context = context;
    }

    private AppRTCUtils() {
    }

    @SuppressWarnings("StringSplitter")
    public Intent connectToRoom(String partnerId, String from, String chatType) {
        String roomUrl = Constants.APPRTC_URL;
        // Video call enabled flag.
        boolean videoCallEnabled = chatType.equals(Constants.TAG_VIDEO);

        // Use screencapture option.
        boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
                EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default);

        // Use Camera2 option.
        boolean useCamera2 = sharedPrefGetBoolean(R.string.pref_camera2_key, EXTRA_CAMERA2,
                R.string.pref_camera2_default);

        // Get default codecs.
        String videoCodec = sharedPrefGetString(R.string.pref_videocodec_key,
                EXTRA_VIDEOCODEC, R.string.pref_videocodec_default, false);
        String audioCodec = sharedPrefGetString(R.string.pref_audiocodec_key,
                EXTRA_AUDIOCODEC, R.string.pref_audiocodec_default, false);

        // Check HW codec flag.
        boolean hwCodec = sharedPrefGetBoolean(R.string.pref_hwcodec_key,
                EXTRA_HWCODEC_ENABLED, R.string.pref_hwcodec_default);

        // Check Capture to texture.
        boolean captureToTexture = sharedPrefGetBoolean(R.string.pref_capturetotexture_key,
                EXTRA_CAPTURETOTEXTURE_ENABLED, R.string.pref_capturetotexture_default
        );

        // Check FlexFEC.
        boolean flexfecEnabled = sharedPrefGetBoolean(R.string.pref_flexfec_key,
                EXTRA_FLEXFEC_ENABLED, R.string.pref_flexfec_default);

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key,
                EXTRA_NOAUDIOPROCESSING_ENABLED, R.string.pref_noaudioprocessing_default
        );

        boolean aecDump = sharedPrefGetBoolean(R.string.pref_aecdump_key,
                EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default);

        boolean saveInputAudioToFile =
                sharedPrefGetBoolean(R.string.pref_enable_save_input_audio_to_file_key,
                        EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED,
                        R.string.pref_enable_save_input_audio_to_file_default);

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPrefGetBoolean(R.string.pref_opensles_key,
                EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default);

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key,
                EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default
        );

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key,
                EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default
        );

        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key,
                EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default
        );

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = sharedPrefGetBoolean(
                R.string.pref_disable_webrtc_agc_and_hpf_key, EXTRA_DISABLE_WEBRTC_AGC_AND_HPF,
                R.string.pref_disable_webrtc_agc_and_hpf_key);

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution =
                    sharedPref.getString(keyprefResolution, context.getString(R.string.pref_resolution_default));
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
        String fps = sharedPref.getString(keyprefFps, context.getString(R.string.pref_fps_default));
        String[] fpsValues = fps.split("[ x]+");
        if (fpsValues.length == 2) {
            try {
                cameraFps = Integer.parseInt(fpsValues[0]);
            } catch (NumberFormatException e) {
                cameraFps = 0;
                Log.e(TAG, "Wrong camera fps setting: " + fps);
            }
        }

        // Check capture quality slider flag.
        boolean captureQualitySlider = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key,
                EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
                R.string.pref_capturequalityslider_default);

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        {
            String bitrateTypeDefault = context.getString(R.string.pref_maxvideobitrate_default);
            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefVideoBitrateValue, context.getString(R.string.pref_maxvideobitratevalue_default));
                videoStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        int audioStartBitrate = 0;
        String bitrateTypeDefault = context.getString(R.string.pref_startaudiobitrate_default);
        String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
        if (!bitrateType.equals(bitrateTypeDefault)) {
            String bitrateValue = sharedPref.getString(
                    keyprefAudioBitrateValue, context.getString(R.string.pref_startaudiobitratevalue_default));
            audioStartBitrate = Integer.parseInt(bitrateValue);
        }

        // Check statistics display option.
        boolean displayHud = sharedPrefGetBoolean(R.string.pref_displayhud_key,
                EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default);

        boolean tracing = sharedPrefGetBoolean(R.string.pref_tracing_key, EXTRA_TRACING,
                R.string.pref_tracing_default);

        // Check Enable RtcEventLog.
        boolean rtcEventLogEnabled = sharedPrefGetBoolean(R.string.pref_enable_rtceventlog_key,
                EXTRA_ENABLE_RTCEVENTLOG, R.string.pref_enable_rtceventlog_default
        );

        // Get datachannel options
        boolean dataChannelEnabled = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key,
                EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default
        );
        boolean ordered = sharedPrefGetBoolean(R.string.pref_ordered_key, EXTRA_ORDERED,
                R.string.pref_ordered_default);
        boolean negotiated = sharedPrefGetBoolean(R.string.pref_negotiated_key,
                EXTRA_NEGOTIATED, R.string.pref_negotiated_default);
        int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key,
                EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default
        );
        int maxRetr =
                sharedPrefGetInteger(R.string.pref_max_retransmits_key, EXTRA_MAX_RETRANSMITS,
                        R.string.pref_max_retransmits_default);
        int id = sharedPrefGetInteger(R.string.pref_data_id_key, EXTRA_ID,
                R.string.pref_data_id_default);
        String protocol = sharedPrefGetString(R.string.pref_data_protocol_key,
                EXTRA_PROTOCOL, R.string.pref_data_protocol_default, false);

        // Start AppRTCMobile activity.
        if (validateUrl(roomUrl)) {
            Uri uri = Uri.parse(roomUrl);
            Intent intent = new Intent(context, VideoCallActivity.class);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(EXTRA_VIDEO_CALL, videoCallEnabled);
            intent.putExtra(EXTRA_SCREENCAPTURE, useScreencapture);
            intent.putExtra(EXTRA_CAMERA2, useCamera2);
            intent.putExtra(EXTRA_VIDEO_WIDTH, videoWidth);
            intent.putExtra(EXTRA_VIDEO_HEIGHT, videoHeight);
            intent.putExtra(EXTRA_VIDEO_FPS, cameraFps);
            intent.putExtra(EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
            intent.putExtra(EXTRA_VIDEO_BITRATE, videoStartBitrate);
            intent.putExtra(EXTRA_VIDEOCODEC, videoCodec);
            intent.putExtra(EXTRA_HWCODEC_ENABLED, hwCodec);
            intent.putExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
            intent.putExtra(EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
            intent.putExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
            intent.putExtra(EXTRA_AECDUMP_ENABLED, aecDump);
            intent.putExtra(EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, saveInputAudioToFile);
            intent.putExtra(EXTRA_OPENSLES_ENABLED, useOpenSLES);
            intent.putExtra(EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
            intent.putExtra(EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
            intent.putExtra(EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
            intent.putExtra(EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
            intent.putExtra(EXTRA_AUDIO_BITRATE, audioStartBitrate);
            intent.putExtra(EXTRA_AUDIOCODEC, audioCodec);
            intent.putExtra(EXTRA_DISPLAY_HUD, displayHud);
            intent.putExtra(EXTRA_TRACING, tracing);
            intent.putExtra(EXTRA_ENABLE_RTCEVENTLOG, rtcEventLogEnabled);
            intent.putExtra(EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);
            intent.putExtra(Constants.TAG_RECEIVER_ID, partnerId);
            intent.putExtra(Constants.TAG_FROM, from);
            intent.putExtra(Constants.TAG_CHAT_TYPE, chatType);

            if (dataChannelEnabled) {
                intent.putExtra(EXTRA_ORDERED, ordered);
                intent.putExtra(EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
                intent.putExtra(EXTRA_MAX_RETRANSMITS, maxRetr);
                intent.putExtra(EXTRA_PROTOCOL, protocol);
                intent.putExtra(EXTRA_NEGOTIATED, negotiated);
                intent.putExtra(EXTRA_ID, id);
            }
            return intent;
        }
        return null;
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private String sharedPrefGetString(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultValue = context.getString(defaultId);
        String attributeName = context.getString(attributeId);
        return sharedPref.getString(attributeName, defaultValue);
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private boolean sharedPrefGetBoolean(
            int attributeId, String intentName, int defaultId) {
        boolean defaultValue = Boolean.parseBoolean(context.getString(defaultId));
        String attributeName = context.getString(attributeId);
        return sharedPref.getBoolean(attributeName, defaultValue);
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private int sharedPrefGetInteger(
            int attributeId, String intentName, int defaultId) {
        String defaultString = context.getString(defaultId);
        int defaultValue = Integer.parseInt(defaultString);
        String attributeName = context.getString(attributeId);
        String value = sharedPref.getString(attributeName, defaultString);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
            return defaultValue;
        }
    }

    private boolean validateUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }

        new AlertDialog.Builder(context)
                .setTitle(context.getText(R.string.invalid_url_title))
                .setMessage(context.getString(R.string.invalid_url_text, url))
                .setCancelable(false)
                .setNeutralButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .create()
                .show();
        return false;
    }

    /**
     * Helper method which throws an exception  when an assertion has failed.
     */
    public static void assertIsTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    /**
     * Helper method for building a string of thread information.
     */
    public static String getThreadInfo() {
        return "@[name=" + Thread.currentThread().getName() + ", id=" + Thread.currentThread().getId()
                + "]";
    }

    /**
     * Information about the current build, taken from system properties.
     */
    public static void logDeviceInfo(String tag) {
        Log.d(tag, "Android SDK: " + Build.VERSION.SDK_INT + ", "
                + "Release: " + Build.VERSION.RELEASE + ", "
                + "Brand: " + Build.BRAND + ", "
                + "Device: " + Build.DEVICE + ", "
                + "Id: " + Build.ID + ", "
                + "Hardware: " + Build.HARDWARE + ", "
                + "Manufacturer: " + Build.MANUFACTURER + ", "
                + "Model: " + Build.MODEL + ", "
                + "Product: " + Build.PRODUCT);
    }
}
