package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.emojidex.emojidexandroid.comparator.EmojiComparator;
import com.emojidex.libemojidex.Emojidex.Service.User;
import com.google.firebase.analytics.FirebaseAnalytics;


public class FilterActivity extends Activity {
    public static final String PREF_NAME = "emojidex_filter";

    private Spinner sortSpinner;
    private CheckBox standardOnlyCheckbox;

    private boolean sortable = false;

    private boolean fromCatalog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.window_filter);

        fromCatalog = getIntent().getBooleanExtra("Catalog", false);

        initializeContentView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Have no menu.
        return false;
    }

    /**
     * Initialize content view.
     */
    private void initializeContentView()
    {
        SharedPreferences pref = getSharedPreferences(FilterActivity.PREF_NAME, Context.MODE_PRIVATE);

        UserData userData = UserData.getInstance();
        userData.init(this);
        User user = new User();
        if (userData.isLogined() &&
                user.authorize(userData.getUsername(), userData.getAuthToken()) && (user.getPremium() || user.getPro())) {
            sortable = true;
        }

        // Close button.
        findViewById(R.id.filter_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeWindow();
            }
        });

        // Action button.
        findViewById(R.id.filter_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filteringEmoji();
            }
        });

        // Sort spinner.
        sortSpinner = (Spinner) findViewById(R.id.filter_spinner);
        if (sortable) {
            final ArrayAdapter<CharSequence> adapter
                    = ArrayAdapter.createFromResource(this, R.array.sort_items, R.layout.spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sortSpinner.setAdapter(adapter);
            sortSpinner.setBackground(getResources().getDrawable(R.drawable.ime_search_spinner_background));
            int sortType = pref.getInt(getString(R.string.preference_key_sort_type), EmojiComparator.SortType.SCORE.getValue());
            sortSpinner.setSelection(sortType);
        } else {
            sortSpinner.setVisibility(View.GONE);
        }

        // Standard only checkbox.
        standardOnlyCheckbox = (CheckBox) findViewById(R.id.filter_checkbox);
        standardOnlyCheckbox.setChecked(pref.getBoolean(getString(R.string.preference_key_standard_only), false));
    }

    /**
     * Filtering emoji.
     */
    private void filteringEmoji()
    {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();

        final boolean standardOnly = standardOnlyCheckbox.isChecked();
        editor.putBoolean(getString(R.string.preference_key_standard_only), standardOnly);
        editor.apply();

        if (sortable) {
            final int sortType = sortSpinner.getSelectedItemPosition();
            editor.putInt(getString(R.string.preference_key_sort_type), sortType);
            editor.apply();

            String type;
            if (fromCatalog)
                type = "sealkit_sort";
            else
                type = "sort";
            FirebaseAnalytics.getInstance(this).logEvent(type, new Bundle());
        }

        closeWindow();
    }

    /**
     * Close this window.
     */
    private void closeWindow() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        finish();
    }
}
