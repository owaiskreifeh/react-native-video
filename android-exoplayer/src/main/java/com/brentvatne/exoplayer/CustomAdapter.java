package com.brentvatne.exoplayer;
import androidx.annotation.NonNull;
import com.brentvatne.exoplayer.ReactExoplayerView;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.npaw.youbora.lib6.YouboraLog;
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter;

import java.util.HashMap;
import java.util.Map;

class CustomAdapter extends Exoplayer2Adapter {
    private boolean skipStateChangedIdle = false;

    public CustomAdapter(@NonNull ExoPlayer player) {
        super(player);
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {

        if(playbackState == Player.STATE_IDLE) {
            stateChangedIdle();
            YouboraLog.debug("onPlaybackStateChanged: STATE_IDLE");
        } else {
            super.onPlaybackStateChanged(playbackState);
        }
    }

    protected void stateChangedIdle() {
        if (!skipStateChangedIdle) {
            fireStop();
        }

        skipStateChangedIdle = false;
    }


    @Override
    public void onPlayerError(@NonNull PlaybackException error) {

        if (error instanceof PlaybackException && ((ExoPlaybackException) error).type == ExoPlaybackException.TYPE_SOURCE) {
            ExoPlaybackException exoPlaybackException = (ExoPlaybackException) error;
            String errorClass = exoPlaybackException.getSourceException().getClass().getSimpleName();

            switch (errorClass) {
                case "InvalidResponseCodeException":
                    invalidResponseCodeException(error, exoPlaybackException);
                    break;
                case "HttpDataSourceException":
                    httpDataSourceException(error, exoPlaybackException);
                    break;
                case "BehindLiveWindowException":
                    fireError(String.valueOf(error.errorCode), error.getMessage(), "");
                    break;
                case "DrmSessionException":
                    handleDRMSessionExceptions(error, exoPlaybackException);
                    break;
                default:
                    fireFatalError(String.valueOf(error.errorCode), error.getMessage() + ", Error Class : " + errorClass, "");
                    break;
            }
        } else {
            fireFatalError(String.valueOf(error.errorCode), error.getMessage(), "");
        }

        skipStateChangedIdle = true;
        YouboraLog.debug("onPlayerError: " + error);
    }

    private void invalidResponseCodeException(PlaybackException error, ExoPlaybackException exoPlaybackException) {
        HttpDataSource.InvalidResponseCodeException invalidResponseCodeException =
                (HttpDataSource.InvalidResponseCodeException) exoPlaybackException.getSourceException();

        String failedURL = invalidResponseCodeException.dataSpec.uri.toString();

        Map<String, String> dimMap = new HashMap<>();
        dimMap.put("failedURL", failedURL);

        fireEvent("Failed Source", dimMap);

        fireFatalError(
                String.valueOf(error.errorCode),
                error.getMessage() + ", " + invalidResponseCodeException.toString(),
                error.getMessage()
        );
    }

    private void httpDataSourceException(PlaybackException error, ExoPlaybackException exoPlaybackException) {
        HttpDataSource.HttpDataSourceException httpDataSourceException =
                (HttpDataSource.HttpDataSourceException) exoPlaybackException.getSourceException();

        String failedURL = httpDataSourceException.dataSpec.uri.toString();

        Map<String, String> dimMap = new HashMap<>();
        dimMap.put("failedURL", failedURL);

        fireEvent("Failed Source", dimMap);

        switch (httpDataSourceException.type) {
            case HttpDataSource.HttpDataSourceException.TYPE_OPEN:
                fireFatalError(
                        String.valueOf(error.errorCode),
                        "OPEN - " + error.getMessage() + ", "+ httpDataSourceException.toString(),
                        error.getMessage()
                );

                break;

            case HttpDataSource.HttpDataSourceException.TYPE_READ:
                fireFatalError(
                        String.valueOf(error.errorCode),
                        "READ - " + error.getMessage() + ", " + httpDataSourceException.toString(),
                        error.getMessage()
                );

                break;

            case HttpDataSource.HttpDataSourceException.TYPE_CLOSE:
                fireFatalError(
                        String.valueOf(error.errorCode),
                        "CLOSE - " + error.getMessage() + ", " + httpDataSourceException.toString(),
                        error.getMessage()
                );

                break;
        }
    }

    private void handleDRMSessionExceptions(PlaybackException error, ExoPlaybackException exoPlaybackException) {
        String sourceExceptionMessage = exoPlaybackException.getSourceException().getMessage();

        Map<String, String> dimMap = new HashMap<>();
        dimMap.put("HUT", ReactExoplayerView.drmUserToken);

        fireEvent("HUT", dimMap);

        fireFatalError(
                String.valueOf(error.errorCode),
                sourceExceptionMessage,
                error.getMessage()
        );
    }
}