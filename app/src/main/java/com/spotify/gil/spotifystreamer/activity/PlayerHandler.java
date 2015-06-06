package com.spotify.gil.spotifystreamer.activity;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by GIL on 01/06/2015.
 */
public class PlayerHandler extends Handler {

    public static final int MSG_UPDATE_SEEKBAR = 1;
    private final WeakReference<PlayerActivity> mActivityInstance;

    public PlayerHandler(final PlayerActivity activity) {
        mActivityInstance = new WeakReference<PlayerActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {

        final PlayerActivity instance = mActivityInstance.get();

        if (instance == null) return;

        if (msg.what == MSG_UPDATE_SEEKBAR) {
            instance.updateProgress();
            sendMessageDelayed(obtainMessage(MSG_UPDATE_SEEKBAR), 100);
        }
    }
}
