package com.emojidex.emojidexandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Created by kou on 14/12/10.
 */
public class SearchDialog extends AbstractDialog {
    private final InputMethodManager inputMethodManager;

    /**
     * Construct object.
     * @param context       Context.
     */
    public SearchDialog(Context context) {
        super(context = context.getApplicationContext());

        // Initialize popup window.
        setInputMethodMode(INPUT_METHOD_FROM_FOCUSABLE);
        setContentView(createView(context));
    }

    @Override
    public void dismiss() {
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
