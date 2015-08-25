package com.brianduverneay.spotifystreamer.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by duvernea on 7/31/15.
 */
public class MusicContract {

    public static final String CONTENT_AUTHORITY = "com.brianduverneay.spotifystreamer";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_SEARCH_TRACKS = "searchtracks";
    public static final String PATH_PLAY_TRACKS = "playtracks";

    public static final class SearchTrackEntry implements BaseColumns {

        public static final String TABLE_NAME = "searchtracks";

        public static final String COLUMN_ARTIST_NAME = "artist_name";
        public static final String COLUMN_TRACK_NAME = "track_name";
        public static final String COLUMN_TRACK_SPOTIFY_ID = "track_spotify_id";
        public static final String COLUMN_ALBUM_NAME = "album_name";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_IMAGE_THUMB = "image_thumb";
        public static final String COLUMN_IMAGE_LARGE = "image_large";
        public static final String COLUMN_PREVIEW_URI = "preview_uri";

        public static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_TRACKS).build();

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEARCH_TRACKS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEARCH_TRACKS;

        public static Uri buildTrackUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class PlayTrackEntry implements BaseColumns {

        public static final String TABLE_NAME = "playtracks";

        public static final String COLUMN_ARTIST_NAME = "artist_name";
        public static final String COLUMN_TRACK_NAME = "track_name";
        public static final String COLUMN_TRACK_SPOTIFY_ID = "track_spotify_id";
        public static final String COLUMN_ALBUM_NAME = "album_name";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_IMAGE_THUMB = "image_thumb";
        public static final String COLUMN_IMAGE_LARGE = "image_large";
        public static final String COLUMN_PREVIEW_URI = "preview_uri";

        public static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAY_TRACKS).build();

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLAY_TRACKS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLAY_TRACKS;

        public static Uri buildTrackUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
