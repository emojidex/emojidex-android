package org.genshin.emojidexandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initEmojidexEditor();
        setShareButtonIcon();
        getIntentData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
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
    private EmojidexEditor emojidex = null;
    private EmojiDataManager emojiDataManager;
    private List<EmojiData> emojiDataList;

    private EditText editText;
    private CustomTextWatcher textWatcher;

    private ToggleButton toggleButton;
    private boolean toggleState = true;

    // private boolean realTime;

    private void initEmojidexEditor()
    {
        if (emojidex == null)
            emojidex = new EmojidexEditor(getApplicationContext());

        emojiDataManager = emojidex.getEmojiDataManager();
        emojiDataList = emojiDataManager.getCategorizedList(getString(R.string.all_category));

        editText = (EditText)findViewById(R.id.edit_text);

        // detects input
        textWatcher = new CustomTextWatcher();
        editText.addTextChangedListener(textWatcher);

        // toggle button state
        toggleButton = (ToggleButton)findViewById(R.id.toggle_button);
        toggleState = toggleButton.isChecked();
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
        return emojidex.toUnicodeString(cs);
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

    private class CustomTextWatcher implements TextWatcher
    {
        public CustomTextWatcher() { }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

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

            if (toggleState)
            {
                editText.removeTextChangedListener(textWatcher);
                editText.setText(emojify(deEmojify(editText.getText())));
                editText.addTextChangedListener(textWatcher);
            }

            // adjustment cursor position
            int addTextLength = editText.getText().length() - oldTextLength;
            int newPos = oldPos + addTextLength;
            if (newPos > editText.getText().length())
                newPos = editText.getText().length();
            else if (newPos < 0)
                newPos = 0;
            editText.setSelection(newPos);

            // load image
            ArrayList<String> emojiNames = new ArrayList<String>();
            emojiNames = emojidex.getEmojiNames();
            if (emojiNames != null)
            {
                for (String emojiName : emojiNames)
                    loadImage(emojiName);
            }
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
        ImageButton button = (ImageButton)findViewById(R.id.last_share_button);

        // set image
        String packageName = FileOperation.loadPreferences(getApplicationContext(), FileOperation.SHARE);
        if (packageName.equals(""))
        {
            // default
            button.setImageResource(android.R.drawable.ic_menu_send);
        }
        else
        {
            boolean set = false;
            PackageManager packageManager = getPackageManager();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            List<ResolveInfo> appInfo = packageManager.queryIntentActivities(intent, 0);
            for (ResolveInfo info : appInfo)
            {
                // get application's icon
                if (info.activityInfo.packageName.equals(packageName))
                {
                    button.setImageDrawable(info.loadIcon(packageManager));
                    set = true;
                    break;
                }
            }
            // set default icon when could not get icon
            if (!set)
                button.setImageResource(android.R.drawable.ic_menu_send);
        }
    }

    /**
     * load image
     * @param name emoji name
     */
    private void loadImage(String name)
    {
        EmojiData emoji = emojiDataManager.getEmojiData(name);
        ImageView blank = (ImageView)findViewById(R.id.blank);
        blank.setImageDrawable(emoji.getIcon());
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
                editText.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
        }
    }

    /**
     * Clear the text
     * @param v
     */
    public void clearText(View v)
    {
        editText.setText("");
    }

    /**
     * Clear and Paste the text(clipboard data).
     * @param v
     */
    public void clearAndPaste(View v)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard.getPrimaryClip();
        ClipData.Item item = clipData.getItemAt(0);
        editText.setText(item.getText());
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
            editText.setText(emojify(editText.getText()));
        else
            editText.setText(deEmojify(editText.getText()));
    }

    /**
     * Create the window(dialog) for emoji search
     * @param v
     */
    public void createSearchWindow(View v)
    {
        // set view
        View view = getLayoutInflater().inflate(R.layout.search_window, null);
        final EditText editText = (EditText)view.findViewById(R.id.search_edittext);

        // set spinner
        final Spinner spinner = (Spinner)view.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.newest));
        adapter.add(getString(R.string.popular));
        adapter.add(getString(R.string.category));
        adapter.add(getString(R.string.emoji));
        spinner.setAdapter(adapter);

        // set dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        Button searchButton = (Button)view.findViewById(R.id.emoji_search_button);
        searchButton.setOnClickListener( new View.OnClickListener() {
            public void onClick( View v ) {
                if (spinner.getSelectedItemPosition() == 2 && editText.getText().toString().equals(""))
                    return;
                if (spinner.getSelectedItemPosition() == 3 && editText.getText().toString().equals(""))
                    return;

                searchEmoji(spinner.getSelectedItemPosition(), editText.getText().toString());
                dialog.dismiss();
            }
        });

        Button closeButton = (Button)view.findViewById(R.id.window_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
    }

    /**
     * Search emoji from emojidex site.
     * @param selected  selectedItem from spinner
     * @param str       string from edittext
     */
    private void searchEmoji(int selected, String str)
    {
        String text = str;
        if (!text.equals(""))
        {
            try
            {
                text = URLEncoder.encode(text, "UTF-8");
            }
            catch (UnsupportedEncodingException e) { e.printStackTrace(); }
        }

        String uri = "";
        switch (selected)
        {
            // newest
            case 0:
                uri = "https://www.emojidex.com/api/v1/newest";
                break;
            // popular
            case 1:
                uri = "https://www.emojidex.com/api/v1/popular";
                break;
            // category
            case 2:
                uri = "https://www.emojidex.com/api/v1/search/categories?[q][name_cont]=" + text;
                break;
            // emoji
            case 3:
                uri = "https://www.emojidex.com/api/v1/search/emoji?[q][code_cont]=" + text;
                break;
        }

        Uri.Builder builder = new Uri.Builder();
        AsyncHttpRequestForGetJson getJsonTask = new AsyncHttpRequestForGetJson(uri);
        getJsonTask.execute(builder);
        String result = "";
        try
        {
            result = getJsonTask.get();
        }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
        Log.e("test", "result:" + result);
    }
}