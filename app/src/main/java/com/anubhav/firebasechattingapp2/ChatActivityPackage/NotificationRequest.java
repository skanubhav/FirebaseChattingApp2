package com.anubhav.firebasechattingapp2.ChatActivityPackage;

import com.anubhav.firebasechattingapp2.UserActivityPackage.User;

public class NotificationRequest {
    private User Sender;
    private String chatMessage;
    private User Reciever;

    public NotificationRequest(User Sender, String chatMessage, User Reciever) {
      this.Sender = Sender;
      this.Reciever = Reciever;
      this.chatMessage = chatMessage;
    }

    public User getReciever() {
        return Reciever;
    }
    public User getSender() {
        return Sender;
    }
    public String getChatMessage() {
        return chatMessage;
    }

    public void setSender(User sender) {
        Sender = sender;
    }
    public void setReciever(User reciever) {
        Reciever = reciever;
    }
    public void setChatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
    }
}


