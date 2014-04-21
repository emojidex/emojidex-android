package org.genshin.emojidexandroid;

import android.content.Context;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by kou on 13/08/11.
 */
public class EmojidexIME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener, GestureDetector.OnGestureListener {
    private EmojiDataManager emojiDataManager;

    private InputMethodManager inputMethodManager = null;
    private int showIMEPickerCode = 0;

    private View layout;
    private HorizontalScrollView categoryScrollView;

    private Map<String, CategorizedKeyboard> categorizedKeyboards;

    private ViewFlipper viewFlipper;
    private ViewFlipper keyboardViewFlipper;
    private GestureDetector detector;
    private boolean swipeFlag = false;

    private PopupWindow popup;

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
        categorizedKeyboards = new HashMap<String, CategorizedKeyboard>();
        for(CategoryData categoryData : emojiDataManager.getCategoryDatas())
        {
            final String categoryName = categoryData.getName();
            categorizedKeyboards.put(categoryName,
                    EmojidexKeyboard.create(this, emojiDataManager.getCategorizedList(categoryName), minHeight));
        }

        // Create GestureDetector
        detector = new GestureDetector(getApplicationContext(), this);
    }

    @Override
    public View onCreateInputView() {
        // Create IME layout.
        layout = getLayoutInflater().inflate(R.layout.ime, null);

        createCategorySelector();
        createKeyboardView();
        createSubKeyboardView();

        // Set ViewFlipper action.
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
        hideWindow();
    }

    @Override
    public void hideWindow()
    {
        for (int i = 0; i < keyboardViewFlipper.getChildCount(); i++)
        {
            EmojidexKeyboardView view = (EmojidexKeyboardView)keyboardViewFlipper.getChildAt(i);
            view.closePopup();
        }
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
        if (swipeFlag)
        {
            swipeFlag = false;
            return;
        }

        List<Integer> codes = new ArrayList<Integer>();
        for(int i = 0;  keyCodes[i] != -1;  ++i)
            codes.add(keyCodes[i]);

        StringBuilder buf = new StringBuilder("Click key : primaryCode = 0x" + String.format("%1$08x", primaryCode) + ", keyCodes = 0x");
        for (int i = 0;  i < codes.size();  ++i)
            buf.append( String.format(" %1$08x", keyCodes[i]) );
        buf.append(", length = " + codes.size());
        android.util.Log.d("ime", buf.toString());

        // Input show ime picker or default keyboard.
        if (primaryCode == showIMEPickerCode)
        {
            boolean result = false;
            String id = FileOperation.loadPreferences(this, FileOperation.KEYBOARD);

            List<InputMethodInfo> inputMethodInfoList = inputMethodManager.getEnabledInputMethodList();
            for (int i = 0; i < inputMethodInfoList.size(); ++i) {
                InputMethodInfo inputMethodInfo = inputMethodInfoList.get(i);
                if (inputMethodInfo.getId().equals(id))
                    result = true;
            }

            if (result)
                switchInputMethod(id);
            else
                inputMethodManager.showInputMethodPicker();
        }
        /*
        else if (primaryCode == KeyEvent.KEYCODE_ENTER)
        {
            String hex = Integer.toHexString(getCurrentInputEditorInfo().inputType);
            int type = Integer.parseInt(hex, 16);

            // check multi-line flag
            if ((type & InputType.TYPE_TEXT_FLAG_MULTI_LINE) > 0 ||
                (type & InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE) > 0)
            {
                sendDownUpKeyEvents(primaryCode);
            }
            // when multi-line is not allowed, hide keyboard
            else
            {
                hideWindow();
            }
        }
        */
        else
        {
            // Input emoji.
            final EmojiData emoji = emojiDataManager.getEmojiData(codes);
            if(emoji != null)
            {
                getCurrentInputConnection().commitText(emoji.createImageString(), 1);
                saveHistories(codes);
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
     * Create category selector.
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
        // Add KeyboardViewFlipper to IME layout.
        keyboardViewFlipper = (ViewFlipper)layout.findViewById(R.id.keyboard_viewFlipper);
        keyboardViewFlipper.setOnTouchListener(new FlickTouchListener());
    }

    /**
     * Create sub KeyboardView object and add to IME layout.
     */
    private void createSubKeyboardView()
    {
        // Create KeyboardView.
        EmojidexSubKeyboardView subKeyboardView = new EmojidexSubKeyboardView(this, null, R.attr.subKeyboardViewStyle);
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
     * @param categoryID category id
     */
    private void setKeyboard(String categoryID)
    {
        keyboardViewFlipper.removeAllViews();
        for (int i = 0; i < categorizedKeyboards.get(categoryID).getKeyboards().size(); i++)
        {
            EmojidexKeyboardView keyboardView = new EmojidexKeyboardView(this, null, R.attr.keyboardViewStyle, getLayoutInflater());
            keyboardView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    boolean result = detector.onTouchEvent(event);
                    return result;
                }
            });
            keyboardView.setOnKeyboardActionListener(this);
            keyboardView.setPreviewEnabled(false);
            keyboardView.setKeyboard(categorizedKeyboards.get(categoryID).getKeyboards().get(i));
            keyboardViewFlipper.addView(keyboardView);
        }
    }

    /**
     * setKeyboard from keyboards
     * @param keyboards categorized keyboards
     */
    private void setKeyboard(CategorizedKeyboard keyboards)
    {
        keyboardViewFlipper.removeAllViews();
        for (int i = 0; i < keyboards.getKeyboards().size(); i++)
        {
            KeyboardView keyboardView = new EmojidexKeyboardView(this, null, R.attr.keyboardViewStyle, getLayoutInflater());
            keyboardView.setOnKeyboardActionListener(this);
            keyboardView.setPreviewEnabled(false);
            keyboardView.setKeyboard(keyboards.getKeyboards().get(i));
            keyboardViewFlipper.addView(keyboardView);
        }
    }

    /**
     * viewFlipper move to left
     * @param v view
     */
    public void moveToLeft(View v)
    {
        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out));
        viewFlipper.showNext();
    }

    /**
     * viewFlipper move to right
     * @param v view
     */
    public void moveToRight(View v)
    {
        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out));
        viewFlipper.showPrevious();
    }

    /**
     * move to the next keyboard view
     * @param direction left or down
     */
    public void moveToNextKeyboard(String direction)
    {
        if (direction.equals("left"))
        {
            keyboardViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in));
            keyboardViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out));
            keyboardViewFlipper.showNext();
        }
        else
        {
            keyboardViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.down_in));
            keyboardViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up_out));
            keyboardViewFlipper.showNext();
        }
    }

    /**
     * move to the prev keyboard view
     * @param direction right or up
     */
    public void moveToPrevKeyboard(String direction)
    {
        if (direction.equals("right"))
        {
            keyboardViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in));
            keyboardViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out));
            keyboardViewFlipper.showPrevious();
        }
        else
        {
            keyboardViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up_in));
            keyboardViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.down_out));
            keyboardViewFlipper.showPrevious();
        }
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float disX = e1.getX() - e2.getX();
        float disY = e1.getY() - e2.getY();
        if ((Math.abs(disX) < 100) && (Math.abs(disY) < 50))
            return true;

        // closing the popup window.
        EmojidexKeyboardView view = (EmojidexKeyboardView)keyboardViewFlipper.getCurrentView();
        view.closePopup();

        // left or right
        if (Math.abs(disX) > Math.abs(disY))
        {
            if (disX > 0)
                moveToNextKeyboard("left");
            else
                moveToPrevKeyboard("right");
        }
        // up or down
        else
        {
            if (disY > 0)
                moveToNextKeyboard("down");
            else
                moveToPrevKeyboard("up");
        }
        swipeFlag = true;
        return false;
    }

    /**
     * ViewFlipper's touchListener
     */
    private class FlickTouchListener implements View.OnTouchListener
    {
        private float lastTouchX;
        private float currentX;

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
//                    if (lastTouchX < currentX)
//                        moveToRight(null);
//                    if (lastTouchX > currentX)
//                        moveToLeft(null);
                    break;
            }
            return true;
        }
    }

    /**
     * show histories keyboard
     * @param v view
     */
    public void showHistories(View v)
    {
        // load histories
        ArrayList<List<Integer>> histories = FileOperation.load(this, FileOperation.HISTORIES);
        createNewKeyboards(histories);
    }

    /**
     * show favorites keyboard
     * @param v view
     */
    public void showFavorites(View v)
    {
        // load favorites
        ArrayList<List<Integer>> favorites = FileOperation.load(this, FileOperation.FAVORITES);
        createNewKeyboards(favorites);
    }

    /**
     * create favorites/histories keyboards
     * @param data keys
     */
    private void createNewKeyboards(ArrayList<List<Integer>> data)
    {

        // get emoji
        List<EmojiData> emojiData = new ArrayList<EmojiData>();
        for (List<Integer> codes : data)
            emojiData.add(emojiDataManager.getEmojiData(codes));

        // create keyboards
        final int minHeight = (int)getResources().getDimension(R.dimen.ime_keyboard_area_height);
        CategorizedKeyboard keyboards = EmojidexKeyboard.create(this, emojiData, minHeight);

        setKeyboard(keyboards);
    }

    /**
     * show settings
     * @param v view
     */
    public void showSettings(View v)
    {
        closePopupWindow(v);
        View view = getLayoutInflater().inflate(R.layout.settings, null);
        createPopupWindow(view);
    }

    /**
     * save histories to local data
     * @param keyCodes save keyCodes
     */
    private void saveHistories(List<Integer> keyCodes)
    {
        FileOperation.save(this, keyCodes, FileOperation.HISTORIES);
    }

    /**
     * create popup window
     * @param v view
     */
    public void createDeleteFavoritesWindow(View v)
    {
        closePopupWindow(v);
        View view = getLayoutInflater().inflate(R.layout.popup_delete_all_favorites, null);
        createPopupWindow(view);
    }

    /**
     * delete all favorites data
     * @param v view
     */
    public void deleteAllFavorites(View v)
    {
        closePopupWindow(v);

        // delete
        boolean result = FileOperation.deleteAll(getApplicationContext(), FileOperation.FAVORITES);
        showResultToast(result);
        setKeyboard(getString(R.string.all_category));
    }

    /**
     * create popup window
     * @param v view
     */
    public void createDeleteHistoriesWindow(View v)
    {
        closePopupWindow(v);

        View view = getLayoutInflater().inflate(R.layout.popup_delete_all_histories, null);
        createPopupWindow(view);
    }

    /**
     * delete all histories data
     * @param v
     */
    public void deleteAllHistories(View v)
    {
        closePopupWindow(v);

        // delete
        boolean result = FileOperation.deleteAll(getApplicationContext(), FileOperation.HISTORIES);
        showResultToast(result);
        setKeyboard(getString(R.string.all_category));
    }

    /**
     * create popup window
     * @param view view
     */
    private void createPopupWindow(View view)
    {
        int height = (int)getResources().getDimension(R.dimen.ime_keyboard_area_height);

        // create popup window
        popup = new PopupWindow(this);
        popup.setContentView(view);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.showAtLocation(layout, Gravity.CENTER_HORIZONTAL, 0, -height);
    }

    /**
     * close popup window
     * @param v view
     */
    public void closePopupWindow(View v)
    {
        if (popup != null)
        {
            popup.dismiss();
            popup = null;
        }
    }

    /**
     * show toast
     * @param result success or failure
     */
    private void showResultToast(boolean result)
    {
        if (result)
            Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, R.string.delete_failure, Toast.LENGTH_SHORT).show();
    }
}
