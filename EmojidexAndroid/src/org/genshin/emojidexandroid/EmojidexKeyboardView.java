package org.genshin.emojidexandroid;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

/**
 * Created by nazuki on 14/01/08.
 */
public class EmojidexKeyboardView extends KeyboardView {
    private Context context;
    private LayoutInflater inflater;

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
        final int[] keyCodes = popupKey.codes;

        // create popup window
        View view = inflater.inflate(R.layout.popup, null);
        final PopupWindow popup = new PopupWindow(this);
        popup.setContentView(view);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.showAtLocation(this, Gravity.CENTER_HORIZONTAL, 0, -this.getHeight());

        // set emoji
        ImageView icon = (ImageView)view.findViewById(R.id.popup_image);
        icon.setImageDrawable(popupKey.icon);

        // set button's ClickListener
        Button yesButton = (Button)view.findViewById(R.id.popup_yes_button);
        yesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFavorites(keyCodes);
                Toast.makeText(context, R.string.register_favorite_done, Toast.LENGTH_LONG).show();
                popup.dismiss();
            }
        });
        Button noButton = (Button)view.findViewById(R.id.popup_no_button);
        noButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        return true;
    }

    public void saveFavorites(int[] keyCodes)
    {
        // TODO implemented saveFavorites
        for (int i = 0; i < keyCodes.length; i++)
        {
            Log.e("test", "code : " + Integer.toHexString(keyCodes[i]));
        }

    }
}
