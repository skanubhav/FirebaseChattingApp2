package com.anubhav.firebasechattingapp2.ChatActivityPackage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.anubhav.firebasechattingapp2.R;

public class DocumentViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_viewer_activity);

        WebView webView = findViewById(R.id.document_viewer);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + getIntent().getStringExtra("DocumentURL"));

        webView.setWebViewClient(new WebViewClient());
    }
}
