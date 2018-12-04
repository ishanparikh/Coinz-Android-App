package com.example.ishan.coinzapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    double ShilToGold;
    double DolrToGold;
    double QuidToGold;
    double PenyToGold;
    double goldValue;
    double goldBank;
    double spareGold;
    public String date;
    public FirebaseUser user;
    public int bankCounter;
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
                        goldBank = document.getDouble("GoldBank");
                        spareGold = document.getDouble("SpareGold");
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
    public void bankCoin(double gold, String emailID,String coinID,int position){
        goldBank += gold;
        HashMap goldBankMap = new HashMap();
        goldBankMap.put("GoldBank",goldBank);
        db.collection("Users").document(emailID)
                .update(goldBankMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Delete coin from Wallet
                        deleteFromWallet(emailID,coinID);

                        coinList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, coinList.size());


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


    public void depositGift(double receivedAmt, String emailID){

        HashMap giftMap = new HashMap();
        giftMap.put("GoldBank",receivedAmt);
        db.collection("Users").document(emailID)
                .update(giftMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                        Log.d(TAG, "Gift has been sent successfully");
                        Log.d(TAG," Gold value: " + receivedAmt);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Gift has NOT been sent", e);
                    }
                });

    }





    public void addToSpareGold(double gold, String emailID,String coinID){
        spareGold += gold;
        HashMap spareGoldMap = new HashMap();
        spareGoldMap.put("SpareGold",spareGold);
        db.collection("Users").document(emailID)
                .update(spareGoldMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Delete coin from Wallet
                        deleteFromWallet(emailID,coinID);

                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                        Log.d(TAG," Gold value: " + spareGold);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

    }


    public void deleteFromWallet(String emailID,String id){
        db.collection("Users").document(emailID).collection("Wallet").document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Banked coin removed from wallet"  );
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    public void giftCoin(double gold, String userID,String giftID,String coinID,int position){
        db.collection("Users").document(giftID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        deleteFromWallet(userID,coinID);
                        double receiverBank = document.getDouble("GoldBank");
                        receiverBank += gold;
                        depositGift(receiverBank,giftID);
                        coinList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, coinList.size());

                    } else {

                        Log.d(TAG, "No such document");
                        Toast.makeText(obj, "Your Friend doesn't play this game.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }

    public void getBankCounter(double gold, String emailID,String coinID, int pos){
        db.collection("Users").document(emailID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        bankCounter = Integer.parseInt(document.get("BankCounter").toString());

                        bankCounter +=1;
                        document.getReference().update("BankCounter", bankCounter).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                TextView storeLoc = ((Activity)obj).findViewById(R.id.StoreLoc);
                                TextView coinsLeft = ((Activity)obj).findViewById(R.id.CoinsLeft);

                                if (bankCounter < 25) {
                                    bankCoin(goldValue,emailID,coinID,pos);
                                    storeLoc.setText("Storing In: Bank");
                                    coinsLeft.setText("Coins Banked: "+(bankCounter));
                                }
                                else {
                                    // put in spare change
//                                    addToSpareGold(goldValue,emailID,coinID);
                                    Toast.makeText(obj, "Cannot Bank any more coins", Toast.LENGTH_SHORT).show();
                                    storeLoc.setText("Daily Quota Reached");
                                    coinsLeft.setText("Coins left to bank: 0");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error incrementing counter", e);
                            }
                        });

                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView coinCurrency, value, estGold, dateCollected;
        private Button buttonBank;
        private Button gift;


        public MyViewHolder(View view) {
            super(view);
            coinCurrency = (TextView) view.findViewById(R.id.coinCurrency);
            value = (TextView) view.findViewById(R.id.value);
            estGold = (TextView) view.findViewById(R.id.estGold);
            dateCollected = (TextView) view.findViewById(R.id.dateCollected);
            buttonBank = (Button) view.findViewById(R.id.bankButton);
            gift = (Button) view.findViewById(R.id.gift);
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
        getBankBalance(userEmail);
        SharedPreferences settings = obj.getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
        ShilToGold = Double.parseDouble(settings.getString("DolrToGold", ""));
        DolrToGold = Double.parseDouble(settings.getString("PenyToGold", ""));
        QuidToGold = Double.parseDouble(settings.getString("QuidToGold", ""));
        PenyToGold = Double.parseDouble(settings.getString("ShilToGold", ""));

        Wallet coin = coinList.get(position);
        holder.coinCurrency.setText("Currency:\n"+coin.currency);
        holder.dateCollected.setText("Date Collected:\n"+coin.date);
        holder.value.setText("Value:\n"+Double.toString(coin.value));



        if(coin.currency.equals("SHIL")){
            goldValue = ShilToGold * coin.value;
        }
        if(coin.currency.equals("DOLR")){
            goldValue = DolrToGold * coin.value;
        }
        if(coin.currency.equals("PENY")){
            goldValue = PenyToGold * coin.value;
        }
        if(coin.currency.equals("QUID")){
            goldValue = QuidToGold * coin.value;
        }
        holder.estGold.setText("Gold Value:\n" + goldValue);
//        holder.estGold.setText(goldValue.toString());


        holder.buttonBank.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if(coin.currency.equals("SHIL")){
                        goldValue = ShilToGold * coin.value;
                    }
                    if(coin.currency.equals("DOLR")){
                        goldValue = DolrToGold * coin.value;
                    }
                    if(coin.currency.equals("PENY")){
                        goldValue = PenyToGold * coin.value;
                    }
                    if(coin.currency.equals("QUID")){
                        goldValue = QuidToGold * coin.value;
                    }

////                    double goldcoins = Double.parseDouble(holder.estGold.getText().toString());
//                    bankCoin(goldValue,userEmail,cl.id);
                    getBankCounter(goldValue,userEmail,coin.id,position);

                    Log.d(TAG,"Banking Coin with Gold value: " + goldValue);
//                    coinList.remove(position);
//                    notifyItemRemoved(position);
//                    notifyItemRangeChanged(position, coinList.size());

                }
        });

        holder.gift.setOnClickListener( new View.OnClickListener() {

            LayoutInflater li = LayoutInflater.from(obj);
            View promtsView = li.inflate(R.layout.gift_layout,null, false);

            @Override
            public void onClick(View v) {
                if(coin.currency.equals("SHIL")){
                    goldValue = ShilToGold * coin.value;
                }
                if(coin.currency.equals("DOLR")){
                    goldValue = DolrToGold * coin.value;
                }
                if(coin.currency.equals("PENY")){
                    goldValue = PenyToGold * coin.value;
                }
                if(coin.currency.equals("QUID")){
                    goldValue = QuidToGold * coin.value;
                }

                // Open dialogue for email address

                AlertDialog.Builder builder = new AlertDialog.Builder(obj)
                        .setTitle("Gifting Coin")
                        .setCancelable(true)
                        .setView(R.layout.gift_layout);
//                final EditText userInp = (EditText) promtsView
//                        .findViewById(R.id.emailID);
//                String giftEmail = userInp.getText().toString();
//                        AlertDialog alertDialog = builder.create();
//                alertDialog.show();
                builder.setPositiveButton("Send Gift", (dialog, which) -> {
                    AlertDialog test = (AlertDialog) dialog;
                    EditText userinput = test.findViewById(R.id.emailID);
                            String giftEmail = userinput.getText().toString();
                    giftCoin(goldValue,userEmail,giftEmail,coin.id,position);
//                    coinList.remove(position);
//                    notifyItemRemoved(position);
//                    notifyItemRangeChanged(position, coinList.size());
//
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> {
//                        alertDialog.dismiss();
                });
                builder.create().show();



            }

        });

    }

    @Override
    public int getItemCount() {
        return coinList.size();
    }





}

