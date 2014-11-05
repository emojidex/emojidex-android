package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.BitmapDrawable;
import android.inputmethodservice.Keyboard;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.List;

/**
 * Created by kou on 14/10/29.
 */
public class newEmojidexKeyboard extends Keyboard {
    private static EmojiFormat EMOJI_FORMAT = null;

    private final Row row;
    private final float iconSize;

    private int displayWidth;
    private int columnCount;
    private int rowCount;

    /**
     * Construct object.
     * @param context      Application context.
     */
    private newEmojidexKeyboard(Context context)
    {
        super(context, R.xml.keyboard);

        if(EMOJI_FORMAT == null)
            EMOJI_FORMAT = EmojiFormat.toFormat(context.getResources().getString(R.string.emoji_format_stamp));

        getKeys().clear();

        row = new Row(this);
        row.defaultWidth = getKeyWidth();
        row.defaultHeight = getKeyHeight();
        row.defaultHorizontalGap = getHorizontalGap();
        row.verticalGap = getVerticalGap();
        row.rowEdgeFlags = 0;
        row.mode = 0;

        iconSize = context.getResources().getDimension(R.dimen.ime_key_icon_size);

        columnCount = Math.max(columnCount, 1);
        rowCount = Math.max(rowCount, 1);
    }

    /**
     * Initialize keyboard.
     * @param emojies   Emoji list.
     */
    public void initialize(List<Emoji> emojies)
    {
        reset();

        final int count = emojies.size();
        final List<Key> keys = getKeys();
        final int leftMargin = (displayWidth - columnCount * getKeyWidth() - (columnCount - 1) * getHorizontalGap()) / 2;
        for(int i = 0;  i < count;  ++i)
        {
            final Key newKey = createKey(emojies.get(i));

            newKey.x = i % columnCount * (getKeyWidth() + getHorizontalGap()) + leftMargin;
            newKey.y = i / columnCount * (getKeyHeight() + getVerticalGap());

            keys.add(newKey);
        }
    }

    /**
     * Reset keyboard.
     */
    public void reset()
    {
        getKeys().clear();
    }

    /**
     * Get key count max.
     * @return  Key count max.
     */
    public int getKeyCountMax()
    {
        return columnCount * rowCount;
    }

    @Override
    protected Row createRowFromXml(Resources res, XmlResourceParser parser)
    {
        final DisplayMetrics metrics = res.getDisplayMetrics();
        displayWidth = metrics.widthPixels;

        columnCount = displayWidth / (getKeyWidth() + getHorizontalGap());
        rowCount = res.getInteger(R.integer.ime_keyboard_row_count);

        final Row newRow = super.createRowFromXml(res, parser);
        newRow.defaultWidth = displayWidth;
        newRow.defaultHeight = rowCount * (getKeyHeight() + getVerticalGap());

        return newRow;
    }

    /**
     * Create key to keybaord.
     * @param emoji         Emoji.
     * @return              New key.
     */
    private Key createKey(Emoji emoji)
    {
        final Key newKey = new Key(row);

        final List<Integer> codes = emoji.getCodes();
        final int codesSize = codes.size();
        newKey.codes = new int[codesSize];
        for(int i = 0;  i < codesSize;  ++i)
            newKey.codes[i] = codes.get(i);

        final BitmapDrawable icon = emoji.getDrawable(EMOJI_FORMAT);
        icon.setTargetDensity((int)(icon.getBitmap().getDensity() * iconSize / icon.getIntrinsicWidth()));
        newKey.icon = icon;

        newKey.popupCharacters = emoji.name;

        return newKey;
    }

    /**
     * Create new keyboard.
     * @param context   Application context.
     * @return          New keyboard.
     */
    public static newEmojidexKeyboard create(Context context)
    {
        context = context.getApplicationContext();

        final newEmojidexKeyboard newKeyboard = new newEmojidexKeyboard(context);

        return newKeyboard;
    }
}
