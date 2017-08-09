package ru.furman.smartnotes.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import ru.furman.smartnotes.Note;

public class DB {
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Context ctx;

    public DB(Context context) {
        this.ctx = context;
    }

    public synchronized void addNote(Note note) {
        dbHelper = new DBHelper(ctx);
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBHelper.KEY_TITLE, note.getTitle());
        cv.put(DBHelper.KEY_BODY, note.getBody());
        cv.put(DBHelper.KEY_IMPORTANCE, note.getImportance());
        cv.put(DBHelper.KEY_PHOTO, note.getPhoto());
        cv.put(DBHelper.KEY_LATTITUDE, note.getLocation().latitude);
        cv.put(DBHelper.KEY_LONGITUDE, note.getLocation().longitude);

        db.insert(DBHelper.TABLE_NOTES, null, cv);
        dbHelper.close();
    }

    public synchronized List<Note> getNotes() {
        List<Note> list = new ArrayList<>();

        dbHelper = new DBHelper(ctx);
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DBHelper.TABLE_NOTES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            list = new ArrayList<>();
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int titleIndex = cursor.getColumnIndex(DBHelper.KEY_TITLE);
            int bodyIndex = cursor.getColumnIndex(DBHelper.KEY_BODY);
            int importanceIndex = cursor.getColumnIndex(DBHelper.KEY_IMPORTANCE);
            int photoIndex = cursor.getColumnIndex(DBHelper.KEY_PHOTO);
            int longitudeIndex = cursor.getColumnIndex(DBHelper.KEY_LONGITUDE);
            int lattitudeIndex = cursor.getColumnIndex(DBHelper.KEY_LATTITUDE);
            do {
                list.add(new Note(cursor.getString(titleIndex), cursor.getString(bodyIndex), cursor.getString(importanceIndex), cursor.getString(photoIndex)
                        , new LatLng(cursor.getDouble(lattitudeIndex), cursor.getDouble(longitudeIndex))
                        , cursor.getInt(idIndex)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        dbHelper.close();

        return list;
    }

    public synchronized void deleteNote(int id) {
        dbHelper = new DBHelper(ctx);
        db = dbHelper.getWritableDatabase();

        db.delete(DBHelper.TABLE_NOTES, "_id = " + id, null);

        dbHelper.close();
    }

    public synchronized void editNote(int id, Note note) {
        dbHelper = new DBHelper(ctx);
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBHelper.KEY_TITLE, note.getTitle());
        cv.put(DBHelper.KEY_BODY, note.getBody());
        cv.put(DBHelper.KEY_IMPORTANCE, note.getImportance());
        cv.put(DBHelper.KEY_PHOTO, note.getPhoto());
        cv.put(DBHelper.KEY_LONGITUDE, note.getLocation().longitude);
        cv.put(DBHelper.KEY_LATTITUDE, note.getLocation().latitude);

        db.update(DBHelper.TABLE_NOTES, cv, "_id = " + id, null);

        dbHelper.close();
    }

    @Nullable
    public synchronized Note getNote(long id) {

        dbHelper = new DBHelper(ctx);
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DBHelper.TABLE_NOTES, null, "_id = " + id, null, null, null, null);
        Note res = null;
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int titleIndex = cursor.getColumnIndex(DBHelper.KEY_TITLE);
            int bodyIndex = cursor.getColumnIndex(DBHelper.KEY_BODY);
            int importanceIndex = cursor.getColumnIndex(DBHelper.KEY_IMPORTANCE);
            int photoIndex = cursor.getColumnIndex(DBHelper.KEY_PHOTO);
            int longitudeIndex = cursor.getColumnIndex(DBHelper.KEY_LONGITUDE);
            int lattitudeIndex = cursor.getColumnIndex(DBHelper.KEY_LATTITUDE);
            res = new Note(cursor.getString(titleIndex), cursor.getString(bodyIndex), cursor.getString(importanceIndex), cursor.getString(photoIndex)
                    , new LatLng(cursor.getDouble(lattitudeIndex), cursor.getDouble(longitudeIndex))
                    , cursor.getInt(idIndex));
        }

        cursor.close();
        dbHelper.close();

        return res;
    }

}
