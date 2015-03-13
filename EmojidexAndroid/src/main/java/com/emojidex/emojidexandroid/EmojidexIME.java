package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by kou on 13/08/11.
 */
public class EmojidexIME extends InputMethodService {
    static final String TAG = MainActivity.TAG + "::EmojidexIME";
    static EmojidexIME currentInstance = null;

    private final Handler invalidateHandler;

    private Emojidex emojidex;

    private InputMethodManager inputMethodManager = null;
    private int showIMEPickerCode = 0;
    private int showSearchWindowCode = 0;
    private EmojidexSubKeyboardView subKeyboardView = null;
    private Keyboard.Key keyEnter = null;
    private int keyEnterIndex;
    private int imeOptions;

    private View layout;
    private HorizontalScrollView categoryScrollView;
    private Button categoryAllButton;

    private ViewFlipper keyboardViewFlipper;
    private boolean swipeFlag = false;

    private PopupWindow popup;

    private SaveDataManager historyManager;
    private SaveDataManager searchManager;
    private KeyboardViewManager keyboardViewManager;

    private String currentCategory = null;

    /**
     * Construct EmojidexIME object.
     */
    public EmojidexIME()
    {
        setTheme(R.style.IMETheme);

        invalidateHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                keyboardViewManager.getCurrentView().invalidateKey(msg.arg1);
            }
        };
    }

    @Override
    public void onInitializeInterface() {
        // Get InputMethodManager object.
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        showIMEPickerCode = getResources().getInteger(R.integer.ime_keycode_show_ime_picker);
        showSearchWindowCode = getResources().getInteger(R.integer.ime_keycode_show_search_window);

        // Initialize Emojidex object.
        emojidex = Emojidex.getInstance();
        emojidex.initialize(this);

        // Create PreferenceManager.
        historyManager = new SaveDataManager(this, SaveDataManager.Type.History);
        searchManager = new SaveDataManager(this, SaveDataManager.Type.Search);

        // Emoji download.
        if(checkExecUpdate())
        {
            final LinkedHashSet<EmojiFormat> formats = new LinkedHashSet<EmojiFormat>();
            formats.add(EmojiFormat.toFormat(getString(R.string.emoji_format_default)));
            formats.add(EmojiFormat.toFormat(getString(R.string.emoji_format_key)));
            formats.add(EmojiFormat.toFormat(getString(R.string.emoji_format_seal)));
            emojidex.download(formats.toArray(new EmojiFormat[formats.size()]), new CustomDownloadListener());
        }
    }

    @Override
    public View onCreateInputView() {
        // Create IME layout.
        layout = getLayoutInflater().inflate(R.layout.ime, null);

        // Get all category button.
        categoryAllButton = (Button)layout.findViewById(R.id.ime_category_button_all);

        createCategorySelector();
        createKeyboardView();
        createSubKeyboardView();

        return layout;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if( !restarting )
        {
            historyManager.load();
            searchManager.load();
        }

        // Get ime options.
        if( (info.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0 )
            imeOptions = EditorInfo.IME_ACTION_NONE;
        else
            imeOptions = info.imeOptions;

        // Set enter key parameter.
        if(keyEnter == null)
            return;

        switch(imeOptions)
        {
            case EditorInfo.IME_ACTION_NONE:
                keyEnter.icon = getResources().getDrawable(R.drawable.key_enter);
                keyEnter.label = null;
                break;
            default:
                keyEnter.icon = null;
                keyEnter.iconPreview = null;
                keyEnter.label = getTextForImeAction(imeOptions);
                break;
        }

        // Redraw keyboard view.
        subKeyboardView.invalidateKey(keyEnterIndex);

        // Set current instance.
        currentInstance = this;
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        currentInstance = null;
        super.onFinishInputView(finishingInput);
        historyManager.save();
    }

    @Override
    public void onWindowShown() {
        // Reset IME
        currentCategory = null;
        categoryScrollView.scrollTo(0, 0);

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        final String defaultCategory = getString(R.string.ime_category_id_all);
        final String startCategory = pref.getString("startCategory", defaultCategory);
        final ViewGroup categoriesView = (ViewGroup)layout.findViewById(R.id.ime_categories);
        final int childCount = categoriesView.getChildCount();
        for(int i = 0;  i < childCount;  ++i)
        {
            final Button button = (Button)categoriesView.getChildAt(i);
            if(button.getContentDescription().equals(startCategory))
            {
                pref.edit().putString("startCategory", defaultCategory).commit();
                button.performClick();
                return;
            }
        }

        pref.edit().putString("startCategory", defaultCategory).commit();
        categoryAllButton.performClick();
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
        final CategoryManager categoryManager = CategoryManager.getInstance();
        categoryManager.initialize(this);

        // Create category buttons and add to IME layout.
        final ViewGroup categoriesView = (ViewGroup)layout.findViewById(R.id.ime_categories);

        for(final String categoryName : emojidex.getCategoryNames())
        {
            categoryManager.add(categoryName, categoryName);
        }

        final int categoryCount = categoryManager.getCategoryCount();
        for(int i = 0;  i < categoryCount;  ++i)
        {
            // Create button.
            final RadioButton newButton = new RadioButton(this);

            newButton.setText(categoryManager.getCategoryText(i));
            newButton.setContentDescription(categoryManager.getCategoryId(i));
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
        subKeyboardView = new EmojidexSubKeyboardView(this, null, R.attr.subKeyboardViewStyle);
        subKeyboardView.setOnKeyboardActionListener(new CustomOnKeyboardActionListener());
        subKeyboardView.setPreviewEnabled(false);

        // Create Keyboard and set to KeyboardView.
        Keyboard keyboard = new Keyboard(this, R.xml.sub_keyboard);
        subKeyboardView.setKeyboard(keyboard);

        // Add KeyboardView to IME layout.
        ViewGroup targetView = (ViewGroup)layout.findViewById(R.id.ime_sub_keyboard);
        targetView.addView(subKeyboardView);

        // Get enter key object.
        final int[] enterCodes = { KeyEvent.KEYCODE_ENTER };
        final List<Keyboard.Key> keys = keyboard.getKeys();
        final int count = keys.size();
        for(keyEnterIndex = 0;  keyEnterIndex < count;  ++keyEnterIndex)
        {
            final Keyboard.Key key = keys.get(keyEnterIndex);
            if( Arrays.equals(key.codes, enterCodes) )
            {
                keyEnter = key;
                break;
            }
        }
    }

    /**
     * Get update flag.
     * @return  true when execution update.
     */
    private boolean checkExecUpdate()
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        final long lastUpdateTime = pref.getLong(getString(R.string.preference_key_last_update_time), 0);
        final long currentTime = new Date().getTime();
        final long updateInterval = Long.parseLong(pref.getString(getString(R.string.preference_key_update_interval), getString(R.string.preference_entryvalue_update_interval_default)));
        return (currentTime - lastUpdateTime) > updateInterval;
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
        Log.d(TAG, "Click category button : category = " + categoryName);
        changeCategory(categoryName);
    }

    /**
     * Change category.
     * @param category  Category.
     */
    public void changeCategory(String category)
    {
        if(currentCategory != null && currentCategory.equals(category))
            return;

        currentCategory = category;

        if(category.equals(getString(R.string.ime_category_id_history)))
        {
            final List<String> emojiNames = historyManager.getEmojiNames();
            keyboardViewManager.initializeFromName(emojiNames);
        }
        else if(category.equals(getString(R.string.ime_category_id_search)))
        {
            final List<String> emojiNames = searchManager.getEmojiNames();
            keyboardViewManager.initializeFromName(emojiNames);
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
//    public void showFavorites(View v)
//    {
//        // load favorites
//        ArrayList<String> favorites = FileOperation.load(this, FileOperation.FAVORITES);
//        keyboardViewManager.initializeFromName(favorites);
//    }

    /**
     * show settings
     * @param v view
     */
//    public void showSettings(View v)
//    {
//        closePopupWindow(v);
//        View view = getLayoutInflater().inflate(R.layout.settings, null);
//        createPopupWindow(view);
//    }

    /**
     * create popup window
     * @param v view
     */
//    public void createDeleteFavoritesWindow(View v)
//    {
//        closePopupWindow(v);
//        View view = getLayoutInflater().inflate(R.layout.popup_delete_all_favorites, null);
//        createPopupWindow(view);
//    }

    /**
     * delete all favorites data
     * @param v view
     */
//    public void deleteAllFavorites(View v)
//    {
//        closePopupWindow(v);
//
//        // delete
//        boolean result = FileOperation.deleteFile(getApplicationContext(), FileOperation.FAVORITES);
//        showResultToast(result);
//        currentCategory = null;
//        categoryAllButton.performClick();
//    }

    /**
     * create popup window
     * @param view view
     */
//    private void createPopupWindow(View view)
//    {
//        int height = keyboardViewFlipper.getHeight();
//
//        // create popup window
//        popup = new PopupWindow(this);
//        popup.setContentView(view);
//        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
//        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
//        popup.showAtLocation(layout, Gravity.CENTER_HORIZONTAL, 0, -height);
//    }

    /**
     * close popup window
     * @param v view
     */
//    public void closePopupWindow(View v)
//    {
//        if (popup != null)
//        {
//            popup.dismiss();
//            popup = null;
//        }
//    }

    /**
     * show toast
     * @param result success or failure
     */
//    private void showResultToast(boolean result)
//    {
//        if (result)
//            Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
//        else
//            Toast.makeText(this, R.string.delete_failure, Toast.LENGTH_SHORT).show();
//    }

    /**
     * Re-draw key.
     * @param emojiName     Emoji name.
     */
    void invalidate(String emojiName)
    {
        final EmojidexKeyboardView view = keyboardViewManager.getCurrentView();
        final Keyboard keyboard = view.getKeyboard();
        final List<Keyboard.Key> keys = keyboard.getKeys();

        for(int i = 0;  i < keys.size();  ++i)
        {
            if(keys.get(i).popupCharacters.equals(emojiName))
            {
                final Message msg = invalidateHandler.obtainMessage();
                msg.arg1 = i;
                invalidateHandler.sendMessage(msg);

                break;
            }
        }
    }

    /**
     * Reload search result tab.
     */
    void reloadSearchResult()
    {
        searchManager.load();
        if(currentCategory.equals(getString(R.string.ime_category_id_search)))
        {
            final String category = currentCategory;
            currentCategory = null;
            changeCategory(category);
        }
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

            final List<Integer> codes = new ArrayList<Integer>();
            for(int i = 0;  i < keyCodes.length && keyCodes[i] != -1;  ++i)
                codes.add(keyCodes[i]);

            // Input show ime picker or default keyboard.
            if (primaryCode == showIMEPickerCode)
            {
                boolean hasDefaultIME = false;
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(EmojidexIME.this);
                final String idNothing = getString(R.string.preference_entryvalue_default_keyboard_nothing);
                final String defaultIME = prefs.getString(getString(R.string.preference_key_default_keyboard), idNothing);

                if( !defaultIME.equals(idNothing) ) {
                    for (InputMethodInfo info : inputMethodManager.getEnabledInputMethodList()) {
                        if (info.getId().equals(defaultIME))
                            hasDefaultIME = true;
                    }
                }

                if (hasDefaultIME)
                    switchInputMethod(defaultIME);
                else
                    inputMethodManager.showInputMethodPicker();
            }
            else if (primaryCode == showSearchWindowCode)
            {
                showSearchWindow();
            }
            else
            {
                // Input emoji.
                final Emoji emoji = emojidex.getEmoji(codes);
                if(emoji != null)
                {
                    getCurrentInputConnection().commitText(emoji.toEmojidexString(), 1);
                    historyManager.addFirst(emoji.getName());
                }
                // Input enter key.
                else if(primaryCode == KeyEvent.KEYCODE_ENTER && imeOptions != EditorInfo.IME_ACTION_NONE)
                {
                    getCurrentInputConnection().performEditorAction(imeOptions);
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

        /**
         * Show emoji search window.
         */
        private void showSearchWindow() {
            final Intent intent = new Intent(EmojidexIME.this, SearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
        @Override
        public void onPostAllJsonDownload(EmojiDownloader downloader) {
            super.onPostAllJsonDownload(downloader);
        }

        @Override
        public void onPreAllEmojiDownload() {
            emojidex.reload();

            final String category = currentCategory;
            currentCategory = null;
            changeCategory(category);
        }

        @Override
        public void onPostOneEmojiDownload(String emojiName) {
            final Emoji emoji = Emojidex.getInstance().getEmoji(emojiName);
            if(emoji != null)
            {
                emoji.reloadImage();

                if(currentInstance != null)
                    currentInstance.invalidate(emojiName);
            }
        }

        @Override
        public void onPostAllEmojiDownload() {
            super.onPostAllEmojiDownload();

            // Save update time.
            final long updateTime = new Date().getTime();
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(EmojidexIME.this);
            final SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putLong(getString(R.string.preference_key_last_update_time), updateTime);
            prefEditor.commit();
        }
    }
}
