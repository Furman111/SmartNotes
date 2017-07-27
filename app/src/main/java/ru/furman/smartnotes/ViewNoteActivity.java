package ru.furman.smartnotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import ru.furman.smartnotes.database.DB;

/**
 * Created by Furman on 26.07.2017.
 */

public class ViewNoteActivity extends AppCompatActivity {

    Note note;
    DB db;

    public static final int EDIT_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_note);

        db = new DB(this);

        Intent intent = getIntent();
        note = intent.getParcelableExtra(MainActivity.NOTE_TAG);
        ((TextView) findViewById(R.id.note_title)).setText(note.getTitle());
        ((TextView) findViewById(R.id.note_body)).setText(note.getBody());

        View view = findViewById(R.id.importance_background);
        switch (note.getImportance()){
            case Note.GREEN_IMPORTANCE:
                view.setBackgroundColor(ContextCompat.getColor(this,R.color.greenImportance));
                break;
            case Note.RED_IMPORTANCE:
                view.setBackgroundColor(ContextCompat.getColor(this,R.color.redImportance));
                break;
            case Note.YELLOW_IMPORTANCE:
                view.setBackgroundColor(ContextCompat.getColor(this,R.color.yellowImportance));
                break;
            case Note.NO_IMPORTANCE:
                view.setBackgroundColor(ContextCompat.getColor(this,R.color.zeroImportance));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_note:
                db.deleteNote(note.getId());
                onBackPressed();
                break;
            case R.id.edit_note:
                Intent intent = new Intent(this,EditNoteActivity.class);
                intent.putExtra(MainActivity.NOTE_TAG,note);
                startActivityForResult(intent,EDIT_REQUEST_CODE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
