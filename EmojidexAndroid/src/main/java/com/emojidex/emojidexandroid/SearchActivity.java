package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.emojidex.emojidexandroid.downloader.DownloadListener;
import com.emojidex.emojidexandroid.downloader.EmojiDownloader;
import com.emojidex.emojidexandroid.downloader.arguments.SearchDownloadArguments;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;


public class SearchActivity extends Activity {
    private SaveDataManager searchManager;

    private EditText editText;

    private String category;
    private ProgressDialog loadingDialog = null;
    private int downloadHandle = EmojiDownloader.HANDLE_NULL;

    private boolean fromCatalog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.window_search);

        fromCatalog = getIntent().getBooleanExtra("Catalog", false);

        if (!fromCatalog) switchIME();
        initializeContentView();

        // Initialize fields.
        if (fromCatalog)
            searchManager = SaveDataManager.getInstance(this, SaveDataManager.Type.CatalogSearch);
        else
            searchManager = SaveDataManager.getInstance(this, SaveDataManager.Type.Search);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Have no menu.
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        if(!fromCatalog)
            switchIME();
    }

    @Override
    protected void onDestroy() {
        if (!fromCatalog) resetIME();
        super.onDestroy();
    }

    /**
     * Initialize content view.
     */
    private void initializeContentView()
    {
        // Edit text.
        editText = (EditText)findViewById(R.id.search_edit_text);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    searchEmoji();
                    return true;
                }

                return false;
            }
        });

        // Search button.
        findViewById(R.id.search_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEmoji();
            }
        });

        // Close button.
        findViewById(R.id.search_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Category spinner.
        final Spinner categorySpinner = (Spinner)findViewById(R.id.search_category_spinner);
        final HashMap<String, String> categoryMap = new HashMap<String, String>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.ime_category_text_all));

        final CategoryManager categoryManager = CategoryManager.getInstance();
        final int categoryCount = categoryManager.getCategoryCount();
        for(int i = 0;  i < categoryCount;  ++i)
        {
            final String id = categoryManager.getCategoryId(i);
            final String text = categoryManager.getCategoryText(i);
            categoryMap.put(text, id);
            adapter.add(text);
        }
        categorySpinner.setAdapter(adapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String selected = (String) categorySpinner.getSelectedItem();
                category = categoryMap.get(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                category = null;
            }
        });
        categorySpinner.setBackground(getResources().getDrawable(R.drawable.ime_search_spinner_background));
    }

    /**
     * Switch input method.
     */
    private void switchIME()
    {
        final InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean hasDefaultIME = false;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String idNothing = getString(R.string.preference_entryvalue_default_keyboard_nothing);
        final String defaultIME = prefs.getString(getString(R.string.preference_key_default_keyboard), idNothing);

        if( !defaultIME.equals(idNothing) )
        {
            for(InputMethodInfo info : inputMethodManager.getEnabledInputMethodList())
            {
                if(info.getId().equals(defaultIME))
                    hasDefaultIME = true;
            }
        }

        if(hasDefaultIME && EmojidexIME.currentInstance != null)
            EmojidexIME.currentInstance.switchInputMethod(defaultIME);
        else
            inputMethodManager.showInputMethodPicker();
    }

    /**
     * Reset input method.
     */
    private void resetIME()
    {
        final InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showInputMethodPicker();
    }

    /**
     * Search emoji.
     */
    private void searchEmoji()
    {
        // Skip if already search.
        if(loadingDialog != null)
            return;

        // Skip if search text is empty.
        final String searchText = editText.getText().toString();
        if(searchText.isEmpty())
            return;

        // Search emoji.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                searchManager.clear();

                final EmojiDownloader downloader = EmojiDownloader.getInstance();

//                downloadHandle = downloader.downloadSearchEmoji(searchText, category, config);
                downloadHandle = downloader.downloadSearchEmoji(
                        new SearchDownloadArguments(searchText)
                            .setCategory(category)
                );

                if(downloadHandle != EmojiDownloader.HANDLE_NULL)
                    Emojidex.getInstance().addDownloadListener(new CustomDownloadListener());
            }
        }, 1000);

        // Create loading dialog.
        createLoadingDialog();

        String type;
        if (fromCatalog)
            type = "sealkit_search";
        else
            type = FirebaseAnalytics.Event.SEARCH;
        FirebaseAnalytics.getInstance(this).logEvent(type, new Bundle());
    }

    /**
     * Create loading dialog.
     */
    private void createLoadingDialog()
    {
        // Create dialog.
        loadingDialog = new ProgressDialog(SearchActivity.this);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setTitle(getString(R.string.search_dialog_title));
        loadingDialog.setMessage(getString(R.string.search_dialog_message));

        // Add cancel button.
        loadingDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.search_dialog_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );
        loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Emojidex.getInstance().getEmojiDownloader().cancelDownload(downloadHandle);
                loadingDialog = null;
            }
        });

        // Set dim amount settings.
        final Window window = loadingDialog.getWindow();
        window.setDimAmount(0.6f);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Show dialog.
        loadingDialog.show();
    }

    @Override
    protected void onPause() {
        if (loadingDialog != null) loadingDialog.dismiss();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (loadingDialog != null) loadingDialog.show();
        super.onResume();
    }

    /**
      * Custom download listener.
      */
    private class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onDownloadJson(int handle, String... emojiNames)
        {
            if(handle == downloadHandle)
            {
                // Add emoji to manager.
                for(String emoji : emojiNames)
                    searchManager.addLast(emoji);
                searchManager.save();

                // Save shared preferences.
                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(SearchActivity.this);
                final SharedPreferences.Editor prefEditor = pref.edit();
                prefEditor.putString(
                        getString(R.string.preference_key_start_category),
                        getString(R.string.ime_category_id_search)
                );
                prefEditor.commit();

                // Close dialog.
                if(loadingDialog != null)
                {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }

                // Close search activity.
                finish();
            }
        }

        @Override
        public void onFinish(int handle, boolean result)
        {
            if(handle == downloadHandle)
            {
                downloadHandle = EmojiDownloader.HANDLE_NULL;
                Emojidex.getInstance().removeDownloadListener(this);
            }
        }

        @Override
        public void onCancelled(int handle, boolean result)
        {
            if(handle == downloadHandle)
            {
                downloadHandle = EmojiDownloader.HANDLE_NULL;
                Emojidex.getInstance().removeDownloadListener(this);
            }
        }
    }
}
