package com.spotify.gil.spotifystreamer.fragment;

import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;

/**
 * Created by GIL on 14/06/2015.
 */
public interface Callbacks {
    Callbacks EMPTY_CALLBACK = new Callbacks() {
        @Override
        public void onArtistSelected(SpotifyArtist artist) {
        }
    };

    void onArtistSelected(SpotifyArtist artist);
}


