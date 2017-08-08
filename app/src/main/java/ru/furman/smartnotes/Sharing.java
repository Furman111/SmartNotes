package ru.furman.smartnotes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKWallPostResult;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import java.util.Arrays;

/**
 * Created by Furman on 06.08.2017.
 */

public class Sharing extends AppCompatActivity implements ShareDialogFragment.ShareDialogListener {

    private Activity activity;
    private Note note;

    public Sharing(final Activity activity, Note note) {
        this.activity = activity;
        this.note = note;
    }


    public static String vkTokenKey = "VK_ACCESS_TOKEN";
    private static String[] vkScope = new String[]{VKScope.WALL, VKScope.PHOTOS};

    @Override
    public void shareVK() {
        if (isConnected()) {
            final VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(activity, vkTokenKey);
            if ((token == null) || token.isExpired()) {
                VKSdk.login(activity, vkScope);
                shareVK();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!note.getPhoto().equals(Note.NO_PHOTO)) {
                            VKRequest request = VKApi.uploadWallPhotoRequest(new VKUploadImage(BitmapFactory.decodeFile(note.getPhoto()),
                                    VKImageParameters.jpgImage(0.9f)), Integer.parseInt(token.userId), 0);
                            request.executeWithListener(new VKRequest.VKRequestListener() {
                                @Override
                                public void onComplete(VKResponse response) {
                                    VKApiPhoto photo = ((VKPhotoArray) response.parsedModel).get(0);
                                    VKAttachments att = new VKAttachments(photo);
                                    makePost(att);
                                }

                                @Override
                                public void onError(VKError error) {
                                    Toast.makeText(activity, activity.getResources().getString(R.string.error) + " " + error.errorMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                        } else
                            makePost(null);
                    }
                }).start();
            }
        } else
            Toast.makeText(activity, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
    }


    void makePost(VKAttachments att) {

        VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(activity, vkTokenKey);
        VKParameters parameters = new VKParameters();

        parameters.put(VKApiConst.OWNER_ID, token.userId);

        if (att != null)
            parameters.put(VKApiConst.ATTACHMENTS, att);

        parameters.put(VKApiConst.MESSAGE, activity.getResources().getString(R.string.note_share) + " " +
                note.getTitle() + "\n\n" +
                note.getBody()
                + "\n\n" +
                activity.getResources().getString(R.string.published_with_smart_notes));

        if (note.getLocation().latitude != Note.NO_LATITUDE) {
            parameters.put(VKApiConst.LAT, note.getLocation().latitude);
            parameters.put(VKApiConst.LONG, note.getLocation().longitude);
        }

        VKRequest post = VKApi.wall().post(parameters);
        post.setModelClass(VKWallPostResult.class);
        post.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                Toast.makeText(activity, activity.getResources().getString(R.string.note) + " " + note.getTitle() + " " + activity.getResources().getString(R.string.is_published), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(activity, activity.getResources().getString(R.string.error) + " " + error.errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void shareFB() {

    }

    @Override
    public void shareTwitter() {

    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting())
            return true;
        return false;
    }

}
