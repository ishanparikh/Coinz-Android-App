package com.example.ishan.coinzapp;


import com.mapbox.mapboxsdk.geometry.LatLng;

public class TodaysMap {

    public String date;
    public String currency;
    public String id;
    public double value;
    public LatLng loc;
    public String symbol;
    TodaysMap(String d, String curr, String coinId, double val,LatLng l,String sym){
        date = d;
        currency = curr;
        id = coinId;
        value = val;
        loc = l;
        symbol = sym;

    }
    TodaysMap(){};
}
