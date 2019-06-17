package com.robritt.restroomtracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RestroomViewScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restroom_view_screen);
    }

    public void openReportScreen(View view){
        Intent intent = new Intent(this, RestroomReportingScreen.class);
        startActivity(intent);
    }
}
