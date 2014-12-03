package com.emojidex.emojidexandroid;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.WindowManager;

/**
 * Created by Yoshida on 2014/11/19.
 */
public class EmojidexService extends Service {
    private SearchWindow searchWindow;
    private WindowManager windowManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        searchWindow = new SearchWindow(getApplicationContext());
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        windowManager.addView(searchWindow, params);
    }
}
