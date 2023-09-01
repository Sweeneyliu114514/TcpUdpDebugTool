package com.sweeneyliu.activitytest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowInsetsControllerCompat;

import com.sweeneyliu.activitytest.databinding.ActivityStartupBinding;

import java.util.Objects;

public class StartupActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        ActivityStartupBinding binding = ActivityStartupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsControllerCompat WindowController = new WindowInsetsControllerCompat(getWindow(), binding.getRoot());
        WindowController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE);

        new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
            Intent i = new Intent(StartupActivity.this, MainActivity.class);
            startActivity(i);
            finish();

        }, 1000);
    }
}
