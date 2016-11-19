package com.stylingandroid.downloadmanager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements Downloader.Listener {
    private static final String URI_STRING = "http://www.cbu.edu.zm/downloads/pdf-sample.pdf";
    private static final String[] PERMISSIONS = new String[]{
            WRITE_EXTERNAL_STORAGE,
            READ_EXTERNAL_STORAGE,
            INTERNET
    };

    private Button download;

    private Downloader downloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        setContentView(R.layout.activity_main);

        download = (Button) findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadOrCancel();
            }
        });
        downloader = Downloader.newInstance(this);
    }

    private void checkPermissions() {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
                return;
            }
        }
    }

    void downloadOrCancel() {
        if (downloader.isDownloading()) {
            cancel();
        } else {
            download();
        }
        updateUi();
    }

    private void cancel() {
        downloader.cancel();
    }

    private void download() {
        Uri uri = Uri.parse(URI_STRING);
        downloader.download(uri);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!areAllPermissionsGranted(requestCode, grantResults)) {
            finish();
        }
    }

    private boolean areAllPermissionsGranted(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void fileDownloaded(Uri uri, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        startActivity(intent);
        updateUi();
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    private void updateUi() {
        if (downloader.isDownloading()) {
            download.setText(R.string.cancel);
        } else {
            download.setText(R.string.download);
        }
    }

    @Override
    protected void onDestroy() {
        downloader.unregister();
        super.onDestroy();
    }
}
