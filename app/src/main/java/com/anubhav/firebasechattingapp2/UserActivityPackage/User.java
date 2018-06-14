package com.anubhav.firebasechattingapp2.UserActivityPackage;


public class User {

    private String user;
    private String uid;
    private String lastMessage;
    private String lastMessageStat;
    private String profilePictureURL;

    public User(String user, String uid, String lastMessage, String lastMessageStat, String profilePictureURL) {
        this.user = user;
        this.uid = uid;
        this.lastMessage = lastMessage;
        this.lastMessageStat = lastMessageStat;
        this.profilePictureURL = profilePictureURL;
    }

    public User() {}

    public String getUser(){
        return user;
    }
    public String getUid() {
        return uid;
    }
    public String getLastMessage() {
        return lastMessage;
    }
    public String getLastMessageStat() {
        return lastMessageStat;
    }
    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public void setUser(String user) {
        this.user = user;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    public void setLastMessageStat(String lastMessageStat) {
        this.lastMessageStat = lastMessageStat;
    }
    public void setProfilePictureURL(String profilePictureURL) {
        this.profilePictureURL = profilePictureURL;
    }
}
