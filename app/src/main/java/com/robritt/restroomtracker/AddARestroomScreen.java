package com.robritt.restroomtracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.model.value.ServerTimestampValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.v1.DocumentTransform;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddARestroomScreen extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap miniMap;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseStorage mStorage;
    StorageReference storageReference;

    private Button btnChoose;
    private Button btnTake;
    private ImageView imageView;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private final int TAKE_IMAGE_REQUEST = 52;
    private static final int PERMISSION_REQUEST_CODE = 200;

    EditText eName;
    EditText eOpenTime;
    EditText eCloseTime;
    CheckBox cTwentyFour;
    RatingBar rCleanliness;
    RatingBar rPrivacy;
    Switch sBaby;
    Switch sHandicap;
    Switch sKey ;
    EditText eFloor;
    RadioGroup rgGender;
    RadioGroup rgAutomatic ;
    RadioGroup rgBuilding ;
    EditText eDirections ;
    Spinner spStalls;
    RadioButton rCheckedGender;

    LatLng positionOfMarker;
    LatLng lastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReference();
        setContentView(R.layout.activity_add_arestroom_screen);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        Bundle bundle = getIntent().getExtras();
        double lat = bundle.getDouble("latitude");
        double lng = bundle.getDouble("longitude");

        lastLocation = new LatLng(lat, lng);




        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        final EditText chooseOpenTime = findViewById(R.id.openTime);
        final EditText chooseCloseTime = findViewById(R.id.closeTime);
        eName = findViewById(R.id.etRestroomName); //restroom name
        eOpenTime = findViewById(R.id.openTime);
        eCloseTime = findViewById(R.id.closeTime);
        cTwentyFour = findViewById(R.id.checkBox); //open 24 hours
        rCleanliness = findViewById(R.id.starsCleanliness);
        rPrivacy = findViewById(R.id.startsPrivacy);
        sBaby = findViewById(R.id.babySwitch);
        sHandicap = findViewById(R.id.handicapSwitch);
        sKey = findViewById(R.id.keySwitch);
        eFloor = findViewById(R.id.eFloor);
        rgGender = findViewById(R.id.genderRadioGroup);
        rgAutomatic = findViewById(R.id.radioGroup); //automatic toilets
        rgBuilding = findViewById(R.id.radioGroup2); //inside building?
        eDirections = findViewById(R.id.buildingDescription);
        spStalls = findViewById(R.id.numStalls);

        filePath = null;

        chooseOpenTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TimePickerDialog timePickerDialog = new TimePickerDialog(AddARestroomScreen.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        String amPm;
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }
                        if(hourOfDay>12){
                            hourOfDay -= 12;
                        }
                        chooseOpenTime.setText(String.format("%02d:%02d", hourOfDay, minutes) + amPm);
                    }
                }, 0, 0, false);
                timePickerDialog.show();
            }
        });

        chooseCloseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TimePickerDialog timePickerDialog = new TimePickerDialog(AddARestroomScreen.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        String amPm;
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }
                        if(hourOfDay>12){
                            hourOfDay -= 12;
                        }
                        chooseCloseTime.setText(String.format("%02d:%02d", hourOfDay, minutes) + amPm);
                    }
                }, 0, 0, false);
                timePickerDialog.show();
            }
        });


        Spinner spinner = (Spinner) findViewById(R.id.numStalls);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.numStallsArray, R.layout.my_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        /*
        public void onItemSelected(AdapterView<?> parent, View view,
        int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }*/

        btnChoose = (Button) findViewById(R.id.uploadButton);
        btnTake = (Button) findViewById(R.id.takePhotoButton);
        imageView = (ImageView) findViewById(R.id.imgView);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        btnTake.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    takeImage();
                } else {
                    requestPermission();
                }

            }
        });



        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
            params.height = 400;
            mapFragment.getView().setLayoutParams(params);
        } else {
            ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
            params.height = 800;
            mapFragment.getView().setLayoutParams(params);
        }


    }
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                    // main logic
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(AddARestroomScreen.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }



    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    private void takeImage() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePicture.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                filePath = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
                startActivityForResult(takePicture, TAKE_IMAGE_REQUEST);//zero can be replaced with any action code
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 71:
                if(resultCode == RESULT_OK){ //pick image
                    Uri selectedImage = data.getData();
                    filePath = data.getData();
                    imageView.setImageURI(selectedImage);
                }

                break;
            case 52:
                if(resultCode == RESULT_OK){ //camera
//                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    imageView.setImageURI(filePath);
                }
                break;
        }
    }



    public void openViewScreen(View view){
        Intent intent = new Intent(this, RestroomViewScreen.class);
        startActivity(intent);
    }

    public void addRestroom(View view){
        //input validation for required stuff

        if (eName.getText().toString().length() == 0){ //name requiremenet
            eName.setError("Name required");
            return;
        }
        if (eOpenTime.getText().toString().length() == 0 && cTwentyFour.isChecked() == false){ //time requiremenet
            eOpenTime.setError("Time required");
            return;
        }
        if (eCloseTime.getText().toString().length() == 0 && cTwentyFour.isChecked() == false){ //time requiremenet
            eCloseTime.setError("Time required");
            return;
        }
        if (eFloor.getText().toString().length() == 0){ //floor requiremenet
            eFloor.setError("Floor required");
            return;
        }
        if (rgGender.getCheckedRadioButtonId() == -1){
            Toast.makeText(this, "Please specify restroom gender", Toast.LENGTH_SHORT).show();
            return;
        }
        else{ //get the radio button that is checked
            rCheckedGender = (RadioButton) findViewById(rgGender.getCheckedRadioButtonId());
        }



        final double latitude = positionOfMarker.latitude;
        final double longitude = positionOfMarker.longitude;
        GeoPoint geopoint = new GeoPoint(latitude,longitude);
        final Map<String,Object> newRestroom = new HashMap<>();
        final Map<String, Object> cleanlinessRating = new HashMap<>();
        final Map<String, Object> privacyRating = new HashMap<>();

        //image upload
        newRestroom.put("image", uploadImage().getPath());

        //required stuff
        newRestroom.put("name", eName.getText().toString());
        newRestroom.put("open", true);

        if(cTwentyFour.isChecked() == false) {
            newRestroom.put("opens", eOpenTime.getText().toString());
            newRestroom.put("closes", eCloseTime.getText().toString());
        }
        else{
            newRestroom.put("opens", "24 hours");
            newRestroom.put("closes", "24 hours");
        }

        newRestroom.put("floor", eFloor.getText().toString());

        //DOES NOT NEED VALIDATION
        newRestroom.put("babychanging", sBaby.isChecked());
        newRestroom.put("handicapped", sHandicap.isChecked());
        newRestroom.put("keyrequired", sKey.isChecked());
        newRestroom.put("gender", rCheckedGender.getText());

        cleanlinessRating.put(mAuth.getCurrentUser().getUid(), rCleanliness.getRating()); //we need to store this in maps to the UID is linked to rating
        newRestroom.put("cleanliness", cleanlinessRating);

        privacyRating.put(mAuth.getCurrentUser().getUid(), rPrivacy.getRating());
        newRestroom.put("privacy", privacyRating);

        //optional stuff
        if (rgAutomatic.getCheckedRadioButtonId() == -1){
            newRestroom.put("automatictoilets", "Not specified");
        }
        else{
            RadioButton rCheckedAutomatic = (RadioButton) findViewById(rgAutomatic.getCheckedRadioButtonId());
            if("Leave Blank".equals(rCheckedAutomatic.getText())){
                newRestroom.put("automatictoilets", "Not specified");
            }
            else {
                newRestroom.put("automatictoilets", rCheckedAutomatic.getText());
            }
        }
        if(rgBuilding.getCheckedRadioButtonId() == -1){
            newRestroom.put("insidebuilding", "Not specified");
        }
        else{

            RadioButton rCheckedBuilding = (RadioButton) findViewById(rgBuilding.getCheckedRadioButtonId());
            if("Leave Blank".equals(rCheckedBuilding.getText())){
                newRestroom.put("insidebuilding", "Not specified");
            }
            else {
                newRestroom.put("insidebuilding", rCheckedBuilding.getText());
            }
        }
//        newRestroom.put("automatictoilets", rgAutomatic.getCheckedRadioButtonId());
//        newRestroom.put("insidebuilding", rgBuilding.getCheckedRadioButtonId());





        newRestroom.put("directions", eDirections.getText().toString());
        newRestroom.put("stalls", spStalls.getSelectedItem().toString());

        //automatic stuff
        newRestroom.put("createdby", mAuth.getCurrentUser().getUid()); //UID of creator
        newRestroom.put("created", FieldValue.serverTimestamp()); //time created
        newRestroom.put("location", geopoint);
        db.collection("restrooms").add(newRestroom).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(AddARestroomScreen.this, "Added new restroom", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
//                File file = new File(filePath.getPath());// TODO make this delete local photo file ONLY IF CAMERA WAS USED TO TAKE PHOTO
//                file.delete();
                filePath = null;
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                setResult(RESULT_CANCELED);
                Toast.makeText(AddARestroomScreen.this, "Add unsuccessful", Toast.LENGTH_SHORT).show();
//                finish();
            }
        });
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        filePath = Uri.fromFile(image);
        return image;
    }

    public StorageReference uploadImage(){
            if(filePath != null) {
//            Toast.makeText(this, "Uploading image", Toast.LENGTH_SHORT).show();
                final StorageReference ref = storageReference.child("restroom-images/" + UUID.randomUUID().toString());
                ref.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            Toast.makeText(AddARestroomScreen.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                Toast.makeText(AddARestroomScreen.this, "Added to " + ref.getPath(), Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddARestroomScreen.this, "Failed image upload" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                //TODO this causes issues when image upload fails as it still returns the correct ref

                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
//                                    .getTotalByteCount()); //TODO maybe use later?
                            }
                        });
                return ref;
            }

            else{
                return storageReference.child("restroom-images/default"); //default file when image upload fails
            }



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        miniMap = googleMap;
        miniMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);



        final Marker locationMarker = miniMap.addMarker(new MarkerOptions()
                .position(lastLocation)
                .draggable(true));

        miniMap.moveCamera(CameraUpdateFactory.newLatLng(lastLocation));
        miniMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        miniMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                locationMarker.setPosition(latLng);
                miniMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                positionOfMarker = latLng;
            }
        });

        miniMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                Log.d("System out", "onMarkerDragEnd...");
                miniMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
                LatLng latLng = arg0.getPosition();
                locationMarker.setPosition(latLng);
                positionOfMarker = latLng;
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
            }
        });

        Button btnLoc = (Button) findViewById(R.id.useLocationButton);

        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationMarker.setPosition(lastLocation);
                miniMap.animateCamera(CameraUpdateFactory.newLatLng(lastLocation));
                positionOfMarker = lastLocation;

            }
        });


    }



}
