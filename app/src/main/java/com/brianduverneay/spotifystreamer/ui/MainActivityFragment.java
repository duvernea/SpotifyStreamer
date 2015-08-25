package com.brianduverneay.spotifystreamer.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brianduverneay.spotifystreamer.music_model.MyAppArtist;
import com.brianduverneay.spotifystreamer.R;
import com.brianduverneay.spotifystreamer.adapters.ArtistAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SEARCH_TEXT = "SEARCH_TEXT";
    private static final long SEARCH_WAIT_TIME = 500; // milliseconds

    private static final String ARTIST_SCROLL_POSITION = "ARTIST_SCROLL_POSITION";


    private EditText mSearchText;
    private ListView mListView;
    private ArtistAdapter mArtistAdapter;

    private int mScrollPosition;
    private Toast mToast;
    private String mUserTextSearchEntry;
    private Context mContext;

    private ProgressBar mProgressBar;

    private SpotifyService mSpotify;
    private SpotifyApi mApi;

    public MainActivityFragment() {
    }
    public interface Callback {
        public void onItemSelected(String artistId, String artistName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // if rotated, restore the scroll position, otherwise set to 0
        if (savedInstanceState ==null) {
            mScrollPosition=0;
        }
        else {
            mScrollPosition = savedInstanceState.getInt(ARTIST_SCROLL_POSITION);
        }

        View rootView = inflater.inflate(R.layout.fragment_main, container);

        // Set up the Adapter and attach it to the ListView
        mContext = getActivity();
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        List<MyAppArtist> myAppArtist = new ArrayList<MyAppArtist>();
        mArtistAdapter = new ArtistAdapter (mContext, myAppArtist);
        mListView = (ListView) rootView.findViewById(R.id.artist_listing);
        mListView.setAdapter(mArtistAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MyAppArtist value = (MyAppArtist) parent.getItemAtPosition(position);
                String artistId = value.getId();
                String artistName = value.getName();

                ((Callback) getActivity())
                        .onItemSelected(artistId, artistName);
            }
        });

        mSearchText = (EditText) rootView.findViewById(R.id.artist_search);
        mSearchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchText.setHint("");
                mSearchText.setCursorVisible(true);
            }
        });
        if (mUserTextSearchEntry == null) {

            mArtistAdapter.clear();
            mArtistAdapter.notifyDataSetChanged();
        }
        mSearchText.addTextChangedListener(new TextWatcher() {
            Timer timer = new Timer();
            long textDelay=SEARCH_WAIT_TIME;
            public void afterTextChanged(final Editable s) {
                mListView.clearFocus();
                if (s.toString().equals(mUserTextSearchEntry)) {
                    mListView.setSelection(mScrollPosition);
                }
                else {
                    mListView.setSelection(-1);
                }
                mUserTextSearchEntry = s.toString();
                if (mUserTextSearchEntry.equals("")) {
                    mArtistAdapter.clear();
                    mSearchText.setHint(R.string.artist_search_hint_text);
                    mSearchText.setCursorVisible(true);
                }
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SearchArtist a = new SearchArtist();

                        if (!mUserTextSearchEntry.equals("")) {
                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.VISIBLE);
                                }
                            });

                            a.execute(mUserTextSearchEntry);
                        }
                    }

                }, textDelay);
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

            }
        });

        if (savedInstanceState != null) {
            mSearchText.setText(savedInstanceState.getString(SEARCH_TEXT));
            Log.d(TAG, "SEARCH NOT NULL: " + savedInstanceState.getString(SEARCH_TEXT));
        }
        Log.d(TAG, "searchtextentry: " + mUserTextSearchEntry);
        return rootView;
    }

    // On rotate, save the search text and scroll position
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(SEARCH_TEXT, mUserTextSearchEntry);
        mScrollPosition = mListView.getFirstVisiblePosition();
        savedInstanceState.putInt(ARTIST_SCROLL_POSITION, mScrollPosition);
    }

    // AsyncTask to search for artists and update the listview
    private class SearchArtist extends AsyncTask<String, Void, ArtistsPager> {

        private boolean exceptionThrown=false;
        private boolean networkConnection=false;

        @Override
        protected ArtistsPager doInBackground(String... params) {

            if (mToast != null) {
                mToast.cancel();
            }
            String searchTerm = params[0];
            Log.d(TAG, "searchterm: " + searchTerm);
            if (searchTerm.length()==0) {
                return null;
            }
            ArtistsPager results;
            List<Artist> artistList;
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                networkConnection=true;
                //network connected
                mApi = new SpotifyApi();
                mSpotify = mApi.getService();


                try {
                    results = mSpotify.searchArtists(searchTerm);
                    artistList = results.artists.items;
                } catch (RetrofitError e) {
                    exceptionThrown = true;
                    return null;
                }
                if (artistList.size() == 0) {
                    return null;
                }
            }
            else {
                networkConnection=false;
                return null;
            }
            return results;
        }

        @Override
        protected void onPostExecute(ArtistsPager a) {
            Log.d(TAG, networkConnection+"");
            super.onPostExecute(a);
            if (mToast != null) {
                mToast.cancel();
            }
            if (!networkConnection) {
                Log.d(TAG, networkConnection+"");
                mToast = Toast.makeText(mContext, getResources().getString(R.string.network_connection_error), Toast.LENGTH_SHORT);
                mToast.show();
                return;
            }
            if (exceptionThrown) {
                mToast = Toast.makeText(mContext, getResources().getString(R.string.spotify_connection_error), Toast.LENGTH_SHORT);
                mToast.show();
                return;
            }

            mArtistAdapter.clear();
            if (a != null) {
                for (Artist artist : a.artists.items) {
                    String image = "";

                    if (!artist.images.isEmpty()) {
                        // 0 = large, 1 = med, 2 = small...
                        image = artist.images.get(artist.images.size()-2).url;
                    }
                    MyAppArtist myArtist = new MyAppArtist(artist.name, artist.id, image);
                    mArtistAdapter.add(myArtist);
                    mListView.setSelection(mScrollPosition);
                    mArtistAdapter.notifyDataSetChanged();

                }
            }
            else {
                mToast = Toast.makeText(getActivity(), mContext.getString(R.string.toast_no_artists_found), Toast.LENGTH_SHORT);
                mToast.show();
            }
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
