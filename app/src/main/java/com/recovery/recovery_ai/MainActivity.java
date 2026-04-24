package com.recovery.recovery_ai;

import Logic.*;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    // Our injury model
    Interpreter tflite;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // user input variables
    String injuryRisk = "high";// temp var for dashboard functionality: low, med, high
    // user input variables
    private String cachedRecoveryPlan = null;
    private String cachedRiskLevel = null;
    int intensity = 0;
    private Vector<Workout> workouts = new Vector<>();
    private List<String> user_goals = new ArrayList<>();
    private int user_age = 0, user_weight = 0;
    private String user_height = "", user_first, user_last, userId, user_gender;
    private Bitmap user_image;

    // Our UI elements

    TextView tvResult;

    // Workout processor
    LogWorkout logWorkout;

    // history / edit helpers
    private Workout selectedWorkout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load our model
        try {
            tflite = new Interpreter(FileUtil.loadMappedFile(this, "risk_model.tflite"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initializing our LogWorkout class
        logWorkout = new LogWorkout(tflite);


        user_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.profile_circle_bg);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            userId = user.getUid();
        }

        loadDataFromFirestore();
        loadDashboardFragment();
    }

    private void loadProfileFragment() {
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

    private void loadDashboardFragment() {
        setContentView(R.layout.dashboard);

        // low group
        View lowTitle = findViewById(R.id.lowRiskTitleWrap);
        View bodyLow = findViewById(R.id.bodyLow);
        View dumbbellLow = findViewById(R.id.dumbbellLow);
        View treadmillLow = findViewById(R.id.treadmillLow);

        // moderate group
        View moderateTitle = findViewById(R.id.moderateRiskTitleWrap);
        View bodyModerate = findViewById(R.id.bodyModerate);
        View symbolModerate = findViewById(R.id.symbolModerate);
        View dumbbellModerate = findViewById(R.id.dumbbellModerate);
        View treadmillModerate = findViewById(R.id.treadmillModerate);

        // high group
        View highTitle = findViewById(R.id.highRiskTitleWrap);
        View bodyHigh = findViewById(R.id.bodyHigh);
        View symbolHigh = findViewById(R.id.symbolHigh);
        View dumbbellHigh = findViewById(R.id.dumbbellHigh);
        View treadmillHigh = findViewById(R.id.treadmillHigh);

        // Hide everything first
        lowTitle.setVisibility(View.GONE);
        bodyLow.setVisibility(View.GONE);
        dumbbellLow.setVisibility(View.GONE);
        treadmillLow.setVisibility(View.GONE);

        moderateTitle.setVisibility(View.GONE);
        bodyModerate.setVisibility(View.GONE);
        symbolModerate.setVisibility(View.GONE);
        dumbbellModerate.setVisibility(View.GONE);
        treadmillModerate.setVisibility(View.GONE);

        highTitle.setVisibility(View.GONE);
        bodyHigh.setVisibility(View.GONE);
        symbolHigh.setVisibility(View.GONE);
        dumbbellHigh.setVisibility(View.GONE);
        treadmillHigh.setVisibility(View.GONE);

        switch (injuryRisk) {
            case "low":
                lowTitle.setVisibility(View.VISIBLE);
                bodyLow.setVisibility(View.VISIBLE);
                dumbbellLow.setVisibility(View.VISIBLE);
                treadmillLow.setVisibility(View.VISIBLE);

                break;
            case "medium":
                moderateTitle.setVisibility(View.VISIBLE);
                bodyModerate.setVisibility(View.VISIBLE);
                symbolModerate.setVisibility(View.VISIBLE);
                dumbbellModerate.setVisibility(View.VISIBLE);
                treadmillModerate.setVisibility(View.VISIBLE);

                break;
            case "high":
                highTitle.setVisibility(View.VISIBLE);
                bodyHigh.setVisibility(View.VISIBLE);
                symbolHigh.setVisibility(View.VISIBLE);
                dumbbellHigh.setVisibility(View.VISIBLE);
                treadmillHigh.setVisibility(View.VISIBLE);
                break;
        }
        populateWorkoutStreak();

        TextView tvDashboardSuggestions = findViewById(R.id.tvDashboardSuggestions);

        switch (injuryRisk.toLowerCase()) {
            case "high":
                tvDashboardSuggestions.setText(
                        "⚠️ High injury risk\nRecovery strongly recommended →"
                );
                break;

            case "medium":
                tvDashboardSuggestions.setText(
                        "⚠️ Moderate risk\nCheck your recovery plan →"
                );
                break;

            case "low":
                tvDashboardSuggestions.setText(
                        "✅ You're recovering well\nView optimized recovery plan →"
                );
                break;
        }

        tvDashboardSuggestions.setOnClickListener(v -> {
            loadRecoveryFragment(injuryRisk);
        });
    }

    // HELPER FUNCTIONS FOR RECOVERY SUGGESTIONS
    private float calculateBMI(int weightlbs, String heightString) {
        try{
            String[] parts = heightString.split("'");
            int feet = Integer.parseInt(parts[0]);
            int inches = Integer.parseInt(parts[1]);
            int totalInches = feet * 12 + inches;
            return (weightlbs * 703) / (totalInches * totalInches);
        } catch (Exception e) {
            e.printStackTrace();
            return 22f; // fallback average BMI
        }
    }

    private void typeText(TextView tv, String text) {
        final int delay = 15;

        new android.os.Handler().postDelayed(new Runnable() {
            int index = 0;

            @Override
            public void run() {
                if (index <= text.length()) {
                    tv.setText(text.substring(0, index));
                    index++;
                    new android.os.Handler().postDelayed(this, delay);
                }
            }
        }, delay);
    }

    private void getRecoveryPlan(String risk, TextView recommendationText) {

        // Use cache if available
        if (cachedRecoveryPlan != null && risk.equalsIgnoreCase(cachedRiskLevel)) {
            typeText(recommendationText, cachedRecoveryPlan);
            return;
        }

        // Otherwise call Gemini
        RecoverySuggestions.getRecoveryAdvice(
                risk,
                new RecoverySuggestions.SuggestionCallBack() {

                    @Override
                    public void onSuggestionReceived(String suggestions) {

                        String formatted = suggestions
                                .replace("MOBILITY:", "Mobility\n")
                                .replace("HYDRATION:", "\n Hydration\n")
                                .replace("REST:", "\n Rest\n")
                                .replace("RETURN:", "\n Return\n")
                                .replace("WARNING:", "\n Warning\n");

                        // Save cache
                        cachedRecoveryPlan = formatted;
                        cachedRiskLevel = risk;

                        runOnUiThread(() ->
                                typeText(recommendationText, formatted)
                        );
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() ->
                                recommendationText.setText(
                                        "RecoveryAI Coach is taking a recovery break, come back later.\n\n" +
                                                "Hydrate well\n" +
                                                "Stretch lightly\n" +
                                                "Get good sleep"
                                )
                        );
                    }
                }
        );
    }

    private void loadRecoveryFragment(String risk){
        setContentView(R.layout.recovery_recommendations);

        TextView recommendationText = findViewById(R.id.recommendationText);

        // Loading state
        recommendationText.setText("🧠 Generating your recovery plan...");

        // ✅ Use helper that includes caching
        getRecoveryPlan(risk, recommendationText);
    }

    private void loadLogWorkoutFragment(){
        setContentView(R.layout.workout_log_screen);

        EditText nameText = findViewById(R.id.etExerciseName);
        EditText descText = findViewById(R.id.etDescription);
        EditText durationText = findViewById(R.id.etDurationValue);

        TextView intensityDescription = findViewById(R.id.intensityDescription);
        intensityDescription.setText(getIntensityDescription());

        Button saveBtn = findViewById(R.id.btnSaveIntensity);
        ImageView lightBtn = findViewById(R.id.intensityLight);
        ImageView mildBtn = findViewById(R.id.intensityMild);
        ImageView averageBtn = findViewById(R.id.intensityAverage);
        ImageView hardBtn = findViewById(R.id.intensityHard);
        ImageView veryHardBtn = findViewById(R.id.intensityVeryHard);
        ImageView maxBtn = findViewById(R.id.intensityMax);

        lightBtn.setOnClickListener(v -> {
            intensity = 1;
            intensityDescription.setText(getIntensityDescription());
        });

        mildBtn.setOnClickListener(v -> {
            intensity = 3;
            intensityDescription.setText(getIntensityDescription());
        });

        averageBtn.setOnClickListener(v -> {
            intensity = 5;
            intensityDescription.setText(getIntensityDescription());
        });

        hardBtn.setOnClickListener(v -> {
            intensity = 6;
            intensityDescription.setText(getIntensityDescription());
        });

        veryHardBtn.setOnClickListener(v -> {
            intensity = 8;
            intensityDescription.setText(getIntensityDescription());
        });

        maxBtn.setOnClickListener(v -> {
            intensity = 10;
            intensityDescription.setText(getIntensityDescription());
        });

        saveBtn.setOnClickListener(v -> {
            if (saveBtn != null) saveBtn.setEnabled(false);
            int duration = Integer.parseInt(durationText.getText().toString());

            Workout newWorkout = new Workout(
                    nameText.getText().toString(),
                    descText.getText().toString(),
                    Integer.parseInt(durationText.getText().toString()),
                    intensity
            );

            workouts.add(newWorkout);

            float age = user_age;
            float gender = 1;
            float bmi = calculateBMI(user_weight, user_height);
            String risk = logWorkout.predictInjuryRisk(age, gender, bmi, intensity, duration);
            injuryRisk = risk.toLowerCase();

            Log.d("Model", "Prediction: " + risk);

            if(risk.equals("medium") || risk.equals("high")){
                RecoverySuggestions.getRecoveryAdvice(
                        risk,
                        new RecoverySuggestions.SuggestionCallBack() {
                            @Override
                            public void onSuggestionReceived(String suggestions) {
                                runOnUiThread(() -> {
                                    android.widget.Toast.makeText(
                                            MainActivity.this,
                                            "Recovery Tips:\n" + suggestions,
                                            android.widget.Toast.LENGTH_LONG
                                    ).show();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    android.widget.Toast.makeText(
                                            MainActivity.this,
                                            "Error getting suggestions",
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show();
                                });
                            }
                        }
                );
            }

            nameText.setText("");
            descText.setText("");
            durationText.setText("0");
            intensityDescription.setText("Select an intensity");

            saveWorkoutToFirestore(newWorkout);
            saveBtn.setEnabled(true);

            loadDashboardFragment();
        });
    }

    private void loadHistoryFragment() {
        setContentView(R.layout.workout_log_history);

        TextView subtitle = findViewById(R.id.subtitle);
        RecyclerView rvWorkoutHistory = findViewById(R.id.rvWorkoutHistory);

        subtitle.setText(workouts.size() + " Workouts Logged");
        rvWorkoutHistory.setLayoutManager(new LinearLayoutManager(this));
        rvWorkoutHistory.setAdapter(new WorkoutHistoryAdapter());
    }

    private void loadAccountSettingsFragment() {
        setContentView(R.layout.account_settings);

        EditText emailText = findViewById(R.id.etChangeEmail);
        EditText confirmEmailText = findViewById(R.id.etConfirmEmail);
        EditText passwordText = findViewById(R.id.etChangePassword);
        EditText confirmPasswordText = findViewById(R.id.etConfirmPassword);
        EditText currentPasswordText = findViewById(R.id.etCurrentPassword);
        ImageView saveBtn = findViewById(R.id.SaveAccountButton);

        saveBtn.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            String newEmail = emailText.getText().toString().trim();
            String confirmEmail = confirmEmailText.getText().toString().trim();
            String newPassword = passwordText.getText().toString().trim();
            String confirmPassword = confirmPasswordText.getText().toString().trim();
            String currentPassword = currentPasswordText.getText().toString().trim();

            boolean changingEmail = !newEmail.isEmpty() || !confirmEmail.isEmpty();
            boolean changingPassword = !newPassword.isEmpty() || !confirmPassword.isEmpty();

            if (!changingEmail && !changingPassword) {
                Toast.makeText(this, "Enter a new email or password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (changingEmail) {
                if (!newEmail.equals(confirmEmail)) {
                    Toast.makeText(this, "Emails do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (changingPassword) {
                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPassword.length() < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (currentPassword.isEmpty()) {
                Toast.makeText(this, "Enter your current password", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            user.reauthenticate(credential)
                    .addOnSuccessListener(unused -> {
                        if (changingEmail && changingPassword) {
                            user.updateEmail(newEmail)
                                    .addOnSuccessListener(unused2 ->
                                            user.updatePassword(newPassword)
                                                    .addOnSuccessListener(unused3 ->
                                                            Toast.makeText(this, "Account updated", Toast.LENGTH_SHORT).show())
                                                    .addOnFailureListener(e ->
                                                            Toast.makeText(this, "Password update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()))
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Email update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

                        } else if (changingEmail) {
                            user.updateEmail(newEmail)
                                    .addOnSuccessListener(unused2 ->
                                            Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Email update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

                        } else {
                            user.updatePassword(newPassword)
                                    .addOnSuccessListener(unused2 ->
                                            Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Password update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_LONG).show());
        });
    }

    private void loadSettingsFragment() {
        setContentView(R.layout.settings);
        EditText nameText = findViewById(R.id.etName);
        EditText ageText = findViewById(R.id.etAge);
        EditText heightText = findViewById(R.id.etHeight);
        EditText weightText = findViewById(R.id.etWeight);
        ImageView saveBtn = findViewById(R.id.SaveProfileButton);
        ImageView logOutBtn = findViewById(R.id.LogoutButton);
        ImageView accountSettingsBtn = findViewById(R.id.AccountSettingsButton);
        ImageView deleteAccountBtn = findViewById(R.id.DeleteAccountButton);

        nameText.setText(user_first+" "+user_last);
        ageText.setText(Integer.toString(user_age));
        weightText.setText(Integer.toString(user_weight) + " lbs");
        heightText.setText(user_height);


        logOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, SplashScreenActivity.class));
        });

        accountSettingsBtn.setOnClickListener(v -> {
            loadAccountSettingsFragment();
        });

        deleteAccountBtn.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userId = user.getUid();

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Delete Firestore data first
                db.collection("users").document(userId).delete()
                        .addOnSuccessListener(aVoid -> {
                            user.delete()
                                    .addOnSuccessListener(c -> {
                                        startActivity(new Intent(MainActivity.this, SplashScreenActivity.class));
                                    });
                        });
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                boolean validated = true; //turns false if any input isn't valid, nothing gets saved if false

                //name input validation
                String[] parts = nameText.getText().toString().trim().split("\\s+", 2); // split into at most 2 parts
                if (parts.length == 2) {
                    String firstName = parts[0];
                    String lastName = parts[1];
                } else {
                    Toast.makeText(v.getContext(), "Please enter first and last name", Toast.LENGTH_SHORT).show();
                }

                //height input validation
                String height = heightText.getText().toString();
                String heightPattern = "^[3-9]'([0-9]|1[01])$";
                if (!height.isEmpty()) {
                    if (!height.matches(heightPattern)) {
                        validated = false;
                        Toast.makeText(v.getContext(),
                                "Enter height like 5'2 (feet'inches)",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    validated = false;
                    Toast.makeText(v.getContext(),
                            "Please enter your height",
                            Toast.LENGTH_SHORT).show();
                }

                //age input validation
                int age = Integer.parseInt(ageText.getText().toString());
                if (user_age < 16){
                    validated = false;
                    Toast.makeText(v.getContext(), "Please enter your age (must be at least 16 years old)", Toast.LENGTH_SHORT).show();
                }

                //weight input validation
                String input = weightText.getText().toString();
                String weightDigitsOnly = input.replaceAll("[^0-9]", "");
                if (!weightDigitsOnly.isEmpty()) {
                    if (Integer.parseInt(weightDigitsOnly) < 70) {
                        validated = false;
                        Toast.makeText(v.getContext(),
                                "Weight must be at least 70 lbs",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    validated = false;
                    Toast.makeText(v.getContext(),
                            "Enter a valid weight",
                            Toast.LENGTH_SHORT).show();
                }

                // if all text is valid, save locally and to firestore
                if(validated) {
                    saveBtn.setEnabled(false);
                    user_first = nameText.getText().toString();
                    user_weight = Integer.parseInt(weightDigitsOnly);

                    user_age = Integer.parseInt(ageText.getText().toString());
                    user_height = heightText.getText().toString();



                    /*
                    TODO -- Allow user to change goals in settings menu.
                    List<String> goals = new ArrayList<>();
                    if (true) goals.add("get_fit");
                    if (true) goals.add("lose_weight");
                    if (true) goals.add("build_muscle");
                    if (true) goals.add("prevent_injury");
                     */

                    Map<String, Object> biometricsDoc = new HashMap<>();
                    biometricsDoc.put("gender", user_gender);
                    biometricsDoc.put("age", user_age);
                    biometricsDoc.put("weight", user_weight);
                    biometricsDoc.put("height", user_height);
                    biometricsDoc.put("goals", user_goals);
                    biometricsDoc.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());


                    String historyDocId = String.valueOf(System.currentTimeMillis());

                    // new biometric data stored in latest, old data gets moved to a historical document in case we add features to track biometric trends
                    db.collection("users")
                            .document(userId)
                            .collection("biometrics")
                            .document("latest")
                            .set(biometricsDoc)
                            .addOnSuccessListener(unused -> {
                                // history snapshot named by timestamp
                                db.collection("users")
                                        .document(userId)
                                        .collection("biometrics")
                                        .document(historyDocId)
                                        .set(biometricsDoc)
                                        .addOnSuccessListener(unused2 -> {
                                            Toast.makeText(v.getContext(),
                                                    "Saved successfully!",
                                                    Toast.LENGTH_LONG).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (saveBtn != null) saveBtn.setEnabled(true);
                                            Toast.makeText(v.getContext(),
                                                    "Saved latest, but failed to save history: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        });

                            })
                            .addOnFailureListener(e -> {
                                if (saveBtn != null) saveBtn.setEnabled(true);
                                Toast.makeText(v.getContext(),
                                        "Error saving biometrics: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                }
            }
        });

    }

    private void loadEditWorkoutFragment(Workout workout) {
        setContentView(R.layout.edit_workout);

        selectedWorkout = workout;
        intensity = workout.getIntensity();

        EditText nameText = findViewById(R.id.etExerciseName);
        EditText descText = findViewById(R.id.etDescription);
        EditText durationText = findViewById(R.id.etDurationValue);
        Button saveBtn = findViewById(R.id.btnSaveIntensity);
        Button backBtn = findViewById(R.id.btnBack);

        nameText.setText(workout.getName());
        descText.setText(workout.getDescription());
        durationText.setText(Integer.toString(workout.getDuration()));
        TextView intensityDescription = findViewById(R.id.intensityDescription);
        intensityDescription.setText(getIntensityDescription());

        ImageView lightBtn = findViewById(R.id.intensityLight);
        ImageView mildBtn = findViewById(R.id.intensityMild);
        ImageView averageBtn = findViewById(R.id.intensityAverage);
        ImageView hardBtn = findViewById(R.id.intensityHard);
        ImageView veryHardBtn = findViewById(R.id.intensityVeryHard);
        ImageView maxBtn = findViewById(R.id.intensityMax);

        lightBtn.setOnClickListener(v -> {
            intensity = 1;
            intensityDescription.setText(getIntensityDescription());
        });

        mildBtn.setOnClickListener(v -> {
            intensity = 3;
            intensityDescription.setText(getIntensityDescription());
        });

        averageBtn.setOnClickListener(v -> {
            intensity = 5;
            intensityDescription.setText(getIntensityDescription());
        });

        hardBtn.setOnClickListener(v -> {
            intensity = 6;
            intensityDescription.setText(getIntensityDescription());
        });

        veryHardBtn.setOnClickListener(v -> {
            intensity = 8;
            intensityDescription.setText(getIntensityDescription());
        });

        maxBtn.setOnClickListener(v -> {
            intensity = 10;
            intensityDescription.setText(getIntensityDescription());
        });

        saveBtn.setOnClickListener(v -> {
            if (selectedWorkout == null) return;

            selectedWorkout.setName(nameText.getText().toString());
            selectedWorkout.setDescription(descText.getText().toString());
            selectedWorkout.setDuration(Integer.parseInt(durationText.getText().toString()));
            selectedWorkout.setIntensity(intensity);

            saveWorkoutToFirestore(selectedWorkout);
            loadHistoryFragment();
        });

        backBtn.setOnClickListener(v -> {
            loadHistoryFragment();
        });
    }

    public void onEditWorkoutClick(View view) {
        if (selectedWorkout != null) {
            loadEditWorkoutFragment(selectedWorkout);
        }
    }

    public void onRecoveryClick(View view) {
        loadRecoveryFragment(injuryRisk);
    }

    public void onDashboardClick(View view) {
        loadDashboardFragment();
    }

    public void onLogClick(View view) {
        loadLogWorkoutFragment();
    }

    public void onSettingsClick(View view) {
        loadSettingsFragment();
    }

    public void onHistoryClick(View view) {
        loadHistoryFragment();
    }

    private void loadDataFromFirestore() {
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
                        user_gender = doc.getString("gender") != null ? doc.getString("gender") : "";
                        user_goals = doc.get("user_goals", List.class);
                    } else {
                        Log.d("Firestore", "No biometrics/latest found");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading biometrics/latest", e));

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

        db.collection("users")
                .document(userId)
                .collection("workouts")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    workouts.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String name = doc.getString("name");
                        String description = doc.getString("description");
                        String date = doc.getString("date");

                        Long intensityLong = doc.getLong("intensity");
                        Long durationLong = doc.getLong("duration");

                        int intensity = intensityLong != null ? intensityLong.intValue() : 0;
                        int duration = durationLong != null ? durationLong.intValue() : 0;

                        String docID = doc.getId();

                        workouts.add(new Workout(name, description, date, duration, intensity, docID));
                    }
                    loadDashboardFragment();
                    Log.d("Firestore", "Workouts loaded: " + workouts.size());
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading workouts", e));
    }

    private void saveWorkoutToFirestore(Workout workout) {
        db = FirebaseFirestore.getInstance();

        Map<String, Object> workoutMap = new HashMap<>();
        workoutMap.put("name", workout.getName());
        workoutMap.put("description", workout.getDescription());
        workoutMap.put("date", workout.getDate());
        workoutMap.put("intensity", workout.getIntensity());
        workoutMap.put("duration", workout.getDuration());

        if (!workout.getDocID().isEmpty()) {
            db.collection("users")
                    .document(userId)
                    .collection("workouts")
                    .document(workout.getDocID())
                    .set(workoutMap)
                    .addOnSuccessListener(unused -> Log.d("Firestore", "Workout updated: " + workout.getDocID()))
                    .addOnFailureListener(e -> Log.w("Firestore", "Error updating workout", e));
        } else {
            db.collection("users")
                    .document(userId)
                    .collection("workouts")
                    .add(workoutMap)
                    .addOnSuccessListener(documentReference -> {
                        for (int i = 0; i < workouts.size(); i++) {
                            if (workouts.get(i).equals(workout)) {
                                workouts.get(i).setDocID(documentReference.getId());
                            }
                        }
                        Log.d("Firestore", "Workout saved with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error saving goal", e));
        }
    }

    private void deleteWorkoutFromFirestore(Workout workout) {
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId).collection("workouts").document(workout.getDocID());
        docRef.delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Document successfully deleted"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error deleting document", e));
    }

    private class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.WorkoutViewHolder> {

        @NonNull
        @Override
        public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_workout_history, parent, false);
            return new WorkoutViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
            Workout workout = workouts.get(position);

            holder.day.setText(getDayLabel(workout.getDate()));
            holder.date.setText(getDateLabel(workout.getDate()));
            holder.durationValue.setText(workout.getDuration() + " MINS");

            setFireIcon(holder, workout.getIntensity());

            holder.cardWorkoutItem.setOnClickListener(v -> loadEditWorkoutFragment(workout));
        }

        @Override
        public int getItemCount() {
            return workouts.size();
        }

        class WorkoutViewHolder extends RecyclerView.ViewHolder {
            View cardWorkoutItem;
            TextView day, date, durationValue;
            ImageView fireVeryLight, fireLight, fireMild, fireHard, fireVeryHard, fireMax;

            WorkoutViewHolder(@NonNull View itemView) {
                super(itemView);

                cardWorkoutItem = itemView.findViewById(R.id.cardWorkoutItem);
                day = itemView.findViewById(R.id.day);
                date = itemView.findViewById(R.id.date);
                durationValue = itemView.findViewById(R.id.durationValue);

                fireVeryLight = itemView.findViewById(R.id.fireVeryLight);
                fireLight = itemView.findViewById(R.id.fireLight);
                fireMild = itemView.findViewById(R.id.fireMild);
                fireHard = itemView.findViewById(R.id.fireHard);
                fireVeryHard = itemView.findViewById(R.id.fireVeryHard);
                fireMax = itemView.findViewById(R.id.fireMax);
            }
        }
    }

    private void setFireIcon(WorkoutHistoryAdapter.WorkoutViewHolder holder, int intensityValue) {
        holder.fireVeryLight.setVisibility(View.GONE);
        holder.fireLight.setVisibility(View.GONE);
        holder.fireMild.setVisibility(View.GONE);
        holder.fireHard.setVisibility(View.GONE);
        holder.fireVeryHard.setVisibility(View.GONE);
        holder.fireMax.setVisibility(View.GONE);

        if (intensityValue <= 1) {
            holder.fireVeryLight.setVisibility(View.VISIBLE);
        } else if (intensityValue <= 3) {
            holder.fireLight.setVisibility(View.VISIBLE);
        } else if (intensityValue <= 5) {
            holder.fireMild.setVisibility(View.VISIBLE);
        } else if (intensityValue <= 6) {
            holder.fireHard.setVisibility(View.VISIBLE);
        } else if (intensityValue <= 8) {
            holder.fireVeryHard.setVisibility(View.VISIBLE);
        } else {
            holder.fireMax.setVisibility(View.VISIBLE);
        }
    }

    private String getDayLabel(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "?";

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateString);
            return new SimpleDateFormat("E", Locale.US).format(date);
        } catch (ParseException e) {
            return "?";
        }
    }

    private String getDateLabel(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "";

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateString);
            return new SimpleDateFormat("MMMM d", Locale.US).format(date).toUpperCase(Locale.US);
        } catch (ParseException e) {
            return dateString.toUpperCase(Locale.US);
        }
    }

    private String getIntensityDescription() {

        if(intensity == 1)
            return "Light Activity - You can maintain this activity for hours, easy to breathe and carry a conversation.";
        else if(intensity == 3)
            return "Mild Activity - Occasionally Breathing hard, can hold a short conversation.";
        else if(intensity == 5)
            return "Average Activity - Breathing heavily, can hold a short conversation. Still somewhat comfortable, but becoming noticeably more challenging.";
        else if(intensity == 6)
            return "Hard Activity - Borderline uncomfortable. Short of breath, can speak a sentence.";
        else if(intensity == 8)
            return "Very Hard Activity - Very difficult to maintain exercise intensity. Can barely breathe and speak only a few words.";
        else if(intensity == 10)
            return "Maximum Activity - Feels almost impossible to keep going. Completely out of breath, unable to talk. Cannot maintain for more than a very short time.";
        return "Select an intensity";
    }

    private void populateWorkoutStreak() {
        TextView dayM = findViewById(R.id.dayM);
        TextView dayT = findViewById(R.id.dayT);
        TextView dayW = findViewById(R.id.dayW);
        TextView dayTH = findViewById(R.id.dayTH);
        TextView dayF = findViewById(R.id.dayF);
        TextView dayS = findViewById(R.id.dayS);
        TextView daySUN = findViewById(R.id.daySUN);

        TextView[] streakDays = {dayM, dayT, dayW, dayTH, dayF, dayS, daySUN};

        // Reset all to inactive first
        for (TextView dayView : streakDays) {
            dayView.setBackgroundResource(R.drawable.streak_day_glass);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar today = Calendar.getInstance();

        for (Workout workout : workouts) {
            try {
                Date workoutDate = sdf.parse(workout.getDate());
                if (workoutDate == null) continue;

                Calendar workoutCal = Calendar.getInstance();
                workoutCal.setTime(workoutDate);

                long diffMillis = today.getTimeInMillis() - workoutCal.getTimeInMillis();
                long diffDays = diffMillis / (1000L * 60 * 60 * 24);

                if (diffDays >= 0 && diffDays < 7) {
                    TextView targetDay = getDayViewForCalendarDay(workoutCal.get(Calendar.DAY_OF_WEEK));
                    if (targetDay != null) {
                        targetDay.setBackgroundResource(R.drawable.streak_day_active);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private TextView getDayViewForCalendarDay(int calendarDay) {
        switch (calendarDay) {
            case Calendar.MONDAY:
                return findViewById(R.id.dayM);
            case Calendar.TUESDAY:
                return findViewById(R.id.dayT);
            case Calendar.WEDNESDAY:
                return findViewById(R.id.dayW);
            case Calendar.THURSDAY:
                return findViewById(R.id.dayTH);
            case Calendar.FRIDAY:
                return findViewById(R.id.dayF);
            case Calendar.SATURDAY:
                return findViewById(R.id.dayS);
            case Calendar.SUNDAY:
                return findViewById(R.id.daySUN);
            default:
                return null;
        }
    }
}