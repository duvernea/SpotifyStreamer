package com.brianduverneay.spotifystreamer.music_model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by duvernea on 6/18/15.
 */
public class MyAppArtist implements Parcelable {

    private String mName;
    private String mId;
    private String mImage;

    public MyAppArtist(String mName, String mId, String mImage) {
        this.mName = mName;
        this.mId = mId;
        this.mImage = mImage;
    }
    private MyAppArtist(Parcel in) {
        super();
        mName = in.readString();
        mId = in.readString();
        mImage = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }
    public String toString() {
        return mName + "--" + mId + "--" + mImage;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mId);
        dest.writeString(mImage);
    }
    public final Parcelable.Creator<MyAppArtist> CREATOR = new Parcelable.Creator<MyAppArtist>() {

        @Override
        public MyAppArtist createFromParcel(Parcel source) {
            return new MyAppArtist(source);
        }

        @Override
        public MyAppArtist[] newArray(int size) {
            return new MyAppArtist[size];
        }
    };
}
