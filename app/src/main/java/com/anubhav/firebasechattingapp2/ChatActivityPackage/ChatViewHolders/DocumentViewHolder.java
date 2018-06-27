package com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatHolder;
import com.anubhav.firebasechattingapp2.R;

public class DocumentViewHolder extends ChatHolder {

    public ImageButton message_document;
    public TextView message_document_name;

    public DocumentViewHolder(View view) {
        super(view);
        message_document = view.findViewById(R.id.message_document);
        message_document_name = view.findViewById(R.id.messages_document_name);
    }
}
