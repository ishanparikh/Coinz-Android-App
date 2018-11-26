package com.example.ishan.coinzapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.MyViewHolder> {

    private String TAG = "CoinAdapter.java";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static List<Wallet> coinList = new ArrayList<>();



    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView coinCurrency, value, estGold, dateCollected;


        public MyViewHolder(View view) {
            super(view);
            coinCurrency = (TextView) view.findViewById(R.id.coinCurrency);
            value = (TextView) view.findViewById(R.id.value);
            estGold = (TextView) view.findViewById(R.id.estGold);
            dateCollected = (TextView) view.findViewById(R.id.dateCollected);

        }
    }

    public  CoinAdapter(List<Wallet> coinList) {
        this.coinList = coinList;
    }

//    public void getData() {
//        db.collection("Coins")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + " => " + document.getData());
//                                String curr = document.get("currency").toString();
//                                String date = document.get("date").toString();
//                                Double vals = (Double) document.get("value");
//
//                                Wallet coin = new Wallet(date, curr,document.getId(),vals);
//                                coinList.add(coin);
//                            }
//                        } else {
//                            Log.d(TAG, "Error getting documents: ", task.getException());
//                        }
//                    }
//                });
//    }

    @Override

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.collections, parent, false);

        return new MyViewHolder(itemView);
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        Wallet cl = coinList.get(position);
        holder.coinCurrency.setText(cl.currency);
        holder.dateCollected.setText(cl.date);
        holder.estGold.setText("TODO");
        holder.value.setText(Double.toString(cl.value));
    }

    @Override
    public int getItemCount() {
        return coinList.size();
    }





}

