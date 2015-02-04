package com.emojidex.emojidexandroid;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by kou on 14/12/10.
 */
public abstract class AbstractDialog extends PopupWindow {
    /**
     * Construct object.
     * @param context       Context.
     */
    public AbstractDialog(Context context) {
        super(context = context.getApplicationContext());

        // Set default parameters.
        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_blank));
        setOutsideTouchable(true);
        setFocusable(true);
        setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setInputMethodMode(INPUT_METHOD_NOT_NEEDED);
        setContentView(createContentView(context));

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

    /**
     * Create content view.
     * @return      New content view.
     */
    protected abstract View createContentView(Context context);
}
