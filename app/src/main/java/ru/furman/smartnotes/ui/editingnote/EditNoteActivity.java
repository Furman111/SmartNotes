package ru.furman.smartnotes.ui.editingnote;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ru.furman.smartnotes.ui.dialog.PhotoChangeDialogFragment;
import ru.furman.smartnotes.ui.dialog.PhotoPickerDialogFragment;
import ru.furman.smartnotes.utils.BackgroundUtil;
import ru.furman.smartnotes.ui.dialog.DeleteNoteDialogFragment;
import ru.furman.smartnotes.utils.ImageFiles;
import ru.furman.smartnotes.ui.notes.NotesActivity;
import ru.furman.smartnotes.ui.MapActivity;
import ru.furman.smartnotes.note.Note;
import ru.furman.smartnotes.utils.PermissionsUtil;
import ru.furman.smartnotes.R;
import ru.furman.smartnotes.ui.ViewImageActivity;
import ru.furman.smartnotes.note.database.DB;

public class EditNoteActivity extends AppCompatActivity implements OnMapReadyCallback,
        DeleteNoteDialogFragment.DeleteNoteDialogFragmentListener,
        PhotoPickerDialogFragment.PhotoPickerDialogListener,
        PhotoChangeDialogFragment.PhotoChangeDialogFragmentListener {

    private EditText titleET, bodyET;
    private ImageView photoIV;
    private RadioGroup importanceRadioGroup;
    private View backgroundView;
    private Note note;
    private DB db;
    private MapView mapView;
    private GoogleMap map;
    private Marker marker;
    private LocationManager locationManager;
    private String oldPhoto, currentPhoto;
    private LatLng currentLocation, newLocation;

    public static final int PHOTO_PICK_REQUEST_CODE = 1;
    public static final int CAMERA_REQUEST_CODE = 2;
    public static final int CHANGE_NOTE_LOCATION_REQUEST_CODE = 3;
    public static final int SAVED_RESULT_CODE = 4;
    public static final int DELETED_RESULT_CODE = 5;

    public static final String NOTE_TAG = "note";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.edit_note);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        note = getIntent().getParcelableExtra(NotesActivity.NOTE_TAG);
        titleET = (EditText) findViewById(R.id.note_title_et);
        bodyET = (EditText) findViewById(R.id.note_body_et);
        Button saveBtn = (Button) findViewById(R.id.save_btn);
        Button cancelBtn = (Button) findViewById(R.id.cancel_btn);
        backgroundView = findViewById(R.id.background_layout);
        photoIV = (ImageView) findViewById(R.id.note_photo_iv);
        mapView = (MapView) findViewById(R.id.map_view);

        db = new DB(this);

        importanceRadioGroup = (RadioGroup) findViewById(R.id.importance_radio_group);
        importanceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.red_importance_radio_btn:
                        backgroundView.setBackground(ContextCompat.getDrawable(EditNoteActivity.this, R.drawable.red_background_gradient));
                        break;
                    case R.id.green_importance_radio_btn:
                        backgroundView.setBackground(ContextCompat.getDrawable(EditNoteActivity.this, R.drawable.green_background_gradient));
                        break;
                    case R.id.yellow_importance_radio_btn:
                        backgroundView.setBackground(ContextCompat.getDrawable(EditNoteActivity.this, R.drawable.yellow_background_gradient));
                        break;
                    case -1:
                        backgroundView.setBackgroundColor(ContextCompat.getColor(EditNoteActivity.this, R.color.zeroImportance));
                        break;
                }
            }
        });

        currentLocation = null;
        if (note != null) {
            setRadioGroupSelectionWithNote();
            if (note.getLocation().longitude != Note.NO_LONGITUDE)
                currentLocation = note.getLocation();
            else
                currentLocation = null;
        } else {
            importanceRadioGroup.clearCheck();
            currentLocation = null;
            requestLocation();
        }
        newLocation = null;
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);

        if (note != null) {
            titleET.setText(note.getTitle());
            bodyET.setText(note.getBody());
            BackgroundUtil.setBackgroundWithImportance(this, backgroundView, note);
        } else {
            getSupportActionBar().setTitle(getResources().getString(R.string.new_note));
        }

        if (note != null && !note.getPhoto().equals(Note.NO_PHOTO)) {
            oldPhoto = note.getPhoto();
            ImageLoader loader = new ImageLoader();
            loader.execute(oldPhoto);
            currentPhoto = oldPhoto;
        } else {
            currentPhoto = null;
            oldPhoto = null;
        }

        photoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPhoto == null) {
                    PhotoPickerDialogFragment dialog = new PhotoPickerDialogFragment();
                    dialog.show(getSupportFragmentManager(), null);
                } else {
                    PhotoChangeDialogFragment chDialog = new PhotoChangeDialogFragment();
                    chDialog.show(getSupportFragmentManager(), null);
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (note != null) {
                    setRadioGroupSelectionWithNote();
                    titleET.setText(note.getTitle());
                    bodyET.setText(note.getBody());
                    if (oldPhoto != null) {
                        ImageLoader loader = new ImageLoader();
                        loader.execute(oldPhoto);
                    } else
                        photoIV.setImageResource(R.mipmap.nophoto);
                    if (currentPhoto != null && !currentPhoto.equals(oldPhoto))
                        ImageFiles.deleteFile(currentPhoto);
                    currentPhoto = oldPhoto;
                } else {
                    importanceRadioGroup.clearCheck();
                    titleET.setText("");
                    bodyET.setText("");
                    photoIV.setImageResource(R.mipmap.nophoto);
                    if (currentPhoto != null)
                        ImageFiles.deleteFile(currentPhoto);
                    currentPhoto = null;
                }
                setMapLocation(currentLocation);
                newLocation = null;
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!titleET.getText().toString().isEmpty()) {
                    if (locationManager != null)
                        locationManager.removeUpdates(locationListener);
                    if (note != null) {
                        if (currentPhoto == null) {
                            if (oldPhoto != null)
                                ImageFiles.deleteFile(oldPhoto);
                            currentPhoto = Note.NO_PHOTO;
                        } else {
                            if (oldPhoto != null && !oldPhoto.equals(currentPhoto)) {
                                ImageFiles.deleteFile(oldPhoto);
                            }
                        }
                        if (newLocation != null)
                            currentLocation = newLocation;
                        else if (currentLocation == null)
                            currentLocation = new LatLng(Note.NO_LATITUDE, Note.NO_LONGITUDE);
                        db.editNote(note.getId(), new Note(titleET.getText().toString(), bodyET.getText().toString(), getImportance(), currentPhoto, currentLocation, -1));
                    } else {
                        if (currentPhoto == null) currentPhoto = Note.NO_PHOTO;
                        if (newLocation != null)
                            currentLocation = newLocation;
                        else if (currentLocation == null) {
                            currentLocation = new LatLng(Note.NO_LATITUDE, Note.NO_LONGITUDE);
                        }
                        db.addNote(new Note(titleET.getText().toString(), bodyET.getText().toString(), getImportance(), currentPhoto, currentLocation, -1));
                    }
                    setResult(SAVED_RESULT_CODE);
                    finish();
                } else
                    Toast.makeText(EditNoteActivity.this, getResources().getString(R.string.name_is_not_entered), Toast.LENGTH_SHORT).show();
            }
        });

        super.onCreate(savedInstanceState);
    }

    private String getImportance() {
        String importance = null;
        switch (importanceRadioGroup.getCheckedRadioButtonId()) {
            case R.id.red_importance_radio_btn:
                importance = Note.RED_IMPORTANCE;
                break;
            case R.id.yellow_importance_radio_btn:
                importance = Note.YELLOW_IMPORTANCE;
                break;
            case R.id.green_importance_radio_btn:
                importance = Note.GREEN_IMPORTANCE;
                break;
            case -1:
                importance = Note.NO_IMPORTANCE;
                break;
        }
        return importance;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (currentPhoto != null && !currentPhoto.equals(oldPhoto))
            ImageFiles.deleteFile(currentPhoto);
        if (locationManager != null)
            locationManager.removeUpdates(locationListener);
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        mapView.onStart();
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (locationManager != null)
                    locationManager.removeUpdates(locationListener);
                if (currentPhoto != null && !currentPhoto.equals(oldPhoto))
                    ImageFiles.deleteFile(currentPhoto);
                finish();
                break;
            case R.id.delete_note_menu_item:
                if (note != null) {
                    DeleteNoteDialogFragment deleteNoteDialogFragment = new DeleteNoteDialogFragment();
                    deleteNoteDialogFragment.show(getSupportFragmentManager(), null);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setRadioGroupSelectionWithNote() {
        switch (note.getImportance()) {
            case Note.GREEN_IMPORTANCE:
                importanceRadioGroup.check(R.id.green_importance_radio_btn);
                break;
            case Note.YELLOW_IMPORTANCE:
                importanceRadioGroup.check(R.id.yellow_importance_radio_btn);
                break;
            case Note.RED_IMPORTANCE:
                importanceRadioGroup.check(R.id.red_importance_radio_btn);
                break;
            case Note.NO_IMPORTANCE:
                importanceRadioGroup.clearCheck();
                break;
        }
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        if (currentPhoto != null && !currentPhoto.equals(oldPhoto))
            ImageFiles.deleteFile(currentPhoto);
        if (locationManager != null)
            locationManager.removeUpdates(locationListener);
        mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    ImageLoader loader = new ImageLoader();
                    loader.execute(newPhoto);
                    if (currentPhoto != null && !currentPhoto.equals(oldPhoto)) {
                        ImageFiles.deleteFile(currentPhoto);
                    }
                    currentPhoto = newPhoto;
                    newPhoto = null;
                }
                return;
            case PHOTO_PICK_REQUEST_CODE:
                if (data != null) {
                    Uri selectedPhoto = data.getData();
                    String[] filePath = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getContentResolver().query(selectedPhoto, filePath, null, null, null);
                    newPhoto=null;
                    if(cursor!=null) {
                        cursor.moveToFirst();
                        newPhoto = cursor.getString(cursor.getColumnIndex(filePath[0]));
                        cursor.close();
                    }
                    if(newPhoto==null)
                        newPhoto = selectedPhoto.getPath();
                    File file = null;
                    try {
                        file = ImageFiles.createImageFile(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (file != null) {
                        try {
                            ImageFiles.copyFile(new File(newPhoto), file);
                        } catch (IOException e) {
                            Toast.makeText(this, getResources().getString(R.string.error)+" "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        if (currentPhoto != null && !currentPhoto.equals(oldPhoto))
                            ImageFiles.deleteFile(currentPhoto);
                        currentPhoto = file.getPath();
                        newPhoto = null;
                    }
                    ImageLoader loader = new ImageLoader();
                    loader.execute(currentPhoto);
                }
                return;
            case CHANGE_NOTE_LOCATION_REQUEST_CODE:
                if (resultCode == MapActivity.RESULT_OK) {
                    newLocation = data.getParcelableExtra(MapActivity.CHOSEN_LOCATION);
                    setMapLocation(newLocation);
                } else {
                    LatLng lng;
                    if (newLocation != null)
                        lng = newLocation;
                    else
                        lng = currentLocation;
                    setMapLocation(lng);
                }
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void deleteNote() {
        if (!note.getPhoto().equals(Note.NO_PHOTO))
            ImageFiles.deleteFile(note.getPhoto());
        db.deleteNote(note.getId());
        setResult(DELETED_RESULT_CODE);
        finish();
    }

    private String newPhoto;

    @Override
    public void deletePhoto() {
        if (currentPhoto != null && !currentPhoto.equals(oldPhoto))
            ImageFiles.deleteFile(currentPhoto);
        currentPhoto = null;
        photoIV.setImageResource(R.mipmap.nophoto);
    }

    @Override
    public void showPhoto() {
        Intent photoView = new Intent(this, ViewImageActivity.class);
        photoView.putExtra(ViewImageActivity.IMAGE_SRC, this.currentPhoto);
        startActivity(photoView);
    }

    @Override
    public void changePhoto() {
        PhotoPickerDialogFragment photoPickerDialogFragment = new PhotoPickerDialogFragment();
        photoPickerDialogFragment.show(this.getSupportFragmentManager(), null);
    }

    @Override
    public void pickPhotoFromGallery() {
        if (PermissionsUtil.isStoragePermissionsGranted(this))
            startGallery();
        else
            PermissionsUtil.verifyStoragePermissions(this);
    }

    private void startGallery() {
        Intent photoPickerItent = new Intent(Intent.ACTION_PICK);
        photoPickerItent.setType("image/*");
        if (photoPickerItent.resolveActivity(getPackageManager()) != null)
            this.startActivityForResult(photoPickerItent, PHOTO_PICK_REQUEST_CODE);
        else
            Toast.makeText(this, R.string.gallery_is_not_available, Toast.LENGTH_LONG).show();
    }

    @Override
    public void pickPhotoWithCamera() {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (photoIntent.resolveActivity(this.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = ImageFiles.createImageFile(this);
                newPhoto = photoFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "ru.furman.smartnotes.fileprovider",
                        photoFile);
                photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(photoIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    this.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                this.startActivityForResult(photoIntent, CAMERA_REQUEST_CODE);
            }
        } else {
            Toast.makeText(this, R.string.camera_is_not_available, Toast.LENGTH_LONG).show();
        }
    }

    private class ImageLoader extends AsyncTask<String, Void, Bitmap> {

        private int reqHeight, reqWidth;

        @Override
        protected void onPreExecute() {
            reqHeight = getResources().getDimensionPixelSize(R.dimen.edit_note_iv_height);
            reqWidth = getResources().getDimensionPixelSize(R.dimen.edit_note_iv_max_width);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            photoIV.setImageBitmap(bitmap);
            super.onPostExecute(bitmap);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String path = params[0];
            return ImageFiles.decodeSampledBitmapFromFile(path, reqWidth, reqHeight);
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        EditNoteActivity.this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);

        if (currentLocation != null) {
            String title;
            if (note != null)
                title = note.getTitle();
            else
                title = getResources().getString(R.string.new_note);
            marker = map.addMarker(new MarkerOptions().position(currentLocation)
                    .title(title));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, MapActivity.DEFAULT_ZOOM_LITTLE_MAP));
            marker.showInfoWindow();
        } else
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(MapActivity.DEFAULT_LOCATION, MapActivity.DEFAULT_ZOOM_LITTLE_MAP));

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (locationManager != null)
                    locationManager.removeUpdates(locationListener);
                Intent intent = new Intent(EditNoteActivity.this, MapActivity.class);
                String title = null;
                LatLng latLng1;
                if (note != null)
                    title = note.getTitle();
                if (newLocation != null)
                    latLng1 = newLocation;
                else
                    latLng1 = currentLocation;
                intent.putExtra(MapActivity.NOTE_TITLE, title);
                intent.putExtra(MapActivity.NOTE_LOCATION, latLng1);
                intent.setAction(MapActivity.ACTION_CHANGE_NOTE_LOCATION);
                EditNoteActivity.this.startActivityForResult(intent, CHANGE_NOTE_LOCATION_REQUEST_CODE);
            }
        });
    }

    private void setMapLocation(LatLng location) {
        if (map != null) {
            if (location == null) {
                if (marker != null)
                    marker.remove();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(MapActivity.DEFAULT_LOCATION, MapActivity.DEFAULT_ZOOM_LITTLE_MAP));
            } else {
                String title;
                if (marker != null) {
                    title = marker.getTitle();
                    marker.remove();
                } else if (note != null)
                    title = note.getTitle();
                else
                    title = getResources().getString(R.string.new_note);
                marker = map.addMarker(new MarkerOptions().position(location)
                        .title(title));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, MapActivity.DEFAULT_ZOOM_LITTLE_MAP));
                marker.showInfoWindow();
            }
        }
    }

    public static final String LOCATION_LOG_TAG = "location_logs";

    private void requestLocation() {
        if (!PermissionsUtil.isLocationPermissionsGranted(this))
            PermissionsUtil.verifyLocationPermissions(this);
        else
            requestLocationWithLocationManager();
    }

    private void requestLocationWithLocationManager() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOCATION_LOG_TAG, "no permissions");
            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Log.d(LOCATION_LOG_TAG, "request location");
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(LOCATION_LOG_TAG, "request location gps");
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.d(LOCATION_LOG_TAG, "request location network");
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(LOCATION_LOG_TAG, "location got");
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            if (map != null)
                setMapLocation(currentLocation);
            locationManager.removeUpdates(this);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionsUtil.REQUEST_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    requestLocationWithLocationManager();
                } else
                    Toast.makeText(this, R.string.location_permissions_are_not_granted, Toast.LENGTH_LONG).show();
                break;
            case PermissionsUtil.REQUEST_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startGallery();
                } else
                    Toast.makeText(this, R.string.storage_permissions_are_not_granted, Toast.LENGTH_LONG).show();
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
