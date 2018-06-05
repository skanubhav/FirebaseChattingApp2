package com.anubhav.firebasechattingapp2.ChatViewHolders;

import android.view.View;
import android.widget.TextView;

import com.anubhav.firebasechattingapp2.ChatHolder;
import com.anubhav.firebasechattingapp2.R;

public class TextViewHolder extends ChatHolder{

    public TextView message_text;

    public TextViewHolder(View view) {
        super(view);
        message_text = view.findViewById(R.id.message_text);
    }
}
