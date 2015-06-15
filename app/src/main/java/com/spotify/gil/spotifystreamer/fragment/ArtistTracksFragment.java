package com.spotify.gil.spotifystreamer.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.adapter.SpotifySongsAdapter;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.spotify.gil.spotifystreamer.internal.SpotifyTrack;
import com.spotify.gil.spotifystreamer.util.Spotify;

import java.util.List;

public class ArtistTracksFragment extends Fragment implements SpotifyArtist.OnTracksLoadListener {

    private SpotifyArtist mArtist;

    private Callbacks mCallbacks = Callbacks.EMPTY_CALLBACK;
    private SpotifySongsAdapter mAdapter;
    private Toast mNoConnectionToast;

    public ArtistTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ListView listView = (ListView) view.findViewById(R.id.list);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mArtist.setSelectedTrack(position);
                mCallbacks.onArtistSelected(mArtist);
            }
        });

        if (savedInstanceState == null) {
            final Bundle args = getArguments();
            if (args != null && args.containsKey(SpotifyArtist.ARTIST_BUNDLE)) {
                mArtist = new SpotifyArtist(getArguments().getBundle(SpotifyArtist.ARTIST_BUNDLE));
            }
        } else {
            mArtist = new SpotifyArtist(savedInstanceState.getBundle(SpotifyArtist.ARTIST_BUNDLE));
        }

        refreshTracks();
    }

    private void refreshTracks() {

        mArtist.setOnTracksLoadListener(this);

        final List<SpotifyTrack> tracks = mArtist.getTracks();

        if (tracks.size() <= 0 && !mArtist.hasLoadingError()) {
            mArtist.loadTracks();
            return;
        }

        if (mArtist.hasLoadingError()) {
            checkConnection();
        } else {
            mAdapter.clear();
            mAdapter.addAll(tracks);
        }
    }

    public void setData(SpotifyArtist artist) {
        if (artist != null && (mArtist == null || !artist.equals(mArtist))) {
            mArtist = artist;
            refreshTracks();
        }
    }

    private void checkConnection() {

        final Context context = getActivity();

        if (context == null) return;

        if (!Spotify.isConnected(context)) {
            mNoConnectionToast = Spotify.showNotConnected(context);
        } else if (mNoConnectionToast != null) {
            mNoConnectionToast.cancel();
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(SpotifyArtist.ARTIST_BUNDLE, mArtist.toBundle());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
        mAdapter = new SpotifySongsAdapter(activity, R.layout.artist_row);

        final Intent intent = activity.getIntent();
        if (intent != null && intent.hasExtra(SpotifyArtist.ARTIST_BUNDLE)) {
            mArtist = new SpotifyArtist(intent.getBundleExtra(SpotifyArtist.ARTIST_BUNDLE));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mArtist != null) {
            mArtist.setOnTracksLoadListener(null);
        }
        mCallbacks = Callbacks.EMPTY_CALLBACK;
    }

    @Override
    public void onTracksLoaded(SpotifyArtist artist) {
        refreshTracks();
    }
}
