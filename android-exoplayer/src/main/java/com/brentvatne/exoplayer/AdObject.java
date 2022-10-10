package com.brentvatne.exoplayer;
import java.util.ArrayList;

public class AdObject {
  public final long adStart;
  public final long adEnd;
  public final long adDuration;
  public final boolean played;
  public final boolean started;
  public final ArrayList <AdObject> slotAds;

  public AdObject(long adStart, long adEnd, long adDuration, boolean played, boolean started, ArrayList <AdObject> slotAds) {
      this.adStart = adStart;
      this.adEnd = adEnd;
      this.adDuration = adDuration;
      this.played = played;
      this.started =  started;
      this.slotAds = slotAds;
  }
}
