package com.emojidex.emojidexandroid;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by kou on 14/12/10.
 */
public class AbstractDialog extends PopupWindow {
    public AbstractDialog(Context context) {
        super(context = context.getApplicationContext());

        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_blank));
        setOutsideTouchable(true);
        setFocusable(true);
        setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setInputMethodMode(INPUT_METHOD_NOT_NEEDED);

        try
        {
            final Method method = getClass().getMethod("setWindowLayoutType", int.class);
            method.invoke(this, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        catch(NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch(InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }
}
