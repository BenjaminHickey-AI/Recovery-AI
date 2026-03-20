package com.recovery.recovery_ai;

import Logic.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;



    //user input variables
    int intensity = 0;
    private Vector<Workout> workouts = new Vector<Workout>();
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

        loadDataFromFirestore();
        loadDashboardFragment();
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

        //UI variables
        Button saveBtn;
        TextView intensityDescription;
        ImageView veryLightBtn, lightBtn, mildBtn, hardBtn, veryHardBtn, maxBtn;
        EditText nameText, descText, durationText;

        //text inputs
        nameText = findViewById(R.id.etExerciseName);
        descText = findViewById(R.id.etDescription);
        durationText = findViewById(R.id.etDurationValue);

        intensityDescription = findViewById(R.id.intensityDescription);
        intensityDescription.setText("Select an intensity");

        //buttons
        saveBtn = findViewById(R.id.btnSaveIntensity);
        veryLightBtn = findViewById(R.id.intensityVeryLight);
        lightBtn = findViewById(R.id.intensityLight);
        mildBtn = findViewById(R.id.intensityMild);
        hardBtn = findViewById(R.id.intensityHard);
        veryHardBtn = findViewById(R.id.intensityVeryHard);
        maxBtn = findViewById(R.id.intensityMax);

        veryLightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intensity = 1;
                intensityDescription.setText("Light Activity - You can maintain this activity for hours, easy to breathe and carry a conversation.");
            }
        });
        lightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intensity = 3;
                intensityDescription.setText("Mild Activity - Occasionally Breathing hard, can hold a short conversation.");
            }
        });
        mildBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intensity = 5;
                intensityDescription.setText("Average Activity - Breathing heavily, can hold a short conversation. Still somewhat comfortable, but becoming noticeably more challenging.");
            }
        });
        hardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intensity = 6;
                intensityDescription.setText("Hard Activity - Borderline uncomfortable. Short of breath, can speak a sentence.");
            }
        });
        veryHardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intensity = 8;
                intensityDescription.setText("Very Hard Activity - Very difficult to maintain exercise intensity. Can barely breathe and speak only a few words.");
            }
        });
        maxBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intensity = 10;
                intensityDescription.setText("Maximum Activity - Feels almost impossible to keep going. Completely out of breath, unable to talk. Cannot maintain for more than a very short time.");
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveBtn != null) saveBtn.setEnabled(false);
                workouts.add(new Workout(nameText.getText().toString(), descText.getText().toString(), Integer.parseInt(durationText.getText().toString()), intensity));
                nameText.setText("");
                descText.setText("");
                durationText.setText("0");
                intensityDescription.setText("Select an intensity");
                saveWorkoutToFirestore(workouts.get(workouts.size()-1));
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

    private void loadDataFromFirestore() {
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

    private void saveWorkoutToFirestore(Workout workout)
    {
        db = FirebaseFirestore.getInstance();

        //
        Map<String, Object> workoutMap = new HashMap<>();
        workoutMap.put("name", workout.getName());
        workoutMap.put("description", workout.getDescription());
        workoutMap.put("date", workout.getDate());
        workoutMap.put("intensity", workout.getIntensity());
        workoutMap.put("duration", workout.getDuration());


        if(!workout.getDocID().isEmpty())
        {
            db.collection("users").document(userId).collection("workouts").get().addOnSuccessListener(querySnapshot ->
            {
                for (DocumentSnapshot document : querySnapshot.getDocuments())
                {
                    if (document.getId().equals(workout.getDocID()))
                    {
                        db.collection("users").document(userId).collection("workouts").document(workout.getDocID()).set(workoutMap);
                    }
                }
            }).addOnFailureListener(e -> Log.w("Firestore", "Error getting documents", e));
        }
        else
        {
            db.collection("users").document(userId).collection("workouts").add(workoutMap).addOnSuccessListener(documentReference ->
            {
                for(int i = 0; i < workouts.size(); i++)
                {
                    if(workouts.get(i).equals(workout))
                        workouts.get(i).setDocID(documentReference.getId());
                }
                Log.d("Firestore", "Workout saved with ID: " + documentReference.getId());
            }).addOnFailureListener(e ->
            {
                Log.e("Firestore", "Error saving goal", e);
            });
        }
    }

    private void deleteWorkoutFromFirestore(Workout workout)
    {
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId).collection("workouts").document(workout.getDocID());
        docRef.delete().addOnSuccessListener(aVoid -> Log.d("Firestore", "Document successfully deleted")).addOnFailureListener(e -> Log.w("Firestore", "Error deleting document", e));
    }
}
