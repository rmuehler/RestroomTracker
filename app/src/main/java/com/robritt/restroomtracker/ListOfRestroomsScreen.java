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
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListOfRestroomsScreen extends AppCompatActivity {

    FirebaseFirestore db;
    double latitude, longitude;
    Location currentLocation;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    Map<Long,String> listToID = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_list_of_restrooms_screen);
        Intent intent = getIntent();
        latitude = getIntent().getDoubleExtra("latitude",0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        currentLocation = new Location("Current Location");
        currentLocation.setLongitude(longitude);
        currentLocation.setLatitude(latitude);
//        HashMap<Marker, String> markerToID = (HashMap<Marker, String>) intent.getSerializableExtra("map"); //gets the marker/ ID referance from mainmap
        //List stuff
        ListView mList = (ListView) findViewById(R.id.list_nearby);
        listItems = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        mList.setAdapter(adapter);


        updateRestroomsList();


        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("ListOfRestrooms", "You clicked Item: " + id + " at position:" + position);
                // Then you start a new Activity via Intent
                Intent intent = new Intent();
                intent.setClass(ListOfRestroomsScreen.this, RestroomViewScreen.class);
                intent.putExtra("position", position);
                // Or / And
                String restroomID = listToID.get(id); //gets restroom DB id from the list ID
                intent.putExtra("id", restroomID);
                Toast.makeText(ListOfRestroomsScreen.this, "ID = " + Long.toString(id) + "RID = " + restroomID, Toast.LENGTH_SHORT).show();

                startActivity(intent);
            }
        });

    } //end oncreate

    private void updateRestroomsList() { //get all restrooms in database with the value "open"
        db.collection("restrooms")
                .whereEqualTo("open", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null){
                            Log.w("Restrooms", "Listen failed", e);
                            return;
                        }
                        long i = 0;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (doc.getBoolean("open") !=  false) {
                                GeoPoint geopoint = (GeoPoint) doc.getData().get("location");
                                double latitude = geopoint.getLatitude();
                                double longitude = geopoint.getLongitude();
                                Location location = new Location("Restroom");
                                location.setLatitude(latitude);
                                location.setLongitude(longitude);
                                float distance =  location.distanceTo(currentLocation);
                                distance = Math.round(distance);
                                String distanceString = Float.toString(distance);
                                adapter.add("Restroom\n\tDistance: " + distanceString + " meters");
                                listToID.put(i,doc.getId());
                                i++;



                            }
                        }











                    }
                });

    } //end restroomlocatiosn


} //end class


