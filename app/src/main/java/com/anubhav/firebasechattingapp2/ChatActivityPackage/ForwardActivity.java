package com.anubhav.firebasechattingapp2.ChatActivityPackage;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.drm.DrmStore;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.anubhav.firebasechattingapp2.MessagingContract;
import com.anubhav.firebasechattingapp2.R;
import com.anubhav.firebasechattingapp2.UserActivityPackage.User;
import com.anubhav.firebasechattingapp2.UserDBHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ForwardActivity extends AppCompatActivity {


    private List<User> UserList;
    private UserDBHelper userDBHelper;
    private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("Users");
    private RecyclerView listOfUsers;
    private ForwardUserRecyclerAdapter mAdapter;
    private ActionBar actionBar;
    private RecyclerView.LayoutManager mLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private User user;

    public static interface ClickListener{
        public void onClick(View view, int position);
        public void onLongClick(View view,int position);
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forward_activity);
        initialize();
    }

    private void initialize() {
        actionBar = getSupportActionBar();
        listOfUsers = findViewById(R.id.list_of_users);
        mLayoutManager = new LinearLayoutManager(this);
        UserList = new ArrayList<>();
        userDBHelper = new UserDBHelper(this);
        dividerItemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);

        initializeUser();
        initializeLocalData();
        initializeCloudData();
    }

    private void initializeUser() {
        Uri userPhoto = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        user = new User(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                "",
                "",
                userPhoto == null? null : userPhoto.toString());
    }

    private void initializeLocalData() {
        SQLiteDatabase database = userDBHelper.getReadableDatabase();
        String sortOrder = MessagingContract.UserDatabase.COLUMN_NAME + " ASC";
        String[] projections = {
                MessagingContract.UserDatabase.COLUMN_ID,
                MessagingContract.UserDatabase.COLUMN_NAME,
                MessagingContract.UserDatabase.COLUMN_LAST_MESSAGE,
                MessagingContract.UserDatabase.COLUMN_LAST_MESSAGE_STAT,
                MessagingContract.UserDatabase.COLUMN_PROFILE_IMAGE
        };

        Cursor cursor = database.query(
                MessagingContract.UserDatabase.TABLE_NAME,
                projections,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while(cursor.moveToNext()) {
            boolean flag = false;
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.UserDatabase.COLUMN_NAME));
            String id = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.UserDatabase.COLUMN_ID));
            String lastMessage = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.UserDatabase.COLUMN_LAST_MESSAGE));
            String lastMessageStat =cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.UserDatabase.COLUMN_LAST_MESSAGE_STAT));
            String profileImage = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.UserDatabase.COLUMN_PROFILE_IMAGE));
            Log.d("SQLiteDatabase", name + " " + id);
            User newUser = new User (name, id, lastMessage, lastMessageStat, profileImage);
            for(int i = 0; i< UserList.size(); i++) {
                flag = UserList.get(i).getUid().equals(newUser.getUid());
            }
            if(!flag)
                UserList.add(newUser);
        }
        displayUsers();
    }

    private void initializeCloudData() {
        userDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                SQLiteDatabase database = userDBHelper.getWritableDatabase();
                String Uid = dataSnapshot.getValue(User.class).getUid();
                String Uname = dataSnapshot.getValue(User.class).getUser();
                String Uimage = dataSnapshot.getValue(User.class).getProfilePictureURL();
                if(!Uid.equals(user.getUid())) {
                    if(!CheckIsInDBorNot(Uid)) {
                        ContentValues values = new ContentValues();
                        values.put(MessagingContract.UserDatabase.COLUMN_ID, Uid);
                        values.put(MessagingContract.UserDatabase.COLUMN_NAME, Uname);
                        values.put(MessagingContract.UserDatabase.COLUMN_LAST_MESSAGE,"");
                        values.put(MessagingContract.UserDatabase.COLUMN_LAST_MESSAGE_STAT,"");
                        values.put(MessagingContract.UserDatabase.COLUMN_PROFILE_IMAGE, Uimage);
                        database.insert(MessagingContract.UserDatabase.TABLE_NAME, null, values);
                        UserList.add(new User(Uname, Uid, "", "", Uimage));
                        displayUsers();
                    }
                    Log.d("UserValueEvent",Uid + " " + Uname);
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

    private boolean CheckIsInDBorNot(String Uid) {
        String selectQuery = "SELECT  * FROM " + MessagingContract.UserDatabase.TABLE_NAME + " WHERE "
                + MessagingContract.UserDatabase.COLUMN_ID +"='"+ Uid + "'";
        SQLiteDatabase db = userDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void displayUsers() {
        mAdapter = new ForwardUserRecyclerAdapter(UserList);
        listOfUsers.setLayoutManager(mLayoutManager);
        listOfUsers.setAdapter(mAdapter);

        listOfUsers.getAdapter().notifyDataSetChanged();
        listOfUsers.addItemDecoration(dividerItemDecoration);

        listOfUsers.addOnItemTouchListener(new RecyclerTouchListener(this,
                listOfUsers,
                new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Intent intent = new Intent();
                        intent.putExtra("Reciever Name", UserList.get(position).getUser());
                        Log.d("Forward User", UserList.get(position).getUser());
                        intent.putExtra("Reciever ID", UserList.get(position).getUid());
                        intent.putExtra("Reciever Photo", UserList.get(position).getProfilePictureURL());
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));
    }
}
