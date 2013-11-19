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
    private static int columnCount = 0;
    private static int minHeight = 0;

    private static final int ROW_COUNT = 3;
    private int displayWidth;

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
     * @param emojiData
     */
    private void addKey(EmojiData emojiData)
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

        int len = emojiData.getCodes().length;
        newKey.codes = new int[len];
        newKey.codes = emojiData.getCodes();
        newKey.x = x;
        newKey.y = y;

        Drawable icon = emojiData.getIcon();
        if(icon != null)
            newKey.icon = icon;
        else
            newKey.label = emojiData.getMoji();

        keys.add(newKey);
    }

    /**
     * Create CategorizedKeyboard object.
     * @param context
     * @param emojiDatas
     * @param minHeight
     * @return      New CategorizedKeyboard object.
     */
    public static CategorizedKeyboard create(Context context, List<EmojiData> emojiDatas, int minHeight)
    {
        // Set keyboard parameters.
        int emojiCount = (emojiDatas != null) ? emojiDatas.size() : 0;
        EmojidexKeyboard.minHeight = minHeight;

        // Create keyboard.
        CategorizedKeyboard categorizedKeyboard = new CategorizedKeyboard();
        EmojidexKeyboard newKeyboard = new EmojidexKeyboard(context);

        // Create categorizedKeyboard's page.
        int num = 0;
        for(int i = 0; i < emojiCount; i++)
        {
            // Create new page.
            if (num == columnCount * 3)
            {
                categorizedKeyboard.setKeyboard(newKeyboard);
                newKeyboard = new EmojidexKeyboard(context);
                num = 0;
            }
            newKeyboard.addKey(emojiDatas.get(i));
            num++;
        }
        categorizedKeyboard.setKeyboard(newKeyboard);

        return categorizedKeyboard;
    }
}
