package ru.furman.smartnotes;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;

import ru.furman.smartnotes.database.DB;

/**
 * Created by Furman on 26.07.2017.
 */

public class ViewNoteActivity extends AppCompatActivity {

    private Note note;
    private DB db;
    private TextView body, title;
    private View view;
    private Button importBtn;
    private FilePickerDialog dialog;


    public static final int EDIT_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_note);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DB(this);

        Intent intent = getIntent();
        note = intent.getParcelableExtra(MainActivity.NOTE_TAG);
        body = (TextView) findViewById(R.id.note_body);
        title = (TextView) findViewById(R.id.note_title);
        title.setText(note.getTitle());
        body.setText(note.getBody());

        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        dialog = new FilePickerDialog(this,properties);
        dialog.setTitle(getResources().getString(R.string.to_choose_directory));
        dialog.setPositiveBtnName(getResources().getString(R.string.to_choose));
        dialog.setNegativeBtnName(getResources().getString(R.string.cancel));

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                Log.d("DSDSD",files[0]);
            }
        });

        importBtn = (Button) findViewById(R.id.export_to_TXT_btn);
        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

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
            case android.R.id.home:
                finish();
                break;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(dialog!=null)
                    {
                        dialog.show();
                    }
                }
                else {
                    Toast.makeText(this,getResources().getString(R.string.permissions_are_not_granted),Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
