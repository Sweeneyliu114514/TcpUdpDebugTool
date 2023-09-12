package com.sweeneyliu.activitytest;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sweeneyliu.activitytest.ui.login.LoginActivity;

public class StartupActivity extends AppCompatActivity{
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        finish();
        Intent intent;
        if(currentUser != null){
            intent = new Intent(StartupActivity.this, MainActivity.class);
        }
        else{
            intent = new Intent(StartupActivity.this, LoginActivity.class);
        }
        startActivity(intent);
    }
    //TODO 1. 将StartupActivity以及LoginActivity精简为MainActivity的一个子Fragment
    //TODO 2. 在StartupFragment中判断用户是否已经登录并作相应的navigate
    //TODO 3. optional 为StartupFragment添加一个布局，显示天气和问候语，以及用户欢迎信息等
}
