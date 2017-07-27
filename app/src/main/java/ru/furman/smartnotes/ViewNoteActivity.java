package ru.furman.smartnotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ru.furman.smartnotes.database.DB;

/**
 * Created by Furman on 26.07.2017.
 */

public class ViewNoteActivity extends AppCompatActivity {

    Note note;
    DB db;
    TextView body, title;
    View view;

    public static final int EDIT_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_note);

        db = new DB(this);

        Intent intent = getIntent();
        note = intent.getParcelableExtra(MainActivity.NOTE_TAG);
        body = (TextView) findViewById(R.id.note_body);
        title = (TextView) findViewById(R.id.note_title);
        title.setText(note.getTitle());
        body.setText(note.getBody());

        view = findViewById(R.id.importance_background);
        Util.setBackgroundWithImportance(this, view, note);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_note:
                db.deleteNote(note.getId());
                setResult(EditNoteActivity.DELETED_RESULT_CODE);
                finish();
                break;
            case R.id.edit_note:
                Intent intent = new Intent(this, EditNoteActivity.class);
                intent.putExtra(MainActivity.NOTE_TAG, note);
                startActivityForResult(intent, EDIT_REQUEST_CODE);
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
                Util.setBackgroundWithImportance(this, view, note);
                Toast.makeText(this,getResources().getString(R.string.note_is_edited),Toast.LENGTH_SHORT).show();
                break;
            case EditNoteActivity.DELETED_RESULT_CODE:
                setResult(EditNoteActivity.DELETED_RESULT_CODE);
                finish();
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}
