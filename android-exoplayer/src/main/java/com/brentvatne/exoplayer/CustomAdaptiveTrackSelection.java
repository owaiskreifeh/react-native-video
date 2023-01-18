package com.brentvatne.exoplayer;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.common.collect.ImmutableList;

public class CustomAdaptiveTrackSelection extends AdaptiveTrackSelection {

        public CustomAdaptiveTrackSelection(TrackGroup group, int[] tracks, BandwidthMeter bandwidthMeter) {
                super(group, tracks, bandwidthMeter);
        }

        @Override
        protected boolean canSelectFormat(Format format, int trackBitrate, float playbackSpeed, long effectiveBitrate) {

                long _effectiveBitrate;

                if(ReactExoplayerView.qualityCounter <= 2 && !ReactExoplayerView.isTrailer) {
                        _effectiveBitrate = 50000;
                        ReactExoplayerView.qualityCounter++;
                } else {
                        _effectiveBitrate = effectiveBitrate;
                }

                return Math.round(trackBitrate * playbackSpeed) <= _effectiveBitrate;
        }

        public static class Factory extends AdaptiveTrackSelection.Factory
        {

                @Override
                protected AdaptiveTrackSelection createAdaptiveTrackSelection(TrackGroup group, BandwidthMeter bandwidthMeter, int[] tracks, ImmutableList<AdaptationCheckpoint> adaptationCheckpoints) {
                        return new CustomAdaptiveTrackSelection(group,tracks,bandwidthMeter);
                }

        }
}