package com.example.ishan.coinzapp;

import android.location.Location;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.Date;

public class TodaysMap {

    String date;
    String currency;
    String id;
    double value;
    LatLng loc;
    TodaysMap(String d, String curr, String coinId, double val,LatLng l){
        date = d;
        currency = curr;
        id = coinId;
        value = val;
        loc = l;

    }


}
