package ru.furman.smartnotes.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.furman.smartnotes.Note;

/**
 * Created by Furman on 26.07.2017.
 */

public class DB {

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Context ctx;

    public DB(Context context) {
        this.ctx = context;
    }

    public void addNote(Note note) {
        dbHelper = new DBHelper(ctx);
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBHelper.KEY_TITLE, note.getTitle());
        cv.put(DBHelper.KEY_BODY, note.getBody());
        cv.put(DBHelper.KEY_IMPORTANCE, note.getImportance());

        db.insert(DBHelper.TABLE_NOTES, null, cv);
        dbHelper.close();
    }

    @Nullable
    public List<Note> getNotes() {
        ArrayList<Note> list = null;

        dbHelper = new DBHelper(ctx);
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DBHelper.TABLE_NOTES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            list = new ArrayList<>();
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int titleIndex = cursor.getColumnIndex(DBHelper.KEY_TITLE);
            int bodyIndex = cursor.getColumnIndex(DBHelper.KEY_BODY);
            int importanceIndex = cursor.getColumnIndex(DBHelper.KEY_IMPORTANCE);
            do {
                list.add(new Note(cursor.getString(titleIndex),cursor.getString(bodyIndex),cursor.getString(importanceIndex),cursor.getInt(idIndex)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        dbHelper.close();

        return list;
    }

    public void deleteNote(int id){
        dbHelper = new DBHelper(ctx);
        db = dbHelper.getWritableDatabase();

        db.delete(DBHelper.TABLE_NOTES,"id = "+id,null);

        dbHelper.close();
    }

    public void editNote(int id, Note note){
        dbHelper = new DBHelper(ctx);
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBHelper.KEY_TITLE, note.getTitle());
        cv.put(DBHelper.KEY_BODY, note.getBody());
        cv.put(DBHelper.KEY_IMPORTANCE, note.getImportance());

        db.update(DBHelper.TABLE_NOTES,cv,"id = "+id,null);

        dbHelper.close();
    }

}
