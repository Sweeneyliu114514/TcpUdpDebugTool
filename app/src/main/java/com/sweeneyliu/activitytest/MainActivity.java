package com.sweeneyliu.activitytest;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sweeneyliu.activitytest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar TopAppBar = findViewById(R.id.topToolbar);
        setSupportActionBar(TopAppBar);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.tcpUdpFragment, R.id.httpFragment, R.id.userSpaceFragment)
                .build();
        // Android Developer Guide中推荐使用FragmentContainerView代替Fragment作为NavHostFragment
        /*NavHostFragment navHostFragmentGlobal = (NavHostFragment) getSupportFragmentManager().
                findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragmentGlobal.getNavController();*/
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        // 令底部导航栏仅在顶层的三个fragment中可见
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.tcpUdpFragment
                    || destination.getId() == R.id.httpFragment
                    || destination.getId() == R.id.userSpaceFragment) {
                navView.setVisibility(View.VISIBLE);
            } else {
                navView.setVisibility(View.GONE);
            }
        });

    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    //TODO 1. 在顶部App bar右侧添加可点击的用户头像，点击头像进入用户信息界面，可添加修改用户昵称和头像、登出、切换账号等
    //TODO 2. 将用户信息界面设计成右侧弹出的侧边栏，点击头像弹出侧边栏，点击空白处收回侧边栏，使用global aciton使其在所有fragment中可用

}