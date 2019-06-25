package com.robritt.restroomtracker;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RestroomTracker extends Application {
    public FirebaseFirestore db;
    public static FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
}
