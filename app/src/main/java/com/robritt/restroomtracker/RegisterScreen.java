package com.robritt.restroomtracker;

import android.content.Intent;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class RegisterScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore userDB;
    private EditText mEmailField, mPasswordField, mConfPasswordField, mUsernameField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
        userDB = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        mEmailField = (EditText) findViewById(R.id.register_email_field);
        mUsernameField = (EditText) findViewById(R.id.register_username_field);
        mPasswordField = (EditText) findViewById(R.id.register_password_field);
        mConfPasswordField = (EditText) findViewById(R.id.register_passconf_field);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser); //TODO changes based on if a user is logged in (sign out button)
    }

    public void startSignUp(final View view) { //TODO anonymous users transfer data https://firebase.google.com/docs/auth/android/anonymous-auth

        final String email = mEmailField.getText().toString();
        final String username = mUsernameField.getText().toString();
        final String password = mPasswordField.getText().toString();
        final String confPassword = mConfPasswordField.getText().toString();

        final Map<String,Object> newUserInformation = new HashMap<>();
        newUserInformation.put("username", username);


        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username)) { //if no email or password provided
            Toast.makeText(RegisterScreen.this, "Please complete all fields.", Toast.LENGTH_LONG).show();
        } else if (!password.equals(confPassword)) {
            Toast.makeText(RegisterScreen.this, "Password fields do not match.", Toast.LENGTH_LONG).show();
        }else {
            Query query = userDB.collection("users").whereEqualTo("username", username);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot documentSnapshot : task.getResult()){
                            String foundUser = documentSnapshot.getString("username");

                            if (foundUser.equals(username)){
                                Log.d("ACCOUNT", "User Exists");
                                Toast.makeText(RegisterScreen.this, "Username already exists.", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }

                    if (task.getResult().size() == 0){
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) { //if signup successful
                                    Log.d("ACCOUNT", "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    userDB.collection("users").add(newUserInformation).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d("ACCOUNT", "Username added with ID: " + documentReference.getId());
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("ACCOUNT", "Error adding username", e);

                                        }
                                    });

                                    updateUI(view,user);


                                } else { //if signup fails
                                    Log.w("ACCOUNT", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(RegisterScreen.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }

                            }//end onComplete, when firebase is give the email and password
                        });

                    }


                }
            });

        }
    }

    private void updateUI(View view,FirebaseUser user){
        if(user != null){ //user is signed in
            openMap(view);
        }
        else{
            //user has not signed in to the app, no redirect
        }

    }

    public void openMap(View view) { //Change later for validating login, this is just for testing purposes
        Intent intent = new Intent(this, MainMap.class);
        startActivity(intent);
    }





}//end RegisterScreen
