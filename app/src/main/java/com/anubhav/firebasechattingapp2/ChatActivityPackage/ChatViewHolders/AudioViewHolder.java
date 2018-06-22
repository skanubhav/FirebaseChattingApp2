package com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatHolder;
import com.anubhav.firebasechattingapp2.R;
import com.google.android.exoplayer2.ui.PlayerView;

public class AudioViewHolder extends ChatHolder {

    public PlayerView audio_play;
    public ImageView start_audio;
    public TextView audio_name;

    public AudioViewHolder(View view) {
        super(view);
        audio_play = view.findViewById(R.id.audio_play);
        start_audio = view.findViewById(R.id.start_audio);
        audio_name = view.findViewById(R.id.audio_name);
    }
}
