package ru.furman.smartnotes.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.List;

import ru.furman.smartnotes.note.Note;
import ru.furman.smartnotes.R;
import ru.furman.smartnotes.note.database.DB;
import ru.furman.smartnotes.ui.viewingnote.ViewNoteActivity;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private LatLng currentLoc;
    private DB db;
    private int editNoteId;

    public static final String NOTE_TITLE = "title";
    public static final String NOTE_LOCATION = "location";
    public static final String CHOSEN_LOCATION = "chosenLoc";

    public static final float DEFAULT_ZOOM_BIG_MAP = 10;
    public static final int DEFAULT_ZOOM_LITTLE_MAP = 11;
    public static final int DEFAULT_ZOOM_LARGE_MAP = 8;


    public static final String ACTION_SHOW_NOTE = "showNote";
    public static final String ACTION_CHANGE_NOTE_LOCATION = "changeNoteLocation";
    public static final String ACTION_SHOW_NOTES = "showNotes";

    public static final int SHOW_NOTE_REQUEST_CODE = 1;

    public static final LatLng DEFAULT_LOCATION = new LatLng(53.24279758, 50.18600692);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);
        db = new DB(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);

        switch (getIntent().getAction()) {
            case ACTION_CHANGE_NOTE_LOCATION:
                String title = getIntent().getStringExtra(NOTE_TITLE);
                LatLng loc = getIntent().getParcelableExtra(NOTE_LOCATION);

                if (title == null) {
                    currentLoc = DEFAULT_LOCATION;
                    Marker marker = map.addMarker(new MarkerOptions().title(getResources().getString(R.string.new_note))
                            .position(DEFAULT_LOCATION)
                            .draggable(true));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM_BIG_MAP));
                    marker.showInfoWindow();
                } else {
                    currentLoc = loc;
                    Marker marker;
                    if (currentLoc == null)
                        marker = map.addMarker(new MarkerOptions().title(title).draggable(true).position(DEFAULT_LOCATION));
                    else
                        marker = map.addMarker(new MarkerOptions().title(title).draggable(true).position(currentLoc));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), DEFAULT_ZOOM_BIG_MAP));
                    marker.showInfoWindow();
                }
                map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        currentLoc = marker.getPosition();
                    }
                });
                break;
            case ACTION_SHOW_NOTE:
                LatLng loc1 = getIntent().getParcelableExtra(NOTE_LOCATION);
                Marker marker = map.addMarker(new MarkerOptions().title(getIntent().getStringExtra(NOTE_TITLE)
                ).position(loc1));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc1, DEFAULT_ZOOM_BIG_MAP));
                marker.showInfoWindow();
                break;
            case ACTION_SHOW_NOTES:
                placeNotesMarkers();
        }

    }

    public void placeNotesMarkers(){
        List<Note> notes = db.getNotes();

        LatLng locat = DEFAULT_LOCATION;

        if (!notes.isEmpty()) {
            for (Note note : notes)
                if (note.getLocation().longitude != Note.NO_LONGITUDE) {
                    locat = note.getLocation();
                    map.addMarker(new MarkerOptions().position(locat).title(note.getTitle())).setTag(note);
                }
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(locat, DEFAULT_ZOOM_LARGE_MAP));
        }

        map.setOnInfoWindowClickListener(new InfoWindowOnClickListener());

    }

    private class InfoWindowOnClickListener implements GoogleMap.OnInfoWindowClickListener{
        @Override
        public void onInfoWindowClick(Marker marker) {
            Intent intent = new Intent(MapActivity.this,ViewNoteActivity.class);
            intent.putExtra(ViewNoteActivity.NOTE_TAG,(Note) marker.getTag());
            editNoteId = ((Note) marker.getTag()).getId();
            marker.remove();
            MapActivity.this.startActivityForResult(intent, SHOW_NOTE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case SHOW_NOTE_REQUEST_CODE:
                Note note = db.getNote(editNoteId);
                if(note!=null){
                    map.addMarker(new MarkerOptions().position(note.getLocation()).title(note.getTitle())).setTag(note);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(note.getLocation(),DEFAULT_ZOOM_LARGE_MAP));
                    map.setOnInfoWindowClickListener(new InfoWindowOnClickListener());
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        if(getIntent().getAction().equals(ACTION_CHANGE_NOTE_LOCATION))
            menu.getItem(0).setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.save_menu_item:
                if (getIntent().getAction().equals(ACTION_CHANGE_NOTE_LOCATION)) {
                    Intent resIntent = new Intent();
                    resIntent.putExtra(CHOSEN_LOCATION, currentLoc);
                    setResult(RESULT_OK, resIntent);
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
