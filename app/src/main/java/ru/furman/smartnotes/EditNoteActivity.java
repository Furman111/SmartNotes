package ru.furman.smartnotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import ru.furman.smartnotes.database.DB;

/**
 * Created by Furman on 26.07.2017.
 */

public class EditNoteActivity extends AppCompatActivity {

    EditText title, body;
    Button saveBtn, cancelBtn;
    Spinner importanceSpinner;
    View background;
    Note note;
    DB db;

    public static final int SAVED_RESULT_CODE = 1;
    public static final int DELETED_RESULT_CODE = 2;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.edit_note);

        note = getIntent().getParcelableExtra(MainActivity.NOTE_TAG);
        title = (EditText) findViewById(R.id.note_title_edit);
        body = (EditText) findViewById(R.id.note_body_edit);
        saveBtn = (Button) findViewById(R.id.save_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        background = findViewById(R.id.importance_background);
        db = new DB(this);

        importanceSpinner = (Spinner) findViewById(R.id.importance_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.spinner_array));
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        importanceSpinner.setAdapter(adapter);

        if (note != null)
            setDefaultSelection();
        else
            importanceSpinner.setSelection(3);

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
                } else {
                    importanceSpinner.setSelection(3);
                    title.setText("");
                    body.setText("");
                }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!title.getText().toString().isEmpty()) {
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
                        db.editNote(note.getId(), new Note(title.getText().toString(), body.getText().toString(), importance, -1));
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
                        db.addNote(new Note(title.getText().toString(), body.getText().toString(), importance, -1));
                    }
                    setResult(SAVED_RESULT_CODE);
                    finish();
                }
                else
                    Toast.makeText(EditNoteActivity.this,getResources().getString(R.string.name_is_not_entered),Toast.LENGTH_SHORT).show();
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
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_note_edit_menu:
                if(note!=null){
                    db.deleteNote(note.getId());
                    setResult(DELETED_RESULT_CODE);
                    finish();
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
}
