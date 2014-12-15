package com.emojidex.emojidexandroid;

import android.app.ActivityManager;
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

import java.util.List;

/**
 * Created by kou on 14/12/10.
 */
public class SearchDialog extends AbstractDialog {
    private final InputMethodManager inputMethodManager;
    private final String oldIME;

    /**
     * Construct object.
     * @param context       Context.
     */
    public SearchDialog(InputMethodService context, View view) {
        super(context);

        // Initialize popup window.
        setInputMethodMode(INPUT_METHOD_FROM_FOCUSABLE);
        setContentView(createView(context.getApplicationContext()));

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

    /**
     * Create view.
     * @return      View.
     */
    private View createView(Context context)
    {
        // window layout.
        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup root = (ViewGroup)inflater.inflate(R.layout.window_search, null);

        // Input area.
//        searchEditText = (EditText) root.findViewById(R.id.search_edit_text);
//        searchEditText.setFocusable(true);

        // Show result space.
//        resultLayout = (LinearLayout) root.findViewById(R.id.search_result_layout);

        // Search button.
        final ImageButton searchButton = (ImageButton) root.findViewById(R.id.search_action);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                searchEmoji();
            }
        });

        // Window close button.
        final ImageButton closeButton = (ImageButton) root.findViewById(R.id.search_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return root;
    }
}
