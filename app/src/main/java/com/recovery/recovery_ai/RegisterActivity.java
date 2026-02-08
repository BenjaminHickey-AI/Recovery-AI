package com.recovery.recovery_ai;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import android.app.DatePickerDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText email, password, confirmPassword, birthday;
    private Button registerBtn;
    private TextView loginLink;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_ui);

        // Registration Vairables and front end links
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.Confirmpassword);
        birthday = findViewById(R.id.Birthday);
        registerBtn = findViewById(R.id.signUpBtn);
        loginLink = findViewById(R.id.loginLink);
        progressDialog = new ProgressDialog(this);

        // Handle birthday date picker
        birthday.setOnClickListener(v -> showDatePickerDialog());

        // Register Button Functionality
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = email.getText().toString().trim();
                String passwordText = password.getText().toString().trim();
                String confirmPasswordText = confirmPassword.getText().toString().trim();
                String birthdayText = birthday.getText().toString().trim();

                if (!validateInput(emailText, passwordText, confirmPasswordText, birthdayText)) return;

                registerBtn.setEnabled(false);  // Disable button to prevent multiple clicks
                progressDialog.setMessage("Registering...");
                progressDialog.show();

                auth.createUserWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            registerBtn.setEnabled(true);

                            if (task.isSuccessful()) {
                                String userId = auth.getCurrentUser().getUid();//grabs users ID to continue to add data into profile

                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", emailText);
                                userData.put("birthday", birthdayText);

                                db.collection("users").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(RegisterActivity.this, "User saved in Firestore!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });

                                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(RegisterActivity.this, NEXT ACTIVITY));
                                finish();
                            } else {
                                String errorMessage = getFirebaseErrorMessage(task.getException());
                                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }

                        });
            }
        });

        loginLink.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegisterActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                    birthday.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private boolean validateInput(String email, String password, String confirmPassword, String birthday) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || birthday.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String getFirebaseErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            return ((FirebaseAuthException) exception).getMessage();
        }
        return "Registration Failed! Please try again.";
    }
}