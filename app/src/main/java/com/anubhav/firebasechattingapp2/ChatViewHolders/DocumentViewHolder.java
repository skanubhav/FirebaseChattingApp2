package com.anubhav.firebasechattingapp2.ChatViewHolders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.anubhav.firebasechattingapp2.ChatHolder;
import com.anubhav.firebasechattingapp2.R;

public class DocumentViewHolder extends ChatHolder {

    public ImageButton message_document;
    public DocumentViewHolder(View view) {
        super(view);
        message_document = view.findViewById(R.id.message_document);
    }
}
