package com.brentvatne.exoplayer;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

class Analytics {
    // Lotame Keys
    private static final String KEY_LOTAME = "lotame";
    private static final String KEY_LOTAME_PROFILE = "profile";
    private static final String KEY_LOTAME_AUDIENCES = "audiences";
    private static final String KEY_LOTAME_AUDIENCES_STRING = "lotameAudiences";

    // Youbora Keys
    // @TODO: Move youbora keys here


    // Lotame Properties
    private ReadableMap lotameProfile;
    private ReadableArray lotameAudiences;
    private String lotameAudiencesString;

    Analytics(ReadableMap analyticsParams){

        // Cast Lotame params
        if (analyticsParams.hasKey(KEY_LOTAME)){
            ReadableMap lotame = analyticsParams.getMap(KEY_LOTAME);
            if (lotame != null) {
                if (lotame.hasKey(KEY_LOTAME_PROFILE)){
                    this.lotameProfile =lotame.getMap(KEY_LOTAME_PROFILE);
                }
                if (lotame.hasKey(KEY_LOTAME_AUDIENCES)){
                    this.lotameAudiences = lotame.getArray(KEY_LOTAME_AUDIENCES);
                }
                if (lotame.hasKey(KEY_LOTAME_AUDIENCES_STRING)){
                    this.lotameAudiencesString = lotame.getString(KEY_LOTAME_AUDIENCES_STRING);
                }
            }

        }

        // @TODO: Move Youbora params here
    }

    public ReadableMap getLotameProfile(){
        return this.lotameProfile;
    }
    public ReadableArray getLotameAudiences(){
        return this.lotameAudiences;
    }
    public String getLotameAudiencesString() {
        return this.lotameAudiencesString;
    }
}
