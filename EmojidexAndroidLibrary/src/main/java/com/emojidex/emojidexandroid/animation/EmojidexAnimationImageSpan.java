package com.emojidex.emojidexandroid.animation;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

/**
 * Created by kou on 16/12/09.
 */

public class EmojidexAnimationImageSpan extends DynamicDrawableSpan
{
    private final EmojidexAnimationDrawable drawable;

    public EmojidexAnimationImageSpan(EmojidexAnimationDrawable d)
    {
        super();

        drawable = d;
    }

    @Override
    public Drawable getDrawable()
    {
        return drawable;//.getCurrentFrame();
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint)
    {
        Drawable b = drawable.getCurrentFrame();
        canvas.save();

        int transY = bottom - b.getBounds().bottom;
        if (mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.getFontMetricsInt().descent;
        }

        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }
}
