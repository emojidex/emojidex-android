package com.emojidex.emojidexandroid;

/**
 * Created by kou on 17/09/08.
 */

public class EmojidexUser {
    private final String username;
    private final String authtoken;

    EmojidexUser()
    {
        this("", "");
    }

    EmojidexUser(String username, String authtoken)
    {
        this.username = (username == null) ? "" : username;
        this.authtoken = (authtoken == null) ? "" : authtoken;
    }

    public String getUserName()
    {
        return username;
    }

    public String getAuthToken()
    {
        return authtoken;
    }
}
