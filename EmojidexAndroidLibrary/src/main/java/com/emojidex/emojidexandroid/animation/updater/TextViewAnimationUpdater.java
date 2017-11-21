package com.emojidex.emojidexandroid.animation.updater;

import android.widget.TextView;

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
}
