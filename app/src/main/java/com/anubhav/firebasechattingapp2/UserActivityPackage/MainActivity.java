package com.anubhav.firebasechattingapp2.UserActivityPackage;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.Toast;

import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatActivity;
import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatMessage;
import com.anubhav.firebasechattingapp2.GlideApp;
import com.anubhav.firebasechattingapp2.MessagingContract;
import com.anubhav.firebasechattingapp2.R;
import com.anubhav.firebasechattingapp2.UserDBHelper;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static int SIGN_IN_REQUEST_CODE = 10;
    private UserRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView listOfUsers;
    private ActionBar actionBar;
    private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("Users");
    private User user = null;
    private DividerItemDecoration dividerItemDecoration;
    private List<User> UserList;
    private UserDBHelper userDBHelper = new UserDBHelper(this);
    public static int EDIT_PROFILE = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Intent notificationIntent = getIntent();

    if(notificationIntent.getStringExtra("SenderID")!=null){
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("SenderID",notificationIntent.getStringExtra("RecieverID") );
        intent.putExtra("SenderName",notificationIntent.getStringExtra("RecieverName"));
        intent.putExtra("SenderPhoto",notificationIntent.getStringExtra("RecieverDP"));
        intent.putExtra("RecieverID", notificationIntent.getStringExtra("SenderID"));
        intent.putExtra("RecieverName",notificationIntent.getStringExtra("SenderName") );
        intent.putExtra("RecieverPhoto",notificationIntent.getStringExtra("SenderDP"));
        startActivity(intent);
    }
    initialize();
    displayUsers();
    signIn();
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        userDBHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(user !=null)
            actionBar.setTitle("Welcome " + user.getUser());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.sign_out)
        {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    "You have been successfully signed out",
                                    Toast.LENGTH_LONG).show();
                            startActivityForResult(
                                    AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            .build(),
                                    SIGN_IN_REQUEST_CODE);
                            SQLiteDatabase database = userDBHelper.getWritableDatabase();
                            database.execSQL("delete from " + MessagingContract.UserDatabase.TABLE_NAME);
                            FirebaseMessaging.getInstance().unsubscribeFromTopic("user_" + user.getUid());
                            UserList.clear();
                        }
                    });

        }
        else if(item.getItemId()==R.id.profile_page) {
            Intent intent =new Intent(MainActivity.this, ProfilePageActivity.class);
            startActivityForResult(intent, EDIT_PROFILE);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==SIGN_IN_REQUEST_CODE) {
            if(resultCode==RESULT_OK)
            {
                initializeUser();
                FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).setValue(user);
                Toast.makeText(this,
                        "Successfully Signed in!",
                        Toast.LENGTH_LONG).show();
                initializeAdapter();
                initializeCloudData();
                initializeLocalData();
            }
            else {
                Toast.makeText(this,
                        "We couldn't sign you in. Try again later",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
        else if(requestCode==EDIT_PROFILE) {
            if(resultCode==RESULT_OK) {
                initializeUser();
            }
        }
    }

    public static interface ClickListener{
        public void onClick(View view, int position);
        public void onLongClick(View view,int position);
    }
    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ChatActivity.ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ChatActivity.ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        public RecyclerTouchListener(Context context, RecyclerView listOfUsers, ClickListener clickListener) {
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

    private void signIn() {
        FirebaseApp.initializeApp(this);
        if(FirebaseAuth.getInstance().getCurrentUser()==null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE);
        }
        else {
            initializeUser();
            Toast.makeText(this,
                    "Welcome " + user.getUser(),
                    Toast.LENGTH_LONG)
                    .show();

            initializeAdapter();
            initializeCloudData();
            initializeLocalData();
        }
    }

    private void initialize() {
        actionBar = getSupportActionBar();
        listOfUsers = findViewById(R.id.list_of_users);
        mLayoutManager = new LinearLayoutManager(this);
        UserList = new ArrayList<>();
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
        dividerItemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        initializeAdapter();
        listOfUsers.setLayoutManager(mLayoutManager);

        mAdapter.notifyDataSetChanged();
        listOfUsers.addItemDecoration(dividerItemDecoration);
        setItemTouchListener();
    }

    private void initializeAdapter() {
        mAdapter = new UserRecyclerAdapter(UserList, user, this);
        listOfUsers.setAdapter(mAdapter);
    }

    private void setItemTouchListener() {
        Log.d("OnClick", "Set Listener");

        listOfUsers.addOnItemTouchListener(new RecyclerTouchListener(this,
                listOfUsers, new ChatActivity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d("OnClick", UserList.get(position).getUser());
                startChat(UserList.get(position));
            }

            @Override
            public void onLongClick(View view, int position) {
                Log.d("OnLongClick", UserList.get(position).getUser());
            }
        }));
    }

    private void startChat(User Reciever) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("SenderID", user.getUid());
        intent.putExtra("SenderName", user.getUser());
        intent.putExtra("SenderPhoto", user.getProfilePictureURL());
        intent.putExtra("RecieverID", Reciever.getUid());
        intent.putExtra("RecieverName", Reciever.getUser());
        intent.putExtra("RecieverPhoto", Reciever.getProfilePictureURL());

        startActivity(intent);
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
            if(!flag) {
                UserList.add(newUser);
            }
        }
        Collections.sort(UserList, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getUser().compareTo(o2.getUser());
            }
        });
        mAdapter.notifyDataSetChanged();
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
                        Collections.sort(UserList, new Comparator<User>() {
                            @Override
                            public int compare(User o1, User o2) {
                                return o1.getUser().compareTo(o2.getUser());
                            }
                        });
                        mAdapter.notifyItemInserted(UserList.size());
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

    private void initializeUser() {
        Uri userPhoto = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        user = new User(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                "",
                "",
                userPhoto == null? null : userPhoto.toString());
        FirebaseMessaging.getInstance().subscribeToTopic("user_" + user.getUid());
        actionBar.setTitle(" Welcome " + user.getUser());
    }
}
