package com.anubhav.firebasechattingapp2;


public class User {

    private String user;
    private String uid;

    public User(String user, String uid) {
        this.user = user;
        this.uid = uid;
    }

    public User() {}

    public String getUser(){
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
