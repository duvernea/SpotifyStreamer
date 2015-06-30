package com.brianduverneay.spotifystreamer.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.brianduverneay.spotifystreamer.music_model.MyAppArtist;
import com.brianduverneay.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by duvernea on 6/27/15.
 */
public class ArtistAdapter extends ArrayAdapter<MyAppArtist> {

    private Context mContext;

    public ArtistAdapter(Context context, List<MyAppArtist> appArtists) {
        super(context, 0, appArtists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mContext = getContext();
        MyAppArtist appArtist = getItem(position);
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.artist_list_item, parent, false);

        TextView textView = (TextView) rootView.findViewById(R.id.artist_list_item_textview);
        textView.setText(appArtist.getName());

        ImageView imageView = (ImageView) rootView.findViewById(R.id.artist_list_item_imageview);
        if (!appArtist.getImage().equals("")) {
            int imageDimen = (int) mContext.getResources().getDimension(R.dimen.imageview_dimen);
            Picasso.with(mContext).load(appArtist.getImage()).resize(imageDimen, imageDimen).centerCrop().into(imageView);
        } else {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_artist_icon));
        }
        return rootView;
    }
}
