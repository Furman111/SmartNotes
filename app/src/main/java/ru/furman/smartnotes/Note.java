package ru.furman.smartnotes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Furman on 26.07.2017.
 */

public class Note implements Parcelable {
    private String title;
    private String body;
    private String importance;
    private String photo;
    private int id;

    public static final String RED_IMPORTANCE = "red";
    public static final String YELLOW_IMPORTANCE = "yellow";
    public static final String GREEN_IMPORTANCE = "green";
    public static final String NO_IMPORTANCE = "null";


    public static final String NO_PHOTO = "noPhoto";

    public Note(String title,String body,String importance,String photo, int id){
        this.title = title;
        this.body = body;
        this.photo = photo;
        this.importance = importance;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getImportance() {
        return importance;
    }

    public String getPhoto(){
        return photo;
    }

    public int getId(){
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(body);
        dest.writeString(importance);
        dest.writeString(photo);
        dest.writeInt(id);
    }

    public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>(){
        public Note createFromParcel(Parcel in){
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public Note(Parcel in){
        title = in.readString();
        body = in.readString();
        importance = in.readString();
        photo = in.readString();
        id = in.readInt();
    }
}
