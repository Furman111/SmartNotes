package ru.furman.smartnotes;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class ViewImageActivity extends AppCompatActivity {

    ImageViewTouch imageViewTouch;

    public static final String IMAGE_SRC = "image";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setContentView(R.layout.view_image);

        imageViewTouch = (ImageViewTouch) findViewById(R.id.iv);
        ImageLoader loader = new ImageLoader();
        loader.execute(getIntent().getStringExtra(IMAGE_SRC));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return true;
    }

    private class ImageLoader extends AsyncTask<String,Void,Bitmap> {
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageViewTouch.setImageBitmap(bitmap);
            super.onPostExecute(bitmap);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String path = params[0];
            Bitmap bitmap = ImageFiles.decodeFile(path);
            return bitmap;
        }
    }
}
