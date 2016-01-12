package com.emojidex.emojidexandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

/**
 * Created by Yoshida on 2016/01/12.
 */
public class WebViewActivity extends Activity {
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = (WebView)findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new LoginJavaScriptInterface(), "Android");

        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");
        webView.loadUrl(url);
    }

    class LoginJavaScriptInterface {
        @JavascriptInterface
        public void setUserData(String authToken, String username) {
            UserData userData = UserData.getInstance();
            userData.setUserData(authToken, username);
            setResult(Activity.RESULT_OK);
            finish();
        }

        @JavascriptInterface
        public void cancel() {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }
}
