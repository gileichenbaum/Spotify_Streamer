package com.spotify.gil.spotifystreamer.fragment;

import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;

/**
 * Created by GIL on 20/06/2015 for Spotify Streamer.
 */
public interface OnTrackSelectedListener {

    OnTrackSelectedListener EMPTY = new OnTrackSelectedListener() {
        @Override
        public void onArtistTrackSelected(SpotifyArtist artist) {
        }
    };

    void onArtistTrackSelected(SpotifyArtist artist);
}
