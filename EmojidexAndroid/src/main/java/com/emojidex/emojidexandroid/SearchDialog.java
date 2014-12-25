package com.emojidex.emojidexandroid;

import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
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

    /**
     * Construct object.
     * @param context       Context.
     */
    public SearchDialog(InputMethodService context, View view) {
        super(context);

        this.context = context;

        // Initialize popup window.
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

        return contentView;
    }

    /**
     * Search emoji.
     */
    private void searchEmoji()
    {
        final String searchText = searchEditText.getText().toString();
        final String url = "https://www.emojidex.com/api/v1/search/emoji?code_cont=" + searchText;

        final LinkedHashSet<EmojiFormat> formats = new LinkedHashSet<EmojiFormat>();
        formats.add(EmojiFormat.toFormat(context.getString(R.string.emoji_format_default)));
        formats.add(EmojiFormat.toFormat(context.getString(R.string.emoji_format_key)));
        formats.add(EmojiFormat.toFormat(context.getString(R.string.emoji_format_seal)));

        final EmojiDownloader downloader = new EmojiDownloader(context);
        downloader.setListener(new CustomDownloadListener());
        downloader.add(url, formats.toArray(new EmojiFormat[formats.size()]));
    }


    private class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onPostOneJsonDownload(String source, String destination) {
            super.onPostOneJsonDownload(source, destination);

            final ArrayList<JsonParam> emojies = JsonParam.readFromFile(new File(destination));
            final PreferenceManager searchManager = new PreferenceManager(context, PreferenceManager.Type.Search);
            for(JsonParam emoji : emojies)
            {
                searchManager.add(emoji.name);
            }
            searchManager.save();
            dismiss();
        }
    }
}
