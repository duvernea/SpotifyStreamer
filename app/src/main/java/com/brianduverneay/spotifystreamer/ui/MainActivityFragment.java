package com.brianduverneay.spotifystreamer.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.brianduverneay.spotifystreamer.music_model.MyAppArtist;
import com.brianduverneay.spotifystreamer.R;
import com.brianduverneay.spotifystreamer.adapters.ArtistAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArtistAdapter mArtistAdapter;
    private EditText mSearchText;
    private Toast mToast;
    private SpotifyApi mApi;
    private Context mContext;
    private SpotifyService mSpotify;

    private static final String TAG = MainActivity.class.getSimpleName();

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container);

        // Set up the Adapter and attach it to the ListView
        mContext = getActivity();

        List<MyAppArtist> myAppArtist = new ArrayList<MyAppArtist>();
        mArtistAdapter = new ArtistAdapter (mContext, myAppArtist);
        ListView listView = (ListView) rootView.findViewById(R.id.artist_listing);
        listView.setAdapter(mArtistAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ArtistDetail.class);
                MyAppArtist value = (MyAppArtist) parent.getItemAtPosition(position);
                String artistId = value.getId();
                intent.putExtra(Intent.EXTRA_TEXT, artistId);
                startActivity(intent);
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

        mSearchText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                SearchArtist a = new SearchArtist();
                a.execute(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

            }
        });
        
        return rootView;
    }
    private class SearchArtist extends AsyncTask<String, Void, ArtistsPager> {

        @Override
        protected ArtistsPager doInBackground(String... params) {
            if (mToast != null) {
                mToast.cancel();
            }
            String searchTerm = params[0];
            if (searchTerm.length()==0) {
                return null;
            }
            mApi = new SpotifyApi();
            mSpotify = mApi.getService();
            ArtistsPager results = mSpotify.searchArtists(searchTerm);
            List<Artist>  artistList = results.artists.items;
            if (artistList.size() == 0) {
                return null;
            }
            return results;
        }

        @Override
        protected void onPostExecute(ArtistsPager a) {
            super.onPostExecute(a);
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
                }
            }
            else {
                mToast = Toast.makeText(getActivity(), mContext.getString(R.string.toast_no_artists_found), Toast.LENGTH_SHORT);
                mToast.show();
            }
        }
    }
}
