package ru.furman.smartnotes;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;

/**
 * Created by Furman on 27.07.2017.
 */

public class Util {

    public static void setBackgroundWithImportance(Context ctx,View view,Note note){
        switch (note.getImportance()){
            case Note.GREEN_IMPORTANCE:
                view.setBackgroundColor(ContextCompat.getColor(ctx,R.color.greenImportance));
                break;
            case Note.RED_IMPORTANCE:
                view.setBackgroundColor(ContextCompat.getColor(ctx,R.color.redImportance));
                break;
            case Note.YELLOW_IMPORTANCE:
                view.setBackgroundColor(ContextCompat.getColor(ctx,R.color.yellowImportance));
                break;
            case Note.NO_IMPORTANCE:
                view.setBackgroundColor(ContextCompat.getColor(ctx,R.color.zeroImportance));
                break;
        }
    }
}
