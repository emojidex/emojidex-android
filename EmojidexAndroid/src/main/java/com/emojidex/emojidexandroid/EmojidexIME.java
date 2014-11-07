package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kou on 13/08/11.
 */
public class EmojidexIME extends InputMethodService {
    private Emojidex emojidex;

    private InputMethodManager inputMethodManager = null;
    private int showIMEPickerCode = 0;

    private View layout;
    private HorizontalScrollView categoryScrollView;

    private ViewFlipper keyboardViewFlipper;
    private boolean swipeFlag = false;

    private PopupWindow popup;

    private HistoryManager historyManager;
    private KeyboardViewManager keyboardViewManager;

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

        // Initialize Emojidex object.
        emojidex = Emojidex.getInstance();
        emojidex.initialize(this);

        // Create HistoryManager.
        historyManager = new HistoryManager(this);

        // Test.
        final DownloadConfig config = new DownloadConfig();
        config.formats.add(EmojiFormat.toFormat(getResources().getString(R.string.emoji_format_stamp)));
        config.listener = new CustomDownloadListener();
        emojidex.download(config);
    }

    @Override
    public View onCreateInputView() {
        // Create IME layout.
        layout = getLayoutInflater().inflate(R.layout.ime, null);

        createCategorySelector();
        createKeyboardView();
        createSubKeyboardView();

        return layout;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if( !restarting )
            historyManager.load();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        historyManager.save();
    }

    @Override
    public void onWindowShown() {
        // Reset IME
        ChangeCategory(getString(R.string.all_category));
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
        if( !keyboardViewManager.getCurrentView().closePopup() )
            super.hideWindow();
    }

    /**
     * Create category selector.
     */
    private void createCategorySelector()
    {
        // Create category buttons and add to IME layout.
        final ViewGroup categoriesView = (ViewGroup)layout.findViewById(R.id.ime_categories);

        for(final String categoryName : emojidex.getCategoryNames())
        {
            // Skip if already added.
            boolean isFind = false;
            for(int i = 0;  i < categoriesView.getChildCount();  ++i)
            {
                if(categoriesView.getChildAt(i).getContentDescription().equals(categoryName))
                {
                    isFind = true;
                    break;
                }
            }
            if(isFind)
                continue;

            // Create button.
            final Button newButton = new Button(this);

            newButton.setText(categoryName);
            newButton.setContentDescription(categoryName);
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickCategoryButton(v);
                }
            });

            // Add button to IME layout.
            categoriesView.addView(newButton);
        }

        // Create categories scroll buttons.
        categoryScrollView = (HorizontalScrollView)layout.findViewById(R.id.ime_category_scrollview);
    }

    /**
     * Create main KeyboardView object and add to IME layout.
     */
    private void createKeyboardView()
    {
        // Add KeyboardViewFlipper to IME layout.
        keyboardViewFlipper = (ViewFlipper)layout.findViewById(R.id.keyboard_viewFlipper);

        // Create KeyboardViewManager.
        keyboardViewManager = new KeyboardViewManager(this, new CustomOnKeyboardActionListener(), new CustomOnTouchListener());

        for(View view : keyboardViewManager.getViews())
            keyboardViewFlipper.addView(view);
    }

    /**
     * Create sub KeyboardView object and add to IME layout.
     */
    private void createSubKeyboardView()
    {
        // Create KeyboardView.
        EmojidexSubKeyboardView subKeyboardView = new EmojidexSubKeyboardView(this, null, R.attr.subKeyboardViewStyle);
        subKeyboardView.setOnKeyboardActionListener(new CustomOnKeyboardActionListener());
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
     * @param categoryName category name
     */
    private void setKeyboard(String categoryName)
    {
        keyboardViewManager.initialize(emojidex.getEmojiList(categoryName));
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
        }
        else
        {
            keyboardViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.down_in));
            keyboardViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up_out));
        }
        keyboardViewManager.next();
        keyboardViewFlipper.showNext();
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
        }
        else
        {
            keyboardViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up_in));
            keyboardViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.down_out));
        }
        keyboardViewManager.prev();
        keyboardViewFlipper.showPrevious();
    }

    /**
     * When click category button.
     * @param v     Clicked button.
     */
    public void onClickCategoryButton(View v)
    {
        final String categoryName = v.getContentDescription().toString();
        android.util.Log.d("ime", "Click category button : category = " + categoryName);
        ChangeCategory(categoryName);
    }

    /**
     * Change category.
     * @param category  Category.
     */
    public void ChangeCategory(String category)
    {
        if(category.equals(getString(R.string.ime_category_id_history)))
        {
            final List<String> histories = historyManager.getHistories();
            keyboardViewManager.initializeFromName(histories);
        }
        else if(category.equals(getString(R.string.ime_category_id_all)))
        {
            final List<Emoji> emojies = emojidex.getAllEmojiList();
            keyboardViewManager.initialize(emojies);
        }
        else
        {
            final List<Emoji> emojies = emojidex.getEmojiList(category);
            keyboardViewManager.initialize(emojies);
        }
    }

    /**
     * show favorites keyboard
     * @param v view
     */
    public void showFavorites(View v)
    {
        // load favorites
        ArrayList<String> favorites = FileOperation.load(this, FileOperation.FAVORITES);
        keyboardViewManager.initializeFromName(favorites);
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
        boolean result = FileOperation.deleteFile(getApplicationContext(), FileOperation.FAVORITES);
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
        boolean result = FileOperation.deleteFile(getApplicationContext(), FileOperation.HISTORIES);
        historyManager.clear();
        showResultToast(result);
        setKeyboard(getString(R.string.all_category));
    }

    /**
     * create popup window
     * @param view view
     */
    private void createPopupWindow(View view)
    {
        int height = keyboardViewFlipper.getHeight();

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


    /**
     * Custom OnKeyboardActionListener.
     */
    private class CustomOnKeyboardActionListener implements KeyboardView.OnKeyboardActionListener
    {
        @Override
        public void onPress(int primaryCode) {
            // nop
        }

        @Override
        public void onRelease(int primaryCode) {
            // nop
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
                String id = FileOperation.loadPreferences(EmojidexIME.this, FileOperation.KEYBOARD);

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
            else
            {
                // Input emoji.
                final Emoji emoji = emojidex.getEmoji(codes);
                if(emoji != null)
                {
                    getCurrentInputConnection().commitText(emoji.toString(), 1);
                    historyManager.regist(emoji.getName());
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
            // nop
        }

        @Override
        public void swipeLeft() {
            // nop
        }

        @Override
        public void swipeRight() {
            // nop
        }

        @Override
        public void swipeDown() {
            // nop
        }

        @Override
        public void swipeUp() {
            // nop
        }
    }


    /**
     * Custom OnGestureListener.
     */
    private class CustomOnGestureListener implements GestureDetector.OnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent e) {
            // nop
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // nop
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // nop
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // nop
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // nop
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float disX = e1.getX() - e2.getX();
            float disY = e1.getY() - e2.getY();
            if ((Math.abs(disX) < 100) && (Math.abs(disY) < 50))
                return true;

            // closing the popup window.
            EmojidexKeyboardView view = keyboardViewManager.getCurrentView();
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
    }


    /**
     * Custom OnTouchListener.
     */
    private class CustomOnTouchListener implements View.OnTouchListener
    {
        private final GestureDetector detector;

        /**
         * Construct object.
         */
        public CustomOnTouchListener()
        {
            detector = new GestureDetector(getApplicationContext(), new CustomOnGestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return detector.onTouchEvent(event);
        }
    }


    /**
     * Custom download listener.
     */
    private class CustomDownloadListener extends DownloadListener
    {
        private final Handler handler = new Handler();

        @Override
        public void onJsonDownloadCompleted() {
            super.onJsonDownloadCompleted();
        }

        @Override
        public void onEmojiDownloadCompleted(String emojiName) {
            final Emoji emoji = Emojidex.getInstance().getEmoji(emojiName);
            if(emoji != null)
            {
                emoji.reloadImage();

                // Find emoji in current keyboard.
                final EmojidexKeyboardView view = keyboardViewManager.getCurrentView();
                final Keyboard keyboard = view.getKeyboard();
                final List<Keyboard.Key> keys = keyboard.getKeys();

                for(int i = 0;  i < keys.size();  ++i)
                {
                    if(keys.get(i).popupCharacters.equals(emojiName))
                    {
                        final int index = i;

                        handler.post(new Runnable(){
                            @Override
                            public void run() {
                                keyboardViewManager.getCurrentView().invalidateKey(index);
                            }
                        });

                        break;
                    }
                }
            }
        }

        @Override
        public void onAllDownloadCompleted() {
            super.onAllDownloadCompleted();
        }
    }
}
