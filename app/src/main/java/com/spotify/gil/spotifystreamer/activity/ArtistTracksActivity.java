package com.spotify.gil.spotifystreamer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.fragment.ArtistTracksFragment;
import com.spotify.gil.spotifystreamer.fragment.Callbacks;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;

public class ArtistTracksActivity extends AppCompatActivity implements Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_tracks);

        SpotifyArtist artist = null;

        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            if (intent != null && intent.hasExtra(SpotifyArtist.ARTIST_BUNDLE)) {
                artist = new SpotifyArtist(intent.getBundleExtra(SpotifyArtist.ARTIST_BUNDLE));
                final ArtistTracksFragment fragment = (ArtistTracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_artist);
                fragment.setData(artist);
            }
        } else if (savedInstanceState.containsKey(SpotifyArtist.ARTIST_BUNDLE)) {
            artist = new SpotifyArtist(savedInstanceState.getBundle(SpotifyArtist.ARTIST_BUNDLE));
        }

        if (artist != null) {
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setSubtitle(artist.getName());
            }
        }
    }

    @Override
    public void onArtistSelected(final SpotifyArtist artist) {
        final Intent playerIntent = new Intent();
        playerIntent.setClass(this, PlayerActivity.class);
        playerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        playerIntent.putExtra(SpotifyArtist.ARTIST_BUNDLE, artist.toBundle());
        startActivity(playerIntent);
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
