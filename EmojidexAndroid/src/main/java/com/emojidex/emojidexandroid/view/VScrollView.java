package com.emojidex.emojidexandroid.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by Yoshida on 2017/12/15.
 */

public class VScrollView extends ScrollView {
    public VScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    public void onTouch(MotionEvent ev) {
        super.onTouchEvent(ev);
    }

    public boolean isContains(int touchX, int touchY) {
        Rect rect = new Rect(getLeft(), getTop(), getRight(), getBottom());
        return rect.contains(touchX, touchY);
    }
}
