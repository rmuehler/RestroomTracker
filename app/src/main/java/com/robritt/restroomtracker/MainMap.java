package com.robritt.restroomtracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_main_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        instance = this;

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (locationMarker != null){
                        locationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                    else{
                        locationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Current location"));
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




    }


    public void openListScreen(View view) {
        Intent intent = new Intent(this, ListOfRestroomsScreen.class);
        startActivity(intent);
    }


    public void openAddScreen(View view) {
        Intent intent = new Intent(this, AddARestroomScreen.class);
        intent.putExtra("latitude", locationMarker.getPosition().latitude);
        intent.putExtra("longitude", locationMarker.getPosition().longitude);
        startActivityForResult(intent,1);
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
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        updateRestroomLocations();
        updateLastLocation();

        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                updateLocation();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Toast.makeText(MainMap.this, "Location permission required.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }).check();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1){
            updateRestroomLocations();
        }
    }

    private void updateRestroomLocations() {
        mMap.clear();
        restrooms = db.collection("restrooms")
                .whereEqualTo("open", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                        Log.i("RESTROOMS",document.getData().get("location").toString());

                                GeoPoint geopoint = (GeoPoint) document.getData().get("location");
                                LatLng restroomLocation = new LatLng(geopoint.getLatitude(), geopoint.getLongitude());


                                Marker marker = mMap.addMarker(new MarkerOptions().position(restroomLocation).title("Restroom").snippet("Restroom description"));
                            }
                        }
                    }
                });
    }

    public void updateLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    locationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Current position"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(locationMarker.getPosition()));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
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



}
