package com.emojidex.emojidexandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebViewActivity extends Activity {
    static final String TAG = "WebViewActivity";
    private static int SELECTED_IMAGE = 1000;

    private ProgressDialog dialog;
    private WebView webView;
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> callback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        // set loading dialog.
        dialog = new ProgressDialog(WebViewActivity.this);
        dialog.setMessage(getString(R.string.webview_loading));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        webView = (WebView)findViewById(R.id.webView);
        final WebSettings ws = webView.getSettings();

        ws.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new EmojidexJavaScriptInterface(), "Android");

        String versionName = "unknown";
        try
        {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch(PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        ws.setUserAgentString(
                ws.getUserAgentString()
            +   " emojidexNativeClient/" + versionName
        );

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                if(errorCode == WebViewClient.ERROR_UNKNOWN)
                {
                    view.loadUrl(failingUrl);
                    return;
                }

                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
                dialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                dialog.dismiss();
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            public void openFileChooser(ValueCallback<Uri> uploadFile)
            {
                openFileChooser(uploadFile, "");
            }

            public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType)
            {
                openFileChooser(uploadFile, acceptType, "");
            }

            public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture)
            {
                if(uploadMessage != null)
                    uploadMessage.onReceiveValue(null);
                uploadMessage = uploadFile;

                createChooser();
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams)
            {
                if (callback != null) {
                    callback.onReceiveValue(null);
                }
                callback = filePathCallback;

                createChooser();

                return true;
            }

            private void createChooser()
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Chooser"), SELECTED_IMAGE);
            }
        });

        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");
        webView.loadUrl(url);
    }

    // for javascript.
    class EmojidexJavaScriptInterface {
        @android.webkit.JavascriptInterface
        public void setUserData(String authToken, String username) {
            UserData userData = UserData.getInstance();
            userData.setUserData(authToken, username);
            setResult(Activity.RESULT_OK);
            dialog.dismiss();
            finish();
        }

        @android.webkit.JavascriptInterface
        public void close() {
            setResult(Activity.RESULT_OK);
            dialog.dismiss();
            finish();
        }

        @android.webkit.JavascriptInterface
        public void registerEmoji(String result, String message) {
            Intent intent = new Intent();
            intent.putExtra("message", message);
            if (result.equals("true")) {
                setResult(Activity.RESULT_OK, intent);
            } else {
                setResult(Activity.RESULT_FIRST_USER, intent);
            }

            dialog.dismiss();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Error check.
        if(     requestCode != SELECTED_IMAGE
            ||  resultCode != RESULT_OK
            ||  data == null
            ||  data.getData() == null      )
        {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        // For Android version >= 5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if(callback != null)
            {
                callback.onReceiveValue(new Uri[]{data.getData()});
                callback = null;
                return;
            }
        }
        // For Android version < 5.0
        else
        {
            if(uploadMessage != null)
            {
                uploadMessage.onReceiveValue(data.getData());
                uploadMessage = null;
                return;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
