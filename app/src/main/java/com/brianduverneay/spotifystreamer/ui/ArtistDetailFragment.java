package com.brianduverneay.spotifystreamer.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.brianduverneay.spotifystreamer.music_model.MyAppTrack;
import com.brianduverneay.spotifystreamer.R;
import com.brianduverneay.spotifystreamer.adapters.TrackAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistDetailFragment extends Fragment {

    private Context mContext;
    private SpotifyApi mApi;
    private SpotifyService mSpotify;
    private TrackAdapter mTrackAdapter;
    private Toast mToast;

    private static final String US_COUNTRY_CODE = "US"; // ISO 3166-1 alpha-2 country code
    private static final String QUERY_COUNTRY = "country";

    private String TAG = ArtistDetail.class.getSimpleName();

    public ArtistDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_artist_detail, container);
        mContext = getActivity();

        List<MyAppTrack> myAppTrack = new ArrayList<MyAppTrack>();
        mTrackAdapter = new TrackAdapter(mContext, myAppTrack);

        ListView listView = (ListView) rootView.findViewById(R.id.track_listing);
        listView.setAdapter(mTrackAdapter);

        Intent intent = getActivity().getIntent();
        String artistId = intent.getStringExtra(Intent.EXTRA_TEXT);

        TopTracks a = new TopTracks();
        a.execute(artistId);

        return rootView;
    }

    private class TopTracks extends AsyncTask<String, Void, List<Track>> {

        @Override
        protected List<Track> doInBackground(String... params) {
            mApi = new SpotifyApi();
            mSpotify = mApi.getService();
            HashMap<String, Object> queryMap = new HashMap<String, Object>();
            queryMap.put(QUERY_COUNTRY, US_COUNTRY_CODE);
            Tracks results = mSpotify.getArtistTopTrack(params[0], queryMap);

            return results.tracks;
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            super.onPostExecute(tracks);
            if (tracks.size() > 0) {

                mTrackAdapter.clear();
                for (Track track : tracks) {
                    // Last item in array of images should be smallest, and quickest to load
                    int numImages = track.album.images.size();
                    String imgUrl = "";
                    if (numImages != 0) {
                        Image imgThumb = track.album.images.get(numImages-1);
                        imgUrl = imgThumb.url;
                    }
                    MyAppTrack myTrack = new MyAppTrack(track.name, track.album.name, imgUrl);
                    mTrackAdapter.add(myTrack);
                }
            }
            else {
                mToast = Toast.makeText(mContext, mContext.getString(R.string.no_tracks_message), Toast.LENGTH_SHORT);
                mToast.show();
            }
        }
    }
}
