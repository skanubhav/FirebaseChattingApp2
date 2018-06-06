package com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders;

import android.view.View;

import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatHolder;
import com.anubhav.firebasechattingapp2.R;
import com.google.android.exoplayer2.ui.PlayerView;

public class AudioViewHolder extends ChatHolder {

    public PlayerView audio_play;

    public AudioViewHolder(View view) {
        super(view);
        audio_play = view.findViewById(R.id.audio_play);
    }
}
