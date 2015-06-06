package com.spotify.gil.spotifystreamer.internal;

import android.os.Bundle;
import android.util.Log;

import com.spotify.gil.spotifystreamer.util.Spotify;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by GIL on 01/06/2015.
 */
public class SpotifyTrack implements Comparable<SpotifyTrack> {

    private static final String TAG = SpotifyTrack.class.getSimpleName();

    public final static String ARTIST_NAME = "artist_name";
    public final static String TRACK_URI = "track_uri";
    public final static String ALBUM_NAME = "album_name";
    public final static String ALBUM_ART_URI = "art_uri";
    public final static String ALBUM_THUMBNAIL_URI = "art_thumbnail_uri";
    public final static String TRACK_NAME = "track_name";
    public static final String TRACKS_BUNDLE = "track_bundle";
    public static final String INDEX = "index";

    private final String mTrackName;
    private final String mArtistName;
    private final String mTrackUri;
    private final String mAlbumName;
    private final String mAlbumArtUri;
    private final Integer mIndex;
    private final String mThumbnailUrl;

    public SpotifyTrack(final int index,Track track) {

        mIndex = index;
        mTrackName = track.name;
        mArtistName = track.artists.get(0).name;
        mTrackUri = track.preview_url;
        mAlbumName = track.album.name;
        mAlbumArtUri = Spotify.getImage(track.album.images, Spotify.ImageSize.LARGE).url;
        mThumbnailUrl = Spotify.getImage(track.album.images, Spotify.ImageSize.SMALL).url;

        Log.i(TAG,toString());
    }

    public SpotifyTrack(final Bundle bundle) {
        mTrackName = bundle.getString(TRACK_NAME);
        mArtistName = bundle.getString(ARTIST_NAME);
        mTrackUri = bundle.getString(TRACK_URI);
        mAlbumName = bundle.getString(ALBUM_NAME);
        mAlbumArtUri = bundle.getString(ALBUM_ART_URI);
        mIndex = bundle.getInt(INDEX);
        mThumbnailUrl = bundle.getString(ALBUM_THUMBNAIL_URI);
    }

    public Bundle toBundle() {
        final Bundle bundle = new Bundle();
        bundle.putString(TRACK_NAME, mTrackName);
        bundle.putString(ARTIST_NAME, mAlbumName);
        bundle.putString(TRACK_URI, mTrackUri);
        bundle.putString(ALBUM_NAME, mAlbumName);
        bundle.putString(ALBUM_ART_URI, mAlbumArtUri);
        bundle.putString(ALBUM_THUMBNAIL_URI, mThumbnailUrl);
        bundle.putInt(INDEX, mIndex);
        return bundle;
    }

    public String getArtUrl() {
        return mAlbumArtUri;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public String getTrackUri() {
        return mTrackUri;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    @Override
    public int compareTo(SpotifyTrack another) {

        if (another == null) return -1;

        return mIndex.compareTo(another.mIndex);
    }

    @Override
    public String toString() {
        return "SpotifyTrack{" +
                "mTrackName='" + mTrackName + '\'' +
                ", mIndex=" + mIndex +
                ", mArtistName='" + mArtistName + '\'' +
                ", mTrackUri='" + mTrackUri + '\'' +
                ", mAlbumName='" + mAlbumName + '\'' +
                ", mAlbumArtUri='" + mAlbumArtUri + '\'' +
                ", mThumbnailUrl='" + mThumbnailUrl + '\'' +
                '}';
    }
}
