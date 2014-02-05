package org.genshin.emojidexandroid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

/**
 * Created by nazuki on 14/01/08.
 */
public class FavoriteKeyboardView extends EmojidexKeyboardView {
    private int stringRes = R.string.delete_favorite;

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
     * set text to TextView
     * @return
     */
    @Override
    protected int setTextViewText()
    {
        return stringRes;
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
