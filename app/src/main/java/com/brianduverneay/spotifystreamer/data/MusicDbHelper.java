package com.brianduverneay.spotifystreamer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by duvernea on 7/31/15.
 */
public class MusicDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "music.db";

    public MusicDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_SEARCHTRACKS_TABLE = "CREATE TABLE " + MusicContract.SearchTrackEntry.TABLE_NAME + " (" +
                MusicContract.SearchTrackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MusicContract.SearchTrackEntry.COLUMN_ARTIST_NAME + " TEXT NOT NULL, " +
                MusicContract.SearchTrackEntry.COLUMN_TRACK_SPOTIFY_ID + " TEXT NOT NULL, " +
                MusicContract.SearchTrackEntry.COLUMN_TRACK_NAME + " TEXT NOT NULL, " +
                MusicContract.SearchTrackEntry.COLUMN_ALBUM_NAME + " TEXT NOT NULL, " +
                MusicContract.SearchTrackEntry.COLUMN_DURATION + " INTEGER NOT NULL, " +
                MusicContract.SearchTrackEntry.COLUMN_IMAGE_THUMB + " STRING NOT NULL, " +
                MusicContract.SearchTrackEntry.COLUMN_IMAGE_LARGE + " STRING NOT NULL, " +
                MusicContract.SearchTrackEntry.COLUMN_PREVIEW_URI + " STRING NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_SEARCHTRACKS_TABLE);

        final String SQL_CREATE_PLAYTRACKS_TABLE = "CREATE TABLE " + MusicContract.PlayTrackEntry.TABLE_NAME + " (" +
                MusicContract.PlayTrackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MusicContract.PlayTrackEntry.COLUMN_ARTIST_NAME + " TEXT NOT NULL, " +
                MusicContract.PlayTrackEntry.COLUMN_TRACK_SPOTIFY_ID + " TEXT NOT NULL, " +
                MusicContract.PlayTrackEntry.COLUMN_TRACK_NAME + " TEXT NOT NULL, " +
                MusicContract.PlayTrackEntry.COLUMN_ALBUM_NAME + " TEXT NOT NULL, " +
                MusicContract.PlayTrackEntry.COLUMN_DURATION + " INTEGER NOT NULL, " +
                MusicContract.PlayTrackEntry.COLUMN_IMAGE_THUMB + " STRING NOT NULL, " +
                MusicContract.PlayTrackEntry.COLUMN_IMAGE_LARGE + " STRING NOT NULL, " +
                MusicContract.PlayTrackEntry.COLUMN_PREVIEW_URI + " STRING NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_PLAYTRACKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MusicContract.SearchTrackEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MusicContract.PlayTrackEntry.TABLE_NAME);
        onCreate(db);
    }
}
