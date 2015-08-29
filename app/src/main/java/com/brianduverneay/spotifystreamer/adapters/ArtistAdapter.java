package com.brianduverneay.spotifystreamer.adapters;

import android.content.Context;
import android.os.Parcelable;
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


public class ArtistAdapter extends ArrayAdapter<MyAppArtist>  {

    private Context mContext;

    public ArtistAdapter(Context context, List<MyAppArtist> appArtists) {
        super(context, 0, appArtists);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mContext = getContext();
        MyAppArtist appArtist = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.artist_list_item, parent, false);
            holder = new ViewHolder();
            holder.artistName = (TextView) convertView.findViewById(R.id.artist_list_item_textview);
            holder.artistImage = (ImageView) convertView.findViewById(R.id.artist_list_item_imageview);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.artistName.setText(appArtist.getName());
        String imageUrl = appArtist.getImage();
        if (!imageUrl.equals("")) {
            int imageDimen = (int) mContext.getResources().getDimension(R.dimen.imageview_dimen);
            try {
                URL url = new URL(imageUrl);
                Picasso.with(mContext).load(imageUrl).resize(imageDimen, imageDimen).centerCrop().into(holder.artistImage);
            } catch (MalformedURLException e) {
                holder.artistImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_artist_icon));
            }
        }
         else {
            holder.artistImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_artist_icon));
        }
        return convertView;
    }
    static class ViewHolder {
        TextView artistName;
        ImageView artistImage;
    }
}
