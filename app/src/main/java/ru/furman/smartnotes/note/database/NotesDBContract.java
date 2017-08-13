package ru.furman.smartnotes.note.database;

import android.provider.BaseColumns;

public final class NotesDBContract {

    public NotesDBContract(){}

    public static abstract class NotesTable implements BaseColumns{
        public static final String TABLE_NAME = "notesTable";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_BODY = "body";
        public static final String COLUMN_NAME_IMPORTANCE = "importance";
        public static final String COLUMN_NAME_PHOTO = "photo";
        public static final String COLUMN_NAME_LATTITUDE = "lattitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
    }
}
