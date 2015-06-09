package com.spotify.gil.spotifystreamer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.adapter.SpotifySongsAdapter;
import com.spotify.gil.spotifystreamer.async.FetchArtistSongsAsyncTask;
import com.spotify.gil.spotifystreamer.internal.SpotifyTrack;
import com.spotify.gil.spotifystreamer.util.Spotify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class TracksActivity extends AppCompatActivity {

    public static final String ARTIST_ID = "artist_id";
    public static final String ARTIST_NAME = "artist_name";
    private final static String TAG = TracksActivity.class.getSimpleName();
    private SpotifySongsAdapter mAdapter;
    private String mArtistName;
    private Toast mNoConnectionToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_artist);

        final ListView listView = (ListView) findViewById(R.id.list);
        mAdapter = new SpotifySongsAdapter(this, R.layout.artist_row);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final Intent playerIntent = new Intent();
                playerIntent.setClass(view.getContext(), PlayerActivity.class);

                final Bundle tracksData = new Bundle();
                fillBundleWithAdapterContent(position, tracksData);
                playerIntent.putExtra(SpotifyTrack.TRACKS_BUNDLE, tracksData);

                playerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(playerIntent);
            }
        });

        final ActionBar actionBar = getSupportActionBar();

        if (savedInstanceState != null && savedInstanceState.containsKey(ARTIST_NAME)) {
            mArtistName = savedInstanceState.getString(ARTIST_NAME);
            fillAdapterFromBundle(savedInstanceState);

        } else {

            final Intent intent = getIntent();
            if (intent != null) {
                mArtistName = intent.getStringExtra(ARTIST_NAME);
                setArtistId(intent.getStringExtra(ARTIST_ID));
            }
        }

        actionBar.setSubtitle(mArtistName);
    }

    public void setArtistId(String artistID) {

        if (mNoConnectionToast != null) {
            mNoConnectionToast.cancel();
        }

        if (Spotify.isConnected(this)) {
            new FetchArtistSongsAsyncTask() {
                @Override
                protected void onPostExecute(Tracks tracks) {
                    super.onPostExecute(tracks);

                    mAdapter.clear();

                    int i = 0;
                    if (tracks != null && tracks.tracks != null && !tracks.tracks.isEmpty()) {
                        for (Track track : tracks.tracks) {
                            mAdapter.add(new SpotifyTrack(i++, track));
                        }
                    } else {
                        checkConnection();
                    }
                }
            }.execute(artistID);
        } else {
            mNoConnectionToast = Spotify.showNotConnected(this);
        }

    }

    private void checkConnection() {
        if (!Spotify.isConnected(this)) {
            mNoConnectionToast = Spotify.showNotConnected(this);
        }
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist, menu);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        final Bundle tracksData = new Bundle();
        fillBundleWithAdapterContent(-1, tracksData);
        outState.putBundle(SpotifyTrack.TRACKS_BUNDLE, tracksData);
        outState.putString(TracksActivity.ARTIST_NAME, mArtistName);
    }

    private void fillAdapterFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            final Bundle tracksBundle = savedInstanceState.getBundle(SpotifyTrack.TRACKS_BUNDLE);
            if (tracksBundle != null) {

                final Set<String> trackNames = tracksBundle.keySet();
                final ArrayList<SpotifyTrack> items = new ArrayList<SpotifyTrack>(trackNames.size());

                for (String trackName : trackNames) {
                    final Bundle trackBundle = tracksBundle.getBundle(trackName);
                    items.add(new SpotifyTrack(trackBundle));
                }

                Collections.sort(items);
                mAdapter.addAll(items);
            }
        }
    }

    private void fillBundleWithAdapterContent(int position, Bundle tracksData) {

        for (int i = 0; i < mAdapter.getCount(); i++) {

            final SpotifyTrack track = mAdapter.getItem(i);

            final Bundle trackBundle = track.toBundle();

            if (i == position) {
                trackBundle.putBoolean(PlayerActivity.AUTO_PLAY, true);
            }

            tracksData.putBundle(track.getTrackName(), trackBundle);
        }
    }
}
