package com.anubhav.firebasechattingapp2.ChatActivityPackage;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anubhav.firebasechattingapp2.R;

public class ChatHolder extends RecyclerView.ViewHolder {

    TextView message_time;
    CardView message_layout;
    RelativeLayout message_relativelayout;
    ImageView message_download;
    ProgressBar message_download_progress;

   public ChatHolder (View view) {
        super(view);
        message_time = view.findViewById(R.id.message_time);
        message_layout = view.findViewById(R.id.message_layout);
        message_relativelayout = view.findViewById(R.id.message_relativelayout);
        message_download = view.findViewById(R.id.download_button);
        message_download_progress = view.findViewById(R.id.download_progress);
    }
}
