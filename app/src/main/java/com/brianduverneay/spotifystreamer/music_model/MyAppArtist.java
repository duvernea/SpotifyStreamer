package com.brianduverneay.spotifystreamer.music_model;

/**
 * Created by duvernea on 6/18/15.
 */
public class MyAppArtist {

    private String mName;
    private String mId;
    private String mImage;

    public MyAppArtist(String mName, String mId, String mImage) {
        this.mName = mName;
        this.mId = mId;
        this.mImage = mImage;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getImage() {
        return mImage;
    }

    public void setImage(String mImage) {
        this.mImage = mImage;
    }
}
