package com.robritt.restroomtracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);
        TextView accountEmail = (TextView) findViewById(R.id.setttings_email_view);
        Button signout = (Button) findViewById(R.id.sign_off_button);
        if(user.getEmail() == null){
            accountEmail.setText("Guest");
            signout.setVisibility(View.INVISIBLE);
        }
        else{//display email if user signed in (should be
            accountEmail.setText(user.getEmail());
            signout.setVisibility(View.VISIBLE);
        }
    }




    public void signOut(View view){

        mAuth.signOut();
        user = mAuth.getCurrentUser();
        if (user == null) {//if sign out successful
            Log.d("ACCOUNT", "Logout:success");
            startActivity(new Intent(this,MainActivity.class));
        }
        else{
            Toast.makeText(this, "Sign out failed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteAccount(View view){
        final String userid = user.getUid();
        user.delete().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    db.collection("users").document(userid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d("ACCOUNT", "UsernameDeletion:success");
                            } //TODO improve this
                        }
                    });
                    user = mAuth.getCurrentUser();
                    Log.d("ACCOUNT", "AccountDeletion:success");
                    Toast.makeText(SettingsScreen.this, "Account deleted.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SettingsScreen.this,MainActivity.class));
                }
                else{
                    Toast.makeText(SettingsScreen.this, "Delete failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
