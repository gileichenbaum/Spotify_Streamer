package com.spotify.gil.spotifystreamer.fragment;

import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;

/**
 * Created by GIL on 20/06/2015 for Spotify Streamer.
 */
public interface OnArtistSelectedListener {

    OnArtistSelectedListener EMPTY = new OnArtistSelectedListener() {
        @Override
        public void onArtistSelected(SpotifyArtist artist) {
        }
    };

    void onArtistSelected(SpotifyArtist artist);
}
