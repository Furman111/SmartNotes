package ru.furman.smartnotes;

/**
 * Created by Furman on 26.07.2017.
 */

public class Note {
    private String title;
    private String body;
    private String importance;
    private int id;

    public static final String RED_IMPORTANCE = "red";
    public static final String YELLOW_IMPORTANCE = "yellow";
    public static final String GREEN_IMPORTANCE = "green";
    public static final String NO_IMPORTANCE = "null";

    public Note(String title,String body,String importance, int id){
        this.title = title;
        this.body = body;
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

    public int getId(){
        return id;
    }
}
