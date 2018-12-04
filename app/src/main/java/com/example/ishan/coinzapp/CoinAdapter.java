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
import java.util.Objects;

import timber.log.Timber;

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.MyViewHolder>  {

    private String TAG = "CoinAdapter.java";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static List<Wallet> coinList = new ArrayList<>();
    private double ShilToGold ;
    private double DolrToGold ;
    private double QuidToGold ;
    private double PenyToGold ;
    private double goldValue;
    private double goldBank;
    private double spareGold;
    public String date;
    private FirebaseUser user;
    private int bankCounter;
    private final String preferencesFile = "MyPrefsFile";
    private Context obj;

    public class NotLoggingTree extends Timber.Tree {
        @Override
        protected void log(final int priority, final String tag, final String message, final Throwable throwable) {

        }
    }

    private void getBankBalance(String emailID){
        db.collection("Users").document(emailID)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            goldBank = document.getDouble("GoldBank");
                            spareGold = document.getDouble("SpareGold");
                            Timber.d("DocumentSnapshot data: %s", document.getData());
                            Timber.d("GoldBank Value: %s", goldBank);
                            Timber.d("SpareGold Value: %s", spareGold);

                        } else {
                            Timber.d("No such document");
                        }
                    } else {
                        Timber.d(task.getException(), "get failed with ");
                    }
                });

    }
    public void bankCoin(double gold, String emailID,String coinID,int position){
        goldBank += gold;
        HashMap goldBankMap = new HashMap();
        goldBankMap.put("GoldBank",goldBank);
        db.collection("Users").document(emailID)
                .update(goldBankMap)
                .addOnSuccessListener((OnSuccessListener<Void>) aVoid -> {
                    // Delete coin from Wallet
                    deleteFromWallet(emailID,coinID);

                    coinList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, coinList.size());


                    Timber.d("DocumentSnapshot successfully updated!");
                    Timber.d(" Gold value: %s", goldBank);
                })
                .addOnFailureListener(e -> Timber.tag(TAG).w(e, "Error updating document"));

    }


    private void depositGift(double receivedAmt, String emailID){

        HashMap giftMap = new HashMap();
        giftMap.put("GoldBank",receivedAmt);
        db.collection("Users").document(emailID)
                .update(giftMap)
                .addOnSuccessListener((OnSuccessListener<Void>) aVoid -> {


                    Timber.d("Gift has been sent successfully");
                    Timber.d(" Gold value: %s", receivedAmt);
                })
                .addOnFailureListener(e -> Timber.tag(TAG).w(e, "Gift has NOT been sent"));

    }





    public void addToSpareGold(double gold, String emailID,String coinID){
        spareGold += gold;
        HashMap spareGoldMap = new HashMap();
        spareGoldMap.put("SpareGold",spareGold);
        db.collection("Users").document(emailID)
                .update(spareGoldMap)
                .addOnSuccessListener((OnSuccessListener<Void>) aVoid -> {
                    // Delete coin from Wallet
                    deleteFromWallet(emailID,coinID);

                    Timber.d("DocumentSnapshot successfully updated!");
                    Timber.d(" Gold value: %s", spareGold);
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.tag(TAG).w(e, "Error updating document");
                    }
                });

    }


    public void deleteFromWallet(String emailID,String id){
        db.collection("Users").document(emailID).collection("Wallet").document(id)
                .delete()
                .addOnSuccessListener(aVoid -> Timber.d("Banked coin removed from wallet"))
                .addOnFailureListener(e -> Timber.tag(TAG).w(e, "Error deleting document"));
    }

    public void giftCoin(double gold, String userID,String giftID,String coinID,int position){
        db.collection("Users").document(giftID)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) {
                            deleteFromWallet(userID,coinID);
                            double receiverBank = document.getDouble("GoldBank");
                            receiverBank += gold;
                            depositGift(receiverBank,giftID);
                            coinList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, coinList.size());

                        } else {

                            Timber.d("No such document");
                            Toast.makeText(obj, "Your Friend doesn't play this game.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Timber.d(task.getException(), "get failed with ");
                    }
                });


    }

    public void getBankCounter(double gold, String emailID,String coinID, int pos){
        db.collection("Users").document(emailID)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) {
                            bankCounter = Integer.parseInt(Objects.requireNonNull(document.get("BankCounter")).toString());

                            bankCounter +=1;
                            document.getReference().update("BankCounter", bankCounter).addOnSuccessListener(aVoid -> {
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
                            })
                            .addOnFailureListener(e -> Timber.tag(TAG).w(e, "Error incrementing counter"));

                            Timber.d("DocumentSnapshot data: %s", document.getData());

                        } else {
                            Timber.d("No such document");
                        }
                    } else {
                        Timber.d(task.getException(), "get failed with ");
                    }
                });

    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView coinCurrency, value, estGold, dateCollected;
        private Button buttonBank;
        private Button gift;


        MyViewHolder(View view) {
            super(view);
            coinCurrency =  view.findViewById(R.id.coinCurrency);
            value =  view.findViewById(R.id.value);
            estGold =  view.findViewById(R.id.estGold);
            dateCollected =  view.findViewById(R.id.dateCollected);
            buttonBank =  view.findViewById(R.id.bankButton);
            gift =  view.findViewById(R.id.gift);
        }
    }

    CoinAdapter(List<Wallet> coinList, Context walletActivity) {
        CoinAdapter.coinList = coinList;
        this.obj = walletActivity;
    }


    @NonNull
    @Override

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.plant(new Timber.DebugTree());
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


        holder.buttonBank.setOnClickListener(v -> {


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
            getBankCounter(goldValue,userEmail,coin.id,position);
            Timber.d("Banking Coin with Gold value: %s", goldValue);

        });

        holder.gift.setOnClickListener(v -> {
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

            builder.setPositiveButton("Send Gift", (dialog, which) -> {
                AlertDialog test = (AlertDialog) dialog;
                EditText userinput = test.findViewById(R.id.emailID);
                        String giftEmail = userinput.getText().toString();
                giftCoin(goldValue,userEmail,giftEmail,coin.id,position);

            });
            builder.setNegativeButton("Cancel", (dialog, which) -> {
            });
            builder.create().show();
        });

    }

    @Override
    public int getItemCount() {
        return coinList.size();
    }
}

