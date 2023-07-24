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
    private static final String TAG = "CustomAdapterYoubora";
    private boolean skipStateChangedIdle = false;
    private ReactExoplayerView view;
    public CustomAdapter(@NonNull ExoPlayer player, ReactExoplayerView view) {
        super(player);
        this.view = view;
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

        ExoPlaybackException exoPlaybackException = (ExoPlaybackException) error;
        Throwable innerErrorCause = null;
        String innerErrorMessage = "UNKNOWN INNER MESSAGE";
        String sourceErrorCauseMessage = "UNKNOWN INNER CAUSE";

        if(view.errorRetries < 1 && error.errorCode >= 3000 && error.errorCode < 4000) {
            view.errorRetries++;
            fireStop();
            return;
        }
        switch (exoPlaybackException.type) {
            case ExoPlaybackException.TYPE_SOURCE:
                innerErrorCause = exoPlaybackException.getSourceException().getCause();
                innerErrorMessage = exoPlaybackException.getSourceException().getMessage();
                break;
            case ExoPlaybackException.TYPE_RENDERER:
                innerErrorCause = exoPlaybackException.getRendererException().getCause();
                innerErrorMessage = exoPlaybackException.getRendererException().getMessage();
                break;
            case ExoPlaybackException.TYPE_UNEXPECTED:
                innerErrorCause = exoPlaybackException.getUnexpectedException().getCause();
                innerErrorMessage = exoPlaybackException.getUnexpectedException().getMessage();
                break;
            case ExoPlaybackException.TYPE_REMOTE:
                innerErrorCause = exoPlaybackException.getCause();
                innerErrorMessage = exoPlaybackException.getMessage();
                break;
        }

        if (innerErrorCause != null) {
            sourceErrorCauseMessage = innerErrorCause.toString();
        }
        
        String extraErrorDetails = String.format("Message: %s | Cause: %s", innerErrorMessage, sourceErrorCauseMessage);

        Map<String, String> customErrorEventMap = new HashMap<String, String>();
        customErrorEventMap.put("details", extraErrorDetails);

        fireEvent("CUSTOM_PLAYER_ERROR", customErrorEventMap);

        if (error instanceof PlaybackException && ((ExoPlaybackException) error).type == ExoPlaybackException.TYPE_SOURCE) {
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
                    fireFatalError(String.valueOf(error.errorCode), error.getMessage() + ", Error Class : " + errorClass, extraErrorDetails);
                    break;
            }
        } else {
            if(error.errorCode >= 4000 && error.errorCode < 5000) {
                fireStop();
            } else {
                fireFatalError(String.valueOf(error.errorCode), error.getMessage(), extraErrorDetails);
            }
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
                failedURL
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
                        failedURL
                );

                break;

            case HttpDataSource.HttpDataSourceException.TYPE_READ:
                fireFatalError(
                        String.valueOf(error.errorCode),
                        "READ - " + error.getMessage() + ", " + httpDataSourceException.toString(),
                        failedURL
                );

                break;

            case HttpDataSource.HttpDataSourceException.TYPE_CLOSE:
                fireFatalError(
                        String.valueOf(error.errorCode),
                        "CLOSE - " + error.getMessage() + ", " + httpDataSourceException.toString(),
                        failedURL
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
                ReactExoplayerView.drmUserToken
        );
    }
}