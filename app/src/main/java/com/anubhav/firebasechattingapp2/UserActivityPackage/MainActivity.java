package com.anubhav.firebasechattingapp2.UserActivityPackage;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatActivity;
import com.anubhav.firebasechattingapp2.MessagingContract;
import com.anubhav.firebasechattingapp2.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static int SIGN_IN_REQUEST_CODE = 10;
    private UserRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView listOfUsers;
    private ActionBar actionBar;
    private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("Users");
    public static User user;
    private List<User> UserList;
    private UserDBHelper userDBHelper = new UserDBHelper(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        signIn();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
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
                        }
                    });
        }
        SQLiteDatabase database = userDBHelper.getWritableDatabase();
        database.execSQL("delete from " + MessagingContract.UserDatabase.TABLE_NAME);
        UserList.clear();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==SIGN_IN_REQUEST_CODE) {
            if(resultCode==RESULT_OK)
            {
                initializeUser();
                Toast.makeText(this,
                        "Successfully Signed in!",
                        Toast.LENGTH_LONG).show();
                FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).setValue(user);
                actionBar.setTitle("Welcome " + user.getUser());
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
                    "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();
            actionBar.setTitle("Welcome " + user.getUser());
            initializeCloudData();
            initializeLocalData();
        }
    }

    private void initialize() {
        actionBar = getSupportActionBar();
        listOfUsers = findViewById(R.id.list_of_users);
        mLayoutManager = new LinearLayoutManager(this);
        UserList = new ArrayList<>();

        LayoutAnimationController layoutAnimationController =
                AnimationUtils.loadLayoutAnimation(listOfUsers.getContext(),R.anim.layout_fall_from_top);
        listOfUsers.setLayoutAnimation(layoutAnimationController);
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
        mAdapter = new UserRecyclerAdapter(UserList);
        listOfUsers.setLayoutManager(mLayoutManager);
        listOfUsers.setAdapter(mAdapter);

        listOfUsers.getAdapter().notifyDataSetChanged();
        listOfUsers.scheduleLayoutAnimation();
    }

    private void initializeLocalData() {
        SQLiteDatabase database = userDBHelper.getReadableDatabase();
        String sortOrder = MessagingContract.UserDatabase.COLUMN_NAME + " ASC";
        String[] projections = {
                MessagingContract.UserDatabase.COLUMN_ID,
                MessagingContract.UserDatabase.COLUMN_NAME
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
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.UserDatabase.COLUMN_NAME));
            String id = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.UserDatabase.COLUMN_ID));
            Log.d("SQLiteDatabase", name + " " + id);
            User newUser = new User (name, id);
            if(!UserList.contains(newUser))
                UserList.add(newUser);
        }
        displayUsers();
    }

    private void initializeCloudData() {
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SQLiteDatabase database = userDBHelper.getWritableDatabase();
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String Uid = postSnapshot.getValue(User.class).getUid();
                    String Uname =  postSnapshot.getValue(User.class).getUser();
                    if(!Uid.equals(user.getUid())) {
                        if(!CheckIsInDBorNot(Uid)) {
                            ContentValues values = new ContentValues();
                            values.put(MessagingContract.UserDatabase.COLUMN_ID, Uid);
                            values.put(MessagingContract.UserDatabase.COLUMN_NAME, Uname);
                            database.insert(MessagingContract.UserDatabase.TABLE_NAME, null, values);
                            UserList.add(new User(Uname, Uid));
                            displayUsers();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void initializeUser() {
        user = new User(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),FirebaseAuth.getInstance().getCurrentUser().getUid());
    }
}
