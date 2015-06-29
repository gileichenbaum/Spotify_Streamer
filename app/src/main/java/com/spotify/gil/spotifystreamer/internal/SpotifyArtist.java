package com.spotify.gil.spotifystreamer.internal;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.spotify.gil.spotifystreamer.async.FetchArtistSongsAsyncTask;
import com.spotify.gil.spotifystreamer.util.Spotify;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by GIL on 06/06/2015.
 */
public class SpotifyArtist implements Serializable {

    public static final String ARTIST_BUNDLE = "artist_bundle";

    public final static String ID = "artist_id";
    public final static String NAME = "artist_name";
    public final static String IMAGE_URL = "artist_image_url";
    public final static String TRACKS_BUNDLE = "track_bundle";
    public static final String HAS_NO_TRACKS = "has_no_tracks";
    public static final String SELECTED_TRACK_POSITION = "track_position";
    public static final String SELECTED_TRACK_INDEX = "track_index";

    private final String mName;
    private final String mImageUrl;
    private final String mSpotifyId;
    private final ArrayList<SpotifyTrack> mTracksList = new ArrayList<>();
    private int mSelectedTrackIndex = -1;
    private int mSelectedTrackPosition;
    private boolean mErrorLoadingTracks;
    private OnTracksLoadListener mOnTracksLoadListener;
    private FetchArtistSongsAsyncTask mTrackLoadingTask;
    private boolean mHasNoTracks;
    private SpotifyTrack currentTrack;

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

    public SpotifyArtist(final Bundle bundle) {

        mSpotifyId = bundle.getString(ID);
        mName = bundle.getString(NAME);
        mImageUrl = bundle.getString(IMAGE_URL);
        mSelectedTrackPosition = bundle.getInt(SELECTED_TRACK_POSITION);
        mSelectedTrackIndex = bundle.getInt(SELECTED_TRACK_INDEX);
        mHasNoTracks = bundle.getBoolean(HAS_NO_TRACKS);

        final Bundle tracksBundle = bundle.getBundle(TRACKS_BUNDLE);

        if (tracksBundle != null && tracksBundle.keySet() != null) {
            for (String trackName : tracksBundle.keySet()) {
                if (!TextUtils.isEmpty(trackName)) {
                    mTracksList.add(new SpotifyTrack(tracksBundle.getBundle(trackName)));
                }
            }
        }

        Collections.sort(mTracksList);
    }

    public String getName() {
        return mName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public boolean hasLoadingError() {
        return mErrorLoadingTracks;
    }

    public void loadTracks() {

        if (mTracksList.size() <= 0 && mTrackLoadingTask == null || (mTrackLoadingTask.getStatus() == AsyncTask.Status.FINISHED)) {

            mErrorLoadingTracks = false;

            mTrackLoadingTask = new FetchArtistSongsAsyncTask() {
                @Override
                protected void onPostExecute(List<SpotifyTrack> spotifyTracks) {
                    super.onPostExecute(spotifyTracks);

                    if (spotifyTracks == null) {
                        mErrorLoadingTracks = true;
                    } else if (spotifyTracks.size() <= 0) {
                        mHasNoTracks = true;
                    } else {
                        mTracksList.addAll(spotifyTracks);
                        Collections.sort(mTracksList);
                    }
                    onTrackLoadingComplete();
                }
            };

            mTrackLoadingTask.execute(mSpotifyId);
        }
    }

    private void onTrackLoadingComplete() {
        if (mOnTracksLoadListener != null) {
            mOnTracksLoadListener.onTracksLoaded(this);
        }
    }

    public Bundle toBundle() {
        final Bundle bundle = new Bundle();
        bundle.putString(ID, mSpotifyId);
        bundle.putString(NAME, mName);
        bundle.putString(IMAGE_URL, mImageUrl);
        bundle.putInt(SELECTED_TRACK_POSITION, mSelectedTrackPosition);
        bundle.putInt(SELECTED_TRACK_INDEX, mSelectedTrackIndex);
        bundle.putBoolean(HAS_NO_TRACKS, mHasNoTracks);

        final Bundle tracksBundle = new Bundle();

        for (int i = 0; i < mTracksList.size(); i++) {
            final SpotifyTrack track = mTracksList.get(i);
            tracksBundle.putBundle(track.getTrackName(), track.toBundle());
        }

        bundle.putBundle(TRACKS_BUNDLE, tracksBundle);

        return bundle;
    }

    public void setSelectedTrack(int selectedTrack) {
        mSelectedTrackIndex = selectedTrack;
        mSelectedTrackPosition = 0;
    }

    public List<SpotifyTrack> getTracks() {
        return mTracksList;
    }

    public void setOnTracksLoadListener(OnTracksLoadListener onTracksLoadListener) {
        mOnTracksLoadListener = onTracksLoadListener;
    }

    public int getCurrentTrackIndex() {
        return mSelectedTrackIndex;
    }

    public void setCurrentTrackPosition(int currentTrackPosition) {
        mSelectedTrackPosition = currentTrackPosition;
    }

    public void nextTrack() {
        setSelectedTrack(Math.min(mSelectedTrackIndex + 1, mTracksList.size()));
    }

    public void prevTrack() {
        setSelectedTrack(Math.max(mSelectedTrackIndex - 1, 0));
    }

    public boolean hasMoreTracks() {
        return mSelectedTrackIndex < mTracksList.size() - 1;
    }

    public boolean isEmpty() {
        return mSpotifyId == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpotifyArtist that = (SpotifyArtist) o;

        if (mSelectedTrackIndex != that.mSelectedTrackIndex) return false;
        if (mSelectedTrackPosition != that.mSelectedTrackPosition) return false;
        if (mErrorLoadingTracks != that.mErrorLoadingTracks) return false;
        if (mName != null ? !mName.equals(that.mName) : that.mName != null) return false;
        if (mImageUrl != null ? !mImageUrl.equals(that.mImageUrl) : that.mImageUrl != null)
            return false;
        if (mSpotifyId != null ? !mSpotifyId.equals(that.mSpotifyId) : that.mSpotifyId != null)
            return false;

        return mTracksList.size() == that.mTracksList.size();
    }

    @Override
    public int hashCode() {
        int result = mName != null ? mName.hashCode() : 0;
        result = 31 * result + (mImageUrl != null ? mImageUrl.hashCode() : 0);
        result = 31 * result + (mSpotifyId != null ? mSpotifyId.hashCode() : 0);
        result = 31 * result + mSelectedTrackIndex;
        result = 31 * result + mSelectedTrackPosition;
        result = 31 * result + (mErrorLoadingTracks ? 1 : 0);
        return result;
    }

    public boolean hasNoTracks() {
        return mHasNoTracks;
    }

    public SpotifyTrack getCurrentTrack() {
        if (mTracksList.size() > 0 && mSelectedTrackIndex > -1) {
            return mTracksList.get(mSelectedTrackIndex);
        }
        return null;
    }

    public interface OnTracksLoadListener {
        void onTracksLoaded(SpotifyArtist artist);
    }
}