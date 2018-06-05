package com.anubhav.firebasechattingapp2;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatHolder extends RecyclerView.ViewHolder {

    TextView message_time;
    CardView message_layout;
    RelativeLayout message_relativelayout;

   public ChatHolder (View view) {
        super(view);
        message_time = view.findViewById(R.id.message_time);
        message_layout = view.findViewById(R.id.message_layout);
        message_relativelayout = view.findViewById(R.id.message_relativelayout);
    }
}
