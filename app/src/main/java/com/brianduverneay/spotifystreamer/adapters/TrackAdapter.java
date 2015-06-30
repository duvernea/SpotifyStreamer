package com.brianduverneay.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.brianduverneay.spotifystreamer.music_model.MyAppTrack;
import com.brianduverneay.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by duvernea on 6/28/15.
 */
public class TrackAdapter extends ArrayAdapter<MyAppTrack> {

    private Context mContext;

    public TrackAdapter(Context context, List<MyAppTrack> appTracks) {
        super(context, 0, appTracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        mContext = getContext();
        MyAppTrack appTrack = getItem(position);
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.track_list_item, parent, false);

        TextView songTitleTextView = (TextView) rootView.findViewById(R.id.track_list_item_songtitle);
        songTitleTextView.setText(appTrack.getTrackName());

        TextView albumTitleTextView = (TextView) rootView.findViewById(R.id.track_list_item_albumtitle);
        albumTitleTextView.setText(appTrack.getAlbumName());


        ImageView imageView = (ImageView) rootView.findViewById(R.id.track_list_item_albumcover);

        if (!appTrack.getAlbumCover().equals("")) {
            int imageDimen = (int) mContext.getResources().getDimension(R.dimen.imageview_dimen);
            Picasso.with(mContext).load(appTrack.getAlbumCover()).resize(imageDimen, imageDimen).centerCrop().into(imageView);
        } else {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_artist_icon));
        }
        return rootView;
    }
}
