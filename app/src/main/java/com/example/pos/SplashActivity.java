package com.example.pos;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo = findViewById(R.id.ivLogo);
        Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.splash_scale);
        ivLogo.startAnimation(scaleAnim);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginScreen.class));
            finish();
        }, SPLASH_DURATION);
    }
} 