package org.genshin.emojidexandroid;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by nazuki on 14/01/08.
 */
public class EmojidexSubKeyboardView extends KeyboardView {
    private Context context;

    /**
     * Construct EmojidexKeyboardView object.
     * @param context
     * @param attrs
     * @param defStyle
     */
    public EmojidexSubKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    /**
     * Behavior when long pressed
     * @param popupKey
     * @return
     */
    @Override
    public boolean onLongPress(Keyboard.Key popupKey)
    {
        int code = getResources().getInteger(R.integer.ime_keycode_show_ime_picker);

        if (popupKey.codes[0] == code)
        {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showInputMethodPicker();
            return true;
        }

        return false;
    }
}
