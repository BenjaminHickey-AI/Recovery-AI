package com.recovery.recovery_ai;

import Logic.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;



    //user input variables
    private int user_age = 0, user_weight = 0;
    private String user_height = "", user_first, user_last, userId;
    private Bitmap user_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.profile_circle_bg);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        userId = user.getUid();

        loadData();
        loadDashboardFragment();
    }

    private void loadData() {
        //load biometric data
        db.collection("users")
                .document(userId)
                .collection("biometrics")
                .document("latest")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Long ageL = doc.getLong("age");
                        Long weightL = doc.getLong("weight");

                        user_age = (ageL != null) ? ageL.intValue() : 0;
                        user_weight = (weightL != null) ? weightL.intValue() : 0;
                        user_height = doc.getString("height") != null ? doc.getString("height") : "";
                    } else {
                        Log.d("Firestore", "No biometrics/latest found");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading biometrics/latest", e));

        // Load name from users
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        user_first = doc.getString("first_name") != null ? doc.getString("first_name") : "";
                        user_last = doc.getString("last_name") != null ? doc.getString("last_name") : "";
                    } else {
                        Log.d("Firestore", "No user doc found");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading user doc", e));
    }

    private void loadProfileFragment(){
        setContentView(R.layout.profile_screen);

        TextView weight, height, age, name;

        name = findViewById(R.id.name);
        weight = findViewById(R.id.weight);
        height = findViewById(R.id.height);
        age = findViewById(R.id.age);

        name.setText(user_first + " " + user_last);
        weight.setText(Integer.toString(user_weight));
        age.setText(Integer.toString(user_age));
        height.setText(user_height);
    }

    private void loadDashboardFragment(){
        setContentView(R.layout.dashboard);
    }

    private void loadRecoveryFragment(){
        setContentView(R.layout.recovery_recommendations);
    }

    private void loadLogWorkoutFragment(){
        setContentView(R.layout.workout_log_screen);
        Button strengthBtn, cardioBtn;
        strengthBtn = findViewById(R.id.btnStrength);
        cardioBtn = findViewById(R.id.btnCardio);

        strengthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strengthBtn.setBackgroundResource(R.drawable.pill_green_active);
                cardioBtn.setBackgroundResource(R.drawable.pill_green_dim);
            }
        });

        cardioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strengthBtn.setBackgroundResource(R.drawable.pill_green_dim);
                cardioBtn.setBackgroundResource(R.drawable.pill_green_active);
            }
        });
    }

    private void loadRiskDetailsFragment() {
        setContentView(R.layout.risk_details);
    }

    private void loadSettingsFragment() {
        setContentView(R.layout.settings);
    }

    private void loadTrendsFragment() {
        setContentView(R.layout.trends);
    }

    public void onProfileClick(View view) {
        loadProfileFragment();
    }

    public void onRecoveryClick(View view) {
        loadRecoveryFragment();
    }

    public void onDashboardClick(View view) {
        loadDashboardFragment();
    }

    public void onLogClick(View view) {
        loadLogWorkoutFragment();
    }

    public void onRiskDetailsClick(View view) {
        loadRiskDetailsFragment();
    }

    public void onSettingsClick(View view) {
        loadSettingsFragment();
    }

    public void onTrendsClick(View view) {
        loadTrendsFragment();
    }
}
