package com.anubhav.firebasechattingapp2.ChatViewHolders;

import android.view.View;
import android.widget.ImageView;

import com.anubhav.firebasechattingapp2.ChatHolder;
import com.anubhav.firebasechattingapp2.R;

public class ImageViewHolder extends ChatHolder {

    public ImageView message_image;

    public ImageViewHolder(View view) {
        super(view);
        message_image = view.findViewById(R.id.message_image);
    }
}
