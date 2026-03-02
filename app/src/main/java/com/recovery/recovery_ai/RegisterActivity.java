package com.recovery.recovery_ai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText firstName, lastName, email, password, confirmPassword;
    private Button registerBtn, backBtn;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Registration Variables and front end links

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        registerBtn = findViewById(R.id.btnSignUp);
        loginLink = findViewById(R.id.btnLogin);
        backBtn = findViewById(R.id.btnBack);

        backBtn.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        // Register Button Functionality
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fNameText = firstName.getText().toString().trim();
                String lNameText = lastName.getText().toString().trim();
                String emailText = email.getText().toString().trim();
                String passwordText = password.getText().toString().trim();
                String confirmPasswordText = confirmPassword.getText().toString().trim();

                if (!validateInput(emailText, passwordText, confirmPasswordText, fNameText, lNameText)) return;

                registerBtn.setEnabled(false);  // Disable button to prevent multiple clicks


                auth.createUserWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(task -> {
                            registerBtn.setEnabled(true);

                            if (task.isSuccessful()) {
                                String userId = auth.getCurrentUser().getUid();//grabs users ID to continue to add data into profile

                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", emailText);
                                userData.put("first_name", fNameText);
                                userData.put("last_name", lNameText);
                                db.collection("users").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegisterActivity.this, SignUpActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            android.util.Log.e("RegisterActivity", "Firestore write failed", e);
                                            Toast.makeText(RegisterActivity.this, "Account created, but profile save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });

                            } else {
                                String errorMessage = getFirebaseErrorMessage(task.getException());
                                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        loginLink.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private boolean validateInput(String email, String password, String confirmPassword, String fname, String lName) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fname.isEmpty() || lName.isEmpty()) {
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

