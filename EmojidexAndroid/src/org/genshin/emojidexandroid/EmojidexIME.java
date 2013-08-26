package org.genshin.emojidexandroid;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by kou on 13/08/11.
 */
public class EmojidexIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    Keyboard keyboard;
    View layout;

    @Override
    public void onInitializeInterface() {
        keyboard = new Keyboard(this, R.xml.keyboard);
    }

    @Override
    public View onCreateInputView() {
        // load IME layout from xml.
        layout = (View)getLayoutInflater().inflate(R.layout.ime, null);

        // create keyboard view.
        KeyboardView keyboardView = new KeyboardView(this, null);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);

        // add keyboard view to IME layout.
        ViewGroup targetView = (ViewGroup)layout.findViewById(R.id.ime_keyboard);
        targetView.addView(keyboardView);

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
