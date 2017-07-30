package ru.furman.smartnotes;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

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

    public static void copyFile(File from, File to) throws IOException {
        FileChannel source = null;
        FileChannel destination = null;

        if (!to.exists())
            to.createNewFile();

        try {
            source = new FileInputStream(from).getChannel();
            destination = new FileOutputStream(to).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}
