package com.brianduverneay.spotifystreamer.ui;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.brianduverneay.spotifystreamer.R;
import com.brianduverneay.spotifystreamer.services.MyAppPlayerService;


public class ArtistDetail extends ActionBarActivity {

    private static final String TAG = ArtistDetail.class.getSimpleName();

    public static final String ARTIST_ID = "ARIST_ID";
    public static final String ARTIST_NAME = "ARIST_NAME";

    private String mArtistName;
    private String mArtistId;

    private boolean mIsLargeScreen;

    // Mediaplayer service variables
    public MyAppPlayerService myAppPlayerService;
    private ServiceConnection mConnection;
    private boolean mPlayerBind = true; //currently not accessed, do i need this?

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsLargeScreen = getResources().getBoolean(R.bool.large_layout);
        setContentView(R.layout.activity_artist_detail);

        myAppPlayerService = new MyAppPlayerService();
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyAppPlayerService.MusicBinder binder = (MyAppPlayerService.MusicBinder) service;
                myAppPlayerService = binder.getService();
                mPlayerBind = true;
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mPlayerBind = false;
            }
        };
        Intent playerIntent = new Intent(this, MyAppPlayerService.class);
        this.bindService(playerIntent, mConnection, Context.BIND_AUTO_CREATE);

        Intent intent;
        Bundle arguments = new Bundle();
        if (savedInstanceState == null) {
            intent = getIntent();
            arguments = new Bundle();
            mArtistId = intent.getStringExtra(ArtistDetail.ARTIST_ID);
            mArtistName = intent.getStringExtra(ARTIST_NAME);
        }
        else {
            mArtistId = savedInstanceState.getString(ARTIST_ID);
            mArtistName = savedInstanceState.getString(ARTIST_NAME);
        }

            arguments.putString(ARTIST_ID, mArtistId);
            arguments.putString(ARTIST_NAME, mArtistName);

            ArtistDetailFragment fragment = new ArtistDetailFragment();
            fragment.setArguments(arguments);

            getFragmentManager().beginTransaction()
                    .replace(R.id.artist_detail_container, fragment)
                    .commit();
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(ARTIST_ID, mArtistId);
        savedInstanceState.putString(ARTIST_NAME, mArtistName);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_artist_detail, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        // need to handle both large and small screen
        if (id == R.id.action_now_playing) {
            FragmentManager fragmentManager = getFragmentManager();

            MusicPlayerActivityFragment newFragment = new MusicPlayerActivityFragment();
            //fragmentManager.beginTransaction().add(newFragment, NOWPLAYING_TAG);
            // int trackIndex = myAppPlayerService.getTrackIndex();
            if (mIsLargeScreen) {
                Bundle args = new Bundle();
                newFragment.show(fragmentManager, MainActivity.NOWPLAYING_TAG);
            }
            else {
                Intent intent = new Intent(this, MusicPlayerActivity.class);
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
        if (mPlayerBind) {
            this.unbindService(mConnection);
            mConnection=null;
            mPlayerBind=false;
        }
    }
}
