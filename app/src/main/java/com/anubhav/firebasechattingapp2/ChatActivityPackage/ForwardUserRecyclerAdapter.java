package com.anubhav.firebasechattingapp2.ChatActivityPackage;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anubhav.firebasechattingapp2.GlideApp;
import com.anubhav.firebasechattingapp2.R;
import com.anubhav.firebasechattingapp2.UserActivityPackage.User;
import com.anubhav.firebasechattingapp2.UserActivityPackage.userHolder;

import java.util.List;

public class ForwardUserRecyclerAdapter extends RecyclerView.Adapter<ForwardUserHolder> {

    List<User> userList;

    public ForwardUserRecyclerAdapter(List<User> UserList) {
        userList = UserList;
    }
    @NonNull
    @Override
    public ForwardUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View userView = LayoutInflater.from(parent.getContext()).inflate(R.layout.forward_user, parent, false);
        return new ForwardUserHolder(userView);
    }

    @Override
    public void onBindViewHolder(@NonNull ForwardUserHolder holder, int position) {
        holder.user.setText(userList.get(position).getUser());

        if(userList.get(position).getProfilePictureURL()!=null)
            GlideApp.with(holder.user.getContext())
                    .load(userList.get(position).getProfilePictureURL())
                    .dontAnimate()
                    .into(holder.user_image);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
