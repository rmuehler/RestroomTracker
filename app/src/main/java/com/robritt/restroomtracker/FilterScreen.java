package com.robritt.restroomtracker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Switch;

public class FilterScreen extends AppCompatActivity {

    SharedPreferences filters;
    SharedPreferences.Editor editor;
    Switch handicap, baby;
    RatingBar privacy, cleanliness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_screen);
        filters = getSharedPreferences("filters", MODE_PRIVATE);
        editor = filters.edit();

        handicap = findViewById(R.id.filter_handicap);
        baby = findViewById(R.id.filter_baby);
        privacy = findViewById(R.id.filter_privacy);
        cleanliness = findViewById(R.id.filter_clean);

        handicap.setChecked(filters.getBoolean("handicap", false));
        baby.setChecked(filters.getBoolean("baby", false));
        privacy.setRating(filters.getFloat("privacy", 0));
        cleanliness.setRating(filters.getFloat("cleanliness", 0));

        Button saveButton =  findViewById(R.id.button_filter_save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("baby", baby.isChecked());
                editor.putBoolean("handicap", handicap.isChecked());
                editor.putFloat("privacy", privacy.getRating());
                editor.putFloat("cleanliness", cleanliness.getRating());

                editor.commit();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();

            }
        });

    }
}
