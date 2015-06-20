package com.spotify.gil.spotifystreamer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.fragment.ArtistListFragment;
import com.spotify.gil.spotifystreamer.fragment.ArtistTracksFragment;
import com.spotify.gil.spotifystreamer.fragment.OnArtistSelectedListener;
import com.spotify.gil.spotifystreamer.fragment.OnTrackSelectedListener;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;

public class ArtistListActivity extends PlayerActivityBase implements OnTrackSelectedListener, OnArtistSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_artist_search);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        mTwoPane = findViewById(R.id.artist_detail_container) != null;

        if (mTwoPane) {
            ((ArtistListFragment) getSupportFragmentManager().findFragmentById(R.id.artist_list)).setActivateOnItemClick(true);
        }

        if (mArtist != null) {
            onArtistSelected(mArtist);
        }
    }

    @Override
    public void onArtistTrackSelected(SpotifyArtist artist) {
        showPlayer(artist);
    }

    @Override
    public void onArtistSelected(SpotifyArtist artist) {

        if (mTwoPane) {
            final ArtistTracksFragment fragment = new ArtistTracksFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.artist_detail_container, fragment).commit();
            fragment.setData(artist);
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
}