package com.anubhav.firebasechattingapp2.ChatActivityPackage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders.AudioViewHolder;
import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders.DocumentViewHolder;
import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders.ImageViewHolder;
import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders.TextViewHolder;
import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders.VideoViewHolder;
import com.anubhav.firebasechattingapp2.GlideApp;
import com.anubhav.firebasechattingapp2.MessagingContract;
import com.anubhav.firebasechattingapp2.R;
import com.anubhav.firebasechattingapp2.UserActivityPackage.User;
import com.anubhav.firebasechattingapp2.UserDBHelper;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatHolder> {


    private List<ChatMessage> ChatList;
    private Context context;
    private String CHAT_TABLE_NAME;

    public ChatMessageAdapter(List<ChatMessage> ChatList, Context context, String CHAT_TABLE_NAME) {
        this.context = context;
        this.ChatList = ChatList;
        this.CHAT_TABLE_NAME = CHAT_TABLE_NAME;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ChatHolder chatHolder = null;

        switch (viewType) {
            case ChatMessage.TEXT :
                chatHolder = new TextViewHolder(
                        inflater.inflate(R.layout.chat_text_layout, parent, false));
                break;

            case ChatMessage.IMAGE :
                chatHolder = new ImageViewHolder(
                        inflater.inflate(R.layout.chat_image_layout, parent, false));
                break;

            case ChatMessage.VIDEO :
                chatHolder =  new VideoViewHolder(
                        inflater.inflate(R.layout.chat_video_layout, parent, false));
                break;

            case ChatMessage.AUDIO :
                chatHolder = new AudioViewHolder(
                        inflater.inflate(R.layout.chat_audio_layout, parent, false));
                break;

            case ChatMessage.DOCUMENT :
                chatHolder = new DocumentViewHolder(
                        inflater.inflate(R.layout.chat_document_layout, parent, false));
                break;
        }
        return chatHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatHolder holder, final int position) {
        holder.message_time.setText(DateFormat.format("dd/MM/yyyy (HH:mm)",
                ChatList.get(position).getMessageTime()));

        if(ChatActivity.selectedPosition == position) {
           holder.message_select.setVisibility(View.VISIBLE);
        }
        else {
           holder.message_select.setVisibility(View.GONE);
        }

        if (ChatList.get(position).getContentType()==ChatMessage.TEXT) {
            handleTextMessage(((TextViewHolder) holder), ChatList.get(position));
        }
        else if(ChatList.get(position).getLocalMediaURL().equals("")) {
            setDownloadListener(holder, ChatList.get(position), position);
        }
        else {
            Log.d("ChatMessageAdapter",ChatList.get(position).getLocalMediaURL());
            if(ChatList.get(position).getContentType()==ChatMessage.IMAGE) {
                handleImageMessage(((ImageViewHolder) holder), ChatList.get(position));
            }
            else if(ChatList.get(position).getContentType()==ChatMessage.VIDEO){
                handleVideoMessage(((VideoViewHolder) holder),ChatList.get(position), position);
            }
            else if(ChatList.get(position).getContentType()==ChatMessage.AUDIO){
           /* try {
                handleAudioMessage(((AudioViewHolder) holder), ChatList.get(position));
            }
            catch (IOException e) { e.printStackTrace(); } */
            }
            else if(ChatList.get(position).getContentType()==ChatMessage.DOCUMENT){
               handleDocumentMessage(((DocumentViewHolder) holder),ChatList.get(position));
            }
        }

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) holder.message_layout.getLayoutParams();
        if(ChatList.get(position).getStatusOfMessage().equals(StatusOfMessage.OUT_MESSAGE)) {
            holder.message_relativelayout.setBackgroundColor(holder.message_layout.getResources().getColor(R.color.messageOutBubble));
            layoutParams.gravity = GravityCompat.END;
        }
        else {
            holder.message_relativelayout.setBackgroundColor(holder.message_layout.getResources().getColor(R.color.messageInBubble));
            layoutParams.gravity = GravityCompat.START;
        }
        holder.message_layout.setLayoutParams(layoutParams);
    }

    private void setDownloadListener(final ChatHolder holder, final ChatMessage model, final int position) {
        holder.message_download.setVisibility(View.VISIBLE);
        holder.message_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.message_download.setVisibility(View.GONE);
                holder.message_download_progress.setVisibility(View.VISIBLE);

                final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(model.getMessageText());
                storageReference.getMetadata().addOnCompleteListener(new OnCompleteListener<StorageMetadata>() {
                    @Override
                    public void onComplete(@NonNull Task<StorageMetadata> task) {
                        String fileName = task.getResult().getName();
                        Log.d("File Download", fileName);
                        File downloadLocation = null;
                        if(model.getContentType()==ChatMessage.IMAGE) {
                            downloadLocation = new File(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES),
                                    "Firebase Messaging Images");
                        }
                        else if(model.getContentType()==ChatMessage.VIDEO) {
                            downloadLocation = new File(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_MOVIES),
                                    "Firebase Messaging Videos");
                        }

                        else if(model.getContentType()==ChatMessage.AUDIO) {
                            downloadLocation = new File(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_MUSIC),
                                    "Firebase Messaging Audio");
                        }
                        else if(model.getContentType()==ChatMessage.DOCUMENT) {
                            downloadLocation = new File(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOCUMENTS),
                                    "Firebase Messaging Document");
                        }

                        downloadLocation.mkdir();

                        Log.d("Download", fileName);

                        final File downloadFile = new File(downloadLocation,fileName);
                        storageReference.getFile(downloadFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                ContentValues values = new ContentValues();
                                UserDBHelper userDBHelper = new UserDBHelper(context);
                                SQLiteDatabase sqLiteDatabase = userDBHelper.getWritableDatabase();
                                values.put(MessagingContract.ChatDatabase.MESSAGE_LOCAL_URL, downloadFile.getAbsolutePath());

                                ChatList.get(position).setLocalMediaURL(downloadFile.getAbsolutePath());
                                notifyDataSetChanged();

                                sqLiteDatabase.update(CHAT_TABLE_NAME,
                                        values,
                                        MessagingContract.ChatDatabase.MESSAGE_TIME + "=?",
                                        new String[]{String.valueOf(model.getMessageTime())});
                                holder.message_download_progress.setVisibility(View.GONE);
                            }
                        });
                    }
                });
                    }
                });
    }

    @Override
    public int getItemViewType(int position) {
        return ChatList.get(position).getContentType();
    }

    @Override
    public int getItemCount() {
        return ChatList.size();
    }

    private void handleTextMessage(final TextViewHolder holder, ChatMessage model) {
        holder.message_text.setText(model.getMessageText());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CharSequence[] item = {"Copy"};
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.message_text.getContext());
                builder.setTitle("Select Action");
                builder.setItems(item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                return true;
            }
        });
    }

    private void handleImageMessage(ImageViewHolder holder, ChatMessage model) {
        showImage(holder.message_image, model.getLocalMediaURL());
        setImageListener(holder.message_image, model.getLocalMediaURL());
    }

    private void handleVideoMessage(final VideoViewHolder holder, final ChatMessage model, final int position)  {
        ImageView message_video_thumbnail = holder.message_video.findViewById(R.id.message_video_thumbnail);
            Log.d("VideoThumbnail", "Video Thumbnail = " + model.getLocalThumbnailURL());

            GlideApp.with(message_video_thumbnail.getContext())
                    .asBitmap()
                    .load(Uri.fromFile(new File(model.getLocalMediaURL())))
                    .into(message_video_thumbnail);
            setVideoListener(holder.message_video, model.getLocalMediaURL());
    }

    private void handleAudioMessage(final AudioViewHolder holder, final ChatMessage model) throws IOException {
        int currentWindow = 0;
        final Uri audioURI = Uri.parse(model.getLocalMediaURL());
        holder.audio_play.setKeepContentOnPlayerReset(true);
        holder.audio_play.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            private SimpleExoPlayer player;
            private long playbackPostion = 0;

            @Override
            public void onViewAttachedToWindow(View v) {
                 player = ExoPlayerFactory.newSimpleInstance(
                        new DefaultRenderersFactory(context),
                        new DefaultTrackSelector(),
                        new DefaultLoadControl());

                DataSpec dataSpec = new DataSpec(audioURI);
                final FileDataSource fileDataSource = new FileDataSource();
                try {
                    fileDataSource.open(dataSpec);
                } catch (FileDataSource.FileDataSourceException e) {
                    e.printStackTrace();
                }

                DataSource.Factory factory = new DataSource.Factory() {
                    @Override
                    public DataSource createDataSource() {
                        return fileDataSource;
                    }
                };

                MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                        factory, new DefaultExtractorsFactory(), null, null);

                holder.audio_play.setPlayer(player);

                holder.audio_play.setShutterBackgroundColor(R.color.fui_transparent);
                player.setPlayWhenReady(false);
                player.prepare(audioSource, true, false);
                player.seekTo(playbackPostion);
                Log.d("Audio Playback", String.valueOf(playbackPostion));
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if(player!=null) {
                    playbackPostion = player.getCurrentPosition();
                    player.stop();
                }
            }
        });
    }

    private void handleDocumentMessage(final DocumentViewHolder holder, final ChatMessage model) {
        final Uri uri = Uri.parse(model.getLocalMediaURL());
        final String fileName = uri.getLastPathSegment();
        holder.message_document.setVisibility(View.VISIBLE);
        holder.message_document.setBackground(context.getResources().getDrawable(R.drawable.ic_attach_document));
        holder.message_document_name.setText(fileName);
        holder.message_document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (fileName.contains(".doc") || fileName.contains(".docx")) {
                    openFile(new File(model.getLocalMediaURL()),"application/msword");
                }
                else if(fileName.contains(".pdf")) {
                    openFile(new File(model.getLocalMediaURL()),"application/pdf");
                }
                else if(fileName.contains(".ppt") || fileName.contains(".pptx")) {
                    openFile(new File(model.getLocalMediaURL()),"application/vnd.ms-powerpoint");
                }
                else if(fileName.contains(".xls") || fileName.contains(".xlsx")) {
                    openFile(new File(model.getLocalMediaURL()),"application/vnd.ms-excel");
                }
                else {
                    String extension = MimeTypeMap.getFileExtensionFromUrl(model.getLocalMediaURL());
                    if(extension!=null) {
                        openFile(new File(model.getLocalMediaURL()), MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension));
                    }
                }
            }
        });
    }

    private void openFile(File file, String mimeType)  {
        Intent viewIntent = new Intent();
        viewIntent.setAction(Intent.ACTION_VIEW);
        viewIntent.setDataAndType(Uri.fromFile(file), mimeType);
        // using the packagemanager to query is faster than trying startActivity
        // and catching the activity not found exception, which causes a stack unwind.
        List<ResolveInfo> resolved = context.getPackageManager().queryIntentActivities(viewIntent, 0);
        if(resolved != null && resolved.size() > 0)  {
            context.startActivity(viewIntent);
        }
        else {
            Toast.makeText(context, "Cannot open this file",Toast.LENGTH_LONG).show();
        }
    }

    private void setImageListener(final ImageView message_image, final String imageURL) {
        message_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(message_image.getContext(), FullImageActivity.class);
                intent.putExtra("Image URL",imageURL);
                message_image.getContext().startActivity(intent);
            }
        });
    }

    private void setVideoListener(final RelativeLayout message_video, final String videoURL) {
        message_video.findViewById(R.id.message_video_play)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(message_video.getContext(), VideoPlayerActivity.class);
                        intent.putExtra("Video URL",videoURL);
                        message_video.getContext().startActivity(intent);
                    }
                });
    }

    private void showImage(ImageView message_image, String uri) {
        GlideApp.with(message_image.getContext())
                .load(uri)
                .thumbnail(0.1f)
                .into(message_image);
    }
}

