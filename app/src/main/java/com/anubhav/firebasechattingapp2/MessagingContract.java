package com.anubhav.firebasechattingapp2;

import android.provider.BaseColumns;

public final class MessagingContract {

    private MessagingContract(){}

    public static class UserDatabase implements BaseColumns {
        public static String TABLE_NAME = "Users";
        public static String COLUMN_NAME = "User_Name";
        public static String COLUMN_ID = "User_ID";
        public static String COLUMN_LAST_MESSAGE = "LastMessage";
        public static String COLUMN_LAST_MESSAGE_STAT = "LastMessageStat";
        public static String COLUMN_PROFILE_IMAGE = "ProfileImageURL";
    }

    public static class ChatDatabase implements  BaseColumns {
        public static String MESSAGE_TEXT = "Text";
        public static String MESSAGE_SENDER = "Sender";
        public static String MESSAGE_RECIEVER = "Reciever";
        public static String MESSAGE_TIME = "Time";
        public static String MESSAGE_STATUS = "StatusOfMessage";
        public static String MESSAGE_CONTENT_TYPE = "ContentType";
        public static String MESSAGE_THUMBNAIL = "ThumbnailURL";
    }
}
