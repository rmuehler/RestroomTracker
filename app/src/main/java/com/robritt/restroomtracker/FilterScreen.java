package com.robritt.restroomtracker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

public class FilterScreen extends AppCompatActivity {

    SharedPreferences filters;
    SharedPreferences.Editor editor;
    Switch handicap, baby;
    RatingBar privacy, cleanliness;
    SeekBar distance;
    TextView distance_display;

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
        distance = findViewById(R.id.filter_distance);
        distance_display = findViewById(R.id.text_filter_distance);


        distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance_display.setText(progress + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        handicap.setChecked(filters.getBoolean("handicap", false));
        baby.setChecked(filters.getBoolean("baby", false));
        privacy.setRating(filters.getFloat("privacy", 0));
        cleanliness.setRating(filters.getFloat("cleanliness", 0));
        distance.setProgress(filters.getInt("distance", 10));

        Button saveButton =  findViewById(R.id.button_filter_save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("baby", baby.isChecked());
                editor.putBoolean("handicap", handicap.isChecked());
                editor.putFloat("privacy", privacy.getRating());
                editor.putFloat("cleanliness", cleanliness.getRating());
                editor.putInt("distance", distance.getProgress());

                editor.commit();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();

            }
        });

    }
}
