package com.robritt.restroomtracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoginScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
    }
    public void openMap(View view){ //Change later for validating login, this is just for testing purposes
        Intent intent = new Intent(this, MainMap.class);
        startActivity(intent);
    }
}
