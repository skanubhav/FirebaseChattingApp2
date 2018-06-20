package com.anubhav.firebasechattingapp2.UserActivityPackage;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatActivity;
import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatMessage;
import com.anubhav.firebasechattingapp2.GlideApp;
import com.anubhav.firebasechattingapp2.MessagingContract;
import com.anubhav.firebasechattingapp2.R;
import com.anubhav.firebasechattingapp2.UserDBHelper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserRecyclerAdapter extends RecyclerView.Adapter<userHolder>  {

    private List<User> UserList;
    private User Sender;
    private Context context;

    public UserRecyclerAdapter(List<User> UserList, User Sender, Context context) {
        this.UserList = UserList;
        this.Sender = Sender;
        this.context = context;
    }

    @NonNull
    @Override
    public userHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View userView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_view, parent, false);
        return new userHolder(userView);
    }

    @Override
    public int getItemCount() {
        return UserList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final userHolder holder,final int position) {
        if(Sender!=null) {
            setText(holder, position);

            setImage(holder, position);

            setListener(holder, UserList.get(position));

            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    setFirebaseDatabaseListener(position, UserList.get(position), holder.user.getContext());
                    return null;
                }
            };
            task.execute();
        }
    }

    private void setText(userHolder holder, int position) {
        holder.user.setText(UserList.get(position).getUser());
        holder.user_last_message.setText(UserList.get(position).getLastMessage());
    }

    private void setImage(userHolder holder, int position) {

        if(UserList.get(position).getLastMessageStat().equals("OUT_MESSAGE")){
            holder.message_stat.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_outgoing_message));
        }
        else if(UserList.get(position).getLastMessageStat().equals("IN_MESSAGE")) {
            holder.message_stat.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_incoming_message));
        }

        if(UserList.get(position).getProfilePictureURL()!=null)
            GlideApp.with(context)
                    .load(UserList.get(position).getProfilePictureURL())
                    .dontAnimate()
                    .into(holder.user_image);
    }

    private void setFirebaseDatabaseListener(final int position, final User Reciever, final Context context) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Chats")
                .child(Sender.getUid())
                .child(Reciever.getUid());

        Query lastQuery = databaseReference.orderByKey().limitToLast(1);

        lastQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String lastMessage = null;
                if(dataSnapshot.getValue(ChatMessage.class).getContentType() == ChatMessage.TEXT)
                    lastMessage = dataSnapshot.getValue(ChatMessage.class).getMessageText();
                else if(dataSnapshot.getValue(ChatMessage.class).getContentType() == ChatMessage.IMAGE)
                    lastMessage = "Image";
                else if(dataSnapshot.getValue(ChatMessage.class).getContentType() == ChatMessage.VIDEO)
                    lastMessage = "Video";
                else if(dataSnapshot.getValue(ChatMessage.class).getContentType() == ChatMessage.AUDIO)
                    lastMessage = "Audio";
                else if(dataSnapshot.getValue(ChatMessage.class).getContentType() == ChatMessage.DOCUMENT)
                    lastMessage = "Document";

                String lastMessageStat = String.valueOf(dataSnapshot.getValue(ChatMessage.class).getStatusOfMessage());
                if(!lastMessage.equals(Reciever.getLastMessage())) {
                    UserList.get(position).setLastMessage(lastMessage);
                    UserList.get(position).setLastMessageStat(lastMessageStat);
                    ContentValues values = new ContentValues();
                    values.put(MessagingContract.UserDatabase.COLUMN_LAST_MESSAGE, lastMessage);
                    values.put(MessagingContract.UserDatabase.COLUMN_LAST_MESSAGE_STAT, lastMessageStat);

                    UserDBHelper userDBHelper = new UserDBHelper(context);
                    SQLiteDatabase sqLiteDatabase = userDBHelper.getWritableDatabase();
                    sqLiteDatabase.update(
                          MessagingContract.UserDatabase.TABLE_NAME,
                          values,
                            MessagingContract.UserDatabase.COLUMN_ID + " =? ",
                            new String[] {Reciever.getUid()}
                    );
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setListener(final userHolder holder, final User Reciever) {
        holder.user_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    Intent intent = new Intent(holder.user.getContext(), ChatActivity.class);

                    intent.putExtra("SenderID", Sender.getUid());
                    intent.putExtra("SenderName", Sender.getUser());
                    intent.putExtra("SenderPhoto", Sender.getProfilePictureURL());
                    intent.putExtra("RecieverID", Reciever.getUid());
                    intent.putExtra("RecieverName", Reciever.getUser());
                    intent.putExtra("RecieverPhoto", Reciever.getProfilePictureURL());

                    holder.user.getContext().startActivity(intent);
                }
            }
        });

    }
}
