// @flow

export type VideoSource = {
  uri: string,
  type: string,
  mainVer: number,
  patchVer: number,
  isNetwork: boolean,
  isAsset: boolean,
};

export type YouboraParams = {
  accountCode: string,
  username: string,
  transactionCode: string,
  isLive: boolean,
  parseCdnNode: boolean,
  enabled: boolean,
  title: string,
  program: string,
  tvShow: string,
  season: string,
  contentType: string,
  contentId: string,
  playbackType: string,
  contentDuration: number,
  contentDrm: boolean,
  contentResource: string,
  contentGenre: string,
  contentLanguage: string,
  contentChannels: string,
  contentStreamingProtocol: string,
  contentCustomDimension1: string,
  contentCustomDimension2: string,
  userType: string,
  contentMetadata: {
    genre: string,
    language: string,
    channel: string,
    year: string,
    cast: string,
    director: string,
    owner: string,
    content_id: string
  }
};
