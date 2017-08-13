package ru.furman.smartnotes.note.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ru.furman.smartnotes.note.database.NotesDBContract.NotesTable;

class NotesDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "notesDB";
    public static final int DATABASE_VERSION = 1;

    public static final String SQL_CREATE_TABLE_NOTES = "create table if not exists " + NotesTable.TABLE_NAME +
            " (" + NotesTable._ID + " integer primary key, " + NotesTable.COLUMN_NAME_TITLE +
            " text, " + NotesTable.COLUMN_NAME_BODY + " text, " + NotesTable.COLUMN_NAME_IMPORTANCE +
            " text, "+NotesTable.COLUMN_NAME_PHOTO + " text, "+NotesTable.COLUMN_NAME_LATTITUDE+" real, "+NotesTable.COLUMN_NAME_LONGITUDE+
            " real)";
    public static final String SQL_DELETE_TABLE_NOTES = "drop table if exists " + NotesTable.TABLE_NAME;

    public NotesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_NOTES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE_NOTES);
        onCreate(db);
    }
}
