package com.spotify.gil.spotifystreamer.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.adapter.SpotifyArtistAdapter;
import com.spotify.gil.spotifystreamer.async.SearchArtistsAsyncTask;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.spotify.gil.spotifystreamer.util.Spotify;

import java.util.LinkedList;

import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.ArtistsPager;

public class ArtistListFragment extends Fragment {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String SEARCH_TEXT = "search_text";
    private static final String ARTIST_LIST = "artist_list";

    private int mActivatedPosition = ListView.INVALID_POSITION;
    private int mListChoiceMode;
    private String mSearchString;

    private Callbacks mCallbacks = Callbacks.EMPTY_CALLBACK;

    private ListView mListView;
    private TextView mSearchTextView;

    private SpotifyArtistAdapter mArtistAdapter;

    private Toast mEmptyResultToast;

    private AsyncTask<Object, Artists, ArtistsPager> mSearchTask;

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            if (mSearchTask != null) {
                mSearchTask.cancel(false);
            }

            if (!TextUtils.isEmpty(s) && (mSearchString == null || !mSearchString.contentEquals(s))) {
                mArtistAdapter.clear();
                searchForArtist(s.toString());
            }
        }
    };

    public ArtistListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSearchTextView = (TextView) view.findViewById(R.id.mainpage_txt_search);

        mListView = (ListView) view.findViewById(R.id.list);
        mListView.setChoiceMode(mListChoiceMode);
        if (mArtistAdapter == null) {
            mArtistAdapter = new SpotifyArtistAdapter(view.getContext(), R.layout.artist_row);
        }
        mListView.setAdapter(mArtistAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onArtistSelected(mArtistAdapter.getItem(position));
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (totalItemCount > 0 && firstVisibleItem > 0 && visibleItemCount > 0 && mArtistAdapter.hasMore() && firstVisibleItem + (visibleItemCount * 2) >= totalItemCount
                        && (mSearchTask == null || mSearchTask.getStatus() == AsyncTask.Status.FINISHED)) {
                    searchForArtist(mSearchString);
                }
            }
        });

        restoreState(savedInstanceState);
        mSearchTextView.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSearchTextView.removeTextChangedListener(mTextWatcher);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
                setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
            }

            if (savedInstanceState.containsKey(ARTIST_LIST)) {

                final Bundle list = savedInstanceState.getBundle(ARTIST_LIST);

                if (list != null && list.keySet() != null) {
                    final LinkedList<SpotifyArtist> artists = new LinkedList<>();
                    for (String key : list.keySet()) {
                        artists.add(Integer.parseInt(key), new SpotifyArtist(list.getBundle(key)));
                    }

                    mArtistAdapter.addAll(artists);
                }
            }

            if (savedInstanceState.containsKey(SEARCH_TEXT)) {
                mSearchString = savedInstanceState.getString(SEARCH_TEXT);
            }
        }
    }

    private void searchForArtist(String searchString) {

        if (mEmptyResultToast != null) {
            mEmptyResultToast.cancel();
        }

        final FragmentActivity activity = getActivity();

        if (activity == null) return;

        if (Spotify.isConnected(activity)) {

            mSearchString = searchString;
            mSearchTask = new SearchArtistsAsyncTask() {

                @Override
                protected void onPostExecute(ArtistsPager artistsPager) {

                    if (mEmptyResultToast != null) {
                        mEmptyResultToast.cancel();
                    }

                    if (isCancelled()) return;

                    if (artistsPager != null && artistsPager.artists != null && artistsPager.artists.items != null && artistsPager.artists.items.size() > 0) {

                        mArtistAdapter.setHasMore(false);

                        for (int i = 0; i < artistsPager.artists.items.size(); i++) {
                            mArtistAdapter.add(new SpotifyArtist(artistsPager.artists.items.get(i)));
                        }

                        mArtistAdapter.setHasMore(artistsPager.artists.total > mArtistAdapter.getCount());

                    } else {
                        showNoResultsToast();
                    }
                }

            }.execute(searchString, mArtistAdapter.getCount());
        } else {
            mEmptyResultToast = Spotify.showNotConnected(activity);
        }
    }

    private void showNoResultsToast() {

        final Context context = getActivity();
        if (context == null) return;

        if (mArtistAdapter.getCount() <= 0 && Spotify.isConnected(context)) {
            final String message = getString(R.string.no_results_found, mSearchTextView.getText());
            mEmptyResultToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
            mEmptyResultToast.show();
        } else {
            mEmptyResultToast = Spotify.showNotConnected(context);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = Callbacks.EMPTY_CALLBACK;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveInstanceState(outState);
    }

    public void saveInstanceState(Bundle outState) {
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }

        if (mSearchTextView != null && mSearchTextView.getText() != null) {
            outState.putString(SEARCH_TEXT, mSearchTextView.getText().toString());
        }

        final Bundle list = new Bundle();

        for (int i = 0; i < mArtistAdapter.getCount(); i++) {
            list.putBundle(String.valueOf(i), mArtistAdapter.getItem(i).toBundle());
        }

        outState.putBundle(ARTIST_LIST, list);
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        mListChoiceMode = activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE;
        if (mListView != null) {
            mListView.setChoiceMode(mListChoiceMode);
        }
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            mListView.setItemChecked(mActivatedPosition, false);
        } else {
            mListView.setItemChecked(position, true);
        }
        mActivatedPosition = position;
    }
}
