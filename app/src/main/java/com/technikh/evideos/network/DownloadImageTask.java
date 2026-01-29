package com.technikh.evideos.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.technikh.evideos.activities.SlideShowActivity;
import com.technikh.evideos.activities.SlideshowSplashScreen;
import com.technikh.evideos.preferences.SlideshowSharedPreferences;
import com.technikh.evideos.util.ImagesCache;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.ContentValues.TAG;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private String imageUrl;
    private BaseAdapter adapter;
    private ImagesCache cache;
    private int desiredWidth, desiredHeight;
    private ImageView ivImageView;
    private static SlideshowSplashScreen _splashScreen;
    private Context context;

    public DownloadImageTask(BaseAdapter adapter, int desiredWidth, int desiredHeight) {
        this.adapter = adapter;
        this.cache = ImagesCache.getInstance();
        this.desiredWidth = desiredWidth;
        this.desiredHeight = desiredHeight;
    }

    public DownloadImageTask(Context context, ImagesCache cache, ImageView ivImageView, int desireWidth, int desireHeight, SlideshowSplashScreen splashScreen) {
        this.context = context;
        this.cache = (cache != null) ? cache : ImagesCache.getInstance();
        this.ivImageView = ivImageView;
        this.desiredHeight = desireHeight;
        this.desiredWidth = desireWidth;
        _splashScreen = splashScreen;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        imageUrl = params[0];
        return getImage(imageUrl);
    }

    @Override
    protected void onPostExecute(Bitmap results) {
        super.onPostExecute(results);

        if (results != null) {
            if (cache != null) {
                cache.addImageToWarehouse(imageUrl, results);
            }
            SlideshowSharedPreferences.SaveBitmap(context, results, imageUrl);

            if (ivImageView != null) {
                ivImageView.setImageBitmap(results);
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        // Logic to move from Splash to SlideShow
        if (ImagesCache.imagesWarehouse != null && _splashScreen != null) {
            if (ImagesCache.imagesWarehouse.putCount() >= SlideshowSplashScreen.CountValue) {
                SlideshowSharedPreferences.IMAGES_SAVED(context, true);
                Intent i = new Intent(_splashScreen, SlideShowActivity.class);
                _splashScreen.startActivity(i);
                _splashScreen.finish();
            }
        }
    }

    private Bitmap getImage(String imageUrl) {
        if (cache == null) cache = ImagesCache.getInstance();

        // Safety check to prevent the "n" error you saw
        Bitmap cachedBitmap = cache.getImageFromWarehouse(imageUrl);
        if (cachedBitmap != null) return cachedBitmap;

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.connect();

            InputStream stream = connection.getInputStream();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, null, options);
            stream.close();

            // Re-open stream for actual decode
            connection = (HttpURLConnection) url.openConnection();
            stream = connection.getInputStream();

            options.inSampleSize = calculateInSampleSize(options, desiredWidth, desiredHeight);
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
            stream.close();
            return bitmap;
        } catch (Exception e) {
            Log.e("getImage", "Error: " + e.toString());
            return null;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}