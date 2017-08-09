package ru.furman.smartnotes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;

import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.DefaultAudience;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
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
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Created by Furman on 06.08.2017.
 */

public abstract class SharingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        callbackManagerFB = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManagerFB, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(SharingActivity.this, SharingActivity.this.getString(R.string.error) + " " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                res.saveTokenToSharedPreferences(SharingActivity.this, SharingActivity.vkTokenKey);
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(SharingActivity.this, getResources().getString(R.string.error) + " " + error.errorMessage, Toast.LENGTH_LONG);
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }

        callbackManagerFB.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String vkTokenKey = "VK_ACCESS_TOKEN";
    private static String[] vkScope = new String[]{VKScope.WALL, VKScope.PHOTOS};


    public void shareVK(final Note note) {
        if (isConnected()) {
            VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(this, vkTokenKey);
            if ((token == null) || token.isExpired()) {
                Toast.makeText(SharingActivity.this, SharingActivity.this.getString(R.string.autorization_is_required), Toast.LENGTH_SHORT).show();
                VKSdk.login(this, vkScope);
                shareVK(note);
            } else {
                final VKAccessToken vkToken = VKAccessToken.tokenFromSharedPreferences(this, vkTokenKey);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!note.getPhoto().equals(Note.NO_PHOTO)) {
                            VKRequest request = VKApi.uploadWallPhotoRequest(new VKUploadImage(BitmapFactory.decodeFile(note.getPhoto()), //потоконебезопасно
                                    VKImageParameters.jpgImage(0.9f)), Integer.parseInt(vkToken.userId), 0);
                            request.executeWithListener(new VKRequest.VKRequestListener() {
                                @Override
                                public void onComplete(VKResponse response) {
                                    VKApiPhoto photo = ((VKPhotoArray) response.parsedModel).get(0);
                                    VKAttachments att = new VKAttachments(photo);
                                    makePost(att, note);
                                }

                                @Override
                                public void onError(VKError error) {
                                    makePost(null, note);
                                }
                            });
                        } else
                            makePost(null, note);
                    }
                }).start();
            }
        } else
            noInternetConnectionToast();
    }


    protected void makePost(VKAttachments att, Note note) {
        VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(this, vkTokenKey);
        VKParameters parameters = new VKParameters();

        parameters.put(VKApiConst.OWNER_ID, token.userId);

        if (att != null)
            parameters.put(VKApiConst.ATTACHMENTS, att);

        parameters.put(VKApiConst.MESSAGE, this.getResources().getString(R.string.note_share) + " " +
                note.getTitle() + "\n\n" +
                note.getBody()
                + "\n\n");

        if (note.getLocation().latitude != Note.NO_LATITUDE) {
            parameters.put(VKApiConst.LAT, note.getLocation().latitude);
            parameters.put(VKApiConst.LONG, note.getLocation().longitude);
        }

        VKRequest post = VKApi.wall().post(parameters);
        post.setModelClass(VKWallPostResult.class);
        final String title = note.getTitle();
        post.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                Toast.makeText(SharingActivity.this, SharingActivity.this.getResources().getString(R.string.note) + " " + title + " " + SharingActivity.this.getResources().getString(R.string.is_published), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(SharingActivity.this, SharingActivity.this.getResources().getString(R.string.error) + " " + error.errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    CallbackManager callbackManagerFB;
    public static final String FB_LOG_TAG = "fb_log_tag";

    public void shareFB(final Note note) {
        if (isConnected()) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            if (accessToken == null || accessToken.isExpired()) {
                Toast.makeText(SharingActivity.this, SharingActivity.this.getString(R.string.autorization_is_required), Toast.LENGTH_SHORT).show();
                LoginManager.getInstance().setDefaultAudience(DefaultAudience.EVERYONE);
                LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!note.getPhoto().equals(Note.NO_PHOTO)) {
                            Bundle bundle = new Bundle();
                            Bitmap bitmap = BitmapFactory.decodeFile(note.getPhoto());
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
                            bundle.putByteArray("picture", byteArrayOutputStream.toByteArray());
                            bundle.putString("caption", note.getTitle() + "\n\n" + note.getBody());
                            new GraphRequest(
                                    AccessToken.getCurrentAccessToken(),
                                    "me/photos",
                                    bundle,
                                    HttpMethod.POST,
                                    new GraphRequest.Callback() {
                                        @Override
                                        public void onCompleted(GraphResponse response) {
                                            if (response.getError() != null)
                                                Log.d(FB_LOG_TAG, response.getError().getErrorMessage());
                                            else
                                                Toast.makeText(SharingActivity.this, SharingActivity.this.getString(R.string.note) + " " + note.getTitle() + " " + SharingActivity.this.getString(R.string.is_published), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            ).executeAsync();
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString("message",note.getTitle()+"\n\n"+note.getBody());
                            GraphRequest request = new GraphRequest(
                                    AccessToken.getCurrentAccessToken(),
                                    "me/feed",
                                    bundle,
                                    HttpMethod.POST,
                                    new GraphRequest.Callback() {
                                        @Override
                                        public void onCompleted(GraphResponse response) {
                                            if (response.getError() == null)
                                                Toast.makeText(SharingActivity.this, SharingActivity.this.getString(R.string.note) + " " + note.getTitle() + " " + SharingActivity.this.getString(R.string.is_published), Toast.LENGTH_SHORT).show();
                                            else {
                                                Log.d(FB_LOG_TAG, response.getError().getErrorMessage());
                                                Toast.makeText(SharingActivity.this, SharingActivity.this.getString(R.string.error) + " " + response.getError().getErrorMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                            );
                            request.executeAsync();
                        }

                    }
                }).start();
            }
        } else
            noInternetConnectionToast();
    }

    public void shareTwitter(Note note) {

    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting())
            return true;
        return false;
    }

    public void noInternetConnectionToast() {
        Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
    }

}
