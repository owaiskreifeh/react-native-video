package com.brentvatne.exoplayer;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.FrameLayout;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdProgressInfo;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.CuePoint;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.StreamDisplayContainer;
import com.google.ads.interactivemedia.v3.api.StreamManager;
import com.google.ads.interactivemedia.v3.api.StreamRequest;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleDai implements AdEvent.AdEventListener, AdErrorEvent.AdErrorListener, AdsLoader.AdsLoadedListener {

    private ImaSdkFactory sdkFactory;
    private AdsLoader adsLoader;
    private StreamDisplayContainer displayContainer;
    private StreamManager streamManager;
    private List<VideoStreamPlayer.VideoStreamPlayerCallback> playerCallbacks;
    private ReactExoplayerView videoPlayer;

    private long snapBackTimeMs; // Stream time to snap back to, in milliseconds.
    private Uri fallbackUrl;
    public boolean adPlaying = false;
    private final VideoEventEmitter eventEmitter;
    private long adProgtrssEmmitTime = 0;
    private boolean cuePointsEmitted = false;

    public GoogleDai(Context context, String language, ReactExoplayerView videoPlayer, VideoEventEmitter eventEmitter, String adsId, Uri fallbackUrl) {

        this.videoPlayer = videoPlayer;
        this.fallbackUrl = fallbackUrl;
        this.eventEmitter = eventEmitter;
        sdkFactory = ImaSdkFactory.getInstance();
        playerCallbacks = new ArrayList<>();

        displayContainer = sdkFactory.createStreamDisplayContainer(
                new FrameLayout(context),
                createVideoStreamPlayer()
        );

        setPlayerCallback();
        createAdsLoader(context, language, adsId);
    }

    private void createAdsLoader(Context context, String language, String adsId) {
        ImaSdkSettings settings = sdkFactory.createImaSdkSettings();
        //settings.setDebugMode(true);
        settings.setLanguage(language);
        adsLoader = sdkFactory.createAdsLoader(context, settings, displayContainer);
        adsLoader.addAdErrorListener(this);
        adsLoader.addAdsLoadedListener(this);

        String[] separatedIDs = adsId.split("_,_");
        StreamRequest request = sdkFactory.createVodStreamRequest(separatedIDs[0], separatedIDs[1], null);

        Map adTagParameters = new HashMap();
        adTagParameters.put("cust_params", "shahid_localization=" + language);
        request.setAdTagParameters(adTagParameters);

        adsLoader.requestStream(request);
    }

    private void setPlayerCallback() {
        videoPlayer.setExoPlayerCallback(
                new ReactExoplayerView.ExoPlayerCallback() {
                    @Override
                    public void onUserTextReceived(String userText) {
                        for (VideoStreamPlayer.VideoStreamPlayerCallback callback : playerCallbacks) {
                            callback.onUserTextReceived(userText);
                        }
                    }

                    @Override
                    public void onSeek(int windowIndex, long positionMs) {
                        if (adPlaying) {
                            if (videoPlayer.getCurrentPositionMs() == 0 && positionMs != 0) {
                                // CW case > if we are currently in pre-roll && not going to seek to 0
                                snapBackTimeMs = getStreamTime(positionMs);
                            }
                            // disable seeking when playing ads
                            // also to prevent edge cases that skips the ad while its playing
                            return;
                        }

                        if (streamManager != null) {
                            positionMs = getStreamTime(positionMs);
                            // positionMs == 0 ? 1 : positionMs > work around for skipping pre-roll
                            CuePoint cuePoint = streamManager.getPreviousCuePointForStreamTimeMs(positionMs == 0 ? 1 : positionMs);

                            if (cuePoint != null) {
                                if (cuePoint.isPlayed()) {
                                    if (cuePoint.getEndTimeMs() >= positionMs) {
                                        // already played skip it
                                        positionMs = cuePoint.getEndTimeMs();
                                    }
                                } else {
                                    snapBackTimeMs = positionMs;
                                    // Missed cue point, so snap back to the beginning of cue point.
                                    positionMs = cuePoint.getStartTimeMs();
                                }
                            }
                        }
                        videoPlayer.seekToFromAfterCallback(positionMs);
                    }

                    @Override
                    public void onContentComplete() {
                        for (VideoStreamPlayer.VideoStreamPlayerCallback callback : playerCallbacks) {
                            callback.onContentComplete();
                        }
                    }

                    @Override
                    public void onPause() {
                        for (VideoStreamPlayer.VideoStreamPlayerCallback callback : playerCallbacks) {
                            callback.onPause();
                        }
                    }

                    @Override
                    public void onResume() {
                        for (VideoStreamPlayer.VideoStreamPlayerCallback callback : playerCallbacks) {
                            callback.onResume();
                        }
                    }

                    @Override
                    public void onVolumeChanged(int percentage) {
                        for (VideoStreamPlayer.VideoStreamPlayerCallback callback : playerCallbacks) {
                            callback.onVolumeChanged(percentage);
                        }
                    }
                });
    }

    private VideoStreamPlayer createVideoStreamPlayer() {
        return new VideoStreamPlayer() {

            @Override
            public void loadUrl(String s, List<HashMap<String, String>> list) {
                videoPlayer.setAdsSrc(s);
            }

            @Override
            public void pause() {
                videoPlayer.setPausedModifier(true);
            }

            @Override
            public void resume() {
                videoPlayer.setPausedModifier(false);
            }

            @Override
            public int getVolume() {
                return 100;
            }

            @Override
            public void onAdPeriodStarted() {
            }

            @Override
            public void onAdPeriodEnded() {
            }

            @Override
            public void seek(long timeMs) {
                videoPlayer.seekTo(timeMs);
            }

            @Override
            public void addCallback(VideoStreamPlayerCallback videoStreamPlayerCallback) {
                playerCallbacks.add(videoStreamPlayerCallback);
            }

            @Override
            public void removeCallback(VideoStreamPlayerCallback videoStreamPlayerCallback) {
                playerCallbacks.remove(videoStreamPlayerCallback);
            }

            @Override
            public void onAdBreakStarted() {
                adPlaying = true;
                eventEmitter.adEvent("AdBreakStarted", null);
            }

            @Override
            public void onAdBreakEnded() {
                adPlaying = false;
                eventEmitter.adEvent("AdBreakEnded", null);

                if (videoPlayer != null) {
                    if (snapBackTimeMs > 0) {
                        // after the ad has ended seek to the snap back time
                        videoPlayer.seekToFromAfterCallback(snapBackTimeMs);
                        snapBackTimeMs = 0;
                    }
                }
            }

            @Override
            public VideoProgressUpdate getContentProgress() {
                if (videoPlayer == null) {
                    return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
                }

                long currentPos = videoPlayer.getCurrentPositionMs();

                if (adPlaying) {
                    emitAdProgress();
                } else {
                    CuePoint cuePoint = streamManager.getPreviousCuePointForStreamTimeMs(currentPos + 200);
                    // currentPos + 100 to avoid case that causes a seek at the exact moment the ad ends
                    if (cuePoint != null && cuePoint.isPlayed() && cuePoint.getEndTimeMs() > currentPos + 100) {
                        // skip ads that are already played
                        videoPlayer.seekToFromAfterCallback(cuePoint.getEndTimeMs());
                    }
                }

                return new VideoProgressUpdate(currentPos, videoPlayer.getDuration());
            }
        };
    }

    public long getContentTime(long streamTime) {
        if (streamManager != null) {
            return streamManager.getContentTimeMsForStreamTimeMs(streamTime);
        }
        return streamTime;
    }

    public long getStreamTime(long contentTime) {
        if (streamManager != null) {
            return streamManager.getStreamTimeMsForContentTimeMs(contentTime);
        }
        return contentTime;
    }

    @Override
    public void onAdError(AdErrorEvent event) {
        Log.d("DAI", String.format("onAdError Error: %s\n", event.getError().getMessage()));
        Log.d("DAI", "Playing fallback Url");
        // fallback to original url without ads
        videoPlayer.setSrc(fallbackUrl, null, null, null);
    }

    @Override
    public void onAdEvent(AdEvent event) {
//        switch (event.getType()) {
//            //case AD_PROGRESS: emitAdProgress(); break;
//            // case AD_BREAK_STARTED: break;
//            // case AD_BREAK_ENDED: break;
//            // case AD_PERIOD_STARTED: break;
//            // case AD_PERIOD_ENDED: break;
//            // case AD_PROGRESS: break;
//            // case AD_BREAK_STARTED: break;
//            // case AD_BREAK_ENDED:  break;
//            // case AD_PERIOD_STARTED: break;
//            // case AD_PERIOD_ENDED: break;
//            //case CUEPOINTS_CHANGED: emitCuepointsChanged(); break;
//        }
    }

    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
        streamManager = event.getStreamManager();
        streamManager.addAdErrorListener(this);
        streamManager.addAdEventListener(this);
        streamManager.init();
    }

    public void emitAdProgress() {
        AdProgressInfo ad = streamManager.getAdProgressInfo();
        long currentTime = System.currentTimeMillis();

        if (currentTime - adProgtrssEmmitTime > 300 && ad != null) {
            adProgtrssEmmitTime = currentTime;
            WritableMap eventData = Arguments.createMap();
            eventData.putString("progress", (int) Math.round(ad.getCurrentTime() / ad.getDuration() * 100) + "%");
            eventData.putInt("remainingTime", Math.max((int) Math.ceil(ad.getDuration() - ad.getCurrentTime()), 0));
            eventData.putInt("currentPosition", ad.getAdPosition());
            eventData.putInt("totalCount", ad.getTotalAds());
            eventEmitter.adEvent("AdProgress", eventData);
        }
    }

    public void emitCuepointsChanged() {
        long duration = videoPlayer.getDuration();
        if (!cuePointsEmitted && duration > 0) {
            List<CuePoint> cuePoints = streamManager.getCuePoints();
            if (!cuePoints.isEmpty()) {
                WritableArray writableArrayOfCuePoints = Arguments.createArray();

                for (CuePoint cuePoint : cuePoints) {
                    //if (!cuePoint.isPlayed()) {
                        float adTime = cuePoint.getStartTimeMs();
                        writableArrayOfCuePoints.pushDouble(adTime / duration * 100);
                    //}
                }

                WritableMap eventData = Arguments.createMap();
                eventData.putArray("cuePoints", writableArrayOfCuePoints);
                cuePointsEmitted = true;
                eventEmitter.adEvent("CuePointsChange", eventData);
            }
        }
    }

    public void release() {
        if (streamManager != null) {
            streamManager.destroy();
            streamManager = null;
        }

        adsLoader.release();
    }
}
