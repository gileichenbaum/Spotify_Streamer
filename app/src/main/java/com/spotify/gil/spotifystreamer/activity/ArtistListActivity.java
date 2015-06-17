package com.spotify.gil.spotifystreamer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.fragment.ArtistListFragment;
import com.spotify.gil.spotifystreamer.fragment.ArtistTracksFragment;
import com.spotify.gil.spotifystreamer.fragment.Callbacks;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.spotify.gil.spotifystreamer.util.Spotify;

public class ArtistListActivity extends AppCompatActivity
        implements Callbacks {

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Spotify.init();

        setContentView(R.layout.activity_artist_search);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        if (findViewById(R.id.artist_detail_container) != null) {
            mTwoPane = true;
            ((ArtistListFragment) getSupportFragmentManager().findFragmentById(R.id.artist_list)).setActivateOnItemClick(true);
        }
    }

    @Override
    public void onArtistSelected(SpotifyArtist artist) {

        final Bundle artistBundle = artist.toBundle();

        if (mTwoPane) {
            final ArtistTracksFragment fragment = new ArtistTracksFragment();
            fragment.setArguments(artistBundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.artist_detail_container, fragment).commit();
        } else {
            final Intent intent = new Intent();
            intent.setClass(this, ArtistTracksActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(SpotifyArtist.ARTIST_BUNDLE, artistBundle);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final ArtistListFragment fragment = ((ArtistListFragment) getSupportFragmentManager().findFragmentById(R.id.artist_list));
        if (fragment != null) {
            fragment.saveInstanceState(outState);
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
