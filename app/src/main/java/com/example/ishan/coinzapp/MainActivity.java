package com.example.ishan.coinzapp;


import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignIn;
    private String TAG = "MainActivity.java";
    private ProgressDialog progressDialog;

    // [START declare_auth]
    private FirebaseAuth firebaseAuth;
    // [END declare_auth]

    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public FirebaseUser user;
    public boolean networkCheck;

    public class NotLoggingTree extends Timber.Tree {
        @Override
        protected void log(final int priority, final String tag, final String message, final Throwable throwable) {

        }
    }

    /**
     * Method to check if the device has active internet connection*/
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_main);
        networkCheck = haveNetworkConnection();
        Timber.d("Connection Status: %s", networkCheck);
        if (!networkCheck ){
            Toast.makeText(getBaseContext(), "Please connect to a WIFI network", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            networkCheck = haveNetworkConnection();
        }

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
        buttonRegister =  findViewById(R.id.buttonRegister);
        progressDialog = new ProgressDialog(this);
        editTextEmail =  findViewById(R.id.editTextEmail);
        editTextPassword =  findViewById(R.id.editTextPassword);
        textViewSignIn = findViewById(R.id.textViewSignIn);
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

        /*
         * Setting up the initial user account with a gold bank, Spare gold,
         * LastDownloadDate and Distance covered
         * */

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful())
                    {
                        user = firebaseAuth.getCurrentUser();
                        HashMap<String, Object> info = new HashMap<>();
                        info.put("GoldBank", 0.0);
                        info.put("SpareGold", 0.0);
                        info.put("LastDownloadDate", "yyyy/mm/dd");
                        info.put("Distance",0.0);

                        db.collection("Users").document(Objects.requireNonNull(user.getEmail()))
                                .set(info)
                                .addOnSuccessListener(aVoid -> Timber.d("Gold Bank initialised "))
                                .addOnFailureListener(e -> Timber.tag(TAG).w(e, "Error, Gold Bank NOT initialised"));

                            finish();
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Registration not done, try again",Toast.LENGTH_SHORT).show();

                    }
                    progressDialog.dismiss();

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
