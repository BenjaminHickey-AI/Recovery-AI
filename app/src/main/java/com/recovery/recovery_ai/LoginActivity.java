package com.recovery.recovery_ai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        auth = FirebaseAuth.getInstance();

        // Take user to registration activity
        //TextView registerLink = findViewById(R.id.btnCreateAccount);
        //registerLink.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Check if the user is already logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }


        // Initialize UI elements after checking login status
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        Button loginBtn = findViewById(R.id.btnSignIn);

        // Log in button functionality
        loginBtn.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString().trim();

            if (!emailText.isEmpty() && !passwordText.isEmpty()) {
                auth.signInWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}