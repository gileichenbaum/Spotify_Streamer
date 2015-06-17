package com.spotify.gil.spotifystreamer.fragment;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.activity.PlayerHandler;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.spotify.gil.spotifystreamer.internal.SpotifyTrack;
import com.spotify.gil.spotifystreamer.util.Spotify;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PlayerFragment extends Fragment {

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("mm:ss", Locale.US);

    private SpotifyArtist mArtist;
    private MediaPlayer mMediaPlayer;
    private ImageButton mPlayButton;
    private SeekBar mSeekbar;
    private TextView mCurrentTimeTxt;
    private PlayerHandler mHandler;
    private List<SpotifyTrack> mTracks;
    private ImageView mImageView;
    private TextView mAlbumNameTxt;
    private TextView mArtistNameTxt;
    private TextView mTrackNameTxt;
    private SpotifyTrack mCurrentTrack;
    private View mPrevTrackBtn;
    private View mNextTrackBtn;
    private int mRetryCount;
    private Toast mNoConnectionToast;
    private TextView mDurationTxt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageView = (ImageView) view.findViewById(R.id.player_img_album);

        mCurrentTimeTxt = (TextView) view.findViewById(R.id.player_txt_current_time);
        mAlbumNameTxt = (TextView) view.findViewById(R.id.player_txt_album);
        mArtistNameTxt = (TextView) view.findViewById(R.id.player_txt_artist);
        mTrackNameTxt = (TextView) view.findViewById(R.id.player_txt_track_name);
        mDurationTxt = (TextView) view.findViewById(R.id.player_txt_total_time);

        mSeekbar = (SeekBar) view.findViewById(R.id.player_seekbar);
        mPlayButton = (ImageButton) view.findViewById(R.id.player_btn_play);
        mPrevTrackBtn = view.findViewById(R.id.player_btn_fr);
        mNextTrackBtn = view.findViewById(R.id.player_btn_ff);

        init();
        setupTracksData(savedInstanceState);
    }

    private void setupTracksData(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mArtist = new SpotifyArtist(savedInstanceState.getBundle(SpotifyArtist.ARTIST_BUNDLE));
        } else {
            final Bundle arguments = getArguments();
            if (arguments != null && arguments.containsKey(SpotifyArtist.ARTIST_BUNDLE)) {
                mArtist = new SpotifyArtist(arguments.getBundle(SpotifyArtist.ARTIST_BUNDLE));
            }
        }

        if (mArtist != null) {
            mTracks = mArtist.getTracks();
            onTrackChanged();
        }
    }

    private void init() {
        mHandler = new PlayerHandler(this);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        initListeners();
    }

    private void onTrackChanged() {

        final Context context = getActivity();

        if (context == null || mArtist == null) return;

        if (Spotify.isConnected(context)) {

            if (mNoConnectionToast != null) {
                mNoConnectionToast.cancel();
            }

            mHandler.removeMessages(PlayerHandler.MSG_UPDATE_SEEKBAR);

            final SpotifyTrack track = mTracks.get(mArtist.getCurrentTrackIndex());

            if (mCurrentTrack != track) {

                mCurrentTrack = track;

                final String artUrl = track.getArtUrl();
                Spotify.setupImage(mImageView, artUrl);

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
            mNoConnectionToast = Spotify.showNotConnected(context);
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
                if (fromUser && mMediaPlayer.isPlaying()) {
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

                mDurationTxt.setText(DATE_FORMAT.format(mp.getDuration()));
                setMediaControlsEnabled(true);
                mSeekbar.setMax(mp.getDuration());

                mp.start();

                final int currentTrackPosition = mArtist.getCurrentTrackPosition();
                if (currentTrackPosition > 0) {
                    mSeekbar.setProgress(currentTrackPosition);
                    mMediaPlayer.seekTo(currentTrackPosition);
                } else {
                    mSeekbar.setProgress(0);
                }

                onPlayStateChanged();
                mRetryCount = 0;
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                onPlayStateChanged();

                if (mp.getCurrentPosition() > 0 && mArtist != null && mArtist.hasMoreTracks()) {
                    mArtist.nextTrack();
                    onTrackChanged();
                }
            }
        });

        final View.OnClickListener trackChangeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setMediaControlsEnabled(false);
                final int id = v.getId();

                if (mArtist != null) {
                    if (id == mNextTrackBtn.getId()) {
                        mArtist.nextTrack();
                    } else if (id == mPrevTrackBtn.getId()) {
                        mArtist.prevTrack();
                    }
                }

                onTrackChanged();
            }
        };

        mPrevTrackBtn.setOnClickListener(trackChangeClickListener);
        mNextTrackBtn.setOnClickListener(trackChangeClickListener);
    }

    private void setMediaControlsEnabled(final boolean enabled) {
        mPrevTrackBtn.setEnabled(mArtist != null && enabled && mArtist.getCurrentTrackIndex() > 0);
        mNextTrackBtn.setEnabled(mArtist != null && enabled && mArtist.hasMoreTracks());
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
    public void onPause() {
        super.onPause();
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        mHandler.removeMessages(PlayerHandler.MSG_UPDATE_SEEKBAR);
    }

    @Override
    public void onDestroy() {
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
        if (mArtist != null) {
            mArtist.setCurrentTrackPosition(progress);
        }
        mCurrentTimeTxt.setText(DATE_FORMAT.format(progress));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mArtist != null) {
            outState.putBundle(SpotifyArtist.ARTIST_BUNDLE, mArtist.toBundle());
        }
    }

    public SpotifyArtist getArtist() {
        return mArtist;
    }

    public void setArtist(SpotifyArtist artist) {
        mArtist = artist;
        mTracks = mArtist.getTracks();
        onTrackChanged();
    }
}
