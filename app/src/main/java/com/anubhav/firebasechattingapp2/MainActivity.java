package com.anubhav.firebasechattingapp2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity {

    public static int SIGN_IN_REQUEST_CODE = 10;
    FirebaseRecyclerAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView listOfUsers;
    ActionBar actionBar;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        signIn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.stopListening();
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
                displayUsers();
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
            displayUsers();
        }
    }

    private void initialize() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().getReference("Chats").keepSynced(true);
        actionBar = getSupportActionBar();
        listOfUsers = findViewById(R.id.list_of_users);
        mLayoutManager = new LinearLayoutManager(this);
        initializeAdapter();

        LayoutAnimationController layoutAnimationController =
                AnimationUtils.loadLayoutAnimation(listOfUsers.getContext(),R.anim.layout_fall_from_top);
        listOfUsers.setLayoutAnimation(layoutAnimationController);
    }

    private void initializeAdapter() {
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByValue();
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query , User.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<User, userHolder>(options){
            @Override
            protected void onBindViewHolder(@NonNull userHolder holder, int position, @NonNull final User model) {
                holder.user.setText(model.getUser());
                if (model.getUid().equals(user.getUid())) {
                    holder.user.setBackgroundColor(getResources().getColor(R.color.fui_linkColor));
                }
                else {
                    setListener(holder, model);
                }
            }

            @NonNull
            @Override
            public userHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View userView = getLayoutInflater().from(parent.getContext()).inflate(R.layout.user_view, parent, false);
                return new userHolder(userView);
            }
        };

    }

    private void setListener(userHolder holder, final User model) {
        holder.user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);

                    intent.putExtra("SenderID", user.getUid());
                    intent.putExtra("SenderName", user.getUser());
                    intent.putExtra("RecieverID", model.getUid());
                    intent.putExtra("RecieverName", model.getUser());

                    startActivity(intent);
                }
            }
        });

    }

    public void displayUsers() {
        listOfUsers.setLayoutManager(mLayoutManager);
        listOfUsers.setAdapter(mAdapter);
        mAdapter.startListening();

        listOfUsers.getAdapter().notifyDataSetChanged();
        listOfUsers.scheduleLayoutAnimation();
    }

    private void initializeUser() {
        user = new User(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),FirebaseAuth.getInstance().getCurrentUser().getUid());
    }
}
