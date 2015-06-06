package com.spotify.gil.spotifystreamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.internal.SpotifyTrack;
import com.squareup.picasso.Picasso;

/**
 * Created by GIL on 30/05/2015.
 */
public class SpotifySongsAdapter extends ArrayAdapter<SpotifyTrack> {
    private final LayoutInflater mLayoutInflater;

    public SpotifySongsAdapter(Context context, int resource) {
        super(context, resource);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.song_row,parent,false);
            final SongRowItemHolder holder = new SongRowItemHolder(convertView);
            convertView.setTag(holder);
        }

        final SongRowItemHolder holder = (SongRowItemHolder) convertView.getTag();
        final SpotifyTrack track = getItem(position);
        holder.setSong(track);

        return convertView;
    }

    private class SongRowItemHolder {
        private final ImageView mImageView;
        private final TextView mSongNameTextView;
        private final TextView mAlbumNameTextView;

        public SongRowItemHolder(final View convertView) {
            mImageView = (ImageView) convertView.findViewById(R.id.list_row_image);
            mSongNameTextView = (TextView) convertView.findViewById(R.id.list_row_song);
            mAlbumNameTextView = (TextView) convertView.findViewById(R.id.list_row_album);
        }

        public void setSong(SpotifyTrack track) {
            if (track != null) {
                mSongNameTextView.setText(track.getTrackName());
                mAlbumNameTextView.setText(track.getAlbumName());
                Picasso.with(mImageView.getContext()).load(track.getThumbnailUrl()).into(mImageView);
            } else {
                mSongNameTextView.setText(null);
                mAlbumNameTextView.setText(null);
                mImageView.setImageDrawable(null);
            }
        }
    }
}
