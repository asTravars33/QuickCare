package com.wangjessica.wecare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class VideosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        /*for(int i=1; i<=6; i++){
            setVideo(i);
        }*/
    }
    public void setVideo(int id){
        WebView youtubeWebView= findViewById(getResources().getIdentifier("vid"+id, "id", getPackageName()));
        String myVideoYoutubeId = youtubeWebView.getTag().toString();

        youtubeWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        WebSettings webSettings = youtubeWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        youtubeWebView.loadUrl("https://www.youtube.com/embed/" + myVideoYoutubeId);
    }
}