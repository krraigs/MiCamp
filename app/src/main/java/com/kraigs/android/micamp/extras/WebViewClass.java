package com.kraigs.android.micamp.extras;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.StrictMode;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.kraigs.android.micamp.R;

import static android.os.Build.VERSION.SDK_INT;

public class WebViewClass extends AppCompatActivity {

    private WebView myWebView;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        getSupportActionBar().setTitle("Website");

        loadingBar = new ProgressDialog(this);
        loadingBar.setMessage("Loading...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        Intent intent = getIntent();
        String webUrl = intent.getStringExtra("webUrl");

        myWebView = (WebView) findViewById(R.id.branchWebView);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl(webUrl);

        myWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
               loadingBar.dismiss();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loadingBar.dismiss();
            }
            });
//        myWebView.setWebViewClient(new WebViewClient());

        myWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {

                //                    int SDK_INT = android.os.Build.VERSION.SDK_INT;
                    if (SDK_INT > 8) {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                .permitAll().build();
                        StrictMode.setThreadPolicy(policy);

                        String downloadUrl = url;
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                        request.setDescription("Downloading...");
                        request.setTitle("Downloads");
// in order for this if to run, you must use the android 3.2 to compile your app
                        if (SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        }
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Documents.pdf");

// get download service and enqueue send_document
                        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                        Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_SHORT).show();

                        finish();

                    }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack())
            myWebView.goBack();
        else
            super.onBackPressed();
    }
}
