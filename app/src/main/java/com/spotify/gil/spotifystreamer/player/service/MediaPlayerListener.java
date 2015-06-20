package com.spotify.gil.spotifystreamer.player.service;

import android.media.MediaPlayer;

import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.spotify.gil.spotifystreamer.internal.SpotifyTrack;

/**
 * Created by GIL on 18/06/2015 for Spotify Streamer.
 */
public interface MediaPlayerListener {

    void onPlayStateChanged(MediaPlayer mp, SpotifyTrack track);

    boolean onError(MediaPlayer mp, int what, int extra);

    void onPrepared(MediaPlayer mp, SpotifyTrack track, SpotifyArtist artist);
}
