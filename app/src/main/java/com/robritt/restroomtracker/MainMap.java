package com.robritt.restroomtracker;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.ArCoreApk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.SphericalUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;
    static MainMap instance;
    private LocationCallback locationCallback;
    private Task<QuerySnapshot> restrooms;
    LatLng currentPosition;
    Marker locationMarker;
    FirebaseFirestore db;
//    private Map<Marker, String> markerToID = new HashMap<>();
    FloatingActionButton mArButton;
    boolean isFABOpen = false;
    private ArrayList<Marker> mMarkers = new ArrayList<Marker>();
    SharedPreferences filters;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        filters = getSharedPreferences("filters", MODE_PRIVATE);
        mArButton = (FloatingActionButton) findViewById(R.id.open_ar_button);
//        maybeEnableArButton();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_main_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        instance = this;

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (locationMarker != null) {
                        locationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                    } else {
                        locationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Current location").visible(false));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationMarker.getPosition()));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    }
////                    locationMarker.remove();
//                    locationMarker.setPosition();
//                    currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
//                    locationMarker = mMap.addMarker(new MarkerOptions().position(currentPosition).title("Current location"));
////                    mMap.moveCamera(CameraUpdateFactory.newLatLng(locationMarker.getPosition()));
////                    mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
                }
            }

            ;
        };

        FloatingActionButton menu_fab = findViewById(R.id.menu_fab);
        menu_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

    }

    private void showFABMenu(){
        isFABOpen=true;
        FloatingActionButton fab1 = findViewById(R.id.open_list_button);
        FloatingActionButton fab2 = findViewById(R.id.open_ar_button);
        FloatingActionButton fab3 = findViewById(R.id.open_favs_button);
        FloatingActionButton fab4 = findViewById(R.id.filter_button);
        fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_60));
        fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_120));
        fab3.animate().translationY(-getResources().getDimension(R.dimen.standard_180));
        fab4.animate().translationY(-getResources().getDimension(R.dimen.standard_240));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        FloatingActionButton fab1 = findViewById(R.id.open_list_button);
        FloatingActionButton fab2 = findViewById(R.id.open_ar_button);
        FloatingActionButton fab3 = findViewById(R.id.open_favs_button);
        FloatingActionButton fab4 = findViewById(R.id.filter_button);
        fab1.animate().translationY(0);
        fab2.animate().translationY(0);
        fab3.animate().translationY(0);
        fab4.animate().translationY(0);
    }


    public void openListScreen(View view) {
        Intent intent = new Intent(this, ListOfRestroomsScreen.class);
//        intent.putExtra("map", (Serializable) markerToID);
        intent.putExtra("latitude", locationMarker.getPosition().latitude);
        intent.putExtra("longitude", locationMarker.getPosition().longitude);
        startActivity(intent);
    }


    public void openAddScreen(View view) {
        if(locationMarker == null){
            return;
        }
        Intent intent = new Intent(this, AddARestroomScreen.class);
        intent.putExtra("latitude", locationMarker.getPosition().latitude);
        intent.putExtra("longitude", locationMarker.getPosition().longitude);
        startActivityForResult(intent, 1);
    }

    public void openFavScreen(View view) {
        Intent intent = new Intent(this, FavoriteRestroomsScreen.class);
        startActivity(intent);
    }

    public void openSettingsScreen(View view) {
        Intent intent = new Intent(this, SettingsScreen.class);
        startActivity(intent);
    }

    public void openARScreen(View view) {
        Intent intent = new Intent(this, ArScreen.class);
        startActivity(intent);
    }

    public void openFilterScreen(View view) {
        Intent intent = new Intent(this, FilterScreen.class);
        startActivityForResult(intent, 500);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
//                updateLastLocation(); //TODO see if this can be reenabled, since it puts the last known location up on the map INSTANTLY
                updateLocation();
                if (ActivityCompat.checkSelfPermission(MainMap.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainMap.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Toast.makeText(MainMap.this, "Location permission required.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }).check();



//        Location myLocation = mMap.getMyLocation();


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
//
//                final String restroomID = markerToID.get(marker); //lookup document id (in database) for current marker
                final String restroomID =(String) marker.getTag();
                if (restroomID != null) { //if the ID doesn't exist in database (e.g. its a custom marker or current location)

                    DocumentReference docRef = db.collection("restrooms").document(restroomID);

                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                Map<String, Object> restroom = task.getResult().getData();
                                GeoPoint geopoint = (GeoPoint) restroom.get("location");

                                Intent intent = new Intent(MainMap.this, RestroomViewScreen.class);

                                intent.putExtra("id", restroomID);
//                                intent.putExtra("latitude", geopoint.getLatitude());
//                                intent.putExtra("longitude", geopoint.getLongitude());
//                                intent.putExtra("createdby", (String)restroom.get("createdby"));
//                                intent.putExtra("time", restroomID);

                                startActivity(intent);
                            } else {
                                Toast.makeText(MainMap.this, "Restroom no longer exists", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });






//        updateLastLocation();
        updateRestroomLocations();



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1){ //after adding a restroom
//            updateRestroomLocations();
        }
        if (resultCode == RESULT_OK && requestCode == 500){ //after changing filters
//            Toast.makeText(instance, "Filters saved", Toast.LENGTH_SHORT).show();
            updateRestroomLocations();
//            this.recreate();

        }
    }



    private void updateRestroomLocations() { //get all restrooms in database with the value "open"
        db.collection("restrooms")
            .whereEqualTo("open", true)
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e != null){
                        Log.w("Restrooms", "Listen failed", e);
                        return;
                    }
                    mMap.clear();
                    mMarkers.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        //gets the average ratings for each category:
                        Map<String, Object> cleanlinessRatings = (Map) doc.getData().get("cleanliness");
                        Map<String, Object> privacyRatings = (Map) doc.getData().get("privacy");

                        double cleanRating = 0;
                        double privacyRating = 0;

                        for (Map.Entry<String, Object> entry : cleanlinessRatings.entrySet()){
                            cleanRating += (Double) entry.getValue();
                        }
                        cleanRating = cleanRating / cleanlinessRatings.size();

                        for (Map.Entry<String, Object> entry : privacyRatings.entrySet()){
                            privacyRating += (Double) entry.getValue();
                        }
                        privacyRating = privacyRating / cleanlinessRatings.size();

                        //find the distance from current Location to marker
                        GeoPoint geopoint = (GeoPoint) doc.getData().get("location");
                        LatLng restroomLocation = new LatLng(geopoint.getLatitude(), geopoint.getLongitude());

                        double distance = SphericalUtil.computeDistanceBetween(locationMarker.getPosition(), restroomLocation); //calc distance


                        if(filters.getBoolean("baby",false) && doc.getBoolean("babychanging") == false){
                            //do nothing
                        }
                        else if(filters.getBoolean("handicap",false) && doc.getBoolean("handicapped") == false){
                            //do nothing
                        }
                        else if (filters.getFloat("privacy", 0) > privacyRating){

                        }
                        else if (filters.getFloat("cleanliness", 0) > cleanRating){

                        }
                        else if ((filters.getInt("distance", 10) * 1000) < distance){

                        }
                        else {
                                Marker marker = mMap.addMarker(new MarkerOptions().position(restroomLocation).title(doc.getString("name"))
                                        .snippet((String) doc.getString("directions"))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))); //TODO we can have a custom restroom icon here

                                marker.setTag(doc.getId());
                                mMarkers.add(marker);
                            }

                    }

                    //TODO filter markers based on custom distance filter
//                     filterRestrooms();

                    }
                });

    } //end restroomlocatiosn

    public void updateLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainMap.this, "Location not found.", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Current position")).setVisible(false);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(locationMarker.getPosition()));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
                } else {
                    locationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(37.419857, -122.078827)));
                }
            }
        });

    }

    public void updateLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(getLocationRequest(),locationCallback, null);
    }

    private LocationRequest getLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onBackPressed() { //if user is signed in, no going to main screen
        if(mAuth.getCurrentUser() != null) {
            //do nothing
        } else {
            super.onBackPressed();
        }
    }

    void maybeEnableArButton() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    maybeEnableArButton();
                }
            }, 200);
        }
        if (availability.isSupported()) {
//            mArButton.setVisibility(View.VISIBLE);
            mArButton.setEnabled(false);
            // indicator on the button.
        } else { // Unsupported or unknown.
//            mArButton.setVisibility(View.INVISIBLE);
            mArButton.setEnabled(false);
        }
    }

    public void filterRestrooms(){


        for (Marker marker : mMarkers) {
//            db.collection("restrooms").document().get
            //location based filters


            //filtering on preferences
            if (filters.getBoolean("baby", false) || filters.getBoolean("handicap", false)){
                db.collection("restrooms").document();
                marker.setVisible(false);
            }


        }
    }





}
