package ru.furman.smartnotes.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;

import ru.furman.smartnotes.note.Note;
import ru.furman.smartnotes.R;

/**
 * Created by Furman on 11.08.2017.
 */

public abstract class BackgroundUtil {

    public static void setBackgroundWithImportance(Context ctx, View backgroundView, Note note) {
        switch (note.getImportance()) {
            case Note.GREEN_IMPORTANCE:
                backgroundView.setBackground(ContextCompat.getDrawable(ctx, R.drawable.green_background_gradient));
                break;
            case Note.RED_IMPORTANCE:
                backgroundView.setBackground(ContextCompat.getDrawable(ctx, R.drawable.red_background_gradient));
                break;
            case Note.YELLOW_IMPORTANCE:
                backgroundView.setBackground(ContextCompat.getDrawable(ctx, R.drawable.yellow_background_gradient));
                break;
            case Note.NO_IMPORTANCE:
                backgroundView.setBackgroundColor(ContextCompat.getColor(ctx, R.color.zeroImportance));
                break;
        }
    }

}
