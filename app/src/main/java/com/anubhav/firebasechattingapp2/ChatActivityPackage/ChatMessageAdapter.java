package com.anubhav.firebasechattingapp2.ChatActivityPackage;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders.AudioViewHolder;
import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders.DocumentViewHolder;
import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders.ImageViewHolder;
import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders.TextViewHolder;
import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatViewHolders.VideoViewHolder;
import com.anubhav.firebasechattingapp2.GlideApp;
import com.anubhav.firebasechattingapp2.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class ChatMessageAdapter extends FirebaseRecyclerAdapter<ChatMessage, ChatHolder> {

    public ChatMessageAdapter(FirebaseRecyclerOptions<ChatMessage> options) {
        super(options);
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ChatMessage.TEXT :
                return new TextViewHolder(
                        inflater.inflate(R.layout.chat_text_layout, parent, false));

            case ChatMessage.IMAGE :
                return new ImageViewHolder(
                        inflater.inflate(R.layout.chat_image_layout, parent, false));

            case ChatMessage.VIDEO :
                return new VideoViewHolder(
                        inflater.inflate(R.layout.chat_video_layout, parent, false));

            case ChatMessage.AUDIO :
                return new AudioViewHolder(
                        inflater.inflate(R.layout.chat_audio_layout, parent, false));

            case ChatMessage.DOCUMENT :
                return new DocumentViewHolder(
                        inflater.inflate(R.layout.chat_document_layout, parent, false));
        }
        return null;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatHolder holder, int position, @NonNull ChatMessage model) {
        holder.message_time.setText(DateFormat.format("dd/MM/yyyy (HH:mm)",
                model.getMessageTime()));

        if (model.getContentType()==ChatMessage.TEXT) {
            handleTextMessage(((TextViewHolder) holder), model);
        }
        else if(model.getContentType()==ChatMessage.IMAGE) {
            handleImageMessage(((ImageViewHolder) holder), model);
        }
        else if(model.getContentType()==ChatMessage.VIDEO){
            handleVideoMessage(((VideoViewHolder) holder), model);
        }
        else if(model.getContentType()==ChatMessage.AUDIO){
            try {
                handleAudioMessage(((AudioViewHolder) holder), model);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(model.getContentType()==ChatMessage.DOCUMENT){
            handleDocumentMessage(((DocumentViewHolder) holder), model);
        }

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) holder.message_layout.getLayoutParams();
        if(model.getStatusOfMessage().equals(StatusOfMessage.OUT_MESSAGE)) {
            holder.message_relativelayout.setBackgroundColor(holder.message_layout.getResources().getColor(R.color.messageOutBubble));
            layoutParams.gravity = GravityCompat.END;
        }
        else {
            holder.message_relativelayout.setBackgroundColor(holder.message_layout.getResources().getColor(R.color.messageInBubble));
            layoutParams.gravity = GravityCompat.START;
        }
        holder.message_layout.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getContentType();
    }

    private void handleTextMessage(TextViewHolder holder, ChatMessage model) {
        holder.message_text.setText(model.getMessageText());
    }

    private void handleImageMessage(ImageViewHolder holder, ChatMessage model) {
        showImage(holder.message_image, model.getMessageText());
        setImageListener(holder.message_image, model.getMessageText());
    }

    private void handleVideoMessage(final VideoViewHolder holder, final ChatMessage model)  {
        ImageView message_video_thumbnail = holder.message_video.findViewById(R.id.message_video_thumbnail);
        GlideApp.with(message_video_thumbnail.getContext())
                .load(model.getThumbnailURL())
                .into(message_video_thumbnail);
        setVideoListener(holder.message_video, model.getMessageText());
    }

    private void handleAudioMessage(final AudioViewHolder holder, final ChatMessage model) throws IOException {
        boolean plaWhenReady = false;
        int currentWindow = 0;
        long playbackPostion = 0;
        Uri audioURI = Uri.parse(model.getMessageText());

        MediaSource mediaSource = new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("FirebaseChattingApp2"))
                .createMediaSource(audioURI);

        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(holder.audio_play.getContext()),
                new DefaultTrackSelector(),
                new DefaultLoadControl());

        holder.audio_play.setPlayer(player);
        player.setPlayWhenReady(plaWhenReady);
        player.seekTo(currentWindow, playbackPostion);
        player.prepare(mediaSource, true, false);
    }

    private void handleDocumentMessage(final DocumentViewHolder holder, final ChatMessage model) {

        final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(model.getMessageText());
        storageReference.getMetadata().addOnCompleteListener(new OnCompleteListener<StorageMetadata>() {
            @Override
            public void onComplete(@NonNull Task<StorageMetadata> task) {
                final String contentType = task.getResult().getContentType();

                holder.message_document_name.setText(task.getResult().getName());

                holder.message_document.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
               /*Intent intent = new Intent(holder.message_document.getContext(),DocumentViewerActivity.class);
               intent.putExtra("DocumentURL", model.getMessageText());
               holder.message_document.getContext().startActivity(intent);*/
                        Log.d("ContentType",contentType);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        if(contentType.equals("application/pdf")) {
                            Log.d("ContentType",contentType);
                            intent.setDataAndType(Uri.parse(model.getMessageText()), "application/pdf");
                        }
                        else if(contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || contentType.equals("application/msword")){
                            Log.d("ContentType",contentType);
                            intent.setDataAndType(Uri.parse(model.getMessageText()), "application/vnd.google-apps.document");
                        }
                        else if(contentType.equals("application/vnd.ms-powerpoint") ||contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                            Log.d("ContentType",contentType);
                            intent.setDataAndType(Uri.parse(model.getMessageText()), "application/vnd.google-apps.presentation");
                        }
                        else if(contentType.equals("application/vnd.ms-excel") || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                            Log.d("ContentType",contentType);
                            intent.setDataAndType(Uri.parse(model.getMessageText()), "application/vnd.google-apps.spreadsheet");
                        }
                        else if(contentType.equals("text/plain")) {
                            Log.d("ContentType",contentType);
                            intent.setDataAndType(Uri.parse(model.getMessageText()), "text/plain");
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Intent newIntent = Intent.createChooser(intent, "Open File");
                        try {
                            holder.message_document.getContext().startActivity(newIntent);
                        } catch (ActivityNotFoundException e) {
                        }
                    }
                });
            }
        });


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

