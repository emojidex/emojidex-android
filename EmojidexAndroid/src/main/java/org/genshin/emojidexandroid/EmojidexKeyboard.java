package org.genshin.emojidexandroid;

import android.app.Service;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by kou on 13/08/11.
 */
public class EmojidexKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    View layout;

    @Override
    public void onInitializeInterface() {
        super.onInitializeInterface();
    }

    @Override
    public View onCreateInputView() {
        layout = (View)getLayoutInflater().inflate(R.layout.keyboard, null);
        return layout;
    }

    @Override
    public void onPress(int i) {

    }

    @Override
    public void onRelease(int i) {

    }

    @Override
    public void onKey(int i, int[] ints) {

    }

    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
