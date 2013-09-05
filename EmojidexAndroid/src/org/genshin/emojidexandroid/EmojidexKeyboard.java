package org.genshin.emojidexandroid;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.util.DisplayMetrics;

import java.util.List;

/**
 * Created by kou on 13/08/26.
 */
public class EmojidexKeyboard extends Keyboard {
    private static int emojiCount;
    private static int rowCount;
    private static int columnCount = 0;

    private final Row row;

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
    }

    @Override
    protected Row createRowFromXml(Resources res, XmlResourceParser parser) {
        // Get display size.
        DisplayMetrics metrics = res.getDisplayMetrics();
        int displayWidth = metrics.widthPixels;

        // Set keyboard parameters.
        if(columnCount <= 0)
            columnCount = displayWidth / getKeyWidth();
        rowCount = emojiCount / columnCount + 1;

        // Create row.
        Row newRow = super.createRowFromXml(res, parser);
        newRow.defaultWidth = displayWidth;
        newRow.defaultHeight *= rowCount;

        return newRow;
    }

    /**
     * Add new key to keyboard.
     * @param emojiData
     */
    private void addKey(EmojiData emojiData)
    {
        List<Key> keys = getKeys();

        // Calculate key position.
        int keyCount = keys.size();
        int x = keyCount % columnCount * (row.defaultWidth + row.defaultHorizontalGap);
        int y = keyCount / columnCount * (row.defaultHeight + row.verticalGap);

        // Create new key and set parameters.
        Key newKey = new Key(row);

        newKey.codes = new int[]{ emojiData.getCode() };
        newKey.x = x;
        newKey.y = y;

        Drawable icon = emojiData.getIcon();
        if(icon != null)
            newKey.icon = emojiData.getIcon();
        else
            newKey.label = emojiData.getMoji();

        keys.add(newKey);
    }

    /**
     * Create EmojidexKeyboard object.
     * @param context
     * @param emojiDatas
     * @return      New EmojidexKeyboard object.
     */
    public static EmojidexKeyboard create(Context context, List<EmojiData> emojiDatas)
    {
        // Set keyboard parameters.
        emojiCount = emojiDatas.size();

        // Create keyboard.
        EmojidexKeyboard newKeyboard = new EmojidexKeyboard(context);

        // Initialize keyboard.

        // Key add test.
        for(EmojiData emojiData : emojiDatas)
        {
            newKeyboard.addKey(emojiData);
        }

        return newKeyboard;
    }
}
