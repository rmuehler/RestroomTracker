package com.robritt.restroomtracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
    public void onStart() { //set UI based on logged in user
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

    private void updateUI(FirebaseUser user){//checks to see if the user is logged in, if he is take him to map
        if(user != null){ //user is signed in
            Intent intent = new Intent(this, MainMap.class);
            startActivity(intent);
        }
        else{
            //user has not signed in to the app, no redirect
        }

    }


    public void guestSignIn(View view){
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("ACCOUNT", "signInAnonymously:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("ACCOUNT", "signInAnonymously:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }

            }
        });

    }






}

