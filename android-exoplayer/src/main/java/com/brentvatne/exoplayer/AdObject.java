package com.brentvatne.exoplayer;
import java.util.ArrayList;

public class AdObject {
    public  long adStart;
    public  long adEnd;
    public  long adDuration;
    public  boolean played;
    public  boolean started;
    public  ArrayList <AdObject> slotAds;

    public AdObject(long adStart, long adEnd, long adDuration, boolean played, boolean started, ArrayList <AdObject> slotAds) {
        this.adStart = adStart;
        this.adEnd = adEnd;
        this.adDuration = adDuration;
        this.played = played;
        this.started =  started;
        this.slotAds = slotAds;
    }

    public AdObject(long adStart, long adEnd, long adDuration) {
        this.adStart = adStart;
        this.adEnd = adEnd;
        this.adDuration = adDuration;
    }
}
