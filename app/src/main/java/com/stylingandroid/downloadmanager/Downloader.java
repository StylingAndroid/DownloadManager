package com.stylingandroid.downloadmanager;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

class Downloader implements DownloadReceiver.Listener {
    private final Listener listener;
    private final DownloadManager downloadManager;

    private DownloadReceiver receiver;

    private long downloadId = -1;

    static Downloader newInstance(Listener listener) {
        Context context = listener.getContext();
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        return new Downloader(downloadManager, listener);
    }

    Downloader(DownloadManager downloadManager, Listener listener) {
        this.downloadManager = downloadManager;
        this.listener = listener;
    }

    void download(Uri uri) {
        if (!isDownloading()) {
            receiver = new DownloadReceiver(this);
            receiver.register(listener.getContext());
            DownloadManager.Request request = new DownloadManager.Request(uri);
            downloadId = downloadManager.enqueue(request);
        }
    }

    @Override
    public void downloadComplete(long completedDownloadId) {
        if (downloadId == completedDownloadId) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            downloadId = -1;
            receiver.unregister(listener.getContext());
            Cursor cursor = downloadManager.query(query);
            while (cursor.moveToNext()) {
                getFileInfo(cursor);
            }
            cursor.close();
        }
    }

    private void getFileInfo(Cursor cursor) {
        String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
        String mimeType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
        Uri uri = Uri.parse(uriString);
        listener.fileDownloaded(uri, mimeType);
    }

    boolean isDownloading() {
        return downloadId >= 0;
    }

    void cancel() {
        if (isDownloading()) {
            downloadManager.remove(downloadId);
            downloadId = -1;
            receiver.unregister(listener.getContext());
        }
    }

    interface Listener {
        void fileDownloaded(Uri uri, String mimeType);
        Context getContext();
    }
}
