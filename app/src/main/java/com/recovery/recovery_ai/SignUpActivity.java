package com.recovery.recovery_ai;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.graphics.Bitmap;


import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    String userId;
    //shared page ui elements
    private Button nextBtn, backBtn;

    //user input variables
    private boolean isMale = false, isFemale = false;
    private boolean toGetFit = false, toLoseWeight = false, toBuildMuscle = false, toPreventInjury = false;
    private int user_age = 0, user_weight = 0;
    private String user_height = "";
    private Bitmap user_image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.profile_circle_bg);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoSignupOne();
    }


    //Sign up page 1
    private void GoSignupOne() {
        setContentView(R.layout.activity_signup_one);

        //page ui elements
        LinearLayout optionMale, optionFemale;

        //element assigning
        nextBtn = findViewById(R.id.btnNext);
        optionMale = findViewById(R.id.optionMale);
        optionFemale = findViewById(R.id.optionFemale);

        //sets button backgrounds to boolean values, if returning to this screen, your previous choice will still be selected.
        if (isMale)
            optionMale.setBackgroundResource(R.drawable.choice_circle);
        else
            optionMale.setBackgroundResource(R.drawable.choice_circle_dim);
        if (isFemale)
            optionFemale.setBackgroundResource(R.drawable.choice_circle);
        else
            optionFemale.setBackgroundResource(R.drawable.choice_circle_dim);

        optionMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMale = true;
                optionMale.setBackgroundResource(R.drawable.choice_circle);
                isFemale = false;
                optionFemale.setBackgroundResource(R.drawable.choice_circle_dim);
            }
        });

        optionFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMale = false;
                optionMale.setBackgroundResource(R.drawable.choice_circle_dim);
                isFemale = true;
                optionFemale.setBackgroundResource(R.drawable.choice_circle);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMale || isFemale)
                    GoSignupTwo();
                else
                    Toast.makeText(SignUpActivity.this, "Please select a Gender", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Sign up page 2
    private void GoSignupTwo() {
        setContentView(R.layout.activity_signup_two);

        //page ui elements
        Button btnChoose, btnTakePhoto;
        ImageView image;

        //element assigning
        backBtn = findViewById(R.id.btnBack);
        nextBtn = findViewById(R.id.btnNext);
        btnChoose = findViewById(R.id.btnChoose);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        image = findViewById(R.id.imgProfile);

        //set image to what is stored in image variable so previously set image will appear when returning to page
        image.setImageBitmap(user_image);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO -- Write code to access photo library and assign image variable to selected image
            }
        });

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO -- Write code to access camera and assign image variable to taken image
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoSignupOne();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) { //TODO -- Write code to unsure an image exists in image variable
                    GoSignupThree();
                } else {
                    Toast.makeText(SignUpActivity.this, "Please upload a picture", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //sign up page 3
    private void GoSignupThree() {
        setContentView(R.layout.activity_signup_three);

        //page ui elements
        ImageView image;

        //element assigning
        backBtn = findViewById(R.id.btnBack);
        nextBtn = findViewById(R.id.btnNext);
        image = findViewById(R.id.imgUploaded);

        //update image view with stored image
        image.setImageBitmap(user_image);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoSignupTwo();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoSignupFour();
            }
        });
    }

    //sign up page 4
    private void GoSignupFour() {
        setContentView(R.layout.activity_signup_four);

        //page ui elements
        EditText age;

        //element assigning
        backBtn = findViewById(R.id.btnBack);
        nextBtn = findViewById(R.id.btnNext);
        age = findViewById(R.id.age);

        //update age text with stored age.
        age.setText(String.valueOf(user_age));

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoSignupThree();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_age = Integer.parseInt(age.getText().toString());
                if (user_age > 15)
                    GoSignupFive();
                else
                    Toast.makeText(SignUpActivity.this, "Please enter your age (must be at least 16 years old)", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //sign up page 5
    private void GoSignupFive() {
        setContentView(R.layout.activity_signup_five);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //page ui elements
        EditText weight;

        //element assigning
        backBtn = findViewById(R.id.btnBack);
        nextBtn = findViewById(R.id.btnNext);
        weight = findViewById(R.id.weight);

        weight.setText(String.valueOf(user_weight));

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoSignupFour();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_weight = Integer.parseInt(weight.getText().toString());
                if (user_weight > 0)
                    GoSignupSix();
                else
                    Toast.makeText(SignUpActivity.this, "Please enter your weight", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //sign up page 6
    private void GoSignupSix() {
        setContentView(R.layout.activity_signup_six);

        //page ui elements
        EditText height;

        //element assigning
        backBtn = findViewById(R.id.btnBack);
        nextBtn = findViewById(R.id.btnNext);
        height = findViewById(R.id.height);

        height.setText(String.valueOf(user_height));

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoSignupFive();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_height = height.getText().toString();
                if (!user_height.isEmpty()) //TODO -- Test to make sure height input follows correct format of num'num ex(5'2)
                    GoSignupSeven();
                else
                    Toast.makeText(SignUpActivity.this, "Please enter your height", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //Sign up page 7
    private void GoSignupSeven() {
        setContentView(R.layout.activity_signup_seven);

        //page ui elements
        Button getFitBtn, loseWeightBtn, buildMuscleBtn, injuryPreventionBtn, finishBtn;

        //element assigning
        backBtn = findViewById(R.id.btnBack);
        finishBtn = findViewById(R.id.btnFinish);
        getFitBtn = findViewById(R.id.btnGetFit);
        loseWeightBtn = findViewById(R.id.btnLoseWeight);
        buildMuscleBtn = findViewById(R.id.btnBuildMuscle);
        injuryPreventionBtn = findViewById(R.id.btnInjuryPrevention);

        //sets button backgrounds to boolean values, if returning to this screen, your previous choice will still be selected.
        if (toGetFit)
            getFitBtn.setBackgroundResource(R.drawable.skip_pill);
        else
            getFitBtn.setBackgroundResource(R.drawable.black_pill);
        if (toLoseWeight)
            loseWeightBtn.setBackgroundResource(R.drawable.skip_pill);
        else
            loseWeightBtn.setBackgroundResource(R.drawable.black_pill);
        if (toBuildMuscle)
            buildMuscleBtn.setBackgroundResource(R.drawable.skip_pill);
        else
            buildMuscleBtn.setBackgroundResource(R.drawable.black_pill);
        if (toPreventInjury)
            injuryPreventionBtn.setBackgroundResource(R.drawable.skip_pill);
        else
            injuryPreventionBtn.setBackgroundResource(R.drawable.black_pill);

        getFitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toGetFit)
                    getFitBtn.setBackgroundResource(R.drawable.black_pill);
                else
                    getFitBtn.setBackgroundResource(R.drawable.skip_pill);
                toGetFit = !toGetFit;
            }
        });

        loseWeightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toLoseWeight)
                    loseWeightBtn.setBackgroundResource(R.drawable.black_pill);
                else
                    loseWeightBtn.setBackgroundResource(R.drawable.skip_pill);
                toLoseWeight = !toLoseWeight;
            }
        });

        buildMuscleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toBuildMuscle)
                    buildMuscleBtn.setBackgroundResource(R.drawable.black_pill);
                else
                    buildMuscleBtn.setBackgroundResource(R.drawable.skip_pill);
                toBuildMuscle = !toBuildMuscle;
            }
        });

        injuryPreventionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toPreventInjury)
                    injuryPreventionBtn.setBackgroundResource(R.drawable.black_pill);
                else
                    injuryPreventionBtn.setBackgroundResource(R.drawable.skip_pill);
                toPreventInjury = !toPreventInjury;
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoSignupSix();
            }
        });

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toBuildMuscle || toLoseWeight || toGetFit || toPreventInjury)
                    saveToFireStore();
                else
                    Toast.makeText(SignUpActivity.this, "Please select a goal", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveToFireStore() {
        Button finishBtn = findViewById(R.id.btnFinish);
        if (finishBtn != null) finishBtn.setEnabled(false);

        // goals array
        List<String> goals = new ArrayList<>();
        if (toGetFit) goals.add("get_fit");
        if (toLoseWeight) goals.add("lose_weight");
        if (toBuildMuscle) goals.add("build_muscle");
        if (toPreventInjury) goals.add("prevent_injury");

        final String gender = isMale ? "male" : "female";

        Map<String, Object> biometricsDoc = new HashMap<>();
        biometricsDoc.put("gender", gender);
        biometricsDoc.put("age", user_age);
        biometricsDoc.put("weight", user_weight);
        biometricsDoc.put("height", user_height);
        biometricsDoc.put("goals", goals);
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
                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                if (finishBtn != null) finishBtn.setEnabled(true);
                                Toast.makeText(SignUpActivity.this,
                                        "Saved latest, but failed to save history: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });

                })
                .addOnFailureListener(e -> {
                    if (finishBtn != null) finishBtn.setEnabled(true);
                    Toast.makeText(SignUpActivity.this,
                            "Error saving biometrics: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}