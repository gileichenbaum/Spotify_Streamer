package com.spotify.gil.spotifystreamer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.fragment.ArtistTracksFragment;
import com.spotify.gil.spotifystreamer.fragment.OnTrackSelectedListener;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.spotify.gil.spotifystreamer.util.Spotify;

public class ArtistTracksActivity extends PlayerActivityBase implements OnTrackSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_tracks);

        final SpotifyArtist artist = Spotify.setupTracksData(savedInstanceState, getIntent());

        if (artist != null) {
            final ArtistTracksFragment fragment = (ArtistTracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_artist);
            fragment.setData(artist);
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            if (artist != null) {
                actionBar.setSubtitle(artist.getName());
            }
        }
    }

    @Override
    public void onArtistTrackSelected(SpotifyArtist artist) {
        final Intent playerIntent = new Intent();
        playerIntent.setClass(this, PlayerActivity.class);
        playerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        playerIntent.putExtra(SpotifyArtist.ARTIST_BUNDLE, artist.toBundle());
        startActivity(playerIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (mPlayerMenuItem != null && id == mPlayerMenuItem.getItemId()) {
            if (mPlayerService != null) {
                onArtistTrackSelected(mPlayerService.getArtist());
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
