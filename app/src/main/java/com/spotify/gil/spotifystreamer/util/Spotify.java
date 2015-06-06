package com.spotify.gil.spotifystreamer.util;

import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by GIL on 30/05/2015.
 */
public class Spotify {

    private static final boolean DEBUG = false;

    //Album art thumbnail (large (640px for Now Playing screen)
    // and small (200px for list items))
    public enum ImageSize {
        SMALL(200), LARGE(640);

        private final int mPixelSize;

        private ImageSize(final int pixelSize) {
            mPixelSize = pixelSize;
        }
    }

    ;

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

    public static ArtistsPager searchArtists(String searchString) {
        return sSpotifyService.searchArtists(searchString + "*", ARTIST_SEARCH_OPTIONS);
    }

    public static Tracks getArtistSongs(String artistID) {
        return sSpotifyService.getArtistTopTrack(artistID, COUNTRY_OPTIONS);
    }

    public static void setupImage(List<Image> images, final ImageView imageView, final ImageSize size) {
        if (images != null && !images.isEmpty()) {
            final Image image = getImage(images, size);
            if (image != null) {
                Picasso.with(imageView.getContext()).load(image.url).into(imageView);
            } else {
                imageView.setImageDrawable(null);
            }
        } else {
            imageView.setImageDrawable(null);
        }
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
}
