package com.example.ishan.coinzapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.MyViewHolder>  {

    private String TAG = "CoinAdapter.java";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static List<Wallet> coinList = new ArrayList<>();
    Double ShilToGold;
    Double DolrToGold;
    Double QuidToGold;
    Double PenyToGold;
    Double goldValue;
    Double goldBank;
    Double spareGold;
    public FirebaseUser user;
    // HashMap<String,TodaysMap> todaysMapList = new HashMap<String,TodaysMap>();
//    HashMap<String,Double> goldBankMap =  new HashMap<String, Double>();
//    HashMap<String,Double> spareGoldMap =  new HashMap<String, Double>();



    private final String preferencesFile = "MyPrefsFile";
    Context obj;

    public void getBankBalance(String emailID){
        db.collection("Users").document(emailID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        goldBank = Double.parseDouble(document.get("GoldBank").toString());
                        spareGold = Double.parseDouble(document.get("SpareGold").toString());
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Log.d(TAG,"GoldBank Value: "+ goldBank);
                        Log.d(TAG,"SpareGold Value: "+ spareGold);

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }
    public void bankCoin(double gold, String emailID){
        goldBank += gold;
        HashMap goldBankMap = new HashMap();
        goldBankMap.put("GoldBank",goldBank);
        db.collection("Users").document(emailID)
                .update(goldBankMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                        Log.d(TAG," Gold value: " + goldBank);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView coinCurrency, value, estGold, dateCollected;
        private Button buttonBank;


        public MyViewHolder(View view) {
            super(view);
            coinCurrency = (TextView) view.findViewById(R.id.coinCurrency);
            value = (TextView) view.findViewById(R.id.value);
            estGold = (TextView) view.findViewById(R.id.estGold);
            dateCollected = (TextView) view.findViewById(R.id.dateCollected);
            buttonBank = (Button) view.findViewById(R.id.bankButton);
        }
    }

    public  CoinAdapter(List<Wallet> coinList, Context walletActivity) {
        this.coinList = coinList;
        this.obj = walletActivity;
    }


    @Override

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.collections, parent, false);

        return new MyViewHolder(itemView);
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = user.getEmail();
//        getBankBalance(userEmail);
        SharedPreferences settings = obj.getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
        ShilToGold = Double.parseDouble(settings.getString("DolrToGold", ""));
        DolrToGold = Double.parseDouble(settings.getString("PenyToGold", ""));
        QuidToGold = Double.parseDouble(settings.getString("QuidToGold", ""));
        PenyToGold = Double.parseDouble(settings.getString("ShilToGold", ""));

        Wallet cl = coinList.get(position);
        holder.coinCurrency.setText("Currency:\n"+cl.currency);
        holder.dateCollected.setText("Date Collected:\n"+cl.date);
        holder.value.setText("Value:\n"+Double.toString(cl.value));

        getBankBalance(userEmail);

        if(cl.currency.equals("SHIL")){
            goldValue = ShilToGold * cl.value;
        }
        if(cl.currency.equals("DOLR")){
            goldValue = DolrToGold * cl.value;
        }
        if(cl.currency.equals("PENY")){
            goldValue = PenyToGold * cl.value;
        }
        if(cl.currency.equals("QUID")){
            goldValue = QuidToGold * cl.value;
        }
        holder.estGold.setText("Gold Value:\n" + goldValue);
//        holder.estGold.setText(goldValue.toString());


        holder.buttonBank.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if(cl.currency.equals("SHIL")){
                        goldValue = ShilToGold * cl.value;
                    }
                    if(cl.currency.equals("DOLR")){
                        goldValue = DolrToGold * cl.value;
                    }
                    if(cl.currency.equals("PENY")){
                        goldValue = PenyToGold * cl.value;
                    }
                    if(cl.currency.equals("QUID")){
                        goldValue = QuidToGold * cl.value;
                    }

////                    double goldcoins = Double.parseDouble(holder.estGold.getText().toString());
                    bankCoin(goldValue,userEmail);

                    Log.d(TAG,"Banking Coin with Gold value: " + goldValue);
//                    // delete coin from wallet
                    
//                    // delete coin from recyclerView

//                    Log.d(TAG,)

//                    ((YourActivityName)myContext).yourDesiredMethod(position);
                }
            }
        );

    }

    @Override
    public int getItemCount() {
        return coinList.size();
    }





}

