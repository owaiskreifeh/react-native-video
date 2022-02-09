package com.brentvatne.exoplayer;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.ads.interactivemedia.v3.api.Ad;
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
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GoogleDai implements AdEvent.AdEventListener, AdErrorEvent.AdErrorListener, AdsLoader.AdsLoadedListener {

    private ImaSdkFactory sdkFactory;
    private AdsLoader adsLoader;
    private StreamDisplayContainer displayContainer;
    private StreamManager streamManager;
    private List<VideoStreamPlayer.VideoStreamPlayerCallback> playerCallbacks;
    private ReactExoplayerView videoPlayer;

    private long bookMarkContentTimeMs; // Bookmarked content time, in milliseconds.
    private long snapBackTimeMs; // Stream time to snap back to, in milliseconds.
    private Uri fallbackUrl;
    public boolean adPlaying = false;

    public GoogleDai(Context context, ReactExoplayerView videoPlayer, String adsId, Uri fallbackUrl) {

        this.videoPlayer = videoPlayer;
        this.fallbackUrl = fallbackUrl;
        sdkFactory = ImaSdkFactory.getInstance();
        playerCallbacks = new ArrayList<>();

        displayContainer = sdkFactory.createStreamDisplayContainer(
                new FrameLayout(context),
                createVideoStreamPlayer()
        );

        setPlayerCallback();
        createAdsLoader(context, adsId);
    }

    private void createAdsLoader(Context context, String adsId) {
        ImaSdkSettings settings = sdkFactory.createImaSdkSettings();
        //settings.setDebugMode(true);
        //settings.setLanguage();
        adsLoader = sdkFactory.createAdsLoader(context, settings, displayContainer);
        adsLoader.addAdErrorListener(this);
        adsLoader.addAdsLoadedListener(this);

        String[] separatedIDs = adsId.split("_,_");
        StreamRequest request = sdkFactory.createVodStreamRequest(separatedIDs[0], separatedIDs[1], null);

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
                        long timeToSeek = positionMs;
                        if (streamManager != null) {
                            CuePoint cuePoint = streamManager.getPreviousCuePointForStreamTimeMs(positionMs == 0 ? 1 : positionMs);
                            long bookMarkStreamTimeMs = streamManager.getStreamTimeMsForContentTimeMs(bookMarkContentTimeMs);

                            if (cuePoint != null) {
                                Log.d("DAI", "onSeek: cuePoint > isPlayed" + cuePoint.isPlayed());

                                if (cuePoint.isPlayed()) {
                                    if (cuePoint.getEndTimeMs() >= positionMs) {
                                        // already played skip it
                                        timeToSeek = cuePoint.getEndTimeMs();
                                    }
                                } else if (cuePoint.getEndTimeMs() > bookMarkStreamTimeMs) {
                                    snapBackTimeMs = timeToSeek; // Update snap back time.
                                    // Missed cue point, so snap back to the beginning of cue point.
                                    timeToSeek = cuePoint.getStartTimeMs();
                                    Log.i("DAI", "SnapBack to " + timeToSeek + " ms.");
                                }

                                videoPlayer.seekToFromAfterCallback(timeToSeek);
                                //videoPlayer.setCanSeek(false);
                                return;
                            }
                        }
                        videoPlayer.seekToFromAfterCallback(timeToSeek);
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

                // Bookmarking
                if (bookMarkContentTimeMs > 0) {
                    long streamTimeMs = streamManager.getStreamTimeMsForContentTimeMs(bookMarkContentTimeMs);
                    videoPlayer.seekTo(streamTimeMs);
                }
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
                Log.d("DAI", "Ad Period Started");
            }

            @Override
            public void onAdPeriodEnded() {
                Log.d("DAI", "Ad Period Ended");
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
                // Disable player controls.
//                videoPlayer.setCanSeek(false);
//                videoPlayer.enableControls(false);
                adPlaying = true;
            }

            @Override
            public void onAdBreakEnded() {
                if (videoPlayer != null) {
//                    videoPlayer.setCanSeek(true);
//                    videoPlayer.enableControls(true);
                    if (snapBackTimeMs > 0) {
                        videoPlayer.seekToFromAfterCallback(snapBackTimeMs);
                    }
                }
                snapBackTimeMs = 0;
                adPlaying = false;
            }

            @Override
            public VideoProgressUpdate getContentProgress() {
                if (videoPlayer == null) {
                    return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
                }

                long currentPos = videoPlayer.getCurrentPositionMs();
                CuePoint cuePoint = streamManager.getPreviousCuePointForStreamTimeMs(currentPos);
                if (cuePoint != null && !adPlaying && cuePoint.getEndTimeMs() > currentPos + 100 && cuePoint.isPlayed()) {
                    videoPlayer.seekTo(cuePoint.getEndTimeMs());
                }

                return new VideoProgressUpdate(currentPos, videoPlayer.getDuration());
            }
        };
    }

    public long getContentTimeMs() {
        if (streamManager != null) {
            return streamManager.getContentTimeMsForStreamTimeMs(videoPlayer.getCurrentPositionMs());
        }
        return videoPlayer.getCurrentPositionMs();
    }

    public long getStreamTimeMsForContentTimeMs(long contentTimeMs) {
        if (streamManager != null) {
            return streamManager.getStreamTimeMsForContentTimeMs(contentTimeMs);
        }
        return contentTimeMs;
    }

    @Override
    public void onAdError(AdErrorEvent event) {
        // play fallback URL.
        Log.d("DAI", String.format("onAdError Error: %s\n", event.getError().getMessage()));
        Log.d("DAI", "Playing fallback Url");
        videoPlayer.setSrc(fallbackUrl, null, null, null);
    }

    @Override
    public void onAdEvent(AdEvent event) {
        Log.d("DAI", String.format("onAdEvent Event: %s\n", event.getType()));
        if (event.getAdData() != null) {
            Log.d("DAI", String.format("onAdEvent Data: %s\n", event.getAdData().toString()));
        }
    }

    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
        streamManager = event.getStreamManager();
        streamManager.addAdErrorListener(this);
        streamManager.addAdEventListener(this);
        streamManager.init();
    }

    public void release() {
        if (streamManager != null) {
            streamManager.destroy();
            streamManager = null;
        }

        adsLoader.release();
    }
}
