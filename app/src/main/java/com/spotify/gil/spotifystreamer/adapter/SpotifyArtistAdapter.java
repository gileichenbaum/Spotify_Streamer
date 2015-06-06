package com.spotify.gil.spotifystreamer.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.async.SearchArtistsAsyncTask;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Created by GIL on 30/05/2015.
 */
public class SpotifyArtistAdapter extends ArrayAdapter<SpotifyArtist> {
    private final LayoutInflater mLayoutInflater;
    private final SpotifyArtist mLoadingItem;
    private boolean mHasMore;
    private AsyncTask<Object, Artists, ArtistsPager> mSearchTask;
    private String mSearchString;

    public SpotifyArtistAdapter(Context context, int resource) {
        super(context, resource);
        mLayoutInflater = LayoutInflater.from(context);
        mLoadingItem = new SpotifyArtist(null);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (mHasMore && position + 5 > getCount() && (mSearchTask == null || mSearchTask.getStatus() == AsyncTask.Status.FINISHED)) {

            mSearchTask = new SearchArtistsAsyncTask() {

                @Override
                protected void onPostExecute(ArtistsPager artistsPager) {

                    remove(mLoadingItem);

                    if (isCancelled()) return;

                    if (artistsPager != null && artistsPager.artists != null) {

                        if (artistsPager.artists.items != null && artistsPager.artists.items.size() > 0) {
                            for (int i = 0; i < artistsPager.artists.items.size(); i++) {
                                add(new SpotifyArtist(artistsPager.artists.items.get(i)));
                            }
                        }

                        setHasMore(artistsPager.artists.total > getCount());
                    }
                }
            }.execute(mSearchString, getCount());
        }

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
        mSearchTask = null;
        mHasMore = hasMore;
        if (mHasMore) {
            add(mLoadingItem);
        }
    }

    public void setSearchString(String searchString) {
        mSearchString = searchString;
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
                    Picasso.with(mImageView.getContext()).load(artist.getImageUrl()).into(mImageView);
                }
            } else {
                mTextView.setText(null);
                mImageView.setImageDrawable(null);
            }
        }
    }
}
