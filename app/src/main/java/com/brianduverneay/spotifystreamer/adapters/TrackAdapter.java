package com.brianduverneay.spotifystreamer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.brianduverneay.spotifystreamer.R;
import com.brianduverneay.spotifystreamer.ui.ArtistDetailFragment;
import com.squareup.picasso.Picasso;

public class TrackAdapter extends CursorAdapter {

    private Context mContext;

    public TrackAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.track_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String albumName = cursor.getString(ArtistDetailFragment.COL_ALBUM_NAME);
        String trackName = cursor.getString((ArtistDetailFragment.COL_TRACK_NAME));
        String albumImage = cursor.getString(ArtistDetailFragment.COL_IMAGE_THUMB);

        viewHolder.mAlbumTitleTextView.setText(albumName);
        viewHolder.mSongTitleTextView.setText(trackName);

        if (!albumImage.equals("")) {
            int imageDimen = (int) context.getResources().getDimension(R.dimen.imageview_dimen);
            Picasso.with(context).load(albumImage).resize(imageDimen, imageDimen)
                    .centerCrop().into(viewHolder.mAlbumImage);
        } else {
            viewHolder.mAlbumImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_artist_icon));
        }
    }

    public static class ViewHolder {
        public final TextView mSongTitleTextView;
        public final TextView mAlbumTitleTextView;
        public final ImageView mAlbumImage;

        public ViewHolder(View view) {
            mSongTitleTextView = (TextView) view.findViewById(R.id.track_list_item_songtitle);
            mAlbumTitleTextView = (TextView) view.findViewById(R.id.track_list_item_albumtitle);
            mAlbumImage = (ImageView) view.findViewById(R.id.track_list_item_albumcover);
        }
    }
}
