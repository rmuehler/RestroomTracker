package com.robritt.restroomtracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText mEmailField, mPasswordField;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login_screen);

        mEmailField = (EditText) findViewById(R.id.username_login_field);
        mPasswordField = (EditText) findViewById(R.id.password_login_field);
        submit = findViewById(R.id.login_button);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignIn();
            }
        });


        mPasswordField.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submit.performClick();
                    return true;
                }
                return false;
            }
        });

    }

    public void openMap() { //Change later for validating login, this is just for testing purposes
        Intent intent = new Intent(this, MainMap.class);
        startActivity(intent);
    }

    private void startSignIn() {

        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) { //if no email or password provided
            Toast.makeText(LoginScreen.this, "Please complete all fields.", Toast.LENGTH_LONG).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) { //if signup successful
                        Log.d("ACCOUNT", "loginUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);


                    } else { //if signup fails
                        Log.w("ACCOUNT", "loginUserWithEmail:failure", task.getException());
                        Toast.makeText(LoginScreen.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }

                }//end onComplete, when firebase is give the email and password
            });

        }
    }

    public void startForgotPassword(View view){
        String email = mEmailField.getText().toString();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("ACCOUNT", "passwordResetEmail:sent");
                    Toast.makeText(LoginScreen.this, "Reset email sent.", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(LoginScreen.this, "Reset failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {//checks to see if the user is logged in, if he is take him to map
        if (user != null) { //user is signed in
            openMap();
        } else {
            //user has not signed in to the app, no redirect
        }

    }


}


