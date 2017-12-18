package com.emojidex.emojidexandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by kou on 15/07/30.
 */
public class SealGenerator {
    private final Context context;

    private Uri uri = null;
    private boolean useLow = false;

    /**
     * Construct object.
     * @param context   Context.
     */
    public SealGenerator(Context context)
    {
        this.context = context;
    }

    /**
     * Generate seal.
     * @param emojiName     Emoji name.
     */
    public void generate(String emojiName)
    {
        generate(Emojidex.getInstance().getEmoji(emojiName));
    }

    /**
     * Generate seal.
     * @param emoji     Emoji.
     */
    public void generate(Emoji emoji)
    {
        // Error check.
        if(emoji == null)
        {
            uri = null;
            return;
        }

        // Create temporary file.
        createTemporaryFile(emoji);
    }

    /**
     * Get seal uri.
     * @return  Seal uri.
     */
    public Uri getUri()
    {
        return uri;
    }

    /**
     * Get flag of use low quality image.
     * @return  true if use low quality image.
     */
    public boolean useLowQuality()
    {
        return useLow;
    }

    /**
     * Create temporary file.
     * @param emoji     Emoji.
     */
    private void createTemporaryFile(Emoji emoji)
    {
        final Uri tmpUri = EmojidexFileUtils.getTemporaryUri(".png");
        useLow = false;

        // If file not found, use default format.
        EmojiFormat format = EmojiFormat.toFormat(context.getString(R.string.emoji_format_seal));
        if( !EmojidexFileUtils.existsLocalFile(EmojidexFileUtils.getLocalEmojiUri(emoji.getCode(), format)) )
        {
            format = EmojiFormat.toFormat(context.getString(R.string.emoji_format_default));
            useLow = true;
        }

        final Bitmap[] bitmaps = emoji.getBitmaps(format);

        // Create temporary file.
        try
        {
            final Bitmap bitmap = bitmaps[0];

            // Change background color to white.
            final Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            newBitmap.eraseColor(Color.WHITE);
            final Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(bitmap, 0, 0, null);

            // Save temporary file.
            final OutputStream os = context.getContentResolver().openOutputStream(tmpUri);
            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        }
        catch(IOException e)
        {
            uri = null;
            return;
        }

        uri = tmpUri;
    }
}
