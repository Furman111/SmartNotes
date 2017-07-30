package ru.furman.smartnotes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.furman.smartnotes.database.DB;

/**
 * Created by Furman on 26.07.2017.
 */

public class EditNoteActivity extends AppCompatActivity {

    private EditText title, body;
    private Button saveBtn, cancelBtn;
    private ImageView photoIV;
    private Spinner importanceSpinner;
    private View background;
    private Note note;
    private DB db;

    private String oldPhoto, currentPhoto, newPhoto;

    public static final int PHOTO_PICK_REQUEST_CODE = 3;
    public static final int CAMERA_REQUSET_CODE = 4;
    public static final int SAVED_RESULT_CODE = 1;
    public static final int DELETED_RESULT_CODE = 2;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.edit_note);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        note = getIntent().getParcelableExtra(MainActivity.NOTE_TAG);
        title = (EditText) findViewById(R.id.note_title_edit);
        body = (EditText) findViewById(R.id.note_body_edit);
        saveBtn = (Button) findViewById(R.id.save_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        background = findViewById(R.id.importance_background);
        photoIV = (ImageView) findViewById(R.id.note_mageIV);
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
                    setPhotoIV(oldPhoto);
                    if (!currentPhoto.equals(oldPhoto))
                        deletePhoto(currentPhoto);
                } else {
                    importanceSpinner.setSelection(3);
                    title.setText("");
                    body.setText("");
                    photoIV.setImageResource(R.mipmap.nophoto);
                    if (currentPhoto != null)
                        deletePhoto(currentPhoto);
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
                        if (currentPhoto == null)
                            currentPhoto = Note.NO_PHOTO;
                        else {
                            if (oldPhoto != null && !oldPhoto.equals(currentPhoto)) {
                                deletePhoto(oldPhoto);
                            }
                        }
                        db.editNote(note.getId(), new Note(title.getText().toString(), body.getText().toString(), currentPhoto, importance, -1));
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
                        if (currentPhoto == null) currentPhoto = Note.NO_PHOTO;
                        db.addNote(new Note(title.getText().toString(), body.getText().toString(), importance, currentPhoto, -1));
                    }
                    setResult(SAVED_RESULT_CODE);
                    finish();
                } else
                    Toast.makeText(EditNoteActivity.this, getResources().getString(R.string.name_is_not_entered), Toast.LENGTH_SHORT).show();
            }
        });

        photoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (note == null || note.getPhoto() == Note.NO_PHOTO) {
                    PhotoPickerDialogFragment dialog = new PhotoPickerDialogFragment();
                    dialog.show(getFragmentManager(), null);
                } else {
                    PhotoChangeDialogFragment chDialog = new PhotoChangeDialogFragment();
                    chDialog.show(getFragmentManager(), null);
                }
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

        if (note != null && !note.getPhoto().equals(Note.NO_PHOTO)) {
            oldPhoto = note.getPhoto();
            photoIV.setImageURI(Uri.parse("file://" + note.getPhoto()));
            currentPhoto = note.getPhoto();
        } else {
            currentPhoto = null;
            oldPhoto = null;
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
            case android.R.id.home:
                finish();
                break;
            case R.id.delete_note_edit_menu:
                if (note != null) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAMERA_REQUSET_CODE:
                if (resultCode == RESULT_OK) {
                    setPhotoIV(newPhoto);
                    if (currentPhoto != null && !currentPhoto.equals(oldPhoto)) {
                        deletePhoto(currentPhoto);
                    }
                    currentPhoto = newPhoto;
                }
                return;
            case PHOTO_PICK_REQUEST_CODE:
                if (data != null) {
                    Uri selectedPhoto = data.getData();
                    File file = null;
                    try {
                        file = PhotoPickerDialogFragment.createImageFile(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (file != null) {
                        newPhoto = selectedPhoto.getPath();
                        try {
                            Util.copyFile(new File(newPhoto), file);
                        } catch (IOException e) {
                            Toast.makeText(this, getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                        }
                        if (currentPhoto != null && !currentPhoto.equals(oldPhoto))
                            deletePhoto(currentPhoto);
                        currentPhoto = file.getPath();
                    }
                    setPhotoIV(currentPhoto);
                }
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static class PhotoPickerDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.add_photo))
                    .setItems(R.array.photo_picker_array, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 1:
                                    Intent photoPickerItent = new Intent(Intent.ACTION_PICK);
                                    photoPickerItent.setType("image/*");
                                    getActivity().startActivityForResult(photoPickerItent, PHOTO_PICK_REQUEST_CODE);
                                    return;
                                case 0:
                                    Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    if (photoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                        File photoFile = null;
                                        try {
                                            photoFile = createImageFile(getActivity());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        if (photoFile != null) {
                                            Uri photoURI = FileProvider.getUriForFile(getActivity(),
                                                    "ru.furman.smartnotes.fileprovider",
                                                    photoFile);
                                            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                            getActivity().startActivityForResult(photoIntent, CAMERA_REQUSET_CODE);
                                        }
                                    }
                            }
                        }
                    });

            return builder.create();
        }


        public static File createImageFile(Context ctx) throws IOException {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = ctx.getFilesDir();
            File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            ((EditNoteActivity) ctx).newPhoto = image.getAbsolutePath();
            return image;
        }
    }


    public static class PhotoChangeDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.photo))
                    .setItems(R.array.photo_array, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    //Просмотр
                                    return;
                                case 1:
                                    PhotoPickerDialogFragment mdialog = new PhotoPickerDialogFragment();
                                    mdialog.show(getActivity().getFragmentManager(), null);
                                    return;
                                case 2:
                                    //удаление
                            }
                        }
                    });

            return builder.create();
        }
    }

    private void setPhotoIV(String path) {
        photoIV.setImageURI(Uri.fromFile(new File(path)));
    }

    private void deletePhoto(String path) {
        File file = new File(path);
        file.delete();
    }
}
