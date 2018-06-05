package com.anubhav.firebasechattingapp2.ChatViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.anubhav.firebasechattingapp2.ChatHolder;
import com.anubhav.firebasechattingapp2.R;
import com.google.android.exoplayer2.ui.PlayerView;

public class AudioViewHolder extends ChatHolder {

    public PlayerView audio_play;

    public AudioViewHolder(View view) {
        super(view);
        audio_play = view.findViewById(R.id.audio_play);
    }
}
