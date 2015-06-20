package com.spotify.gil.spotifystreamer.fragment;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spotify.gil.spotifystreamer.R;
import com.spotify.gil.spotifystreamer.internal.SpotifyArtist;
import com.spotify.gil.spotifystreamer.internal.SpotifyTrack;
import com.spotify.gil.spotifystreamer.player.service.MediaPlayerListener;
import com.spotify.gil.spotifystreamer.util.Spotify;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PlayerFragment extends DialogFragment implements MediaPlayerListener {

    public static final String SHOW_AS_DIALOG = "show_as_dialog";
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("mm:ss", Locale.US);
    private ImageButton mPlayButton;
    private SeekBar mSeekbar;
    private TextView mCurrentTimeTxt;
    private ImageView mImageView;
    private TextView mAlbumNameTxt;
    private TextView mArtistNameTxt;
    private TextView mTrackNameTxt;
    private View mPrevTrackBtn;
    private View mNextTrackBtn;
    private TextView mDurationTxt;
    private Callbacks mCallbacks;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args != null) {
            setShowsDialog(args.getBoolean(SHOW_AS_DIALOG, true));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
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

        initListeners();
    }

    private void initListeners() {

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.togglePlayState();
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mCallbacks.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final View.OnClickListener trackChangeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mNextTrackBtn.setEnabled(false);
                mPrevTrackBtn.setEnabled(false);

                final int id = v.getId();

                if (id == mNextTrackBtn.getId()) {
                    mCallbacks.nextTrack();
                } else if (id == mPrevTrackBtn.getId()) {
                    mCallbacks.prevTrack();
                }
            }
        };

        mPrevTrackBtn.setOnClickListener(trackChangeClickListener);
        mNextTrackBtn.setOnClickListener(trackChangeClickListener);
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

    private void updateProgress(final MediaPlayer mp) {
        if (mp == null) {
            mSeekbar.setProgress(0);
            mCurrentTimeTxt.setText(null);
        } else {
            final int progress = mp.getCurrentPosition();
            mSeekbar.setProgress(progress);
            mCurrentTimeTxt.setText(DATE_FORMAT.format(progress));
        }
    }

    @Override
    public void onPlayStateChanged(MediaPlayer mp, SpotifyTrack track) {
        if (mp != null && mp.isPlaying()) {
            mPlayButton.setImageResource(R.drawable.ic_pause_black_48dp);
        } else {
            mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        }
        updateProgress(mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp, SpotifyTrack track, SpotifyArtist artist) {

        mPrevTrackBtn.setEnabled(artist != null && artist.getCurrentTrackIndex() > 0);
        mNextTrackBtn.setEnabled(artist != null && artist.hasMoreTracks());

        mDurationTxt.setText(DATE_FORMAT.format(mp.getDuration()));
        mSeekbar.setMax(mp.getDuration());

        final String artUrl = track.getArtUrl();
        Spotify.setupImage(mImageView, artUrl);

        mAlbumNameTxt.setText(track.getAlbumName());
        mArtistNameTxt.setText(track.getArtistName());
        mTrackNameTxt.setText(track.getTrackName());
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
}
