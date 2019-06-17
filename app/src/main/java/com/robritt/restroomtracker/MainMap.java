package com.robritt.restroomtracker;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

public class MainMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void openListScreen(View view){
        Intent intent = new Intent(this, ListOfRestroomsScreen.class);
        startActivity(intent);
    }


    public void openAddScreen(View view){
        Intent intent = new Intent(this, AddARestroomScreen.class);
        startActivity(intent);
    }

    public void openFavScreen(View view){
        Intent intent = new Intent(this, FavoriteRestroomsScreen.class);
        startActivity(intent);
    }

    public void openSettingsScreen(View view){
        Intent intent = new Intent(this, SettingsScreen.class);
        startActivity(intent);
    }

    public void openARScreen(View view){
        Intent intent = new Intent(this, ArScreen.class);
        startActivity(intent);
    }

    public void openFilterScreen(View view){
        Intent intent = new Intent(this, FilterScreen.class);
        startActivity(intent);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(28.058959, -82.413908);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
