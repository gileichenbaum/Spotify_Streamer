package com.spotify.gil.spotifystreamer.activity;

import android.os.Handler;
import android.os.Message;

import com.spotify.gil.spotifystreamer.fragment.PlayerFragment;

import java.lang.ref.WeakReference;

/**
 * Created by GIL on 01/06/2015 for Spotify Streamer.
 */
public class PlayerHandler extends Handler {

    public static final int MSG_UPDATE_SEEKBAR = 1;
    private final WeakReference<PlayerFragment> mActivityInstance;

    public PlayerHandler(final PlayerFragment fragment) {
        mActivityInstance = new WeakReference<>(fragment);
    }

    @Override
    public void handleMessage(Message msg) {

        final PlayerFragment instance = mActivityInstance.get();

        if (instance == null) return;

        if (msg.what == MSG_UPDATE_SEEKBAR) {
            instance.updateProgress();
            sendMessageDelayed(obtainMessage(MSG_UPDATE_SEEKBAR), 100);
        }
    }
}
