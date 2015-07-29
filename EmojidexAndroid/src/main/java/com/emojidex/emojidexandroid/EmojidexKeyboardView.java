package com.emojidex.emojidexandroid;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;

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
        popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_blank));
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
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopup();
            }
        });

        // Set seal send button.
        Button sealSendButton = (Button)view.findViewById(R.id.popup_seal_send_button);
        sealSendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopup();

                // Send intent.
                final Intent proxyIntent = new Intent(context, SendSealActivity.class);
                proxyIntent.putExtra(Intent.EXTRA_TEXT, emojiName);
                proxyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(proxyIntent);
            }
        });

        ToolTipRelativeLayout toolTipRelativeLayout = (ToolTipRelativeLayout)view.findViewById(R.id.popup_tooltip1);
        ToolTip toolTip = new ToolTip()
                .withText("Send seal!")
                .withColor(Color.WHITE)
                .withShadow()
                .withAnimationType(ToolTip.AnimationType.FROM_TOP);
        ToolTipView myToolTipView = toolTipRelativeLayout.showToolTipForView(toolTip, sealSendButton);
        myToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
            @Override
            public void onToolTipViewClicked(ToolTipView toolTipView) {

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
