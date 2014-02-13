package org.genshin.emojidexandroid;

import android.content.Context;
import android.util.AttributeSet;
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
public class FavoriteKeyboardView extends EmojidexKeyboardView {

    /**
     * Construct EmojidexKeyboardView object.
     *
     * @param context
     * @param attrs
     * @param defStyle
     * @param inflater
     */
    public FavoriteKeyboardView(Context context, AttributeSet attrs, int defStyle, LayoutInflater inflater) {
        super(context, attrs, defStyle, inflater);
    }

    /**
     * create PopupWindow
     */
    @Override
    protected void createPopupWindow()
    {
        closePopup();

        // create popup window
        View view = inflater.inflate(R.layout.popup_delete, null);
        popup = new PopupWindow(this);
        popup.setContentView(view);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.showAtLocation(this, Gravity.CENTER_HORIZONTAL, 0, -this.getHeight());

        // set emoji
        ImageView icon = (ImageView)view.findViewById(R.id.popup_delete_image);
        icon.setImageDrawable(key.icon);

        // set button's ClickListener
        Button yesButton = (Button)view.findViewById(R.id.popup_yes_button);
        yesButton.setOnClickListener(createListener());
        Button noButton = (Button)view.findViewById(R.id.popup_no_button);
        noButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                closePopup();
            }
        });
    }

    /**
     * create onClickListener
     * @return
     */
    @Override
    protected OnClickListener createListener()
    {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete favorites
                boolean result = FileOperation.delete(context, keyCodes);
                if (result)
                    Toast.makeText(context, R.string.delete_success, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, R.string.delete_failure, Toast.LENGTH_SHORT).show();
                popup.dismiss();
            }
        };
    }
}
