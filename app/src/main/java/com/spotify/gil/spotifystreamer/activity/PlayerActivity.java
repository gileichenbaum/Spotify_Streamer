package com.spotify.gil.spotifystreamer.activity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.internal.SpotifyTrack;
import com.spotify.gil.spotifystreamer.util.Spotify;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public class PlayerActivity extends AppCompatActivity {

    public static final String AUTO_PLAY = "auto_play";
    private static final String TAG = PlayerActivity.class.getSimpleName();
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("mm:ss", Locale.US);
    private static final String CURRENT_TRACK_INDEX = "current_index";

    private MediaPlayer mMediaPlayer;
    private ImageButton mPlayButton;
    private SeekBar mSeekbar;
    private TextView mCurrentTimeTxt;
    private  PlayerHandler mHandler;
    private ArrayList<SpotifyTrack> mTracks;
    private int mCurrentTrackIndex;
    private ImageView mImageView;
    private TextView mAlbumNameTxt;
    private TextView mArtistNameTxt;
    private TextView mTrackNameTxt;
    private SpotifyTrack mCurrentTrack;
    private View mPrevTrackBtn;
    private View mNextTrackBtn;
    private int mRetryCount;
    private Toast mNoConnectionToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mHandler = new PlayerHandler(this);

        final Intent intent = getIntent();

        if (intent != null && intent.hasExtra(SpotifyTrack.TRACKS_BUNDLE)) {

            final Bundle tracksBundle = intent.getBundleExtra(SpotifyTrack.TRACKS_BUNDLE);

            final Set<String> trackNames = tracksBundle.keySet();
            mTracks = new ArrayList<SpotifyTrack>();

            SpotifyTrack trackToPlay = null;

            for (String trackName: trackNames) {
                final Bundle bundle = tracksBundle.getBundle(trackName);
                final SpotifyTrack track = new SpotifyTrack(bundle);
                mTracks.add(track);
                if (bundle.getBoolean(AUTO_PLAY,false)) {
                    trackToPlay = track;
                }
            }

            Collections.sort(mTracks);

            if (trackToPlay != null) {
                mCurrentTrackIndex = mTracks.indexOf(trackToPlay);
            }

            mSeekbar = (SeekBar) findViewById(R.id.player_seekbar);
            mPlayButton = (ImageButton) findViewById(R.id.player_btn_play);
            mImageView = (ImageView) findViewById(R.id.player_img_album);

            mCurrentTimeTxt = (TextView) findViewById(R.id.player_txt_current_time);
            mAlbumNameTxt = ((TextView) findViewById(R.id.player_txt_album));
            mArtistNameTxt = ((TextView) findViewById(R.id.player_txt_artist));
            mTrackNameTxt = ((TextView) findViewById(R.id.player_txt_track_name));

            mPrevTrackBtn = findViewById(R.id.player_btn_fr);
            mNextTrackBtn = findViewById(R.id.player_btn_ff);

            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            initListeners();

            onTrackChanged();

        }
    }

    private void onTrackChanged() {

        if (Spotify.isConnected(this)) {

            if (mNoConnectionToast != null) {
                mNoConnectionToast.cancel();
            }

            mHandler.removeMessages(PlayerHandler.MSG_UPDATE_SEEKBAR);

            final SpotifyTrack track = mTracks.get(mCurrentTrackIndex);

            if (mCurrentTrack != track) {

                mCurrentTrack = track;

                Picasso.with(this).load(track.getArtUrl()).into(mImageView);
                mAlbumNameTxt.setText(track.getAlbumName());
                mArtistNameTxt.setText(track.getArtistName());
                mTrackNameTxt.setText(track.getTrackName());

                try {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(track.getTrackUri());
                    mMediaPlayer.prepareAsync();

                } catch (IllegalStateException il) {
                    if (mRetryCount < 10) {
                        mRetryCount++;
                        onTrackChanged();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mNoConnectionToast = Spotify.showNotConnected(this);
        }
    }

    private void initListeners() {

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayState();
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mSeekbar.setProgress(0);
                mSeekbar.setMax(mp.getDuration());
                ((TextView) findViewById(R.id.player_txt_total_time)).setText(DATE_FORMAT.format(mp.getDuration()));
                mp.start();
                onPlayStateChanged();
                mRetryCount = 0;
                setMediaControlsEnabled(true);
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                onPlayStateChanged();
                
                if (mp.getCurrentPosition() > 0 && hasMoreTracks()) {
                    mCurrentTrackIndex++;
                    onTrackChanged();
                }
            }
        });

        final View.OnClickListener trackChangeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setMediaControlsEnabled(false);
                final int id = v.getId();

                if (id == mNextTrackBtn.getId()) {
                    mCurrentTrackIndex++;
                } else if (id == mPrevTrackBtn.getId()){
                    mCurrentTrackIndex--;
                }

                onTrackChanged();
            }
        };

        mPrevTrackBtn.setOnClickListener(trackChangeClickListener);
        mNextTrackBtn.setOnClickListener(trackChangeClickListener);
    }

    private void setMediaControlsEnabled(final boolean enabled) {
        mPrevTrackBtn.setEnabled(enabled && mCurrentTrackIndex > 0);
        mNextTrackBtn.setEnabled(enabled && hasMoreTracks());
    }

    private boolean hasMoreTracks() {
        return mCurrentTrackIndex < mTracks.size() - 1;
    }

    private void onPlayStateChanged() {
        if (mMediaPlayer.isPlaying()) {
            mPlayButton.setImageResource(R.drawable.ic_pause_black_48dp);
            mHandler.handleMessage(mHandler.obtainMessage(PlayerHandler.MSG_UPDATE_SEEKBAR));
        } else {
            mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
            mHandler.removeMessages(PlayerHandler.MSG_UPDATE_SEEKBAR);
            updateProgress();
        }
    }

    private void togglePlayState() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
        onPlayStateChanged();
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
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

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        mHandler.removeMessages(PlayerHandler.MSG_UPDATE_SEEKBAR);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(PlayerHandler.MSG_UPDATE_SEEKBAR);
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void updateProgress() {
        final int progress = mMediaPlayer.getCurrentPosition();
        mSeekbar.setProgress(progress);
        mCurrentTimeTxt.setText(DATE_FORMAT.format(progress));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final Intent intent = getIntent();
        if (intent != null) {
            final Bundle bundleExtra = intent.getBundleExtra(SpotifyTrack.TRACKS_BUNDLE);
            if (bundleExtra != null) {
                outState.putBundle(SpotifyTrack.TRACKS_BUNDLE, bundleExtra);
                outState.putInt(CURRENT_TRACK_INDEX, mCurrentTrackIndex);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final Bundle tracksBundle = savedInstanceState.getBundle(SpotifyTrack.TRACKS_BUNDLE);

        if (tracksBundle != null) {
            final Set<String> trackNames = tracksBundle.keySet();
            mTracks = new ArrayList<SpotifyTrack>();

            SpotifyTrack trackToPlay = null;

            for (String trackName : trackNames) {
                final Bundle bundle = tracksBundle.getBundle(trackName);
                final SpotifyTrack track = new SpotifyTrack(bundle);
                mTracks.add(track);
                if (bundle.getBoolean(AUTO_PLAY, false)) {
                    trackToPlay = track;
                }
            }

            Collections.sort(mTracks);

            if (trackToPlay != null) {
                mCurrentTrackIndex = mTracks.indexOf(trackToPlay);
            }
        }

        if (savedInstanceState.containsKey(CURRENT_TRACK_INDEX)) {
            mCurrentTrackIndex = savedInstanceState.getInt(CURRENT_TRACK_INDEX);
        }
    }
}
