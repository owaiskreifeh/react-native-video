package com.brentvatne.exoplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
//import android.view.ViewGroup;
import android.view.Window;
import android.view.accessibility.CaptioningManager;
import android.widget.FrameLayout;

import com.brentvatne.react.R;
import com.brentvatne.receiver.AudioBecomingNoisyReceiver;
import com.brentvatne.receiver.BecomingNoisyListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Dynamic;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter;
import com.npaw.youbora.lib6.plugin.Options;
import com.npaw.youbora.lib6.plugin.Plugin;

import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.CuePoint;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.StreamDisplayContainer;
import com.google.ads.interactivemedia.v3.api.StreamManager;
import com.google.ads.interactivemedia.v3.api.StreamRequest;
import com.google.ads.interactivemedia.v3.api.AdProgressInfo;
import com.google.ads.interactivemedia.v3.api.StreamRequest.StreamFormat;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressLint("ViewConstructor")
class ReactExoplayerView extends FrameLayout implements LifecycleEventListener, Player.EventListener,
        BandwidthMeter.EventListener, BecomingNoisyListener, AudioManager.OnAudioFocusChangeListener, MetadataOutput,
        AdEvent.AdEventListener, AdErrorEvent.AdErrorListener, AdsLoader.AdsLoadedListener {

//    private static final String TAG = "ReactExoplayerView";

    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    private static final int SHOW_PROGRESS = 1;

    private static SimpleExoPlayer lastPlayerinstance;
    private static ExoPlayerView lastExoPlayerView;
    private boolean killLastInstance;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private final VideoEventEmitter eventEmitter;
    private final ReactExoplayerConfig config;
    private final DefaultBandwidthMeter bandwidthMeter;
//    private View playPauseControlContainer;
//    private Player.EventListener eventListener;

    private ExoPlayerView exoPlayerView;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private boolean playerNeedsSource;

    private int resumeWindow;
    private long resumePosition;
    private boolean loadVideoStarted;
    private boolean isFullscreen;
    private boolean isInBackground;
    private boolean isPaused;
    private boolean isBuffering;
    private boolean muted = false;
    private float rate = 1f;
    private float audioVolume = 1f;
    private int minLoadRetryCount = 3;
    private int maxBitRate = 0;
    private long seekTime = C.TIME_UNSET;

    private int minBufferMs = DefaultLoadControl.DEFAULT_MIN_BUFFER_MS;
    private int maxBufferMs = DefaultLoadControl.DEFAULT_MAX_BUFFER_MS;
    private int bufferForPlaybackMs = DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS;
    private int bufferForPlaybackAfterRebufferMs = DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS;

    // DRM Integration
    private FrameworkMediaDrm mediaDrm;
    private String drmLicenseUrl;

    private String userAgent;

    // Youbora Integration
    private Plugin youboraPlugin;

    // IMA DAI
    private ImaSdkFactory mSdkFactory;
    private AdsLoader mAdsLoader;
    private StreamDisplayContainer mDisplayContainer;
    private StreamManager mStreamManager;
    private List<VideoStreamPlayer.VideoStreamPlayerCallback> mPlayerCallbacks;
    private long mSnapBackTime = C.TIME_UNSET;
    private CuePoint[] playedAds;
    private boolean adsPlaying = false;
    private String language = "ar";

    // Props from React
    private Uri srcUri;
    private String adsId;
    private String extension;
    private boolean repeat;
    private String audioTrackType;
    private Dynamic audioTrackValue;
    private String videoTrackType;
    private Dynamic videoTrackValue;
    private String textTrackType;
    private Dynamic textTrackValue;
    private ReadableArray textTracks;
    private boolean disableFocus;
    private long mProgressUpdateInterval = 250;
    private boolean playInBackground = false;
    private Map<String, String> requestHeaders;
    private boolean mReportBandwidth = false;
    // \ End props

    // React
    private final ThemedReactContext themedReactContext;
    private final AudioManager audioManager;
    private final AudioBecomingNoisyReceiver audioBecomingNoisyReceiver;

    @SuppressLint("HandlerLeak")
    private final Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == SHOW_PROGRESS && player != null && player.getPlaybackState() == Player.STATE_READY
                    && player.getPlayWhenReady()) {

                if (mStreamManager == null) {
                    eventEmitter.progressChanged(
                            /* Current Position */
                            player.getCurrentPosition() / 1000.0,
                            /* Buffered Duration */
                            player.getBufferedPercentage() * player.getDuration() / 100000.0,
                            /* Duration */
                            player.getDuration() / 1000.0);
                } else {
                    if (adsPlaying) {
                        AdProgressInfo ad = mStreamManager.getAdProgressInfo();
                        if (ad != null) {
                            WritableMap eventData = Arguments.createMap();
                            eventData.putString("progress",
                                    (int) Math.round(ad.getCurrentTime() / ad.getDuration() * 100) + "%");
                            eventData.putInt("remainingTime",
                                    Math.max((int) Math.ceil(ad.getDuration() - ad.getCurrentTime()), 0));
                            eventData.putInt("currentPosition", ad.getAdPosition());
                            eventData.putInt("totalCount", ad.getTotalAds());
                            eventEmitter.adEvent("AdProgress", eventData);
                        }
                    } else {
                        double currentSeconds = player.getCurrentPosition() / 1000.0;
                        skipPlayedAds(currentSeconds);

                        eventEmitter.progressChanged(
                                /* Current Position */
                                mStreamManager.getContentTimeForStreamTime(player.getCurrentPosition() / 1000.0),
                                /* Buffered Duration */
                                mStreamManager.getContentTimeForStreamTime(
                                        player.getBufferedPercentage() * player.getDuration() / 100000.0),
                                /* Duration */
                                mStreamManager.getContentTimeForStreamTime(player.getDuration() / 1000.0));
                    }
                }

                msg = obtainMessage(SHOW_PROGRESS);
                sendMessageDelayed(msg, mProgressUpdateInterval);
            }
        }
    };

    public ReactExoplayerView(ThemedReactContext context, ReactExoplayerConfig config) {
        super(context);

        this.themedReactContext = context;
        this.eventEmitter = new VideoEventEmitter(context);
        this.config = config;
        this.bandwidthMeter = config.getBandwidthMeter();
        this.userAgent = Util.getUserAgent(context, "SHAHID");

        createViews();

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        themedReactContext.addLifecycleEventListener(this);
        audioBecomingNoisyReceiver = new AudioBecomingNoisyReceiver(themedReactContext);

        initializePlayer();
    }

    private StreamRequest buildStreamRequest() {
        // VOD HLS request.
        String[] ids = adsId.split("_,_");
        StreamRequest request = mSdkFactory.createVodStreamRequest(ids[0], ids[1], null);
        Map adTagParameters = new HashMap();
        adTagParameters.put("cust_params", "shahid_localization=" + this.language);
        request.setAdTagParameters(adTagParameters);
        request.setFormat(StreamFormat.HLS);
        return request;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        eventEmitter.setViewId(id);
    }

    private void createViews() {
        clearResumePosition();
        mediaDataSourceFactory = buildDataSourceFactory(true);
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        exoPlayerView = new ExoPlayerView(getContext());
        exoPlayerView.setLayoutParams(layoutParams);

        addView(exoPlayerView, 0, layoutParams);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initializePlayer();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        /*
         * We want to be able to continue playing audio when switching tabs. Leave this
         * here in case it causes issues.
         */
        // stopPlayback();
    }

    // React LifecycleEventListener implementation
    @Override
    public void onHostResume() {
        if (!playInBackground || !isInBackground) {
            setPlayWhenReady(!isPaused);
        }
        isInBackground = false;
    }

    @Override
    public void onHostPause() {
        isInBackground = true;
        if (playInBackground) {
            return;
        }
        setPlayWhenReady(false);
    }

    @Override
    public void onHostDestroy() {
        stopPlayback();
        releaseMediaDrm();
        releaseAds();
    }

    public void cleanUpResources() {
        stopPlayback();
    }

    // BandwidthMeter.EventListener implementation
    @Override
    public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {
        if (mReportBandwidth) {
            eventEmitter.bandwidthReport(bitrate);
        }
    }

    // Internal methods

    private void releaseMediaDrm() {
        if (mediaDrm != null) {
            mediaDrm.release();
            mediaDrm = null;
        }
    }

    /**
     * Returns a {@link HttpDataSource.Factory}.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(userAgent, null);
    }

    private DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(String licenseUrl)
            throws UnsupportedDrmException {

        HttpDataSource.Factory licenseDataSourceFactory = buildHttpDataSourceFactory();
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, true, licenseDataSourceFactory);

        releaseMediaDrm();
        mediaDrm = FrameworkMediaDrm.newInstance(C.WIDEVINE_UUID);
        return new DefaultDrmSessionManager<>(C.WIDEVINE_UUID, mediaDrm, drmCallback, null, true);
    }

    private void initializePlayer() {
        ReactExoplayerView self = this;
        // This ensures all props have been settled, to avoid async racing conditions.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player == null) {

                    DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager;
                    try {
                        drmSessionManager = buildDrmSessionManagerV18(drmLicenseUrl);
                    } catch (UnsupportedDrmException e) {
                        // Log.d(TAG, "Unsupported drm exception");
                        drmSessionManager = null;
                    }

                    TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
                    trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
                    trackSelector.setParameters(trackSelector.buildUponParameters()
                            .setMaxVideoBitrate(maxBitRate == 0 ? Integer.MAX_VALUE : maxBitRate));

                    DefaultAllocator allocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);
                    DefaultLoadControl.Builder defaultLoadControlBuilder = new DefaultLoadControl.Builder();
                    defaultLoadControlBuilder.setAllocator(allocator);
                    defaultLoadControlBuilder.setBufferDurationsMs(minBufferMs, maxBufferMs, bufferForPlaybackMs,
                            bufferForPlaybackAfterRebufferMs);
                    defaultLoadControlBuilder.setTargetBufferBytes(-1);
                    defaultLoadControlBuilder.setPrioritizeTimeOverSizeThresholds(true);
                    DefaultLoadControl defaultLoadControl = defaultLoadControlBuilder.createDefaultLoadControl();
                    DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(getContext())
                            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
                    // TODO: Add drmSessionManager to 5th param from:
                    // https://github.com/react-native-community/react-native-video/pull/1445

                    if (killLastInstance && lastPlayerinstance != null){
                        lastPlayerinstance.release();
                        lastExoPlayerView.removeAllViews();
                    }

                    player = ExoPlayerFactory.newSimpleInstance(getContext(), renderersFactory, trackSelector,
                            defaultLoadControl, drmSessionManager, bandwidthMeter);
                    lastPlayerinstance = player;
                    lastExoPlayerView = exoPlayerView;
                    player.addListener(self);
                    player.addMetadataOutput(self);
                    exoPlayerView.setPlayer(player);

                    // Once the player is created, attach the Youbora Adapter to the plugin
                    if (youboraPlugin != null) {
                        Exoplayer2Adapter adapter = new Exoplayer2Adapter(player);
                        adapter.setCustomEventLogger(trackSelector);
                        youboraPlugin.setAdapter(adapter);
                    }
                    audioBecomingNoisyReceiver.setListener(self);
                    bandwidthMeter.addEventListener(new Handler(), self);
                    setPlayWhenReady(!isPaused);
                    playerNeedsSource = true;

                    PlaybackParameters params = new PlaybackParameters(rate, 1f);
                    player.setPlaybackParameters(params);
                }

                if (playerNeedsSource && srcUri != null) {
                    if (adsId == null) {
                        loadSource(srcUri, extension);
                        eventEmitter.loadStart();
                        loadVideoStarted = true;
                    } else {
                        if (mSdkFactory == null) {
                            initializeAds();
                        }
                        mAdsLoader.requestStream(buildStreamRequest());
                    }
                }

                applyModifiers();
            }
        }, 1);
    }

    private void loadSource(Uri uri, String extension) {
        ArrayList<MediaSource> mediaSourceList = buildTextSources();
        MediaSource videoSource = buildMediaSource(uri, extension);
        MediaSource mediaSource;
        if (mediaSourceList.size() == 0) {
            mediaSource = videoSource;
        } else {
            mediaSourceList.add(0, videoSource);
            MediaSource[] textSourceArray = mediaSourceList.toArray(new MediaSource[mediaSourceList.size()]);
            mediaSource = new MergingMediaSource(textSourceArray);
        }

        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            player.seekTo(resumeWindow, resumePosition);
        }
        player.prepare(mediaSource, !haveResumePosition, false);
        playerNeedsSource = false;
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = Util.inferContentType(
                !TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension : uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setLoadErrorHandlingPolicy(config.buildLoadErrorHandlingPolicy(minLoadRetryCount))
                        .createMediaSource(uri);
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setLoadErrorHandlingPolicy(config.buildLoadErrorHandlingPolicy(minLoadRetryCount))
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .setLoadErrorHandlingPolicy(config.buildLoadErrorHandlingPolicy(minLoadRetryCount))
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(mediaDataSourceFactory)
                        .setLoadErrorHandlingPolicy(config.buildLoadErrorHandlingPolicy(minLoadRetryCount))
                        .createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private ArrayList<MediaSource> buildTextSources() {
        ArrayList<MediaSource> textSources = new ArrayList<>();
        if (textTracks == null) {
            return textSources;
        }

        for (int i = 0; i < textTracks.size(); ++i) {
            ReadableMap textTrack = textTracks.getMap(i);
            String language = textTrack.getString("language");
            String title = textTrack.hasKey("title") ? textTrack.getString("title") : language + " " + i;
            Uri uri = Uri.parse(textTrack.getString("uri"));
            MediaSource textSource = buildTextSource(title, uri, textTrack.getString("type"), language);
            if (textSource != null) {
                textSources.add(textSource);
            }
        }
        return textSources;
    }

    private MediaSource buildTextSource(String title, Uri uri, String mimeType, String language) {
        Format textFormat = Format.createTextSampleFormat(title, mimeType, Format.NO_VALUE, language);
        return new SingleSampleMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri, textFormat,
                C.TIME_UNSET);
    }

    private void releasePlayer() {
        if (player != null) {
            updateResumePosition();
            player.release();
            player.removeMetadataOutput(this);
            trackSelector = null;
            player = null;
        }
        clearProgressMessageHandler();
        themedReactContext.removeLifecycleEventListener(this);
        audioBecomingNoisyReceiver.removeListener();
        bandwidthMeter.removeEventListener(this);

        if (youboraPlugin != null) {
            youboraPlugin.removeAdapter();
        }

        releaseAds();
    }

    private void releaseAds() {
        if (mAdsLoader != null) {
            mAdsLoader.removeAdErrorListener(this);
            mAdsLoader.removeAdsLoadedListener(this);
            this.mAdsLoader = null;
        }

        if (mStreamManager != null) {
            mStreamManager.removeAdErrorListener(this);
            mStreamManager.removeAdEventListener(this);
            mStreamManager.destroy();
            this.mStreamManager = null;
        }

        if (mDisplayContainer != null) {
            mDisplayContainer.destroy();
            this.mDisplayContainer = null;
        }
    }

    private boolean requestAudioFocus() {
        if (disableFocus || srcUri == null) {
            return true;
        }
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void setPlayWhenReady(boolean playWhenReady) {
        if (player == null) {
            return;
        }

        if (playWhenReady) {
            boolean hasAudioFocus = requestAudioFocus();
            if (hasAudioFocus) {
                player.setPlayWhenReady(true);
            }
            if (!disableFocus) {
                setKeepScreenOn(true);
            }
        } else {
            player.setPlayWhenReady(false);
            setKeepScreenOn(false);
        }
    }

    private void startPlayback() {
        if (player != null) {
            switch (player.getPlaybackState()) {
                case Player.STATE_IDLE:
                case Player.STATE_ENDED:
                    initializePlayer();
                    break;
                case Player.STATE_BUFFERING:
                case Player.STATE_READY:
                    if (!player.getPlayWhenReady()) {
                        setPlayWhenReady(true);
                    }
                    break;
                default:
                    break;
            }

        } else {
            initializePlayer();
        }
    }

    private void pausePlayback() {
        if (player != null) {
            if (player.getPlayWhenReady()) {
                setPlayWhenReady(false);
            }
        }
    }

    private void stopPlayback() {
        onStopPlayback();
        releasePlayer();
    }

    private void onStopPlayback() {
        if (isFullscreen) {
            setFullscreen(false);
        }
        setKeepScreenOn(false);
        audioManager.abandonAudioFocus(this);
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition()) : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #bandwidthMeter} as a listener
     *                          to the new DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return DataSourceUtil.getDefaultDataSourceFactory(this.themedReactContext,
                useBandwidthMeter ? bandwidthMeter : null, requestHeaders);
    }

    // AudioManager.OnAudioFocusChangeListener implementation
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                eventEmitter.audioFocusChanged(false);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                eventEmitter.audioFocusChanged(true);
                break;
            default:
                break;
        }

        if (player != null) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // Lower the volume
                if (!muted) {
                    player.setVolume(audioVolume * 0.8f);
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Raise it back to normal
                if (!muted) {
                    player.setVolume(audioVolume * 1);
                }
            }
        }
    }

    // AudioBecomingNoisyListener implementation
    @Override
    public void onAudioBecomingNoisy() {
        eventEmitter.audioBecomingNoisy();
    }

    // Player.EventListener implementation
    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_IDLE:
                eventEmitter.idle();
                clearProgressMessageHandler();
                break;
            case Player.STATE_BUFFERING:
                onBuffering(true);
                clearProgressMessageHandler();
                break;
            case Player.STATE_READY:
                eventEmitter.ready();
                onBuffering(false);
                startProgressHandler();
                videoLoaded();
                break;
            case Player.STATE_ENDED:
                eventEmitter.end();
                onStopPlayback();
                break;
        }
    }

    private void startProgressHandler() {
        progressHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    /*
     * The progress message handler will duplicate recursions of the
     * onProgressMessage handler on change of player state from any state to
     * STATE_READY with playWhenReady is true (when the video is not paused). This
     * clears all existing messages.
     */
    private void clearProgressMessageHandler() {
        progressHandler.removeMessages(SHOW_PROGRESS);
    }

    private void videoLoaded() {
        if (loadVideoStarted) {
            loadVideoStarted = false;
            setSelectedAudioTrack(audioTrackType, audioTrackValue);
            setSelectedVideoTrack(videoTrackType, videoTrackValue);
            setSelectedTextTrack(textTrackType, textTrackValue);
            Format videoFormat = player.getVideoFormat();
            int width = videoFormat != null ? videoFormat.width : 0;
            int height = videoFormat != null ? videoFormat.height : 0;

            if (mStreamManager == null) {
                eventEmitter.load(player.getDuration(), player.getCurrentPosition(), width, height, getAudioTrackInfo(),
                        getTextTrackInfo(), getVideoTrackInfo());
            } else {
                eventEmitter.load(mStreamManager.getContentTimeForStreamTime(player.getDuration()),
                        mStreamManager.getContentTimeForStreamTime(player.getCurrentPosition()), width, height,
                        getAudioTrackInfo(), getTextTrackInfo(), getVideoTrackInfo());

                setImaCuePoints();
            }
        }
    }

    private WritableArray getAudioTrackInfo() {
        WritableArray audioTracks = Arguments.createArray();

        MappingTrackSelector.MappedTrackInfo info = trackSelector.getCurrentMappedTrackInfo();
        int index = getTrackRendererIndex(C.TRACK_TYPE_AUDIO);
        if (info == null || index == C.INDEX_UNSET) {
            return audioTracks;
        }

        TrackGroupArray groups = info.getTrackGroups(index);
        for (int i = 0; i < groups.length; ++i) {
            Format format = groups.get(i).getFormat(0);
            WritableMap audioTrack = Arguments.createMap();
            audioTrack.putInt("index", i);
            audioTrack.putString("title", format.id != null ? format.id : "");
            audioTrack.putString("label", format.label != null ? format.label : "");
            audioTrack.putString("type", format.sampleMimeType);
            audioTrack.putString("language", format.language != null ? format.language : "");
            audioTrack.putString("bitrate", format.bitrate == Format.NO_VALUE ? ""
                    : String.format(Locale.US, "%.2fMbps", format.bitrate / 1000000f));
            audioTracks.pushMap(audioTrack);
        }
        return audioTracks;
    }

    private WritableArray getVideoTrackInfo() {
        WritableArray videoTracks = Arguments.createArray();

        MappingTrackSelector.MappedTrackInfo info = trackSelector.getCurrentMappedTrackInfo();
        int index = getTrackRendererIndex(C.TRACK_TYPE_VIDEO);
        if (info == null || index == C.INDEX_UNSET) {
            return videoTracks;
        }

        TrackGroupArray groups = info.getTrackGroups(index);
        for (int i = 0; i < groups.length; ++i) {
            TrackGroup group = groups.get(i);

            for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                Format format = group.getFormat(trackIndex);
                WritableMap videoTrack = Arguments.createMap();
                videoTrack.putInt("width", format.width == Format.NO_VALUE ? 0 : format.width);
                videoTrack.putInt("height", format.height == Format.NO_VALUE ? 0 : format.height);
                videoTrack.putInt("bitrate", format.bitrate == Format.NO_VALUE ? 0 : format.bitrate);
                videoTrack.putString("codecs", format.codecs != null ? format.codecs : "");
                videoTrack.putString("trackId", format.id == null ? String.valueOf(trackIndex) : format.id);
                videoTracks.pushMap(videoTrack);
            }
        }
        return videoTracks;
    }

    private WritableArray getTextTrackInfo() {
        WritableArray textTracks = Arguments.createArray();

        MappingTrackSelector.MappedTrackInfo info = trackSelector.getCurrentMappedTrackInfo();
        int index = getTrackRendererIndex(C.TRACK_TYPE_TEXT);
        if (info == null || index == C.INDEX_UNSET) {
            return textTracks;
        }

        TrackGroupArray groups = info.getTrackGroups(index);
        for (int i = 0; i < groups.length; ++i) {
            Format format = groups.get(i).getFormat(0);
            WritableMap textTrack = Arguments.createMap();
            textTrack.putInt("index", i);
            textTrack.putString("title", format.id != null ? format.id : "");
            textTrack.putString("label", format.label != null ? format.label : "");
            textTrack.putString("type", format.sampleMimeType);
            textTrack.putString("language", format.language != null ? format.language : "");
            textTracks.pushMap(textTrack);
        }
        return textTracks;
    }

    private void onBuffering(boolean buffering) {
        if (isBuffering == buffering) {
            return;
        }

        isBuffering = buffering;
        if (buffering) {
            eventEmitter.buffering(true);
        } else {
            eventEmitter.buffering(false);
        }
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        if (playerNeedsSource) {
            // This will only occur if the user has performed a seek whilst in the error
            // state. Update the
            // resume position so that if the user then retries, playback will resume from
            // the position to
            // which they seeked.
            updateResumePosition();
        }
        // When repeat is turned on, reaching the end of the video will not cause a
        // state change
        // so we need to explicitly detect it.
        if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION
                && player.getRepeatMode() == Player.REPEAT_MODE_ONE) {
            eventEmitter.end();
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
        // Do nothing.
    }

    @Override
    public void onSeekProcessed() {
        eventEmitter.seek(player.getCurrentPosition(), seekTime);
        seekTime = C.TIME_UNSET;
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        // Do nothing.
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        // Do nothing.
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        // Do Nothing.
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters params) {
        eventEmitter.playbackRateChange(params.speed);
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        String errorString = null;
        Exception ex = e;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                // Special case for decoder initialization failures.
                MediaCodecRenderer.DecoderInitializationException decoderInitializationException = (MediaCodecRenderer.DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                        errorString = getResources().getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getResources().getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = getResources().getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getResources().getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
                }
            }
        } else if (e.type == ExoPlaybackException.TYPE_SOURCE) {
            ex = e.getSourceException();
            errorString = getResources().getString(R.string.unrecognized_media_format);
        }
        if (errorString != null) {
            eventEmitter.error(errorString, ex);
        } else {
            eventEmitter.error(e.getMessage(), ex);
        }
        playerNeedsSource = true;
        if (isBehindLiveWindow(e)) {
            clearResumePosition();
            initializePlayer();
        } else {
            updateResumePosition();
        }
    }

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

    public int getTrackRendererIndex(int trackType) {
        int rendererCount = player.getRendererCount();
        for (int rendererIndex = 0; rendererIndex < rendererCount; rendererIndex++) {
            if (player.getRendererType(rendererIndex) == trackType) {
                return rendererIndex;
            }
        }
        return C.INDEX_UNSET;
    }

    @Override
    public void onMetadata(Metadata metadata) {
        eventEmitter.timedMetadata(metadata);
    }

    private void reloadSource() {
        playerNeedsSource = true;
        if (player != null) {
            initializePlayer();
        }
    }

    private int getGroupIndexForDefaultLocale(TrackGroupArray groups) {
        if (groups.length == 0) {
            return C.INDEX_UNSET;
        }

        int groupIndex = 0; // default if no match
        String locale2 = Locale.getDefault().getLanguage(); // 2 letter code
        String locale3 = Locale.getDefault().getISO3Language(); // 3 letter code
        for (int i = 0; i < groups.length; ++i) {
            Format format = groups.get(i).getFormat(0);
            String language = format.language;
            if (language != null && (language.equals(locale2) || language.equals(locale3))) {
                groupIndex = i;
                break;
            }
        }
        return groupIndex;
    }

    public void seekTo(long positionMs) {
        if (player != null) {
            double seconds = positionMs / 1000.0;

            if (adsPlaying) {
                seconds = mStreamManager.getStreamTimeForContentTime(seconds);
                mSnapBackTime = (long) seconds;
            } else {
                if (mStreamManager != null) {
                    seconds = mStreamManager.getStreamTimeForContentTime(seconds);
                    CuePoint cuePoint = mStreamManager.getPreviousCuePointForStreamTime(seconds);
                    if (cuePoint != null && !cuePoint.isPlayed()) {
                        mSnapBackTime = (long) seconds;
                        // Missed cue point, so snap back to the beginning of cue point.
                        seconds = cuePoint.getStartTime();
                    }
                }

                this.seekTime = (long) seconds * 1000;
                player.seekTo(seekTime);
            }
        }
    }

    // #region Ads
    private void initializeAds() {
        this.mSdkFactory = ImaSdkFactory.getInstance();
        this.mPlayerCallbacks = new ArrayList<>();
        ImaSdkSettings settings = mSdkFactory.createImaSdkSettings();
        VideoStreamPlayer videoStreamPlayer = createVideoStreamPlayer();
        this.mDisplayContainer = ImaSdkFactory.createStreamDisplayContainer(new FrameLayout(themedReactContext),
                videoStreamPlayer);
        this.mAdsLoader = mSdkFactory.createAdsLoader(themedReactContext, settings, mDisplayContainer);
        mAdsLoader.addAdErrorListener(this);
        mAdsLoader.addAdsLoadedListener(this);
    }

    private VideoStreamPlayer createVideoStreamPlayer() {
        return new VideoStreamPlayer() {
            @Override
            public void loadUrl(String url, List<HashMap<String, String>> subtitles) {
                if (player != null) {
                    loadSource(Uri.parse(url), null);
                    eventEmitter.loadStart();
                    loadVideoStarted = true;
                }
            }

            @Override
            public void pause() {
                setPausedModifier(true);
            }

            @Override
            public void resume() {
                setPausedModifier(false);
            }

            @Override
            public int getVolume() {
                return 100;
            }

            @Override
            public void addCallback(VideoStreamPlayerCallback videoStreamPlayerCallback) {
                mPlayerCallbacks.add(videoStreamPlayerCallback);
            }

            @Override
            public void removeCallback(VideoStreamPlayerCallback videoStreamPlayerCallback) {
                mPlayerCallbacks.remove(videoStreamPlayerCallback);
            }

            @Override
            public void onAdBreakStarted() {
                adsPlaying = true;

                eventEmitter.adEvent("AdBreakStarted", null);
            }

            @Override
            public void onAdBreakEnded() {
                adsPlaying = false;
                if (mSnapBackTime != C.TIME_UNSET) {
                    seekTime = (long) mSnapBackTime * 1000;
                    player.seekTo(seekTime);
                    mSnapBackTime = C.TIME_UNSET;
                }

                eventEmitter.adEvent("AdBreakEnded", null);
            }

            @Override
            public void onAdPeriodStarted() {

            }

            @Override
            public void onAdPeriodEnded() {

            }

            @Override
            public void seek(long timeMs) {
                // An ad was skipped. Skip to the content time.
                seekTime = timeMs * 1000;
                player.seekTo(seekTime);
            }

            @Override
            public VideoProgressUpdate getContentProgress() {
                if (player != null) {
                    return new VideoProgressUpdate(player.getCurrentPosition(), player.getDuration());
                }
                return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
            }
        };
    }

    private void skipPlayedAds(double currentSeconds) {
        if (playedAds != null) {
            double nextItirationSeconds = currentSeconds + (mProgressUpdateInterval / 1000.0);
            CuePoint cuePoint;
            for (int i = playedAds.length - 1; i >= 0; i--) {
                cuePoint = playedAds[i];
                if (cuePoint.getEndTime() < nextItirationSeconds) {
                    break;
                } else if (cuePoint.getStartTime() <= nextItirationSeconds) {
                    seekTime = ((long) cuePoint.getEndTime() * 1000) + 100;
                    player.seekTo(seekTime);
                    break;
                }
            }
        }
    }

    /**
     * AdErrorListener implementation
     */
    @Override
    public void onAdError(AdErrorEvent event) {
        // log(String.format("Error: %s\n", event.getError().getMessage()));
        if (!loadVideoStarted && player != null) {
            loadSource(srcUri, extension);
            eventEmitter.loadStart();
            loadVideoStarted = true;
        }
    }

    private void setImaCuePoints() {
        List<CuePoint> cuePoints = mStreamManager.getCuePoints();
        ArrayList<CuePoint> tmpPlayedAds = new ArrayList<CuePoint>();

        if (playedAds == null) {
            if (player != null && player.getDuration() > 0 && cuePoints != null && !cuePoints.isEmpty()) {
                // set 1st time value
                WritableArray writableArrayOfCuePoints = Arguments.createArray();
                double duration = mStreamManager.getContentTimeForStreamTime(player.getDuration() / 1000.0);
                for (CuePoint cuePoint : cuePoints) {
                    double adTime = mStreamManager.getContentTimeForStreamTime(cuePoint.getStartTime());
                    writableArrayOfCuePoints.pushDouble(adTime / duration * 100);

                    if (cuePoint.isPlayed()) {
                        tmpPlayedAds.add(cuePoint);
                    }
                }

                this.playedAds = tmpPlayedAds.toArray(new CuePoint[tmpPlayedAds.size()]);

                WritableMap eventData = Arguments.createMap();
                eventData.putArray("cuePoints", writableArrayOfCuePoints);
                eventEmitter.adEvent("CuePointsChange", eventData);
            }
        } else {
            // update if changed 'isPlayed'
            if (cuePoints != null) {
                for (CuePoint cuePoint : cuePoints) {
                    if (cuePoint.isPlayed()) {
                        tmpPlayedAds.add(cuePoint);
                    }
                }
            }
            this.playedAds = tmpPlayedAds.toArray(new CuePoint[tmpPlayedAds.size()]);
        }
    }


    public void setFontSizeTrack(int fontSizeTrack) {
        exoPlayerView.setFontSizeTrack(fontSizeTrack);
    }

    public void setPaddingBottomTrack(float paddingBottomTrack) {
        exoPlayerView.setPaddingBottomTrack(paddingBottomTrack);
    }

    /**
     * AdEventListener implementation
     */
    @Override
    public void onAdEvent(AdEvent event) {
        switch (event.getType()) {
            // case AD_PROGRESS:  break;
            //case AD_BREAK_STARTED: break;
            //case AD_BREAK_ENDED:  break;
            // case AD_PERIOD_STARTED: break;
            // case AD_PERIOD_ENDED: break;
            case CUEPOINTS_CHANGED:
                setImaCuePoints();
                break;
        }
    }

    /**
     * AdsLoadedListener implementation
     */
    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
        if (this.mStreamManager == null) {
            this.mStreamManager = event.getStreamManager();
            mStreamManager.addAdErrorListener(this);
            mStreamManager.addAdEventListener(this);
            mStreamManager.init();
        }
    }
    // #endregion

    // #region react props
    public void setSrc(final Uri uri, final String extension, Map<String, String> headers, String ads) {
        if (uri != null) {
            boolean shouldReload = !uri.equals(srcUri) || (ads != null && ads != this.adsId);

            this.srcUri = uri;
            this.adsId = ads;
            this.extension = extension;
            this.requestHeaders = headers;
            this.mediaDataSourceFactory = DataSourceUtil.getDefaultDataSourceFactory(this.themedReactContext,
                    bandwidthMeter, this.requestHeaders);

            if (shouldReload) {
                reloadSource();
            }
        }
    }

    public void setProgressUpdateInterval(final float progressUpdateInterval) {
        mProgressUpdateInterval = Math.round(progressUpdateInterval);
    }

    public void setReportBandwidth(boolean reportBandwidth) {
        mReportBandwidth = reportBandwidth;
    }

    public void setRawSrc(final Uri uri, final String extension) {
        if (uri != null) {
            boolean isOriginalSourceNull = srcUri == null;
            boolean isSourceEqual = uri.equals(srcUri);

            this.srcUri = uri;
            this.extension = extension;
            this.mediaDataSourceFactory = buildDataSourceFactory(true);

            if (!isOriginalSourceNull && !isSourceEqual) {
                reloadSource();
            }
        }
    }

    public void setTextTracks(ReadableArray textTracks) {
        this.textTracks = textTracks;
        reloadSource();
    }

    public void setResizeModeModifier(@ResizeMode.Mode int resizeMode) {
        exoPlayerView.setResizeMode(resizeMode);
    }

    private void applyModifiers() {
        setRepeatModifier(repeat);
        setMutedModifier(muted);
    }

    public void setRepeatModifier(boolean repeat) {
        if (player != null) {
            if (repeat) {
                player.setRepeatMode(Player.REPEAT_MODE_ONE);
            } else {
                player.setRepeatMode(Player.REPEAT_MODE_OFF);
            }
        }
        this.repeat = repeat;
    }

    public void setSelectedTrack(int trackType, String type, Dynamic value) {
        if (player == null)
            return;
        int rendererIndex = getTrackRendererIndex(trackType);
        if (rendererIndex == C.INDEX_UNSET) {
            return;
        }
        MappingTrackSelector.MappedTrackInfo info = trackSelector.getCurrentMappedTrackInfo();
        if (info == null) {
            return;
        }

        TrackGroupArray groups = info.getTrackGroups(rendererIndex);
        int groupIndex = C.INDEX_UNSET;
        int[] tracks = {0};

        if (TextUtils.isEmpty(type)) {
            type = "default";
        }

        DefaultTrackSelector.Parameters disableParameters = trackSelector.getParameters().buildUpon()
                .setRendererDisabled(rendererIndex, true).build();

        if (type.equals("disabled")) {
            trackSelector.setParameters(disableParameters);
            return;
        } else if (type.equals("language")) {
            for (int i = 0; i < groups.length; ++i) {
                Format format = groups.get(i).getFormat(0);
                if (format.language != null && format.language.equals(value.asString())) {
                    groupIndex = i;
                    break;
                }
            }
        } else if (type.equals("title")) {
            for (int i = 0; i < groups.length; ++i) {
                Format format = groups.get(i).getFormat(0);
                if (format.id != null && format.id.equals(value.asString())) {
                    groupIndex = i;
                    break;
                }
            }
        } else if (type.equals("index")) {
            if (value.asInt() < groups.length) {
                groupIndex = value.asInt();
            }
        } else if (type.equals("resolution")) {
            int height = value.asInt();
            for (int i = 0; i < groups.length; ++i) { // Search for the exact height
                TrackGroup group = groups.get(i);
                for (int j = 0; j < group.length; j++) {
                    Format format = group.getFormat(j);
                    if (format.height == height) {
                        groupIndex = i;
                        tracks[0] = j;
                        break;
                    }
                }
            }
        } else if (rendererIndex == C.TRACK_TYPE_TEXT && Util.SDK_INT > 18) { // Text default
            // Use system settings if possible
            CaptioningManager captioningManager = (CaptioningManager) themedReactContext
                    .getSystemService(Context.CAPTIONING_SERVICE);
            if (captioningManager != null && captioningManager.isEnabled()) {
                groupIndex = getGroupIndexForDefaultLocale(groups);
            }
        } else if (rendererIndex == C.TRACK_TYPE_AUDIO) { // Audio default
            groupIndex = getGroupIndexForDefaultLocale(groups);
        }

        if (groupIndex == C.INDEX_UNSET && trackType == C.TRACK_TYPE_VIDEO && groups.length != 0) { // Video auto
            // Add all tracks as valid options for ABR to choose from
            TrackGroup group = groups.get(0);
            tracks = new int[group.length];
            groupIndex = 0;
            for (int j = 0; j < group.length; j++) {
                tracks[j] = j;
            }
        }

        if (groupIndex == C.INDEX_UNSET) {
            trackSelector.setParameters(disableParameters);
            return;
        }

        DefaultTrackSelector.Parameters selectionParameters = trackSelector.getParameters().buildUpon()
                .setRendererDisabled(rendererIndex, false).setSelectionOverride(rendererIndex, groups,
                        new DefaultTrackSelector.SelectionOverride(groupIndex, tracks))
                .build();
        trackSelector.setParameters(selectionParameters);
    }

    public void setSelectedVideoTrack(String type, Dynamic value) {
        videoTrackType = type;
        videoTrackValue = value;
        setSelectedTrack(C.TRACK_TYPE_VIDEO, videoTrackType, videoTrackValue);
    }

    public void setSelectedAudioTrack(String type, Dynamic value) {
        audioTrackType = type;
        audioTrackValue = value;
        setSelectedTrack(C.TRACK_TYPE_AUDIO, audioTrackType, audioTrackValue);
    }

    public void setSelectedTextTrack(String type, Dynamic value) {
        textTrackType = type;
        textTrackValue = value;
        setSelectedTrack(C.TRACK_TYPE_TEXT, textTrackType, textTrackValue);
    }

    public void setLanguage(String lang) {
        this.language = lang;
    }

    public void setKillLastInstance(boolean kill) {this.killLastInstance = kill;}

    public void setPausedModifier(boolean paused) {
        isPaused = paused;
        if (player != null) {
            if (!paused) {
                startPlayback();
            } else {
                pausePlayback();
            }
        }
    }

    public void setMutedModifier(boolean muted) {
        this.muted = muted;
        audioVolume = muted ? 0.f : 1.f;
        if (player != null) {
            player.setVolume(audioVolume);
        }
    }

    public void setVolumeModifier(float volume) {
        audioVolume = volume;
        if (player != null) {
            player.setVolume(audioVolume);
        }
    }

    public void setRateModifier(float newRate) {
        rate = newRate;

        if (player != null) {
            PlaybackParameters params = new PlaybackParameters(rate, 1f);
            player.setPlaybackParameters(params);
        }
    }

    public void setMaxBitRateModifier(int newMaxBitRate) {
        maxBitRate = newMaxBitRate;
        if (player != null) {
            trackSelector.setParameters(trackSelector.buildUponParameters()
                    .setMaxVideoBitrate(maxBitRate == 0 ? Integer.MAX_VALUE : maxBitRate));
        }
    }

    public void setMinLoadRetryCountModifier(int newMinLoadRetryCount) {
        minLoadRetryCount = newMinLoadRetryCount;
        releasePlayer();
        initializePlayer();
    }

    public void setPlayInBackground(boolean playInBackground) {
        this.playInBackground = playInBackground;
    }

    public void setDisableFocus(boolean disableFocus) {
        this.disableFocus = disableFocus;
    }

    public void setFullscreen(boolean fullscreen) {
        if (fullscreen == isFullscreen) {
            return; // Avoid generating events when nothing is changing
        }
        isFullscreen = fullscreen;

        Activity activity = themedReactContext.getCurrentActivity();
        if (activity == null) {
            return;
        }
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        int uiOptions;
        if (isFullscreen) {
            if (Util.SDK_INT >= 19) { // 4.4+
                uiOptions = SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | SYSTEM_UI_FLAG_FULLSCREEN;
            } else {
                uiOptions = SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_FULLSCREEN;
            }
            eventEmitter.fullscreenWillPresent();
            decorView.setSystemUiVisibility(uiOptions);
            eventEmitter.fullscreenDidPresent();
        } else {
            uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            eventEmitter.fullscreenWillDismiss();
            decorView.setSystemUiVisibility(uiOptions);
            eventEmitter.fullscreenDidDismiss();
        }
    }

    public void setUseTextureView(boolean useTextureView) {
        exoPlayerView.setUseTextureView(useTextureView);
    }

    public void setHideShutterView(boolean hideShutterView) {
        exoPlayerView.setHideShutterView(hideShutterView);
    }

    public void setBufferConfig(int newMinBufferMs, int newMaxBufferMs, int newBufferForPlaybackMs,
                                int newBufferForPlaybackAfterRebufferMs) {
        minBufferMs = newMinBufferMs;
        maxBufferMs = newMaxBufferMs;
        bufferForPlaybackMs = newBufferForPlaybackMs;
        bufferForPlaybackAfterRebufferMs = newBufferForPlaybackAfterRebufferMs;
        releasePlayer();
        initializePlayer();
    }

    /**
     * Handling controls prop
     *
     * @param controls Controls prop, if true enable controls, if false disable them
     */
    public void setControls(boolean controls) {
        // todo : remove
    }

    public void setDrmLicenseUrl(String drmLicenseUrl) {
        this.drmLicenseUrl = drmLicenseUrl;
    }

    public void setYouboraParams(Options youboraOptions) {
        this.youboraPlugin = new Plugin(youboraOptions, this.getContext());
        if (player != null && trackSelector != null) {
            Exoplayer2Adapter adapter = new Exoplayer2Adapter(player);
            adapter.setCustomEventLogger(trackSelector);
            youboraPlugin.setAdapter(adapter);
        }
    }

    public void setThumbnailsVttUrl(String thumbnailsVttUrl) {
        new ThumbnailParserTask().execute(thumbnailsVttUrl);
    }
    // #endregion

    // #region vtt
    private class ThumbnailParserTask extends AsyncTask<String, Integer, List<String>> {
        protected List<String> doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                ArrayList<String> keys = new ArrayList<>();
                ArrayList<String> values = new ArrayList<>();

                String line;
                while ((line = rd.readLine()) != null) {

                    StringBuilder builder = new StringBuilder();

                    if (line.startsWith("./")) {
                        String image = line.substring(1, line.lastIndexOf("#xywh"));
                        builder.append("\"image\" : \"" + image + "\"");
                        builder.append(", ");
                        String xywh = line.substring(line.lastIndexOf("#xywh=") + 6);
                        String[] xy = xywh.split(",");
                        builder.append("\"x\" : " + xy[0] + "");
                        builder.append(", ");
                        builder.append("\"y\" : " + xy[1] + "");
                        String value = builder.toString();
                        values.add(value);
                    } else if (line != "") {
                        if (line.contains("-->")) {
                            String start = line.substring(0, line.indexOf("-->") - 1);
                            String end = line.substring(line.indexOf("-->") + 3);
                            String key = "\"start\" : " + parseDuration(start) + " , \"end\" : " + parseDuration(end)
                                    + ", ";
                            keys.add(key);
                        }
                    }
                }

                List<String> cuePoints = new ArrayList<>();
                for (int i = 0; i < keys.size(); i++) {
                    StringBuilder resultBuilder = new StringBuilder();
                    resultBuilder.append("{ ");
                    resultBuilder.append(keys.get(i));
                    resultBuilder.append(values.get(i));
                    resultBuilder.append(" }");
                    cuePoints.add(resultBuilder.toString());
                }
                return cuePoints;
            } catch (Exception e) {
                // Log.e(TAG, e.getMessage());
                return null;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String> cuePoints) {
            // this is executed on the main thread after the process is over
            // update your UI here
            if (cuePoints != null) {
                WritableArray writableArrayOfCuePoints = new WritableNativeArray();
                for (String cuePoint : cuePoints) {
                    writableArrayOfCuePoints.pushString(cuePoint);
                }
                eventEmitter.vttCuePointsChange(writableArrayOfCuePoints);
            }
        }

        private int parseDuration(String duration) {
            String[] segments = duration.trim().split(":");
            return (Integer.parseInt(segments[0]) * 3600) + (Integer.parseInt(segments[1]) * 60)
                    + (int) Math.floor(Float.parseFloat(segments[2]));
        }
    }
    // #endregion
}
