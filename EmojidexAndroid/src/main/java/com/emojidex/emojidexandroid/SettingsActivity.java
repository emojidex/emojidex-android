package com.emojidex.emojidexandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nazuki on 2014/01/31.
 */
public class SettingsActivity extends PreferenceActivity {
    private Context context;

    private final Preference.OnPreferenceChangeListener onPreferenceChangeListener = new OnListPreferenceChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new CustomPreferenceFragment()).commit();

        context = getApplicationContext();
    }

    /**
     * create dialog
     * @param textRes favorites or histories text resources
     * @param mode favorites or histories
     */
    private void createDeleteDialog(int textRes, final String mode)
    {
        // create dialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(textRes);
        dialog.setPositiveButton(R.string.yes,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean result = FileOperation.deleteFile(context, mode);
                    if (result)
                        Toast.makeText(context, R.string.delete_success, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, R.string.delete_failure, Toast.LENGTH_SHORT).show();
                }
            });
        dialog.setNegativeButton(R.string.no,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        dialog.show();
    }


    /**
     * Custom PreferenceFragment.
     */
    private class CustomPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            // Initialize.
            createDefaultKeyboardPreference();
            createUpdateIntervalPreference();
            createClearDataPreference();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        /**
         * Create default keyboard preference.
         */
        private void createDefaultKeyboardPreference()
        {
            final ListPreference defaultKeyboard = (ListPreference)findPreference(getString(R.string.preference_key_default_keyboard));
            final ArrayList<CharSequence> entries = new ArrayList<CharSequence>();
            final ArrayList<CharSequence> entryValues = new ArrayList<CharSequence>();

            // Create entries and entryValues.
            final InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            final List<InputMethodInfo> inputMethodInfos = inputMethodManager.getEnabledInputMethodList();
            entries.add(getString(R.string.settings_entry_default_keyboard_nothing));
            entryValues.add(getString(R.string.preference_entryvalue_default_keyboard_nothing));
            for(InputMethodInfo info : inputMethodInfos)
            {
                final CharSequence id = info.getId();
                if(id.equals(getPackageName() + "/.EmojidexIME"))
                    continue;
                entries.add(info.loadLabel(getPackageManager()));
                entryValues.add(info.getId());
            }
            defaultKeyboard.setEntries(entries.toArray(new CharSequence[entries.size()]));
            defaultKeyboard.setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));

            // Set summary.
            defaultKeyboard.setSummary(defaultKeyboard.getEntry());

            // Set changed event.
            defaultKeyboard.setOnPreferenceChangeListener(onPreferenceChangeListener);
        }

        /**
         * Create update interval preference.
         */
        private void createUpdateIntervalPreference()
        {
            final ListPreference updateInterval = (ListPreference)findPreference(getString(R.string.preference_key_update_interval));

            // Set summary.
            updateInterval.setSummary(updateInterval.getEntry());

            // Set changed event.
            updateInterval.setOnPreferenceChangeListener(onPreferenceChangeListener);
        }

        /**
         * Create clear data preference.
         */
        private void createClearDataPreference()
        {
            final Preference clearFavorite = findPreference(getString(R.string.preference_key_clear_favorite));
            clearFavorite.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    createDeleteDialog(R.string.delete_favorites_all_confirm, FileOperation.FAVORITES);
                    return true;
                }
            });

            final Preference clearHistory = findPreference(getString(R.string.preference_key_clear_history));
            clearHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    createDeleteDialog(R.string.delete_histories_all_confirm, FileOperation.HISTORIES);
                    return true;
                }
            });

            final Preference clearSearchResult = findPreference(getString(R.string.preference_key_clear_search_result));
            clearSearchResult.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    createDeleteDialog(R.string.delete_search_results_all_confirm, FileOperation.SEARCH_RESULT);
                    return true;
                }
            });
        }
    }

    /**
     * List preference change event listener.
     */
    private class OnListPreferenceChangeListener implements Preference.OnPreferenceChangeListener
    {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final ListPreference lp = (ListPreference)preference;
            final int newIndex = lp.findIndexOfValue((String)newValue);
            final CharSequence newEntry = lp.getEntries()[newIndex];
            lp.setSummary(newEntry);
            return true;
        }
    }
}
