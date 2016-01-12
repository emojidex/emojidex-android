package com.emojidex.emojidexandroid;

/**
 * Created by Yoshida on 2016/01/12.
 */
public class UserData {
    private static UserData instance = new UserData();
    private String authToken;
    private String username;

    private UserData() {}

    public static UserData getInstance() {
        return instance;
    }

    public void setUserData(String authToken, String username) {
        this.authToken = authToken;
        this.username = username;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getUsername() {
        return  username;
    }
}
