package com.sweeneyliu.activitytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.sweeneyliu.activitytest.databinding.ActivityStartupBinding;
import com.sweeneyliu.activitytest.ui.login.LoginActivity;

import java.util.Objects;

public class StartupActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        ActivityStartupBinding binding = ActivityStartupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsControllerCompat WindowController = new WindowInsetsControllerCompat(getWindow(), binding.getRoot());
        WindowController.hide(WindowInsetsCompat.Type.systemBars());

        new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
            finish();
            Intent i = new Intent(StartupActivity.this, LoginActivity.class);
            startActivity(i);
        }, 2000);
    }
}
