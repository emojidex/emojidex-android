package org.genshin.emojidexandroid;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
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
    private Context context;
    private LayoutInflater inflater;

    private PopupWindow popup;
    private List<Integer> keyCodes = new ArrayList<Integer>();
    private Keyboard.Key key;

    private ImageButton imageButton;
    private boolean register;

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
    protected void createPopupWindow()
    {
        closePopup();

        // create popup window
        View view = inflater.inflate(R.layout.popup_favorite, null);
        popup = new PopupWindow(this);
        popup.setContentView(view);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.showAtLocation(this, Gravity.CENTER_HORIZONTAL, 0, -this.getHeight());

        // set emoji data
        TextView textView = (TextView)view.findViewById(R.id.favorite_name);
        textView.setText(":" + key.popupCharacters + ":");
        textView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData.Item item = new ClipData.Item(":" + key.popupCharacters + ":");
                String[] mimeType = new String[1];
                mimeType[0] = ClipDescription.MIMETYPE_TEXT_PLAIN;
                ClipData data = new ClipData(new ClipDescription("text_data", mimeType), item);
                manager.setPrimaryClip(data);
                Toast.makeText(context, R.string.clipboard, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        ImageView icon = (ImageView)view.findViewById(R.id.popup_favorite_image);
        icon.setImageDrawable(key.icon);

        // register button
        imageButton = (ImageButton)view.findViewById(R.id.favorite_register_button);
        imageButton.setOnClickListener(createListener());
        if (FileOperation.searchFavorite(context, keyCodes))
        {
            imageButton.setImageResource(android.R.drawable.star_big_on);
            register = true;
        }
        else
        {
            imageButton.setImageResource(android.R.drawable.star_big_on);
            register = false;
        }

        // close button
        Button closeButton = (Button)view.findViewById(R.id.popup_close_button);
        closeButton.setOnClickListener(new OnClickListener()
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
    protected OnClickListener createListener()
    {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                // save or delete favorites
                if (register)
                {
                    FileOperation.delete(context, keyCodes);
                    imageButton.setImageResource(android.R.drawable.star_big_on);
                    register = false;
                }
                else
                {
                    FileOperation.save(context, keyCodes, FileOperation.FAVORITES);
                    imageButton.setImageResource(android.R.drawable.star_big_on);
                    register = true;
                }
//                switch (result) {
//                    case FileOperation.SUCCESS :
//                        Toast.makeText(context, R.string.register_success, Toast.LENGTH_SHORT).show();
//                        break;
//                    case FileOperation.DONE :
//                        Toast.makeText(context, R.string.register_done, Toast.LENGTH_SHORT).show();
//                        break;
//                    case FileOperation.FAILURE :
//                        Toast.makeText(context, R.string.register_failure, Toast.LENGTH_SHORT).show();
//                        break;
//                }
            }
        };
    }

    /**
     * close popup window
     */
    public void closePopup()
    {
        if (popup != null)
        {
            popup.dismiss();
            popup = null;
        }
    }
}
