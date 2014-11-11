package com.emojidex.emojidexandroid;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
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

import java.io.File;

/**
 * Created by nazuki on 14/01/08.
 */
public class EmojidexKeyboardView extends KeyboardView {
    protected Context context;
    protected LayoutInflater inflater;

    protected PopupWindow popup;
    protected Keyboard.Key key;
    protected String emojiName;

    protected ImageButton imageButton;
    protected boolean first;
    protected boolean registered;

    /**
     * Construct EmojidexKeyboardView object.
     * @param context
     * @param attrs
     * @param defStyle
     */
    public EmojidexKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
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
    protected void createPopupWindow()
    {
        closePopup();

        // Create popup window.
        View view = inflater.inflate(R.layout.popup_favorite, null);
        popup = new PopupWindow(this);
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setContentView(view);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.showAtLocation(this, Gravity.CENTER, 0, -this.getHeight());

        // Set emoji data.
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

        // Set register button.
        imageButton = (ImageButton)view.findViewById(R.id.favorite_register_button);
        imageButton.setOnClickListener(createListener());

        // Set star(favorite) icon from current state.
        setCurrentState();

        // Set close button.
        Button closeButton = (Button)view.findViewById(R.id.popup_close_button);
        closeButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                closePopup();
            }
        });

        // Set stamp send button.
        Button stampSendButton = (Button)view.findViewById(R.id.popup_stamp_send_button);
        stampSendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopup();

                // Get emoji data.
                final Emoji emoji = Emojidex.getInstance().getEmoji(emojiName);
                final String formatName = context.getResources().getString(R.string.emoji_format_stamp);
                final EmojiFormat format = EmojiFormat.toFormat(formatName);

                // Send intent.
                final File file = new File(emoji.getImageFilePath(format));
                final Uri uri = Uri.fromFile(file);
                final ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                final ActivityManager.RunningTaskInfo taskInfo =  am.getRunningTasks(1).get(0);

                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/png");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.setPackage(taskInfo.baseActivity.getPackageName());

                final Intent proxyIntent = new Intent(context, ProxyActivity.class);
                proxyIntent.putExtra(Intent.EXTRA_INTENT, intent);
                proxyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(proxyIntent);
            }
        });
    }

    /**
     * Create onClickListener.
     * @return
     */
    protected OnClickListener createListener()
    {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change icon.
                if (registered)
                {
                    imageButton.setImageResource(android.R.drawable.star_big_off);
                    registered = false;
                }
                else
                {
                    imageButton.setImageResource(android.R.drawable.star_big_on);
                    registered = true;
                }
            }
        };
    }

    /**
     * Set star(favorite) icon from current state.
     */
    protected void setCurrentState()
    {
        if (FileOperation.searchEmoji(context, FileOperation.FAVORITES, emojiName))
        {
            imageButton.setImageResource(android.R.drawable.star_big_on);
            first = true;
            registered = true;
        }
        else
        {
            imageButton.setImageResource(android.R.drawable.star_big_off);
            first = false;
            registered = false;
        }
    }

    /**
     * Close popup window.
     * @return  false if popup is not opened.
     */
    public boolean closePopup()
    {
        // If changed favorite state, save current state.
        if (registered != first)
        {
            if (registered)
                FileOperation.save(context, emojiName, FileOperation.FAVORITES);
            else
                FileOperation.delete(context, emojiName, FileOperation.FAVORITES);
        }

        if (popup != null)
        {
            popup.dismiss();
            popup = null;
            return true;
        }
        return false;
    }
}
