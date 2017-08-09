package ru.furman.smartnotes.viewingnote;

import android.app.DialogFragment;
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

import ru.furman.smartnotes.DeleteNoteDialogFragment;
import ru.furman.smartnotes.EditNoteActivity;
import ru.furman.smartnotes.ImageSampler;
import ru.furman.smartnotes.MapActivity;
import ru.furman.smartnotes.Note;
import ru.furman.smartnotes.R;
import ru.furman.smartnotes.Util;
import ru.furman.smartnotes.ViewImageActivity;
import ru.furman.smartnotes.database.DB;

public class ViewNoteActivity extends SharingActivity implements ShareDialogFragment.ShareDialogListener, OnMapReadyCallback, DeleteNoteDialogFragment.NoticeDialogListener {

    private Note note;
    private DB db;
    private TextView body, title;
    private View view;
    private FilePickerDialog dialog;
    private ImageView noteIV;
    private MapView mapView;
    private GoogleMap map;
    private Marker marker;
    private LatLng loc;

    public static final int EDIT_REQUEST_CODE = 1;
    public static final int SHOW_NOTE_REQUEST_CODE = 1;

    public static final String NOTE_TAG = "note";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.view_note);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DB(this);

        Intent intent = getIntent();
        note = intent.getParcelableExtra(NOTE_TAG);

        title = (TextView) findViewById(R.id.note_title);
        title.setText(note.getTitle());

        body = (TextView) findViewById(R.id.note_body);
        body.setText(note.getBody());

        noteIV = (ImageView) findViewById(R.id.note_image);
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

        Button importBtn = (Button) findViewById(R.id.export_to_TXT_btn);
        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        view = findViewById(R.id.importance_background);
        Util.setBackgroundWithImportance(this, view, note);

        mapView = (MapView) findViewById(R.id.map_view);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);

        super.onCreate(savedInstanceState);
    }

    private void onCreateDialogFilePicker(){
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        dialog = new FilePickerDialog(this, properties);
        dialog.setTitle(getString(R.string.to_choose_directory));
        dialog.setPositiveBtnName(getString(R.string.to_choose));
        dialog.setNegativeBtnName(getString(R.string.cancel));

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null) {
                    if (files[0].contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                        if (!Environment.getExternalStorageState().equals(
                                Environment.MEDIA_MOUNTED)) {
                            Toast.makeText(ViewNoteActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(files[0] + "/" + note.getTitle() + ".txt")));
                                bufferedWriter.write(getString(R.string.note_tile) + "\n" + note.getTitle() + "\n" + getString(R.string.note_body) + "\n" + note.getBody());
                                bufferedWriter.close();
                                Toast.makeText(ViewNoteActivity.this, getString(R.string.note_is_exported), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(ViewNoteActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        try {
                            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(files[0] + "/" + note.getTitle() + ".txt")));
                            bufferedWriter.write(getString(R.string.note_tile) + "\n" + note.getTitle() + "\n" + getString(R.string.note_body) + "\n" + note.getBody());
                            bufferedWriter.close();
                            Toast.makeText(ViewNoteActivity.this, getString(R.string.note_is_exported), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(ViewNoteActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.delete_note:
                DeleteNoteDialogFragment deleteNoteDialogFragment = new DeleteNoteDialogFragment();
                deleteNoteDialogFragment.show(getFragmentManager(), null);
                break;
            case R.id.edit_note:
                Intent intent = new Intent(this, EditNoteActivity.class);
                intent.putExtra(EditNoteActivity.NOTE_TAG, note);
                startActivityForResult(intent, EDIT_REQUEST_CODE);
                break;
            case R.id.share_note:
                ShareDialogFragment shareDialogFragment = new ShareDialogFragment();
                shareDialogFragment.setListener(ViewNoteActivity.this);
                shareDialogFragment.show(getFragmentManager(), null);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case EditNoteActivity.SAVED_RESULT_CODE:
                note = db.getNote(note.getId());
                title.setText(note.getTitle());
                body.setText(note.getBody());
                if (!note.getPhoto().equals(Note.NO_PHOTO)) {
                    ImageLoader loader = new ImageLoader();
                    loader.execute(note.getPhoto());
                } else
                    noteIV.setImageDrawable(null);
                Util.setBackgroundWithImportance(this, view, note);
                Toast.makeText(this, getString(R.string.note_is_edited), Toast.LENGTH_SHORT).show();
                break;
            case EditNoteActivity.DELETED_RESULT_CODE:
                setResult(EditNoteActivity.DELETED_RESULT_CODE);
                finish();
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

        if (note.getLocation().longitude != Note.NO_LONGITUDE)
            loc = note.getLocation();
        else
            loc = null;
        setLocationMap(loc);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (dialog != null) {
                        dialog.show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.permissions_are_not_granted), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setMapToolbarEnabled(false);

        if (note.getLocation().longitude != Note.NO_LONGITUDE) {
            loc = note.getLocation();
        } else {
            loc = null;
        }

        if (loc == null)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapActivity.DEFAULT_LOCATION, MapActivity.DEFAULT_ZOOM_LITTLE_MAP));
        else {
            marker = googleMap.addMarker(new MarkerOptions().position(loc)
                    .title(note.getTitle()));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(note.getLocation(), MapActivity.DEFAULT_ZOOM_LITTLE_MAP));
            marker.showInfoWindow();
        }

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (loc != null) {
                    Intent intent = new Intent(ViewNoteActivity.this, MapActivity.class);
                    intent.setAction(MapActivity.ACTION_SHOW_NOTE);
                    intent.putExtra(MapActivity.NOTE_TITLE, note.getTitle());
                    intent.putExtra(MapActivity.NOTE_LOCATION, loc);
                    ViewNoteActivity.this.startActivityForResult(intent, SHOW_NOTE_REQUEST_CODE);
                } else
                    Toast.makeText(ViewNoteActivity.this, getString(R.string.no_geodata), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setLocationMap(LatLng location) {
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


    @Override
    protected void onStart() {
        mapView.onStart();
        super.onStart();
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
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
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
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (!note.getPhoto().equals(Note.NO_PHOTO))
            Util.deleteFile(note.getPhoto());
        db.deleteNote(note.getId());
        setResult(EditNoteActivity.DELETED_RESULT_CODE);
        finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void shareVK() {
        shareVK(note);
    }

    @Override
    public void shareFB() {
        shareFB(note);
    }

    @Override
    public void shareTwitter() {
        shareTwitter(note);
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
            reqHeight = getResources().getDimensionPixelSize(R.dimen.view_note_iv_height);
            reqWidth = getResources().getDisplayMetrics().widthPixels / 2;
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String path = params[0];
            return ImageSampler.decodeSampledBitmapFromFile(path, reqWidth, reqHeight);
        }
    }

}