package com.example.ishan.coinzapp;

import android.util.Log;

public class DownloadCompleteRunner {
    String result;
    private String tag = "DownloadCompleteRunner";
    public void downloadComplete(String result) {
        this.result = result;
    }
     //Log.debug(tag," file downloaded" );

}
