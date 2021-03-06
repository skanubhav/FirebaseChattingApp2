package com.anubhav.firebasechattingapp2.ChatActivityPackage;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

import com.anubhav.firebasechattingapp2.R;

public class VideoPlayerActivity extends AppCompatActivity {

    private Uri videoURI;
    private VideoView videoView;
    private MediaController mediaController;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoplayer_layout);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String videoURL = getIntent().getStringExtra("Video URL");
        videoView = findViewById(R.id.video_view);
        mediaController = new MediaController(this);

        videoURI = Uri.parse(videoURL);
        mediaController.setAnchorView(videoView);

        videoView.setMediaController(mediaController);
        videoView.setVideoURI(videoURI);
        videoView.requestFocus();
        videoView.start();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
