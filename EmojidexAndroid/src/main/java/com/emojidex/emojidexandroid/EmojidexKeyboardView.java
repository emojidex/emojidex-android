package com.emojidex.emojidexandroid;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by nazuki on 14/01/08.
 */
public class EmojidexKeyboardView extends KeyboardView {
    protected Context context;
    protected LayoutInflater inflater;

    protected PopupWindow popup;
    protected Keyboard.Key key;

    protected TextView popupTextView;
    protected ImageView popupIcon;
    protected LinearLayout variantsLayout;
    protected String emojiName;
    protected EmojiFormat format;
    protected final float iconSize;

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

        format = EmojiFormat.toFormat(context.getResources().getString(R.string.emoji_format_key));
        iconSize = context.getResources().getDimension(R.dimen.ime_key_icon_size);
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
        popupTextView = (TextView)view.findViewById(R.id.favorite_name);
        popupTextView.setText(":" + key.popupCharacters + ":");
        popupTextView.setOnLongClickListener(new OnLongClickListener() {
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
        popupIcon = (ImageView)view.findViewById(R.id.popup_favorite_image);
        popupIcon.setImageDrawable(key.icon);

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

        // Set variants.
        variantsLayout = (LinearLayout) view.findViewById(R.id.favorite_variants);
        setVariants();
    }

    protected void setVariants()
    {
        final Emojidex emojidex = Emojidex.getInstance();
        Emoji emoji = emojidex.getEmoji(emojiName);

        if (emoji == null || emoji.getVariants().size() == 0 && emoji.getBase() == null) return;

        if (emoji.getBase() != null)
        {
            emoji = emojidex.getEmoji(emoji.getBase());
        }

        ArrayList<Emoji> variants = new ArrayList<>();
        if (emoji.getVariants().size() != 0)
        {
            variants.add(emoji);

            for (String name : emoji.getVariants())
            {
                Emoji variant = emojidex.getEmoji(name);
                variants.add(variant);
            }
        }

        variantsLayout.removeAllViews();
        for (final Emoji variant : variants)
        {
            ImageButton button = new ImageButton(context);
            button.setImageDrawable(variant.getDrawable(emojidex.getDefaultFormat()));
            button.setBackground(null);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] keyCodes = new int[variant.getCodes().size()];
                    for(int i = 0;  i < variant.getCodes().size(); i++)
                        keyCodes[i] = variant.getCodes().get(i);
                    // TODO: 正しい入力先が取得できない
                    closePopup();
                    EmojidexIME.currentInstance.changeKeyboard(variant);
//                    getOnKeyboardActionListener().onKey(variant.getCodes().get(0), keyCodes);
                }
            });
            button.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    changeEmoji(variant);
                    return true;
                }
            });
            variantsLayout.addView(button);
        }
    }

    protected void changeEmoji(Emoji emoji)
    {
        emojiName = emoji.getName();
        popupTextView.setText(":" + emojiName + ":");

        final BitmapDrawable icon = emoji.getDrawable(format);
        icon.setTargetDensity((int) (icon.getBitmap().getDensity() * iconSize / icon.getIntrinsicWidth()));
        popupIcon.setImageDrawable(icon);

        setCurrentState();
        setVariants();
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
