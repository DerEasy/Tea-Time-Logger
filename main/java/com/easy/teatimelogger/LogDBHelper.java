package com.easy.teatimelogger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.easy.teatimelogger.LogContract.*;

import androidx.annotation.Nullable;

public class LogDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "teatimelog.db";
    public static final int DATABASE_VERSION = 4;

    public LogDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_TEATIMELOG_TABLE = "CREATE TABLE " +
                LogEntry.TABLE_NAME + " (" +
                LogEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LogEntry.COLUMN_DATE + " DATETIME DEFAULT (datetime('now','localtime')), " +
                LogEntry.COLUMN_MINUTES + " INTEGER NOT NULL, " +
                LogEntry.COLUMN_SECONDS + " INTEGER NOT NULL " +
                ");";

        db.execSQL(SQL_CREATE_TEATIMELOG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}