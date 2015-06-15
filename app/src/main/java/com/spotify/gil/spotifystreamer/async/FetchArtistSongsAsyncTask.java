package com.spotify.gil.spotifystreamer.async;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.spotify.gil.spotifystreamer.internal.SpotifyTrack;
import com.spotify.gil.spotifystreamer.util.Spotify;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artists;

/**
 * Created by GIL on 30/05/2015.
 */
public class FetchArtistSongsAsyncTask extends AsyncTask<String, Artists, List<SpotifyTrack>> {

    @Override
    protected List<SpotifyTrack> doInBackground(String... params) {

        final String artistID = params != null && params.length > 0 ? params[0] : null;

        if (TextUtils.isEmpty(artistID)) {
            return null;
        }

        return Spotify.getArtistSongs(artistID);
    }
}
