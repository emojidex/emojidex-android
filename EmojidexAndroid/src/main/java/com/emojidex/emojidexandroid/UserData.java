package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.emojidex.libemojidex.Emojidex.Service.User;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import java.io.IOException;


public class UserData {
    private static String PREF_NAME = "user_data";
    private static String KEY_TOKEN = "emojidex_auth_token";
    private static String KEY_NAME = "emojidex_username";
    private static String ENTITY_TOKEN = "entity_auth_token";
    private static String ENTITY_NAME = "entity_username";

    private Context context;
    private static UserData instance = new UserData();
    private String authToken;
    private String username;

    private boolean premium;
    private boolean pro;
    private boolean r18;

    private UserData() {}

    public static UserData getInstance() {
        return instance;
    }

    public void setUserData(String authToken, String username) {
        this.authToken = authToken;
        this.username = username;
        Emojidex.getInstance().setUser(username, authToken);
        getUserData();
        save();
    }

    public void init(Context context) {
        this.context = context;

        if( !isLogined() )
            load();
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getUsername() {
        return  username;
    }

    public boolean isLogined()
    {
        return authToken != null && !authToken.isEmpty();
    }

    private void save() {
        // TODO destroy deprecated (?)
        Crypto crypto = new Crypto(new SharedPrefsBackedKeyChain(context), new SystemNativeCryptoLibrary());
        if (!crypto.isAvailable()) return;

        if (authToken == null || username == null) return;

        // set user data.
        try {
            byte[] cipherTextToken = crypto.encrypt(authToken.getBytes("utf-8"), new Entity(ENTITY_TOKEN));
            byte[] cipherTextName = crypto.encrypt(username.getBytes("utf-8"), new Entity(ENTITY_NAME));

            SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(KEY_TOKEN, Base64.encodeToString(cipherTextToken, Base64.DEFAULT));
            editor.putString(KEY_NAME, Base64.encodeToString(cipherTextName, Base64.DEFAULT));
            editor.apply();
        } catch (KeyChainException | CryptoInitializationException | IOException e) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(KEY_TOKEN, "");
            editor.putString(KEY_NAME, "");
            editor.apply();
        }
    }

    private void load() {
        // TODO destroy deprecated (?)
        Crypto crypto = new Crypto(new SharedPrefsBackedKeyChain(context), new SystemNativeCryptoLibrary());
        if (!crypto.isAvailable()) return;

        // get user data.
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(KEY_TOKEN, "");
        String name = preferences.getString(KEY_NAME, "");

        // user data not exist.
        if (token.equals("") || name.equals("")) {
            authToken = "";
            username = "";
            premium = false;
            pro = false;
            r18 = false;
            Emojidex.getInstance().setUser(username, authToken);
            return;
        }

        // set user data.
        try {
            byte[] decryptedToken = crypto.decrypt(Base64.decode(token, Base64.DEFAULT), new Entity(ENTITY_TOKEN));
            byte[] decryptedName = crypto.decrypt(Base64.decode(name, Base64.DEFAULT), new Entity(ENTITY_NAME));

            authToken = new String(decryptedToken, "utf-8");
            username = new String(decryptedName, "utf-8");
            Emojidex.getInstance().setUser(username, authToken);
            getUserData();
        } catch (KeyChainException | CryptoInitializationException | IOException e) {
            authToken = "";
            username = "";
            Emojidex.getInstance().setUser(username, authToken);
        }
    }

    public void reset() {
        username = "";
        authToken = "";
        premium = false;
        pro = false;
        r18 = false;
        Emojidex.getInstance().setUser(username, authToken);
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.clear().apply();
    }

    public boolean isPremium() { return premium; }

    public boolean isPro() { return pro; }

    public boolean isSubscriber() { return premium || pro; }

    public boolean isR18() { return r18; }

    private void getUserData() {
        User user = new User();
        if (user.authorize(username, authToken)) {
            premium = user.getPremium();
            pro = user.getPro();
            r18 = user.getR18();
        }
    }
}
