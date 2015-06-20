package com.spotify.gil.spotifystreamer.fragment;

/**
 * Created by GIL on 14/06/2015.
 */
public interface Callbacks {
    Callbacks EMPTY_CALLBACK = new Callbacks() {

        @Override
        public void seekTo(int position) {

        }

        @Override
        public void nextTrack() {
        }

        @Override
        public void prevTrack() {
        }

        @Override
        public void togglePlayState() {
        }
    };

    void seekTo(int position);

    void nextTrack();

    void prevTrack();

    void togglePlayState();
}


