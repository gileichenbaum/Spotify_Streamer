package com.spotify.gil.spotifystreamer.player.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.activity.ArtistListActivity;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.spotify.gil.spotifystreamer.internal.SpotifyTrack;
import com.spotify.gil.spotifystreamer.util.Spotify;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.List;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public static final String ACTION_PLAY = "play";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_PREVIOUS = "prev";
    public static final String ACTION_STOP = "stop";
    private final IBinder mBinder = new PlayerBinder();
    private SpotifyArtist mArtist;
    private MediaPlayer mMediaPlayer;
    private List<SpotifyTrack> mTracks;
    private SpotifyTrack mCurrentTrack;
    private int mRetryCount;
    private Toast mNoConnectionToast;
    private MediaPlayerListener mMediaPlayerListener = null;

    private PlayerHandler mHandler;

    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;
    private Bitmap mBitmap;

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();

        if (mController == null) return;

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mController.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            mController.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            mController.getTransportControls().stop();
        }
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private void initMediaSessions() {

        final ComponentName mRemoteComponenetName = new ComponentName(getApplicationContext(), PlayerService.class);

        try {

            final Intent intent = new Intent(getApplicationContext(), ArtistListActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

            mSession = new MediaSessionCompat(getApplicationContext(), "player", mRemoteComponenetName, pendingIntent);
            mController = new MediaControllerCompat(getApplicationContext(), mSession.getSessionToken());

            mSession.setCallback(new MediaSessionCompat.Callback() {
                                     @Override
                                     public void onPlay() {
                                         super.onPlay();
                                         if (!mMediaPlayer.isPlaying()) {
                                             mMediaPlayer.start();
                                         }
                                         startNotification();
                                     }

                                     @Override
                                     public void onPause() {
                                         super.onPause();
                                         if (mMediaPlayer.isPlaying()) {
                                             mMediaPlayer.pause();
                                         }
                                         startNotification();
                                     }

                                     @Override
                                     public void onSkipToNext() {
                                         super.onSkipToNext();
                                         if (mArtist != null) {
                                             mArtist.nextTrack();
                                             onTrackChanged();
                                         }
                                     }

                                     @Override
                                     public void onSkipToPrevious() {
                                         super.onSkipToPrevious();
                                         if (mArtist != null) {
                                             mArtist.prevTrack();
                                             onTrackChanged();
                                         }
                                     }

                                     @Override
                                     public void onFastForward() {
                                         super.onFastForward();
                                     }

                                     @Override
                                     public void onRewind() {
                                         super.onRewind();
                                     }

                                     @Override
                                     public void onStop() {
                                         super.onStop();
                                         stop();
                                     }

                                     @Override
                                     public void onSeekTo(long pos) {
                                         super.onSeekTo(pos);
                                         mMediaPlayer.seekTo((int) pos);
                                     }

                                     @Override
                                     public void onSetRating(RatingCompat rating) {
                                         super.onSetRating(rating);
                                     }
                                 }
            );

        } catch (Exception ignored) {

        }
    }

    private void startNotification() {

        if (mSession == null) {
            initMediaSessions();
        }

        final Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction(ACTION_STOP);
        final PendingIntent stopIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        final Intent contentIntent = new Intent(getApplicationContext(), ArtistListActivity.class);
        contentIntent.addCategory(Intent.ACTION_MAIN);
        contentIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();
        style.setMediaSession(mSession.getSessionToken());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setStyle(style);
        builder.setContentTitle(mCurrentTrack.getTrackName());
        builder.setContentText(mArtist.getName());
        builder.setDeleteIntent(stopIntent);
        builder.setContentInfo(mCurrentTrack.getAlbumName());
        builder.setSmallIcon(R.drawable.ic_play_white_24dp);
        builder.setContentIntent(pendingIntent);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setShowWhen(false);
        //.setOngoing(MediaPlayerState == PlaybackStateCompat.STATE_PLAYING);

        final boolean hasPrevTrack = mArtist.getCurrentTrackIndex() > 0;
        if (hasPrevTrack) {
            builder.addAction(generateAction(R.drawable.ic_rewind_white_24dp, "prev", ACTION_PREVIOUS));
        }
        addPlayPauseAction(builder);
        final boolean hasNextTrack = mArtist.hasMoreTracks();
        if (hasNextTrack) {
            builder.addAction(generateAction(R.drawable.ic_fast_forward_white_24dp, "next", ACTION_NEXT));
        }
        if (hasNextTrack && hasPrevTrack) {
            style.setShowActionsInCompactView(0, 1, 2);
        } else {
            style.setShowActionsInCompactView(0, 1);
        }

        if (mBitmap != null) {
            builder.setLargeIcon(mBitmap);
        }

        NotificationManagerCompat.from(getApplicationContext()).notify("player", 0, builder.build());

        if (mMediaPlayerListener != null) {
            mMediaPlayerListener.onPlayStateChanged(mMediaPlayer, mCurrentTrack, mArtist);
        }

        if (mMediaPlayer.isPlaying()) {
            mHandler.handleMessage(mHandler.obtainMessage(PlayerHandler.MSG_UPDATE_SEEKBAR));
        } else {
            mHandler.removeMessages(PlayerHandler.MSG_UPDATE_SEEKBAR);
        }
    }

    private void addPlayPauseAction(NotificationCompat.Builder builder) {
        if (mMediaPlayer.isPlaying()) {
            builder.addAction(generateAction(R.drawable.ic_pause_white_24dp, "Pause", ACTION_PAUSE));
        } else {
            builder.addAction(generateAction(R.drawable.ic_play_white_24dp, "Play", ACTION_PLAY));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new PlayerHandler(this);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mHandler.removeMessages(PlayerHandler.MSG_UPDATE_SEEKBAR);
        mSession.release();
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayerListener = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(PlayerHandler.MSG_UPDATE_SEEKBAR);
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayerListener = null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        mHandler.removeMessages(PlayerHandler.MSG_UPDATE_SEEKBAR);

        if (mMediaPlayerListener != null) {
            mMediaPlayerListener.onPlayStateChanged(mMediaPlayer, mCurrentTrack, mArtist);
        }

        if (mp.getCurrentPosition() > 0) {
            if (mArtist != null && mArtist.hasMoreTracks()) {
                mArtist.nextTrack();
                onTrackChanged();
            } else {
                stop();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mMediaPlayerListener != null) {
            return mMediaPlayerListener.onError(mp, what, extra);
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        mp.start();

        mRetryCount = 0;

        if (mMediaPlayerListener != null) {
            mMediaPlayerListener.onPrepared(mp, mCurrentTrack, mArtist);
        }

        startNotification();
    }

    private void onTrackChanged() {

        mHandler.removeMessages(PlayerHandler.MSG_UPDATE_SEEKBAR);

        if (Spotify.isConnected(this)) {

            if (mNoConnectionToast != null) {
                mNoConnectionToast.cancel();
            }

            final SpotifyTrack track = mTracks.get(mArtist.getCurrentTrackIndex());

            if (track == null) {
                return;
            }

            if (track.equals(mCurrentTrack) && isPlaying()) {
                if (mMediaPlayerListener != null) {
                    mMediaPlayerListener.onPrepared(mMediaPlayer, mCurrentTrack, mArtist);
                }
                startNotification();
            } else {

                mBitmap = null;
                mCurrentTrack = track;

                final String thumbnailUrl = track.getThumbnailUrl();
                if (!TextUtils.isEmpty(thumbnailUrl)) {
                    Picasso.with(getApplicationContext()).load(thumbnailUrl).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            mBitmap = bitmap;
                            startNotification();
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
                }

                try {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(track.getTrackUri());
                    mMediaPlayer.prepareAsync();

                } catch (IllegalStateException il) {
                    if (mRetryCount < 10) {
                        mRetryCount++;
                        onTrackChanged();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mNoConnectionToast = Spotify.showNotConnected(this);
        }
    }

    public void seekTo(int progress) {
        if (mController != null) {
            mController.getTransportControls().seekTo(progress);
        }
    }

    public void setMediaListener(MediaPlayerListener listener) {
        mMediaPlayerListener = listener;
    }

    public void nextTrack() {
        if (mController != null) {
            mController.getTransportControls().skipToNext();
        }
    }

    public void prevTrack() {

        if (mController != null) {
            mController.getTransportControls().skipToPrevious();
        }
    }

    public void pause() {

        if (mController != null) {
            mController.getTransportControls().pause();
        }
    }

    public void start() {

        if (mController != null) {
            mController.getTransportControls().play();
        }
    }

    public void updateProgress() {
        mArtist.setCurrentTrackPosition(mMediaPlayer.getCurrentPosition());
        if (mMediaPlayerListener != null) {
            mMediaPlayerListener.onPlayStateChanged(mMediaPlayer, mCurrentTrack, mArtist);
        }
    }

    public void togglePlayState() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                pause();
            } else {
                start();
            }
        }
    }

    public void stop() {

        NotificationManagerCompat.from(getApplicationContext()).cancelAll();

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }

        /*final Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        stopService(intent);*/
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public SpotifyArtist getArtist() {
        return mArtist;
    }

    public void setArtist(final SpotifyArtist artist) {
        mArtist = artist;
        if (mArtist != null) {
            mTracks = mArtist.getTracks();
            onTrackChanged();
        }
    }

    public void onConnected() {
        if (isPlaying()) {
            startNotification();
        } else {
            stop();
        }
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }
}
