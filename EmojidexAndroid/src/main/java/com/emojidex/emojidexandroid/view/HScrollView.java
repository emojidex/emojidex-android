package com.emojidex.emojidexandroid.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by Yoshida on 2017/12/15.
 */

public class HScrollView extends HorizontalScrollView {
    public HScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    public void onTouch(MotionEvent ev) {
        super.onTouchEvent(ev);
    }
}
