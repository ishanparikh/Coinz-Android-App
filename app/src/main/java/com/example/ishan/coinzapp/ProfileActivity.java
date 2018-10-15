package com.example.ishan.coinzapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener
{
    //firebase auth object
    private FirebaseAuth firebaseAuth;

    //view objects
    private TextView textViewUserEmail;
    private Button buttonLogout;
    private Button buttonOpenMap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //initializing views
        textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);
        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        buttonOpenMap = (Button) findViewById(R.id.buttonOpenMap);

        //displaying logged in user name
        textViewUserEmail.setText("Welcome "+user.getEmail());

        //adding listener to button
        buttonLogout.setOnClickListener(this);

        //listener to OpenMap
        buttonOpenMap.setOnClickListener(this);
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
