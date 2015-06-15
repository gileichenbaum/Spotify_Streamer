package com.spotify.gil.spotifystreamer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.fragment.ArtistTracksFragment;
import com.spotify.gil.spotifystreamer.fragment.Callbacks;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;

public class ArtistTracksActivity extends AppCompatActivity implements Callbacks {

    private SpotifyArtist mArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            if (intent != null) {
                mArtist = new SpotifyArtist(intent.getBundleExtra(SpotifyArtist.ARTIST_BUNDLE));
            }
        } else {
            mArtist = new SpotifyArtist(savedInstanceState.getBundle(SpotifyArtist.ARTIST_BUNDLE));
        }

        setContentView(R.layout.activity_artist_tracks);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle(mArtist.getName());
        }

        final ArtistTracksFragment fragment = (ArtistTracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_artist);
        fragment.setData(mArtist);
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
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(SpotifyArtist.ARTIST_BUNDLE, mArtist.toBundle());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mArtist = new SpotifyArtist(savedInstanceState.getBundle(SpotifyArtist.ARTIST_BUNDLE));
    }

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, ArtistListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}
