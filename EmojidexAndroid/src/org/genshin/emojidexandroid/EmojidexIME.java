package org.genshin.emojidexandroid;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ViewFlipper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kou on 13/08/11.
 */
public class EmojidexIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    private EmojiDataManager emojiDataManager;
    private Map<String, Keyboard> keyboards;

    private View layout;
    private KeyboardView keyboardView;
    private KeyboardView subKeyboardView;

    ViewFlipper viewFlipper;

    @Override
    public void onInitializeInterface() {
        // Create EmojiDataManager object.
        emojiDataManager = new EmojiDataManager(this);

        // Create categorized keyboards.
        keyboards = new HashMap<String, Keyboard>();
        for(String categoryName : emojiDataManager.getCategoryNames())
        {
            Keyboard newKeyboard = EmojidexKeyboard.create(this, emojiDataManager.getCategorizedList(categoryName));
            keyboards.put(categoryName, newKeyboard);
        }
    }

    @Override
    public View onCreateInputView() {
        // Create IME layout.
        layout = (View)getLayoutInflater().inflate(R.layout.ime, null);

        createCategoryButtons();
        createKeyboardView();
        createSubKeyboardView();

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

    /**
     * Create category buttons and add to IME layout.
     */
    private void createCategoryButtons()
    {
        ViewGroup targetView = (ViewGroup)layout.findViewById(R.id.ime_categories);
        float textSize = getResources().getDimension(R.dimen.ime_text_size);

        for(final String categoryName : emojiDataManager.getCategoryNames())
        {
            Button newButton = new Button(this);
            newButton.setText(categoryName);
            newButton.setTextSize(textSize);
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setKeyboard(categoryName);
                }
            });
            targetView.addView(newButton);
        }
    }

    /**
     * Create main KeyboardView object and add to IME layout.
     */
    private void createKeyboardView()
    {
        // Create KeyboardView.
        keyboardView = new KeyboardView(this, null);
        keyboardView.setOnKeyboardActionListener(this);

        // Add KeyboardView to IME layout.
        ViewGroup targetView = (ViewGroup)layout.findViewById(R.id.ime_keyboard);
        targetView.addView(keyboardView);

        // Set default keyboard.
        setKeyboard(getString(R.string.ime_all_category_name));
    }

    /**
     * Create sub KeyboardView object and add to IME layout.
     */
    private void createSubKeyboardView()
    {
        // Create KeyboardView.
        subKeyboardView = new KeyboardView(this, null);
        subKeyboardView.setOnKeyboardActionListener(this);

        // Create Keyboard and set to KeyboardView.
        Keyboard keyboard = new Keyboard(this, R.xml.sub_keyboard);
        subKeyboardView.setKeyboard(keyboard);

        // Add KeyboardView to IME layout.
        ViewGroup targetView = (ViewGroup)layout.findViewById(R.id.ime_sub_keyboard);
        targetView.addView(subKeyboardView);
    }

    /**
     * Set categorized keyboard.
     * @param categoryName
     */
    private void setKeyboard(String categoryName)
    {
        keyboardView.setKeyboard(keyboards.get(categoryName));
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
