package com.brianduverneay.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.brianduverneay.spotifystreamer.music_model.MyAppArtist;
import com.brianduverneay.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


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
        String imageUrl = appArtist.getImage();
        if (!imageUrl.equals("")) {
            int imageDimen = (int) mContext.getResources().getDimension(R.dimen.imageview_dimen);
            try {
                URL url = new URL(imageUrl);
                Picasso.with(mContext).load(imageUrl).resize(imageDimen, imageDimen).centerCrop().into(imageView);
            } catch (MalformedURLException e) {
                imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_artist_icon));
            }
        }
         else {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_artist_icon));
        }
        return rootView;
    }
}
