package ru.furman.smartnotes;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import ru.furman.smartnotes.database.DB;

/**
 * Created by Furman on 27.07.2017.
 */

public class OnNoteClickListener implements AdapterView.OnItemClickListener {

    Context ctx;

    public OnNoteClickListener(Context ctx){
        this.ctx = ctx;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(ctx,ViewNoteActivity.class);
        intent.putExtra(MainActivity.NOTE_TAG,(new DB(ctx)).getNote(id));
        ((MainActivity) ctx).startActivityForResult(intent,MainActivity.VIEW_NOTE_REQUEST_CODE,null);
    }

}
