package ru.furman.smartnotes;

import android.app.Application;

import com.twitter.sdk.android.core.Twitter;
import com.vk.sdk.VKSdk;

public class SmartNotesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
        Twitter.initialize(this);
    }

}
