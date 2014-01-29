package org.genshin.emojidexandroid;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nazuki on 14/01/08.
 */
public class EmojidexKeyboardView extends KeyboardView {
    protected Context context;
    protected LayoutInflater inflater;

    protected PopupWindow popup;
    protected List<Integer> keyCodes = new ArrayList<Integer>();
    protected Keyboard.Key key;

    private int stringRes = R.string.register_favorite;

    /**
     * Construct EmojidexKeyboardView object.
     * @param context
     * @param attrs
     * @param defStyle
     * @param inflater
     */
    public EmojidexKeyboardView(Context context, AttributeSet attrs, int defStyle, LayoutInflater inflater) {
        super(context, attrs, defStyle);
        this.context = context;
        this.inflater = inflater;
    }

    /**
     * Behavior when long pressed
     * @param popupKey
     * @return
     */
    @Override
    public boolean onLongPress(android.inputmethodservice.Keyboard.Key popupKey)
    {
        keyCodes = new ArrayList<Integer>();
        key = popupKey;
        for (int code : key.codes)
        {
            keyCodes.add(code);
        }

        createPopupWindow();

        return true;
    }

    /**
     * create PopupWindow
     */
    private void createPopupWindow()
    {
        // create popup window
        View view = inflater.inflate(R.layout.popup, null);
        popup = new PopupWindow(this);
        popup.setContentView(view);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.showAtLocation(this, Gravity.CENTER_HORIZONTAL, 0, -this.getHeight());

        // set emoji
        ImageView icon = (ImageView)view.findViewById(R.id.popup_image);
        icon.setImageDrawable(key.icon);

        // set text
        TextView textView = (TextView)view.findViewById(R.id.popup_text);
        textView.setText(setTextViewText());

        // set button's ClickListener
        Button yesButton = (Button)view.findViewById(R.id.popup_yes_button);
        yesButton.setOnClickListener(createListener());
        Button noButton = (Button)view.findViewById(R.id.popup_no_button);
        noButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
    }

    /**
     * set text to TextView
     * @return
     */
    protected int setTextViewText()
    {
        return stringRes;
    }

    /**
     * create onClickListener
     * @return
     */
    protected OnClickListener createListener()
    {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                // save favorites
                int result = JsonDataOperation.save(context, keyCodes, JsonDataOperation.FAVORITES);
                switch (result) {
                    case JsonDataOperation.SUCCESS :
                        Toast.makeText(context, R.string.register_favorite_success, Toast.LENGTH_SHORT).show();
                        break;
                    case JsonDataOperation.DONE :
                        Toast.makeText(context, R.string.register_favorite_done, Toast.LENGTH_SHORT).show();
                        break;
                    case JsonDataOperation.FAILURE :
                        Toast.makeText(context, R.string.register_favorite_failure, Toast.LENGTH_SHORT).show();
                        break;
                }
                popup.dismiss();
            }
        };
    }
}
