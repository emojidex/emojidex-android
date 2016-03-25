package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nazuki on 2014/01/31.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new CustomPreferenceFragment()).commit();
    }


    /**
     * Custom PreferenceFragment.
     */
    public static class CustomPreferenceFragment extends PreferenceFragment
    {
        private final Preference.OnPreferenceChangeListener onPreferenceChangeListener = new OnListPreferenceChangeListener();

        private Activity parentActivity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            parentActivity = activity;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            // Initialize.
            createDefaultKeyboardPreference();
            createUpdateIntervalPreference();
            createClearDataPreference();
            createTutorialPreference();
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
            final InputMethodManager inputMethodManager = (InputMethodManager)parentActivity.getSystemService(INPUT_METHOD_SERVICE);
            final List<InputMethodInfo> inputMethodInfos = inputMethodManager.getEnabledInputMethodList();
            entries.add(getString(R.string.settings_entry_default_keyboard_nothing));
            entryValues.add(getString(R.string.preference_entryvalue_default_keyboard_nothing));
            for(InputMethodInfo info : inputMethodInfos)
            {
                final CharSequence id = info.getId();
                if(id.equals(parentActivity.getPackageName() + "/.EmojidexIME"))
                    continue;
                entries.add(info.loadLabel(parentActivity.getPackageManager()));
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
            // Auto update list.
            final ListPreference updateInterval = (ListPreference)findPreference(getString(R.string.preference_key_update_interval));
            updateInterval.setSummary(updateInterval.getEntry());
            updateInterval.setOnPreferenceChangeListener(onPreferenceChangeListener);

            // Manual update.
            final Preference updateNow = findPreference(getString(R.string.preference_key_update_now));
            updateNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (new EmojidexUpdater(parentActivity).startUpdateThread(true))
                        Toast.makeText(parentActivity, R.string.ime_message_update_start, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(parentActivity, R.string.ime_message_already_update, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
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
                    createDeleteDialog(SaveDataManager.Type.Favorite);
                    return true;
                }
            });

            final Preference clearHistory = findPreference(getString(R.string.preference_key_clear_history));
            clearHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    createDeleteDialog(SaveDataManager.Type.History);
                    return true;
                }
            });

            final Preference clearSearchResult = findPreference(getString(R.string.preference_key_clear_search_result));
            clearSearchResult.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    createDeleteDialog(SaveDataManager.Type.Search);
                    return true;
                }
            });

            final Preference clearCache = findPreference(getString(R.string.preference_key_clear_cache));
            updateClearCacheSummary();
            clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    createDeleteCacheDialog();
                    return true;
                }
            });
        }

        /**
         * Create tutorial preference.
         */
        private void createTutorialPreference()
        {
            final Preference tutorial = findPreference(getString(R.string.preference_key_tutorial));
            tutorial.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final Intent intent = new Intent(getActivity(), TutorialActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }

        /**
         * create dialog
         * @param type  Save data type.
         */
        private void createDeleteDialog(final SaveDataManager.Type type)
        {
            int confirmMessageResId;
            int succeededMessageResId;
            int failedMessageResId;

            switch(type)
            {
                case History:
                    confirmMessageResId = R.string.settings_delete_history_confirm;
                    succeededMessageResId = R.string.settings_delete_history_succeeded;
                    failedMessageResId = R.string.settings_delete_history_failed;
                    break;
                case Search:
                    confirmMessageResId = R.string.settings_delete_search_confirm;
                    succeededMessageResId = R.string.settings_delete_search_succeeded;
                    failedMessageResId = R.string.settings_delete_search_failed;
                    break;
                case Favorite:
                    confirmMessageResId = R.string.settings_delete_favorite_confirm;
                    succeededMessageResId = R.string.settings_delete_favorite_succeeded;
                    failedMessageResId = R.string.settings_delete_favorite_failed;
                    break;
                default:
                    return;
            }

            // create dialog
            final int successId = succeededMessageResId;
            final int failedId = failedMessageResId;
            AlertDialog.Builder dialog = new AlertDialog.Builder(parentActivity);
            dialog.setMessage(confirmMessageResId);
            dialog.setPositiveButton(R.string.yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean result = new SaveDataManager(parentActivity, type).deleteFile();
                            if (result)
                                Toast.makeText(parentActivity, successId, Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(parentActivity, failedId, Toast.LENGTH_SHORT).show();
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

        private void createDeleteCacheDialog()
        {

            // create dialog
            final int successId = R.string.settings_delete_cache_succeeded;
            final int failedId = R.string.settings_delete_cache_failed;
            AlertDialog.Builder dialog = new AlertDialog.Builder(parentActivity);
            dialog.setMessage(R.string.settings_delete_cache_confirm);
            dialog.setPositiveButton(R.string.yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean result = deleteFile(new File(PathUtils.LOCAL_ROOT_PATH));
                            if (result)
                                Toast.makeText(parentActivity, successId, Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(parentActivity, failedId, Toast.LENGTH_SHORT).show();
                            updateClearCacheSummary();
                            Emojidex.getInstance().reload();
                            if(EmojidexIME.currentInstance != null)
                                EmojidexIME.currentInstance.reloadCategory();
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

        private void updateClearCacheSummary()
        {
            final Preference clearCache = findPreference(getString(R.string.preference_key_clear_cache));
            clearCache.setSummary(sizeToString(getFileSize(new File(PathUtils.LOCAL_ROOT_PATH))));
        }

        private long getFileSize(File file)
        {
            if( !file.isDirectory() )
                return file.length();

            long size = 0;
            for(File child : file.listFiles())
                size += getFileSize(child);

            return size;
        }

        private String sizeToString(long size)
        {
            final String[] suffix = { "B", "kB", "MB", "GB" };
            final float base = 1000.0f;
            float result = (float)size;
            int index = 0;

            while(index < suffix.length - 1)
            {
                if(result < base)
                {
                    break;
                }
                result /= base;
                ++index;
            }

            return String.format("%.1f%s", result, suffix[index]);
        }

        private boolean deleteFile(File file)
        {
            boolean result = true;
            if(file.isDirectory())
                for(File child : file.listFiles())
                    result = deleteFile(child) && result;
            return file.delete() && result;
        }


        /**
         * List preference change event listener.
         */
        private static class OnListPreferenceChangeListener implements Preference.OnPreferenceChangeListener
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
}
