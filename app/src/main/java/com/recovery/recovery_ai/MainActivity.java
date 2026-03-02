package com.recovery.recovery_ai;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    //page ui elements
    private Button nextBtn, backBtn;

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
        loadProfileFragment();
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
                    loadProfileFragment();
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
                    loadProfileFragment();
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


}
