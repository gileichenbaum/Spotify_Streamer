package com.spotify.gil.spotifystreamer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.fragment.ArtistTracksFragment;
import com.spotify.gil.spotifystreamer.fragment.Callbacks;
import com.spotify.gil.spotifystreamer.fragment.PlayerFragment;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.spotify.gil.spotifystreamer.internal.SpotifyTrack;
import com.spotify.gil.spotifystreamer.player.service.MediaPlayerListener;
import com.spotify.gil.spotifystreamer.player.service.PlayerService;
import com.spotify.gil.spotifystreamer.util.Spotify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.spotify.gil.spotifystreamer.util.Spotify.setupTracksData;

/**
 * Created by GIL on 20/06/2015 for Spotify Streamer.
 */
public abstract class PlayerActivityBase extends AppCompatActivity implements Callbacks, MediaPlayerListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_KEY_SHOW_NOTIFICATION = "show_notification";
    public static final String PREFERENCES_NAME = "prefs";
    protected final static String PLAYER_FRAGMENT_TAG = "player_fragment";
    private static final String PREF_KEY_USER_COUNTRY = "pref_user_country";
    private static final String TAG = "PlayerActivityBase";

    protected ArtistTracksFragment mArtistTracksFragment;
    protected PlayerFragment mPlayerFragment;
    protected PlayerService mPlayerService;
    protected SpotifyArtist mArtist;
    protected MenuItem mPlayerMenuItem;
    protected ShareActionProvider mShareActionProvider;
    private MenuItem mShareItem;
    private boolean mServiceConnected;
    private SharedPreferences mPreferences;
    private HashMap<String, String> mCountriesWithLocale;
    private SpotifyTrack mTrack;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceConnected = true;
            final PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            setupServiceListener(binder.getService());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceConnected = false;
            mPlayerService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Spotify.init();

        final String[] countryNames = getResources().getStringArray(R.array.country_names);
        final String[] countryCodes = getResources().getStringArray(R.array.country_codes);

        mCountriesWithLocale = new HashMap<>();
        for (int i = 0; i < countryNames.length; i++) {
            Log.i(TAG, countryNames[i] + " ," + countryCodes[i]);
            mCountriesWithLocale.put(countryNames[i], countryCodes[i]);
        }

        initPlayerService();
        mPreferences = getSharedPreferences(PREFERENCES_NAME, 0);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        Spotify.setUserCountry(mPreferences.getString(PREF_KEY_USER_COUNTRY, "US"));
        mArtist = setupTracksData(savedInstanceState, getIntent());
    }

    protected void initPlayerService() {
        if (!mServiceConnected || mPlayerService == null) {
            final Context applicationContext = getApplicationContext();
            final Intent intent = new Intent(applicationContext, PlayerService.class);
            applicationContext.startService(intent);
            applicationContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    protected void setupServiceListener(PlayerService service) {
        mPlayerService = service;
        service.setMediaListener(this);
        if (mArtist != null && mArtist.getTracks() != null && mArtist.getCurrentTrackIndex() > -1) {
            mPlayerService.setArtist(mArtist);
        } else if (mPlayerService != null && mPlayerService.getArtist() != null) {
            service.onConnected();
        }
        refreshPlayerMenuItemVisibility();
    }

    @Override
    public void seekTo(int position) {
        if (mServiceConnected && mPlayerService != null) {
            mPlayerService.seekTo(position);
        }
    }

    @Override
    public void nextTrack() {
        if (mServiceConnected && mPlayerService != null) {
            mPlayerService.nextTrack();
        }
    }

    @Override
    public void prevTrack() {
        if (mServiceConnected && mPlayerService != null) {
            mPlayerService.prevTrack();
        }
    }

    @Override
    public void togglePlayState() {
        if (mServiceConnected && mPlayerService != null) {
            mPlayerService.togglePlayState();
        }
    }

    @Override
    public void onPlayStateChanged(MediaPlayer mp, SpotifyTrack track, SpotifyArtist artist) {
        mArtist = artist;
        if (mPlayerFragment != null) {
            mPlayerFragment.onPlayStateChanged(mp, track, artist);
        }
        refreshPlayerMenuItemVisibility();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        refreshPlayerMenuItemVisibility();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp, SpotifyTrack track, SpotifyArtist artist) {
        if (mPlayerFragment != null) {
            mPlayerFragment.onPrepared(mp, track, artist);
        }
        if (mArtistTracksFragment != null) {
            mArtistTracksFragment.setTrack(track);
        }
        refreshPlayerMenuItemVisibility();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mArtist != null) {
            outState.putBundle(SpotifyArtist.ARTIST_BUNDLE, mArtist.toBundle());
        }
    }

    protected void refreshPlayerMenuItemVisibility() {

        if (mPlayerMenuItem != null) {
            if (mPlayerService != null) {
                final boolean visible = mPlayerMenuItem.isVisible();
                final boolean isPlaying = mPlayerService.isPlaying();
                if (isPlaying != visible) {
                    mPlayerMenuItem.setVisible(isPlaying);
                }
            }
        }

        if (mShareItem != null && mArtist != null) {

            final SpotifyTrack currentTrack = mArtist.getCurrentTrack();

            if (currentTrack != null && !currentTrack.equals(mTrack)) {
                final Intent shareIntent = buildShareIntent(currentTrack);
                final boolean sharingEnabled = mShareActionProvider != null && shareIntent != null;
                mShareItem.setVisible(sharingEnabled);
                if (sharingEnabled) {
                    mShareActionProvider.setShareIntent(shareIntent);
                }
                mTrack = currentTrack;
            }
        }
    }

    private Intent buildShareIntent(final SpotifyTrack track) {
        if (track != null) {
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, String.format("Check out %s by %s\r\n%s", track.getTrackName(), track.getArtistName(), track.getExternaUrl()));
            return shareIntent;
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        mPlayerMenuItem = menu.findItem(R.id.action_player);
        mShareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareItem);

        final MenuItem item = menu.findItem(R.id.country_spinner);
        final Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        final List<String> countries = new ArrayList<>(mCountriesWithLocale.keySet());
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countries));
        spinner.setSelection(countries.indexOf(mPreferences.getString(PREF_KEY_USER_COUNTRY, "USA")));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPreferences.edit().putString(PREF_KEY_USER_COUNTRY, mCountriesWithLocale.get(parent.getAdapter().getItem(position).toString())).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final MenuItem notificationSwitchItem = menu.findItem(R.id.show_notification_switch);
        final SwitchCompat switchCompat = (SwitchCompat) MenuItemCompat.getActionView(notificationSwitchItem);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switchCompat.setChecked(mPreferences.getBoolean(PREF_KEY_SHOW_NOTIFICATION, true));
            switchCompat.setText(R.string.switch_show_notification_text);
            switchCompat.setTextOn(getString(R.string.switch_show_notification_text_on));
            switchCompat.setTextOff(getString(R.string.switch_show_notification_text_off));
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mPreferences.edit().putBoolean(PREF_KEY_SHOW_NOTIFICATION, isChecked).apply();
                }
            });
        } else {
            notificationSwitchItem.setVisible(false);
        }

        refreshPlayerMenuItemVisibility();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void supportInvalidateOptionsMenu() {
        super.supportInvalidateOptionsMenu();
        refreshPlayerMenuItemVisibility();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREF_KEY_USER_COUNTRY.equals(key)) {
            Spotify.setUserCountry(mPreferences.getString(PREF_KEY_USER_COUNTRY, "US"));
            onUserCountryChanged();
        }
    }

    protected void onUserCountryChanged() {
    }
}
