package com.spotify.gil.spotifystreamer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.fragment.PlayerFragment;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;

public class PlayerActivity extends AppCompatActivity {

    private SpotifyArtist mArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            if (intent != null && intent.hasExtra(SpotifyArtist.ARTIST_BUNDLE)) {
                mArtist = new SpotifyArtist(intent.getBundleExtra(SpotifyArtist.ARTIST_BUNDLE));
            }
        } else {
            mArtist = new SpotifyArtist(savedInstanceState.getBundle(SpotifyArtist.ARTIST_BUNDLE));
        }

        setContentView(R.layout.activity_player);

        ((PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_fragment)).setArtist(mArtist);
    }

    /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            final Intent upIntent = new Intent(this, ArtistTracksActivity.class);
            upIntent.putExtra(SpotifyArtist.ARTIST_BUNDLE, mArtist.toBundle());
            NavUtils.navigateUpTo(this, upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
}
