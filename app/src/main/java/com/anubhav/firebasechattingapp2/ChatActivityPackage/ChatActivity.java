package com.anubhav.firebasechattingapp2.ChatActivityPackage;

import android.animation.Animator;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anubhav.firebasechattingapp2.MessagingContract;
import com.anubhav.firebasechattingapp2.R;
import com.anubhav.firebasechattingapp2.UserActivityPackage.User;
import com.anubhav.firebasechattingapp2.UserDBHelper;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {


    //SQL Create Query
    private static String SQL_CREATE_CHAT_ENTRIES;

    // Views
    private RecyclerView listOfMessages;
    private EditText message_input;
    private FloatingActionButton fab;
    private CardView attachCard;
    private FrameLayout attachFrameLayout;
    private ProgressBar chat_loading;

    // RecyclerView Requirements
    private LinearLayoutManager mLayoutManager;
    private ChatMessageAdapter mAdapter;
    private List<ChatMessage> ChatList;

    // Firebase References
    private DatabaseReference databaseReference;
    private StorageReference UploadRef;

    private String CHAT_TABLE_NAME;
    private UserDBHelper userDBHelper = new UserDBHelper(this);

    private ChildEventListener childEventListener;
    private SQLiteDatabase database;

    private long NumberOfMessages = 20;
    private long NumberOfTableMessages;
    private boolean isLoading = false;

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

        storageReference = FirebaseStorage.getInstance().getReference();
        ChatList = new ArrayList<>();

        initializeViews();
        initializeUsers();

       // SQLiteDatabase sqLiteDatabase = userDBHelper.getWritableDatabase();
       // sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CHAT_TABLE_NAME);
        // sqLiteDatabase.execSQL(SQL_CREATE_CHAT_ENTRIES);
        // sqLiteDatabase.execSQL("DELETE FROM " + CHAT_TABLE_NAME);
        setListeners();
        initializeAdapter();
        initializeLocalData();
        initializeCloudData();
        displayChatMessages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addmedia,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.attach_file) {

            if(attachFrameLayout.getVisibility()==View.INVISIBLE || attachFrameLayout.getVisibility()==View.GONE ){
                int cx = attachCard.getWidth()/2;
                int cy = attachCard.getHeight()/2;
                float radius = (float) Math.hypot(cx, cy);
                Animator animator = ViewAnimationUtils.createCircularReveal(
                        attachCard,
                        cx,
                        cy,
                        0,
                        radius
                );
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        attachFrameLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
            }

            else if (attachFrameLayout.getVisibility()==View.VISIBLE){
                int cx = attachCard.getWidth()/2;
                int cy = attachCard.getHeight()/2;
                float radius = (float) Math.hypot(cx, cy);
                Animator animator = ViewAnimationUtils.createCircularReveal(
                        attachCard,
                        cx,
                        cy,
                        radius,
                        0
                );
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        attachFrameLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                animator.start();
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
    protected void onDestroy() {
        // databaseReference.removeEventListener(childEventListener);
        // userDBHelper.close();
        super.onDestroy();
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

    // CALLED AT START OF ACTIVITY
    private void initializeViews() {
        listOfMessages = findViewById(R.id.list_of_messages);
        mLayoutManager = new LinearLayoutManager(this);
        fab = findViewById(R.id.fab);
        message_input = findViewById(R.id.message_input);
        attachCard = findViewById(R.id.attach_card);
        attachFrameLayout = findViewById(R.id.attach_activity_layout);
        chat_loading = findViewById(R.id.chat_loading);

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(listOfMessages.getContext(),R.anim.layout_slide_from_bottom);
        mLayoutManager.setStackFromEnd(true);
        listOfMessages.setLayoutManager(mLayoutManager);
    }

    private void initializeUsers() {
        Intent intent = getIntent();
        Sender = new User(intent.getStringExtra("SenderName"),
                intent.getStringExtra("SenderID"),
                null,
                null,
                intent.getStringExtra("SenderPhoto"));
        Reciever = new User(intent.getStringExtra("RecieverName"),
                intent.getStringExtra("RecieverID"),
                null,
                null,
                intent.getStringExtra("RecieverPhoto"));

        CHAT_TABLE_NAME = Sender.getUser().charAt(0) + Sender.getUid() + Reciever.getUid();
        SQL_CREATE_CHAT_ENTRIES =  "CREATE TABLE " + CHAT_TABLE_NAME + " ("
                + MessagingContract.ChatDatabase._ID + " INTEGER PRIMARY KEY, "
                + MessagingContract.ChatDatabase.MESSAGE_TEXT + " TEXT, "
                + MessagingContract.ChatDatabase.MESSAGE_SENDER + " TEXT, "
                + MessagingContract.ChatDatabase.MESSAGE_RECIEVER + " TEXT, "
                + MessagingContract.ChatDatabase.MESSAGE_TIME + " INTEGER, "
                + MessagingContract.ChatDatabase.MESSAGE_STATUS + " TEXT, "
                + MessagingContract.ChatDatabase.MESSAGE_CONTENT_TYPE + " INTEGER, "
                + MessagingContract.ChatDatabase.MESSAGE_THUMBNAIL + " TEXT, "
                + MessagingContract.ChatDatabase.MESSAGE_LOCAL_URL + " TEXT, "
                + MessagingContract.ChatDatabase.MESSAGE_LOCAL_THUMBNAIL + " TEXT )";

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Chats")
                .child(Sender.getUid())
                .child(Reciever.getUid());

        getSupportActionBar().setTitle(Reciever.getUser());
    }

    private void setListeners() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message_input.getText().toString().equals("")) {
                    showToast("Input cannot be empty");
                }
                else {
                    sendData(message_input.getText().toString(),ChatMessage.TEXT, null, "");
                }
            }
        });

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
                if(attachFrameLayout.getVisibility()==View.VISIBLE){
                    int cx = attachCard.getWidth()/2;
                    int cy = attachCard.getHeight()/2;
                    float radius = (float) Math.hypot(cx, cy);
                    Animator animator = ViewAnimationUtils.createCircularReveal(
                            attachCard,
                            cx,
                            cy,
                            radius,
                            0
                    );
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            attachFrameLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });

                    animator.start();
                }
            }
        });


    }

    private void initializeAdapter() {
        database  = userDBHelper.getWritableDatabase();
        mAdapter = new ChatMessageAdapter(ChatList, this, CHAT_TABLE_NAME);
       // mAdapter.notifyDataSetChanged();
        mAdapter.notifyItemInserted(ChatList.size());
    }

    private void initializeLocalData() {
        Cursor Tablecursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"
                + CHAT_TABLE_NAME + "'", null);

        if (Tablecursor.getCount() <= 0) {
            database.execSQL(SQL_CREATE_CHAT_ENTRIES);
        }
        NumberOfTableMessages = DatabaseUtils.queryNumEntries(database, CHAT_TABLE_NAME);

        String sortOrder = MessagingContract.ChatDatabase.MESSAGE_TIME + " DESC";
        String[] projections = {
                MessagingContract.ChatDatabase.MESSAGE_TEXT,
                MessagingContract.ChatDatabase.MESSAGE_SENDER,
                MessagingContract.ChatDatabase.MESSAGE_RECIEVER,
                MessagingContract.ChatDatabase.MESSAGE_TIME,
                MessagingContract.ChatDatabase.MESSAGE_STATUS,
                MessagingContract.ChatDatabase.MESSAGE_CONTENT_TYPE,
                MessagingContract.ChatDatabase.MESSAGE_THUMBNAIL,
                MessagingContract.ChatDatabase.MESSAGE_LOCAL_URL,
                MessagingContract.ChatDatabase.MESSAGE_LOCAL_THUMBNAIL
        };

        Cursor cursor = database.query(
                CHAT_TABLE_NAME,
                projections,
                null,
                null,
                null,
                null,
                sortOrder,
                String.valueOf(NumberOfMessages)
        );

        while (cursor.moveToNext()) {
            String message = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_TEXT));
            String Sender = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_SENDER));
            String Reciever = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_RECIEVER));
            long Time = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_TIME)));
            StatusOfMessage Status = StatusOfMessage.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_STATUS)));
            int ContentType = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_CONTENT_TYPE)));
            String Thumbnail = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_THUMBNAIL));
            String LocalMediaURL = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_LOCAL_URL));
            String LocalThumbnailURL = cursor.getString(cursor.getColumnIndexOrThrow(MessagingContract.ChatDatabase.MESSAGE_LOCAL_THUMBNAIL));

            ChatMessage newMessage = new ChatMessage(message, Sender, Reciever, Time, Status, ContentType, Thumbnail, LocalMediaURL, LocalThumbnailURL);
            if(ChatList.size()==0) {
                ChatList.add(0,newMessage);
            }
            else if(ChatList.size()>0) {
                if(ChatList.get(0).getMessageTime() > newMessage.getMessageTime()){
                    ChatList.add(0,newMessage);
                    mAdapter.notifyItemInserted(0);
                }
            }
        }
    }

    private void initializeCloudData() {

        childEventListener = databaseReference.addChildEventListener(new ChildEventListener() {
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
                    values.put(MessagingContract.ChatDatabase.MESSAGE_LOCAL_URL,"");
                    values.put(MessagingContract.ChatDatabase.MESSAGE_LOCAL_THUMBNAIL,"");
                    database.insert(CHAT_TABLE_NAME, null, values);

                    ChatList.add(new ChatMessage(message, Sender, Reciever, Time, Status, ContentType,
                            Thumbnail, "", ""));
                    NumberOfMessages++;
                    // mAdapter.notifyDataSetChanged();
                    mAdapter.notifyItemInserted(ChatList.size());
                }
                NumberOfTableMessages = DatabaseUtils.queryNumEntries(database, CHAT_TABLE_NAME);
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

    private void displayChatMessages() {
        listOfMessages.setAdapter(mAdapter);
       // mLayoutManager.smoothScrollToPosition(listOfMessages, null,ChatList.size());

        listOfMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (!recyclerView.canScrollVertically(-1)) {
                    Log.d("Loading", String.valueOf(isLoading));
                    if(!isLoading) {
                        isLoading = true;
                        final long OldNumber = NumberOfMessages;
                        Log.d("ScrollOld", String.valueOf(NumberOfMessages));
                        NumberOfMessages +=20;
                        if(NumberOfMessages>=NumberOfTableMessages) {
                            NumberOfMessages = NumberOfTableMessages;
                        }
                        Log.d("ScrollNew", String.valueOf(NumberOfMessages));
                        if(OldNumber < NumberOfTableMessages)
                            chat_loading.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        initializeLocalData();
                                        chat_loading.setVisibility(View.GONE);
                                        if(OldNumber!= NumberOfTableMessages)
                                            mLayoutManager.scrollToPosition(Integer.parseInt(String.valueOf(NumberOfMessages-OldNumber-1)));
                                    }
                                }
                        , 1500);
                        Log.d("ScrollTo",String.valueOf(NumberOfMessages-OldNumber));
                    }
                    isLoading = false;
                    super.onScrolled(recyclerView, dx, dy);
                }
            }

        });
        listOfMessages.getAdapter().notifyDataSetChanged();
    }


    // SEND VIDEO, AUDIO OR DOCUMENT
    private void uploadFile(Uri data, String typeOfData, final int contentType) throws IOException {
        if(typeOfData=="Images") {
            compressAndSendImage(data);
        }
        else {
            if(new File(data.getPath()).length()<=26214400) {
                String fileName = null;
                try {
                    String filePath = getPath(this, data);
                    fileName = new Date().getTime() + " " +  filePath.substring(filePath.lastIndexOf("/")+1);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                UploadRef = storageReference.child(typeOfData).child(Sender.getUid()).child(Reciever.getUid()).child(fileName);
                Log.d("File Upload", fileName);
                UploadTask uploadTask = UploadRef.putFile(data);

                showNotification(uploadTask, contentType, data, this);
            }
            else {
                showToast("Files larger thar 25 MB cannot be uploaded");
            }
        }
    }

    // SEND IMAGE
    private void compressAndSendImage (Uri data) throws IOException {
        byte[] bdata = compressImage(data);
        String fileName = new Date().getTime() + getFileName(data) ;
        UploadRef = storageReference.child("Images").child(Sender.getUid()).child(Reciever.getUid()).child(fileName);
        UploadTask uploadTask = UploadRef.putBytes(bdata);

        showNotification(uploadTask, ChatMessage.IMAGE, data, this);
    }

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


    private void showNotification(final UploadTask uploadTask, final int contentType, final Uri data, final Context context) {
        final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Uploading File")
                .setContentText("Uploading...")
                .setSmallIcon(R.drawable.ic_attach_file)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100,0,false);
        notificationManagerCompat.notify(NOTIFICATION_ID, mBuilder.build());
        Log.d("Upload Local Uri", data.toString());

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
                            try {
                                sendData(task.getResult().toString(), contentType, null, getPath(context, data));
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                            status = "Upload Succesful";
                        }
                        else {
                            status = "Upload Failed";
                        }
                        showToast(status);
                    }
                });
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;

        if (DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{ split[1] };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    Log.d("File Path", cursor.getString(column_index));
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    Log.d("File Name", result);
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


    private void showToast(String text) {
        Toast.makeText(this, text,Toast.LENGTH_LONG).show();
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

    // CALLED BY ATTACH ICONS
    public void launchGalleryImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,RC_TAKE_PICTURE);
    }

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

    private void launchGalleryVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,RC_TAKE_VIDEO);
    }

    private void launchGalleryAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,RC_TAKE_AUDIO);
    }

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


    // Send Text data
    private void sendData(String data, int contentType, String thumbnailURL, String localMediaURL) {
        long Time = new Date().getTime();

        ContentValues values = new ContentValues();
        values.put(MessagingContract.ChatDatabase.MESSAGE_TEXT, data);
        values.put(MessagingContract.ChatDatabase.MESSAGE_SENDER, Sender.getUser());
        values.put(MessagingContract.ChatDatabase.MESSAGE_RECIEVER, Reciever.getUser());
        values.put(MessagingContract.ChatDatabase.MESSAGE_TIME, String.valueOf(Time));
        values.put(MessagingContract.ChatDatabase.MESSAGE_STATUS, String.valueOf(StatusOfMessage.OUT_MESSAGE));
        values.put(MessagingContract.ChatDatabase.MESSAGE_CONTENT_TYPE, contentType);
        values.put(MessagingContract.ChatDatabase.MESSAGE_THUMBNAIL, "");
        values.put(MessagingContract.ChatDatabase.MESSAGE_LOCAL_URL, localMediaURL);
        values.put(MessagingContract.ChatDatabase.MESSAGE_LOCAL_THUMBNAIL,"");
        database.insert(CHAT_TABLE_NAME, null, values);

        ChatList.add(new ChatMessage(data, Sender.getUser(), Reciever.getUser(), Time, StatusOfMessage.OUT_MESSAGE,
                contentType, thumbnailURL, localMediaURL, ""));
        NumberOfMessages++;
        mAdapter.notifyItemInserted(ChatList.size());

        FirebaseDatabase.getInstance().getReference("Chats")
                .child(Sender.getUid())
                .child(Reciever.getUid())
                .child(String.valueOf(new Date().getTime()))
                .setValue(new ChatMessage(data, Sender.getUser(), Reciever.getUser(), Time, StatusOfMessage.OUT_MESSAGE,
                        contentType, thumbnailURL, null, null));
        FirebaseDatabase.getInstance().getReference("Chats")
                .child(Reciever.getUid())
                .child(Sender.getUid())
                .child(String.valueOf(new Date().getTime()))
                .setValue(new ChatMessage(data, Sender.getUser(), Reciever.getUser(), Time, StatusOfMessage.IN_MESSAGE,
                        contentType, thumbnailURL, null, null));
        message_input.setText("");
        mLayoutManager.smoothScrollToPosition(listOfMessages, null, ChatList.size());
    }
}
