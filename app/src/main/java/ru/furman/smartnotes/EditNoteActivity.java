package ru.furman.smartnotes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.furman.smartnotes.database.DB;

/**
 * Created by Furman on 26.07.2017.
 */

public class EditNoteActivity extends AppCompatActivity implements OnMapReadyCallback, DeleteNoteDialogFragment.NoticeDialogListener {

    private EditText title, body;
    private ImageView photoIV;
    private Spinner importanceSpinner;
    private View background;
    private Note note;
    private DB db;
    private MapView mapView;
    private GoogleMap googleMap;
    private Marker marker;
    private LocationManager locationManager;

    private String oldPhoto, currentPhoto, newPhoto;

    private LatLng currentLoc, newLoc;

    public static final int PHOTO_PICK_REQUEST_CODE = 3;
    public static final int CAMERA_REQUEST_CODE = 4;
    public static final int CHANGE_NOTE_LOCATION_REQUEST_CODE = 5;

    public static final int SAVED_RESULT_CODE = 1;
    public static final int DELETED_RESULT_CODE = 2;

    public static final String NOTE_TAG = "note";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.edit_note);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        note = getIntent().getParcelableExtra(MainActivity.NOTE_TAG);
        title = (EditText) findViewById(R.id.note_title_edit);
        body = (EditText) findViewById(R.id.note_body_edit);
        Button saveBtn = (Button) findViewById(R.id.save_btn);
        Button cancelBtn = (Button) findViewById(R.id.cancel_btn);
        background = findViewById(R.id.importance_background);
        photoIV = (ImageView) findViewById(R.id.note_mageIV);
        mapView = (MapView) findViewById(R.id.map);

        db = new DB(this);

        importanceSpinner = (Spinner) findViewById(R.id.importance_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.spinner_array));
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        importanceSpinner.setAdapter(adapter);

        currentLoc = null;
        if (note != null) {
            setDefaultSelection();
            if (note.getLocation().longitude != Note.NO_LONGITUDE)
                currentLoc = note.getLocation();
            else
                currentLoc = null;
        } else {
            importanceSpinner.setSelection(3);
            currentLoc = null;
            requestLocation();
        }
        newLoc = null;

        importanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        background.setBackgroundColor(ContextCompat.getColor(EditNoteActivity.this, R.color.redImportance));
                        view.setBackgroundColor(ContextCompat.getColor(EditNoteActivity.this, R.color.redImportance));
                        break;
                    case 1:
                        background.setBackgroundColor(ContextCompat.getColor(EditNoteActivity.this, R.color.yellowImportance));
                        view.setBackgroundColor(ContextCompat.getColor(EditNoteActivity.this, R.color.yellowImportance));
                        break;
                    case 2:
                        background.setBackgroundColor(ContextCompat.getColor(EditNoteActivity.this, R.color.greenImportance));
                        view.setBackgroundColor(ContextCompat.getColor(EditNoteActivity.this, R.color.greenImportance));
                        break;
                    case 3:
                        background.setBackgroundColor(ContextCompat.getColor(EditNoteActivity.this, R.color.zeroImportance));
                        view.setBackgroundColor(ContextCompat.getColor(EditNoteActivity.this, R.color.zeroImportance));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (note != null) {
                    setDefaultSelection();
                    title.setText(note.getTitle());
                    body.setText(note.getBody());
                    if (oldPhoto != null) {
                        ImageLoader loader = new ImageLoader();
                        loader.execute(oldPhoto);
                    } else
                        photoIV.setImageResource(R.mipmap.nophoto);
                    if (currentPhoto != null && !currentPhoto.equals(oldPhoto))
                        Util.deleteFile(currentPhoto);
                    currentPhoto = oldPhoto;
                } else {
                    importanceSpinner.setSelection(3);
                    title.setText("");
                    body.setText("");
                    photoIV.setImageResource(R.mipmap.nophoto);
                    if (currentPhoto != null)
                        Util.deleteFile(currentPhoto);
                    currentPhoto = null;
                }
                setMapLocation(currentLoc);
                newLoc = null;
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!title.getText().toString().isEmpty()) {
                    if (locationManager != null)
                        locationManager.removeUpdates(locationListener);
                    if (note != null) {
                        String importance = null;
                        switch (importanceSpinner.getSelectedItemPosition()) {
                            case 0:
                                importance = Note.RED_IMPORTANCE;
                                break;
                            case 1:
                                importance = Note.YELLOW_IMPORTANCE;
                                break;
                            case 2:
                                importance = Note.GREEN_IMPORTANCE;
                                break;
                            case 3:
                                importance = Note.NO_IMPORTANCE;
                                break;
                        }
                        if (currentPhoto == null) {
                            if (oldPhoto != null)
                                Util.deleteFile(oldPhoto);
                            currentPhoto = Note.NO_PHOTO;
                        } else {
                            if (oldPhoto != null && !oldPhoto.equals(currentPhoto)) {
                                Util.deleteFile(oldPhoto);
                            }
                        }
                        if (newLoc != null)
                            currentLoc = newLoc;
                        else if (currentLoc == null) {
                            currentLoc = new LatLng(Note.NO_LATITUDE, Note.NO_LONGITUDE);
                        }
                        db.editNote(note.getId(), new Note(title.getText().toString(), body.getText().toString(), importance, currentPhoto, currentLoc, -1));
                    } else {
                        String importance = null;
                        switch (importanceSpinner.getSelectedItemPosition()) {
                            case 0:
                                importance = Note.RED_IMPORTANCE;
                                break;
                            case 1:
                                importance = Note.YELLOW_IMPORTANCE;
                                break;
                            case 2:
                                importance = Note.GREEN_IMPORTANCE;
                                break;
                            case 3:
                                importance = Note.NO_IMPORTANCE;
                                break;
                        }
                        if (currentPhoto == null) currentPhoto = Note.NO_PHOTO;
                        if (newLoc != null)
                            currentLoc = newLoc;
                        else if (currentLoc == null) {
                            currentLoc = new LatLng(Note.NO_LATITUDE, Note.NO_LONGITUDE);
                        }
                        db.addNote(new Note(title.getText().toString(), body.getText().toString(), importance, currentPhoto, currentLoc, -1));
                    }
                    setResult(SAVED_RESULT_CODE);
                    finish();
                } else
                    Toast.makeText(EditNoteActivity.this, getResources().getString(R.string.name_is_not_entered), Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = getIntent();
        if (note != null) {
            title.setText(note.getTitle());
            body.setText(note.getBody());
            Util.setBackgroundWithImportance(this, background, note);
        } else {
            getSupportActionBar().setTitle(getResources().getString(R.string.new_note));
        }

        if (note != null && !note.getPhoto().equals(Note.NO_PHOTO)) {
            oldPhoto = note.getPhoto();
            ImageLoader loader = new ImageLoader();
            loader.execute(oldPhoto);
            currentPhoto = note.getPhoto();
        } else {
            currentPhoto = null;
            oldPhoto = null;
        }

        photoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPhoto == null) {
                    PhotoPickerDialogFragment dialog = new PhotoPickerDialogFragment();
                    dialog.show(getFragmentManager(), null);
                } else {
                    PhotoChangeDialogFragment chDialog = new PhotoChangeDialogFragment();
                    chDialog.show(getFragmentManager(), null);
                }
            }
        });

        mapView.getMapAsync(this);

        mapView.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (currentPhoto != null && !currentPhoto.equals(oldPhoto))
            Util.deleteFile(currentPhoto);
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
                    Util.deleteFile(currentPhoto);
                finish();
                break;
            case R.id.delete_note_edit_menu:
                if (note != null) {
                    DeleteNoteDialogFragment deleteNoteDialogFragment = new DeleteNoteDialogFragment();
                    deleteNoteDialogFragment.show(getFragmentManager(), null);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setDefaultSelection() {
        switch (note.getImportance()) {
            case Note.GREEN_IMPORTANCE:
                importanceSpinner.setSelection(2);
                break;
            case Note.YELLOW_IMPORTANCE:
                importanceSpinner.setSelection(1);
                break;
            case Note.RED_IMPORTANCE:
                importanceSpinner.setSelection(0);
                break;
            case Note.NO_IMPORTANCE:
                importanceSpinner.setSelection(3);
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
                        Util.deleteFile(currentPhoto);
                    }
                    currentPhoto = newPhoto;
                }
                return;
            case PHOTO_PICK_REQUEST_CODE:
                if (data != null) {
                    Uri selectedPhoto = data.getData();
                    File file = null;
                    try {
                        file = PhotoPickerDialogFragment.createImageFile(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (file != null) {
                        newPhoto = selectedPhoto.getPath();
                        try {
                            Util.copyFile(new File(newPhoto), file);
                        } catch (IOException e) {
                            Toast.makeText(this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                        if (currentPhoto != null && !currentPhoto.equals(oldPhoto))
                            Util.deleteFile(currentPhoto);
                        currentPhoto = file.getPath();
                    }
                    ImageLoader loader = new ImageLoader();
                    loader.execute(currentPhoto);
                }
                return;
            case CHANGE_NOTE_LOCATION_REQUEST_CODE:
                if (resultCode == MapActivity.RESULT_OK) {
                    newLoc = data.getParcelableExtra(MapActivity.CHOSEN_LOCATION);
                    setMapLocation(newLoc);
                } else {
                    LatLng lng;
                    if (newLoc != null)
                        lng = newLoc;
                    else
                        lng = currentLoc;
                    setMapLocation(lng);
                }
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onDeletePhoto() {
        if (currentPhoto != null && !currentPhoto.equals(oldPhoto))
            Util.deleteFile(currentPhoto);
        currentPhoto = null;
        photoIV.setImageResource(R.mipmap.nophoto);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        EditNoteActivity.this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        if (currentLoc != null) {
            String title;
            if (note != null)
                title = note.getTitle();
            else
                title = getResources().getString(R.string.new_note);
            marker = googleMap.addMarker(new MarkerOptions().position(currentLoc)
                    .title(title));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, MapActivity.DEFAULT_ZOOM_LITTLE_MAP));
            marker.showInfoWindow();
        } else
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapActivity.DEFAULT_LOCATION, MapActivity.DEFAULT_ZOOM_LITTLE_MAP));

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (locationManager != null)
                    locationManager.removeUpdates(locationListener);
                Intent intent = new Intent(EditNoteActivity.this, MapActivity.class);
                String title = null;
                LatLng latLng1;
                if (note != null)
                    title = note.getTitle();
                if (newLoc != null)
                    latLng1 = newLoc;
                else
                    latLng1 = currentLoc;
                intent.putExtra(MapActivity.NOTE_TITLE, title);
                intent.putExtra(MapActivity.NOTE_LOCATION, latLng1);
                intent.setAction(MapActivity.ACTION_CHANGE_NOTE_LOCATION);
                EditNoteActivity.this.startActivityForResult(intent, CHANGE_NOTE_LOCATION_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (!note.getPhoto().equals(Note.NO_PHOTO))
            Util.deleteFile(note.getPhoto());
        db.deleteNote(note.getId());
        setResult(DELETED_RESULT_CODE);
        finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    public static class PhotoPickerDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.add_photo))
                    .setItems(R.array.photo_picker_array, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 1:
                                    Util.verifyStoragePermissions(getActivity());
                                    Intent photoPickerItent = new Intent(Intent.ACTION_PICK);
                                    photoPickerItent.setType("image/*");
                                    getActivity().startActivityForResult(photoPickerItent, PHOTO_PICK_REQUEST_CODE);
                                    return;
                                case 0:
                                    Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    if (photoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                        File photoFile = null;
                                        try {
                                            photoFile = createImageFile(getActivity());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        if (photoFile != null) {
                                            Uri photoURI = FileProvider.getUriForFile(getActivity(),
                                                    "ru.furman.smartnotes.fileprovider",
                                                    photoFile);
                                            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                            getActivity().startActivityForResult(photoIntent, CAMERA_REQUEST_CODE);
                                        }
                                    }
                            }
                        }
                    });

            return builder.create();
        }


        public static File createImageFile(Context ctx) throws IOException {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = ctx.getFilesDir();
            File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            ((EditNoteActivity) ctx).newPhoto = image.getAbsolutePath();
            return image;
        }
    }


    public static class PhotoChangeDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.photo))
                    .setItems(R.array.photo_array, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    Intent photoView = new Intent(getActivity(), ViewImageActivity.class);
                                    photoView.putExtra(ViewImageActivity.IMAGE_SRC, ((EditNoteActivity) getActivity()).currentPhoto);
                                    startActivity(photoView);
                                    return;
                                case 1:
                                    PhotoPickerDialogFragment mdialog = new PhotoPickerDialogFragment();
                                    mdialog.show(getActivity().getFragmentManager(), null);
                                    return;
                                case 2:
                                    ((EditNoteActivity) getActivity()).onDeletePhoto();
                                    ((EditNoteActivity) getActivity()).photoIV.setImageResource(R.mipmap.nophoto);
                            }
                        }
                    });

            return builder.create();
        }
    }


    private void setMapLocation(LatLng location) {
        if (location == null) {
            if (marker != null)
                marker.remove();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapActivity.DEFAULT_LOCATION, MapActivity.DEFAULT_ZOOM_LITTLE_MAP));
        } else {
            String title;
            if (marker != null) {
                title = marker.getTitle();
                marker.remove();
            } else if (note != null)
                title = note.getTitle();
            else
                title = getResources().getString(R.string.new_note);
            marker = googleMap.addMarker(new MarkerOptions().position(location)
                    .title(title));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, MapActivity.DEFAULT_ZOOM_LITTLE_MAP));
            marker.showInfoWindow();
        }
    }

    private class ImageLoader extends AsyncTask<String, Void, Bitmap> {

        private int reqHeight, reqWidth;

        @Override
        protected void onPreExecute() {
            reqHeight = getResources().getDimensionPixelSize(R.dimen.edit_note_iv_height);
            reqWidth = getResources().getDisplayMetrics().widthPixels;
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
            return ImageSampler.decodeSampledBitmapFromFile(path, reqWidth, reqHeight);
        }
    }

    public void requestLocation() {
        Util.verifyLocationPermissions(this);

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Location", "no permissions");
            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Log.d("Location", "request location");
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("Location", "request location gps");
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.d("Location", "request location network");
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Util.REQUEST_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("Location", "location got");
            currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            if (googleMap != null)
                setMapLocation(currentLoc);
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

}
