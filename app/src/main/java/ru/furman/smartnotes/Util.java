package ru.furman.smartnotes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
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

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

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

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
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
