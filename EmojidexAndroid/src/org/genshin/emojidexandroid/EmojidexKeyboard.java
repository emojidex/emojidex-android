package org.genshin.emojidexandroid;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.util.DisplayMetrics;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
     */
    private void addKey()
    {
        List<Key> keys = getKeys();

        // Calculate key position.
        int keyCount = keys.size();
        int x = keyCount % columnCount * (row.defaultWidth + row.defaultHorizontalGap);
        int y = keyCount / columnCount * (row.defaultHeight + row.verticalGap);

        Key newKey = new Key(row);

        newKey.codes = new int[]{ 'a' };
        newKey.label = null;
        newKey.icon = testIcon;
        newKey.x = x;
        newKey.y = y;

        keys.add(newKey);
    }

    /**
     * Create EmojidexKeyboard object.
     * @param context
     * @param emojiArray
     * @return      New EmojidexKeyboard object.
     */
    public static EmojidexKeyboard create(Context context, Object emojiArray)
    {
        // Set keyboard parameters.
        emojiCount = 42;

        // Create keyboard.
        EmojidexKeyboard newKeyboard = new EmojidexKeyboard(context);

        // Initialize keyboard.

        // Key add test.
        if(testIcon == null)    loadTestIcon(context.getResources());
        for(int i = 0;  i < emojiCount; ++i)
            newKeyboard.addKey();

        return newKeyboard;
    }

    private static Drawable testIcon;

    private static void loadTestIcon(Resources res)
    {
        try
        {
            InputStream is = res.getAssets().open("mdpi/anchor.png");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();
            testIcon = new BitmapDrawable(res, bitmap);
            testIcon.setBounds(0, 0, testIcon.getIntrinsicWidth(), testIcon.getIntrinsicHeight());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
