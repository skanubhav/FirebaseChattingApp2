package com.anubhav.firebasechattingapp2.UserActivityPackage;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Credentials;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anubhav.firebasechattingapp2.GlideApp;
import com.anubhav.firebasechattingapp2.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfilePageActivity extends AppCompatActivity {

    private EditText userNameView;
    private EditText userEmailView;
    private EditText userPasswordView;
    private Button editButton;
    private User user;
    private ImageView userImage;
    private FloatingActionButton uploadProfileImage;
    private ProgressBar uploadProgress;
    private StorageReference UploadRef;
    private String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public static int SHOW_MODE = 0;
    public static int EDIT_MODE = 1;
    public static int RC_TAKE_PICTURE = 10;
    private int  MODE = SHOW_MODE;;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page_layout);

        initialize();
        setData();
        setListener();
        setFabListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==RC_TAKE_PICTURE){
            if(resultCode==RESULT_OK) {
                try {
                    uploadProgress.setVisibility(View.VISIBLE);
                    uploadImage(data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if(MODE == EDIT_MODE) {
            setDisabled();
            userNameView.setText(user.getUser());
            userEmailView.setText(userEmail);
            userPasswordView.setText(R.string.password);
        }
        else
            super.onBackPressed();
    }

    private void initialize() {
        uploadProgress = findViewById(R.id.profile_image_loading);
        userNameView = findViewById(R.id.profile_name);
        userEmailView = findViewById(R.id.profile_email);
        userPasswordView = findViewById(R.id.profile_password);
        editButton = findViewById(R.id.edit_button);
        userImage = findViewById(R.id.profile_image);
        uploadProfileImage = findViewById(R.id.upload_profile_image);

        user = new User(
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                "",
                "",
                FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()==null? null: FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString()
        );

        userNameView.setEnabled(false);
        userEmailView.setEnabled(false);
        userPasswordView.setEnabled(false);
    }

    private void setData() {

        if(user.getProfilePictureURL()!=null) {
            Uri profileImageURL = Uri.parse(user.getProfilePictureURL());
            GlideApp.with(this)
                    .load(profileImageURL)
                    .dontAnimate()
                    .into(userImage);
        }

        userNameView.setText(user.getUser());
        userEmailView.setText(userEmail);
    }

    private void setListener() {
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MODE == SHOW_MODE){
                    userNameView.setEnabled(true);
                    userEmailView.setEnabled(true);
                    userPasswordView.setEnabled(true);
                    editButton.setText("SAVE");
                    userEmail = userEmailView.getText().toString();
                    MODE = EDIT_MODE;
                }

                else if(MODE == EDIT_MODE) {
                    if(!user.getUser().equals(userNameView.getText().toString())){
                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userNameView.getText().toString())
                                .build();
                        firebaseUser.updateProfile(profileChangeRequest)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                showToast("Display Name Changed");
                                setDisabled();
                            }
                        });
                        user.setUser(userNameView.getText().toString());
                        FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).setValue(user);

                    }
                    if(!userEmail.equals(userEmailView.getText().toString()) && !userPasswordView.getText().toString().equals("password")) {
                        reuthenticateAndChange("EMAILANDPASSWORD");
                    }
                    else {
                        if(!userEmail.equals(userEmailView.getText().toString())) {
                            Log.d("ProfilePage", userEmailView.getText().toString());
                            if(isEmailValid(userEmailView.getText().toString()))
                                reuthenticateAndChange("EMAIL");
                            else
                                showToast("E-Mail Invalid");
                        }

                        if(!userPasswordView.getText().toString().equals("password")) {
                            reuthenticateAndChange("PASSWORD");
                        }
                    }
                }
            }
        });
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void setDisabled() {
        userNameView.setEnabled(false);
        userEmailView.setEnabled(false);
        userPasswordView.setEnabled(false);
        editButton.setText("EDIT");
        MODE = SHOW_MODE;
    }

    private void reuthenticateAndChange(final String changeParameter) {

        final View reauthenticateView = getLayoutInflater().inflate(R.layout.reauthenticate_layout, null);
        AlertDialog alertDialog = new AlertDialog.Builder(userEmailView.getContext())
                .setView(reauthenticateView)
                .setPositiveButton("Auhtenticate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editEmail = reauthenticateView.findViewById(R.id.edit_email);
                        EditText editPassword = reauthenticateView.findViewById(R.id.edit_password);
                        AuthCredential credentials = EmailAuthProvider.getCredential(
                                editEmail.getText().toString(),
                                editPassword.getText().toString()
                        );
                        firebaseUser.reauthenticateAndRetrieveData(credentials);
                        if(changeParameter == "EMAIL") {
                            firebaseUser.updateEmail(userEmailView.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                showToast("Email Changed");
                                                setDisabled();
                                            }


                                        }
                                    });
                        }
                        else if(changeParameter=="PASSWORD") {
                            firebaseUser.updatePassword(userPasswordView.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                showToast("Password Changed");
                                                setDisabled();
                                            }

                                        }
                                    });
                        }
                        else if(changeParameter=="EMAILANDPASSWORD") {
                            firebaseUser.updateEmail(userEmailView.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                                showToast("Email Changed");
                                        }
                                    });
                            firebaseUser.updatePassword(userPasswordView.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                showToast("Password Changed");
                                                setDisabled();
                                            }
                                        }
                                    });
                        }
                    }
                })
                .create();

        alertDialog.show();
    }


    private void uploadImage(Uri data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),data);
        String fileName = new Date().getTime() + getFileName(data) ;
        UploadRef = FirebaseStorage.getInstance().getReference().child("ProfileImages/" + user.getUid()+ "/" + fileName);

        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] bdata = byteArrayOutputStream.toByteArray();

        UploadTask uploadTask = UploadRef.putBytes(bdata);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()) {
                    throw task.getException();
                }
                else {
                    return UploadRef.getDownloadUrl();
                }
            }
        })
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()) {
                            user.setProfilePictureURL(task.getResult().toString());
                            FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).setValue(user);
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(task.getResult())
                                    .build();
                            firebaseUser.updateProfile(profileChangeRequest)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            GlideApp.with(userImage.getContext())
                                                    .load(firebaseUser.getPhotoUrl())
                                                    .dontAnimate()
                                                    .into(userImage);
                                            uploadProgress.setVisibility(View.GONE);
                                            showToast("Profile Picture Changed");
                                        }
                                    });
                        }
                    }
                });
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void setFabListener() {
        uploadProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchGallery();
            }
        });
    }

    public void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,RC_TAKE_PICTURE);
    }


    private void showToast(String text) {
        Toast.makeText(this, text,Toast.LENGTH_LONG).show();
    }

}
