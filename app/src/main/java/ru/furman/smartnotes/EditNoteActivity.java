package ru.furman.smartnotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Furman on 26.07.2017.
 */

public class EditNoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);

        if (intent.getParcelableExtra(MainActivity.NOTE_TAG)!=null){
            //если не пустой
        }
        else
        {
            getSupportActionBar().setTitle(getResources().getString(R.string.new_note));
            //если пустой
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_note_edit_menu:
                //удалить заметку
                break;
            case R.id.ok_note_edit_menu:
                //сохранить заметку
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
