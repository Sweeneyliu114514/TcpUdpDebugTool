package com.sweeneyliu.activitytest.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Patterns;

import com.sweeneyliu.activitytest.data.LoginRepository;
import com.sweeneyliu.activitytest.data.Result;
import com.sweeneyliu.activitytest.data.model.LoggedInUser;
import com.sweeneyliu.activitytest.R;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        Result<LoggedInUser> result = loginRepository.login(username, password);

        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
        } else {
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username) || !isPasswordValid(password)) {
            if (!isPasswordValid(password)) {
                loginFormState.setValue(new LoginFormState(null, R.string.invalid_password, false));
            }
            if (!isUserNameValid(username)) {
                loginFormState.setValue(new LoginFormState(R.string.invalid_username, null, false));
            }
        }
        else {
            loginFormState.setValue(new LoginFormState(null,null,true));
        }
    }

    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        //只有当用户名包含@时且符合邮箱格式时,才返回true
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return false;
        }
    }

    private boolean isPasswordValid(String password) {
        //密码非空白字符长度大于5则返回true
        return password != null && password.trim().length() >= 5;
    }
}