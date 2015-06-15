package com.spotify.gil.spotifystreamer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.internal.SpotifyTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

import static android.widget.Toast.makeText;

/**
 * Created by GIL on 30/05/2015.
 */
public class Spotify {

    private static final boolean DEBUG = false;
    private final static HashMap<String, Object> COUNTRY_OPTIONS = new HashMap<String, Object>() {{
        put("country", "US");
    }};
    private final static HashMap<String, Object> ARTIST_SEARCH_OPTIONS = new HashMap<String, Object>() {{
        put("type", "artist");
    }};
    private static SpotifyService sSpotifyService;

    public static void init() {
        if (sSpotifyService == null) {
            final SpotifyApi spotifyApi = new SpotifyApi();
            sSpotifyService = spotifyApi.getService();
        }
    }

    public static ArtistsPager searchArtists(String searchString, Integer offset) {
        try {
            if (offset == null || offset == 0) {
                return sSpotifyService.searchArtists(searchString + "*", ARTIST_SEARCH_OPTIONS);
            } else {
                final HashMap<String, Object> params = new HashMap<>();
                params.put("type", "artist");
                params.put("offset", offset);
                return sSpotifyService.searchArtists(searchString + "*", params);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static List<SpotifyTrack> getArtistSongs(String artistID) {

        try {
            final Tracks artistTopTracks = sSpotifyService.getArtistTopTrack(artistID, COUNTRY_OPTIONS);
            if (artistTopTracks != null && artistTopTracks.tracks != null && artistTopTracks.tracks.size() > 0) {

                final List<SpotifyTrack> tracks = new ArrayList<>(artistTopTracks.tracks.size());

                int i = 0;

                for (Track track : artistTopTracks.tracks) {
                    tracks.add(new SpotifyTrack(i++, track));
                }

                return tracks;
            }

        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public static Image getImage(List<Image> images, ImageSize size) {

        if (images == null || images.isEmpty()) {
            return null;
        }

        int height = size.mPixelSize;
        int width = size.mPixelSize;

        Image selectedImage = null;

        int i = 0;

        for (Image image : images) {
            if (selectedImage == null ||
                    (Math.abs(selectedImage.width - width) > Math.abs(image.width - width) &&
                            Math.abs(selectedImage.height - height) > Math.abs(image.height - height))) {
                selectedImage = image;
            }
            if (DEBUG) {
                Log.i("image", "image [" + i + "] dimens=" + selectedImage.width + "x" + selectedImage.height);
                i++;
            }
        }

        if (DEBUG) {
            Log.i("image", "selected image dimens=" + selectedImage.width + "x" + selectedImage.height);
        }
        return selectedImage;
    }

    public static boolean isConnected(final Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static Toast showNotConnected(final Context context) {
        final Toast toast = makeText(context, R.string.no_connection, Toast.LENGTH_LONG);
        toast.show();
        return toast;
    }

    public static void setupImage(final ImageView imageView, final String artUrl) {

        if (imageView != null) {

            final Context context = imageView.getContext();

            if (context != null) {
                if (TextUtils.isEmpty(artUrl)) {
                    imageView.setImageDrawable(null);
                } else {
                    try {
                        Picasso.with(imageView.getContext()).load(artUrl).into(imageView);
                    } catch (Exception e) {
                        imageView.setImageDrawable(null);
                    }
                }
            }
        }
    }



    //Album art thumbnail (large (640px for Now Playing screen)
    // and small (200px for list items))
    public enum ImageSize {
        SMALL(200), LARGE(640);

        private final int mPixelSize;

        ImageSize(final int pixelSize) {
            mPixelSize = pixelSize;
        }
    }

}
