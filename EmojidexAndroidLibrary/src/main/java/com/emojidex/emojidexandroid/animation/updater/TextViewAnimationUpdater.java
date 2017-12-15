package com.emojidex.emojidexandroid.animation.updater;

import android.text.Spannable;
import android.widget.TextView;

import com.emojidex.emojidexandroid.animation.EmojidexAnimationDrawable;
import com.emojidex.emojidexandroid.animation.EmojidexAnimationImageSpan;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by kou on 17/11/21.
 */

public class TextViewAnimationUpdater implements AnimationUpdater {
    private final TextView view;

    /**
     * Construct TextViewAnimationUpdater.
     * @param textView      Target text view.
     */
    public TextViewAnimationUpdater(TextView textView)
    {
        view = textView;
    }

    @Override
    public void update()
    {
        view.setText(view.getText());
    }

    @Override
    public Collection<EmojidexAnimationDrawable> getDrawables()
    {
        final ArrayList<EmojidexAnimationDrawable> drawables = new ArrayList<EmojidexAnimationDrawable>();
        final Spannable text = (Spannable)view.getText();
        for(EmojidexAnimationImageSpan span : text.getSpans(0, text.length(), EmojidexAnimationImageSpan.class))
            drawables.add((EmojidexAnimationDrawable)span.getDrawable());
        return drawables;
    }
}
