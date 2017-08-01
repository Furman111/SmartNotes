package ru.furman.smartnotes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import ru.furman.smartnotes.database.DB;

public class MainActivity extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    private DB db;
    public static final String NOTE_TAG = "note";

    public static final int VIEW_NOTE_REQUEST_CODE = 1;
    public static final int CREATE_NOTE_REQUEST_CODE = 2;
    public static final int EDIT_NOTE_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DB(this);

        notesRecyclerView = (RecyclerView) findViewById(R.id.notes_recycler_view);
        notesRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        notesRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyAdapter(this);
        notesRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Util.deletePhoto(db.getNote(((MyAdapter.ViewHolder) viewHolder).getId()).getPhoto());
                db.deleteNote(((MyAdapter.ViewHolder) viewHolder).getId());
                mAdapter.notifyDataSetChanged();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(notesRecyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_note:
                Intent intent = new Intent(this, EditNoteActivity.class);
                startActivityForResult(intent, CREATE_NOTE_REQUEST_CODE);
                return true;
            case R.id.show_on_map:
                Intent intent1 = new Intent(this, MapActivity.class);
                intent1.putExtra(MapActivity.REQUEST_CODE, MapActivity.SHOW_LIST_NOTES_REQUEST_CODE);
                startActivityForResult(intent1, MapActivity.SHOW_LIST_NOTES_REQUEST_CODE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mAdapter.notifyDataSetChanged();
        switch (resultCode) {
            case EditNoteActivity.SAVED_RESULT_CODE:
                Toast.makeText(this, R.string.note_is_saved, Toast.LENGTH_SHORT).show();
                break;
            case EditNoteActivity.DELETED_RESULT_CODE:
                Toast.makeText(this, R.string.note_is_deleted, Toast.LENGTH_SHORT).show();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
