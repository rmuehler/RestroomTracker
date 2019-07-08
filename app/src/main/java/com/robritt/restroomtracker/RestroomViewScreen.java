package com.robritt.restroomtracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.firebase.Timestamp.now;

public class RestroomViewScreen extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap miniMap;
    FirebaseFirestore db;
    FirebaseStorage mStorage;
    StorageReference storageReference;

    ListView listView;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;

    LatLng restroomLocation = new LatLng(0, 0);

    RatingBar cleanliness, privacy;
    TextView privacyNo, cleanlinessNo, createdBy, createdAt;
    ImageButton favoriteButton;

    String restroomID;

    Map<String, Object> userPrivacyRatings;
    Map<String, Object> userCleanlinessRatings;
    Map<String, Object> favorites;
    Map<String, Object> document = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReference();


        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_restroom_view_screen);
        restroomID = getIntent().getStringExtra("id");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.restroomMapView);
        mapFragment.getMapAsync(this);

        final TextView longitudeTextView = findViewById(R.id.view_longitude);

        final TextView nameTextView = findViewById(R.id.restroomName);
        final TextView genderTextView = findViewById(R.id.genderDisplay);
        createdBy = findViewById(R.id.createdByDisplay);
        createdAt = findViewById(R.id.createdAtDisplay);

        favoriteButton = findViewById(R.id.favButton);

        cleanliness = findViewById(R.id.reviewStarsCleanliness);
        privacy = findViewById(R.id.reviewStarsPrivacy);

        privacyNo = findViewById(R.id.privacy_no);
        cleanlinessNo = findViewById(R.id.cleanliness_no);

        userPrivacyRatings = new HashMap<>();
        userCleanlinessRatings = new HashMap<>();
        favorites = null;

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(favorites == null || favorites.get(getIntent().getStringExtra("uid")) == null){
                    favorites = new HashMap<>();
                    favorites.put(getIntent().getStringExtra("uid"), true);
                    favoriteButton.setImageResource((android.R.drawable.btn_star_big_on));
                }
                else if ((boolean)favorites.get(getIntent().getStringExtra("uid")) == true){
                    favorites.put(getIntent().getStringExtra("uid"), false);
                    document.put("favorites", favorites);
                    favoriteButton.setImageResource((android.R.drawable.btn_star_big_off));
                }
                else{
                    favorites.put(getIntent().getStringExtra("uid"), true);
                    document.put("favorites", favorites);
                    favoriteButton.setImageResource((android.R.drawable.btn_star_big_on));
                }

                db.collection("restrooms").document(restroomID).set(document).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });


        final Button reviewButton = findViewById(R.id.reviewButton);
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (document != null) {
                    userCleanlinessRatings.put(getIntent().getStringExtra("uid"), (double) cleanliness.getRating());
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
                if (task.isSuccessful()) {
                    document = task.getResult().getData();
                    GeoPoint geopoint = (GeoPoint) document.get("location");
                    double lat = geopoint.getLatitude();
                    double lng = geopoint.getLongitude();
                    LatLng ll = new LatLng(lat, lng);
                    restroomLocation = ll;
                    longitudeTextView.setText(Double.toString(lng));

                    nameTextView.setText((String) document.get("name"));
                    String createdByText = "";


                    favorites = (Map<String, Object>) document.get("favorites");
                    if(favorites == null || favorites.get(getIntent().getStringExtra("uid")) == null){
                        favoriteButton.setImageResource((android.R.drawable.btn_star_big_off));
                    }
                    else if ((boolean)favorites.get(getIntent().getStringExtra("uid")) == true){
                        favoriteButton.setImageResource((android.R.drawable.btn_star_big_on));
                    }
                    else{
                        favoriteButton.setImageResource((android.R.drawable.btn_star_big_off));
                    }
                    //lookup current user of creator
                    db.collection("users").document((String) document.get("createdby")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult().getData() != null) {
                                String createdByText = "Created by: " + (String) task.getResult().getData().get("username");
                                createdBy.setText(createdByText);

                            } else {
                                String createdByText = "Created by: Deleted User";
                                createdBy.setText(createdByText);
                            }
                        }
                    });

                    Timestamp created = (Timestamp) document.get("created");
//                    String relativeTime = (String) DateUtils.getRelativeTimeSpanString(created.toDate().getTime(), now().toDate().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
                    String relativeTime = (String) DateUtils.getRelativeTimeSpanString(created.toDate().getTime());
//                    String stringTimeSince = DateUtils.getRelativeTimeSpanString(document.get("created"), FieldValue.serverTimestamp(), DateUtils.MINUTE_IN_MILLIS, document.get("created");
//                    whenTextView.setText( Integer.toString(now().compareTo(created)));

                        createdAt.setText(relativeTime);


                    String dbOpen = (String) document.get("opens");
                    String dbClose = (String) document.get("closes");

                    adapter.notifyDataSetChanged();

                    if (dbOpen.equals("24 hours")) {
                        listItems.add("Open 24 Hours");
                        adapter.notifyDataSetChanged();
                    } else {
                        String a = "Open: " + dbOpen + " - " + dbClose;
                        listItems.add(a);
                        adapter.notifyDataSetChanged();

                    }

//                    //Change these to be scores based on averages from reviews
//                    listItems.add("Cleanliness: ");
//                    adapter.notifyDataSetChanged();
//
//                    listItems.add("Privacy: ");
//                    adapter.notifyDataSetChanged();

                    Boolean babyBoolean = (Boolean) document.get("babychanging");
                    if (babyBoolean) {
                        listItems.add("Baby Changing Station Available");
                        adapter.notifyDataSetChanged();
                    } else {
                        listItems.add("No Baby Changing Stations");
                        adapter.notifyDataSetChanged();

                    }

                    Boolean handicapBoolean = (Boolean) document.get("handicapped");
                    if (handicapBoolean) {
                        listItems.add("Handicap Accessible");
                        adapter.notifyDataSetChanged();
                    } else {
                        listItems.add("Not Handicap Accessible");
                        adapter.notifyDataSetChanged();
                    }

                    Boolean keyBoolean = (Boolean) document.get("keyrequired");
                    if (keyBoolean) {
                        listItems.add("Key is Required for Access");
                        adapter.notifyDataSetChanged();
                    } else {
                        listItems.add("No Key Required for Access");
                        adapter.notifyDataSetChanged();
                    }

                    String b = "Floor: " + (String) document.get("floor");
                    listItems.add(b);
                    adapter.notifyDataSetChanged();


                    String dbGender = (String) document.get("gender");

                    if(dbGender != null) {
                        if (dbGender.equals("Family")) {
                            genderTextView.setText("Family Restroom");
                        } else if (dbGender.equals("Men")) {
                            genderTextView.setText("Men's Room");

                        } else if (dbGender.equals("Women")) {
                            genderTextView.setText("Women's Room");

                        } else {
                            genderTextView.setText("Unisex Restroom");
                        }
                    }

                    String dbAutomatic = (String) document.get("automatictoilets");
                    if (dbAutomatic != null) {
                        if (dbAutomatic.equals("Yes")) {
                            listItems.add("Automatic Toilets");
                            adapter.notifyDataSetChanged();
                        } else if (dbAutomatic.equals("No")) {
                            listItems.add("No Automatic Toilets");
                            adapter.notifyDataSetChanged();
                        }
                    }

                    String dbInside = (String) document.get("insidebuilding");
                    if (dbInside != null) {
                        if (dbInside.equals("Yes")) {
                            listItems.add("Inside Building");
                            adapter.notifyDataSetChanged();
                        } else if (dbInside.equals("No")) {
                            listItems.add("Accessible from Outside Building");
                            adapter.notifyDataSetChanged();
                        }
                    }

                    String dbDirections = (String) document.get("directions");
                    if(dbDirections != null) {
                        if (!dbDirections.equals("")) {
                            listItems.add("Directions: " + dbDirections);
                            adapter.notifyDataSetChanged();
                        }
                    }


                    String dbStalls = (String) document.get("stalls");
                    if (dbStalls != null) {
                        if (!dbStalls.equals("Leave Blank")) {
                            listItems.add("Number of Stalls: " + dbStalls);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    userCleanlinessRatings = (Map<String, Object>) document.get("cleanliness");
                    userPrivacyRatings = (Map<String, Object>) document.get("privacy");

                    double cleanRating = getAverageCleanlinessRating();
                    cleanliness.setRating((float) cleanRating);

                    double privacyRating = getAveragePrivacyRating();
                    privacy.setRating((float) privacyRating);

                    privacyNo.setText(userPrivacyRatings.size() + " rating(s)");
                    cleanlinessNo.setText(userCleanlinessRatings.size() + " rating(s)");

                    String dbImage = (String) document.get("image");
                    ImageView showPhoto = (ImageView) findViewById(R.id.imgView);

                    setListViewHeightBasedOnChildren(listView);

                    StorageReference storageRef = storageReference.child(dbImage);//reach out to your photo file hierarchically
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("URI", uri.toString()); //check path is correct or not ?
                            Picasso.with(RestroomViewScreen.this).load(uri.toString()).into(showPhoto);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle errors
                        }
                    });

                } else {

                }
            }
        });


    }

    private double getAveragePrivacyRating() {
        double privacyRating = 0;
        for (Map.Entry<String, Object> entry : userPrivacyRatings.entrySet()) {
            privacyRating += (Double) entry.getValue();
        }
        privacyRating = privacyRating / userCleanlinessRatings.size();
        return privacyRating;
    }

    private double getAverageCleanlinessRating() {
        double cleanRating = 0;
        for (Map.Entry<String, Object> entry : userCleanlinessRatings.entrySet()) {
            cleanRating += (Double) entry.getValue();
        }
        cleanRating = cleanRating / userCleanlinessRatings.size();
        return cleanRating;
    }

    public void openReportScreen(View view) {
        Intent intent = new Intent(this, RestroomReportingScreen.class);
        intent.putExtra("uid", getIntent().getStringExtra("uid"));
        intent.putExtra("id", getIntent().getStringExtra("id"));
        startActivity(intent);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        miniMap = googleMap;
        miniMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        miniMap.setMyLocationEnabled(true);

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
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

}