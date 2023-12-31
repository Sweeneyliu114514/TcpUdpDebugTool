package com.sweeneyliu.activitytest.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sweeneyliu.activitytest.MainActivity;
import com.sweeneyliu.activitytest.R;
import com.sweeneyliu.activitytest.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final TextInputLayout usernameWrapper = binding.usernameWrapper;
        final TextInputLayout passwordWrapper = binding.passwordWrapper;
        final Button loginButton = binding.btnLogin;
        final Button signUpButton = binding.btnSignUp;
        final ProgressBar loadingProgressBar = binding.loading;
        final ImageView leftMoegirl = binding.imageMoegirlLeft;
        final ImageView rightMoegirl = binding.imageMoegirlRight;
        //当密码框获得焦点时,让22娘和33娘闭眼
        passwordWrapper.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                leftMoegirl.setImageResource(R.drawable.ic_moegirl_22_eyeclosed);
                rightMoegirl.setImageResource(R.drawable.ic_moegirl_33_eyeclosed);
                Log.d("LoginActivity", "密码框获得焦点");
            } else {
                leftMoegirl.setImageResource(R.drawable.ic_moegirl_22_eyeopened);
                rightMoegirl.setImageResource(R.drawable.ic_moegirl_33_eyeopened);
                Log.d("LoginActivity", "密码框失去焦点");
            }
        });

        //给loginFormState添加观察者,这里使用lambda表达式简化了observer的接口名和抽象方法onChanged的实现
        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            //只有当用户名和密码都合法时,登录按钮才可点击
            loginButton.setEnabled(loginFormState.isDataValid());
            signUpButton.setEnabled(loginFormState.isDataValid());

            if (loginFormState.getUsernameError() != null) {
                usernameWrapper.setErrorEnabled(true);
                usernameWrapper.setError(getString(loginFormState.getUsernameError()));
            } else {
                usernameWrapper.setErrorEnabled(false);
            }
            if (loginFormState.getPasswordError() != null) {
                passwordWrapper.setErrorEnabled(true);
                passwordWrapper.setError(getString(loginFormState.getPasswordError()));
            } else {
                passwordWrapper.setErrorEnabled(false);
            }
        });

        //给loginResult添加观察者
        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            //若登录失败,则显示登录失败的错误码
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            //若登录成功,则更新UI
            if (loginResult.getSuccess() != null) {
                updateUiWithUserLoggedInDummy(loginResult.getSuccess());
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int  count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameWrapper.getEditText().getText().toString(),
                        passwordWrapper.getEditText().getText().toString());
            }
        };
        usernameWrapper.getEditText().addTextChangedListener(afterTextChangedListener);
        passwordWrapper.getEditText().addTextChangedListener(afterTextChangedListener);

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            /*loginViewModel.login(usernameWrapper.getEditText().getText().toString(),
                    passwordWrapper.getEditText().getText().toString());*/
            signIn(usernameWrapper.getEditText().getText().toString(),
                    passwordWrapper.getEditText().getText().toString());
        });
        signUpButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            Log.d("LoginActivity", "当前是否已有用户登录：" + (mAuth.getCurrentUser() != null));
            createAccount(usernameWrapper.getEditText().getText().toString(),
                    passwordWrapper.getEditText().getText().toString());
        });
        //软键盘的回车登录功能
        /*passwordWrapper.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(usernameWrapper.getEditText().getText().toString(),
                        passwordWrapper.getEditText().getText().toString());
            }
            return false;
        });*/

    }

    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("LoginActivity", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUiWithUserSignedUp(user);
                    } else {
                        Log.w("LoginActivity", "createUserWithEmail:failure", task.getException());
                        updateUiWithUserSignedUp(null);
                    }
                });
    }
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d("LoginActivity", "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUiWithUserLoggedIn(user);
                } else {
                    Log.w("LoginActivity", "signInWithEmail:failure", task.getException());
                    updateUiWithUserLoggedIn(null);
                }
            });
    }

    private void updateUiWithUserLoggedIn(FirebaseUser user) {
        binding.loading.setVisibility(View.GONE);
        if(user == null){
            String promptFailed = "登录失败!";
            Toast.makeText(getApplicationContext(), promptFailed, Toast.LENGTH_LONG).show();
        }
        else {
            String prompt = "邮箱：" + user.getEmail() + " 登录成功!";
            Toast.makeText(getApplicationContext(), prompt, Toast.LENGTH_LONG).show();
            //用户登录成功后结束当前activity跳转到MainActivity
            finish();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void updateUiWithUserLoggedInDummy(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        //用户登录成功后结束当前activity跳转到MainActivity
        finish();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void updateUiWithUserSignedUp(FirebaseUser user) {
        binding.loading.setVisibility(View.GONE);
        if(user == null){
            String promptFailed = "注册失败!";
            Toast.makeText(getApplicationContext(), promptFailed, Toast.LENGTH_LONG).show();
        }
        else {
            String prompt = "邮箱：" + user.getEmail() + " 注册成功!";
            Toast.makeText(getApplicationContext(), prompt, Toast.LENGTH_LONG).show();
            //用户注册成功后清除输入框内容，并清空错误提示
            binding.usernameWrapper.getEditText().setText("");
            binding.passwordWrapper.getEditText().setText("");
            binding.usernameWrapper.setErrorEnabled(false);
            binding.passwordWrapper.setErrorEnabled(false);
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}