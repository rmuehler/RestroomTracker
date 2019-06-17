package com.robritt.restroomtracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);

        if (mAuth.getCurrentUser() != null) {//display email if user signed in (should be)
            TextView accountEmail = (TextView) findViewById(R.id.setttings_email_view);
            accountEmail.setText(mAuth.getCurrentUser().getEmail());
        }
    }


    public void signOut(View view){

        mAuth.signOut();

        if (mAuth.getCurrentUser() == null) {//if sign out successful
            startActivity(new Intent(this,MainActivity.class));
        }
        else{
            Toast.makeText(this, "Sign out failed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteAccount(View view){
        mAuth.getCurrentUser().delete();

        if (mAuth.getCurrentUser() == null) {//if delete successful
            startActivity(new Intent(this,MainActivity.class));
            Toast.makeText(this, "Account deleted.", Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(this, "Delete failed.", Toast.LENGTH_SHORT).show();
        }
    }
}
