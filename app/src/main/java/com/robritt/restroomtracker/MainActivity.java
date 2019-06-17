package com.robritt.restroomtracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button registerButton = (Button) findViewById(R.id.welcome_register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,RegisterScreen.class));
            }
        });

//        mAuthListener = new FirebaseAuth.AuthStateListener() { //TODO is this needed?
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                if (mAuth.getCurrentUser() != null){
//                    startActivity(new Intent(MainActivity.this, MainMap.class));
//                }
//            }
//        };



    }
    @Override
    public void onStart() { //checks to see if the user is logged in, if he is take him to map
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void openLoginScreen(View view){
        Intent intentLogin = new Intent(this, LoginScreen.class);
        startActivity(intentLogin);
    }

    public void openRegisterScreen(View view){
        Intent intentRegister= new Intent(this, RegisterScreen.class);
        startActivity(intentRegister);
    }

    private void updateUI(FirebaseUser user){
        if(user != null){ //user is signed in
            Intent intent = new Intent(this, MainMap.class);
            startActivity(intent);
        }
        else{
            //user has not signed in to the app, no redirect
        }

    }






}

