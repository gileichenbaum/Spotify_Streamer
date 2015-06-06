package com.spotify.gil.spotifystreamer.internal;

import com.spotify.gil.spotifystreamer.util.Spotify;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by GIL on 06/06/2015.
 */
public class SpotifyArtist {
    private final String mName;
    private final String mImageUrl;
    private final String mSpotifyId;

    public SpotifyArtist(Artist artist) {

        if (artist == null) {
            mSpotifyId = null;
            mName = null;
            mImageUrl = null;
        } else {
            mName = artist.name;
            final Image image = Spotify.getImage(artist.images, Spotify.ImageSize.SMALL);
            mImageUrl = image == null ? null : image.url;
            mSpotifyId = artist.id;
        }

    }

    public String getName() {
        return mName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getSpotifyId() {
        return mSpotifyId;
    }

    public boolean isEmpty() {
        return mSpotifyId == null;
    }
}
