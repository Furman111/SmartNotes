package ru.furman.smartnotes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import ru.furman.smartnotes.database.DB;

public class MainActivity extends AppCompatActivity {

    DB db;
    NotesListAdapter adapter;
    public static final String NOTE_TAG  = "note";
    public static final int VIEW_NOTE_REQUEST_CODE = 1;
    public static final int CREATE_NOTE_REQUEST_CODE = 2;
    public static final int EDIT_NOTE_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DB(this);
        adapter = new NotesListAdapter(this,db.getNotes());

        ListView lv =(ListView) findViewById(R.id.notes_list_view);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new OnNoteClickListener(this));

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
                Intent intent = new Intent(this,EditNoteActivity.class);
                startActivityForResult(intent,CREATE_NOTE_REQUEST_CODE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        adapter.notifyDataSetChanged(db.getNotes());
        switch (resultCode){
            case EditNoteActivity.SAVED_RESULT_CODE:
                Toast.makeText(this,R.string.note_is_saved,Toast.LENGTH_SHORT).show();
                break;
            case EditNoteActivity.DELETED_RESULT_CODE:
                Toast.makeText(this,R.string.note_is_deleted,Toast.LENGTH_SHORT).show();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
