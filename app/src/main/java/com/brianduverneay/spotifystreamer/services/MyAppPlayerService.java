package com.brianduverneay.spotifystreamer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.brianduverneay.spotifystreamer.R;
import com.brianduverneay.spotifystreamer.data.MusicContract;
import com.brianduverneay.spotifystreamer.music_model.MyAppTrack;
import com.brianduverneay.spotifystreamer.ui.ArtistDetailFragment;
import com.brianduverneay.spotifystreamer.ui.MainActivity;
import com.brianduverneay.spotifystreamer.ui.MusicPlayerActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by duvernea on 7/26/15.
 */
public class MyAppPlayerService extends Service implements Loader.OnLoadCompleteListener<Cursor> {

    public static final String BROADCAST_ACTION = "com.brianduverneay.spotifystreamer.services.MyAppPlayerService";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    private static final String TAG = MyAppPlayerService.class.getSimpleName();
    private static final String MEDIA_SESSION_TAG = "media_session";

    private static final int SERVICE_NOTIFICATION_ID =100;
    private static final int LOADER_ID_NETWORK = 10;

    private MediaSessionManager mManager;
    private MediaSession mSession;
    public static MediaPlayer mediaPlayer;
    private MediaSession.Token mToken;
    private Target mTarget;

    private ArrayList<MyAppTrack> mTracks;

    private Context mContext;

    private Boolean mStatePause=false;
    private Boolean mStateStopped=true;
    private Boolean mInitialized=false;
    private Boolean mNotificationsDisplayed=false;

    Bitmap mBitmapAlbumImage;
    String mArtistName;
    String mAlbumImage;
    String mTrackName;

    private int mTrackIndex;  // 0-9, which track is selected and loaded

    private Intent mStopIntent;

    private CursorLoader mCursorLoader;
    private Cursor mCursorData;

    private final IBinder mBinder = new MusicBinder();

    @Override
    public void onCreate() {

        mContext=this;
        super.onCreate();
        Uri trackUri = MusicContract.PlayTrackEntry.CONTENT_URI;
        mCursorLoader = new CursorLoader(this, trackUri, null, null, null, null);
        mCursorLoader.registerListener(LOADER_ID_NETWORK, this);
        mCursorLoader.startLoading();

        this.registerReceiver(broadcastReceiver, new IntentFilter(this.BROADCAST_ACTION));

        if( mediaPlayer == null ) {
            mStopIntent = new Intent(BROADCAST_ACTION);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mContext = this;

        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String displayNotificationsKey=this.getString(R.string.pref_enable_notifications_key);
        prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            }
        });
        boolean displayNotifications =
                prefs.getBoolean(displayNotificationsKey, Boolean.parseBoolean(this.getString(R.string.pref_enable_notifications_default)));
        mNotificationsDisplayed =
                prefs.getBoolean(displayNotificationsKey, Boolean.parseBoolean(this.getString(R.string.pref_enable_notifications_default)));
    }

    private Notification.Action buildNotificationAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MyAppPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    private void buildNotification(final String action) {

        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mBitmapAlbumImage = bitmap;
                Intent resultIntent = new Intent(mContext, MusicPlayerActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);

                stackBuilder.addParentStack(MusicPlayerActivity.class);
                stackBuilder.addNextIntent(resultIntent);

                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.MediaStyle style = new Notification.MediaStyle();
                style.setMediaSession(mToken);

                Notification.Builder builder = new Notification.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_audiotrack_white_36dp)
                        .setContentTitle(mArtistName)
                        .setContentText(mTrackName)
                        //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.no_artist_icon))
                        .setLargeIcon(mBitmapAlbumImage)
                        .setStyle(style);

                builder.setContentIntent(resultPendingIntent);

                builder.addAction(buildNotificationAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
                if (action.equals(ACTION_PAUSE)) {
                    builder.addAction(buildNotificationAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                } else {
                    builder.addAction(buildNotificationAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
                }
                builder.addAction(buildNotificationAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
                style.setShowActionsInCompactView(0, 1, 2);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(SERVICE_NOTIFICATION_ID, builder.build());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mSession = new MediaSession(mContext, MEDIA_SESSION_TAG);
        mToken = mSession.getSessionToken();
            String action = intent.getAction();

        if (intent.getAction() != null) {
             if (action.equalsIgnoreCase(ACTION_PLAY))  {
                 onPlay();
             }
            if (action.equalsIgnoreCase(ACTION_PAUSE)) {
                onPause();
            }
            if (action.equalsIgnoreCase(ACTION_NEXT)) {
                nextTrack();
            }
            if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
                previousTrack();
            }
        }
        return Service.START_STICKY;
    }
    public boolean isInitialized() {
        return mInitialized;
    }

    public void setContent(Uri previewUrl, int index) {
        mInitialized=true;
        mStateStopped=false;
        mTrackIndex = index;
        mCursorData.moveToPosition(index);
        mArtistName = mCursorData.getString(ArtistDetailFragment.COL_ARTIST_NAME);
        mAlbumImage = mCursorData.getString(ArtistDetailFragment.COL_IMAGE_THUMB);
        mTrackName = mCursorData.getString(ArtistDetailFragment.COL_TRACK_NAME);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, previewUrl);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    public void resetPlayer() {
        mStatePause=false;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
    }
    public int getTrackIndex() {
        return mTrackIndex;
    }
    public void setTrackIndex(int index) {
        mTrackIndex = index;
    }

    public void nextTrack() {
        if (mTrackIndex<mCursorData.getCount()-1) {
            mTrackIndex++;
            mCursorData.moveToPosition(mTrackIndex);
            mArtistName = mCursorData.getString(ArtistDetailFragment.COL_ARTIST_NAME);
            mAlbumImage = mCursorData.getString(ArtistDetailFragment.COL_IMAGE_THUMB);
            mTrackName = mCursorData.getString(ArtistDetailFragment.COL_TRACK_NAME);
            String imgString = mCursorData.getString(ArtistDetailFragment.COL_PREVIEW_URI);
            Uri imgUrl = Uri.parse(imgString);

            resetPlayer();
            setContent(imgUrl, mTrackIndex);
            onPlay();
        }
    }
    public void previousTrack() {
        if (mTrackIndex > 0) {
            mTrackIndex--;
            mCursorData.moveToPosition(mTrackIndex);
            mArtistName = mCursorData.getString(ArtistDetailFragment.COL_ARTIST_NAME);
            mAlbumImage = mCursorData.getString(ArtistDetailFragment.COL_IMAGE_THUMB);
            mTrackName = mCursorData.getString(ArtistDetailFragment.COL_TRACK_NAME);
            String imgString = mCursorData.getString(ArtistDetailFragment.COL_PREVIEW_URI);
            Uri imgUrl = Uri.parse(imgString);

            resetPlayer();
            setContent(imgUrl, mTrackIndex);
            onPlay();
        }
    }
    public void onPlay() {
        if (mStatePause) {
            mediaPlayer.start();
            mStatePause=false;
        }
        else  {
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // mDuration=mp.getDuration();
                    mp.start();
                    mStatePause=false;
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // update the play/pause button
                    try {
                        sendBroadcast(mStopIntent);
                    } catch (NullPointerException npe) {
                        npe.printStackTrace();
                    }
                    mStateStopped = true;
                    mediaPlayer.stop();
                }
            });
            mediaPlayer.prepareAsync();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String displayNotificationsKey=this.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications =
                prefs.getBoolean(displayNotificationsKey, Boolean.parseBoolean(this.getString(R.string.pref_enable_notifications_default)));
        if (displayNotifications) {
            buildNotification(ACTION_PAUSE);
            loadThumb();
        }
        else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(SERVICE_NOTIFICATION_ID);
            notificationManager.cancelAll();
        }

    }
    public void setTrackPosition(int position) {
        mediaPlayer.seekTo(position);

    }
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void onPause() {
        mediaPlayer.pause();
        mStatePause=true;
        Log.d(TAG, "mplayer paused");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String displayNotificationsKey=this.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications =
                prefs.getBoolean(displayNotificationsKey, Boolean.parseBoolean(this.getString(R.string.pref_enable_notifications_default)));
        if (displayNotifications) {
            buildNotification(ACTION_PLAY);
            loadThumb();
        }
        else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(SERVICE_NOTIFICATION_ID);
            notificationManager.cancelAll();
        }
    }
    public Boolean getPauseState() {
        return mStatePause;
    }
    public Boolean getStoppedState() {
        return mStateStopped;
    }
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        mCursorData = data;
    }

    public class MusicBinder extends Binder {
        public MyAppPlayerService getService() {
            return MyAppPlayerService.this;
        }
    }
    private void loadThumb() {
        int imageDimen;
        try {
            URL url = new URL(mAlbumImage);
            imageDimen = (int) getResources().getDimension(R.dimen.imageview_dimen_player_large);
            Picasso.with(mContext).load(mAlbumImage).resize(imageDimen, imageDimen).centerCrop().into(mTarget);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(SERVICE_NOTIFICATION_ID);
        if (broadcastReceiver != null) {
            try {
                this.unregisterReceiver(broadcastReceiver);
                broadcastReceiver=null;
            }
            catch (IllegalArgumentException iae) {
                iae.printStackTrace();
            }
        }
    }
    public void setCursorData(Cursor data) {
        mCursorData = data;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(SERVICE_NOTIFICATION_ID);
            buildNotification(ACTION_PLAY);
        }
    };

}
