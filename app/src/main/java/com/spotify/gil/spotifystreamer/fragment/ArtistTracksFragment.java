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

    private OnTrackSelectedListener mOnTracksSelectedListener = OnTrackSelectedListener.EMPTY;
    private SpotifySongsAdapter mAdapter;
    private Toast mNoConnectionToast;

    public ArtistTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ListView listView = (ListView) view.findViewById(R.id.list);

        if (mAdapter == null) {
            mAdapter = new SpotifySongsAdapter(view.getContext(), R.layout.artist_row);
        }
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mArtist.setSelectedTrack(position);
                mOnTracksSelectedListener.onArtistTrackSelected(mArtist);
            }
        });

        if (savedInstanceState != null) {
            mArtist = new SpotifyArtist(savedInstanceState.getBundle(SpotifyArtist.ARTIST_BUNDLE));
        }

        refreshTracks();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mArtist = new SpotifyArtist(savedInstanceState.getBundle(SpotifyArtist.ARTIST_BUNDLE));
            refreshTracks();
        }
    }

    private void refreshTracks() {

        if (mArtist == null) return;

        if (mArtist.hasNoTracks()) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.no_tracks_for_artist, mArtist.getName()), Toast.LENGTH_LONG).show();
            return;
        }

        final List<SpotifyTrack> tracks = mArtist.getTracks();
        if (tracks.size() <= 0 && !mArtist.hasLoadingError() && !mArtist.hasNoTracks()) {
            mArtist.setOnTracksLoadListener(this);
            mArtist.loadTracks();
            return;
        }

        if (mArtist.hasLoadingError()) {
            checkConnection();
        } else {
            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter.addAll(tracks);
            }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(SpotifyArtist.ARTIST_BUNDLE, mArtist.toBundle());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OnTrackSelectedListener)) {
            throw new IllegalStateException("Activity must implement OnTrackSelectedListener");
        }
        mOnTracksSelectedListener = (OnTrackSelectedListener) activity;

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
        mOnTracksSelectedListener = OnTrackSelectedListener.EMPTY;
    }

    @Override
    public void onTracksLoaded(SpotifyArtist artist) {
        refreshTracks();
    }
}
