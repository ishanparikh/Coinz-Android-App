package com.example.ishan.coinzapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import android.content.res.Resources;
import android.graphics.Rect;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class WalletActivity extends AppCompatActivity   {
    private RecyclerView recyclerView;
    private CoinAdapter coinAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static List<Wallet> coinList = new ArrayList<>();
    private String TAG = "WalletActivity.java";
    private FirebaseUser user;
    TextView storeLoc;
    TextView coinsLeft;
    ImageButton homeButton;
//    public FirebaseUser user;


    public void getData() {

         db.collection("Users").document(user.getEmail()).collection("Wallet")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                String curr = document.get("currency").toString();
                                String date = document.get("date").toString();
                                Double vals = (Double) document.get("value");

                                Wallet coin = new Wallet(date, curr,document.getId(),vals);
                                coinList.add(coin);
                            }
                            coinAdapter = new CoinAdapter(coinList, WalletActivity.this);
                            recyclerView.setAdapter(coinAdapter);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        setContentView(R.layout.activity_wallet);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.addItemDecoration(new GridSpacingItemDecoration(, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        storeLoc = (TextView) findViewById(R.id.StoreLoc);
        coinsLeft = (TextView) findViewById(R.id.CoinsLeft);
        homeButton = (ImageButton) findViewById(R.id.backToWalletActivity);

        homeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
//                this.
                startActivity(new Intent(WalletActivity.this, MapboxActivity.class));
            }
        });
//        if (user != null) {
//            db.collection("Users").document(user.getEmail())
//                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot document = task.getResult();
//                        if (document.exists()) {
//                            int bankCounter = Integer.parseInt(document.get("BankCounter").toString());
//                            if(bankCounter < 25){
//                                storeLoc.setText("Store Location: Bank");
//                                coinsLeft.setText("Coins left to bank: "+(25-bankCounter));
//                            }
//                            else {
//                                storeLoc.setText("Store Location: Spare Gold");
//                                coinsLeft.setText("Coins left to bank: 0");
//
//                            }
//                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//                        } else {
//                            Log.d(TAG, "No such document");
//                        }
//                    } else {
//                        Log.d(TAG, "get failed with ", task.getException());
//                    }
//                }
//            });
//
//        }



//     textViewSignup  = (TextView) findViewById(R.id.textViewSignUp);

        getData();

//        coinAdapter = new CoinAdapter(cl);
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
//        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.setAdapter(coinAdapter);
//        coinAdapter.notifyDataSetChanged();
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }


}
