package ru.furman.smartnotes;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Furman on 27.07.2017.
 */

public class NotesListAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;
    Context ctx;
    List<Note> noteList;

    public NotesListAdapter(Context ctx, List<Note> notesList) {
        this.ctx = ctx;
        this.noteList = notesList;
        layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return noteList.size();
    }

    @Override
    public Note getItem(int position) {
        return noteList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return noteList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.note_item_in_list, parent,false);
        }

        final Note note = getItem(position);

        ((TextView) view.findViewById(R.id.note_title)).setText(note.getTitle());
        switch (note.getImportance()){
            case Note.GREEN_IMPORTANCE:
                view.setBackgroundColor(ContextCompat.getColor(ctx,R.color.greenImportance));
                break;
            case Note.RED_IMPORTANCE:
                view.setBackgroundColor(ContextCompat.getColor(ctx,R.color.redImportance));
                break;
            case Note.YELLOW_IMPORTANCE:
                view.setBackgroundColor(ContextCompat.getColor(ctx,R.color.yellowImportance));
                break;
            case Note.NO_IMPORTANCE:
                view.setBackgroundColor(ContextCompat.getColor(ctx,R.color.zeroImportance));
                break;

        }
        ((ImageView) view.findViewById(R.id.edit_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, EditNoteActivity.class);
                intent.putExtra(MainActivity.NOTE_TAG, note);
                ctx.startActivity(intent);
            }
        });

        return view;
    }

    public void notifyDataSetChanged(List<Note> notes) {
        noteList = notes;
    }
}
