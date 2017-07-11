package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.DynamicDrawableSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.emojidex.emojidexandroid.downloader.DownloadListener;
import com.emojidex.emojidexandroid.downloader.EmojiDownloader;
import com.emojidex.libemojidex.Emojidex.Service.User;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

public class MainActivity extends Activity {
    static final String TAG = "EmojidexAndroid";
    private static final int LOGIN_RESULT = 1000;
    private static final int REGISTER_RESULT = 1001;
    private static final String EMOJIDEX_URL = "https://www.emojidex.com";

    private InputMethodManager inputMethodManager;
    private boolean isAnimating;

    private AdView adView;
    private FirebaseAnalytics analytics;

    private final DownloadListener downloadListener = new CustomDownloadListener();
    private EmojiFormat defaultFormat;
    private EmojiFormat keyFormat;
    private Handler updateTextHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        defaultFormat = EmojiFormat.toFormat(getString(R.string.emoji_format_default));
        keyFormat = EmojiFormat.toFormat(getString(R.string.emoji_format_key));

        updateTextHandler = new Handler() {
            @Override
            public void handleMessage(Message msg)
            {
                editText.removeTextChangedListener(textWatcher);

                final int oldStart = editText.getSelectionStart();
                final int oldEnd = editText.getSelectionEnd();

                final Editable text = editText.getText();
                text.replace(
                        0, text.length(),
                        toggleState ? emojify(text, false) : toUnicodeString(text)
                );

                final int length = editText.length();
                editText.setSelection(
                        Math.min(oldStart, length),
                        Math.min(oldEnd, length)
                );

                editText.addTextChangedListener(textWatcher);
            }
        };

        initEmojidexEditor();
        getIntentData();

        showTutorial();
        imeEnableCheck();

        initAds();
        setAdsVisibility();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_text_copy:
                copyText(null, "menu_copy_text");
                return true;
            case R.id.action_clear_and_paste:
                clearAndPaste(null);
                return true;
            case R.id.action_clear:
                clearText(null, "menu_clear_text");
                return true;
            case R.id.action_clear_search:
                clearSearchResult();
                return true;
            case R.id.action_conversion_switch:
                switchToggle();
                return true;
            case R.id.action_emojidex_web:
                openEmojidexWebSite();
                return true;
            case R.id.action_settings:
                openSettings(null);
                return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence("text", editText.getText().toString());
        outState.putBoolean("toggle", toggleState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        toggleButton.setChecked(savedInstanceState.getBoolean("toggle"));
        toggleState = savedInstanceState.getBoolean("toggle");
        editText.setText(savedInstanceState.getCharSequence("text"));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        emojidex.addDownloadListener(downloadListener);
    }

    @Override
    protected void onStop()
    {
        emojidex.removeDownloadListener(downloadListener);

        super.onStop();
    }

    /**
     * Emojidex
     */
    private Emojidex emojidex = null;

    private View rootView;

    private EditText editText;
    private final TextWatcher textWatcher = new CustomTextWatcher();

    private ToggleButton toggleButton;
    private boolean toggleState = true;

    private Button loginButton;
    private Button newEmojiButton;
    private Button myEmojiButton;
    private UserData userData;

    private void initEmojidexEditor()
    {
        new CacheAnalyzer().analyze(this);

        // Initialize emojdiex.
        emojidex = Emojidex.getInstance();
        emojidex.initialize(this);

        // Get root view.
        rootView = findViewById(R.id.activity_main_root);

        // Get edit text.
        editText = (EditText)findViewById(R.id.edit_text);

        // detects input
        editText.addTextChangedListener(textWatcher);

        // detects focus
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(hasFocus)
                {
                    stopAnimation();
                }
                else
                {
                    startAnimation();
                }
            }
        });
        editText.requestFocus();

        // toggle button state
        toggleButton = (ToggleButton)findViewById(R.id.toggle_button);
        toggleState = toggleButton.isChecked();

        // for emojidex web.
        loginButton = (Button)findViewById(R.id.login_button);
        newEmojiButton = (Button)findViewById(R.id.new_emoji_button);
        myEmojiButton = (Button)findViewById(R.id.my_emoji_button);
        userData = UserData.getInstance();
        userData.init(this);

        if (userData.isLogined()) {
            setLoginButtonVisibility(false);
        }
    }

    private CharSequence emojify(final CharSequence cs)
    {
        return emojify(cs, true);
    }

    private CharSequence emojify(final CharSequence cs, boolean autoDownload)
    {
        return autoDownload
                ? emojidex.emojify(cs, true, true, defaultFormat, defaultFormat, keyFormat)
                : emojidex.emojify(cs, true, true, defaultFormat, null);
    }

    private CharSequence deEmojify(final CharSequence cs)
    {
        return emojidex.deEmojify(cs);
    }

    private CharSequence toUnicodeString(final CharSequence cs)
    {
        return emojidex.emojify(cs, true, false);
    }

    /**
     * share
     * @param v
     */
    public void shareData(View v)
    {
        if (editText.getText().toString().equals(""))
            return;

        String data = setShareData();
        createList(data);
    }

    /**
     * set share data
     * @return data
     */
    private String setShareData()
    {
        String text;

        if (toggleState)
            text = toUnicodeString(editText.getText()).toString();
        else
            text = editText.getText().toString();

        return  text;
    }

    /**
     * Custom TextWatcher
     */
    private class CustomTextWatcher implements TextWatcher
    {
        private int start, end;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            // nop
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            this.start = start;
            this.end = start + count;
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            // exclude while converting the Japanese
            final Object[] spans = s.getSpans(start, end, Object.class);
            for(Object span : spans)
                if((s.getSpanFlags(span) & Spanned.SPAN_COMPOSING) == Spanned.SPAN_COMPOSING)
                    return;

            int oldPos = editText.getSelectionStart();
            int oldTextLength = s.length();

            final DynamicDrawableSpan[] imageSpans = s.getSpans(start, end, DynamicDrawableSpan.class);
            if (toggleState)
            {
                if(imageSpans.length == 0)
                {
                    editText.removeTextChangedListener(textWatcher);

                    final Spannable text = (Spannable)emojify(s);
                    final int length = text.length();
                    if(length != 0)
                    {
                        final int size = (int)editText.getTextSize();
                        for(DynamicDrawableSpan span : text.getSpans(0, length, DynamicDrawableSpan.class))
                            span.getDrawable().setBounds(0, 0, size, size);
                    }

                    s.replace(0, s.length(), text);

                    editText.addTextChangedListener(textWatcher);
                }
                else
                {
                    // Resize emoji image.
                    final int size = (int)editText.getTextSize();
                    for(DynamicDrawableSpan span : imageSpans)
                        span.getDrawable().setBounds(0, 0, size, size);
                }
            }
            else
            {
                if(imageSpans.length > 0)
                {
                    editText.removeTextChangedListener(textWatcher);
                    for(DynamicDrawableSpan span : imageSpans)
                        s.removeSpan(span);
                    final CharSequence subSequence = s.subSequence(start, end).toString();
                    s.replace(start, end, toUnicodeString(subSequence));
                    editText.addTextChangedListener(textWatcher);
                }
            }

            // adjustment cursor position
            int addTextLength = s.length() - oldTextLength;
            int newPos = oldPos + addTextLength;
            if (newPos > editText.getText().length())
                newPos = editText.getText().length();
            else if (newPos < 0)
                newPos = 0;
            editText.setSelection(newPos);
        }
    }

    /**
     * open settings view
     * @param v
     */
    public void openSettings(View v)
    {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);

        analytics.logEvent("menu_show_settings", new Bundle());
    }

    /**
     * create application list for sharing.
     * @param data
     */
    private void createList(final String data)
    {
        // get destination application list
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        final List<ResolveInfo> appInfo = packageManager.queryIntentActivities(intent, 0);

        // create listView
        ListView listView = new ListView(this);
        listView.setAdapter(new appInfoAdapter(this, R.layout.applicationlist_view, appInfo));

        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.share));
        builder.setView(listView);
        builder.setPositiveButton(R.string.cancel, null);
        final AlertDialog dialog = builder.show();

        // when click a listView's item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                analytics.logEvent(FirebaseAnalytics.Event.SHARE, new Bundle());

                ResolveInfo info = appInfo.get(position);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setPackage(info.activityInfo.packageName);
                intent.putExtra(Intent.EXTRA_TEXT, data);
                startActivity(intent);
            }
        });
    }

    /**
     * Check IME enable.
     */
    private void imeEnableCheck()
    {
        // Skip if ime enable.
        for(InputMethodInfo info : inputMethodManager.getEnabledInputMethodList())
        {
            if(info.getServiceName().equals(EmojidexIME.class.getName()))
                return;
        }

        // Show dialog and go to settings.
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.editor_enable_check_message);
        dialog.setNegativeButton(R.string.editor_enable_check_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nop
            }
        });
        dialog.setPositiveButton(R.string.editor_enable_check_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    /**
     * Show tutorial.
     */
    private void showTutorial()
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        final String key = getString(R.string.preference_key_show_tutorial);
        final boolean showTutorialFlag = pref.getBoolean(key, true);

        // Skip tutorial if show flag is false.
        if(!showTutorialFlag)
            return;

        // Set show flag false.
        pref.edit().putBoolean(key, false).commit();

        // Show choise dialog.
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.editor_show_tutorial_message);
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
                startActivity(intent);
                analytics.logEvent("show_tutorial", new Bundle());
            }
        });
        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nop
            }
        });
        dialog.show();
    }

    /**
     * application list adapter for sharing.
     */
    private class appInfoAdapter extends ArrayAdapter<ResolveInfo>
    {
        private LayoutInflater inflater;
        private int layout;

        public appInfoAdapter(Context context, int resource, List<ResolveInfo> objects) {
            super(context, resource, objects);

            inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
            layout = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null)
                view = this.inflater.inflate(this.layout, null);

            // set application icon & name
            PackageManager packageManager = getPackageManager();
            ResolveInfo info = getItem(position);
            ImageView icon = (ImageView)view.findViewById(R.id.application_list_icon);
            icon.setImageDrawable(info.loadIcon(packageManager));
            TextView textView = (TextView)view.findViewById(R.id.application_list_name);
            textView.setText(info.loadLabel(packageManager));
            return view;
        }
    }

    /**
     * When sent other application's text(intent).
     */
    private void getIntentData()
    {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (action.equals(Intent.ACTION_SEND) && type != null)
        {
            if (type.equals("text/plain"))
            {
                final String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                if(toggleState)
                    editText.setText(emojify(text));
                else
                    editText.setText(toUnicodeString(text));

                // Move cursor to last.
                editText.setSelection(editText.length());
            }
        }
    }

    /**
     * Clear the text
     * @param v
     */
    public void clearText(View v)
    {
        clearText(v, "clear_text");
    }

    public void clearText(View v, String event)
    {
        editText.getText().clear();

        Toast.makeText(this, R.string.editor_message_text_clear, Toast.LENGTH_SHORT).show();

        analytics.logEvent(event, new Bundle());
    }

    /**
     * Clear and Paste the text(clipboard data).
     * @param v
     */
    public void clearAndPaste(View v)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard.getPrimaryClip();
        CharSequence newText = (clipData == null) ? "" : clipData.getItemAt(0).getText();

        final Editable text = editText.getText();
        text.replace(
                0, text.length(),
                toggleState ? emojify(newText) : toUnicodeString(newText)
        );

        Toast.makeText(this, R.string.editor_message_text_clear_and_paste, Toast.LENGTH_SHORT).show();

        analytics.logEvent("menu_clear_and_paste", new Bundle());
    }

    /**
     * When click the toggle button, put the state and convert the text.
     * @param v
     */
    public void clickToggleButton(View v)
    {
        clickToggleButton(v, "switch_auto_conversion");
    }

    public void clickToggleButton(View v, String event)
    {
        toggleState = toggleButton.isChecked();

        // convert text
        final Editable text = editText.getText();
        if (toggleState)
        {
            text.replace(0, text.length(), emojify(text));
            Toast.makeText(this, R.string.editor_message_conversion_on, Toast.LENGTH_SHORT).show();
        }
        else
        {
            text.replace(0, text.length(), toUnicodeString(text));
            Toast.makeText(this, R.string.editor_message_conversion_off, Toast.LENGTH_SHORT).show();
        }

        // Move cursor to last.
        editText.setSelection(editText.length());

        analytics.logEvent(event, new Bundle());
    }

    /**
     * Auto conversion switching.
     */
    public void switchToggle() {
        toggleButton.setChecked(!toggleButton.isChecked());
        clickToggleButton(null, "menu_switch_auto_conversion");
    }

    /**
     * Copy the text in the text editor.
     * @param v view
     */
    public void copyText(View v) {
        copyText(v, "copy_text");
    }

    public void copyText(View v, String event) {
        String text = setShareData();
        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("emojidex", text));

        Toast.makeText(this, R.string.editor_message_text_copy, Toast.LENGTH_SHORT).show();

        analytics.logEvent(event, new Bundle());
    }

    /**
     * Clear the search results.
     */
    public void clearSearchResult() {
        SaveDataManager.getInstance(this, SaveDataManager.Type.Search).deleteFile();
        if(EmojidexIME.currentInstance != null)
            EmojidexIME.currentInstance.reloadSearchResult();

        Toast.makeText(this, R.string.editor_message_search_clear, Toast.LENGTH_SHORT).show();

        analytics.logEvent("menu_delete_search_result", new Bundle());
    }

    /**
     * Open the emojidex web site.
     */
    public void openEmojidexWebSite() {
        Uri uri = Uri.parse(getString(R.string.emojidex_url));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);

        analytics.logEvent("menu_show_emojidex_web", new Bundle());
    }

    /**
     * Open option menu.
     * @param v view
     */
    public void openOptions(View v) {
        analytics.logEvent("open_menu", new Bundle());
        openOptionsMenu();
    }

    @Override
    protected void onDestroy()
    {
        editText.removeTextChangedListener(textWatcher);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initialize login button.
        setLoginButtonVisibility(!UserData.getInstance().isLogined());

        // Initialize ads.
        setAdsVisibility();

        // Initialize text size.
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        editText.setTextSize(Float.valueOf(pref.getString(
                getString(R.string.preference_key_text_size),
                getString(R.string.editor_text_size_default)
        )));

        final Editable text = editText.getText();
        final int length = text.length();
        if(length != 0)
        {
            final int size = (int)editText.getTextSize();
            for(DynamicDrawableSpan span : text.getSpans(0, length, DynamicDrawableSpan.class))
                span.getDrawable().setBounds(0, 0, size, size);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        inputMethodManager.hideSoftInputFromWindow(
                rootView.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS
        );
        rootView.requestFocus();
        return true;
    }

    /**
     * Login to emojidex web site.
     * @param v view
     */
    public void loginEmojidex(View v) {
        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        intent.putExtra("URL", EMOJIDEX_URL + "/mobile_app/login");
        startActivityForResult(intent, LOGIN_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final HistoryManager hm = HistoryManager.getInstance(this);
        final FavoriteManager fm = FavoriteManager.getInstance(this);

        // Get result.
        switch (requestCode) {
            case LOGIN_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    setLoginButtonVisibility(false);
                    hm.saveBackup();
                    fm.saveBackup();
                    hm.loadFromUser();
                    fm.loadFromUser();
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.menu_login_success) + userData.getUsername(),
                            Toast.LENGTH_SHORT).show();
                    analytics.logEvent(FirebaseAnalytics.Event.LOGIN, new Bundle());
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.menu_login_cancel), Toast.LENGTH_SHORT).show();
                }
                break;
            case REGISTER_RESULT:
                if(data == null)
                {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.menu_new_failure),
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(),
                            data.getStringExtra("message") + getString(R.string.menu_new_success),
                            Toast.LENGTH_SHORT).show();
                    analytics.logEvent("registered_emoji", new Bundle());
                } else if (resultCode == Activity.RESULT_FIRST_USER){
                    Toast.makeText(getApplicationContext(),
                            data.getStringExtra("message"), Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    /**
     * Register a emoji in the emojidex web site.
     * @param v view
     */
    public void registerNewEmoji(View v) {
        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        intent.putExtra("URL", EMOJIDEX_URL + "/emoji/new");
        startActivityForResult(intent, REGISTER_RESULT);
    }

    /**
     * show the user's emojis in the emojidex web site.
     * @param v view
     */
    public void showMyEmoji(View v) {
        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        intent.putExtra("URL", EMOJIDEX_URL + "/users/" + UserData.getInstance().getUsername());
        startActivity(intent);
        analytics.logEvent("show_my_emoji", new Bundle());
    }

    public void setLoginButtonVisibility(boolean isVisible) {
        // before login.
        if (isVisible) {
            loginButton.setVisibility(View.VISIBLE);
            newEmojiButton.setVisibility(View.GONE);
            myEmojiButton.setVisibility(View.GONE);
        } else {
            loginButton.setVisibility(View.GONE);
            newEmojiButton.setVisibility(View.VISIBLE);
            myEmojiButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * init AdMob, Firebase.
     */
    private void initAds() {
        adView = (AdView) findViewById(R.id.editor_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        analytics = FirebaseAnalytics.getInstance(this);
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, new Bundle());
    }

    /**
     * show/hide AdMob.
     */
    private void setAdsVisibility()
    {
        userData = UserData.getInstance();

        if (!userData.isLogined())
        {
            adView.setVisibility(View.VISIBLE);
            return;
        }

        User user = new User();
        if (user.authorize(userData.getUsername(), userData.getAuthToken()))
        {
            if (user.getPremium())
                adView.setVisibility(View.GONE);
        }
    }

    private void startAnimation()
    {
        if(isAnimating)
            return;

        final Editable text = editText.getText();
        if(text.getSpans(0, text.length(), EmojidexAnimationImageSpan.class).length == 0)
            return;

        isAnimating = true;

        final Handler handler = new Handler();
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if(!isAnimating)
                    return;

                final int start = editText.getSelectionStart();
                final int end = editText.getSelectionEnd();
                editText.setText(editText.getText());
                editText.setSelection(start, end);

                handler.postDelayed(this, 100);
            }
        });
    }

    private void stopAnimation()
    {
        isAnimating = false;
    }

    /**
     * Custom download listener
     */
    private class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onFinish(int handle, EmojiDownloader.Result result)
        {
            final Editable text = editText.getText();

            // Skip if text is empty.
            if(text.length() == 0)
                return;

            // Skip if text is composing.
            final Object[] spans = text.getSpans(0, text.length(), Object.class);
            for(Object span : spans)
                if((text.getSpanFlags(span) & Spanned.SPAN_COMPOSING) == Spanned.SPAN_COMPOSING)
                    return;

            // Update text.
            if(toggleState)
                updateTextHandler.sendMessage(updateTextHandler.obtainMessage());
        }
    }
}