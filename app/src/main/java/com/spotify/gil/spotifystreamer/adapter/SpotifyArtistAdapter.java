package com.spotify.gil.spotifystreamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.util.Spotify;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by GIL on 30/05/2015.
 */
public class SpotifyArtistAdapter extends ArrayAdapter<Artist> {
    private final LayoutInflater mLayoutInflater;

    public SpotifyArtistAdapter(Context context, int resource) {
        super(context, resource);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.artist_row,parent,false);
            final ArtistRowItemHolder holder = new ArtistRowItemHolder(convertView);
            convertView.setTag(holder);
        }

        final ArtistRowItemHolder holder = (ArtistRowItemHolder) convertView.getTag();
        final Artist artist = getItem(position);
        holder.setArtist(artist);

        return convertView;
    }

    private class ArtistRowItemHolder {
        private final ImageView mImageView;
        private final TextView mTextView;

        public ArtistRowItemHolder(final View convertView) {
            mImageView = (ImageView) convertView.findViewById(R.id.list_row_image);
            mTextView = (TextView) convertView.findViewById(R.id.list_row_text);
        }

        public void setArtist(Artist artist) {
            if (artist != null) {
                mTextView.setText(artist.name);
                Spotify.setupImage(artist.images, mImageView, Spotify.ImageSize.SMALL);
            } else {
                mTextView.setText(null);
                mImageView.setImageDrawable(null);
            }
        }
    }
}
