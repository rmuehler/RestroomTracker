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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class RestroomReportingScreen extends AppCompatActivity {

    FirebaseFirestore db;

    Map<String, String> document = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_restroom_reporting_screen);

        EditText reportEditText = (EditText) findViewById(R.id.reportDescription);
        Button reportRestroom = findViewById(R.id.button_report);

        reportRestroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userReport = reportEditText.getText().toString();
                document.put("report", userReport);

                if(document != null){
                    if (reportEditText.getText().toString().length() == 0){ //report required
                        reportEditText.setError("Report Required");
                        return;
                    }


                    db.collection("restrooms").document(getIntent().getStringExtra("id")).set(document).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(RestroomReportingScreen.this, "Report saved", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RestroomReportingScreen.this, "Report failed to save", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }
}
