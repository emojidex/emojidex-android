package org.genshin.emojidexandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuInflater;
import android.widget.PopupMenu;

/**
 * Created by R on 14/01/08.
 */
public class EmojidexKeyboardView extends KeyboardView {
    private Context context;

    public EmojidexKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    public boolean onLongPress(android.inputmethodservice.Keyboard.Key popupKey)
    {
        PopupMenu popup = new PopupMenu(context, this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main, popup.getMenu());
        popup.show();

//        AlertDialog.Builder alert = new AlertDialog.Builder(context);
//        alert.setTitle(R.string.register_favorite_title);
//        alert.setMessage(R.string.register_favorite);
//        alert.setPositiveButton(R.string.yes,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Log.e("test", "yes");
//                    }
//                });
//        alert.setNegativeButton(R.string.no,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Log.e("test", "no");
//                    }
//                });
//
//        alert.show();

        return true;
    }
}
