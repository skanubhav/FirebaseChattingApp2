package com.anubhav.firebasechattingapp2;

import android.provider.BaseColumns;

public final class MessagingContract {

    private MessagingContract(){}

    public static class UserDatabase implements BaseColumns {
        public static String TABLE_NAME = "Users";
        public static String COLUMN_NAME = "User_Name";
        public static String COLUMN_ID = "User_ID";
    }
}
