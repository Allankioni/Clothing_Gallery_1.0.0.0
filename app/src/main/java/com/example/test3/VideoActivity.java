package com.example.test3;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VideoActivity extends AppCompatActivity {
    WebView webView;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        webView = findViewById(R.id.videoView);
//        String video ="<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/dQw4w9WgXcQ\"title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen></iframe>";
//        webView.loadData(video, "text/html", "utf-8");
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
//        webView.setWebViewClient(new WebViewClient());

        webView.setWebViewClient(new WebViewClient());

        webView.loadUrl("https://youtu.be/dQw4w9WgXcQ?si=fZgGFPpgbEPOvyPC");

        WebSettings webSettings=webView.getSettings();

        webSettings.setJavaScriptEnabled(true);


    }
}