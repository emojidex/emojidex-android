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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by nazuki on 14/01/08.
 */
public class DownloadKeyboardView extends EmojidexKeyboardView {
    private Button downloadButton;

    /**
     * Construct EmojidexKeyboardView object.
     * @param context
     * @param attrs
     * @param defStyle
     * @param inflater
     */
    public DownloadKeyboardView(Context context, AttributeSet attrs, int defStyle, LayoutInflater inflater) {
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
        View view = inflater.inflate(R.layout.popup_favorite_download, null);
        popup = new PopupWindow(this);
        popup.setContentView(view);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.showAtLocation(this, Gravity.CENTER_HORIZONTAL, 0, -this.getHeight());

        // Set emoji data.
        TextView textView = (TextView)view.findViewById(R.id.favorite_download_name);
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
        ImageView icon = (ImageView)view.findViewById(R.id.favorite_download_image);
        icon.setImageDrawable(key.icon);

        // Set register button.
        imageButton = (ImageButton)view.findViewById(R.id.favorite_download_register_button);
        imageButton.setOnClickListener(createListener());

        // Set star(favorite) icon from current state.
        setCurrentState();

        // Set close button.
        Button closeButton = (Button)view.findViewById(R.id.favorite_download_close_button);
        closeButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                closePopup();
            }
        });

        // Set delete button.
        Button deleteButton = (Button)view.findViewById(R.id.favorite_download_delete_button);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopup();
                createConfirmWindow();
            }
        });
    }

    /**
     * Create popup window for confirm.
     */
    private void createConfirmWindow()
    {
        // Create popup window.
        View view = inflater.inflate(R.layout.popup_delete_download, null);
        final PopupWindow confirmWindow = new PopupWindow(this);
        confirmWindow.setContentView(view);
        confirmWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        confirmWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        confirmWindow.showAtLocation(this, Gravity.CENTER_HORIZONTAL, 0, -this.getHeight());

        // Set [Yes] button.
        Button yesButton = (Button)view.findViewById(R.id.delete_download_yes);
        yesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FileOperation.deleteEmoji(context, emojiName))
                    Toast.makeText(context, context.getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, context.getString(R.string.delete_failure), Toast.LENGTH_SHORT).show();
                confirmWindow.dismiss();
            }
        });

        // Set [No] button.
        Button noButton = (Button)view.findViewById(R.id.delete_download_no);
        noButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmWindow.dismiss();
                createPopupWindow();
            }
        });
    }
}
