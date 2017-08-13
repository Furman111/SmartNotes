package ru.furman.smartnotes.note.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import ru.furman.smartnotes.note.Note;
import ru.furman.smartnotes.note.database.NotesContract.NotesTable;

public class NotesDB {
    private NotesDBHelper notesDbHelper;
    private SQLiteDatabase notesDB;
    private Context ctx;

    public NotesDB(Context context) {
        this.ctx = context;
    }

    public synchronized void addNote(Note note) {
        notesDbHelper = new NotesDBHelper(ctx);
        notesDB = notesDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(NotesTable.COLUMN_NAME_TITLE, note.getTitle());
        cv.put(NotesTable.COLUMN_NAME_BODY, note.getBody());
        cv.put(NotesTable.COLUMN_NAME_IMPORTANCE, note.getImportance());
        cv.put(NotesTable.COLUMN_NAME_PHOTO, note.getPhoto());
        cv.put(NotesTable.COLUMN_NAME_LATTITUDE, note.getLocation().latitude);
        cv.put(NotesTable.COLUMN_NAME_LONGITUDE, note.getLocation().longitude);

        notesDB.insert(NotesTable.TABLE_NAME, null, cv);
        notesDbHelper.close();
    }

    public synchronized List<Note> getNotes() {
        List<Note> list = new ArrayList<>();

        notesDbHelper = new NotesDBHelper(ctx);
        notesDB = notesDbHelper.getReadableDatabase();

        Cursor cursor = notesDB.query(NotesTable.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            list = new ArrayList<>();
            int idIndex = cursor.getColumnIndex(NotesTable._ID);
            int titleIndex = cursor.getColumnIndex(NotesTable.COLUMN_NAME_TITLE);
            int bodyIndex = cursor.getColumnIndex(NotesTable.COLUMN_NAME_BODY);
            int importanceIndex = cursor.getColumnIndex(NotesTable.COLUMN_NAME_IMPORTANCE);
            int photoIndex = cursor.getColumnIndex(NotesTable.COLUMN_NAME_PHOTO);
            int longitudeIndex = cursor.getColumnIndex(NotesTable.COLUMN_NAME_LONGITUDE);
            int lattitudeIndex = cursor.getColumnIndex(NotesTable.COLUMN_NAME_LATTITUDE);
            do {
                list.add(new Note(cursor.getString(titleIndex), cursor.getString(bodyIndex), cursor.getString(importanceIndex), cursor.getString(photoIndex)
                        , new LatLng(cursor.getDouble(lattitudeIndex), cursor.getDouble(longitudeIndex))
                        , cursor.getInt(idIndex)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        notesDbHelper.close();

        return list;
    }

    public synchronized void deleteNote(int id) {
        notesDbHelper = new NotesDBHelper(ctx);
        notesDB = notesDbHelper.getWritableDatabase();

        notesDB.delete(NotesTable.TABLE_NAME, NotesTable._ID +" = " + id, null);

        notesDbHelper.close();
    }

    public synchronized void editNote(int id, Note note) {
        notesDbHelper = new NotesDBHelper(ctx);
        notesDB = notesDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(NotesTable.COLUMN_NAME_TITLE, note.getTitle());
        cv.put(NotesTable.COLUMN_NAME_BODY, note.getBody());
        cv.put(NotesTable.COLUMN_NAME_IMPORTANCE, note.getImportance());
        cv.put(NotesTable.COLUMN_NAME_PHOTO, note.getPhoto());
        cv.put(NotesTable.COLUMN_NAME_LONGITUDE, note.getLocation().longitude);
        cv.put(NotesTable.COLUMN_NAME_LATTITUDE, note.getLocation().latitude);

        notesDB.update(NotesTable.TABLE_NAME, cv, NotesTable._ID +" = " + id, null);

        notesDbHelper.close();
    }

    @Nullable
    public synchronized Note getNote(long id) {

        notesDbHelper = new NotesDBHelper(ctx);
        notesDB = notesDbHelper.getReadableDatabase();

        Cursor cursor = notesDB.query(NotesTable.TABLE_NAME, null, NotesTable._ID +" = " + id, null, null, null, null);
        Note res = null;
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(NotesTable._ID);
            int titleIndex = cursor.getColumnIndex(NotesTable.COLUMN_NAME_TITLE);
            int bodyIndex = cursor.getColumnIndex(NotesTable.COLUMN_NAME_BODY);
            int importanceIndex = cursor.getColumnIndex(NotesTable.COLUMN_NAME_IMPORTANCE);
            int photoIndex = cursor.getColumnIndex(NotesTable.COLUMN_NAME_PHOTO);
            int longitudeIndex = cursor.getColumnIndex(NotesTable.COLUMN_NAME_LONGITUDE);
            int lattitudeIndex = cursor.getColumnIndex(NotesTable.COLUMN_NAME_LATTITUDE);
            res = new Note(cursor.getString(titleIndex), cursor.getString(bodyIndex), cursor.getString(importanceIndex), cursor.getString(photoIndex)
                    , new LatLng(cursor.getDouble(lattitudeIndex), cursor.getDouble(longitudeIndex))
                    , cursor.getInt(idIndex));
        }

        cursor.close();
        notesDbHelper.close();

        return res;
    }

}
