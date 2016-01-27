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
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.List;

public class MainActivity extends Activity {
    static final String TAG = "EmojidexAndroid";
    private static final int LOGIN_RESULT = 1000;
    private static final int LOGOUT_RESULT = 1001;
    private static final int REGISTER_RESULT = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initEmojidexEditor();
        setShareButtonIcon();
        getIntentData();

        showTutorial();
        imeEnableCheck();
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
                copyText(null);
                return true;
            case R.id.action_clear_and_paste:
                clearAndPaste(null);
                return true;
            case R.id.action_clear:
                clearText(null);
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

    /**
     * Emojidex
     */
    private Emojidex emojidex = null;

    private EditText editText;

    private ToggleButton toggleButton;
    private boolean toggleState = true;

    private Button loginButton;
    private Button logoutButton;
    private Button newEmojiButton;
    private Button myEmojiButton;
    private UserData userData;

    private void initEmojidexEditor()
    {
        // Initialize emojdiex.
        emojidex = Emojidex.getInstance();
        emojidex.initialize(this);

        // Get edit text.
        editText = (EditText)findViewById(R.id.edit_text);

        // detects input
        editText.addTextChangedListener(new CustomTextWatcher());

        // toggle button state
        toggleButton = (ToggleButton)findViewById(R.id.toggle_button);
        toggleState = toggleButton.isChecked();

        // for emojidex web.
        loginButton = (Button)findViewById(R.id.login_button);
        logoutButton = (Button)findViewById(R.id.logout_button);
        newEmojiButton = (Button)findViewById(R.id.new_emoji_button);
        myEmojiButton = (Button)findViewById(R.id.my_emoji_button);
        userData = UserData.getInstance();
        userData.init(this);

        if (!userData.getAuthToken().equals("")) {
            setLoginButtonVisibility(false);
        }
    }

    private CharSequence emojify(final CharSequence cs)
    {
        return emojidex.emojify(cs);
    }

    private CharSequence deEmojify(final CharSequence cs)
    {
        return emojidex.deEmojify(cs);
    }

    private CharSequence toUnicodeString(final CharSequence cs)
    {
        return emojidex.emojify(cs, false);
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
     * share to last selected application
     * @param v
     */
    public void shareDataLastSelected(View v)
    {
        // TODO: 不要になる？
        if (editText.getText().toString().equals(""))
            return;

        String packageName = FileOperation.loadPreferences(getApplicationContext(), FileOperation.SHARE);
        if (packageName.equals(""))
            shareData(v);
        else
        {
            // set share data
            String data = setShareData();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setPackage(packageName);
            intent.putExtra(Intent.EXTRA_TEXT, data);
            startActivity(intent);
        }
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
            final Object[] spans = s.getSpans(0, s.length(), Object.class);
            for(Object span : spans)
                if(span.getClass().getName().equals("android.view.inputmethod.ComposingText"))
                    return;

            int oldPos = editText.getSelectionStart();
            int oldTextLength = editText.getText().length();

            final ImageSpan[] imageSpans = s.getSpans(start, end, ImageSpan.class);
            if (toggleState)
            {
                if(imageSpans.length == 0)
                {
                    final String subSequence = s.subSequence(start, end).toString();
                    // Text has separator.
                    if(subSequence.indexOf(Emojidex.SEPARATOR) != -1)
                    {
                        editText.removeTextChangedListener(this);
                        editText.setText(emojify(deEmojify(s)));
                        editText.addTextChangedListener(this);
                    }
                    else
                    {
                        editText.removeTextChangedListener(this);
                        s.replace(start, end, emojify(deEmojify(subSequence)));
                        editText.setText(s);
                        editText.addTextChangedListener(this);
                    }
                }
            }
            else
            {
                if(imageSpans.length > 0)
                {
                    editText.removeTextChangedListener(this);
                    editText.setText(toUnicodeString(deEmojify(s)));
                    editText.addTextChangedListener(this);
                }
            }

            // adjustment cursor position
            int addTextLength = editText.getText().length() - oldTextLength;
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

                ResolveInfo info = appInfo.get(position);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setPackage(info.activityInfo.packageName);
                intent.putExtra(Intent.EXTRA_TEXT, data);
                startActivity(intent);

                // save settings and set icon
                FileOperation.savePreferences(getApplicationContext(), info.activityInfo.packageName, FileOperation.SHARE);
                setShareButtonIcon();
            }
        });
    }

    /**
     * Check IME enable.
     */
    private void imeEnableCheck()
    {
        // Skip if ime enable.
        final InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        for(InputMethodInfo info : imm.getEnabledInputMethodList())
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
     * set the last selected application's icon to share button
     */
    private void setShareButtonIcon()
    {
        // TODO: 不要になる？
//        ImageButton button = (ImageButton)findViewById(R.id.last_share_button);
//
//        // set image
//        String packageName = FileOperation.loadPreferences(getApplicationContext(), FileOperation.SHARE);
//        if (packageName.equals(""))
//        {
//            // default
//            button.setImageResource(android.R.drawable.ic_menu_send);
//        }
//        else
//        {
//            boolean set = false;
//            PackageManager packageManager = getPackageManager();
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType("text/plain");
//            List<ResolveInfo> appInfo = packageManager.queryIntentActivities(intent, 0);
//            for (ResolveInfo info : appInfo)
//            {
//                // get application's icon
//                if (info.activityInfo.packageName.equals(packageName))
//                {
//                    button.setImageDrawable(info.loadIcon(packageManager));
//                    set = true;
//                    break;
//                }
//            }
//            // set default icon when could not get icon
//            if (!set)
//                button.setImageResource(android.R.drawable.ic_menu_send);
//        }
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
                    editText.setText(emojify(deEmojify(text)));
                else
                    editText.setText(toUnicodeString(deEmojify(text)));
            }
        }
    }

    /**
     * Clear the text
     * @param v
     */
    public void clearText(View v)
    {
        editText.setText("");

        Toast.makeText(this, R.string.editor_message_text_clear, Toast.LENGTH_SHORT).show();
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
        editText.setText(newText);

        Toast.makeText(this, R.string.editor_message_text_clear_and_paste, Toast.LENGTH_SHORT).show();
    }

    /**
     * When click the toggle button, put the state and convert the text.
     * @param v
     */
    public void clickToggleButton(View v)
    {
        toggleState = toggleButton.isChecked();

        // convert text
        if (toggleState)
        {
            editText.setText(emojify(deEmojify(editText.getText())));
            Toast.makeText(this, R.string.editor_message_conversion_on, Toast.LENGTH_SHORT).show();
        }
        else
        {
            editText.setText(toUnicodeString(deEmojify(editText.getText())));
            Toast.makeText(this, R.string.editor_message_conversion_off, Toast.LENGTH_SHORT).show();
        }

        // Move cursor to last.
        editText.setSelection(editText.length());
    }

    /**
     * Auto conversion switching.
     */
    public void switchToggle() {
        toggleButton.performClick();
    }

    /**
     * Copy the text in the text editor.
     * @param v view
     */
    public void copyText(View v) {
        String text = setShareData();
        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("emojidex", text));

        Toast.makeText(this, R.string.editor_message_text_copy, Toast.LENGTH_SHORT).show();
    }

    /**
     * Clear the search results.
     */
    public void clearSearchResult() {
        new SaveDataManager(this, SaveDataManager.Type.Search).deleteFile();
        if(EmojidexIME.currentInstance != null)
            EmojidexIME.currentInstance.reloadSearchResult();

        Toast.makeText(this, R.string.editor_message_search_clear, Toast.LENGTH_SHORT).show();
    }

    /**
     * Open the emojidex web site.
     */
    public void openEmojidexWebSite() {
        Uri uri = Uri.parse(getString(R.string.emojidex_url));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * Open option menu.
     * @param v view
     */
    public void openOptions(View v) {
        openOptionsMenu();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    /**
     * Login to emojidex web site.
     * @param v view
     */
    public void loginEmojidex(View v) {
        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        // TODO: address
        intent.putExtra("URL", "http://7bd96a9.ngrok.com/mobile_app/login?mobile=true");
        startActivityForResult(intent, LOGIN_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Get result.
        switch (requestCode) {
            case LOGIN_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    setLoginButtonVisibility(false);
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.menu_login_success) + userData.getUsername(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.menu_login_cancel), Toast.LENGTH_SHORT).show();
                }
                break;
            case LOGOUT_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    setLoginButtonVisibility(true);
                    userData.reset();
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.menu_logout), Toast.LENGTH_SHORT).show();
                }
                break;
            case REGISTER_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(),
                            data.getStringExtra("message") + getString(R.string.menu_new_success),
                            Toast.LENGTH_SHORT).show();
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
        // TODO: address
        intent.putExtra("URL", "http://7bd96a9.ngrok.com/emoji/new?mobile=true");
        startActivityForResult(intent, REGISTER_RESULT);
    }

    /**
     * Logout of emojidex web site.
     * @param v view
     */
    public void logoutEmojidex(View v) {
        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        // TODO: address
        intent.putExtra("URL", "http://7bd96a9.ngrok.com/mobile_app/logout?mobile=true");
        startActivityForResult(intent, LOGOUT_RESULT);
    }

    /**
     * show the user's emojis in the emojidex web site.
     * @param v view
     */
    public void showMyEmoji(View v) {
        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        // TODO: address
        intent.putExtra("URL", "http://7bd96a9.ngrok.com/users/"
                + UserData.getInstance().getUsername() + "?mobile=true");
        startActivity(intent);
    }

    public void setLoginButtonVisibility(boolean isVisible) {
        // before login.
        if (isVisible) {
            loginButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
            newEmojiButton.setVisibility(View.GONE);
            myEmojiButton.setVisibility(View.GONE);
        } else {
            loginButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
            newEmojiButton.setVisibility(View.VISIBLE);
            myEmojiButton.setVisibility(View.VISIBLE);
        }
    }
}