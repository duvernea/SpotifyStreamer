package com.brianduverneay.spotifystreamer.ui;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.brianduverneay.spotifystreamer.data.MusicContract;
import com.brianduverneay.spotifystreamer.data.MusicDbHelper;
import com.brianduverneay.spotifystreamer.music_model.MyAppTrack;
import com.brianduverneay.spotifystreamer.R;
import com.brianduverneay.spotifystreamer.adapters.TrackAdapter;
import com.brianduverneay.spotifystreamer.services.MyAppPlayerService;

import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

public class ArtistDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArtistDetail.class.getSimpleName();
    public static final String SELECTED_TRACK_POSITION = "POSITION";
    private static final String US_COUNTRY_CODE = "US"; // ISO 3166-1 alpha-2 country code
    private static final String QUERY_COUNTRY = "country";

    public static final int TRACKS_LOADER = 0;
    private static final int LOADER_ID_NETWORK = 11;

    boolean mIsLargeScreen;
    private Context mContext;
    private SpotifyApi mApi;
    private SpotifyService mSpotify;
    private TrackAdapter mTrackAdapter;
    private Toast mToast;
    //private String mArtistName;
    //private String mArtistId;

    int mPosition = 1;

    private ListView mListView;

    CursorLoader mCursorLoader;

    // Mediaplayer service variables
    public MyAppPlayerService myAppPlayerService;
    private ServiceConnection mConnection;
    private Intent mPlayIntent;
    private boolean mPlayerBind = false;

    private static final String[] FORECAST_COLUMNS = {

            MusicContract.SearchTrackEntry.TABLE_NAME + "." + MusicContract.SearchTrackEntry._ID,
            MusicContract.SearchTrackEntry.COLUMN_ARTIST_NAME,
            MusicContract.SearchTrackEntry.COLUMN_TRACK_SPOTIFY_ID,
            MusicContract.SearchTrackEntry.COLUMN_TRACK_NAME,
            MusicContract.SearchTrackEntry.COLUMN_ALBUM_NAME,
            MusicContract.SearchTrackEntry.COLUMN_DURATION,
            MusicContract.SearchTrackEntry.COLUMN_IMAGE_THUMB,
            MusicContract.SearchTrackEntry.COLUMN_IMAGE_LARGE,
            MusicContract.SearchTrackEntry.COLUMN_PREVIEW_URI
    };

    public static final int COL_TRACK_ID = 0;
    public static final int COL_ARTIST_NAME = 1;
    public static final int COL_SPOTIFY_ID = 2;
    public static final int COL_TRACK_NAME = 3;
    public static final int COL_ALBUM_NAME = 4;
    public static final int COL_DURATION = 5;
    public static final int COL_IMAGE_THUMB = 6;
    public static final int COL_IMAGE_LARGE = 7;
    public static final int COL_PREVIEW_URI = 8;

    private ProgressBar mProgressBar;

    public ArtistDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_artist_detail, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar_artist_detail);

        mProgressBar.setVisibility(View.INVISIBLE);

        mIsLargeScreen = getResources().getBoolean(R.bool.large_layout);

        getLoaderManager().initLoader(TRACKS_LOADER, null, this);

        myAppPlayerService = new MyAppPlayerService();

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyAppPlayerService.MusicBinder binder = (MyAppPlayerService.MusicBinder) service;
                myAppPlayerService = binder.getService();
                mPlayerBind = true;
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mPlayerBind = false;
            }
        };
        Intent intent = new Intent(mContext, MyAppPlayerService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        mTrackAdapter = new TrackAdapter(mContext, null, 0);

        mListView= (ListView) rootView.findViewById(R.id.track_listing);
        mListView.setAdapter(mTrackAdapter);
        mListView.setSelection(mPosition);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                MusicDbHelper helper = new MusicDbHelper(getActivity());

                SQLiteDatabase db = helper.getWritableDatabase();

                final String DELETE_ROWS = "DELETE FROM " + MusicContract.PlayTrackEntry.TABLE_NAME + ";";
                final String COPY_TABLE = "INSERT INTO " + MusicContract.PlayTrackEntry.TABLE_NAME + " SELECT * FROM " +
                        MusicContract.SearchTrackEntry.TABLE_NAME + ";";
                db.execSQL(DELETE_ROWS);
                db.execSQL(COPY_TABLE);
                Uri trackUri = MusicContract.PlayTrackEntry.CONTENT_URI;
                mPosition = position;
                mCursorLoader = new CursorLoader(getActivity(), trackUri, null, null, null, null);
                mCursorLoader.registerListener(LOADER_ID_NETWORK, new Loader.OnLoadCompleteListener<Cursor>() {
                    @Override
                    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
                        Log.d(TAG, "count: " +data.getCount());
                        myAppPlayerService.setCursorData(data);
                        data.moveToPosition(mPosition);
                        FragmentManager fragmentManager = getFragmentManager();
                        MusicPlayerActivityFragment newFragment = new MusicPlayerActivityFragment();
                        if (mIsLargeScreen) {
                            Bundle args = new Bundle();
                            args.putInt(SELECTED_TRACK_POSITION, mPosition);
                            newFragment.setArguments(args);
                            String uri = data.getString(COL_PREVIEW_URI);
                            Uri a = Uri.parse(uri);
                            myAppPlayerService.setContent(a, mPosition);

                            newFragment.show(fragmentManager, MainActivity.DIALOG_TAG);
                            myAppPlayerService.onPlay();
                        } else {
                            Intent intent = new Intent(mContext, MusicPlayerActivity.class);
                            intent.putExtra(SELECTED_TRACK_POSITION, mPosition);
                            String uri = data.getString(COL_PREVIEW_URI);
                            Uri a = Uri.parse(uri);
                            myAppPlayerService.setContent(a, mPosition);
                            myAppPlayerService.onPlay();
                            startActivity(intent);
                        }
                    }
                });
                mCursorLoader.startLoading();
            }
        });
        return rootView;
    }
    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri trackUri = MusicContract.SearchTrackEntry.CONTENT_URI;
        return new android.content.CursorLoader(getActivity(), trackUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        mTrackAdapter.swapCursor(data);
        mTrackAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mTrackAdapter.swapCursor(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayerBind) {
            mContext.unbindService(mConnection);
            mConnection=null;
            mPlayerBind=false;
        }
    }
}
