package ru.furman.smartnotes.note.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "notesDB";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NOTES = "notesTable";

    public static final String KEY_ID = "_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_IMPORTANCE = "importance";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_LATTITUDE = "lattitude";
    public static final String KEY_LONGITUDE = "longitude";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_NOTES + " (" + KEY_ID + " integer primary key, " + KEY_TITLE + " text, " + KEY_BODY + " text, " + KEY_IMPORTANCE + " text, "+KEY_PHOTO + " text, "+KEY_LATTITUDE+" real, "+KEY_LONGITUDE+" real)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + DATABASE_NAME);
        onCreate(db);
    }
}
