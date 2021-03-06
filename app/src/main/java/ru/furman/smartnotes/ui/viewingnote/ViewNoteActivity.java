package ru.furman.smartnotes.ui.viewingnote;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ru.furman.smartnotes.ui.ViewImageActivity;
import ru.furman.smartnotes.utils.BackgroundUtil;
import ru.furman.smartnotes.ui.dialog.DeleteNoteDialogFragment;
import ru.furman.smartnotes.utils.PermissionsUtil;
import ru.furman.smartnotes.ui.editingnote.EditNoteActivity;
import ru.furman.smartnotes.utils.ImageFiles;
import ru.furman.smartnotes.ui.MapActivity;
import ru.furman.smartnotes.note.Note;
import ru.furman.smartnotes.R;
import ru.furman.smartnotes.note.database.NotesDB;

public class ViewNoteActivity extends SharingActivity implements OnMapReadyCallback, DeleteNoteDialogFragment.DeleteNoteDialogFragmentListener {

    private Note note;
    private NotesDB notesDb;
    private TextView bodyTV, titleTV;
    private View backgroundView;
    private FilePickerDialog filePickerDialog;
    private ImageView noteIV;
    private MapView mapView;
    private GoogleMap map;
    private Marker marker;
    private LatLng location;

    public static final int EDIT_REQUEST_CODE = 1;
    public static final int SHOW_NOTE_ON_MAP_REQUEST_CODE = 2;

    public static final String NOTE_TAG = "note";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_note_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        notesDb = new NotesDB(this);

        Intent intent = getIntent();
        note = intent.getParcelableExtra(NOTE_TAG);

        titleTV = (TextView) findViewById(R.id.note_title_tv);
        titleTV.setText(note.getTitle());

        bodyTV = (TextView) findViewById(R.id.note_body_tv);
        bodyTV.setText(note.getBody());

        noteIV = (ImageView) findViewById(R.id.note_photo_iv);
        if (!note.getPhoto().equals(Note.NO_PHOTO)) {
            ImageLoader loader = new ImageLoader();
            loader.execute(note.getPhoto());
            noteIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent photoView = new Intent(ViewNoteActivity.this, ViewImageActivity.class);
                    photoView.putExtra(ViewImageActivity.IMAGE_SRC, note.getPhoto());
                    ViewNoteActivity.this.startActivity(photoView);
                }
            });
        }

        onCreateDialogFilePicker();

        Button importBtn = (Button) findViewById(R.id.export_to_txt_btn);
        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionsUtil.isStoragePermissionsGranted(ViewNoteActivity.this))
                    filePickerDialog.show();
                else
                    PermissionsUtil.verifyStoragePermissions(ViewNoteActivity.this);
            }
        });

        backgroundView = findViewById(R.id.background_layout);
        BackgroundUtil.setBackgroundWithNoteImportance(this, backgroundView, note);

        mapView = (MapView) findViewById(R.id.map_view);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);
    }

    private void onCreateDialogFilePicker() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        filePickerDialog = new FilePickerDialog(this, properties);
        filePickerDialog.setTitle(getString(R.string.to_choose_directory));
        filePickerDialog.setPositiveBtnName(getString(R.string.to_choose));
        filePickerDialog.setNegativeBtnName(getString(R.string.cancel));

        filePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null) {
                    if (files[0].contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                        if (!Environment.getExternalStorageState().equals(
                                Environment.MEDIA_MOUNTED)) {
                            Toast.makeText(ViewNoteActivity.this, getString(R.string.external_storage_is_not_available), Toast.LENGTH_SHORT).show();
                        } else
                            writeNoteToFile(files[0]);
                    } else
                        writeNoteToFile(files[0]);
                }
            }
        });
    }

    private void writeNoteToFile(String filePath) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(filePath + "/" + note.getTitle() + ".txt")));
            bufferedWriter.write(getString(R.string.note_title) + "\n" + note.getTitle() + "\n\n" + getString(R.string.note_body) + "\n" + note.getBody());
            if (!note.getImportance().equals(Note.NO_IMPORTANCE)) {
                String importance = null;
                switch (note.getImportance()) {
                    case Note.GREEN_IMPORTANCE:
                        importance = getResources().getStringArray(R.array.imprortance_array)[2];
                        break;
                    case Note.YELLOW_IMPORTANCE:
                        importance = getResources().getStringArray(R.array.imprortance_array)[1];
                        break;
                    case Note.RED_IMPORTANCE:
                        importance = getResources().getStringArray(R.array.imprortance_array)[0];
                        break;
                }
                bufferedWriter.write("\n\n" + getString(R.string.importance_of_note) + " " + importance);
            }
            if (note.getLocation().latitude != Note.NO_LATITUDE) {
                bufferedWriter.write("\n\n" + getString(R.string.location) + "\n" +
                        getString(R.string.lattitude) + ": " + String.valueOf(note.getLocation().latitude)
                        + "\n" + getString(R.string.longitude) + ": " + String.valueOf(note.getLocation().longitude));
            }
            bufferedWriter.close();
            Toast.makeText(ViewNoteActivity.this, getString(R.string.note_is_exported), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ViewNoteActivity.this, getString(R.string.error) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_note_activity_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.delete_note_menu_item:
                DeleteNoteDialogFragment deleteNoteDialogFragment = new DeleteNoteDialogFragment();
                deleteNoteDialogFragment.show(getSupportFragmentManager(), null);
                break;
            case R.id.edit_note_menu_item:
                Intent intent = new Intent(this, EditNoteActivity.class);
                intent.putExtra(EditNoteActivity.NOTE_TAG, note);
                startActivityForResult(intent, EDIT_REQUEST_CODE);
                break;
            case R.id.share_vk_menu_item:
                shareVK(note);
                break;
            case R.id.share_fb_menu_item:
                shareFB(note);
                break;
            case R.id.share_twitter_menu_item:
                shareTwitter(note);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case EditNoteActivity.SAVED_RESULT_CODE:
                note = notesDb.getNote(note.getId());
                titleTV.setText(note.getTitle());
                bodyTV.setText(note.getBody());
                if (!note.getPhoto().equals(Note.NO_PHOTO)) {
                    ImageLoader loader = new ImageLoader();
                    loader.execute(note.getPhoto());
                } else
                    noteIV.setImageResource(R.mipmap.nophoto);
                if (note.getLocation().longitude != Note.NO_LONGITUDE)
                    location = note.getLocation();
                else
                    location = null;
                setLocationMap(location);
                BackgroundUtil.setBackgroundWithNoteImportance(this, backgroundView, note);
                Toast.makeText(this, getString(R.string.note_is_edited), Toast.LENGTH_SHORT).show();
                break;
            case EditNoteActivity.DELETED_RESULT_CODE:
                setResult(EditNoteActivity.DELETED_RESULT_CODE);
                finish();
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionsUtil.REQUEST_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    filePickerDialog.show();
                } else
                    Toast.makeText(this, R.string.storage_permissions_are_not_granted, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);

        if (note.getLocation().longitude != Note.NO_LONGITUDE) {
            location = note.getLocation();
        } else {
            location = null;
        }

        if (location == null)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapActivity.DEFAULT_LOCATION, MapActivity.DEFAULT_ZOOM_LITTLE_MAP));
        else {
            marker = googleMap.addMarker(new MarkerOptions().position(location)
                    .title(note.getTitle()));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(note.getLocation(), MapActivity.DEFAULT_ZOOM_LITTLE_MAP));
            marker.showInfoWindow();
        }

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (location != null) {
                    Intent intent = new Intent(ViewNoteActivity.this, MapActivity.class);
                    intent.setAction(MapActivity.ACTION_SHOW_NOTE);
                    intent.putExtra(MapActivity.NOTE_TITLE, note.getTitle());
                    intent.putExtra(MapActivity.NOTE_LOCATION, location);
                    ViewNoteActivity.this.startActivityForResult(intent, SHOW_NOTE_ON_MAP_REQUEST_CODE);
                } else
                    Toast.makeText(ViewNoteActivity.this, getString(R.string.location_is_not_availiable), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setLocationMap(LatLng location) {
        if (map != null) {
            if (location == null) {
                if (marker != null)
                    marker.remove();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(MapActivity.DEFAULT_LOCATION, MapActivity.DEFAULT_ZOOM_LITTLE_MAP));
            } else {
                if (marker != null)
                    marker.remove();
                marker = map.addMarker(new MarkerOptions().position(location)
                        .title(note.getTitle()));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, MapActivity.DEFAULT_ZOOM_LITTLE_MAP));
                marker.showInfoWindow();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void deleteNote() {
        if (!note.getPhoto().equals(Note.NO_PHOTO))
            ImageFiles.deleteFile(note.getPhoto());
        notesDb.deleteNote(note.getId());
        setResult(EditNoteActivity.DELETED_RESULT_CODE);
        finish();
    }

    private class ImageLoader extends AsyncTask<String, Void, Bitmap> {
        int reqWidth, reqHeight;

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            noteIV.setImageBitmap(bitmap);
            super.onPostExecute(bitmap);
        }

        @Override
        protected void onPreExecute() {
            reqHeight = getResources().getDimensionPixelSize(R.dimen.view_note_photo_height);
            reqWidth = getResources().getDimensionPixelSize(R.dimen.view_note_max_photo_width);
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String path = params[0];
            return ImageFiles.decodeSampledBitmapFromFile(path, reqWidth, reqHeight);
        }
    }

}
