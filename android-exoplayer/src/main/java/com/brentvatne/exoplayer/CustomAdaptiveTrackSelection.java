package com.brentvatne.exoplayer;

import android.util.Log;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.common.collect.ImmutableList;

public class CustomAdaptiveTrackSelection extends AdaptiveTrackSelection {

    int tracksNumber;
    public CustomAdaptiveTrackSelection(TrackGroup group, int[] tracks, BandwidthMeter bandwidthMeter) {
        super(group, tracks, bandwidthMeter);
        tracksNumber = tracks.length;
    }

    @Override
    protected boolean canSelectFormat(Format format, int trackBitrate, long effectiveBitrate) {

        long _effectiveBitrate;

        if(ReactExoplayerView.qualityCounter <= (tracksNumber * 2) && !ReactExoplayerView.isTrailer) {
            _effectiveBitrate = 745040;
            ReactExoplayerView.qualityCounter++;
        } else {
            _effectiveBitrate = effectiveBitrate;
        }

        return trackBitrate <= _effectiveBitrate;
    }

    public static class Factory extends AdaptiveTrackSelection.Factory
    {

        @Override
    protected AdaptiveTrackSelection createAdaptiveTrackSelection(
            TrackGroup group,
            int[] tracks,
            int type,
            BandwidthMeter bandwidthMeter,
            ImmutableList<AdaptationCheckpoint> adaptationCheckpoints) {
            return new CustomAdaptiveTrackSelection(group,tracks, bandwidthMeter);
        }

    }
}