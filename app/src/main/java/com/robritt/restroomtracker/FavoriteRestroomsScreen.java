package com.robritt.restroomtracker;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class FavoriteRestroomsScreen extends AppCompatActivity {
    FirebaseFirestore db;
    double latitude, longitude;
    Location currentLocation;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    Map<Long,String> listToID = new HashMap<>();
    TreeMap<Float, String> distanceID = new TreeMap<>();
    TreeMap<String, String> restroomNames = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_restrooms_screen);
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        latitude = getIntent().getDoubleExtra("latitude",0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        currentLocation = new Location("Current Location");
        currentLocation.setLongitude(longitude);
        currentLocation.setLatitude(latitude);
//        HashMap<Marker, String> markerToID = (HashMap<Marker, String>) intent.getSerializableExtra("map"); //gets the marker/ ID referance from mainmap
        //List stuff
        ListView mList = (ListView) findViewById(R.id.list_nearby_fav);
        listItems = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, R.layout.list_tv_text, listItems);
        mList.setAdapter(adapter);

        RadioButton rb5 = (RadioButton) findViewById(R.id.nearest5_fav);
        RadioButton rb15 = (RadioButton) findViewById(R.id.nearest15_fav);
        RadioButton rb25 = (RadioButton) findViewById(R.id.nearest25_fav);


        updateFavoriteRestroomsList(5);

        rb5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFavoriteRestroomsList(5);
            }
        });

        rb15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFavoriteRestroomsList(15);
            }
        });
        rb25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFavoriteRestroomsList(25);
            }
        });


        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("ListOfRestrooms", "You clicked Item: " + id + " at position:" + position);
                // Then you start a new Activity via Intent
                Intent intent = new Intent();
                intent.setClass(FavoriteRestroomsScreen.this, RestroomViewScreen.class);
                intent.putExtra("position", position);
                // Or / And
                String restroomID = listToID.get(id); //gets restroom DB id from the list ID
                intent.putExtra("id", restroomID);
                intent.putExtra("uid", getIntent().getStringExtra("uid"));
//                Toast.makeText(ListOfRestroomsScreen.this, "ID = " + Long.toString(id) + "RID = " + restroomID, Toast.LENGTH_SHORT).show();

                startActivity(intent);
            }
        });


    }

    private void updateFavoriteRestroomsList(final int numberToDisplay) { //get all restrooms in database with the value "open"

        db.collection("restrooms")
                .whereEqualTo("open", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null){
                            Log.w("Restrooms", "Listen failed", e);
                            return;
                        }
                        distanceID.clear();
                        adapter.clear();
                        listToID.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Map<String, Object> favorites = (Map<String, Object>) doc.getData().get("favorites");
                            Boolean isFavorited;
                            if (favorites.get(getIntent().getStringExtra("uid"))!= null){
                                isFavorited = (Boolean) favorites.get(getIntent().getStringExtra("uid"));
                            }
                            else{
                                isFavorited = Boolean.FALSE;
                            }
                            if (doc.getBoolean("open") == true && isFavorited) {
                                GeoPoint geopoint = (GeoPoint) doc.getData().get("location");
                                double latitude = geopoint.getLatitude();
                                double longitude = geopoint.getLongitude();
                                Location location = new Location("Restroom");
                                location.setLatitude(latitude);
                                location.setLongitude(longitude);
                                float distance =  location.distanceTo(currentLocation);
                                distance = Math.round(distance);
                                distanceID.put(distance, doc.getId());
                                restroomNames.put(doc.getId(), (String)doc.getData().get("name"));

                            }
                            else{

                            }
                        }//done quering changes in restrooms
                        long listID = 0;
                        for (Map.Entry m : distanceID.entrySet()) {
                            if (listID >= numberToDisplay){
                                break;

                            }

                            String restroomID =  (String) m.getValue();
                            String restroomName = restroomNames.get(restroomID);

                            int listIdInt = (int)listID;
                            int listIdIntPlusOne = listIdInt + 1;
                            String numberListing = Integer.toString(listIdIntPlusOne);
                            String distanceString = Float.toString((Float) m.getKey());
                            adapter.add(numberListing + " " + restroomName + "\n\t\tDistance " + distanceString + " meters"); //TODO m.getValue() gives us restroomUID, can be used to pull info like tags to display
                            listToID.put(listID, (String) m.getValue()); //add reference ID when adding stuff to ListView, for lookup in onClickListener
                            listID++;
                        }

                    }
                });

    } //end restroomlocatiosn
}
