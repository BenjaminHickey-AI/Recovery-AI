package com.recovery.recovery_ai;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {
    Button loginBtn, signUpBtn;
    ImageView background, background2;
    private final int[] backgrounds = {
            R.drawable.splash_1,
            R.drawable.splash_2,
            R.drawable.splash_3,
            R.drawable.splash_4
    };

    private boolean showingA = true;
    private int index = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());


    private final Runnable cycler = new Runnable() {
        @Override
        public void run() {
            ImageView current = showingA ? background : background2;
            ImageView next = showingA ? background2 : background;

            next.setImageResource(backgrounds[index]);
            index = (index + 1) % backgrounds.length;

            next.setAlpha(0f);
            next.animate().alpha(1f).setDuration(900).start();
            current.animate().alpha(0f).setDuration(900).withEndAction(() -> {

                showingA = !showingA;

                handler.postDelayed(cycler, 4000);
            }).start();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        loginBtn = findViewById(R.id.btnSignIn);
        signUpBtn = findViewById(R.id.btnCreateAccount);
        background = findViewById(R.id.backgroundSplash);
        background2 = findViewById(R.id.backgroundSplash2);
        background.setImageResource(backgrounds[0]);

        background.setAlpha(1f);
        background2.setAlpha(0f);
        showingA = true;

        index = 1;

        handler.postDelayed(cycler, 0);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashScreenActivity.this, RegisterActivity.class));
            }
        });
    }
}