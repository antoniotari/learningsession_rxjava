package com.antoniotari.java8learning;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import static com.antoniotari.java8learning.MainActivity.downloadImage;

/**
 * Created by antonio on 11/06/15.
 */
public class DownloadImageTask extends AsyncTask<String,Void,Drawable> {

    ImageDownloadCallback callback;

    DownloadImageTask(ImageDownloadCallback callback){
        this.callback=callback;
    }

    @Override
    protected Drawable doInBackground(final String... params) {
        BitmapDrawable drawable= null;
        try {
            Bitmap bitmap = downloadImage(params[0]);
            drawable = new BitmapDrawable(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    callback.onDownloadError(e);
                }
            });
        }
        return drawable;
    }

    @Override
    protected void onPostExecute(final Drawable drawable) {
        super.onPostExecute(drawable);
        if(drawable!=null){
            callback.onImageDownload(drawable);
        } else {
        }
        callback.onCompleted();
    }
}
