package com.brentvatne.common;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

class ErrorRange {
    int from, to;
    ErrorRange(int from, int to) {
        this.from = from;
        this.to = to;
    }
    public boolean contains(int code) {
        return from <= code && code <= to;
    }

    @NonNull
    @Override
    public String toString() {
        return from + ".." + to;
    }
}

public class ExoUserConfig {

    public static final String TAG = "ExoUserConfig";
    public static ExoUserConfig currentConfig = new ExoUserConfig();

    public int maxRetries = 3;
    public int retryDelay = 100; // milliseconds
    public int requestTimeout = 30000; // milliseconds
    public ErrorRange[] handledErrorRanges = null;

    ExoUserConfig() {
        // NOOP
    }

    public static ExoUserConfig createFromReadableMap(ReadableMap configMap, Boolean setDefault) {
        ExoUserConfig config = new ExoUserConfig();
        if (configMap.hasKey("exoRetries")) {
            config.maxRetries = configMap.getInt("exoRetries");
        }
        if (configMap.hasKey("exoRetryWait")) {
            config.retryDelay = configMap.getInt("exoRetryWait");
        }
        if (configMap.hasKey("exoTimeout")) {
            config.requestTimeout = configMap.getInt("exoTimeout");
        }
        if (configMap.hasKey("exoHandledErrorRanges")) {
            ReadableArray array = configMap.getArray("exoHandledErrorRanges");
            assert array != null;
            config.handledErrorRanges = new ErrorRange[array.size()];
            for (int i = 0; i < array.size(); i++) {
                ReadableMap map = array.getMap(i);
                if (map.hasKey("from") && map.hasKey("to")) {
                    config.handledErrorRanges[i] = new ErrorRange(map.getInt("from"), map.getInt("to"));
                }
            }
        }

        if (setDefault) {
            currentConfig = config;
        }

        return config;
    }

    public Boolean isHandledError(int code) {
        if (handledErrorRanges == null) {
            return false;
        }
        for (ErrorRange range : handledErrorRanges) {
            if (range.contains(code)) {
                return true;
            }
        }
        return false;
    }
}
