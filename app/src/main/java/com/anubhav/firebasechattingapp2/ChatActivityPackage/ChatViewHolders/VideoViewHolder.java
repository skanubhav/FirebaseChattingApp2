package com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders;

import android.view.View;
import android.widget.RelativeLayout;

import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatHolder;
import com.anubhav.firebasechattingapp2.R;

public class VideoViewHolder extends ChatHolder {

    public  RelativeLayout message_video;

    public VideoViewHolder(View view) {
        super(view);
        message_video = view.findViewById(R.id.message_video);
    }
}
