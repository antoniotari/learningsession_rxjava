package com.antoniotari.java8learning;

import android.graphics.drawable.Drawable;

/**
 * Created by antonio on 11/06/15.
 */
public interface ImageDownloadCallback {
    void onImageDownload(Drawable drawable);
    void onDownloadError(Throwable throwable);
    void onCompleted();
}
