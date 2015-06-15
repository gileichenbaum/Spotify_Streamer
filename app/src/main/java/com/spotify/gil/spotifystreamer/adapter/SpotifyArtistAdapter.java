package com.spotify.gil.spotifystreamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.spotify.gil.spotifystreamer.util.Spotify;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by GIL on 30/05/2015.
 */
public class SpotifyArtistAdapter extends ArrayAdapter<SpotifyArtist> {
    private final LayoutInflater mLayoutInflater;
    private SpotifyArtist mLoadingItem;

    public SpotifyArtistAdapter(Context context, int resource) {
        super(context, resource);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.artist_row, parent, false);
            final ArtistRowItemHolder holder = new ArtistRowItemHolder(convertView);
            convertView.setTag(holder);
        }

        final ArtistRowItemHolder holder = (ArtistRowItemHolder) convertView.getTag();
        final SpotifyArtist artist = getItem(position);
        holder.setArtist(artist);

        return convertView;
    }

    public void setHasMore(final boolean hasMore) {
        if (hasMore && mLoadingItem == null) {
            mLoadingItem = new SpotifyArtist((Artist) null);
            add(mLoadingItem);
        } else if (!hasMore && mLoadingItem != null) {
            remove(mLoadingItem);
            mLoadingItem = null;
        }
    }

    public boolean hasMore() {
        return mLoadingItem != null;
    }

    private class ArtistRowItemHolder {
        private final ImageView mImageView;
        private final TextView mTextView;
        private final ProgressBar mLoadingProgressBar;
        private final TextView mLoadingTextView;

        public ArtistRowItemHolder(final View convertView) {
            mImageView = (ImageView) convertView.findViewById(R.id.list_row_image);
            mTextView = (TextView) convertView.findViewById(R.id.list_row_text);
            mLoadingTextView = (TextView) convertView.findViewById(R.id.list_row_loading_text);
            mLoadingProgressBar = (ProgressBar) convertView.findViewById(R.id.progressbar);
        }

        public void setArtist(SpotifyArtist artist) {
            if (artist != null) {
                if (artist.isEmpty()) {
                    mTextView.setVisibility(View.INVISIBLE);
                    mImageView.setVisibility(View.INVISIBLE);
                    mLoadingTextView.setVisibility(View.VISIBLE);
                    mLoadingProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mTextView.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.VISIBLE);
                    mLoadingTextView.setVisibility(View.INVISIBLE);
                    mLoadingProgressBar.setVisibility(View.INVISIBLE);
                    mTextView.setText(artist.getName());
                    final String imageUrl = artist.getImageUrl();
                    Spotify.setupImage(mImageView, imageUrl);
                }
            } else {
                mTextView.setText(null);
                mImageView.setImageDrawable(null);
            }
        }
    }
}
