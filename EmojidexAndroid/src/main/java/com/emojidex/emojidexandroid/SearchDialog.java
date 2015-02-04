package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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
    private final Handler handler;
    private final SaveDataManager saveDataManager;
    private final String oldIME;

    private View contentView;
    private EditText searchEditText;
    private GridLayout resultGridLayout;

    private String category = null;
    private LoadingDialog loadingDialog = null;

    /**
     * Construct object.
     * @param context       Context.
     */
    public SearchDialog(InputMethodService context) {
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

        // Initialize fields.
        saveDataManager = new SaveDataManager(this.context, SaveDataManager.Type.Search);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                addButton((String)msg.obj);
            }
        };


        //----- TEST
//        searchEditText.setText("face");
//        searchEmoji();
    }

    @Override
    public void dismiss()
    {
        saveDataManager.save();
        inputMethodManager.showInputMethodPicker();
        super.dismiss();
    }

    @Override
    protected View createContentView(Context context)
    {
        // window layout.
        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.window_search, null);

        // Input area.
        searchEditText = (EditText)contentView.findViewById(R.id.search_edit_text);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                if(actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    searchEmoji();
                    handled = true;
                }

                return handled;
            }
        });

        // Show result space.
        resultGridLayout = (GridLayout)contentView.findViewById(R.id.search_result_layout);

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
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        categorySpinner.setBackground(context.getResources().getDrawable(R.drawable.ime_search_spinner_background));

        return contentView;
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
        final String searchText = searchEditText.getText().toString();
        if(searchText.isEmpty())
            return;

        // Clear result.
        resultGridLayout.removeAllViews();

        // Search emoji.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
        }, 1000);

        // Create loading dialog.
        loadingDialog = new LoadingDialog(context);
        loadingDialog.showAtLocation(searchEditText, Gravity.CENTER, 0, 0);
    }

    /**
     * Add emoji button to result area.
     * @param emojiName     Emoji name.
     */
    private void addButton(final String emojiName)
    {
        final Emojidex emojidex = Emojidex.getInstance();
        final EmojiFormat emojiFormat = EmojiFormat.toFormat(context.getString(R.string.emoji_format_key));
        final ImageView button = new ImageView(context);
        button.setScaleType(ImageView.ScaleType.CENTER);

        // Set drawable.
        final BitmapDrawable drawable = emojidex.getEmoji(emojiName).getDrawable(emojiFormat);
        final float drawableSize = context.getResources().getDimension(R.dimen.ime_search_emoji_button_icon_size);
        drawable.setTargetDensity((int) (drawable.getBitmap().getDensity() * drawableSize / drawable.getIntrinsicWidth()));
        button.setImageDrawable(drawable);

        // Set size.
        final float buttonSize = context.getResources().getDimension(R.dimen.ime_search_emoji_button_size);
        final ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams((int)buttonSize, (int)buttonSize);
        button.setLayoutParams(lp);

        // Set click event.
        final Drawable background = context.getResources().getDrawable(R.drawable.ime_search_emoji_button_background_checked);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getBackground() == null)
                {
                    v.setBackground(background);
                    saveDataManager.addFirst(emojiName);
                }
                else
                {
                    v.setBackground(null);
                    saveDataManager.remove(emojiName);
                }
            }
        });

        // Add to grid layout.
        if(resultGridLayout.getChildCount() == 0)
        {
            resultGridLayout.setColumnCount((int)(resultGridLayout.getWidth() / buttonSize));
        }
        resultGridLayout.addView(button);
    }


    private class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onPostOneJsonDownload(String source, String destination) {
            super.onPostOneJsonDownload(source, destination);

            final File file = new File(destination);
            final ArrayList<JsonParam> emojies = JsonParam.readFromFile(file);
            for(JsonParam emoji : emojies)
            {
                // Convert emoji name.
                emoji.name = emoji.name.replaceAll(" ", "_");

                // Add emoji button to result space.
                final Message message = handler.obtainMessage();
                message.obj = emoji.name;
                handler.sendMessage(message);
            }
            JsonParam.writeToFile(file, emojies);
        }

        @Override
        public void onPostAllJsonDownload(EmojiDownloader downloader) {
            super.onPostAllJsonDownload(downloader);
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            final SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putString("startCategory", context.getString(R.string.ime_category_id_search));
            prefEditor.commit();
            loadingDialog.close();
            loadingDialog = null;
        }
    }
}
