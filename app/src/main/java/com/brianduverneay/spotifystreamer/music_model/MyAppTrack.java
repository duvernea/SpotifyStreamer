package com.brianduverneay.spotifystreamer.music_model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by duvernea on 6/28/15.
 */
public class MyAppTrack implements Parcelable {

    private String mTrackName;
    private String mAlbumName;
    private String mAlbumCoverThumb;
    private String mTrackId;
    private String mPreviewUrl;

    public MyAppTrack(String track, String albumname, String albumcover, String url, String trackId) {
        mTrackName = track;
        mAlbumName = albumname;
        mAlbumCoverThumb = albumcover;
        mPreviewUrl = url;
        mTrackId = trackId;

    }

    public String getTrackName() {
        return mTrackName;
    }

    public void setTrackName(String mTrackName) {
        this.mTrackName = mTrackName;
    }

    public String getPreviewUrl() {
        return mPreviewUrl;
    }

    public void setPreviewUrl(String mPreviewUrl) {
        this.mPreviewUrl = mPreviewUrl;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String mAlbumName) {
        this.mAlbumName = mAlbumName;
    }

    public String getAlbumCoverThumb() {
        return mAlbumCoverThumb;
    }

    public void setAlbumCoverThumb(String mAlbumCover) {
        this.mAlbumCoverThumb = mAlbumCover;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTrackId);

    }
}
