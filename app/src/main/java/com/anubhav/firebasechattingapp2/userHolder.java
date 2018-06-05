package com.anubhav.firebasechattingapp2;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class userHolder extends RecyclerView.ViewHolder {
    TextView user;

    userHolder(View view)
    {
        super(view);
        user = view.findViewById(R.id.user);
    }
}
