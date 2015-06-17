package com.spotify.gil.spotifystreamer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.fragment.PlayerFragment;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState == null) {

            final PlayerFragment fragment = (PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_fragment);

            if (fragment != null) {

                final Intent intent = getIntent();

                if (intent != null && intent.hasExtra(SpotifyArtist.ARTIST_BUNDLE)) {
                    final SpotifyArtist artist = new SpotifyArtist(intent.getBundleExtra(SpotifyArtist.ARTIST_BUNDLE));
                    fragment.setArtist(artist);
                }
            }
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }*/

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
