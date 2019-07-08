package com.robritt.restroomtracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class RestroomReportingScreen extends AppCompatActivity {

    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_restroom_reporting_screen);

        final EditText latEdit = findViewById(R.id.edit_lat);
        final EditText longEdit = findViewById(R.id.edit_long);


        db.collection("restrooms").document(getIntent().getStringExtra("id")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    GeoPoint geoPoint = (GeoPoint) document.get("location");
                    latEdit.setText(Double.toString(geoPoint.getLatitude()));
                    longEdit.setText(Double.toString(geoPoint.getLongitude()));

                }
            } //TODO fail conditions
        });



        Button reportRestroom = findViewById(R.id.button_report);

        reportRestroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("restrooms").document(getIntent().getStringExtra("id")).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RestroomReportingScreen.this, "Restroom reported", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RestroomReportingScreen.this, MainMap.class));
                        }
                    }
                });
            }
        });
    }
}
