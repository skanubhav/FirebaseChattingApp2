package com.anubhav.firebasechattingapp2.ChatActivityPackage;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.anubhav.firebasechattingapp2.R;

public class ForwardUserHolder extends RecyclerView.ViewHolder {

    TextView user;
    FrameLayout user_card;
    ImageView user_image;

    public ForwardUserHolder(View itemView) {
        super(itemView);
        user = itemView.findViewById(R.id.user);
        user_card = itemView.findViewById(R.id.user_card);
        user_image = itemView.findViewById(R.id.profile_image);
    }
}
