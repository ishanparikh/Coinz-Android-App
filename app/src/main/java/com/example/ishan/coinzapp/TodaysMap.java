package com.example.ishan.coinzapp;


import com.mapbox.mapboxsdk.geometry.LatLng;

public class TodaysMap {

    String date;
    String currency;
    String id;
    double value;
    LatLng loc;
    String symbol;
    TodaysMap(String d, String curr, String coinId, double val,LatLng l,String sym){
        date = d;
        currency = curr;
        id = coinId;
        value = val;
        loc = l;
        symbol = sym;

    }
}
