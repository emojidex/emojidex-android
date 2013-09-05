package org.genshin.emojidexandroid;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;
import android.widget.ViewFlipper;

/**
 * Created by kou on 13/08/11.
 */
public class EmojidexIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    private EmojiDataManager emojiDataManager;
    private Keyboard keyboard;
    private View layout;

    ViewFlipper viewFlipper;

    @Override
    public void onInitializeInterface() {
        emojiDataManager = new EmojiDataManager(this);
        keyboard = EmojidexKeyboard.create(this, emojiDataManager.getCategorizedList(getString(R.string.ime_all_category_name)));
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

        // set viewFlipper action
        viewFlipper = (ViewFlipper)layout.findViewById(R.id.viewFlipper);
        viewFlipper.setOnTouchListener(new FlickTouchListener());

        return layout;
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        // Input emoji code.
        if(primaryCode > Character.MAX_CODE_POINT)
        {
            EmojiData emoji = emojiDataManager.getEmojiData(primaryCode);

            getCurrentInputConnection().commitText(emoji.getMoji(), 1);
        }
        // Input unicode.
        else
        {
            sendDownUpKeyEvents(primaryCode);
        }
    }

    @Override
    public void onText(CharSequence text) {

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


    private float lastTouchX;
    private float currentX;
    private class FlickTouchListener implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    currentX = event.getX();
                    if (lastTouchX < currentX)
                    {
                        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in));
                        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out));
                        viewFlipper.showPrevious();
                    }
                    if (lastTouchX > currentX)
                    {
                        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in));
                        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out));
                        viewFlipper.showNext();
                    }
                    break;
            }
            return true;
        }
    }
}
