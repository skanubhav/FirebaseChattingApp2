package com.anubhav.firebasechattingapp2.UserActivityPackage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.anubhav.firebasechattingapp2.R;

public class ProfilePageActivity extends AppCompatActivity {

    private EditText userName;
    private EditText userEmail;
    private EditText userPassword;
    private Button editButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page_layout);

        userName = findViewById(R.id.profile_name);
        userEmail = findViewById(R.id.profile_email);
        userPassword = findViewById(R.id.profile_password);
        editButton = findViewById(R.id.edit_button);

        userName.setEnabled(false);
        userEmail.setEnabled(false);
        userPassword.setEnabled(false);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editButton.getText().equals("EDIT")){
                    userName.setEnabled(true);
                    userEmail.setEnabled(true);
                    userPassword.setEnabled(true);
                    editButton.setText("SAVE");
                }
                else if(editButton.getText().equals("SAVE")) {
                    userName.setEnabled(false);
                    userEmail.setEnabled(false);
                    userPassword.setEnabled(false);
                    editButton.setText("EDIT");
                }
            }
        });
    }
}
