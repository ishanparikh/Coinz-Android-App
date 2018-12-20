package com.example.ishan.coinzapp;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener
{
    //firebase auth object
    private FirebaseAuth firebaseAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button buttonLogout;
    private Button buttonOpenMap;
    double distWalked;
    private FirebaseUser user;
    private TextView distance;
    public boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    @SuppressLint("SetTextI18n")
    public void getDistanceWalked(String emailID){
        db.collection("Users").document(emailID)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) {
                            TextView textViewUserEmail = findViewById(R.id.textViewUserEmail);
                            buttonLogout =  findViewById(R.id.buttonLogout);
                            buttonOpenMap = findViewById(R.id.buttonOpenMap);
                            distance = findViewById(R.id.distance);


                            //displaying logged in user name
                            assert user != null;
                            textViewUserEmail.setText("Welcome "+user.getEmail());

                            //adding listener to button
                            buttonLogout.setOnClickListener(ProfileActivity.this);

                            //listener to OpenMap
                            buttonOpenMap.setOnClickListener(ProfileActivity.this);
                            distWalked = document.getDouble("Distance");
                            distance.setText("Distance Covered:\n" + Math.round(distWalked)+"m");
                            Timber.d("DocumentSnapshot data: %s", document.getData());
                        } else {
                            Timber.d("No such document");
                        }
                    } else {
                        Timber.d(task.getException(), "get failed with ");
                    }
                });
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_profile);
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        boolean networkCheck = haveNetworkConnection();
        Timber.d("Connection Status: %s", networkCheck);
        if (!networkCheck ){
//            ConnectionStatus iia = new ConnectionStatus(getApplicationContext());
            Toast.makeText(getBaseContext(), "Please connect to a hotspot", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        //if the user is not logged in
        //that means current user will return null
        if(firebaseAuth.getCurrentUser() == null){
            //closing this activity
            finish();
            //starting login activity
            startActivity(new Intent(this, LogInActivity.class));
        }

        //getting current user
        user = firebaseAuth.getCurrentUser();

        //initializing views
        TextView textViewUserEmail = findViewById(R.id.textViewUserEmail);
        buttonLogout =  findViewById(R.id.buttonLogout);
        buttonOpenMap = findViewById(R.id.buttonOpenMap);
        distance = findViewById(R.id.distance);


        //displaying logged in user name
        assert user != null;
        textViewUserEmail.setText("Welcome "+user.getEmail());

        //adding listener to button
        buttonLogout.setOnClickListener(this);

        //listener to OpenMap
        buttonOpenMap.setOnClickListener(this);

        getDistanceWalked(user.getEmail());
        Timber.d("Distance History is :%s", distWalked);


//        String d = getIntent().getStringExtra("Distance");
//        if(d == null){
//
//
//        }else{
//            double dist = Double.parseDouble(d);
//            distance.setText("Distance Covered:\n" + Math.round(dist)+"m");
//        }



    }

    @Override
    public void onClick(View view)
    {
        if(view == buttonLogout){
            //logging out the user
            firebaseAuth.signOut();
            //closing activity
            finish();
            //starting login activity
            startActivity(new Intent(this, LogInActivity.class));
        }

        if(view == buttonOpenMap)
        {
            //finish();
            startActivity(new Intent(this, MapboxActivity.class));
        }

    }


}
