package com.spotify.gil.spotifystreamer.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.adapter.SpotifyArtistAdapter;
import com.spotify.gil.spotifystreamer.async.SearchArtistsAsyncTask;
import com.spotify.gil.spotifystreamer.util.Spotify;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.ArtistsPager;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private SpotifyArtistAdapter mArtistAdapter;
    private AsyncTask<String, Artists, ArtistsPager> mSearchTask;
    private Toast mEmptyResultToast;
    private TextView mSearchTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Spotify.init();
        setContentView(R.layout.activity_main);

        mSearchTextView = (TextView) findViewById(R.id.mainpage_txt_search);
        mSearchTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                if (mSearchTask != null) {
                    mSearchTask.cancel(false);
                }

                mArtistAdapter.clear();

                if (!TextUtils.isEmpty(s)) {
                    searchForArtist(s.toString());
                }
            }
        });

        final ListView mListView = (ListView) findViewById(R.id.list);
        mArtistAdapter = new SpotifyArtistAdapter(this,R.layout.artist_row);
        mListView.setAdapter(mArtistAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Artist artist = mArtistAdapter.getItem(position);
                final Intent intent = new Intent();
                intent.setClass(view.getContext(), TracksActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(TracksActivity.ARTIST_NAME,artist.name);
                intent.putExtra(TracksActivity.ARTIST_ID, artist.id);
                view.getContext().startActivity(intent);
            }
        });
    }

    private void searchForArtist(String searchString) {

        mSearchTask = new SearchArtistsAsyncTask(){

            @Override
            protected void onPostExecute(ArtistsPager artistsPager) {

                if (mEmptyResultToast != null) {
                    mEmptyResultToast.cancel();
                }

                if (isCancelled()) return;

                if (artistsPager != null && artistsPager.artists != null && artistsPager.artists.items != null && artistsPager.artists.items.size() > 0) {
                    mArtistAdapter.addAll(artistsPager.artists.items);
                } else {
                    showNoResultsToast();
                }
            }

        }.execute(searchString);
    }

    private void showNoResultsToast() {

        final String message = getString(R.string.no_results_found,mSearchTextView.getText());
        mEmptyResultToast = makeText(this, message , Toast.LENGTH_LONG);
        mEmptyResultToast.show();
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
