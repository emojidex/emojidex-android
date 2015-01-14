package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by kou on 14/12/10.
 */
public class SearchDialog extends AbstractDialog {
    private final Context context;
    private final InputMethodManager inputMethodManager;
    private final String oldIME;

    private EditText searchEditText;

    private String category = null;

    /**
     * Construct object.
     * @param context       Context.
     */
    public SearchDialog(InputMethodService context) {
        super(context);

        this.context = context;

        // Initialize popupz window.
        setInputMethodMode(INPUT_METHOD_FROM_FOCUSABLE);

        // Switch input method.
        inputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        oldIME = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);

        boolean hasDefaultIME = false;
        final String defaultIME = FileOperation.loadPreferences(context, FileOperation.KEYBOARD);
        final List<InputMethodInfo> inputMethodInfos = inputMethodManager.getEnabledInputMethodList();
        for(InputMethodInfo inputMethodInfo : inputMethodInfos)
        {
            if(inputMethodInfo.getId().equals(defaultIME))
            {
                hasDefaultIME = true;
            }
        }

        if(hasDefaultIME)
            context.switchInputMethod(defaultIME);
        else
            inputMethodManager.showInputMethodPicker();
    }

    @Override
    public void dismiss()
    {
        inputMethodManager.showInputMethodPicker();
        super.dismiss();
    }

    @Override
    protected View createContentView(Context context)
    {
        // window layout.
        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View contentView = inflater.inflate(R.layout.window_search, null);

        // Input area.
        searchEditText = (EditText)contentView.findViewById(R.id.search_edit_text);
//        searchEditText.setFocusable(true);

        // Show result space.
//        resultLayout = (LinearLayout)contentView.findViewById(R.id.search_result_layout);

        // Search button.
        final ImageButton searchButton = (ImageButton)contentView.findViewById(R.id.search_action);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEmoji();
            }
        });

        // Window close button.
        final ImageButton closeButton = (ImageButton)contentView.findViewById(R.id.search_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        // Category spinner.
        final HashMap<String, String> categoryMap = new HashMap<String, String>();
        final Spinner categorySpinner = (Spinner)contentView.findViewById(R.id.search_category_spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
        adapter.add(context.getString(R.string.ime_category_text_all));

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
                final String selected = (String)categorySpinner.getSelectedItem();
                category = categoryMap.get(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                category = null;
            }
        });

        return contentView;
    }

    /**
     * Search emoji.
     */
    private void searchEmoji()
    {
        final String searchText = searchEditText.getText().toString();
        final String url = "https://www.emojidex.com/api/v1/search/emoji?detailed=true&code_cont=" + searchText + (category == null ? "" : "&categories[]=" + category);

        final LinkedHashSet<EmojiFormat> formats = new LinkedHashSet<EmojiFormat>();
        formats.add(EmojiFormat.toFormat(context.getString(R.string.emoji_format_default)));
        formats.add(EmojiFormat.toFormat(context.getString(R.string.emoji_format_key)));
        formats.add(EmojiFormat.toFormat(context.getString(R.string.emoji_format_seal)));

        final EmojiDownloader downloader = new EmojiDownloader(context);
        downloader.setListener(new CustomDownloadListener());
        downloader.add(
                url,
                formats.toArray(new EmojiFormat[formats.size()]),
                "http://assets.emojidex.com/emoji"
        );
    }


    private class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onPostOneJsonDownload(String source, String destination) {
            super.onPostOneJsonDownload(source, destination);

            final File file = new File(destination);
            final ArrayList<JsonParam> emojies = JsonParam.readFromFile(file);
            final SaveDataManager searchManager = new SaveDataManager(context, SaveDataManager.Type.Search);
            for(JsonParam emoji : emojies)
            {
                emoji.name = emoji.name.replaceAll(" ", "_");
                searchManager.add(emoji.name);
            }
            searchManager.save();
            JsonParam.writeToFile(file, emojies);
        }

        @Override
        public void onPostAllJsonDownload(EmojiDownloader downloader) {
            super.onPostAllJsonDownload(downloader);
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            final SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putString("startCategory", context.getString(R.string.ime_category_id_search));
            prefEditor.commit();
            dismiss();
        }
    }
}
