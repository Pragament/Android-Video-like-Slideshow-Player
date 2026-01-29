package com.technikh.evideos.network;

import com.technikh.evideos.models.slideshow.SlideshowJsonModel;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SlideshowGetDataService {
    @GET("slideshow/original.json")
    Call<SlideshowJsonModel> getAllJson();
}