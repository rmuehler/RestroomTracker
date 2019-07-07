package com.robritt.restroomtracker;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import com.google.firestore.v1.DocumentTransform;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddARestroomScreen extends AppCompatActivity  {

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    private Button btnChoose;
    private ImageView imageView;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_add_arestroom_screen);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        final EditText chooseOpenTime = findViewById(R.id.openTime);
        final EditText chooseCloseTime = findViewById(R.id.closeTime);

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
        imageView = (ImageView) findViewById(R.id.imgView);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });


    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }



    public void openViewScreen(View view){
        Intent intent = new Intent(this, RestroomViewScreen.class);
        startActivity(intent);
    }

    public void addRestroom(View view){
        final double latitude = getIntent().getDoubleExtra("latitude",0);
        final double longitude = getIntent().getDoubleExtra("longitude", 0);
        GeoPoint geopoint = new GeoPoint(latitude,longitude);
        final Map<String,Object> newRestroom = new HashMap<>();
        newRestroom.put("location", geopoint);                  //add fields that go into each restroom here
        newRestroom.put("open", true);
        newRestroom.put("createdby", mAuth.getCurrentUser().getUid());
        newRestroom.put("created", FieldValue.serverTimestamp());
        db.collection("restrooms").add(newRestroom).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(AddARestroomScreen.this, "Added new restroom", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                setResult(RESULT_CANCELED);
                Toast.makeText(AddARestroomScreen.this, "Add unsuccessful", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

}
