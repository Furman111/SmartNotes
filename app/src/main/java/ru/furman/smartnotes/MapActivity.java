package ru.furman.smartnotes;


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

import ru.furman.smartnotes.database.DB;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng currentLoc;
    private DB db;
    private int editNoteId;

    public static final String NOTE_TITLE = "title";
    public static final String NOTE_LOCATION = "location";
    public static final String CHOSEN_LOCATION = "chosenLoc";
    public static final String REQUEST_CODE = "requestCode";

    public static final float DEFAULT_ZOOM_BIG_MAP = 15;
    public static final int DEFAULT_ZOOM_LITTLE_MAP = 10;
    public static final int DEFAULT_ZOOM_LARGE_MAP = 8;


    public static final int SHOW_NOTE_REQUEST_CODE = 1;
    public static final int CHANGE_NOTE_LOCATION_REQUEST_CODE = 2;
    public static final int SHOW_LIST_NOTES_REQUEST_CODE = 3;

    public static final int SHOW_NOTE_REQUST_CODE = 4;

    public static final LatLng DEFAULT_LOCATION = new LatLng(53, 50);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        switch (getIntent().getIntExtra(REQUEST_CODE, 0)) {
            case CHANGE_NOTE_LOCATION_REQUEST_CODE:
                String title = getIntent().getStringExtra(NOTE_TITLE);
                LatLng loc = getIntent().getParcelableExtra(NOTE_LOCATION);

                if (title == null) {
                    currentLoc = DEFAULT_LOCATION;
                    mMap.addMarker(new MarkerOptions().title(getResources().getString(R.string.new_note))
                            .position(DEFAULT_LOCATION)
                            .draggable(true)).showInfoWindow();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM_BIG_MAP));
                } else {
                    currentLoc = loc;
                    Marker marker;
                    if (currentLoc.longitude == Note.NO_LONGITUDE)
                        marker = mMap.addMarker(new MarkerOptions().title(title).draggable(true).position(DEFAULT_LOCATION));
                    else
                        marker = mMap.addMarker(new MarkerOptions().title(title).draggable(true).position(currentLoc));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), DEFAULT_ZOOM_BIG_MAP));
                    marker.showInfoWindow();
                }
                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
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
            case SHOW_NOTE_REQUEST_CODE:
                LatLng loc1 = (LatLng) getIntent().getParcelableExtra(NOTE_LOCATION);
                mMap.addMarker(new MarkerOptions().title(getIntent().getStringExtra(NOTE_TITLE)
                ).position(loc1)).showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc1, DEFAULT_ZOOM_BIG_MAP));
                break;
            case SHOW_LIST_NOTES_REQUEST_CODE:
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
                    mMap.addMarker(new MarkerOptions().position(locat).title(note.getTitle())).setTag(note);
                }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locat, DEFAULT_ZOOM_LARGE_MAP));
        }

        mMap.setOnInfoWindowClickListener(new InfoWindowOnClickListener());

    }

    private class InfoWindowOnClickListener implements GoogleMap.OnInfoWindowClickListener{
        @Override
        public void onInfoWindowClick(Marker marker) {
            Intent intent = new Intent(MapActivity.this,ViewNoteActivity.class);
            intent.putExtra(ViewNoteActivity.NOTE_TAG,(Note) marker.getTag());
            editNoteId = ((Note) marker.getTag()).getId();
            marker.remove();
            MapActivity.this.startActivityForResult(intent,SHOW_NOTE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case SHOW_NOTE_REQUEST_CODE:
                Note note = db.getNote(editNoteId);
                if(note!=null){
                    mMap.addMarker(new MarkerOptions().position(note.getLocation()).title(note.getTitle())).setTag(note);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(note.getLocation(),DEFAULT_ZOOM_LARGE_MAP));
                    mMap.setOnInfoWindowClickListener(new InfoWindowOnClickListener());
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.save:
                if (getIntent().getIntExtra(REQUEST_CODE, 0) == CHANGE_NOTE_LOCATION_REQUEST_CODE) {
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
