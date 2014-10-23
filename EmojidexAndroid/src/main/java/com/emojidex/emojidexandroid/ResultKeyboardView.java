package com.emojidex.emojidexandroid;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.inputmethodservice.Keyboard;
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

/**
 * Created by nazuki on 14/01/08.
 */
public class ResultKeyboardView extends EmojidexKeyboardView {
    private Button downloadButton;

    /**
     * Construct EmojidexKeyboardView object.
     * @param context
     * @param attrs
     * @param defStyle
     * @param inflater
     */
    public ResultKeyboardView(Context context, AttributeSet attrs, int defStyle, LayoutInflater inflater) {
        super(context, attrs, defStyle, inflater);
    }

    /**
     * Behavior when long pressed.
     * @param popupKey
     * @return
     */
    @Override
    public boolean onLongPress(Keyboard.Key popupKey)
    {
        key = popupKey;
        emojiName = String.valueOf(key.popupCharacters);
        createPopupWindow();

        return true;
    }

    /**
     * Create PopupWindow.
     */
    @Override
    protected void createPopupWindow()
    {
        closePopup();

        // Create popup window.
        View view = inflater.inflate(R.layout.popup_download, null);
        popup = new PopupWindow(this);
        popup.setContentView(view);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.showAtLocation(this, Gravity.CENTER_HORIZONTAL, 0, -this.getHeight());

        // Set emoji data.
        TextView textView = (TextView)view.findViewById(R.id.download_name);
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
        ImageView icon = (ImageView)view.findViewById(R.id.popup_download_image);
        icon.setImageDrawable(key.icon);

        // Set register button.
        downloadButton = (Button)view.findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (FileOperation.saveEmoji(context, emojiName, key.icon))
                {
                    case FileOperation.SUCCESS :
                        Toast.makeText(context, context.getString(R.string.download_success), Toast.LENGTH_SHORT).show();
                        break;
                    case FileOperation.DONE :
                        Toast.makeText(context, context.getString(R.string.download_done), Toast.LENGTH_SHORT).show();
                        break;
                    case FileOperation.FAILURE :
                        Toast.makeText(context, context.getString(R.string.download_failure), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        // Set close button.
        Button closeButton = (Button)view.findViewById(R.id.download_close_button);
        closeButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                closePopup();
            }
        });
    }

    /**
     * Close popup window.
     */
    @Override
    public void closePopup()
    {
        if (popup != null)
        {
            popup.dismiss();
            popup = null;
        }
    }
}
