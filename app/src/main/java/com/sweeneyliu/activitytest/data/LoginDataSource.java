package com.sweeneyliu.activitytest.data;

import com.sweeneyliu.activitytest.data.model.LoggedInUser;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private static ArrayList<String> fakeUserList = new ArrayList<>();
    static {
        fakeUserList.add("admin");
        fakeUserList.add("user");
    }

    public Result<LoggedInUser> login(String username, String password) {

        try {
            // TODO: handle loggedInUser authentication

            if(fakeUserList.contains(username)){
                return new Result.Success<>(new LoggedInUser(java.util.UUID.randomUUID().toString(), username));
            }
            else return new Result.Error(new IOException("No such username registered"));
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}