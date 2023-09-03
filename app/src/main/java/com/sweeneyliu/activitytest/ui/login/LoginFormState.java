package com.sweeneyliu.activitytest.ui.login;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class LoginFormState {
    @Nullable
    private Integer usernameError;
    @Nullable
    private Integer passwordError;
    private boolean isDataValid;
    LoginFormState(@Nullable Integer usernameError, @Nullable Integer passwordError,boolean isDataValid) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getUsernameError() {
        return usernameError;
    }

    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    boolean isDataValid() {
        return isDataValid;
    }

}