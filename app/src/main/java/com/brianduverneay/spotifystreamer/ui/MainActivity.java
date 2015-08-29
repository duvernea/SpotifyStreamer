package com.brianduverneay.spotifystreamer.ui;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.widget.LinearLayout;
import android.support.v7.widget.ShareActionProvider;
import android.widget.Toast;

import com.brianduverneay.spotifystreamer.R;
import com.brianduverneay.spotifystreamer.data.MusicContract;
import com.brianduverneay.spotifystreamer.music_model.MyAppTrack;
import com.brianduverneay.spotifystreamer.services.MyAppPlayerService;

import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


public class MainActivity extends ActionBarActivity implements MainActivityFragment.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ARTISTDETAILFRAGMENT_TAG = "ADF_TAG";
    public static final String NOWPLAYING_TAG = "NOWPLAYING_TAG";
    public static final String DIALOG_TAG = "dialog";
    public static final String SELECTED_TRACK_POSITION = "POSITION";

    private static final String US_COUNTRY_CODE = "US"; // ISO 3166-1 alpha-2 country code
    private static final String QUERY_COUNTRY = "country";

    private boolean mTwoPane = false;
    private boolean mIsLargeScreen;
    private boolean mPlayerBind = true; //currently not accessed, do i need this?

    private Toast mToast;

    private Context mContext;
    private SpotifyApi mApi;
    private SpotifyService mSpotify;

    private String mArtistName;
    private String mArtistId;

    // Mediaplayer service variables
    public MyAppPlayerService myAppPlayerService;
    private ServiceConnection mConnection;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mIsLargeScreen = getResources().getBoolean(R.bool.large_layout);

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
        Intent intent = new Intent(this, MyAppPlayerService.class);
        this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if (findViewById(R.id.artist_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.artist_detail_container, new ArtistDetailFragment(), ARTISTDETAILFRAGMENT_TAG)
                        .commit();
            }
            LinearLayout ll = (LinearLayout) this.findViewById(R.id.linear_layout_main);
            ll.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            ll.setDividerDrawable(this.getResources().getDrawable(R.drawable.mydivider));
        }
        else {
            mTwoPane = false;
        }
    }

    @Override
    public void onItemSelected(String artistId, String artistName) {

        mArtistId = artistId;
        mArtistName = artistName;
        TopTracks topTracks = new TopTracks();
        topTracks.execute(mArtistId);

        if (mTwoPane == true) {
            ArtistDetailFragment fragment = new ArtistDetailFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.artist_detail_container, fragment, ARTISTDETAILFRAGMENT_TAG)
                    .commit();
        }
        else {

            Intent intent = new Intent(this, ArtistDetail.class);
            startActivity(intent);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_now_playing) {
            FragmentManager fragmentManager = getFragmentManager();

            MusicPlayerActivityFragment newFragment = new MusicPlayerActivityFragment();
            //fragmentManager.beginTransaction().add(newFragment, NOWPLAYING_TAG);
            // int trackIndex = myAppPlayerService.getTrackIndex();
            if (mIsLargeScreen) {
                Bundle args = new Bundle();
                newFragment.show(fragmentManager, NOWPLAYING_TAG);
            }
            else {
                Intent intent = new Intent(this, MusicPlayerActivity.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean returnvalue = super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_now_playing);
        if (myAppPlayerService.isPlaying() ) {
            item.setVisible(true);
        }
        else if (!myAppPlayerService.isInitialized()) {
            item.setVisible(false);
        }
        else {
            item.setVisible(true);
        }
        return returnvalue;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConnection != null) {
            this.unbindService(mConnection);
        }
    }
    private class TopTracks extends AsyncTask<String, Void, List<Track>> {

        private boolean exceptionThrown=false;
        private boolean networkConnection=false;
        @Override
        protected List<Track> doInBackground(String... params) {

            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            HashMap<String, Object> queryMap = new HashMap<String, Object>();

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            String countryCodePref = sharedPrefs.getString(getString(R.string.pref_country_key), US_COUNTRY_CODE);
            queryMap.put(QUERY_COUNTRY, countryCodePref);
            Tracks results;

            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                networkConnection = true;
                //network connected
                mApi = new SpotifyApi();
                mSpotify = mApi.getService();
                try {
                    results = mSpotify.getArtistTopTrack(params[0], queryMap);
                } catch (RetrofitError e) {

                    e.printStackTrace();
                    exceptionThrown = true;
                    return null;
                }
            }
            else {
                networkConnection=false;
                return null;
            }
            return results.tracks;
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            super.onPostExecute(tracks);
            if (mToast != null) {
                mToast.cancel();
            }
            if (!networkConnection) {
                mToast = Toast.makeText(mContext, getResources().getString(R.string.network_connection_error), Toast.LENGTH_SHORT);
                mToast.show();
                return;
            }
            if (exceptionThrown) {
                mToast = Toast.makeText(mContext, getResources().getString(R.string.spotify_connection_error), Toast.LENGTH_SHORT);
                mToast.show();
                return;
            }
            int numRowsDeleted = mContext.getContentResolver().delete(MusicContract.SearchTrackEntry.CONTENT_URI, null, null);

            if (tracks.size() > 0) {
                for (Track track : tracks) {
                    // Last item in array of images should be smallest, and quickest to load
                    // First item in the array of images is highest quality.
                    // This high quality image could be used for both top tracks fragment and player fragments
                    int i;
                    int numImages = track.album.images.size();

                    String thumbImgUrl = "";
                    String largeImgUrl = "";

                    if (numImages != 0) {
                        //Image imgThumb = track.album.images.get(numImages-1);
                        Image largeImg = track.album.images.get(0);
                        largeImgUrl = largeImg.url;
                        if (numImages>1) {
                            Image thumbImg = track.album.images.get(1);
                            thumbImgUrl = thumbImg.url;
                        } else {
                            Image thumbImg = track.album.images.get(0);
                            thumbImgUrl = thumbImg.url;
                        }
                    }
                    ContentValues trackValues;
                    trackValues = new ContentValues();
                    trackValues.put(MusicContract.SearchTrackEntry.COLUMN_ARTIST_NAME, mArtistName);
                    trackValues.put(MusicContract.SearchTrackEntry.COLUMN_TRACK_SPOTIFY_ID, track.id);
                    trackValues.put(MusicContract.SearchTrackEntry.COLUMN_TRACK_NAME, track.name);
                    trackValues.put(MusicContract.SearchTrackEntry.COLUMN_ALBUM_NAME, track.album.name);

                    trackValues.put(MusicContract.SearchTrackEntry.COLUMN_DURATION, track.duration_ms);
                    trackValues.put(MusicContract.SearchTrackEntry.COLUMN_IMAGE_LARGE, largeImgUrl);
                    trackValues.put(MusicContract.SearchTrackEntry.COLUMN_IMAGE_THUMB, thumbImgUrl);
                    trackValues.put(MusicContract.SearchTrackEntry.COLUMN_PREVIEW_URI, track.preview_url);

                    MyAppTrack myTrack = new MyAppTrack(track.name, track.album.name, thumbImgUrl, track.preview_url, track.id);

                    Uri uri = mContext.getContentResolver().insert(MusicContract.SearchTrackEntry.CONTENT_URI, trackValues);
                    mContext.getContentResolver().notifyChange(uri, null);
                }
            }
            else {
                mToast = Toast.makeText(mContext, mContext.getString(R.string.no_tracks_message), Toast.LENGTH_SHORT);
                mToast.show();
            }
            // mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}

