package com.brianduverneay.spotifystreamer.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brianduverneay.spotifystreamer.R;
import com.brianduverneay.spotifystreamer.data.MusicContract;
import com.brianduverneay.spotifystreamer.services.MyAppPlayerService;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

public class MusicPlayerActivityFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    private static final String TAG = MusicPlayerActivityFragment.class.getSimpleName();
    private static final String TRACK_INDEX="TRACK_INDEX";
    private static final int TRACKS_LOADER = 5;
    private final int PREVIEW_DURATION=30000; // all previews are 30000 milliseconds

    private Context mContext;
    android.support.v7.widget.ShareActionProvider mShareActionProvider;

    // Mediaplayer service variables
    public MyAppPlayerService myAppPlayerService;
    private Intent mPlayIntent;
    private boolean mPlayerBind = false;
    private ServiceConnection mConnection;

    private TextView mArtistNameTextView;
    private TextView mAlbumNameTextView;
    private ImageView mAlbumImageView;
    private TextView mDurationTextView;
    private TextView mProgressTextView;
    private TextView mTrackNameTextView;
    private ImageButton mPlayPauseImageView;
    private SeekBar mSeekBar;

    private Runnable mUpdateTrackProgress;

    boolean mIsLargeScreen;

    private int mTrackIndex=0;

    Cursor mCursorData;
    private Handler mHandler;

    public MusicPlayerActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            if (getArguments() != null) {
                mTrackIndex = getArguments().getInt(ArtistDetailFragment.SELECTED_TRACK_POSITION, 1);
            }
        }
        else {
            mTrackIndex = savedInstanceState.getInt(TRACK_INDEX);
        }
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_player, container, false);
        mPlayPauseImageView = (ImageButton) rootView.findViewById(R.id.player_playpause);

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyAppPlayerService.MusicBinder binder = (MyAppPlayerService.MusicBinder) service;
                myAppPlayerService = binder.getService();
                mPlayerBind = true;
                mTrackIndex=myAppPlayerService.getTrackIndex();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mPlayerBind = false;
            }
        };
        Intent serviceIntent = new Intent(getActivity(), MyAppPlayerService.class);

        getActivity().bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        getActivity().startService(serviceIntent);
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(MyAppPlayerService.BROADCAST_ACTION));

        mIsLargeScreen = getResources().getBoolean(R.bool.large_layout);

        Intent intent = getActivity().getIntent();

        mArtistNameTextView = (TextView) rootView.findViewById(R.id.artist_textView);
        mAlbumNameTextView = (TextView) rootView.findViewById(R.id.album_textView);
        mAlbumImageView = (ImageView) rootView.findViewById(R.id.player_album_image);
        mDurationTextView = (TextView) rootView.findViewById(R.id.track_duration);
        mTrackNameTextView = (TextView) rootView.findViewById(R.id.trackName_textView);
        mProgressTextView = (TextView) rootView.findViewById(R.id.track_progress);

        mSeekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        mSeekBar.setMax(PREVIEW_DURATION);
        mContext = getActivity();

        myAppPlayerService = new MyAppPlayerService();

        if  (!mIsLargeScreen) {
            if (savedInstanceState == null) {
                mTrackIndex = intent.getIntExtra(ArtistDetailFragment.SELECTED_TRACK_POSITION, 1);
            }
            else {
                mTrackIndex = savedInstanceState.getInt(TRACK_INDEX);

            }
        }
        myAppPlayerService.setTrackIndex(mTrackIndex);


        mPlayPauseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myAppPlayerService.isPlaying()) {

                }
                if (myAppPlayerService.getStoppedState()) {
                    myAppPlayerService.onPlay();
                    mPlayPauseImageView.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                }
                else {
                    boolean bool = myAppPlayerService.getPauseState();
                    if (!bool) {
                        mPlayPauseImageView.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                        myAppPlayerService.onPause();
                    }
                    if (bool) {
                        mPlayPauseImageView.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                        myAppPlayerService.onPlay();
                    }
                }
            }
        });
        ImageButton backButton = (ImageButton) rootView.findViewById(R.id.player_previous);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mCursorData.moveToPrevious();
                if (mTrackIndex>0) {
                    mPlayPauseImageView.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                    mTrackIndex = mTrackIndex - 1;
                    updateUI(mCursorData);
                    mSeekBar.setProgress(0);
                    mProgressTextView.setText("0:00");
                    myAppPlayerService.previousTrack();
                }
                else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_more_tracks_message), Toast.LENGTH_SHORT).show();
                }
            }
        });
        ImageButton forwardButton = (ImageButton) rootView.findViewById(R.id.player_next);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mCursorData.moveToPrevious();
                Log.d(TAG, "mtrackindex: " + mTrackIndex);
                if (mTrackIndex < mCursorData.getCount()-1) {
                    mPlayPauseImageView.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                    mTrackIndex = mTrackIndex + 1;
                    updateUI(mCursorData);
                    mSeekBar.setProgress(0);
                    mProgressTextView.setText("0:00");
                    myAppPlayerService.nextTrack();
                }
                else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_more_tracks_message), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mHandler= new Handler();
        mUpdateTrackProgress = new Runnable() {
            @Override
            public void run() {
                int currentPosition = myAppPlayerService.getCurrentPosition();
                mSeekBar.setProgress(currentPosition);
                int currentPositionSeconds = (int) Math.floor(currentPosition / 1000);
                String currentPositionSecondsDisplay;
                if (currentPositionSeconds < 10) {
                    currentPositionSecondsDisplay = "0" + currentPositionSeconds;
                } else {
                    currentPositionSecondsDisplay = "" + currentPositionSeconds;
                }
                mProgressTextView.setText("0:" + currentPositionSecondsDisplay);
                mHandler.postDelayed(this, 10);
            }
        };
        mUpdateTrackProgress.run();

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mSeekBar.setProgress(progress);
                    myAppPlayerService.setTrackPosition(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(TRACKS_LOADER, null, this);
        if (myAppPlayerService.getTrackIndex() != -1) {
            mTrackIndex = myAppPlayerService.getTrackIndex();
            if (mCursorData != null) {
                updateUI(mCursorData);
            }
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        if (broadcastReceiver != null) {
            try {
                getActivity().unregisterReceiver(broadcastReceiver);
                broadcastReceiver=null;
            }
            catch (IllegalArgumentException iae) {
                iae.printStackTrace();
            }
        }
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(TRACK_INDEX, mTrackIndex);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri trackUri = MusicContract.PlayTrackEntry.CONTENT_URI;
        return new android.content.CursorLoader(getActivity(), trackUri, null, null, null, null);

    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        // mTrackAdapter.swapCursor(data);
        // mListView.setSelection(mPosition);
        mCursorData = data;
        updateUI(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        // mTrackAdapter.swapCursor(null);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConnection != null) {
            getActivity().unbindService(mConnection);
            Intent intent = new Intent(getActivity(), MyAppPlayerService.class);
            getActivity().stopService(intent);
            mPlayerBind=false;
            mHandler.removeCallbacks(mUpdateTrackProgress);
        }
    }
    public void updateUI(Cursor data) {
        mCursorData.moveToPosition(mTrackIndex);

        data.moveToPosition(mTrackIndex);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        } else {
            Log.d(TAG, "Share Action Provider is null?");
        }
        String artistName = data.getString(ArtistDetailFragment.COL_ARTIST_NAME);
        String albumName = data.getString(ArtistDetailFragment.COL_ALBUM_NAME);
        String albumImage = data.getString(ArtistDetailFragment.COL_IMAGE_LARGE);
        String previewString = data.getString(ArtistDetailFragment.COL_PREVIEW_URI);
        String trackName = data.getString(ArtistDetailFragment.COL_TRACK_NAME);
        Uri previewUri = Uri.parse(previewString);
        mArtistNameTextView.setText(artistName);
        mAlbumNameTextView.setText(albumName);
        mTrackNameTextView.setText(trackName);
        mDurationTextView.setText("0:30");

        if (!albumImage.equals("")) {
            int imageDimen;
            try {
                URL url = new URL(albumImage);
                if (mIsLargeScreen) {
                    imageDimen = (int) getResources().getDimension(R.dimen.imageview_dimen_player_large);
                    mAlbumImageView.getLayoutParams().height = imageDimen;
                    mAlbumImageView.getLayoutParams().width = imageDimen;
                }
                else {
                    imageDimen = (int) mContext.getResources().getDimension(R.dimen.imageview_dimen_player);
                }

                Picasso.with(mContext).load(albumImage).resize(imageDimen, imageDimen).centerCrop().into(mAlbumImageView);
            } catch (MalformedURLException e) {
                mAlbumImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_artist_icon));
            }
        }

    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPlayPauseImageView.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
            String imgString = mCursorData.getString(ArtistDetailFragment.COL_PREVIEW_URI);
            Uri imgUrl = Uri.parse(imgString);
            myAppPlayerService.setContent(imgUrl, mTrackIndex);
        }
    };
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_music_player, menu);
        if (!mIsLargeScreen) {
            MenuItem menuItem = menu.findItem(R.id.action_share);
            mShareActionProvider =
                    (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent());
            } else {
                Log.d(TAG, "Share Action Provider is null?");
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        if (mCursorData != null) {
            String shareString = mCursorData.getString(ArtistDetailFragment.COL_ARTIST_NAME) + " - \"" +
                    mCursorData.getString(ArtistDetailFragment.COL_TRACK_NAME) + "\" - " +
                    mCursorData.getString(ArtistDetailFragment.COL_PREVIEW_URI);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareString);
            return shareIntent;
        }
        else {
            return null;
        }
    }
}