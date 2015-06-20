package com.spotify.gil.spotifystreamer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.fragment.Callbacks;
import com.spotify.gil.spotifystreamer.fragment.PlayerFragment;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.spotify.gil.spotifystreamer.internal.SpotifyTrack;
import com.spotify.gil.spotifystreamer.player.service.MediaPlayerListener;
import com.spotify.gil.spotifystreamer.player.service.PlayerService;
import com.spotify.gil.spotifystreamer.util.Spotify;

/**
 * Created by GIL on 20/06/2015 for Spotify Streamer.
 */
public abstract class PlayerActivityBase extends AppCompatActivity implements Callbacks, MediaPlayerListener {
    protected PlayerService mPlayerService;
    protected PlayerFragment mPlayerFragment;
    protected boolean mTwoPane;
    protected SpotifyArtist mArtist;
    protected MenuItem mPlayerMenuItem;
    private boolean mServiceConnected;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceConnected = true;
            final PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            setupServiceListener(binder.getService());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceConnected = false;
            mPlayerService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Spotify.init();
        initPlayerService();
        mArtist = Spotify.setupTracksData(savedInstanceState, getIntent());
    }

    private void initPlayerService() {
        if (!mServiceConnected || mPlayerService == null) {
            final Context applicationContext = getApplicationContext();
            final Intent intent = new Intent(applicationContext, PlayerService.class);
            applicationContext.startService(intent);
            applicationContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    protected void showPlayer(SpotifyArtist artist) {

        mPlayerFragment = (PlayerFragment) getSupportFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());

        if (mPlayerFragment != null) {
            return;
        } else if (!mTwoPane && findViewById(R.id.player_container) == null) {
            final Intent playerIntent = new Intent();
            playerIntent.setClass(this, PlayerActivity.class);
            if (artist != null) {
                playerIntent.putExtra(SpotifyArtist.ARTIST_BUNDLE, artist.toBundle());
            }
            startActivity(playerIntent);
            return;
        }

        if (mPlayerFragment == null) {
            mPlayerFragment = new PlayerFragment();
            final Bundle b = new Bundle();
            b.putBoolean(PlayerFragment.SHOW_AS_DIALOG, mTwoPane);
            if (artist != null) {
                b.putBundle(SpotifyArtist.ARTIST_BUNDLE, artist.toBundle());
            }
            mPlayerFragment.setArguments(b);
            mPlayerFragment.setShowsDialog(mTwoPane);
        }

        if (mTwoPane) {
            mPlayerFragment.show(getSupportFragmentManager(), "tablet_player");
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.player_container, mPlayerFragment, PlayerFragment.class.getSimpleName()).commit();
        }
    }

    @Override
    public void seekTo(int position) {
        if (mServiceConnected && mPlayerService != null) {
            mPlayerService.seekTo(position);
        }
    }

    @Override
    public void nextTrack() {
        if (mServiceConnected && mPlayerService != null) {
            mPlayerService.nextTrack();
        }
    }

    @Override
    public void prevTrack() {
        if (mServiceConnected && mPlayerService != null) {
            mPlayerService.prevTrack();
        }
    }

    @Override
    public void togglePlayState() {
        if (mServiceConnected && mPlayerService != null) {
            mPlayerService.togglePlayState();
        }
    }

    private void setupServiceListener(PlayerService service) {
        mPlayerService = service;
        service.setMediaListener(this);
        if (mArtist != null && mArtist.getTracks() != null && mArtist.getCurrentTrackIndex() > -1) {
            mPlayerService.setArtist(mArtist);
        } else if (mPlayerService != null && mPlayerService.getArtist() != null) {
            service.onConnected();
        }
        refreshPlayerMenuItemVisibility();
    }

    @Override
    public void onPlayStateChanged(MediaPlayer mp, SpotifyTrack track) {
        if (mPlayerFragment != null) {
            mPlayerFragment.onPlayStateChanged(mp, track);
        }
        refreshPlayerMenuItemVisibility();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        refreshPlayerMenuItemVisibility();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp, SpotifyTrack track, SpotifyArtist artist) {
        if (mPlayerFragment != null) {
            mPlayerFragment.onPrepared(mp, track, artist);
        }
        refreshPlayerMenuItemVisibility();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mArtist != null) {
            outState.putBundle(SpotifyArtist.ARTIST_BUNDLE, mArtist.toBundle());
        }
    }

    protected void refreshPlayerMenuItemVisibility() {

        if (mPlayerMenuItem != null) {
            if (mPlayerService != null) {
                final boolean visible = mPlayerMenuItem.isVisible();
                final boolean isPlaying = mPlayerService.isPlaying();
                if (isPlaying != visible) {
                    mPlayerMenuItem.setVisible(isPlaying);
                }
            }
        }
    }
}
