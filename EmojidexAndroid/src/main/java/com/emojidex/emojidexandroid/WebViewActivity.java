package com.emojidex.emojidexandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebViewActivity extends Activity {
    static final String TAG = "WebViewActivity";
    private static int SELECTED_IMAGE = 1000;

    private boolean isLogin;
    private ProgressDialog dialog;
    private WebView webView;
    private ValueCallback<Uri> callback;
    private ValueCallback<Uri[]> callbacks;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        // set loading dialog.
        dialog = new ProgressDialog(WebViewActivity.this);
        dialog.setMessage("Loading...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set webView.
        webView = (WebView)findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new LoginJavaScriptInterface(), "Android");

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                dialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dialog.dismiss();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isLogin) {
                    if (url.contains("?locale=")) {
                        url += "&mobile=true";
                    } else {
                        url += "?mobile=true";
                    }
                    webView.loadUrl(url);
                    return false;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                openFileChooser(uploadMsg, acceptType, "");
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                if (callback != null) {
                    callback.onReceiveValue(null);
                }
                callback = uploadMsg;

                fileChoose();
            }

            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                          WebChromeClient.FileChooserParams fileChooserParams) {
                if (callbacks != null) {
                    callbacks.onReceiveValue(null);
                }
                callbacks = filePathCallback;

                fileChoose();

                return true;
            }

            private void fileChoose() {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Chooser"), SELECTED_IMAGE);
            }
        });

        // set webView url.
        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");
        isLogin = intent.getBooleanExtra("login", false);
        webView.loadUrl(url);
    }

    // for javascript.
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != SELECTED_IMAGE || (callback == null && callbacks == null) || resultCode != RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (callback != null) {
            if (data != null && data.getData() != null) {
//                Uri selectedFile = data.getData();
//                String[] filePathColumn = { MediaStore.Images.Media.DATA };
//                Cursor cursor = getContentResolver().query(selectedFile, filePathColumn, null, null, null);
//                cursor.moveToFirst();
//                int index = cursor.getColumnIndex(filePathColumn[0]);
//                String filePath = cursor.getString(index);
//                cursor.close();
//                String filenameSegments[] = filePath.split("/");
//                String filename = filenameSegments[filenameSegments.length - 1];

                Log.e(TAG, "image : " + data.getData());
                callback.onReceiveValue(data.getData());
//                callbacks.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            }
        }

        if (callbacks != null) {
            if (data != null && data.getDataString() != null) {
                Uri[] results = new Uri[]{Uri.parse(data.getDataString())};
                callbacks.onReceiveValue(results);
            }
        }

        callback = null;
        callbacks = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
