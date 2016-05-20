package com.emojidex.emojidexandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.ValueCallback;

import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;


public class WebViewActivity extends Activity {
    static final String TAG = "WebViewActivity";
    private static int SELECTED_IMAGE = 1000;

    private ProgressDialog dialog;
    private XWalkView xWalkView;
    private ValueCallback<Uri> callback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        // set loading dialog.
        dialog = new ProgressDialog(WebViewActivity.this);
        dialog.setMessage(getString(R.string.webview_loading));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        xWalkView = (XWalkView) findViewById(R.id.xwalkView);
        xWalkView.addJavascriptInterface(new EmojidexJavaScriptInterface(), "Android");

        xWalkView.setUIClient(new XWalkUIClient(xWalkView){
            @Override
            public void onPageLoadStarted(XWalkView view, String url) {
                super.onPageLoadStarted(view, url);
                dialog.show();
            }

            @Override
            public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
                super.onPageLoadStopped(view, url, status);
                dialog.dismiss();
            }

            @Override
            public void openFileChooser(XWalkView view, ValueCallback<Uri> uploadFile, String acceptType, String capture) {
                if (callback != null) {
                    callback.onReceiveValue(null);
                }
                callback = uploadFile;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Chooser"), SELECTED_IMAGE);
            }
        });

        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");
        xWalkView.load(url, null);
    }

    // for javascript.
    class EmojidexJavaScriptInterface {
        @org.xwalk.core.JavascriptInterface
        public void setUserData(String authToken, String username) {
            UserData userData = UserData.getInstance();
            userData.setUserData(authToken, username);
            setResult(Activity.RESULT_OK);
            dialog.dismiss();
            finish();
        }

        @org.xwalk.core.JavascriptInterface
        public void close() {
            setResult(Activity.RESULT_OK);
            dialog.dismiss();
            finish();
        }

        @org.xwalk.core.JavascriptInterface
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
        if (xWalkView != null) {
            xWalkView.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode != SELECTED_IMAGE || callback == null || resultCode != RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (data != null && data.getData() != null) {
            callback.onReceiveValue(data.getData());
        }
        callback = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && xWalkView.getNavigationHistory().canGoBack()) {
            xWalkView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (xWalkView != null) {
            xWalkView.pauseTimers();
            xWalkView.onHide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (xWalkView != null) {
            xWalkView.resumeTimers();
            xWalkView.onShow();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (xWalkView != null) {
            xWalkView.onDestroy();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (xWalkView != null) {
            xWalkView.onNewIntent(intent);
        }
    }
}
