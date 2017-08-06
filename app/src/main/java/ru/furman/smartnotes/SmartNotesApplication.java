package ru.furman.smartnotes;

import android.app.Application;

import com.vk.sdk.VKSdk;

/**
 * Created by Furman on 06.08.2017.
 */

public class SmartNotesApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
