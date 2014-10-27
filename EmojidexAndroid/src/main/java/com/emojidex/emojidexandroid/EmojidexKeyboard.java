package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.inputmethodservice.Keyboard;
import android.util.DisplayMetrics;

import com.emojidex.emojidexandroid2.Emoji;
import com.emojidex.emojidexandroid2.EmojiFormat;

import java.util.Collection;
import java.util.List;

/**
 * Created by kou on 13/08/26.
 */
public class EmojidexKeyboard extends Keyboard {
    private static int columnCount = 0;
    private static int minHeight = 0;

    private static final int ROW_COUNT = 3;
    private int displayWidth;

    private final Row row;
    private final float keyIconSize;

    /**
     * Construct EmojidexKeyboard object.
     * @param context
     */
    private EmojidexKeyboard(Context context) {
        super(context, R.xml.keyboard);

        getKeys().clear();

        row = new Row(this);
        row.defaultWidth = getKeyWidth();
        row.defaultHeight = getKeyHeight();
        row.defaultHorizontalGap = getHorizontalGap();
        row.verticalGap = getVerticalGap();
        row.rowEdgeFlags = 0;
        row.mode = 0;

        keyIconSize = context.getResources().getDimension(R.dimen.ime_key_icon_size);
    }

    @Override
    protected Row createRowFromXml(Resources res, XmlResourceParser parser)
    {
        // Get display size.
        DisplayMetrics metrics = res.getDisplayMetrics();
        displayWidth = metrics.widthPixels;

        // Set keyboard parameters.
        columnCount = displayWidth / (getKeyWidth() + getHorizontalGap());

        // Create row.
        Row newRow = super.createRowFromXml(res, parser);
        newRow.defaultWidth = displayWidth;
        newRow.defaultHeight = getKeyHeight() * ROW_COUNT;
        newRow.defaultHeight = Math.max(newRow.defaultHeight, minHeight);

        return newRow;
    }

    /**
     * Add new key to keyboard.
     * @param emoji
     * @param emojiFormat
     */
    private void addKey(Emoji emoji, EmojiFormat emojiFormat)
    {
        List<Key> keys = getKeys();

        // Calculate key position.
        int keyCount = keys.size();
        int leftMargin = (displayWidth - columnCount * getKeyWidth()
                                          - (columnCount - 1) * getHorizontalGap() ) / 2;
        int x = keyCount % columnCount * (row.defaultWidth + row.defaultHorizontalGap) + leftMargin;
        int y = keyCount / columnCount * (row.defaultHeight + row.verticalGap);

        // Create new key and set parameters.
        Key newKey = new Key(row);

        final List<Integer> codes = emoji.getCodes();
        final int size = codes.size();
        newKey.codes = new int[size];
        for(int i = 0;  i < size;  ++i)
            newKey.codes[i] = codes.get(i);

        newKey.x = x;
        newKey.y = y;

        final Drawable drawable = emoji.getDrawable(emojiFormat);
        final ScaleDrawable icon = new ScaleDrawable(drawable, 0 , 1.0f, 1.0f);
        final float scale = ((float)keyIconSize) / drawable.getIntrinsicWidth();
        icon.setBounds(
                0, 0,
                icon.getIntrinsicWidth(), icon.getIntrinsicHeight()
        );
        if(icon.setLevel((int)(10000 * scale)))
            icon.invalidateSelf();

        newKey.icon = icon;

        newKey.popupCharacters = emoji.getName();

        keys.add(newKey);
    }

    /**
     * Create CategorizedKeyboard object.
     * @param context
     * @param emojies
     * @param minHeight
     * @return      New CategorizedKeyboard object.
     */
    public static CategorizedKeyboard create(Context context, Collection<Emoji> emojies, int minHeight)
    {
        context = context.getApplicationContext();

        // Set keyboard parameters.
        int emojiCount = (emojies != null) ? emojies.size() : 0;
        EmojidexKeyboard.minHeight = minHeight;

        // Create keyboard.
        CategorizedKeyboard categorizedKeyboard = new CategorizedKeyboard();
        EmojidexKeyboard newKeyboard = new EmojidexKeyboard(context);

        // Create categorizedKeyboard's page.
        final EmojiFormat emojiFormat = EmojiFormat.toFormat(context.getResources().getString(R.string.emoji_format_default));
        final int keyCountMax = columnCount * 3;
        int count = 0;
        for(Emoji emoji : emojies)
        {
            if( count == keyCountMax )
            {
                categorizedKeyboard.setKeyboard(newKeyboard);
                newKeyboard = new EmojidexKeyboard(context);
                count = 0;
            }
            newKeyboard.addKey(emoji, emojiFormat);
            ++count;
        }
        categorizedKeyboard.setKeyboard(newKeyboard);

        return categorizedKeyboard;
    }
}
