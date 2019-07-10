package com.robritt.restroomtracker;


import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class SettingsScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    TextView accountEmail, accountUsername;
    Button signoutButton, deleteButton;
    ImageButton editUsername;

    Map<String, Object> userInfo = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);
        accountEmail = (TextView) findViewById(R.id.setttings_email_view);
        accountUsername = findViewById(R.id.text_usernamedisplay);
        signoutButton = (Button) findViewById(R.id.sign_off_button);
        deleteButton = findViewById(R.id.delete_account_button);
        editUsername = findViewById(R.id.button_editusername);
        accountEmail.setText(user.getEmail());


        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
        }
    });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsScreen.this)
                        .setTitle("Delete account")
                        .setMessage("Are you sure you want to permanently delete your account?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                deleteAccount();
                            }})
                        .setNegativeButton("NO", null).show();
            }
        });

        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    userInfo = (Map<String, Object>) task.getResult().getData();
                    accountUsername.setText((String)userInfo.get("username"));
                    if (userInfo.get("username").equals("Guest")){
                        signoutButton.setVisibility(View.INVISIBLE);
                        editUsername.setVisibility(View.INVISIBLE);
                    }
                }
                else{

                }
            }
        });

        editUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText newUsernamePrompt = new EditText(SettingsScreen.this);

                AlertDialog.Builder alert = new AlertDialog.Builder(SettingsScreen.this)
                        .setTitle("Enter new username: ")
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String newUsername = newUsernamePrompt.getText().toString();

                                Query query = db.collection("users").whereEqualTo("username", newUsername);
                                Boolean userNameFound = false;
                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().size() == 0){
                                                userInfo.put("username", newUsername);
                                                db.collection("users").document(user.getUid()).set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(SettingsScreen.this, "Username saved", Toast.LENGTH_SHORT).show();
                                                        accountUsername.setText(newUsername);
                                                        return;
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(SettingsScreen.this, "Error saving username", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }
                                            else{
                                                Log.d("USERNAME", "User Exists");
                                                Toast.makeText(SettingsScreen.this, "Username already exists.", Toast.LENGTH_SHORT).show();
                                                return;
                                                }

                                            } //end task success

                                        }

                                    });
                                };




                            })
                        .setNegativeButton("Cancel", null)
                        .setView(newUsernamePrompt);

                alert.show();

            }
        });


}



    public void signOut(){
            mAuth.signOut();
            user = mAuth.getCurrentUser();
            if (user == null) {//if sign out successful
                Log.d("ACCOUNT", "Logout:success");
                startActivity(new Intent(this,MainActivity.class));
            }
            else{
                Toast.makeText(SettingsScreen.this, "Sign out failed.", Toast.LENGTH_SHORT).show();
            }
        }


    public void deleteAccount(){
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

    @Override
    protected void onResume() {
        super.onResume();
//        Button signoutButton = (Button) findViewById(R.id.sign_off_button);
        if(user.getEmail() == null){
            signoutButton.setVisibility(View.INVISIBLE);
            editUsername.setVisibility(View.INVISIBLE);
        }
        else{//display email if user signed in (should be
            signoutButton.setVisibility(View.VISIBLE);
            editUsername.setVisibility(View.VISIBLE);
        }
    }
}
