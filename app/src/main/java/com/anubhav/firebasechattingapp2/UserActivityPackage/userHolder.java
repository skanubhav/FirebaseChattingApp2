package com.anubhav.firebasechattingapp2.UserActivityPackage;

import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.anubhav.firebasechattingapp2.R;

public class userHolder extends RecyclerView.ViewHolder {
    TextView user;
    TextView user_last_message;
    CardView user_card;
    ImageView message_stat;
    ImageView user_image;

    userHolder(View view)
    {
        super(view);
        user = view.findViewById(R.id.user);
        user_last_message = view.findViewById(R.id.user_last_message);
        user_card = view.findViewById(R.id.user_card);
        message_stat = view.findViewById(R.id.message_stat);
        user_image = view.findViewById(R.id.profile_image);
    }

}
