package com.anubhav.firebasechattingapp2.ChatActivityPackage;

public class NotificationRequest {
    private String Uid;
    private String message;
    private String Uname;

    public NotificationRequest(String Uid, String message, String Uname) {
        this.Uid = Uid;
        this.message = message;
        this.Uname = Uname;
    }


    public String getUid() {
        return Uid;
    }
    public String getMessage() {
        return message;
    }
    public String getUname() {
        return Uname;
    }

    public void setUid(String uid) {
        Uid = uid;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setUname(String uname) {
        Uname = uname;
    }
}


