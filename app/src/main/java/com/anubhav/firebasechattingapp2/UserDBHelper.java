package com.anubhav.firebasechattingapp2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDBHelper extends SQLiteOpenHelper {

    public static String SQL_CREATE_USER_ENTRIES =  "CREATE TABLE " + MessagingContract.UserDatabase.TABLE_NAME + " ("
            + MessagingContract.UserDatabase._ID + " INTEGER PRIMARY KEY, "
            + MessagingContract.UserDatabase.COLUMN_ID + " TEXT, "
            + MessagingContract.UserDatabase.COLUMN_NAME + " TEXT, "
            + MessagingContract.UserDatabase.COLUMN_LAST_MESSAGE + " TEXT, "
            + MessagingContract.UserDatabase.COLUMN_LAST_MESSAGE_STAT + " TEXT, "
            +  MessagingContract.UserDatabase.COLUMN_PROFILE_IMAGE + " TEXT )";

    private static final String SQL_DELETE_USER_ENTRIES = "DROP TABLE IF EXISTS " + MessagingContract.UserDatabase.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FirebaseMessaging.db";

    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        createUserTable(db);
    }

    private void createUserTable(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USER_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_USER_ENTRIES);
        onCreate(db);
    }
}
