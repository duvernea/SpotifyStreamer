package com.brianduverneay.spotifystreamer.ui;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
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

import com.brianduverneay.spotifystreamer.R;
import com.brianduverneay.spotifystreamer.services.MyAppPlayerService;


public class MainActivity extends ActionBarActivity implements MainActivityFragment.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ARTISTDETAILFRAGMENT_TAG = "ADF_TAG";
    public static final String NOWPLAYING_TAG = "NOWPLAYING_TAG";
    public static final String DIALOG_TAG = "dialog";
    public static final String SELECTED_TRACK_POSITION = "POSITION";

    private boolean mTwoPane = false;
    private boolean mIsLargeScreen;
    private boolean mPlayerBind = true; //currently not accessed, do i need this?

    // Mediaplayer service variables
    public MyAppPlayerService myAppPlayerService;
    private ServiceConnection mConnection;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mIsLargeScreen = getResources().getBoolean(R.bool.large_layout);

        myAppPlayerService = new MyAppPlayerService();
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected running...");
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

        Log.d(TAG, "Artist Id: " + artistId);
        if (mTwoPane == true) {
            Bundle args = new Bundle();
            args.putString(ArtistDetail.ARTIST_ID, artistId);
            args.putString(ArtistDetail.ARTIST_NAME, artistName);
            ArtistDetailFragment fragment = new ArtistDetailFragment();
            fragment.setArguments(args);
            getFragmentManager().beginTransaction()
                    .replace(R.id.artist_detail_container, fragment, ARTISTDETAILFRAGMENT_TAG)
                    .commit();
        }
        else {

            Intent intent = new Intent(this, ArtistDetail.class);
            Bundle extras = new Bundle();
            extras.putString(ArtistDetail.ARTIST_ID, artistId);
            extras.putString(ArtistDetail.ARTIST_NAME, artistName);
            intent.putExtras(extras);
            startActivity(intent);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.

        // TODO
        // need to add logic to only show now playing when musicplayer is playing

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
        // TODO
        // need to handle both large and small screen
        if (id == R.id.action_now_playing) {
            FragmentManager fragmentManager = getFragmentManager();


            MusicPlayerActivityFragment newFragment = new MusicPlayerActivityFragment();
            //fragmentManager.beginTransaction().add(newFragment, NOWPLAYING_TAG);
            // int trackIndex = myAppPlayerService.getTrackIndex();
            if (mIsLargeScreen) {
                Bundle args = new Bundle();


                // Log.d("MAIN track index: ", trackIndex+"");
                // args.putInt(SELECTED_TRACK_POSITION, trackIndex);
                // newFragment.setArguments(args);
                newFragment.show(fragmentManager, NOWPLAYING_TAG);
            }
            else {
                Intent intent = new Intent(this, MusicPlayerActivity.class);
                // intent.putExtra(SELECTED_TRACK_POSITION, trackIndex);
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
        if (!myAppPlayerService.isInitialized()) {
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
}

