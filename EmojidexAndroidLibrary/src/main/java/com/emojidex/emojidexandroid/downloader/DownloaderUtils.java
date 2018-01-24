package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.EmojidexUser;
import com.emojidex.libemojidex.Emojidex.Client;

/**
 * Created by kou on 18/01/24.
 */

class DownloaderUtils {
    /**
     * Create emojidex client.
     * @param user      User.
     * @return          Emojidex client.
     */
    public static Client createEmojidexClient(EmojidexUser user)
    {
        // Create client.
        final Client client = new Client();

        // Login if has username and auth_token.
        final String username = user.getUserName();
        final String authtoken = user.getAuthToken();
        if( !username.isEmpty() && !authtoken.isEmpty() )
        {
            client.getUser().authorize(username, authtoken);
        }

        return client;
    }
}
