package com.stylingandroid.downloadmanager;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.StringRes;

class Downloader implements DownloadReceiver.Listener {
    private static final String DIRECTORY = "Download/DownloadManager";
    private final Listener listener;
    private final DownloadManager downloadManager;

    private DownloadReceiver receiver = null;

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
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(getString(R.string.notification_title));
            request.setDescription(getString(R.string.notification_description));
            request.setDestinationInExternalPublicDir(DIRECTORY, uri.getLastPathSegment());
            downloadId = downloadManager.enqueue(request);
            register();
        }
    }

    private String getString(@StringRes int resId) {
        return listener.getContext().getString(resId);
    }

    boolean isDownloading() {
        return downloadId >= 0;
    }

    void register() {
        if (receiver == null && isDownloading()) {
            receiver = new DownloadReceiver(this);
            receiver.register(listener.getContext());
        }
    }

    @Override
    public void downloadComplete(long completedDownloadId) {
        if (downloadId == completedDownloadId) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            downloadId = -1;
            unregister();
            Cursor cursor = downloadManager.query(query);
            while (cursor.moveToNext()) {
                getFileInfo(cursor);
            }
            cursor.close();
        }
    }

    void unregister() {
        if (receiver != null) {
            receiver.unregister(listener.getContext());
        }
        receiver = null;
    }

    private void getFileInfo(Cursor cursor) {
        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            Long id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
            Uri uri = downloadManager.getUriForDownloadedFile(id);
            String mimeType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
            listener.fileDownloaded(uri, mimeType);
        }
    }

    void cancel() {
        if (isDownloading()) {
            downloadManager.remove(downloadId);
            downloadId = -1;
            unregister();
        }
    }

    interface Listener {
        void fileDownloaded(Uri uri, String mimeType);
        Context getContext();
    }
}
