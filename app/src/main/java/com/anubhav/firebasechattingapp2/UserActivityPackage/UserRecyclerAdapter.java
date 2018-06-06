package com.anubhav.firebasechattingapp2.UserActivityPackage;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatActivity;
import com.anubhav.firebasechattingapp2.R;

import java.util.List;

public class UserRecyclerAdapter extends RecyclerView.Adapter<userHolder>  {

    private List<User> UserList;

    public UserRecyclerAdapter(List<User> UserList) {
        this.UserList = UserList;
    }

    @NonNull
    @Override
    public userHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View userView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_view, parent, false);
        return new userHolder(userView);
    }

    @Override
    public void onBindViewHolder(@NonNull userHolder holder, int position) {
        holder.user.setText(UserList.get(position).getUser());
        setListener(holder, UserList.get(position));
    }

    @Override
    public int getItemCount() {
        return UserList.size();
    }

    private void setListener(final userHolder holder, final User Reciever) {
        holder.user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    Intent intent = new Intent(holder.user.getContext(), ChatActivity.class);

                    intent.putExtra("SenderID", MainActivity.user.getUid());
                    intent.putExtra("SenderName", MainActivity.user.getUser());
                    intent.putExtra("RecieverID", Reciever.getUid());
                    intent.putExtra("RecieverName", Reciever.getUser());

                    holder.user.getContext().startActivity(intent);
                }
            }
        });

    }
}
