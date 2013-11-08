package org.genshin.emojidexandroid;

import android.content.Context;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.ViewFlipper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by kou on 13/08/11.
 */
public class EmojidexIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    private EmojiDataManager emojiDataManager;
    private Map<String, Keyboard> keyboards;

    private InputMethodManager inputMethodManager = null;
    private int showIMEPickerCode = 0;

    private View layout;
    private HorizontalScrollView categoryScrollView;
    private ScrollView keyboardScrollView;
    private KeyboardView keyboardView;
    private KeyboardView subKeyboardView;

    private ViewFlipper viewFlipper;


    /**
     * Construct EmojidexIME object.
     */
    public EmojidexIME()
    {
        setTheme(R.style.IMETheme);
    }

    @Override
    public void onInitializeInterface() {
        // Get InputMethodManager object.
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        showIMEPickerCode = getResources().getInteger(R.integer.ime_keycode_show_ime_picker);

        // Create EmojiDataManager object.
        emojiDataManager = EmojiDataManager.create(this);

        // Create categorized keyboards.
        final int minHeight = (int)getResources().getDimension(R.dimen.ime_keyboard_area_height);
        keyboards = new HashMap<String, Keyboard>();
        for(CategoryData categoryData : emojiDataManager.getCategoryDatas())
        {
            final String categoryName = categoryData.getName();
            Keyboard newKeyboard = EmojidexKeyboard.create(this, emojiDataManager.getCategorizedList(categoryName), minHeight);
            keyboards.put(categoryName, newKeyboard);
        }
    }

    @Override
    public View onCreateInputView() {
        // Create IME layout.
        layout = (View)getLayoutInflater().inflate(R.layout.ime, null);

        createCategorySelector();
        createKeyboardView();
        createSubKeyboardView();

        // set viewFlipper action
        viewFlipper = (ViewFlipper)layout.findViewById(R.id.viewFlipper);
        viewFlipper.setOnTouchListener(new FlickTouchListener());

        return layout;
    }

    @Override
    public void onWindowShown() {
        // Reset IME.
        setKeyboard(getString(R.string.all_category));
        categoryScrollView.scrollTo(0, 0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        super.hideWindow();
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        android.util.Log.d("ime", "Click key : code = 0x" + Integer.toString(primaryCode, 16));

        // Input show ime picker.
        if(primaryCode == showIMEPickerCode)
        {
            inputMethodManager.showInputMethodPicker();
        }
        else
        {
            // Input emoji.
            final EmojiData emoji = emojiDataManager.getEmojiData(primaryCode);
            if(emoji != null)
            {
                getCurrentInputConnection().commitText(emoji.createImageString(), 1);
            }
            // Input other.
            else
            {
                sendDownUpKeyEvents(primaryCode);
            }
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
     * Create category selector..
     */
    private void createCategorySelector()
    {
        // Create category buttons and add to IME layout.
        final ViewGroup categoriesView = (ViewGroup)layout.findViewById(R.id.ime_categories);

        for(final CategoryData categoryData : emojiDataManager.getCategoryDatas())
        {
            // Create button.
            final Button newButton = new Button(this);

            // Set button parametors.
            final Locale locale = Locale.getDefault();
            final boolean isJapanese = locale.equals(Locale.JAPANESE) || locale.equals(Locale.JAPAN);
            final String buttonText = isJapanese ? categoryData.getJapaneseName() : categoryData.getEnglishName();
            newButton.setText(buttonText);
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String categoryName = categoryData.getName();
                    android.util.Log.d("ime", "Click category button : category = " + categoryName);
                    setKeyboard(categoryName);
                }
            });

            // Add button to IME layout.
            categoriesView.addView(newButton);
        }

        // Create categories scroll buttons.
        categoryScrollView = (HorizontalScrollView)layout.findViewById(R.id.ime_category_scrollview);
        final ImageButton leftButton = (ImageButton)layout.findViewById(R.id.ime_category_button_left);
        final ImageButton rightButton = (ImageButton)layout.findViewById(R.id.ime_category_button_right);

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final float currentX = categoryScrollView.getScrollX();
                final int childCount = categoriesView.getChildCount();
                int nextX = 0;
                for(int i = 0;  i < childCount;  ++i)
                {
                    final float childX = categoriesView.getChildAt(i).getX();
                    if(childX >= currentX)
                        break;
                    nextX = (int)childX;
                }
                categoryScrollView.smoothScrollTo(nextX, 0);
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final float currentX = categoryScrollView.getScrollX();
                final int childCount = categoriesView.getChildCount();
                int nextX = 0;
                for(int i = 0;  i < childCount;  ++i)
                {
                    final float childX = categoriesView.getChildAt(i).getX();
                    if(childX > currentX)
                    {
                        nextX = (int)childX;
                        break;
                    }
                }
                categoryScrollView.smoothScrollTo(nextX, 0);
            }
        });
    }

    /**
     * Create main KeyboardView object and add to IME layout.
     */
    private void createKeyboardView()
    {
        // Create KeyboardView.
        keyboardView = new KeyboardView(this, null, R.attr.keyboardViewStyle);
        keyboardView.setOnKeyboardActionListener(this);
        keyboardView.setPreviewEnabled(false);

        // Add KeyboardView to IME layout.
        keyboardScrollView = (ScrollView)layout.findViewById(R.id.ime_keyboard);
        keyboardScrollView.addView(keyboardView);
    }

    /**
     * Create sub KeyboardView object and add to IME layout.
     */
    private void createSubKeyboardView()
    {
        // Create KeyboardView.
        subKeyboardView = new KeyboardView(this, null, R.attr.subKeyboardViewStyle);
        subKeyboardView.setOnKeyboardActionListener(this);
        subKeyboardView.setPreviewEnabled(false);

        // Create Keyboard and set to KeyboardView.
        Keyboard keyboard = new Keyboard(this, R.xml.sub_keyboard);
        subKeyboardView.setKeyboard(keyboard);

        // Add KeyboardView to IME layout.
        ViewGroup targetView = (ViewGroup)layout.findViewById(R.id.ime_sub_keyboard);
        targetView.addView(subKeyboardView);
    }

    /**
     * Set categorized keyboard.
     * @param categoryID
     */
    private void setKeyboard(String categoryID)
    {
        // Set categorized keyboard to KeyboardView.
        keyboardView.setKeyboard(keyboards.get(categoryID));

        // Scroll to top.
        keyboardScrollView.scrollTo(0, 0);
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
