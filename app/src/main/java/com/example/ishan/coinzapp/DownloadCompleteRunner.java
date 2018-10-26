package com.example.ishan.coinzapp;

import android.util.Log;

import com.mapbox.mapboxsdk.Mapbox;

public class DownloadCompleteRunner {
    static String result;
    public void downloadComplete(String result) {
        this.result = result;
    }
}
