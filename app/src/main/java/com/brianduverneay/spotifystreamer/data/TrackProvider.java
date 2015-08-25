package com.brianduverneay.spotifystreamer.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by duvernea on 7/31/15.
 */
public class TrackProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MusicDbHelper mOpenHelper;

    static final int SEARCH_TRACKS = 100;
    static final int PLAY_TRACKS = 200;

    private static final SQLiteQueryBuilder searchMusicQueryBuilder;
    private static final SQLiteQueryBuilder playMusicQueryBuilder;

    static {
        searchMusicQueryBuilder = new SQLiteQueryBuilder();
        searchMusicQueryBuilder.setTables(MusicContract.SearchTrackEntry.TABLE_NAME);
        playMusicQueryBuilder = new SQLiteQueryBuilder();
        playMusicQueryBuilder.setTables(MusicContract.PlayTrackEntry.TABLE_NAME);

    }
    String[] selectionArgs;
    String selection;

    private Cursor getTracksByArtist(Uri uri, String[] projection, String sortOrder) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SEARCH_TRACKS: {
                return searchMusicQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            }
            case PLAY_TRACKS: {
                return playMusicQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            }
            default:
                return null;
        }
    }
    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MusicContract.CONTENT_AUTHORITY, MusicContract.SearchTrackEntry.TABLE_NAME, SEARCH_TRACKS);
        uriMatcher.addURI(MusicContract.CONTENT_AUTHORITY, MusicContract.PlayTrackEntry.TABLE_NAME, PLAY_TRACKS);
        return uriMatcher;
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new MusicDbHelper(getContext());
        return true;
    }

    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        if (match == SEARCH_TRACKS || match==PLAY_TRACKS) {
            return MusicContract.SearchTrackEntry.CONTENT_DIR_TYPE;
        }
        else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case SEARCH_TRACKS: {
                long _id = db.insert(MusicContract.SearchTrackEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MusicContract.SearchTrackEntry.buildTrackUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PLAY_TRACKS: {
                long _id = db.insert(MusicContract.PlayTrackEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MusicContract.PlayTrackEntry.buildTrackUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        String path = uri.getLastPathSegment();
        switch (sUriMatcher.match(uri)) {
            case SEARCH_TRACKS: {
                retCursor = getTracksByArtist(uri, projection, sortOrder);
                retCursor.setNotificationUri(getContext().getContentResolver(), MusicContract.SearchTrackEntry.CONTENT_URI);
                break;
            }
            case PLAY_TRACKS: {
                retCursor = getTracksByArtist(uri, projection, sortOrder);
                retCursor.setNotificationUri(getContext().getContentResolver(), MusicContract.PlayTrackEntry.CONTENT_URI);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return retCursor;
    }


    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int numRows = 0;
        if (selection == null) selection = "1";
        switch (match) {
            case SEARCH_TRACKS: {
                numRows = db.delete(MusicContract.SearchTrackEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PLAY_TRACKS: {
                numRows = db.delete(MusicContract.PlayTrackEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        if (numRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRows;
    }
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        int numRows = 0;
        switch (match) {
            case SEARCH_TRACKS: {
                numRows = db.update(MusicContract.SearchTrackEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case PLAY_TRACKS: {
                numRows = db.update(MusicContract.PlayTrackEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

            }
        if (numRows !=0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRows;
    }
}
