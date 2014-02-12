package org.genshin.emojidexandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nazuki on 2014/01/31.
 */
public class SettingsActivity extends Activity {
    private Context context;

    private ListView listView;
    private ArrayList<String> keyboardIds;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        context = getApplicationContext();

        // checkbox settings
        checkBox = (CheckBox)findViewById(R.id.settings_checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // save checkbox statement
                FileOperation.savePreferences(getApplicationContext(), String.valueOf(isChecked), FileOperation.REALTIME);
            }
        });
        // load checkbox statement
        if (FileOperation.loadPreferences(getApplicationContext(), FileOperation.REALTIME).equals("false"))
            checkBox.setChecked(false);
        else
            checkBox.setChecked(true);
    }

    /**
     * set keyboard
     * @param v
     */
    public void setKeyboard(View v)
    {
        // create dialog's view
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.keyboardlist_view,
                                            (ViewGroup)findViewById(R.id.action_settings));
        listView = (ListView)layout.findViewById(R.id.keyboard_list_view);
        setListView();

        // create dialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setView(layout);
        dialog.setPositiveButton(R.string.set,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int position = listView.getCheckedItemPosition();
                    boolean result = FileOperation.savePreferences(context, keyboardIds.get(position), FileOperation.KEYBOARD);
                    if (result)
                        Toast.makeText(context, R.string.register_success, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, R.string.register_failure, Toast.LENGTH_SHORT).show();
                }
            });
        dialog.setNegativeButton(R.string.cancel,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        dialog.show();
    }

    /**
     * set listView
     */
    private void setListView()
    {
        // get keyboard list
        ArrayList<String> keyboardNames = new ArrayList<String>();
        keyboardIds = new ArrayList<String>();
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> inputMethodInfoList = imm.getEnabledInputMethodList();
        for (InputMethodInfo info : inputMethodInfoList)
        {
            keyboardNames.add(String.valueOf(info.loadLabel(getPackageManager())));
            keyboardIds.add(info.getId());
        }

        // current keyboard
        int current = 0;
        String name = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        for (String id : keyboardIds)
        {
            if (name.equals(id))
            {
                current = keyboardIds.indexOf(id);
                break;
            }
        }

        // set listView
        listView.setAdapter(new ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_single_choice,
            keyboardNames
        ));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemChecked(current, true);
    }

    /**
     * back to the main activity.
     * @param v
     */
    public void backToMainActivity(View v)
    {
        super.onBackPressed();
    }

    /**
     * delete all favorites
     * @param v
     */
    public void deleteAllFavorites(View v)
    {
        createDeleteDialog(R.string.delete_favorites_all_confirm, FileOperation.FAVORITES);
    }

    /**
     * delete all histories
     * @param v
     */
    public void deleteAllHistories(View v)
    {
        createDeleteDialog(R.string.delete_histories_all_confirm, FileOperation.HISTORIES);
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
                    boolean result = FileOperation.deleteAll(context, mode);
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
}
