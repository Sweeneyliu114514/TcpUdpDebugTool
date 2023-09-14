package com.sweeneyliu.activitytest.ui.userspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sweeneyliu.activitytest.StartupActivity;
import com.sweeneyliu.activitytest.databinding.FragmentUserSpaceBinding;

public class UserSpaceFragment extends Fragment {

    private FragmentUserSpaceBinding binding;
    private FirebaseAuth mAuth;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UserSpaceViewModel userSpaceViewModel =
                new ViewModelProvider(this).get(UserSpaceViewModel.class);

        binding = FragmentUserSpaceBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 将已登录用户信息显示在界面上
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userInfo = "欢迎！用户：" + currentUser.getEmail() + "\n您的UID是："+currentUser.getUid();
        binding.textUserInfo.setText(userInfo);
        // 为注销按钮添加点击事件
        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            // 结束当前Activity，跳转到登录界面
            Intent intent = new Intent(getActivity(), StartupActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}