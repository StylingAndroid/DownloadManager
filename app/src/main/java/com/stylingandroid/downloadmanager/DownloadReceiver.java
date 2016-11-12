package com.stylingandroid.downloadmanager;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

class DownloadReceiver extends BroadcastReceiver {
    private final Listener listener;

    DownloadReceiver(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        listener.downloadComplete(downloadId);
    }

    public void register(Context context) {
        IntentFilter downloadFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(this, downloadFilter);
    }

    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }

    interface Listener {
        void downloadComplete(long downloadId);
    }
}
