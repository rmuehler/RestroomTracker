package com.robritt.restroomtracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText mEmailField, mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login_screen);

        mEmailField = (EditText) findViewById(R.id.username_login_field);
        mPasswordField = (EditText) findViewById(R.id.password_login_field);

    }

    public void openMap(View view) { //Change later for validating login, this is just for testing purposes
        Intent intent = new Intent(this, MainMap.class);
        startActivity(intent);
    }

    public void startSignIn(final View view) {

        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) { //if no email or password provided
            Toast.makeText(LoginScreen.this, "Please complete all fields.", Toast.LENGTH_LONG).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) { //if signup successful
                        Log.d("USER LOGIN", "loginUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(view,user);


                    } else { //if signup fails
                        Log.w("USER LOGIN", "loginUserWithEmail:failure", task.getException());
                        Toast.makeText(LoginScreen.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }

                }//end onComplete, when firebase is give the email and password
            });

        }
    }

    private void updateUI(View view,FirebaseUser user) {//checks to see if the user is logged in, if he is take him to map
        if (user != null) { //user is signed in
            openMap(view);
        } else {
            //user has not signed in to the app, no redirect
        }

    }


}


