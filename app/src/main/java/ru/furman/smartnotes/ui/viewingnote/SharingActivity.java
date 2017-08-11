package ru.furman.smartnotes.ui.viewingnote;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.MediaService;
import com.twitter.sdk.android.core.services.StatusesService;

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
import java.io.IOException;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import ru.furman.smartnotes.utils.ImageFiles;
import ru.furman.smartnotes.note.Note;
import ru.furman.smartnotes.R;

public abstract class SharingActivity extends AppCompatActivity {

    private Note noteToShare;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        callbackManagerFB = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManagerFB, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                shareFB(noteToShare);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                showErrorInformationToast(error.getMessage());
            }
        });

        twitterAuthClient = new TwitterAuthClient();

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                res.saveTokenToSharedPreferences(SharingActivity.this, SharingActivity.vkTokenKey);
                shareVK(noteToShare);
            }

            @Override
            public void onError(VKError error) {
                showErrorInformationToast(error.errorMessage);
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }

        callbackManagerFB.onActivityResult(requestCode, resultCode, data);
        twitterAuthClient.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static final String vkTokenKey = "VK_ACCESS_TOKEN";
    public static final String[] vkScope = new String[]{VKScope.WALL, VKScope.PHOTOS};
    public static final String VK_LOG_TAG = "vk_logs";


    public void shareVK(final Note note) {
        noteToShare = note;
        if (isConnected()) {
            VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(this, vkTokenKey);
            if ((token == null) || token.isExpired()) {
                showAutorizationIsRequiredToast();
                VKSdk.login(this, vkScope);
            } else {
                final VKAccessToken vkToken = VKAccessToken.tokenFromSharedPreferences(this, vkTokenKey);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!note.getPhoto().equals(Note.NO_PHOTO)) {
                            VKRequest request = VKApi.uploadWallPhotoRequest(new VKUploadImage(ImageFiles.decodeFile(note.getPhoto()),
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
                                    Log.d(VK_LOG_TAG, error.errorMessage);
                                    makePost(null, note);
                                }
                            });
                        } else
                            makePost(null, note);
                    }
                }).start();
            }
        } else
            showNoInternetConnectionToast();
    }


    private void makePost(VKAttachments att, Note note) {
        VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(this, vkTokenKey);
        VKParameters parameters = new VKParameters();

        parameters.put(VKApiConst.OWNER_ID, token.userId);

        if (att != null)
            parameters.put(VKApiConst.ATTACHMENTS, att);

        parameters.put(VKApiConst.MESSAGE, note.getTitle() +
                "\n\n" +
                note.getBody());

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
                showNoteIsPublishedToast(title);
            }

            @Override
            public void onError(VKError error) {
                Log.d(VK_LOG_TAG, error.errorMessage);
                showErrorInformationToast(error.errorMessage);
            }
        });
    }

    private CallbackManager callbackManagerFB;
    public static final String FB_LOG_TAG = "fb_log_tag";

    public void shareFB(final Note note) {
        noteToShare = note;
        if (isConnected()) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            if (accessToken == null || accessToken.isExpired()) {
                showAutorizationIsRequiredToast();
                LoginManager.getInstance().setDefaultAudience(DefaultAudience.EVERYONE);
                LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!note.getPhoto().equals(Note.NO_PHOTO)) {
                            Bundle bundle = new Bundle();
                            Bitmap bitmap = ImageFiles.decodeFile(note.getPhoto());
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
                                                showNoteIsPublishedToast(note.getTitle());
                                        }
                                    }
                            ).executeAsync();
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString("message", note.getTitle() + "\n\n" + note.getBody());
                            GraphRequest request = new GraphRequest(
                                    AccessToken.getCurrentAccessToken(),
                                    "me/feed",
                                    bundle,
                                    HttpMethod.POST,
                                    new GraphRequest.Callback() {
                                        @Override
                                        public void onCompleted(GraphResponse response) {
                                            if (response.getError() == null)
                                                showNoteIsPublishedToast(note.getTitle());
                                            else {
                                                Log.d(FB_LOG_TAG, response.getError().getErrorMessage());
                                                showErrorInformationToast(response.getError().getErrorMessage());
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
            showNoInternetConnectionToast();
    }

    private TwitterAuthClient twitterAuthClient;
    public static final String TWITTER_LOG_TAG = "twitter_logs";

    public void shareTwitter(final Note note) {
        noteToShare = note;
        if (isConnected()) {
            TwitterAuthToken token = null;
            TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
            if (session != null)
                token = session.getAuthToken();
            if (token == null || token.isExpired()) {
                showAutorizationIsRequiredToast();
                twitterAuthClient.authorize(this, new Callback<TwitterSession>() {

                    @Override
                    public void success(Result<TwitterSession> result) {
                        shareTwitter(noteToShare);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d(TWITTER_LOG_TAG, exception.getMessage());
                        showErrorInformationToast(exception.getMessage());
                    }

                });
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final StringBuilder sb = new StringBuilder(note.getTitle());
                        sb.append("\n");
                        sb.append(note.getBody());
                        if (sb.length() > 140) {
                            sb.setLength(137);
                            sb.append("...");
                        }
                        final Double lattitude, longitude;
                        if (note.getLocation().latitude == Note.NO_LATITUDE) {
                            lattitude = note.getLocation().latitude;
                            longitude = note.getLocation().longitude;
                        } else {
                            lattitude = null;
                            longitude = null;
                        }
                        if (!note.getPhoto().equals(Note.NO_PHOTO)) {
                            Call<Media> call;
                            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(TwitterCore.getInstance().getSessionManager().getActiveSession());
                            MediaService mediaService = twitterApiClient.getMediaService();
                            call = mediaService.upload(new RequestBody() {
                                                           @Override
                                                           public MediaType contentType() {
                                                               return MediaType.parse(note.getPhoto());
                                                           }

                                                           @Override
                                                           public void writeTo(BufferedSink sink) throws IOException {
                                                               Bitmap bitmap = ImageFiles.decodeFile(note.getPhoto());
                                                               ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                                               bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
                                                               sink.write(byteArrayOutputStream.toByteArray());
                                                           }
                                                       },
                                    null,
                                    null);
                            call.enqueue(new Callback<Media>() {
                                @Override
                                public void success(Result<Media> result) {
                                    tweet(sb.toString(), lattitude, longitude, result.data.mediaIdString, note);
                                }

                                @Override
                                public void failure(TwitterException exception) {
                                    Log.d(TWITTER_LOG_TAG, exception.getMessage());
                                    tweet(sb.toString(), lattitude, longitude, null, note);
                                }
                            });
                        } else {
                            tweet(sb.toString(), lattitude, longitude, null, note);
                        }
                    }
                }).start();
            }
        } else
            showNoInternetConnectionToast();
    }

    private void tweet(String message, Double lattitude, Double longitude, String mediaIds, final Note note) {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(TwitterCore.getInstance().getSessionManager().getActiveSession());
        final StatusesService statusesService = twitterApiClient.getStatusesService();

        Call<Tweet> call = statusesService.update(
                message,
                null,
                null,
                lattitude,
                longitude,
                null,
                true,
                null,
                mediaIds
        );
        call.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                showNoteIsPublishedToast(note.getTitle());
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TWITTER_LOG_TAG, exception.getMessage());
                showErrorInformationToast(exception.getMessage());
            }
        });
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting())
            return true;
        return false;
    }

    private void showNoInternetConnectionToast() {
        Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
    }

    private void showAutorizationIsRequiredToast() {
        Toast.makeText(SharingActivity.this, SharingActivity.this.getString(R.string.autorization_is_required), Toast.LENGTH_SHORT).show();
    }

    private void showNoteIsPublishedToast(String title) {
        Toast.makeText(this, getString(R.string.note) + " " + title + " " + getString(R.string.is_published), Toast.LENGTH_SHORT).show();
    }

    private void showErrorInformationToast(String errorMessage) {
        Toast.makeText(this, getString(R.string.error) + " " + errorMessage, Toast.LENGTH_SHORT).show();
    }

}
