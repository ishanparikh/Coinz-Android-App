package com.example.ishan.coinzapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.Manifest;
import com.google.firebase.auth.FirebaseUser;
//import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;


import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignIn;
    private MapView mapView;

    private ProgressDialog progressDialog;

    // [START declare_auth]
    private FirebaseAuth firebaseAuth;
    // [END declare_auth]


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth =  FirebaseAuth.getInstance();

        //if getCurrentUser does not returns null
        if(firebaseAuth.getCurrentUser() != null){
            //that means user is already logged in
            //so close this activity
            finish();

            //and open profile activity
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }

        // init Views
        buttonRegister = (Button) findViewById(R.id.buttonRegister);

        progressDialog = new ProgressDialog(this);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        textViewSignIn =(TextView) findViewById(R.id.textViewSignIn);

        buttonRegister.setOnClickListener(this);
        textViewSignIn.setOnClickListener(this);

    }


    private void registerUser()
    {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email))
        {
            //email field left blank
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password))
        {
            //password field left blank
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();

            //stopping further execution
            return;
        }

        //if validations are good, progress bar will appear
        progressDialog.setMessage("Registering User");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            finish();
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Registration not done, try again",Toast.LENGTH_SHORT).show();

                        }
                        progressDialog.dismiss();

                    }
                });

    }

    public void onClick(View view)
    {
        if(view == buttonRegister)

        {
            registerUser();
        }

        if(view == textViewSignIn)
        {
            // open login activity here

            startActivity(new Intent(this, LogInActivity.class));

        }
    }

}
