package com.spotify.gil.spotifystreamer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.fragment.ArtistTracksFragment;
import com.spotify.gil.spotifystreamer.fragment.OnArtistSelectedListener;
import com.spotify.gil.spotifystreamer.fragment.OnTrackSelectedListener;
import com.spotify.gil.spotifystreamer.fragment.PlayerFragment;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.spotify.gil.spotifystreamer.player.service.PlayerService;

public class ArtistListActivity extends PlayerActivityBase implements OnTrackSelectedListener, OnArtistSelectedListener {

    private static final String SHOULD_SHOW_PLAYER = "show_player";
    private static final String TRACKS_FRAGMENT_TAG = "tracks_fragment";
    protected boolean mTwoPane;
    private boolean mShowPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_artist_search);

        mShowPlayer = savedInstanceState != null && savedInstanceState.containsKey(SHOULD_SHOW_PLAYER) && savedInstanceState.getBoolean(SHOULD_SHOW_PLAYER);
        mTwoPane = findViewById(R.id.artist_detail_container) != null;
        if (mArtist != null && !mTwoPane) {
            onArtistSelected(mArtist);
        }

        mArtistTracksFragment = (ArtistTracksFragment) getSupportFragmentManager().findFragmentByTag(TRACKS_FRAGMENT_TAG);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected void setupServiceListener(final PlayerService service) {
        super.setupServiceListener(service);
        if (mShowPlayer && service != null && service.isPlaying()) {
            showPlayer(service.getArtist());
        }
    }

    protected void showPlayer(SpotifyArtist artist) {

        mArtist = artist;
        mPlayerFragment = (PlayerFragment) getSupportFragmentManager().findFragmentByTag(PLAYER_FRAGMENT_TAG);

        if (mPlayerFragment != null) {
            return;
        }

        mPlayerFragment = new PlayerFragment();
        final Bundle b = new Bundle();
        b.putBoolean(PlayerFragment.SHOW_AS_DIALOG, mTwoPane);
        if (artist != null) {
            b.putBundle(SpotifyArtist.ARTIST_BUNDLE, artist.toBundle());
        }
        mPlayerFragment.setArguments(b);
        mPlayerFragment.setShowsDialog(mTwoPane);

        mPlayerFragment.show(getSupportFragmentManager(), PLAYER_FRAGMENT_TAG);
       /* if (mPlayerService != null) {
            if (mPlayerService.isPlaying()) {
                mPlayerService.setArtist(mArtist);
            } else {
                setupServiceListener(mPlayerService);
            }
        } else {
            initPlayerService();
        }*/

        if (mPlayerService != null) {
            mPlayerService.setArtist(mArtist);
        } else {
            initPlayerService();
        }
    }

    @Override
    public void onArtistTrackSelected(SpotifyArtist artist) {
        if (mTwoPane) {
            showPlayer(artist);
        } else {
            final Intent playerIntent = new Intent();
            playerIntent.setClass(this, PlayerActivity.class);
            if (artist != null) {
                playerIntent.putExtra(SpotifyArtist.ARTIST_BUNDLE, artist.toBundle());
            }
            startActivity(playerIntent);
        }
    }

    @Override
    public void onArtistSelected(SpotifyArtist artist) {

        if (mTwoPane) {
            mArtistTracksFragment = new ArtistTracksFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.artist_detail_container, mArtistTracksFragment, TRACKS_FRAGMENT_TAG).commit();
            mArtistTracksFragment.setData(artist);
        } else {
            final Intent intent = new Intent();
            intent.setClass(this, ArtistTracksActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(SpotifyArtist.ARTIST_BUNDLE, artist.toBundle());
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManagerCompat.from(this).cancelAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mPlayerMenuItem = menu.findItem(R.id.action_player);
        refreshPlayerMenuItemVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (mPlayerMenuItem != null && id == mPlayerMenuItem.getItemId()) {
            if (mPlayerService == null) {
                showPlayer(mArtist);
            } else {
                showPlayer(mPlayerService.getArtist());
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void supportInvalidateOptionsMenu() {
        super.supportInvalidateOptionsMenu();
        refreshPlayerMenuItemVisibility();
    }

    @Override
    protected void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        final boolean isShowingPlayer = mPlayerFragment != null && mPlayerFragment.isVisible();
        outState.putBoolean(SHOULD_SHOW_PLAYER, isShowingPlayer);
    }
}