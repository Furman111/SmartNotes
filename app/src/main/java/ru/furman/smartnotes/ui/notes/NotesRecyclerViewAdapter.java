package ru.furman.smartnotes.ui.notes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.furman.smartnotes.utils.ImageFiles;
import ru.furman.smartnotes.note.Note;
import ru.furman.smartnotes.R;
import ru.furman.smartnotes.ui.ViewImageActivity;
import ru.furman.smartnotes.note.database.NotesDB;
import ru.furman.smartnotes.ui.editingnote.EditNoteActivity;
import ru.furman.smartnotes.ui.viewingnote.ViewNoteActivity;

public class NotesRecyclerViewAdapter extends RecyclerView.Adapter<NotesRecyclerViewAdapter.ViewHolder> {

    private NotesDB notesDb;
    private Context ctx;
    private LruCache<String, Bitmap> memoryCache;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View background;
        public TextView titleTV;
        public ImageView editIV;
        public ImageView noteIV;
        private int id;

        public int getId() {
            return id;
        }

        public ViewHolder(View view) {
            super(view);
            this.background = view.findViewById(R.id.background);
            this.titleTV = (TextView) view.findViewById(R.id.note_title_tv);
            this.editIV = (ImageView) view.findViewById(R.id.edit_btn_iv);
            this.noteIV = (ImageView) view.findViewById(R.id.note_photo_iv);
        }
    }

    public NotesRecyclerViewAdapter(Context ctx) {
        this.notesDb = new NotesDB(ctx);
        this.ctx = ctx;

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    @Override
    public NotesRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item_in_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NotesRecyclerViewAdapter.ViewHolder holder,int position) {
        final List<Note> notes = notesDb.getNotes();
        switch (notes.get(position).getImportance()) {
            case Note.GREEN_IMPORTANCE:
                holder.background.setBackground(ContextCompat.getDrawable(ctx, R.drawable.green_list_item_gradient));
                break;
            case Note.RED_IMPORTANCE:
                holder.background.setBackground(ContextCompat.getDrawable(ctx, R.drawable.red_list_item_gradient));
                break;
            case Note.YELLOW_IMPORTANCE:
                holder.background.setBackground(ContextCompat.getDrawable(ctx, R.drawable.yellow_list_item_gradient));
                break;
            case Note.NO_IMPORTANCE:
                holder.background.setBackgroundColor(ContextCompat.getColor(ctx, R.color.zeroImportance));
                break;
        }
        holder.id = notes.get(position).getId();
        holder.titleTV.setText(notes.get(position).getTitle());
        holder.editIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, EditNoteActivity.class);
                intent.putExtra(NotesActivity.NOTE_TAG, notesDb.getNotes().get(holder.getAdapterPosition()));
                ((NotesActivity) ctx).startActivityForResult(intent, NotesActivity.EDIT_NOTE_REQUEST_CODE);
            }
        });

        if (!notes.get(position).getPhoto().equals(Note.NO_PHOTO)) {
            Bitmap bitmap = memoryCache.get(notes.get(position).getPhoto());
            if (bitmap != null)
                holder.noteIV.setImageBitmap(bitmap);
            else {
                ImageLoader loader = new ImageLoader();
                loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder);
            }
            holder.noteIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent photoView = new Intent(ctx, ViewImageActivity.class);
                    photoView.putExtra(ViewImageActivity.IMAGE_SRC, notes.get(holder.getAdapterPosition()).getPhoto());
                    ctx.startActivity(photoView);
                }
            });
        } else
            holder.noteIV.setImageDrawable(null);


        holder.background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, ViewNoteActivity.class);
                intent.putExtra(NotesActivity.NOTE_TAG, notesDb.getNotes().get(holder.getAdapterPosition()));
                ((NotesActivity) ctx).startActivityForResult(intent, NotesActivity.VIEW_NOTE_REQUEST_CODE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notesDb.getNotes().size();
    }

    @Override
    public long getItemId(int position) {
        return notesDb.getNotes().get(position).getId();
    }

    private class ImageLoader extends AsyncTask<ViewHolder, Void, ImageLoader.ResultContainer> {

        private int reqWidth, reqHeight;
        private String photoPath;

        @Override
        protected void onPostExecute(ResultContainer resultContainer) {
            resultContainer.viewHolder.noteIV.setImageBitmap(resultContainer.bitmap);
            super.onPostExecute(resultContainer);
        }

        @Override
        protected void onPreExecute() {
            reqHeight = ctx.getResources().getDimensionPixelSize(R.dimen.list_item_iv_tumbnail_size);
            reqWidth = reqHeight;
        }

        @Override
        protected ResultContainer doInBackground(ViewHolder... params) {
            ViewHolder vh = params[0];
            photoPath = notesDb.getNote(vh.getId()).getPhoto();
            Bitmap bitmap = ImageFiles.decodeSampledBitmapFromFile(photoPath, reqWidth, reqHeight);

            if (memoryCache.get(photoPath) == null)
                memoryCache.put(photoPath, bitmap);

            ResultContainer resultContainer = new ResultContainer();
            resultContainer.bitmap = bitmap;
            resultContainer.viewHolder = vh;
            return resultContainer;
        }

        class ResultContainer {
            private ViewHolder viewHolder;
            private Bitmap bitmap;
        }
    }

}
