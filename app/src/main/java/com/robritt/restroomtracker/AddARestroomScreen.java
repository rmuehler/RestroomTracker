package com.robritt.restroomtracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.model.value.ServerTimestampValue;
import com.google.firestore.v1.DocumentTransform;

import java.util.HashMap;
import java.util.Map;

public class AddARestroomScreen extends AppCompatActivity  {

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_add_arestroom_screen);
    }

    public void openViewScreen(View view){
        Intent intent = new Intent(this, RestroomViewScreen.class);
        startActivity(intent);
    }

    public void addRestroom(View view){
        final double latitude = getIntent().getDoubleExtra("latitude",0);
        final double longitude = getIntent().getDoubleExtra("longitude", 0);
        GeoPoint geopoint = new GeoPoint(latitude,longitude);
        final Map<String,Object> newRestroom = new HashMap<>();
        newRestroom.put("location", geopoint);                  //add fields that go into each restroom here
        newRestroom.put("open", true);
        newRestroom.put("createdby", mAuth.getCurrentUser().getUid());
        newRestroom.put("created", FieldValue.serverTimestamp());
        db.collection("restrooms").add(newRestroom).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(AddARestroomScreen.this, "Added new restroom", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                setResult(RESULT_CANCELED);
                Toast.makeText(AddARestroomScreen.this, "Add unsuccessful", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

}
