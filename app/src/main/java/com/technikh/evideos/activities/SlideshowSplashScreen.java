package com.technikh.evideos.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.gson.Gson;
import com.technikh.evideos.R;
import com.technikh.evideos.app.MyApplication;
import com.technikh.evideos.models.slideshow.SlideshowJsonModel;
import com.technikh.evideos.models.slideshow.lineMedia;
import com.technikh.evideos.network.SlideshowGetDataService;
import com.technikh.evideos.network.SlideshowRetrofitInstance;
import com.technikh.evideos.preferences.SlideshowSharedPreferences;
import com.technikh.evideos.util.ImagesCache;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SlideshowSplashScreen extends AppCompatActivity {
    HashMap<String, Boolean> downloadStatus = new HashMap<String, Boolean>();
    private SlideshowJsonModel data;
    private ImageView imageView;
    private ImagesCache cache;
    public static int CountValue = 0;
    private int processedCount = 0; // NEW: Track processed items (success + fail)
    HttpProxyCacheServer cacheProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cacheProxy = MyApplication.getProxy(SlideshowSplashScreen.this);
        imageView = findViewById(R.id.Image);

        // REMOVED ProviderInstaller: It was causing the SecurityException/Developer Error on SDK 34

        CountValue = 0;
        processedCount = 0;

        downloadStatus = (HashMap) SlideshowSharedPreferences.getPreferenceObjectDownloadStatus(SlideshowSplashScreen.this);
        if (downloadStatus == null) {
            downloadStatus = new HashMap<>();
        }

        boolean showSplashScreen = downloadStatus.isEmpty();
        if (!showSplashScreen) {
            for (Boolean value : downloadStatus.values()) {
                if (!value) {
                    showSplashScreen = true;
                    break;
                }
            }
        }

        if (showSplashScreen) {
            new LoadJSON().execute("");
        } else {
            proceedToNextActivity();
        }
    }

    private void LoadImages() {
        cache = ImagesCache.getInstance();
        cache.initializeCache();
        boolean foundImages = false;

        if (data == null || data.getSlides() == null) return;

        // Collect all URLs first to know the total count
        for (int i = 0; i < data.getSlides().size(); i++) {
            if (data.getSlides().get(i).getBackgrounds() != null) {
                for (int b = 0; b < data.getSlides().get(i).getBackgrounds().size(); b++) {
                    String url = data.getSlides().get(i).getBackgrounds().get(b).getUrl();
                    if (url != null && !url.isEmpty()) {
                        addImageInCache(url);
                        foundImages = true;
                    }
                }
            }
            if (data.getSlides().get(i).getLines() != null) {
                for (int l = 0; l < data.getSlides().get(i).getLines().size(); l++) {
                    List<lineMedia> dImages = data.getSlides().get(i).getLines().get(l).getImages();
                    if (dImages != null) {
                        for (int img = 0; img < dImages.size(); img++) {
                            if (dImages.get(img).getMediaFile() != null && dImages.get(img).getMediaFile().getImage() != null) {
                                String url = dImages.get(img).getMediaFile().getImage().getUrl();
                                addImageInCache(url);
                                foundImages = true;
                            }
                        }
                    }
                }
            }
        }

        if (!foundImages) proceedToNextActivity();
    }

    private void addImageInCache(final String url) {
        if (downloadStatus.get(url) == null) {
            downloadStatus.put(url, false);
        }
        CountValue++;

        Glide.with(this)
                .asBitmap()
                .load(url)
                .override(600, 600)
                .timeout(15000)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (imageView != null) imageView.setImageBitmap(resource);
                        cache.addBitmapToCache(url, resource);
                        downloadStatus.put(url, true);
                        checkCompletion();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) { }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Log.e("GLIDE_ERROR", "Skipping dead link: " + url);
                        // Mark as true (processed) so the app doesn't hang on dead links
                        downloadStatus.put(url, true);
                        checkCompletion();
                    }
                });
    }

    private synchronized void checkCompletion() {
        processedCount++;
        // Save status periodically
        SlideshowSharedPreferences.setPreferenceObject(SlideshowSplashScreen.this, downloadStatus, "downloadStatus");

        // If all items in the current batch are processed, move forward
        if (processedCount >= CountValue) {
            runOnUiThread(() -> proceedToNextActivity());
        }
    }

    private void proceedToNextActivity() {
        SlideshowSharedPreferences.IMAGES_SAVED(SlideshowSplashScreen.this, true);
        String json_string = new Gson().toJson(data);
        Intent intent = new Intent(SlideshowSplashScreen.this, SlideShowActivity.class);
        intent.putExtra("json_string", json_string);
        startActivity(intent);
        finish();
    }

    public void loadData() {
        Retrofit retrofit = SlideshowRetrofitInstance.getRetrofitInstance();
        SlideshowGetDataService service = retrofit.create(SlideshowGetDataService.class);
        service.getAllJson().enqueue(new Callback<SlideshowJsonModel>() {
            @Override
            public void onResponse(Call<SlideshowJsonModel> call, Response<SlideshowJsonModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data = response.body();
                    SlideshowSharedPreferences.setPreferenceObject(SlideshowSplashScreen.this, data, "data");
                    LoadImages();
                } else {
                    Log.e("API_ERROR", "Response failed");
                }
            }
            @Override
            public void onFailure(Call<SlideshowJsonModel> call, Throwable t) {
                Log.e("API_ERROR", "Retrofit Failed: " + t.getMessage());
            }
        });
    }

    private class LoadJSON extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            loadData();
            return "";
        }
    }
}