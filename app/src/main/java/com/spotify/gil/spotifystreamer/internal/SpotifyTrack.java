package com.spotify.gil.spotifystreamer.internal;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.spotify.gil.spotifystreamer.util.Spotify;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by GIL on 01/06/2015.
 */
public class SpotifyTrack implements Comparable<SpotifyTrack> {

    public final static String ARTIST_NAME = "artist_name";
    public final static String TRACK_URI = "track_uri";
    public final static String ALBUM_NAME = "album_name";
    public final static String ALBUM_ART_URI = "art_uri";
    public final static String ALBUM_THUMBNAIL_URI = "art_thumbnail_uri";
    public final static String TRACK_NAME = "track_name";
    public final static String EXTERNAL_URL = "external_ur;";
    public static final String INDEX = "index";
    private final String mTrackName;
    private final String mArtistName;
    private final String mTrackUri;
    private final String mAlbumName;
    private final String mAlbumArtUri;
    private final Integer mIndex;
    private final String mThumbnailUrl;
    private final String mExternalUrl;

    public SpotifyTrack(final int index, Track track) {

        mIndex = index;
        mTrackName = track.name;

        mExternalUrl = track.external_urls == null ? null : track.external_urls.get("spotify");

        if (track.artists.size() == 1) {
            mArtistName = track.artists.get(0).name;
        } else {
            final StringBuilder builder = new StringBuilder();
            for (ArtistSimple artist : track.artists) {
                builder.append(artist.name).append(" & ");
            }
            mArtistName = builder.substring(0, builder.length() - 3);
        }
        mTrackUri = track.preview_url;
        mAlbumName = track.album.name;
        mAlbumArtUri = Spotify.getImage(track.album.images, Spotify.ImageSize.LARGE).url;
        mThumbnailUrl = Spotify.getImage(track.album.images, Spotify.ImageSize.SMALL).url;
    }

    public SpotifyTrack(final Bundle bundle) {
        mTrackName = bundle.getString(TRACK_NAME);
        mArtistName = bundle.getString(ARTIST_NAME);
        mTrackUri = bundle.getString(TRACK_URI);
        mAlbumName = bundle.getString(ALBUM_NAME);
        mAlbumArtUri = bundle.getString(ALBUM_ART_URI);
        mIndex = bundle.getInt(INDEX);
        mThumbnailUrl = bundle.getString(ALBUM_THUMBNAIL_URI);
        mExternalUrl = bundle.getString(EXTERNAL_URL);
    }

    public Bundle toBundle() {
        final Bundle bundle = new Bundle();
        bundle.putString(TRACK_NAME, mTrackName);
        bundle.putString(ARTIST_NAME, mAlbumName);
        bundle.putString(ARTIST_NAME, mArtistName);
        bundle.putString(TRACK_URI, mTrackUri);
        bundle.putString(ALBUM_NAME, mAlbumName);
        bundle.putString(ALBUM_ART_URI, mAlbumArtUri);
        bundle.putString(ALBUM_THUMBNAIL_URI, mThumbnailUrl);
        bundle.putInt(INDEX, mIndex);
        bundle.putString(EXTERNAL_URL, mExternalUrl);
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

    public String getExternaUrl() {
        return mExternalUrl;
    }

    @Override
    public int compareTo(@NonNull SpotifyTrack another) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpotifyTrack that = (SpotifyTrack) o;

        if (mTrackName != null ? !mTrackName.equals(that.mTrackName) : that.mTrackName != null)
            return false;
        if (mArtistName != null ? !mArtistName.equals(that.mArtistName) : that.mArtistName != null)
            return false;
        if (mTrackUri != null ? !mTrackUri.equals(that.mTrackUri) : that.mTrackUri != null)
            return false;
        if (mAlbumName != null ? !mAlbumName.equals(that.mAlbumName) : that.mAlbumName != null)
            return false;
        if (mAlbumArtUri != null ? !mAlbumArtUri.equals(that.mAlbumArtUri) : that.mAlbumArtUri != null)
            return false;
        if (!mIndex.equals(that.mIndex)) {
            return false;
        }
        return !(mThumbnailUrl != null ? !mThumbnailUrl.equals(that.mThumbnailUrl) : that.mThumbnailUrl != null);

    }

    @Override
    public int hashCode() {
        int result = mTrackName != null ? mTrackName.hashCode() : 0;
        result = 31 * result + (mArtistName != null ? mArtistName.hashCode() : 0);
        result = 31 * result + (mTrackUri != null ? mTrackUri.hashCode() : 0);
        result = 31 * result + (mAlbumName != null ? mAlbumName.hashCode() : 0);
        result = 31 * result + (mAlbumArtUri != null ? mAlbumArtUri.hashCode() : 0);
        result = 31 * result + mIndex;
        result = 31 * result + (mThumbnailUrl != null ? mThumbnailUrl.hashCode() : 0);
        return result;
    }
}
