package com.anubhav.firebasechattingapp2.UserActivityPackage;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.anubhav.firebasechattingapp2.R;

public class userHolder extends RecyclerView.ViewHolder {
    TextView user;

    userHolder(View view)
    {
        super(view);
        user = view.findViewById(R.id.user);
    }
}
