package com.brentvatne.exoplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;


import com.facebook.react.bridge.Dynamic;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.bridge.ReactMethod;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.npaw.youbora.lib6.plugin.Options;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;
import java.util.UUID;

import javax.annotation.Nullable;

public class ReactExoplayerViewManager extends ViewGroupManager<ReactExoplayerView> {

    private static final String REACT_CLASS = "RCTVideo";

    private static final String PROP_SRC = "src";
    private static final String PROP_SRC_URI = "uri";
    private static final String PROP_SRC_TYPE = "type";
    private static final String PROP_DRM = "drm";
    private static final String PROP_DRM_TYPE = "type";
    private static final String PROP_DRM_LICENSESERVER = "licenseServer";
    private static final String PROP_DRM_HEADERS = "headers";
    private static final String PROP_SRC_HEADERS = "requestHeaders";
    private static final String PROP_RESIZE_MODE = "resizeMode";
    private static final String PROP_AD_BREAK_POINT = "adsBreakPoints";
    private static final String PROP_REPEAT = "repeat";
    private static final String PROP_SELECTED_AUDIO_TRACK = "selectedAudioTrack";
    private static final String PROP_SELECTED_AUDIO_TRACK_TYPE = "type";
    private static final String PROP_SELECTED_AUDIO_TRACK_VALUE = "value";
    private static final String PROP_SELECTED_TEXT_TRACK = "selectedTextTrack";
    private static final String PROP_SELECTED_TEXT_TRACK_TYPE = "type";
    private static final String PROP_SELECTED_TEXT_TRACK_VALUE = "value";
    private static final String PROP_TEXT_TRACKS = "textTracks";
    private static final String PROP_PAUSED = "paused";
    private static final String PROP_MUTED = "muted";
    private static final String PROP_VOLUME = "volume";
    private static final String PROP_BUFFER_CONFIG = "bufferConfig";
    private static final String PROP_BUFFER_CONFIG_MIN_BUFFER_MS = "minBufferMs";
    private static final String PROP_BUFFER_CONFIG_MAX_BUFFER_MS = "maxBufferMs";
    private static final String PROP_BUFFER_CONFIG_BUFFER_FOR_PLAYBACK_MS = "bufferForPlaybackMs";
    private static final String PROP_BUFFER_CONFIG_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = "bufferForPlaybackAfterRebufferMs";
    private static final String PROP_PREVENTS_DISPLAY_SLEEP_DURING_VIDEO_PLAYBACK = "preventsDisplaySleepDuringVideoPlayback";
    private static final String PROP_PROGRESS_UPDATE_INTERVAL = "progressUpdateInterval";
    private static final String PROP_REPORT_BANDWIDTH = "reportBandwidth";
    private static final String PROP_SEEK = "seek";
    private static final String PROP_RATE = "rate";
    private static final String PROP_MIN_LOAD_RETRY_COUNT = "minLoadRetryCount";
    private static final String PROP_MAXIMUM_BIT_RATE = "maxBitRate";
    private static final String PROP_PLAY_IN_BACKGROUND = "playInBackground";
    private static final String PROP_DISABLE_FOCUS = "disableFocus";
    private static final String PROP_FULLSCREEN = "fullscreen";
    private static final String PROP_USE_TEXTURE_VIEW = "useTextureView";
    private static final String PROP_SELECTED_VIDEO_TRACK = "selectedVideoTrack";
    private static final String PROP_SELECTED_VIDEO_TRACK_TYPE = "type";
    private static final String PROP_SELECTED_VIDEO_TRACK_VALUE = "value";
    private static final String PROP_HIDE_SHUTTER_VIEW = "hideShutterView";
    private static final String PROP_CONTROLS = "controls";
    private static final String PROP_IS_DRM = "isDrm";
    private static final String PROP_ENABLE_CDN_BALANCER = "enableCdnBalancer";
    private static final String PROP_IS_LIVE = "isLive";

    private static final String PROP_FONT_SIZE_TRACK = "fontSizeTrack";
    private static final String PROP_PADDING_BOTTOM_TRACK = "paddingBottomTrack";

    private static final String PROP_YOUBORA_PARAMS = "youboraParams";
    private static final String PROP_YOUBORA_ACCOUNT_CODE = "accountCode";
    private static final String PROP_YOUBORA_USERNAME = "username";
    private static final String PROP_YOUBORA_CONTENT_TRANSACTION_CODE = "contentTransactionCode";
    private static final String PROP_YOUBORA_IS_LIVE = "isLive";
    private static final String PROP_YOUBORA_PARSE_CDN_NODE = "parseCdnNode";
    private static final String PROP_YOUBORA_ENABLED = "enabled";
    private static final String PROP_YOUBORA_TITLE = "title";
    private static final String PROP_YOUBORA_PROGRAM = "program";
    private static final String PROP_YOUBORA_TV_SHOW = "tvShow";
    private static final String PROP_YOUBORA_SEASON = "season";
    private static final String PROP_YOUBORA_CONTENT_TYPE = "contentType";
    private static final String PROP_YOUBORA_CONTENT_ID = "contentId";
    private static final String PROP_YOUBORA_CONTENT_PLAYBACK_TYPE = "contentPlaybackType";
    private static final String PROP_YOUBORA_CONTENT_DURATION = "contentDuration";
    private static final String PROP_YOUBORA_CONTENT_DRM = "contentDrm";
    private static final String PROP_YOUBORA_CONTENT_RESOURCE = "contentResource";
    private static final String PROP_YOUBORA_CONTENT_GENRE = "contentGenre";
    private static final String PROP_YOUBORA_CONTENT_LANGUAGE = "contentLanguage";
    private static final String PROP_YOUBORA_CONTENT_CHANNELS = "contentChannels";
    private static final String PROP_YOUBORA_CONTENT_STREAMING_PROTOCOL = "contentStreamingProtocol";
    private static final String PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_1 = "contentCustomDimension1";
    private static final String PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_2 = "contentCustomDimension2";
    private static final String PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_3 = "contentCustomDimension3";
    private static final String PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_4 = "contentCustomDimension4";
    private static final String PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_5 = "contentCustomDimension5";
    private static final String PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_6 = "contentCustomDimension6";
    private static final String PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_7 = "contentCustomDimension7";
    private static final String PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_8 = "contentCustomDimension8";
    private static final String PROP_YOUBORA_RENDITION= "rendition";
    private static final String PROP_YOUBORA_USER_TYPE = "userType";
    private static final String PROP_YOUBORA_APP_NAME = "appName";
    private static final String PROP_YOUBORA_RELEASE_VERSION = "releaseVersion";
    private static final String PROP_LANGUAGE = "language";

    // analytics props
    private static final String PROP_ANALYTICS = "analytics";

    private ReactExoplayerConfig config;

    public ReactExoplayerViewManager(ReactExoplayerConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected ReactExoplayerView createViewInstance(ThemedReactContext themedReactContext) {
        return new ReactExoplayerView(themedReactContext, config);
    }

    @Override
    public void onDropViewInstance(ReactExoplayerView view) {
        view.cleanUpResources();
    }

    @Override
    public @Nullable Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        for (String event : VideoEventEmitter.Events) {
            builder.put(event, MapBuilder.of("registrationName", event));
        }
        return builder.build();
    }

    @Override
    public @Nullable Map<String, Object> getExportedViewConstants() {
        return MapBuilder.<String, Object>of(
                "ScaleNone", Integer.toString(ResizeMode.RESIZE_MODE_FIT),
                "ScaleAspectFit", Integer.toString(ResizeMode.RESIZE_MODE_FIT),
                "ScaleToFill", Integer.toString(ResizeMode.RESIZE_MODE_FILL),
                "ScaleAspectFill", Integer.toString(ResizeMode.RESIZE_MODE_CENTER_CROP)
        );
    }

    @ReactProp(name = PROP_DRM)
    public void setDRM(final ReactExoplayerView videoView, @Nullable ReadableMap drm) {
        if (drm != null && drm.hasKey(PROP_DRM_TYPE)) {
            String drmType = drm.hasKey(PROP_DRM_TYPE) ? drm.getString(PROP_DRM_TYPE) : null;
            String drmLicenseServer = drm.hasKey(PROP_DRM_LICENSESERVER) ? drm.getString(PROP_DRM_LICENSESERVER) : null;
            ReadableMap drmHeaders = drm.hasKey(PROP_DRM_HEADERS) ? drm.getMap(PROP_DRM_HEADERS) : null;
            if (drmType != null && drmLicenseServer != null && Util.getDrmUuid(drmType) != null) {
                UUID drmUUID = Util.getDrmUuid(drmType);
                videoView.setDrmType(drmUUID);
                videoView.setDrmLicenseUrl(drmLicenseServer);
                if (drmHeaders != null) {
                    ArrayList<String> drmKeyRequestPropertiesList = new ArrayList<>();
                    ReadableMapKeySetIterator itr = drmHeaders.keySetIterator();
                    while (itr.hasNextKey()) {
                        String key = itr.nextKey();
                        drmKeyRequestPropertiesList.add(key);
                        drmKeyRequestPropertiesList.add(drmHeaders.getString(key));
                    }
                    videoView.setDrmLicenseHeader(drmKeyRequestPropertiesList.toArray(new String[0]));
                }
                videoView.setUseTextureView(false);
            }
        }
    }

    @ReactProp(name = PROP_SRC)
    public void setSrc(final ReactExoplayerView videoView, @Nullable ReadableMap src) {
        Context context = videoView.getContext().getApplicationContext();
        String uriString = src.hasKey(PROP_SRC_URI) ? src.getString(PROP_SRC_URI) : null;
        String extension = src.hasKey(PROP_SRC_TYPE) ? src.getString(PROP_SRC_TYPE) : null;
        Map<String, String> headers = src.hasKey(PROP_SRC_HEADERS) ? toStringMap(src.getMap(PROP_SRC_HEADERS)) : null;

        if (TextUtils.isEmpty(uriString)) {
            videoView.clearSrc();
            return;
        }

        if (startsWithValidScheme(uriString)) {
            Uri srcUri = Uri.parse(uriString);
            String adsId = src.hasKey("adsId") ? src.getString("adsId") : null;

            if (srcUri != null) {
                videoView.setSrc(srcUri, extension, headers, adsId);
            }
        } else {
            int identifier = context.getResources().getIdentifier(
                    uriString,
                    "drawable",
                    context.getPackageName()
            );
            if (identifier == 0) {
                identifier = context.getResources().getIdentifier(
                        uriString,
                        "raw",
                        context.getPackageName()
                );
            }
            if (identifier > 0) {
                Uri srcUri = RawResourceDataSource.buildRawResourceUri(identifier);
                if (srcUri != null) {
                    videoView.setRawSrc(srcUri, extension);
                }
            }
        }
    }

    @ReactProp(name = PROP_RESIZE_MODE)
    public void setResizeMode(final ReactExoplayerView videoView, final String resizeModeOrdinalString) {
        videoView.setResizeModeModifier(convertToIntDef(resizeModeOrdinalString));
    }
    @ReactProp(name = PROP_REPEAT, defaultBoolean = false)
    public void setRepeat(final ReactExoplayerView videoView, final boolean repeat) {
        videoView.setRepeatModifier(repeat);
    }

    @ReactProp(name = PROP_PREVENTS_DISPLAY_SLEEP_DURING_VIDEO_PLAYBACK, defaultBoolean = false)
    public void setPreventsDisplaySleepDuringVideoPlayback(final ReactExoplayerView videoView, final boolean preventsSleep) {
        videoView.setPreventsDisplaySleepDuringVideoPlayback(preventsSleep);
    }

    @ReactProp(name = PROP_SELECTED_VIDEO_TRACK)
    public void setSelectedVideoTrack(final ReactExoplayerView videoView,
                                      @Nullable ReadableMap selectedVideoTrack) {
        String typeString = null;
        Dynamic value = null;
        if (selectedVideoTrack != null) {
            typeString = selectedVideoTrack.hasKey(PROP_SELECTED_VIDEO_TRACK_TYPE)
                    ? selectedVideoTrack.getString(PROP_SELECTED_VIDEO_TRACK_TYPE) : null;
            value = selectedVideoTrack.hasKey(PROP_SELECTED_VIDEO_TRACK_VALUE)
                    ? selectedVideoTrack.getDynamic(PROP_SELECTED_VIDEO_TRACK_VALUE) : null;
        }
        videoView.setSelectedVideoTrack(typeString, value);
    }

    @ReactProp(name = PROP_SELECTED_AUDIO_TRACK)
    public void setSelectedAudioTrack(final ReactExoplayerView videoView,
                                      @Nullable ReadableMap selectedAudioTrack) {
        String typeString = null;
        Dynamic value = null;
        if (selectedAudioTrack != null) {
            typeString = selectedAudioTrack.hasKey(PROP_SELECTED_AUDIO_TRACK_TYPE)
                    ? selectedAudioTrack.getString(PROP_SELECTED_AUDIO_TRACK_TYPE) : null;
            value = selectedAudioTrack.hasKey(PROP_SELECTED_AUDIO_TRACK_VALUE)
                    ? selectedAudioTrack.getDynamic(PROP_SELECTED_AUDIO_TRACK_VALUE) : null;
        }
        videoView.setSelectedAudioTrack(typeString, value);
    }

    @ReactProp(name = PROP_SELECTED_TEXT_TRACK)
    public void setSelectedTextTrack(final ReactExoplayerView videoView,
                                     @Nullable ReadableMap selectedTextTrack) {
        String typeString = null;
        Dynamic value = null;
        if (selectedTextTrack != null) {
            typeString = selectedTextTrack.hasKey(PROP_SELECTED_TEXT_TRACK_TYPE)
                    ? selectedTextTrack.getString(PROP_SELECTED_TEXT_TRACK_TYPE) : null;
            value = selectedTextTrack.hasKey(PROP_SELECTED_TEXT_TRACK_VALUE)
                    ? selectedTextTrack.getDynamic(PROP_SELECTED_TEXT_TRACK_VALUE) : null;
        }
        videoView.setSelectedTextTrack(typeString, value);
    }

    @ReactProp(name = PROP_AD_BREAK_POINT)
    public void setAdsBreakPoints(final ReactExoplayerView videoView,
                                  @Nullable ReadableArray adsBreakPoints) {
        videoView.setAdsBreakPoints(adsBreakPoints);
    }

    @ReactProp(name = PROP_TEXT_TRACKS)
    public void setPropTextTracks(final ReactExoplayerView videoView,
                                  @Nullable ReadableArray textTracks) {

        videoView.setTextTracks(textTracks);
    }

    @ReactProp(name = PROP_PAUSED, defaultBoolean = false)
    public void setPaused(final ReactExoplayerView videoView, final boolean paused) {
        videoView.setPausedModifier(paused);
    }

    @ReactProp(name = PROP_IS_DRM, defaultBoolean = false)
    public void setIsDrm(final ReactExoplayerView videoView, final boolean isDrm) {
        videoView.setIsDrmModifier(isDrm);
    }
    @ReactProp(name = PROP_ENABLE_CDN_BALANCER, defaultBoolean = false)
    public void setEnableCdnBalancer(final ReactExoplayerView videoView, final boolean enableCdnBalancer) {
        videoView.setEnableCdnBalancerModifier(enableCdnBalancer);
    }

    @ReactProp(name = PROP_IS_LIVE, defaultBoolean = false)
    public void setIsLive(final ReactExoplayerView videoView, final boolean isLive) {
        videoView.setIsLiveModifier(isLive);
    }

    @ReactProp(name = PROP_MUTED, defaultBoolean = false)
    public void setMuted(final ReactExoplayerView videoView, final boolean muted) {
        videoView.setMutedModifier(muted);
    }

    @ReactProp(name = PROP_VOLUME, defaultFloat = 1.0f)
    public void setVolume(final ReactExoplayerView videoView, final float volume) {
        videoView.setVolumeModifier(volume);
    }

    @ReactProp(name = PROP_PROGRESS_UPDATE_INTERVAL, defaultFloat = 250.0f)
    public void setProgressUpdateInterval(final ReactExoplayerView videoView, final float progressUpdateInterval) {
        videoView.setProgressUpdateInterval(progressUpdateInterval);
    }

    @ReactProp(name = PROP_REPORT_BANDWIDTH, defaultBoolean = false)
    public void setReportBandwidth(final ReactExoplayerView videoView, final boolean reportBandwidth) {
        videoView.setReportBandwidth(reportBandwidth);
    }

    @ReactProp(name = PROP_SEEK)
    public void setSeek(final ReactExoplayerView videoView, final float seek) {
        videoView.seekTo(Math.round(seek * 1000f));
    }

    @ReactProp(name = PROP_RATE)
    public void setRate(final ReactExoplayerView videoView, final float rate) {
        videoView.setRateModifier(rate);
    }

    @ReactProp(name = PROP_MAXIMUM_BIT_RATE)
    public void setMaxBitRate(final ReactExoplayerView videoView, final int maxBitRate) {
        videoView.setMaxBitRateModifier(maxBitRate);
    }

    @ReactProp(name = PROP_MIN_LOAD_RETRY_COUNT)
    public void minLoadRetryCount(final ReactExoplayerView videoView, final int minLoadRetryCount) {
        videoView.setMinLoadRetryCountModifier(minLoadRetryCount);
    }

    @ReactProp(name = PROP_PLAY_IN_BACKGROUND, defaultBoolean = false)
    public void setPlayInBackground(final ReactExoplayerView videoView, final boolean playInBackground) {
        videoView.setPlayInBackground(playInBackground);
    }

    @ReactProp(name = PROP_DISABLE_FOCUS, defaultBoolean = false)
    public void setDisableFocus(final ReactExoplayerView videoView, final boolean disableFocus) {
        videoView.setDisableFocus(disableFocus);
    }

    @ReactProp(name = PROP_FULLSCREEN, defaultBoolean = false)
    public void setFullscreen(final ReactExoplayerView videoView, final boolean fullscreen) {
        videoView.setFullscreen(fullscreen);
    }

    @ReactProp(name = PROP_USE_TEXTURE_VIEW, defaultBoolean = true)
    public void setUseTextureView(final ReactExoplayerView videoView, final boolean useTextureView) {
        videoView.setUseTextureView(useTextureView);
    }

    @ReactProp(name = PROP_HIDE_SHUTTER_VIEW, defaultBoolean = false)
    public void setHideShutterView(final ReactExoplayerView videoView, final boolean hideShutterView) {
        videoView.setHideShutterView(hideShutterView);
    }

    @ReactProp(name = PROP_CONTROLS, defaultBoolean = false)
    public void setControls(final ReactExoplayerView videoView, final boolean controls) {
        videoView.setControls(controls);
    }

    @ReactProp(name = PROP_FONT_SIZE_TRACK)
    public void setFontSizeTrack(final ReactExoplayerView videoView, final int fontSizeTrack) {
        videoView.setFontSizeTrack(fontSizeTrack);
    }

    @ReactProp(name = PROP_PADDING_BOTTOM_TRACK, defaultFloat = 0f)
    public void setPaddingBottomTrack(final ReactExoplayerView videoView, final float paddingBottomTrack) {
        videoView.setPaddingBottomTrack(paddingBottomTrack);
    }

    @ReactProp(name = PROP_BUFFER_CONFIG)
    public void setBufferConfig(final ReactExoplayerView videoView, @Nullable ReadableMap bufferConfig) {
        int minBufferMs = DefaultLoadControl.DEFAULT_MIN_BUFFER_MS;
        int maxBufferMs = DefaultLoadControl.DEFAULT_MAX_BUFFER_MS;
        int bufferForPlaybackMs = DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS;
        int bufferForPlaybackAfterRebufferMs = DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS;
        if (bufferConfig != null) {
            minBufferMs = bufferConfig.hasKey(PROP_BUFFER_CONFIG_MIN_BUFFER_MS)
                    ? bufferConfig.getInt(PROP_BUFFER_CONFIG_MIN_BUFFER_MS) : minBufferMs;
            maxBufferMs = bufferConfig.hasKey(PROP_BUFFER_CONFIG_MAX_BUFFER_MS)
                    ? bufferConfig.getInt(PROP_BUFFER_CONFIG_MAX_BUFFER_MS) : maxBufferMs;
            bufferForPlaybackMs = bufferConfig.hasKey(PROP_BUFFER_CONFIG_BUFFER_FOR_PLAYBACK_MS)
                    ? bufferConfig.getInt(PROP_BUFFER_CONFIG_BUFFER_FOR_PLAYBACK_MS) : bufferForPlaybackMs;
            bufferForPlaybackAfterRebufferMs = bufferConfig.hasKey(PROP_BUFFER_CONFIG_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
                    ? bufferConfig.getInt(PROP_BUFFER_CONFIG_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS) : bufferForPlaybackAfterRebufferMs;
            videoView.setBufferConfig(minBufferMs, maxBufferMs, bufferForPlaybackMs, bufferForPlaybackAfterRebufferMs);
        }
    }

    @ReactProp(name = PROP_YOUBORA_PARAMS)
    public void setYouboraParams(final ReactExoplayerView videoView, @Nullable ReadableMap src) {
        if (src == null){
            videoView.setYouboraParams(null);
            return;
        }

        String accountCode = src.hasKey(PROP_YOUBORA_ACCOUNT_CODE) ? src.getString(PROP_YOUBORA_ACCOUNT_CODE) : null;
        String username = src.hasKey(PROP_YOUBORA_USERNAME) ? src.getString(PROP_YOUBORA_USERNAME) : null;
        String contentTransactionCode = src.hasKey(PROP_YOUBORA_CONTENT_TRANSACTION_CODE) ? src.getString(PROP_YOUBORA_CONTENT_TRANSACTION_CODE) : null;
        boolean isLive = src.hasKey(PROP_YOUBORA_IS_LIVE) ? src.getBoolean(PROP_YOUBORA_IS_LIVE) : false;
        boolean parseCdnNode = src.hasKey(PROP_YOUBORA_PARSE_CDN_NODE) ? src.getBoolean(PROP_YOUBORA_PARSE_CDN_NODE) : false;
        boolean enabled = src.hasKey(PROP_YOUBORA_ENABLED) ? src.getBoolean(PROP_YOUBORA_ENABLED) : false;
        String title = src.hasKey(PROP_YOUBORA_TITLE) ? src.getString(PROP_YOUBORA_TITLE) : null;
        String program = src.hasKey(PROP_YOUBORA_PROGRAM) ? src.getString(PROP_YOUBORA_PROGRAM) : null;
        String tvShow = src.hasKey(PROP_YOUBORA_TV_SHOW) ? src.getString(PROP_YOUBORA_TV_SHOW) : null;
        String season = src.hasKey(PROP_YOUBORA_SEASON) ? src.getString(PROP_YOUBORA_SEASON) : null;
        String contentType = src.hasKey(PROP_YOUBORA_CONTENT_TYPE) ? src.getString(PROP_YOUBORA_CONTENT_TYPE) : null;
        String contentId = src.hasKey(PROP_YOUBORA_CONTENT_ID) ? src.getString(PROP_YOUBORA_CONTENT_ID) : null;
        String contentPlaybackType = src.hasKey(PROP_YOUBORA_CONTENT_PLAYBACK_TYPE) ? src.getString(PROP_YOUBORA_CONTENT_PLAYBACK_TYPE) : null;
        double contentDuration = src.hasKey(PROP_YOUBORA_CONTENT_DURATION) ? src.getDouble(PROP_YOUBORA_CONTENT_DURATION) : 0.00;
        boolean contentDrm = src.hasKey(PROP_YOUBORA_CONTENT_DRM) ? src.getBoolean(PROP_YOUBORA_CONTENT_DRM) : false;
        String contentResource = src.hasKey(PROP_YOUBORA_CONTENT_RESOURCE) ? src.getString(PROP_YOUBORA_CONTENT_RESOURCE) : null;
        String contentGenre = src.hasKey(PROP_YOUBORA_CONTENT_GENRE) ? src.getString(PROP_YOUBORA_CONTENT_GENRE) : null;
        String contentLanguage = src.hasKey(PROP_YOUBORA_CONTENT_LANGUAGE) ? src.getString(PROP_YOUBORA_CONTENT_LANGUAGE) : null;
        String contentChannels = src.hasKey(PROP_YOUBORA_CONTENT_CHANNELS) ? src.getString(PROP_YOUBORA_CONTENT_CHANNELS) : null;
        String contentStreamingProtocol = src.hasKey(PROP_YOUBORA_CONTENT_STREAMING_PROTOCOL) ? src.getString(PROP_YOUBORA_CONTENT_STREAMING_PROTOCOL) : null;
        String contentCustomDimension1 = src.hasKey(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_1) ? src.getString(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_1) : null;
        String contentCustomDimension2 = src.hasKey(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_2) ? src.getString(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_2) : null;
        String contentCustomDimension3 = src.hasKey(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_3) ? src.getString(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_3) : null;
        String contentCustomDimension4 = src.hasKey(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_4) ? src.getString(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_4) : null;
        String contentCustomDimension5 = src.hasKey(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_5) ? src.getString(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_5) : null;
        String contentCustomDimension6 = src.hasKey(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_6) ? src.getString(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_6) : null;
        String contentCustomDimension7 = src.hasKey(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_7) ? src.getString(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_7) : null;
        String contentCustomDimension8 = src.hasKey(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_8) ? src.getString(PROP_YOUBORA_CONTENT_CUSTOM_DIMENSION_8) : null;
        String rendition = src.hasKey(PROP_YOUBORA_RENDITION) ? src.getString(PROP_YOUBORA_RENDITION) : null;
        String userType = src.hasKey(PROP_YOUBORA_USER_TYPE) ? src.getString(PROP_YOUBORA_USER_TYPE) : null;

        Options youboraOptions = new Options();

        youboraOptions.setAccountCode(accountCode); // Account code
        youboraOptions.setUsername(username);  // UserId or Guest
        youboraOptions.setContentTransactionCode(contentTransactionCode); // Subscribed or Free
        youboraOptions.setContentIsLive(isLive); // VOD or Live
        youboraOptions.setParseCdnNode(parseCdnNode);   // Allow Youbora to parse CDN from Host
        youboraOptions.setEnabled(enabled);
        youboraOptions.setContentRendition(rendition);

        youboraOptions.setContentTitle(title);  // Content Title
        youboraOptions.setProgram(program);  // Content Title 2
        youboraOptions.setContentTvShow(tvShow); // Show name for shows, otherwise empty
        youboraOptions.setContentSeason(season); // Season number for shows, otherwise empty
        youboraOptions.setContentType(contentType);  // movie, series & program
        youboraOptions.setContentId(contentId);  // Content Id
        youboraOptions.setContentPlaybackType(contentPlaybackType); // sVOD or aVOD
        youboraOptions.setContentDuration(contentDuration); // Duration in millis
        youboraOptions.setContentDrm(String.valueOf(contentDrm));

        youboraOptions.setContentResource(contentResource);    // Content Url
        youboraOptions.setContentGenre(contentGenre);  // Content Genre comma separated
        youboraOptions.setContentLanguage(contentLanguage); // Content dialects comma separated
        youboraOptions.setContentChannel(contentChannels);

        youboraOptions.setContentStreamingProtocol(contentStreamingProtocol); // HLS, widevine or widevine dash

        youboraOptions.setContentCustomDimension1(contentCustomDimension1);
        youboraOptions.setContentCustomDimension2(contentCustomDimension2);
        youboraOptions.setContentCustomDimension3(contentCustomDimension3);
        youboraOptions.setContentCustomDimension4(contentCustomDimension4);
        youboraOptions.setContentCustomDimension5(contentCustomDimension5);
        youboraOptions.setContentCustomDimension6(contentCustomDimension6);
        youboraOptions.setContentCustomDimension7(contentCustomDimension7);
        youboraOptions.setContentCustomDimension8(contentCustomDimension8);
        youboraOptions.setUserType(userType);

        youboraOptions.setAppName(src.hasKey(PROP_YOUBORA_APP_NAME) ? src.getString(PROP_YOUBORA_APP_NAME) : null);
        youboraOptions.setAppReleaseVersion(src.hasKey(PROP_YOUBORA_RELEASE_VERSION) ? src.getString(PROP_YOUBORA_RELEASE_VERSION) : null);
        youboraOptions.setDeviceCode("AndroidTV");

        videoView.setYouboraParams(youboraOptions);
    }

    @ReactProp(name = PROP_LANGUAGE)
    public void setLanguageParams(final ReactExoplayerView videoView, @Nullable String lang){
        videoView.setLanguage(lang);
    }

    @ReactProp(name = PROP_ANALYTICS)
    public void setAnalytics(final ReactExoplayerView videoView,@Nullable ReadableMap analyticsParams){
        videoView.setAnalyticsParams(analyticsParams);
    }

    private boolean startsWithValidScheme(String uriString) {
        return uriString.startsWith("http://")
                || uriString.startsWith("https://")
                || uriString.startsWith("content://")
                || uriString.startsWith("file://")
                || uriString.startsWith("asset://");
    }

    private @ResizeMode.Mode int convertToIntDef(String resizeModeOrdinalString) {
        if (!TextUtils.isEmpty(resizeModeOrdinalString)) {
            int resizeModeOrdinal = Integer.parseInt(resizeModeOrdinalString);
            return ResizeMode.toResizeMode(resizeModeOrdinal);
        }
        return ResizeMode.RESIZE_MODE_FIT;
    }

    /**
     * toStringMap converts a {@link ReadableMap} into a HashMap.
     *
     * @param readableMap The ReadableMap to be conveted.
     * @return A HashMap containing the data that was in the ReadableMap.
     * @see 'Adapted from https://github.com/artemyarulin/react-native-eval/blob/master/android/src/main/java/com/evaluator/react/ConversionUtil.java'
     */
    public static Map<String, String> toStringMap(@Nullable ReadableMap readableMap) {
        if (readableMap == null)
            return null;

        com.facebook.react.bridge.ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        if (!iterator.hasNextKey())
            return null;

        Map<String, String> result = new HashMap<>();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            result.put(key, readableMap.getString(key));
        }

        return result;
    }
}
