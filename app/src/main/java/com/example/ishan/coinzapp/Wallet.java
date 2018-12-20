package com.example.ishan.coinzapp;


/**
 * Class for individual coins
 */
public class Wallet {
    String date;
    String currency;
    String id;
    double value;
    Wallet(String d, String curr, String coinId, double val){
        date = d;
        currency = curr;
        id = coinId;
        value = val;

    }



}
