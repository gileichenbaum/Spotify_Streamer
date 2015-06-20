package com.spotify.gil.spotifystreamer.player.service;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by GIL on 01/06/2015 for Spotify Streamer.
 */
public class PlayerHandler extends Handler {

    public static final int MSG_UPDATE_SEEKBAR = 1;
    private final WeakReference<PlayerService> mServiceInstance;

    public PlayerHandler(final PlayerService fragment) {
        mServiceInstance = new WeakReference<>(fragment);
    }

    @Override
    public void handleMessage(Message msg) {

        final PlayerService instance = mServiceInstance.get();

        if (instance == null) {
            removeMessages(PlayerHandler.MSG_UPDATE_SEEKBAR);
            return;
        }

        if (msg.what == MSG_UPDATE_SEEKBAR) {
            instance.updateProgress();
            sendMessageDelayed(obtainMessage(MSG_UPDATE_SEEKBAR), 100);
        }
    }
}
