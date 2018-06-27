package com.anubhav.firebasechattingapp2.ChatActivityPackage;

public class ChatMessage {

    public final static int TEXT = 0;
    public final static int IMAGE = 1;
    public final static int VIDEO = 2;
    public final static int AUDIO = 3;
    public final static int DOCUMENT = 4;
    public final static int DATE_CHANGE=6;

    private String messageText;
    private String messageSender;
    private String messageReciever;
    private long messageTime;
    private StatusOfMessage statusOfMessage;
    private int contentType;
    private String thumbnailURL;
    private String localMediaURL;
    private String localThumbnailURL;

    public ChatMessage (String messageText, String messageSender, String messageReciever, long messageTime,
                        StatusOfMessage statusOfMessage, int contentType, String thumbnailURL, String localMediaURL,
                        String localThumbnailURL) {
        this.messageText = messageText;
        this.messageSender = messageSender;
        this.messageReciever = messageReciever;
        this.statusOfMessage = statusOfMessage;
        this.contentType=contentType;
        this.messageTime = messageTime;
        this.thumbnailURL = thumbnailURL;
        this.localMediaURL = localMediaURL;
        this.localThumbnailURL = localThumbnailURL;
    }

    public ChatMessage(){}

    public String getMessageText() {
        return messageText;
    }
    public String getMessageReciever() {
        return messageReciever;
    }
    public String getMessageSender() {
        return messageSender;
    }
    public int getContentType() {
        return contentType;
    }
    public long getMessageTime() {
        return messageTime;
    }
    public StatusOfMessage getStatusOfMessage() {
        return statusOfMessage;
    }
    public String getThumbnailURL() {
        return thumbnailURL;
    }
    public String getLocalMediaURL() {
        return localMediaURL;
    }
    public String getLocalThumbnailURL() {
        return localThumbnailURL;
    }

    public void setMessageText(String messageText) {
        this.messageText=messageText;
    }
    public void setMessageTime(long messageTime) {
        this.messageTime=messageTime;
    }
    public void setContentType(int contentType) {
        this.contentType = contentType;
    }
    public void setMessageReciever(String messageReciever) {
        this.messageReciever = messageReciever;
    }
    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }
    public void setStatusOfMessage(StatusOfMessage statusOfMessage) {
        this.statusOfMessage = statusOfMessage;
    }
    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }
    public void setLocalMediaURL(String localMediaURL) {
        this.localMediaURL = localMediaURL;
    }
    public void setLocalThumbnailURL(String localThumbnailURL) {
        this.localThumbnailURL = localThumbnailURL;
    }
}
