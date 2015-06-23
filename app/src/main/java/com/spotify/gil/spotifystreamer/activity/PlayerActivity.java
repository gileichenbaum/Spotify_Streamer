package com.spotify.gil.spotifystreamer.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.fragment.PlayerFragment;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;

public class PlayerActivity extends PlayerActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);

        showPlayer(mArtist);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    protected void showPlayer(SpotifyArtist artist) {
        mPlayerFragment = new PlayerFragment();
        final Bundle b = new Bundle();
        b.putBoolean(PlayerFragment.SHOW_AS_DIALOG, false);
        if (artist != null) {
            b.putBundle(SpotifyArtist.ARTIST_BUNDLE, artist.toBundle());
        }
        mPlayerFragment.setArguments(b);
        mPlayerFragment.setShowsDialog(false);
        getSupportFragmentManager().beginTransaction().replace(R.id.player_container, mPlayerFragment, PlayerActivityBase.PLAYER_FRAGMENT_TAG).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_track_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
