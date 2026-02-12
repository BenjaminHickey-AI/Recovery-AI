package com.recovery.recovery_ai;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // ✅ switch from Splash theme to normal theme ASAP
        setTheme(R.style.Theme_RecoveryAI);

        super.onCreate(savedInstanceState);

        // custom splash screen layout
        setContentView(R.layout.home_splash_screen);

        // Wait 2 seconds, then go to onboarding
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}
