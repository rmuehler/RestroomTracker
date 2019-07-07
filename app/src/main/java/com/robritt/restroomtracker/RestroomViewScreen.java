package com.robritt.restroomtracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.Map;

import static com.google.firebase.Timestamp.now;

public class RestroomViewScreen extends AppCompatActivity {

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_restroom_view_screen);

        final TextView latitudeTextView = findViewById(R.id.view_latitude);
        final TextView longitudeTextView = findViewById(R.id.view_longitude);
        final TextView createdTextView = findViewById(R.id.view_createdby);
        final TextView whenTextView = findViewById(R.id.view_createdwhen);

        DocumentReference restRef = db.collection("restrooms").document(getIntent().getStringExtra("id")); //trying to find the current username of the creator of restroom
        restRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Map<String, Object> document = task.getResult().getData();
                    GeoPoint geopoint = (GeoPoint) document.get("location");
                    latitudeTextView.setText(Double.toString(geopoint.getLatitude()));
                    longitudeTextView.setText(Double.toString(geopoint.getLongitude()));
                    Timestamp created = (Timestamp) document.get("created");
//                    String relativeTime = (String) DateUtils.getRelativeTimeSpanString(created.toDate().getTime(), now().toDate().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
                    String relativeTime = (String) DateUtils.getRelativeTimeSpanString(created.toDate().getTime());
//                    String stringTimeSince = DateUtils.getRelativeTimeSpanString(document.get("created"), FieldValue.serverTimestamp(), DateUtils.MINUTE_IN_MILLIS, document.get("created");
//                    whenTextView.setText( Integer.toString(now().compareTo(created)));
                    whenTextView.setText(relativeTime);


                    //lookup current user of creator
                    db.collection("users").document((String) document.get("createdby")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful() && task.getResult().getData() != null){
                                String createdBy = (String) task.getResult().getData().get("username");
                                createdTextView.setText(createdBy);
                            }
                            else{
                                createdTextView.setText("Deleted user");
                            }
                        }
                    });


                }
                else{

                }
            }
        });




    }

    public void openReportScreen(View view){
        Intent intent = new Intent(this, RestroomReportingScreen.class);
        intent.putExtra("id", getIntent().getStringExtra("id"));
        startActivity(intent);
    }
}
