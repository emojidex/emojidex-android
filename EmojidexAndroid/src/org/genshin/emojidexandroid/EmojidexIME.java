package org.genshin.emojidexandroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by kou on 13/08/11.
 */
public class EmojidexIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    private Keyboard keyboard;
    private View layout;

    @Override
    public void onInitializeInterface() {
        keyboard = EmojidexKeyboard.create(this, null);
    }

    @Override
    public View onCreateInputView() {
        // Create IME layout.
        layout = (View)getLayoutInflater().inflate(R.layout.ime, null);

        // Create KeyboardView.
        KeyboardView keyboardView = new KeyboardView(this, null);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);

        // Add KeyboardView to IME layout.
        ScrollView targetView = (ScrollView)layout.findViewById(R.id.ime_keyboard);
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
