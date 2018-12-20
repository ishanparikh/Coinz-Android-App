package com.example.ishan.coinzapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.support.annotation.NonNull;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import timber.log.Timber;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener{

    //defining views
    private Button buttonSignIn;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignup;

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    public class NotLoggingTree extends Timber.Tree {
        @Override
        protected void log(final int priority, final String tag, final String message, final Throwable throwable) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_log_in);

        //getting firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //if the objects getcurrentuser method is not null
        //means user is already logged in
        if(firebaseAuth.getCurrentUser() != null){
            //close this activity
            finish();
            //opening profile activity
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }



        //initializing views
        editTextEmail =  findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn =  findViewById(R.id.buttonSignin);
        textViewSignup  =  findViewById(R.id.textViewSignUp);

        progressDialog = new ProgressDialog(this);

        //attaching click listener
        buttonSignIn.setOnClickListener(this);
        textViewSignup.setOnClickListener(this);

    }

    private void userLogin()
    {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //checking if email and passwords are empty

        if (TextUtils.isEmpty(email))
        {
            //email field left blank
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password))
        {
            //password field left blank
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();

            //stopping further execution
            return;
        }

        //if validations are good, progress bar will appear
        progressDialog.setMessage("Logging User In, please wait ...");
        progressDialog.show();
        //logging in the user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    //if the task is successfull
                    if(task.isSuccessful()){
                        //start the profile activity
                        finish();
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    }
                    if(!task.isSuccessful()){
                        Toast.makeText(this, "User has not registered, Sign Up instead", Toast.LENGTH_LONG).show();
                    }
                });

    }

    @Override
    public void onClick(View view) {
        if (view ==buttonSignIn)
        {
            userLogin();
        }

        if(view == textViewSignup)
        {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}
