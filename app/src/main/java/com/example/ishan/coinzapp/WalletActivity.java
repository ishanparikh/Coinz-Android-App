package com.example.ishan.coinzapp;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.List;
import java.util.Objects;
import android.widget.ImageButton;
import android.widget.TextView;
import timber.log.Timber;

public class WalletActivity extends AppCompatActivity   {
    public RecyclerView recyclerView;
    public CoinAdapter coinAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static List<Wallet> coinList = new ArrayList<>();
    public FirebaseUser user;
    TextView storeLoc;
    TextView coinsLeft;
    ImageButton homeButton;

    public class NotLoggingTree extends Timber.Tree {
        @Override
        protected void log(final int priority, final String tag, final String message, final Throwable throwable) {

        }
    }


    public void getData() {

         db.collection("Users").document(Objects.requireNonNull(user.getEmail())).collection("Wallet")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Timber.d(document.getId() + " => " + document.getData());
                            String curr = Objects.requireNonNull(document.get("currency")).toString();
                            String date = Objects.requireNonNull(document.get("date")).toString();
                            Double vals = (Double) document.get("value");

                            Wallet coin = new Wallet(date, curr,document.getId(),vals);
                            coinList.add(coin);
                        }
                        coinAdapter = new CoinAdapter(coinList, WalletActivity.this);
                        recyclerView.setAdapter(coinAdapter);
                    } else {
                        Timber.d(task.getException(), "Error getting documents: ");
                    }
                });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        user = FirebaseAuth.getInstance().getCurrentUser();
        setContentView(R.layout.activity_wallet);
        recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        storeLoc =  findViewById(R.id.StoreLoc);
        coinsLeft = findViewById(R.id.CoinsLeft);
        homeButton = findViewById(R.id.backToWalletActivity);

        homeButton.setOnClickListener(v -> {
            // Code here executes on main thread after user presses button
//                this.
            startActivity(new Intent(WalletActivity.this, MapboxActivity.class));
        });
        getData();

    }

}
