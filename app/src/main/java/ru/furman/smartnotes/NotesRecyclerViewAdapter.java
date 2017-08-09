package ru.furman.smartnotes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.furman.smartnotes.database.DB;

public class NotesRecyclerViewAdapter extends RecyclerView.Adapter<NotesRecyclerViewAdapter.ViewHolder> {

    private DB db;
    private Context ctx;
    private LruCache<String, Bitmap> memoryCache;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView backgroundCV;
        public TextView titleTV;
        public ImageView editIV;
        public ImageView noteIV;
        private int id;

        public int getId() {
            return id;
        }

        public ViewHolder(View view) {
            super(view);
            this.backgroundCV = (CardView) view.findViewById(R.id.background);
            this.titleTV = (TextView) view.findViewById(R.id.note_title);
            this.editIV = (ImageView) view.findViewById(R.id.edit_btn);
            this.noteIV = (ImageView) view.findViewById(R.id.note_image);
        }
    }

    public NotesRecyclerViewAdapter(Context ctx) {
        this.db = new DB(ctx);
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
    public void onBindViewHolder(final NotesRecyclerViewAdapter.ViewHolder holder, final int position) {
        final List<Note> notes = db.getNotes();
        switch (notes.get(position).getImportance()) {
            case Note.GREEN_IMPORTANCE:
                holder.backgroundCV.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.greenImportance));
                break;
            case Note.RED_IMPORTANCE:
                holder.backgroundCV.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.redImportance));
                break;
            case Note.YELLOW_IMPORTANCE:
                holder.backgroundCV.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.yellowImportance));
                break;
            case Note.NO_IMPORTANCE:
                holder.backgroundCV.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.zeroImportance));
                break;
        }
        holder.id = notes.get(position).getId();
        holder.titleTV.setText(notes.get(position).getTitle());
        holder.editIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, EditNoteActivity.class);
                intent.putExtra(MainActivity.NOTE_TAG, db.getNotes().get(holder.getAdapterPosition()));
                ((MainActivity) ctx).startActivityForResult(intent, MainActivity.EDIT_NOTE_REQUEST_CODE);
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
                    photoView.putExtra(ViewImageActivity.IMAGE_SRC, notes.get(position).getPhoto());
                    ctx.startActivity(photoView);
                }
            });
        } else
            holder.noteIV.setImageDrawable(null);


        holder.backgroundCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, ViewNoteActivity.class);
                intent.putExtra(MainActivity.NOTE_TAG, db.getNotes().get(holder.getAdapterPosition()));
                ((MainActivity) ctx).startActivityForResult(intent, MainActivity.VIEW_NOTE_REQUEST_CODE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return db.getNotes().size();
    }

    @Override
    public long getItemId(int position) {
        return db.getNotes().get(position).getId();
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
            reqHeight = ctx.getResources().getDimensionPixelSize(R.dimen.image_view_tumbnail);
            reqWidth = reqHeight;
        }

        @Override
        protected ResultContainer doInBackground(ViewHolder... params) {
            ViewHolder vh = params[0];
            photoPath = db.getNote(vh.getId()).getPhoto();
            Bitmap bitmap = ImageSampler.decodeSampledBitmapFromFile(photoPath, reqWidth, reqHeight);

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
