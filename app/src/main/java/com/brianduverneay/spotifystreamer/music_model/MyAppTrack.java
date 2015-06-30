package com.brianduverneay.spotifystreamer.music_model;

/**
 * Created by duvernea on 6/28/15.
 */
public class MyAppTrack {

    private String mTrackName;
    private String mAlbumName;
    private String mAlbumCover;

    public MyAppTrack(String track, String albumname, String albumcover) {
        mTrackName = track;
        mAlbumName = albumname;
        mAlbumCover = albumcover;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public void setTrackName(String mTrackName) {
        this.mTrackName = mTrackName;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String mAlbumName) {
        this.mAlbumName = mAlbumName;
    }

    public String getAlbumCover() {
        return mAlbumCover;
    }

    public void setAlbumCover(String mAlbumCover) {
        this.mAlbumCover = mAlbumCover;
    }
}
