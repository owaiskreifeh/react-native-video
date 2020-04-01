// @flow

import { RCTViewManager } from "react-native-dom";

import RCTVideo from "./RCTVideo";
import resizeModes from "./resizeModes";

import { VideoSource, YouboraParams } from "./types";

class RCTVideoManager extends RCTViewManager {
  static moduleName = "RCTVideoManager";

  view() {
    return new RCTVideo(this.bridge);
  }

  describeProps() {
    return super
      .describeProps()
      .addBooleanProp("controls", this.setControls)
      .addStringProp("id", this.setId)
      .addBooleanProp("muted", this.setMuted)
      .addBooleanProp("paused", this.setPaused)
      .addNumberProp("progressUpdateInterval", this.setProgressUpdateInterval)
      .addBooleanProp("rate", this.setRate)
      .addBooleanProp("repeat", this.setRepeat)
      .addNumberProp("resizeMode", this.setResizeMode)
      .addNumberProp("seek", this.setSeek)
      .addObjectProp("src", this.setSource)
      .addNumberProp("volume", this.setVolume)
      .addStringProp("drmLicenseUrl", this.setDrmLicenseUrl)
      .addStringProp("thumbnailsVttUrl", this.setThumbnailsVttUrl)
      .addObjectProp("youboraParams", this.setYouboraParams)
      .addDirectEvent("onVideoEnd")
      .addDirectEvent("onVideoError")
      .addDirectEvent("onVideoLoad")
      .addDirectEvent("onVideoLoadStart")
      .addDirectEvent("onVideoProgress");
  }

  dismissFullscreenPlayer() {
    // not currently working
  }

  presentFullscreenPlayer() {
    // not currently working
  }

  setControls(view: RCTVideo, value: boolean) {
    view.controls = value;
  }

  setDrmLicenseUrl(view: RCTVideo, value: string) {
    view.drmLicenseUrl = value;
  }

  setThumbnailsVttUrl(view: RCTVideo, value: string) {
    view.thumbnailsVttUrl = value;
  }

  setId(view: RCTVideo, value: string) {
    view.id = value;
  }

  setMuted(view: RCTVideo, value: boolean) {
    view.muted = value;
  }

  setPaused(view: RCTVideo, value: boolean) {
    view.paused = value;
  }

  setRate(view: RCTVideo, value: number) {
    view.rate = value;
  }

  setRepeat(view: RCTVideo, value: boolean) {
    view.repeat = value;
  }

  setResizeMode(view: RCTVideo, value: number) {
    view.resizeMode = value;
  }

  setSeek(view: RCTVideo, value: number) {
    view.seek = value;
  }

  setSource(view: RCTVideo, value: VideoSource) {
    view.source = value;
  }

  setYouboraParams(view: RCTVideo, value: YouboraParams) {
    view.source = value;
  }

  constantsToExport() {
    return { ...resizeModes };
  }
}

export default RCTVideoManager;
