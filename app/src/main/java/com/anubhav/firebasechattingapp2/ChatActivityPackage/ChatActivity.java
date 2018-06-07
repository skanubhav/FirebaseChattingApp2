package com.anubhav.firebasechattingapp2.ChatActivityPackage;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.anubhav.firebasechattingapp2.MessagingContract;
import com.anubhav.firebasechattingapp2.R;
import com.anubhav.firebasechattingapp2.UserActivityPackage.User;
import com.anubhav.firebasechattingapp2.UserDBHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.data.DataBuffer;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {


    //SQL Create Query
    private String SQL_CREATE_CHAT_ENTRIES;

    // Views
    private RecyclerView listOfMessages;
    private EditText message_input;
    private FloatingActionButton fab;
    private CardView attachCard;
    private FrameLayout attachFrameLayout;

    // RecyclerView Requirements
    private LinearLayoutManager mLayoutManager;
    private ChatMessageAdapter mAdapter;

    // Firebase References
    private DatabaseReference databaseReference;
    private StorageReference UploadRef;

    private String CHAT_TABLE_NAME;
    private UserDBHelper userDBHelper = new UserDBHelper(this);
    private List<ChatMessage> ChatList;

    // Sender and Receiver Data
    private User Sender;
    private User Reciever;

    // Storage reference for firebase
    private StorageReference storageReference;

    // Animation Controller for recycler view
    private LayoutAnimationController layoutAnimationController;

    // Request Codes
    public static int RC_TAKE_PICTURE = 10;
    public static int RC_TAKE_CAMERA = 20;
    public static int RC_TAKE_VIDEO = 30;
    public static int RC_TAKE_AUDIO = 40;
    public static int RC_TAKE_DOCUMENT = 50;

    public static int NOTIFICATION_ID = 100;

    Uri imageFilePath;
    String thumbnailURL = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addmedia,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.attach_file) {
            if(attachFrameLayout.getVisibility()==View.GONE){
                attachFrameLayout.setVisibility(View.VISIBLE);
            }

            else if (attachFrameLayout.getVisibility()==View.VISIBLE){
                attachFrameLayout.setVisibility(View.GONE);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==RC_TAKE_PICTURE) {
            if(resultCode==RESULT_OK) {
                    try {
                        uploadFile(data.getData(),"Images", ChatMessage.IMAGE);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        else if(requestCode==RC_TAKE_CAMERA) {
            if(resultCode==RESULT_OK) {
                try {
                    uploadFile(imageFilePath,"Images", ChatMessage.IMAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode==RC_TAKE_VIDEO) {
            if(resultCode==RESULT_OK) {
                try {
                    uploadFile(data.getData(),"Videos", ChatMessage.VIDEO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode==RC_TAKE_AUDIO) {
            if(resultCode==RESULT_OK) {
                try {
                    uploadFile(data.getData(),"Audio", ChatMessage.AUDIO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode==RC_TAKE_DOCUMENT) {
            if(resultCode==RESULT_OK) {
                try {
                    uploadFile(data.getData(),"Document", ChatMessage.DOCUMENT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
       attachFrameLayout.setVisibility(View.GONE);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if(UploadRef!=null)
            outState.putString("Refernce",UploadRef.toString());
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String reference = savedInstanceState.getString("Reference");
        if (reference == null) {
            return;
        }

        UploadRef = FirebaseStorage.getInstance().getReferenceFromUrl(reference);
        List<UploadTask> tasks = UploadRef.getActiveUploadTasks();

        if (tasks.size() > 0) {
            UploadTask task = tasks.get(0);
        }
    }

    // Check type of file and upload from uri
    private void uploadFile(Uri data, String typeOfData, final int contentType) throws IOException {

        if(typeOfData=="Images") {
            compressAndSendImage(data);
        }
        else {
            if(new File(data.getPath()).length()<=26214400) {
                String fileName = new Date().getTime() + getFileName(data);
                UploadRef = storageReference.child(typeOfData + "/" + fileName);
                UploadTask uploadTask = UploadRef.putFile(data);

                showNotification(uploadTask, contentType, data);
            }
            else {
                showToast("Files larger thar 25 MB cannot be uploaded");
            }
        }
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

    // compress image before sending as byte stream
    private void compressAndSendImage (Uri data) throws IOException {
        byte[] bdata = compressImage(data);
        String fileName = new Date().getTime() + getFileName(data) ;
        UploadRef = storageReference.child("Images/" + fileName);
        UploadTask uploadTask = UploadRef.putBytes(bdata);

        showNotification(uploadTask, ChatMessage.IMAGE, null);
    }

    // compress image using bitmap
    private byte[] compressImage (Uri imageUri) throws IOException {

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if(bitmap.getAllocationByteCount()>512000) {
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,byteArrayOutputStream);
        }
        else if(bitmap.getAllocationByteCount()>256000) {
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        }
        else {
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        }
        byte[] bdata = byteArrayOutputStream.toByteArray();
        return bdata;
    }

    private void showNotification(UploadTask uploadTask,final int contentType, final Uri data) {
        final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Uploading File")
                .setContentText("Uploading...")
                .setSmallIcon(R.drawable.ic_attach_file)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100,0,false);
        notificationManagerCompat.notify(NOTIFICATION_ID, mBuilder.build());

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0f*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();

                mBuilder.setProgress(100,(int)progress,false);
                notificationManagerCompat.notify(NOTIFICATION_ID, mBuilder.build());
            }
        })
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                        mBuilder.setContentText("Download complete")
                                .setProgress(0,0,false);
                        notificationManagerCompat.notify(NOTIFICATION_ID, mBuilder.build());
                        String status;
                        if(task.isSuccessful()) {
                            thumbnailURL = null;
                            Log.d("Upload Thumbnail","Check before Upload");
                            if(contentType == ChatMessage.VIDEO){
                                Log.d("Upload Thumbnail","Start Upload");
                                uploadThumbnail(data, task.getResult().toString());
                            }
                            else
                                sendData(task.getResult().toString(), contentType, null);

                            status = "Upload Succesful";
                        }
                        else {
                            status = "Upload Failed";
                        }
                        showToast(status);
                    }
                });
    }

    private void uploadThumbnail(Uri data, final String videoURL)  {
        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
        mMMR.setDataSource(this,data);
            Bitmap bitmap = mMMR.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            Log.d("Upload Thumbnail",bitmap.toString());
            Log.d("Upload Thumbnail","BeforeUpload");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            byte[] bdata = byteArrayOutputStream.toByteArray();
            final StorageReference thumbnailRef =storageReference.child("Videos").child("Thumbnail").child("thumb"+new Date().getTime());
            UploadTask uploadTask = thumbnailRef.putBytes(bdata);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()) {
                        Log.d("Upload Thumbnail","Uplaoad Failed");
                        throw task.getException();
                    }
                    else {
                        Log.d("Upload Thumbnail","Upload Success");
                        return thumbnailRef.getDownloadUrl();
                    }
                }
            })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()) {
                                Log.d("Upload Thumbnail","Upload Complete");
                                thumbnailURL = task.getResult().toString();
                                sendData(videoURL, ChatMessage.VIDEO, thumbnailURL);
                            }
                        }
                    });
    }

    private void showToast(String text) {
        Toast.makeText(this, text,Toast.LENGTH_LONG).show();
    }

    private void initialize() {
        storageReference = FirebaseStorage.getInstance().getReference();
        ChatList = new ArrayList<>();
        initializeViews();
        initializeUsers();

        setfabListener();
        setAttachListeners();
        initializeAdapter();
        initializeLocalData();
        initializeCloudData();
        displayChatMessages();
        getSupportActionBar().setTitle(Reciever.getUser());
    }

    private void initializeLocalData() {
        SQLiteDatabase database = userDBHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"
                + CHAT_TABLE_NAME + "'", null);

        if (cursor.getCount() <= 0) {
            database.execSQL(SQL_CREATE_CHAT_ENTRIES);
        }

        String sortOrder = MessagingContract.ChatDatabase.MESSAGE_TIME + " ASC";
        String[] projections = {
                MessagingContract.ChatDatabase.MESSAGE_TEXT,
                MessagingContract.ChatDatabase.MESSAGE_SENDER,
                MessagingContract.ChatDatabase.MESSAGE_RECIEVER,
                MessagingContract.ChatDatabase.MESSAGE_TIME,
                MessagingContract.ChatDatabase.MESSAGE_STATUS,
                MessagingContract.ChatDatabase.MESSAGE_CONTENT_TYPE,
                MessagingContract.ChatDatabase.MESSAGE_THUMBNAIL
        };

        cursor = database.query(
                CHAT_TABLE_NAME,
                projections,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while (cursor.moveToNext()) {
            boolean flag = true;
            String message = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_TEXT));
            String Sender = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_SENDER));
            String Reciever = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_RECIEVER));
            long Time = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_TIME)));
            StatusOfMessage Status = StatusOfMessage.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_STATUS)));
            ;
            int ContentType = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_CONTENT_TYPE)));
            String Thumbnail = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_THUMBNAIL));

            Log.d("SQlite Select", Sender + Reciever + message);

            ChatMessage newMessage = new ChatMessage(message, Sender, Reciever, Time, Status, ContentType, Thumbnail);
            for (int i = 0; i < ChatList.size(); i++) {
                ChatMessage chatMessage = ChatList.get(i);
                if(chatMessage.getMessageTime()==newMessage.getMessageTime()){
                    flag = false;
                }
            }
            if(flag)
                ChatList.add(newMessage);
        }
        for(int i = 0; i < ChatList.size();i++)
            Log.d("ChatListMessages", ChatList.get(i).getMessageText());

        mAdapter.notifyDataSetChanged();

    }

    private void initializeCloudData() {
        final SQLiteDatabase database = userDBHelper.getWritableDatabase();

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String message = dataSnapshot.getValue(ChatMessage.class).getMessageText();
                String Sender = dataSnapshot.getValue(ChatMessage.class).getMessageSender();
                String Reciever = dataSnapshot.getValue(ChatMessage.class).getMessageReciever();
                long Time = dataSnapshot.getValue(ChatMessage.class).getMessageTime();
                StatusOfMessage Status = dataSnapshot.getValue(ChatMessage.class).getStatusOfMessage();
                int ContentType = dataSnapshot.getValue(ChatMessage.class).getContentType();
                String Thumbnail = dataSnapshot.getValue(ChatMessage.class).getThumbnailURL();

                if (!CheckIsInDBorNot(Time)) {
                    ContentValues values = new ContentValues();
                    values.put(MessagingContract.ChatDatabase.MESSAGE_TEXT, message);
                    values.put(MessagingContract.ChatDatabase.MESSAGE_SENDER, Sender);
                    values.put(MessagingContract.ChatDatabase.MESSAGE_RECIEVER, Reciever);
                    values.put(MessagingContract.ChatDatabase.MESSAGE_TIME, Time);
                    values.put(MessagingContract.ChatDatabase.MESSAGE_STATUS, Status.toString());
                    values.put(MessagingContract.ChatDatabase.MESSAGE_CONTENT_TYPE, ContentType);
                    values.put(MessagingContract.ChatDatabase.MESSAGE_THUMBNAIL, Thumbnail);
                    database.insert(CHAT_TABLE_NAME, null, values);

                    ChatList.add(new ChatMessage(message, Sender, Reciever, Time, Status, ContentType, Thumbnail));
                    mAdapter.notifyDataSetChanged();

                    Log.d("ValueEventList", Sender + "->" + Reciever + " = " + message);
                }
                Log.d("ValueEventList2", Sender + "->" + Reciever + " = " + message);
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

    private boolean CheckIsInDBorNot(long Time) {
        String selectQuery = "SELECT  * FROM " + CHAT_TABLE_NAME + " WHERE "
                + MessagingContract.ChatDatabase.MESSAGE_TIME +"='"+ Time + "'";
        SQLiteDatabase db = userDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    // Set click listeners for attach icons
    private void setAttachListeners() {

        attachCard.findViewById(R.id.attach_image)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchGalleryImage();
                    }
                });
        attachCard.findViewById(R.id.attach_camera)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchCamera();
                    }
                });
        attachCard.findViewById(R.id.attach_video)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchGalleryVideo();
                    }
                });
        attachCard.findViewById(R.id.attach_audio)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchGalleryAudio();
                    }
                });
        attachCard.findViewById(R.id.attach_document)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchAttachDocument();
                    }
                });

        attachFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(attachFrameLayout.getVisibility()==View.VISIBLE)
                    attachFrameLayout.setVisibility(View.GONE);
            }
        });
    }

    // Select image
    public void launchGalleryImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,RC_TAKE_PICTURE);
    }

    // Capture Image
    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent.resolveActivity(getPackageManager())!=null){
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }
            catch (Exception e){
            }

            if(photoFile!=null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageFilePath);
                startActivityForResult(intent,RC_TAKE_CAMERA);
            }
        }
    }

    // Select Video
    private void launchGalleryVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,RC_TAKE_VIDEO);
    }

    // Select Audio
    private void launchGalleryAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,RC_TAKE_AUDIO);
    }

    // Select Document
    private void launchAttachDocument() {
        String[] mimeTypes =
                {"application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                        "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                        "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                        "text/plain",
                        "application/pdf",};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent,"ChooseFile"),RC_TAKE_DOCUMENT);
    }

    private File createImageFile() throws IOException {
        String timeStamp = (String) DateFormat.format("yyyydd_HHmmss", new Date().getTime());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        imageFilePath = Uri.fromFile(image);
        return image;
    }

    private void initializeViews() {
        listOfMessages = findViewById(R.id.list_of_messages);
        mLayoutManager = new LinearLayoutManager(this);
        fab = findViewById(R.id.fab);
        message_input = findViewById(R.id.message_input);
        attachCard = findViewById(R.id.attach_card);
        attachFrameLayout = findViewById(R.id.attach_activity_layout);

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(listOfMessages.getContext(),R.anim.layout_slide_from_bottom);
        mLayoutManager.setStackFromEnd(true);
        listOfMessages.setLayoutAnimation(layoutAnimationController);
        listOfMessages.setLayoutManager(mLayoutManager);

        //message_input.setInputType(InputType.TYPE_NULL);

    }

    // Set RecyclerView Adapter using firebaseUI
    private void initializeAdapter() {
        mAdapter = new ChatMessageAdapter(ChatList);
        mAdapter.notifyDataSetChanged();
    }

    // Set Receiver and Sender Data
    private void initializeUsers() {
        Intent intent = getIntent();
        Sender = new User(intent.getStringExtra("SenderName"),intent.getStringExtra("SenderID"));
        Reciever = new User(intent.getStringExtra("RecieverName"),intent.getStringExtra("RecieverID"));

        CHAT_TABLE_NAME = Sender.getUid() + Reciever.getUid();
        SQL_CREATE_CHAT_ENTRIES =  "CREATE TABLE " + CHAT_TABLE_NAME + " ("
                + MessagingContract.ChatDatabase._ID + " INTEGER PRIMARY KEY, "
                + MessagingContract.ChatDatabase.MESSAGE_TEXT + " TEXT, "
                + MessagingContract.ChatDatabase.MESSAGE_SENDER + " TEXT, "
                + MessagingContract.ChatDatabase.MESSAGE_RECIEVER + " TEXT, "
                + MessagingContract.ChatDatabase.MESSAGE_TIME + " INTEGER, "
                + MessagingContract.ChatDatabase.MESSAGE_STATUS + " TEXT, "
                + MessagingContract.ChatDatabase.MESSAGE_CONTENT_TYPE + " INTEGER, "
                + MessagingContract.ChatDatabase.MESSAGE_THUMBNAIL + " TEXT )";

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Chats")
                .child(Sender.getUid())
                .child(Reciever.getUid());
    }

    // Set click listener to send message
    private void setfabListener() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message_input.getText().toString().equals("")) {
                   showToast("Input cannot be empty");
                }
                else {
                    sendData(message_input.getText().toString(),ChatMessage.TEXT, null);
                }
            }
        });
    }

    // Send Text data
    private void sendData(String data, int contentType, String thumbnailURL) {
        long Time = new Date().getTime();
        FirebaseDatabase.getInstance().getReference("Chats")
                .child(Sender.getUid())
                .child(Reciever.getUid())
                .child(String.valueOf(new Date().getTime()))
                .setValue(new ChatMessage(data, Sender.getUser(), Reciever.getUser(), Time, StatusOfMessage.OUT_MESSAGE, contentType, thumbnailURL));
        FirebaseDatabase.getInstance().getReference("Chats")
                .child(Reciever.getUid())
                .child(Sender.getUid())
                .child(String.valueOf(new Date().getTime()))
                .setValue(new ChatMessage(data, Sender.getUser(), Reciever.getUser(), Time, StatusOfMessage.IN_MESSAGE, contentType, thumbnailURL));
        message_input.setText("");
        mLayoutManager.smoothScrollToPosition(listOfMessages, null, ChatList.size());
    }

    // Set adapter to recyclerView and listen for changes
    private void displayChatMessages() {
        listOfMessages.setAdapter(mAdapter);
        mLayoutManager.smoothScrollToPosition(listOfMessages, null, ChatList.size());

        listOfMessages.getAdapter().notifyDataSetChanged();
        listOfMessages.scheduleLayoutAnimation();
    }
}