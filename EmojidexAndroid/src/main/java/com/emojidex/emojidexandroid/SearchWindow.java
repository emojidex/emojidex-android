package com.emojidex.emojidexandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * SearchWindow.java
 */
public class SearchWindow extends LinearLayout {
    private Context context;

    private EditText searchEditText;
    private LinearLayout resultLayout;

    /**
     * Constructor
     * @param context context
     */
    public SearchWindow(Context context) {
        super(context);
        this.context = context;

        // window layout.
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.window_search, null);
        addView(root);

        // Input area.
        searchEditText = (EditText) root.findViewById(R.id.search_edit_text);
        searchEditText.setFocusable(true);

        // Show result space.
        resultLayout = (LinearLayout) root.findViewById(R.id.search_result_layout);

        // Search button.
        ImageButton searchButton = (ImageButton) root.findViewById(R.id.search_action);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEmoji();
            }
        });

        // Window close button.
        ImageButton closeButton = (ImageButton) root.findViewById(R.id.search_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                dismiss();
            }
        });
    }

    /**
     * Search for emoji using the API.
     */
    public void searchEmoji() {
        String searchText = String.valueOf(searchEditText.getText());

        // TODO: 絵文字をAPIから検索して、結果をaddEmoji()でresultLayoutに配置する
        addEmoji(null);
    }

    /**
     * Add emoji.
     * @param emoji emoji data
     */
    public void addEmoji(Emoji emoji) {
        // TODO: 絵文字のデータをセット
        ImageView button = new ImageView(context);
        button.setImageResource(R.drawable.ic_launcher);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("test");
            }
        });
        resultLayout.addView(button);
    }
}
