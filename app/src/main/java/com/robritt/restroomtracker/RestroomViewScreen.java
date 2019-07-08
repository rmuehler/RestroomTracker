package com.robritt.restroomtracker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.firebase.Timestamp.now;

public class RestroomViewScreen extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap miniMap;
    FirebaseFirestore db;

    ListView listView;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;

    LatLng restroomLocation = new LatLng(0,0);

    RatingBar cleanliness, privacy;
    TextView privacyNo, cleanlinessNo;

    Map<String, Object> userPrivacyRatings;
    Map<String, Object> userCleanlinessRatings;
    Map<String, Object> document = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_restroom_view_screen);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.restroomMapView);
        mapFragment.getMapAsync(this);

        final TextView longitudeTextView = findViewById(R.id.view_longitude);
        final TextView createdTextView = findViewById(R.id.view_createdby);
        final TextView whenTextView = findViewById(R.id.view_createdwhen);
        final TextView nameTextView = findViewById(R.id.restroomName);
        final TextView hoursTextView = findViewById(R.id.hoursDisplay);
        final TextView cleanTextView = findViewById(R.id.cleanlinessDisplay);
        final TextView privacyTextView = findViewById(R.id.privacyDisplay);
        final TextView babyTextView = findViewById(R.id.babyDisplay);
        final TextView handicapTextView = findViewById(R.id.handicapDisplay);
        final TextView keyTextView = findViewById(R.id.keyDisplay);
        final TextView floorTextView = findViewById(R.id.floorDisplay);
        final TextView genderTextView = findViewById(R.id.genderDisplay);

        cleanliness = findViewById(R.id.reviewStarsCleanliness);
        privacy = findViewById(R.id.reviewStarsPrivacy);

        privacyNo = findViewById(R.id.privacy_no);
        cleanlinessNo = findViewById(R.id.cleanliness_no);

        userPrivacyRatings = new HashMap<>();
        userCleanlinessRatings = new HashMap<>();

        final Button reviewButton = findViewById(R.id.reviewButton);
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                userCleanlinessRatings.put(getIntent().getStringExtra("uid"), (double)cleanliness.getRating());
                userPrivacyRatings.put(getIntent().getStringExtra("uid"), (double) privacy.getRating());
                document.put("privacy", userPrivacyRatings);
                document.put("cleanliness", userCleanlinessRatings);

                db.collection("restrooms").document(getIntent().getStringExtra("id")).set(document).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RestroomViewScreen.this, "Rating saved", Toast.LENGTH_SHORT).show();

                        privacy.setRating((float) getAveragePrivacyRating());
                        privacyNo.setText(userPrivacyRatings.size() + " rating(s)");
                        cleanliness.setRating((float) getAverageCleanlinessRating());
                        cleanlinessNo.setText(userCleanlinessRatings.size() + " rating(s)");

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RestroomViewScreen.this, "Rating failed to save", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        listView = (ListView) findViewById(R.id.listViewOptional);
        listItems = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, R.layout.my_tv_listview, listItems);
        listView.setAdapter(adapter);


        DocumentReference restRef = db.collection("restrooms").document(getIntent().getStringExtra("id")); //trying to find the current username of the creator of restroom
        restRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    document = task.getResult().getData();
                    GeoPoint geopoint = (GeoPoint) document.get("location");
                    double lat = geopoint.getLatitude();
                    double lng = geopoint.getLongitude();
                    LatLng ll = new LatLng(lat,lng);
                    restroomLocation = ll;
                    longitudeTextView.setText(Double.toString(lng));

                    nameTextView.setText((String)document.get("name"));

                    String dbOpen = (String)document.get("opens");
                    String dbClose = (String)document.get("closes");

                    if(dbOpen.equals("24 hours")){
                        hoursTextView.setText("Open 24 Hours");
                    }
                    else{
                        hoursTextView.setText("Open: " + dbOpen + " - " + dbClose);
                    }

                    //Change these to be scores based on averages from reviews
                    cleanTextView.setText("Cleanliness: " /* + (String)document.get("cleanliness") */);
                    privacyTextView.setText("Privacy: ");

                    Boolean babyBoolean = (Boolean) document.get("babychanging");
                    if(babyBoolean){
                        babyTextView.setText("Baby Changing Station Available");
                    }
                    else{
                        babyTextView.setText("No Baby Changing Stations");
                    }

                    Boolean handicapBoolean = (Boolean) document.get("handicapped");
                    if(handicapBoolean){
                        handicapTextView.setText("Handicap Accessible");
                    }
                    else{
                        handicapTextView.setText("Not Handicap Accessible");
                    }

                    Boolean keyBoolean = (Boolean)document.get("keyrequired");
                    if(keyBoolean){
                        keyTextView.setText("Key is Required for Access");
                    }
                    else{
                        keyTextView.setText("No Key Required for Access");
                    }

                    floorTextView.setText("Floor: " + (String)document.get("floor"));


                    String dbGender = (String)document.get("gender");

                    if(dbGender.equals("Family")){
                        genderTextView.setText("Family Restroom");
                    }
                    else if(dbGender.equals("Men")){
                        genderTextView.setText("Men's Room");

                    }
                    else if(dbGender.equals("Women")){
                        genderTextView.setText("Women's Room");

                    }
                    else {
                        genderTextView.setText("Unisex Restroom");
                    }

                    String dbAutomatic = (String)document.get("automatictoilets");
                    if(dbAutomatic.equals("Yes")){
                        listItems.add("Automatic Toilets");
                        adapter.notifyDataSetChanged();
                    } else if(dbAutomatic.equals("No")){
                        listItems.add("No Automatic Toilets");
                        adapter.notifyDataSetChanged();
                    }

                    String dbInside = (String)document.get("insidebuilding");
                    if(dbInside.equals("Yes")){
                        listItems.add("Inside Building");
                        adapter.notifyDataSetChanged();
                    } else if(dbInside.equals("No")){
                        listItems.add("Accessible from Outside Building");
                        adapter.notifyDataSetChanged();
                    }

                    String dbDirections = (String)document.get("directions");
                    if(!dbDirections.equals("")){
                        listItems.add("Directions: " + dbDirections);
                        adapter.notifyDataSetChanged();
                    }

                    String dbStalls = (String)document.get("stalls");
                    if(!dbStalls.equals("Leave Blank")){
                        listItems.add("Number of Stalls: " + dbStalls);
                        adapter.notifyDataSetChanged();
                    }

                    userCleanlinessRatings = (Map<String, Object>) document.get("cleanliness");
                    userPrivacyRatings = (Map<String, Object>) document.get("privacy");

                    double cleanRating = getAverageCleanlinessRating();
                    double privacyRating = getAveragePrivacyRating();





                    cleanliness.setRating((float) cleanRating);
                    cleanlinessNo.setText(userCleanlinessRatings.size() + " rating(s)");
                    privacy.setRating((float) privacyRating);
                    privacyNo.setText(userPrivacyRatings.size() + " rating(s)");


                    Timestamp created = (Timestamp) document.get("created");
                    String relativeTime = (String) DateUtils.getRelativeTimeSpanString(created.toDate().getTime());
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

    private double getAveragePrivacyRating(){
        double privacyRating = 0;
        for (Map.Entry<String, Object> entry : userPrivacyRatings.entrySet()){
            privacyRating += (Double) entry.getValue();
        }
        privacyRating = privacyRating / userCleanlinessRatings.size();
        return privacyRating;
    }

    private double getAverageCleanlinessRating(){
        double cleanRating = 0;
        for (Map.Entry<String, Object> entry : userCleanlinessRatings.entrySet()){
            cleanRating += (Double) entry.getValue();
        }
        cleanRating = cleanRating / userCleanlinessRatings.size();
        return cleanRating;
    }

    public void openReportScreen(View view){
        Intent intent = new Intent(this, RestroomReportingScreen.class);
        intent.putExtra("id", getIntent().getStringExtra("id"));
        startActivity(intent);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        miniMap = googleMap;
        miniMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        final TextView longitudeTextView = findViewById(R.id.view_longitude);

        longitudeTextView.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                miniMap.addMarker(new MarkerOptions()
                        .position(restroomLocation));
                miniMap.moveCamera(CameraUpdateFactory.newLatLng(restroomLocation));
                miniMap.moveCamera(CameraUpdateFactory.zoomTo(15));
            }
        });


    }
}
