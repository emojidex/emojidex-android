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
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        windowManager.addView(searchWindow, params);
    }
}
