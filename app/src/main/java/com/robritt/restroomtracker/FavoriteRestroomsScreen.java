package com.robritt.restroomtracker;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.TreeMap;

public class FavoriteRestroomsScreen extends AppCompatActivity {
    FirebaseFirestore db;
    ArrayAdapter<String> adapter;
    TreeMap<String, String> restroomNames = new TreeMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_restrooms_screen);
        db = FirebaseFirestore.getInstance();



    }

    private void updateFavoritesList(final int numberToDisplay) {

        db.collection("restrooms")
                .whereEqualTo("open", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null){
                            Log.w("Restrooms", "Listen failed", e);
                            return;
                        }

                        adapter.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (doc.getBoolean("open") !=  false) {
                                GeoPoint geopoint = (GeoPoint) doc.getData().get("location");
                                double latitude = geopoint.getLatitude();
                                double longitude = geopoint.getLongitude();
                                Location location = new Location("Restroom");
                                location.setLatitude(latitude);
                                location.setLongitude(longitude);
                                restroomNames.put(doc.getId(), (String)doc.getData().get("name"));
                            }
                        }
                        long listID = 0;
                        for (TreeMap.Entry m : restroomNames.entrySet()) {
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

                            listID++;
                        }

                    }
                });
    }
}
