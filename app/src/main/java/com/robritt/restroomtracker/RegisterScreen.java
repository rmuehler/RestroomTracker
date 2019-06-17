package com.robritt.restroomtracker;

import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;


public class RegisterScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mEmailField, mPasswordField, mConfPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        mEmailField = (EditText) findViewById(R.id.register_email_field);
        mPasswordField = (EditText) findViewById(R.id.register_password_field);
        mConfPasswordField = (EditText) findViewById(R.id.register_passconf_field);

        final Button signupButton = (Button) findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignUp();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser); //TODO changes based on if a user is logged in (sign out button)
    }

    private void startSignUp() {

        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        String confPassword = mConfPasswordField.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) { //if no email or password provided
            Toast.makeText(RegisterScreen.this, "Please complete all fields.", Toast.LENGTH_LONG).show();
        } else if (!password.equals(confPassword)) {
            Toast.makeText(RegisterScreen.this, "Password fields do not match.", Toast.LENGTH_LONG).show();
        }
        else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) { //if signup successful
                        Log.d("USER REGISTER", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);


                    } else { //if signup fails
                        Log.w("USER REGISTER", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterScreen.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }

                }//end onComplete, when firebase is give the email and password
            });

        }
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





}//end RegisterScreen
